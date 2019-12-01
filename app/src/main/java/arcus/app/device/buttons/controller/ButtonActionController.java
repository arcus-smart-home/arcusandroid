/*
 *  Copyright 2019 Arcus Project.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package arcus.app.device.buttons.controller;

import android.app.Activity;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import arcus.cornea.CorneaClientFactory;
import arcus.cornea.provider.RuleModelProvider;
import arcus.cornea.provider.RuleTemplateModelProvider;
import com.iris.capability.util.Addresses;
import com.iris.client.ClientEvent;
import com.iris.client.capability.Rule;
import com.iris.client.capability.RuleTemplate;
import com.iris.client.event.Listener;
import com.iris.client.model.DeviceModel;
import com.iris.client.model.RuleModel;
import com.iris.client.model.RuleTemplateModel;
import arcus.app.device.buttons.model.Button;
import arcus.app.device.buttons.model.ButtonAction;
import arcus.app.device.buttons.model.ButtonDevice;
import arcus.app.device.buttons.model.FobButton;
import arcus.app.device.buttons.model.FourButtonFobButtonAction;
import arcus.app.device.buttons.model.FourButtonGen3FobButtonAction;
import arcus.app.device.buttons.model.SmartButton;
import arcus.app.device.buttons.model.SmartButtonAction;
import arcus.app.device.buttons.model.TwoButtonFobButtonAction;
import arcus.cornea.utils.Listeners;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Controls the process of assigning an "action" to a key-fob button, pendant, or smart button.
 * This class encapsulates the business logic of determining which actions are available to each
 * device and mapping those actions to a platform rule.
 */
public class ButtonActionController {

    private final static Logger logger = LoggerFactory.getLogger(ButtonActionController.class);

    private final static int STATE_NOT_STARTED = 0;
    private final static int STATE_EDITING_DEVICE = 1;
    private final static int STATE_EDITING_BUTTON = 2;
    private final static int STATE_ASSIGNED_RULE = 3;

    private ButtonDevice selectedButtonDevice;
    private Button selectedButton;
    private String selectedDeviceAddress;
    @Nullable
    private ButtonAction currentAction;
    private int state = STATE_NOT_STARTED;

    private final Callback listener;
    private final Activity activity;

    public ButtonActionController(Activity activity, Callback listener) {
        this.activity = activity;
        this.listener = listener;
    }

    public ButtonActionController() {
        this.activity = null;
        this.listener = null;
    }

    /**
     * Begin the button action editing process for the identified device. If the device has two or
     * more buttons, the {@link Callback#onShowButtonSelector(Button[])}; callback will be fired,
     * otherwise, when only one button exists, the {@link Callback#editButton(Button)} method will
     * be invoked.
     *
     * @param device The device whose buttons are to be edited.
     * @throws IllegalArgumentException When the given device is not a ButtonDevice.
     */
    public void editButtonDevice(@NonNull final DeviceModel device) {
        state = STATE_EDITING_DEVICE;

        if (!ButtonDevice.isButtonDevice(device.getProductId())) {
            throw new IllegalArgumentException("Device is not a ButtonDevice that can be edited with this controller.");
        }

        selectedButtonDevice = ButtonDevice.fromProductId(device.getProductId());
        selectedDeviceAddress = device.getAddress();

        logger.debug("Editing button actions for device {}.", selectedButtonDevice);

        Button[] buttons = getButtonsForDevice(selectedButtonDevice);
        if (buttons.length > 1) {
            fireOnShowButtonSelector(buttons);
        } else {
            editButton(buttons[0]);
        }
    }

    /**
     * Begin the action editing process for a given button on the device being edited (i.e., the device
     * provided to {@link #editButtonDevice(DeviceModel)} ).
     * <p>
     * Must call {@link #editButtonDevice(DeviceModel)} before invoking this method.
     * <p>
     * This method:
     * 1. Attempts to fetch all the rules associated with the current place
     * 2. Determines the actions which can be assigned to the given button
     * 3. Determines if the given button is already participating in one or more
     * rules. If it participates in exactly one rule and that rule is identified by
     * an action available to this button, then that action will be the initial
     * selection in the list of available actions. Otherwise, the "default action"
     * will be selected (typically "Activate a rule" in the UI).
     * 4. Once the available actions and current action have been determined, the callback
     * {@link Callback#onShowButtonRuleEditor(ButtonAction[], ButtonAction)} is fired.
     *
     * @param button The button whose action should be edited.
     */
    public void editButton(@NonNull final Button button) {
        logger.debug("Editing button {} of device {}.", button, selectedButtonDevice);

        if (state < STATE_EDITING_DEVICE) {
            throw new IllegalStateException("Please call editButtonDevice() before editButton().");
        }
        state = STATE_EDITING_BUTTON;

        selectedButton = button;
        fireOnLoading();
        RuleModelProvider
                .instance()
                .getRules()
                .onSuccess(Listeners.runOnUiThread(initialRules -> {
                    if (initialRules == null) {
                        rulesLoaded(button, Collections.emptyList());
                    } else {
                        rulesLoaded(button, initialRules);
                    }
                }))
                .onFailure(Listeners.runOnUiThread(error -> {
                    logger.debug("Failed to load rules.", error);
                    fireOnError(error);
                }));
    }

    private void rulesLoaded(@NonNull final Button button, List<RuleModel> rules) {
        logger.debug("Loaded rules; got {} rule instances.", rules.size());

        ButtonAction[] assignableActions = getAssignableActionsForDevice(selectedButtonDevice);
        currentAction = getCurrentButtonAction(rules, selectedDeviceAddress, assignableActions, button);

        fireOnShowButtonRuleEditor(assignableActions, currentAction);
    }

    /**
     * Assigns the selected action to the current button (i.e., the button provided to
     * {@link #editButton(Button)}).
     * <p>
     * Must call {@link #editButton(Button)} before invoking this method.
     * <p>
     * If the action previously assigned to this button was not the default action, then the rule
     * associated with that action will be deleted before assigning the new, selected action.
     *
     * @param selectedAction
     */
    public void assignButtonAction(@NonNull final ButtonAction selectedAction) {
        logger.debug("Assigning action {} to button {} of device {}.", selectedAction, selectedButton, selectedButtonDevice);

        if (state < STATE_EDITING_BUTTON) {
            throw new IllegalStateException("Please call editButton() before assignButtonAction().");
        }
        state = STATE_ASSIGNED_RULE;

        // If the last assigned value wasn't the default action, then delete it
        if (!currentAction.isDefaultAction()) {
            deleteButtonAction(currentAction, new ButtonActionDeletionListener() {
                @Override
                public void onButtonActionDeleted() {
                    completeButtonActionAssignment(selectedAction);
                }
            });
        }

        else {
            completeButtonActionAssignment(selectedAction);
        }
    }

    private void completeButtonActionAssignment(@NonNull ButtonAction selectedAction) {

        // User selected an action other than the default, create the rule
        if (!selectedAction.isDefaultAction()) {
            applyButtonAction(selectedAction);
        }

        // User selected 'default action' (i.e., "Activate a rule"); nothing to do
        else {
            fireOnComplete();
        }

    }

    /**
     * Determines the current action assigned to the given button given a list of available actions.
     * If no rules/actions are assigned, or if more than one rule is assigned, the method returns
     * the first "default action" available in the list of available actions.
     *
     * @param rules
     * @param deviceAddress
     * @param availableActions
     * @param forButton
     * @return
     */
    @NonNull
    public ButtonAction getCurrentButtonAction(@NonNull List<RuleModel> rules, String deviceAddress, @NonNull ButtonAction[] availableActions, @NonNull Button forButton) {

        List<RuleModel> attachedRules = getRulesAttachedToButton(rules, deviceAddress, forButton);

        // Only one rule attached to this button...
        if (attachedRules.size() == 1) {

            // ... is it one of the known, basic user actions?
            ButtonAction action = getButtonActionForRule(attachedRules.get(0), availableActions);
            if (action != null) {
                return action;
            }

            // Only one rule attached, but it's a basic rule; use "Activate a rule" as selection
            return getDefaultAction(availableActions);
        }

        // No rules, or more than one rule assigned to this button; current selection should be the default
        // i.e., "Activate a rule"
        else {
            return getDefaultAction(availableActions);
        }
    }

    /**
     * Returns the set of ButtonActions that can be applied to a button on the given ButtonDevice.
     *
     * @param device
     * @return
     */
    @NonNull
    public ButtonAction[] getAssignableActionsForDevice(@NonNull ButtonDevice device) {

        // WARNING! These seemingly redundant ButtonAction[] casts are, in fact, not redundant.
        // Without them, the JIT compiler fails validation on and throws a VerifyError exception.
        // Stupid Android.

        switch (device) {
            case GEN3_FOUR_BUTTON_FOB:
                return (ButtonAction[]) FourButtonGen3FobButtonAction.values();

            case GEN2_FOUR_BUTTON_FOB:
                return (ButtonAction[]) FourButtonFobButtonAction.values();

            case GEN1_TWO_BUTTON_FOB:
                return (ButtonAction[]) TwoButtonFobButtonAction.values();

            case GEN1_SMART_BUTTON:
            case GEN2_SMART_BUTTON:
                return (ButtonAction[]) SmartButtonAction.values();

            default:
                throw new IllegalStateException("Bug! Button device not implemented: " + device);
        }
    }

    /**
     * Attempts to delete the rule associated with the given action from the selected device and
     * button.
     *
     * @param action
     */
    private void deleteButtonAction(@NonNull final ButtonAction action, @Nullable final ButtonActionDeletionListener listener) {
        logger.debug("Removing action {} from button {} of device {}.", action, selectedButton, selectedButtonDevice);

        RuleModelProvider
                .instance()
                .getRules()
                .onSuccess(Listeners.runOnUiThread(rules -> {
                    logger.debug("Loaded rules; got {} rule instances.", rules.size());

                    RuleModel rule = getRuleAttachedToAction(rules, action, selectedButton, selectedDeviceAddress);

                    if (rule != null) {

                        fireOnLoading();
                        rule.delete().onSuccess(new Listener<Rule.DeleteResponse>() {
                            @Override
                            public void onEvent(Rule.DeleteResponse deleteResponse) {
                                logger.debug("Successfully removed action rule {} from button {} of device {}.", action, selectedButton, selectedButtonDevice);
                                if (listener != null) {
                                    listener.onButtonActionDeleted();
                                }
                            }
                        }).onFailure(new Listener<Throwable>() {
                            @Override
                            public void onEvent(Throwable throwable) {
                                logger.debug("An error occured removing action {} from button {} of device {}.", action, selectedButton, selectedButtonDevice);
                                fireOnError(throwable);
                            }
                        });
                    } else {
                        logger.error("Failed to remove action {} from button {} of device {} because the action could not be resolved to a rule. This button may have multiple actions attached to it.", action, selectedButton, selectedButtonDevice);
                    }
                }))
                .onFailure(Listeners.runOnUiThread(throwable -> {
                    logger.error("Failed to load rules due to: {}. Button action not deleted; this button may have multiple actions attached to it.", throwable.getMessage());
                    fireOnError(throwable);
                }));
    }

    /**
     * Attempts to apply the selected action to the selected device and button. Fetches the
     * RuleTemplateModel associated with the action and, when successful, invokes
     * {@link #applyButtonActionAsRule(ButtonAction, RuleTemplateModel)}.
     *
     * @param selectedAction
     */
    private void applyButtonAction(@NonNull final ButtonAction selectedAction) {
        logger.debug("Applying action {} to button {} of device {}", selectedAction, selectedButton, selectedButtonDevice);

        // Nothing to do if selected action is the default action
        if (selectedAction.isDefaultAction()) {
            return;
        }

        fireOnLoading();

        logger.debug("Fetching RuleTemplateModel {} for button action {}.", selectedAction.getRuleTemplateId(), selectedAction);
        RuleTemplateModelProvider.instance().getModel(Addresses.toObjectAddress(RuleTemplate.NAMESPACE, selectedAction.getRuleTemplateId())).reload().onSuccess(new Listener<RuleTemplateModel>() {
            @Override
            public void onEvent(@NonNull RuleTemplateModel ruleTemplateModel) {
                logger.debug("Successfully loaded rule template model {} for button action {}.", selectedAction.getRuleTemplateId(), selectedAction);
                applyButtonActionAsRule(selectedAction, ruleTemplateModel);
            }
        }).onFailure(new Listener<Throwable>() {
            @Override
            public void onEvent(Throwable throwable) {
                logger.debug("An error occured loading RuleTemplateModel {} for button action {}.", selectedAction.getRuleTemplateId(), selectedAction);
                fireOnError(throwable);
            }
        });
    }

    /**
     * Attempts to instantiate a rule of the given RuleTemplateModel for the selected action. Fires
     * the callback {@link Callback#onComplete()} when successful or {@link Callback#onError(Throwable)}
     * if not.
     *
     * @param selectedAction
     * @param ruleTemplateModel
     */
    private void applyButtonActionAsRule(@NonNull final ButtonAction selectedAction, @NonNull final RuleTemplateModel ruleTemplateModel) {
        logger.debug("Applying action {} as rule instance of RuleTemplateModel {}.", selectedAction, selectedAction.getRuleTemplateId());

        // Create a new rule for this device
        RuleTemplate.CreateRuleRequest request = new RuleTemplate.CreateRuleRequest();
        request.setAddress(Addresses.toObjectAddress(RuleTemplate.NAMESPACE, selectedAction.getRuleTemplateId()));
        request.setPlaceId(CorneaClientFactory.getClient().getActivePlace().toString());
        request.setName(ruleTemplateModel.getName());
        request.setDescription(ruleTemplateModel.getDescription());

        // Fill in the template parameters (selectors)
        Map<String, Object> selectors = new HashMap<>();
        selectors.put(selectedAction.getDeviceAddressArgumentName(), selectedDeviceAddress);

        // If action doesn't specify a button identifier, then one isn't required
        if (selectedAction.getButtonIdArgumentName() != null) {
            selectors.put(selectedAction.getButtonIdArgumentName(), selectedButton.toString().toLowerCase());
        }
        request.setContext(selectors);

        fireOnLoading();
        CorneaClientFactory.getClient().request(request)
                .onSuccess(new Listener<ClientEvent>() {
                    @Override
                    public void onEvent(ClientEvent clientEvent) {
                        logger.debug("Successfully created rule for action {} for button {} on device {}.", selectedAction, selectedButton, selectedButtonDevice);
                        currentAction = selectedAction;
                        fireOnComplete();
                    }
                })
                .onFailure(new Listener<Throwable>() {
                    @Override
                    public void onEvent(Throwable throwable) {
                        logger.debug("An error occurred creating rule for action {} for button {} on device {}.", selectedAction, selectedButton, selectedButtonDevice);
                        fireOnError(throwable);
                    }
                });
    }

    /**
     * Determines the ButtonAction associated with a given a RuleModel, or null if the rule
     * is not identified by any action.
     *
     * @param rule
     * @param actions
     * @return
     */
    @Nullable
    private ButtonAction getButtonActionForRule(@NonNull RuleModel rule, @NonNull ButtonAction[] actions) {
        String template = rule.getTemplate();
        for (ButtonAction thisAction : actions) {
            String ruleTemplateID = thisAction.getRuleTemplateId();
            if (ruleTemplateID != null) {
                if (ruleTemplateID.equalsIgnoreCase(template))
                    return thisAction;
            }
        }
        // Rule is not in the list of known actions (means the user has assigned the rule outside
        // of the button rule editor)
        return null;
    }

    /**
     * Returns the first ButtonAction that identifies itself as a default action. The set of
     * provided actions must have at least one default action.
     *
     * @param actions
     * @return
     */
    @NonNull
    private ButtonAction getDefaultAction(@NonNull ButtonAction[] actions) {
        for (ButtonAction thisAction : actions) {
            if (thisAction.isDefaultAction()) {
                return thisAction;
            }
        }

        throw new IllegalStateException("Bug! No default action defined in " + Arrays.toString(actions));
    }

    /**
     * Given a list of rules, determines the rule attached to the provided ButtonAction for the given
     * button and device.
     *
     * @param rules
     * @param action
     * @param button
     * @param deviceAddress
     * @return
     */
    private RuleModel getRuleAttachedToAction(@NonNull List<RuleModel> rules, @NonNull ButtonAction action, @NonNull Button button, String deviceAddress) {

        List<RuleModel> buttonAttachedRules = getRulesAttachedToButton(rules, deviceAddress, button);
        logger.debug("Looking for rules attached associated with action {} for button {} on device {}; {} rules attached to button", action, button, deviceAddress, buttonAttachedRules.size());

        for (RuleModel thisRule : buttonAttachedRules) {
            if (thisRule.getTemplate().equalsIgnoreCase(action.getRuleTemplateId())) {
                return thisRule;
            }
        }

        return null;
    }

    /**
     * Given a list of rules, returns those that the given device is participating in.
     *
     * @param rules
     * @param deviceAddress
     * @return
     */
    @NonNull
    private List<RuleModel> getRulesAttachedToDevice(@NonNull List<RuleModel> rules, final String deviceAddress) {

        List<RuleModel> associatedRules = new ArrayList<>();

        // For each active rule...
        for (RuleModel thisRule : rules) {

            // Walk through the value assigned to each template field
            for (Object thisContextValue : thisRule.getContext().values()) {

                // And if it matches this device, we've got a wiener!
                if (thisContextValue.toString().equalsIgnoreCase(deviceAddress)) {
                    associatedRules.add(thisRule);
                }
            }
        }

        logger.debug("Found {} for rules attached to device {}; {} rules in total.", associatedRules.size(), deviceAddress, rules.size());
        return associatedRules;
    }

    /**
     * Given a list of rules, returns those that the provided device and button are participating in.
     *
     * @param rules
     * @param deviceAddress
     * @param button
     * @return
     */
    @NonNull
    private List<RuleModel> getRulesAttachedToButton(@NonNull List<RuleModel> rules, String deviceAddress, @NonNull Button button) {

        List<RuleModel> buttonAttachedRules = new ArrayList<>();
        List<RuleModel> deviceAttachedRules = getRulesAttachedToDevice(rules, deviceAddress);

        // Don't need to filter device rules on the basis of button when device has only one button
        if (button.isSingleton()) {
            return deviceAttachedRules;
        }

        // Walk through each rule attached to this device
        for (RuleModel thisRule : deviceAttachedRules) {

            // Then, for each rule, walk through the template parameter values
            for (Object thisValue : thisRule.getContext().values()) {

                // And if they match this button name, then assume the rule is associated with the button
                if (thisValue.toString().equalsIgnoreCase(button.getButtonName())) {
                    buttonAttachedRules.add(thisRule);
                }
            }
        }

        logger.debug("Found {} rules attached to button {} of device {}; {} rules attached to device.", buttonAttachedRules.size(), button, deviceAddress, deviceAttachedRules.size());
        return buttonAttachedRules;
    }

    /**
     * Returns the set of buttons on the given ButtonDevice (i.e., home/away, circle/square/diamond/hex).
     *
     * @param device
     * @return
     */
    @NonNull
    private Button[] getButtonsForDevice(@NonNull ButtonDevice device) {

        // WARNING! These seemingly redundant Button[] casts are, in fact, not redundant.
        // Without them, the JIT compiler fails validation on and throws a VerifyError exception.
        // Stupid Android.

        switch (device) {
            case GEN3_FOUR_BUTTON_FOB:
                return (Button[]) FobButton.constructGen3FourButtonFob();

            case GEN2_FOUR_BUTTON_FOB:
                return (Button[]) FobButton.constructFourButtonFob();

            case GEN1_TWO_BUTTON_FOB:
                return (Button[]) FobButton.constructTwoButtonFob();

            case GEN1_SMART_BUTTON:
            case GEN2_SMART_BUTTON:
                return (Button[]) SmartButton.values();

            default:
                throw new IllegalStateException("Bug! Button device not implemented: " + device);
        }
    }

    private void fireOnShowButtonSelector(final Button[] buttons) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                listener.onShowButtonSelector(buttons);
            }
        });
    }

    private void fireOnShowButtonRuleEditor(final ButtonAction[] actions, final ButtonAction currentSelection) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                listener.onShowButtonRuleEditor(actions, currentSelection);
            }
        });
    }

    private void fireOnError(final Throwable reason) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                listener.onError(reason);
            }
        });
    }

    private void fireOnLoading() {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                listener.onLoading();
            }
        });
    }

    private void fireOnComplete() {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                listener.onComplete();
            }
        });
    }

    private interface ButtonActionDeletionListener {
        void onButtonActionDeleted();
    }

    public interface Callback {
        void onShowButtonSelector(Button[] buttons);

        void onShowButtonRuleEditor(ButtonAction[] actions, ButtonAction currentSelection);

        void onLoading();

        void onError(Throwable reason);

        void onComplete();
    }
}

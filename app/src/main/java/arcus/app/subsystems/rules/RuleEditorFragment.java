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
package arcus.app.subsystems.rules;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import arcus.cornea.controller.RuleEditorController;
import arcus.cornea.events.TimeSelectedEvent;
import arcus.cornea.model.RuleDisplayModel;
import arcus.cornea.model.RuleEditorCallbacks;
import arcus.cornea.model.StringPair;
import arcus.cornea.model.TemplateTextField;
import arcus.app.R;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.error.ErrorManager;
import arcus.app.common.error.type.RuleErrorType;
import arcus.app.common.events.ButtonSelected;
import arcus.app.common.fragments.BaseFragment;
import arcus.app.common.image.ImageManager;
import arcus.app.common.image.picasso.transformation.AlphaPreset;
import arcus.app.common.models.ListItemModel;
import arcus.app.common.models.RegistrationContext;
import arcus.app.common.popups.AMPMTimePopupWithHeader;
import arcus.app.common.popups.DurationPickerPopup;
import arcus.app.common.popups.MultiModelPopup;
import arcus.app.common.popups.TupleSelectorPopup;
import arcus.app.common.popups.ScheduleNewRulePopup;
import arcus.app.common.utils.ActivityUtils;
import arcus.app.common.view.Version1Button;

import arcus.app.dashboard.AddMenuFragment;
import arcus.app.subsystems.rules.schedule.RulesWeeklyScheduleFragment;
import arcus.app.subsystems.rules.schedule.model.RulesCommand;
import arcus.app.subsystems.rules.views.OnTemplateFieldClickListener;
import arcus.app.subsystems.rules.views.RuleTemplateView;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;

import de.greenrobot.event.EventBus;


public class RuleEditorFragment extends BaseFragment implements RuleEditorCallbacks, OnTemplateFieldClickListener, ScheduleNewRulePopup.Callback {

    public static final String RULE_ID_ARG = "rule-template-id";
    public static final String RULE_NAME_ARG = "rule-template-name";
    public static final String EXISTING_RULE_ADDRESS = "existing-rule-address";

    protected static final Logger logger = LoggerFactory.getLogger(RuleEditorFragment.class);

    protected RuleTemplateView templateView;
    protected TextView rulesEditability;
    protected LinearLayout satisfiableEditList;
    protected LinearLayout editRuleNameLayout;
    protected TextView ruleNameAbstract;
    protected LinearLayout ruleRepeatLayout;
    protected Version1Button saveButton;

    protected RuleEditorController controller;


    @NonNull
    public static RuleEditorFragment newInstance(String ruleTemplateName, String ruleTemplateId) {
        RuleEditorFragment instance = new RuleEditorFragment();
        Bundle arguments = new Bundle();
        arguments.putString(RULE_ID_ARG, ruleTemplateId);
        arguments.putString(RULE_NAME_ARG, ruleTemplateName);
        instance.setArguments(arguments);

        return instance;
    }

    @NonNull
    public static RuleEditorFragment newInstance(String ruleTemplateName, String ruleTemplateId, String existingRuleAddress) {
        RuleEditorFragment instance = new RuleEditorFragment();
        Bundle arguments = new Bundle();
        arguments.putString(RULE_ID_ARG, ruleTemplateId);
        arguments.putString(RULE_NAME_ARG, ruleTemplateName);
        arguments.putString(EXISTING_RULE_ADDRESS, existingRuleAddress);
        instance.setArguments(arguments);

        return instance;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        templateView = (RuleTemplateView) view.findViewById(R.id.rule_template);
        rulesEditability = (TextView) view.findViewById(R.id.rules_editability);
        satisfiableEditList = (LinearLayout) view.findViewById(R.id.satisfiable_rules_edit_list);

        editRuleNameLayout = (LinearLayout) view.findViewById(R.id.rule_name_layout);
        ruleNameAbstract = (TextView) view.findViewById(R.id.rule_name_abstract);

        ruleRepeatLayout = (LinearLayout) view.findViewById(R.id.rule_repeat_layout);
        saveButton = (Version1Button) view.findViewById(R.id.save_button);

        setHasOptionsMenu(true);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        controller = RuleEditorController.getInstance();

        getActivity().setTitle(getTitle());
        hideProgressBar();

        // Transition to rule name editor when user clicks the link
        editRuleNameLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BackstackManager.getInstance().navigateToFragment(RuleNameEditorFragment.newInstance(getTitle()), true);
            }
        });


        ruleRepeatLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BackstackManager.getInstance().navigateToFragment(RuleRepeatEditorFragment.newInstance(), true);
            }
        });

        populateRuleTemplate();

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

        RegistrationContext registrationContext = RegistrationContext.getInstance();
        if(registrationContext.getPlaceModel() != null){
            ImageManager.with(getActivity())
                    .putPlaceImage(registrationContext.getPlaceModel().getId())
                    .intoWallpaper(AlphaPreset.LIGHTEN)
                    .execute();
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    public boolean onBackPressed() {
        FrameLayout view = (FrameLayout) getActivity().findViewById(R.id.floating);
        // Check to see if a floating fragment is showing, if so, ignore the back press
        // If not, if we're the visible fragment the user should be going back to the previous screen we want to reset
        // the editor.  If we're not visible it's possible they came from the title or other pages of rule setup after
        // this one.
        if (view == null) { // Danger Will Robinson, where are we - this view should never be MIA!
            controller.reset();
        } else if (view.getChildCount() == 0 && isVisible()) {
            controller.reset();
        }
        return super.onBackPressed();
    }

    private void populateRuleTemplate() {
        final String ruleId = getArguments().getString(RULE_ID_ARG);
        logger.debug("Populating rule template for rule template id {}", ruleId);
        String existingRule = getArguments() != null ? getArguments().getString(EXISTING_RULE_ADDRESS) : null;
        controller.select(ruleId, existingRule, this);
    }

    @Nullable
    @Override
    public String getTitle() {
        if (!Strings.isNullOrEmpty(controller.getTitle())) {
            return controller.getTitle();
        } else {
            return getArguments().getString(RULE_NAME_ARG);
        }
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_rule_editor;
    }

    /**
     * Controller indicates that template is unsatisfiable and we should modify our layout
     * accordingly
     */
    @Override
    public void showUnavailable(@NonNull RuleDisplayModel model) {
        logger.debug("Rule template is not satisfiable; adjusting layout accordingly.");
        //  ruleDisplayModel = model;
        templateView.setRuleTemplate(model.getTemplateTextFields(), this);
        templateView.setEnabled(false);

        satisfiableEditList.setVisibility(View.GONE);
        rulesEditability.setText(getString(R.string.rules_add_addl_device));

        saveButton.setText(R.string.rules_shop_now);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityUtils.launchShopNow();
            }
        });

        hideProgressBar();
    }

    @Override
    public void showEditable(@NonNull final RuleDisplayModel model) {
        logger.debug("Rule template is satisfiable; adjusting layout accordingly.");

        // ruleDisplayModel = model;

        templateView.setRuleTemplate(model.getTemplateTextFields(), this);
        satisfiableEditList.setVisibility(View.VISIBLE);
        rulesEditability.setText(getString(R.string.rules_tap_to_edit));

        ruleNameAbstract.setText(getTitle());

        saveButton.setText(R.string.rules_name_save);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (model.allFieldsEdited()) {
                    controller.setDescription(templateView.getText().toString());
                    controller.save();
                } else {
                    ErrorManager.in(getActivity()).show(RuleErrorType.NOT_EDITED);
                }
            }
        });

        hideProgressBar();
    }

    @Override
    public void showModelListSelector(Collection<String> identifiers) {
        List<String> optionsList = Lists.newLinkedList(identifiers);
        List<String> selected = Lists.newArrayList(controller.getSelectedValue());

        MultiModelPopup popup = MultiModelPopup.newInstance(optionsList, R.string.choose_an_option_text, selected, false);
        popup.setCallback(new MultiModelPopup.Callback() {
            @Override
            public void itemSelectedAddress(ListItemModel itemModel) {
                controller.set(itemModel.getAddress());
            }
        });
        BackstackManager.getInstance().navigateToFloatingFragment(popup, popup.getClass().getCanonicalName(), true);
    }

    @Override
    public void showTimeRangeSelector() {
        AMPMTimePopupWithHeader picker = AMPMTimePopupWithHeader.newInstanceWithStartEnd();
        BackstackManager.getInstance().navigateToFloatingFragment(picker, picker.getClass().getCanonicalName(), true);
    }

    @Override
    public void showDayOfWeekSelector() {
        logger.debug("Controller requested day-of-week selector; showing popup.");
    }

    @Override
    public void showTimeOfDaySelector() {
        AMPMTimePopupWithHeader picker = AMPMTimePopupWithHeader.newInstanceAsTimeOnly();
        BackstackManager.getInstance().navigateToFloatingFragment(picker, picker.getClass().getCanonicalName(), true);
    }

    @Override
    public void showDurationSelector() {
        DurationPickerPopup picker = DurationPickerPopup.newInstance("", new DurationPickerPopup.SelectionListener() {
            @Override
            public void timeSelected(Integer leftValue, Integer rightValue) {
                if (leftValue != 0 || rightValue != 0) {
                    controller.set(leftValue + ":" + (rightValue < 10 ? "0" + rightValue : rightValue));
                }
            }

            @Override
            public void indefiniteSelected() {
                controller.set("0");
            }
        });

        BackstackManager.getInstance().navigateToFloatingFragment(picker, picker.getClass().getCanonicalName(), true);
    }

    @Override
    public void onTemplateFieldClicked(@NonNull TemplateTextField field) {
        controller.edit(field.getFieldName());
    }

    @Override
    public void showTupleEditor(final List<StringPair> values) {
        TupleSelectorPopup popup = TupleSelectorPopup.newInstance(values, R.string.choose_button_action_text, controller.getSelectedValue());
        popup.setCallback(new TupleSelectorPopup.Callback() {
            @Override
            public void selectedItem(StringPair selected) {
                int index = values.indexOf(selected);
                if (index != -1) {
                    controller.set(values.get(index).getKey());
                }
            }
        });
        BackstackManager.getInstance().navigateToFloatingFragment(popup, popup.getClass().getCanonicalName(), true);
    }

    @Override
    public void showLoading() {
        hideProgressBar();
        showProgressBar();
    }

    @Override
    public void saveSuccess() {
        hideProgressBar();


    }

    @Override
    public void errorOccurred(Throwable throwable) {
        hideProgressBar();
        ErrorManager.in(getActivity()).showGenericBecauseOf(throwable);
    }

    @Override
    public void showTextEditor() {
        logger.debug("showTextEditor selected");
    }

    @Override
    public void showScheduleDialog() {


        ScheduleNewRulePopup ruleChooseActivePopup = ScheduleNewRulePopup.newInstance(RulesCommand.State.INACTIVE);
        ruleChooseActivePopup.setCallback(this);
        BackstackManager.getInstance().navigateToFloatingFragment(ruleChooseActivePopup, ruleChooseActivePopup.getClass().getSimpleName(), true);


    }

    @Override
    public void allowScheduling(String state, String strModelAddress, String strModelName) {


        BackstackManager.getInstance().navigateBack();
        BackstackManager.getInstance().navigateBack();
        if (BackstackManager.getInstance().isFragmentOnStack(AddMenuFragment.class)) {
            BackstackManager.getInstance().navigateBackToFragment(AddMenuFragment.class);
        }

        BackstackManager.getInstance().navigateToFragment(RulesWeeklyScheduleFragment.newInstance(strModelAddress, strModelName), true);
    }

    @Override
    public void activeInactiveSelected(RulesCommand.State state) {
        // mStateOfCreatedRule = state;
        controller.createNewRule(state == RulesCommand.State.ACTIVE ? "active" : "inactive");
    }


    public void onEvent(@NonNull ButtonSelected buttonSelected) {
        controller.set(buttonSelected.getButtonValue());
    }

    public void onEvent(TimeSelectedEvent event) {
        controller.set(event);
    }
}

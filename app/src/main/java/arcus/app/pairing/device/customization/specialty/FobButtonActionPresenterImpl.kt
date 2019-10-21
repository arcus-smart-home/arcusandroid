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
package arcus.app.pairing.device.customization.specialty

import arcus.cornea.CorneaClientFactory
import arcus.cornea.presenter.BasePresenterContract
import arcus.cornea.presenter.KBasePresenter
import arcus.cornea.provider.DeviceModelProvider
import arcus.cornea.provider.RuleModelProvider
import arcus.cornea.provider.RuleTemplateModelProvider
import arcus.cornea.utils.Listeners
import com.iris.capability.util.Addresses
import com.iris.client.ClientEvent
import com.iris.client.capability.Rule
import com.iris.client.capability.RuleTemplate
import com.iris.client.event.Listener
import com.iris.client.model.DeviceModel
import com.iris.client.model.RuleModel
import com.iris.client.model.RuleTemplateModel
import arcus.app.device.buttons.controller.ButtonActionController
import arcus.app.device.buttons.model.*
import org.slf4j.LoggerFactory
import java.util.*

interface FobButtonActionView {

    /**
     * Called to display the list of button actions that can be assigned to the a button.
     *
     *  @param buttonActions: The list of Button Actions.
     *  @param currentActionIndex: The index of the current of Button Action.
     */
    fun onButtonsLoaded(buttonActions: List<ButtonAction>, currentActionIndex: Int)

    /**
     * Called when a the selected Button Action has been saved to the Platform.
     *
     */
    fun onButtonActionSaved()

    /**
     * Called when a Exception is thrown by the Presenter for the View to handle
     *
     *  @param throwable: Throwable Exception
     */
    fun showError(throwable: Throwable)



}

interface FobButtonActionPresenter : BasePresenterContract<FobButtonActionView> {

    /**
     * Loads the Fob Button device model and get the available Button Actions.
     *
     * @param deviceAddress: Address of the device containing the buttons.
     */
    fun loadFromDeviceAddress(deviceAddress: String, buttonName: String)

    /**
     * Sets the Button Action for the selected Fon Button.
     *
     * @param buttonName: Name of the Fob Button that will have Action changed.
     * @param action: Sets the selected Button Action to the Fob Button.
     */
    fun setButtonSelection(buttonName: String, newAction: ButtonAction)
}

class FobButtonActionPresenterImpl: FobButtonActionPresenter, KBasePresenter<FobButtonActionView>() {

    private val buttonActionController = ButtonActionController()
    private val errorListener = Listeners.runOnUiThread<Throwable> { error ->
        onlyIfView { view ->
            view.showError(error)
        }
    }

    private var deviceModel: DeviceModel? = null
    private var currentActionIndex: Int = 0
    private lateinit var buttonsList: List<Button>
    private lateinit var selectedButton: Button
    private lateinit var selectedDeviceAddress: String
    private lateinit var currentAction: ButtonAction

    override fun loadFromDeviceAddress(deviceAddress: String, buttonName: String) {
        selectedDeviceAddress = deviceAddress
        DeviceModelProvider
                .instance()
                .getModel(deviceAddress)
                .load()
                .transform{
                    it?.let {
                        deviceModel = it
                        if (!ButtonDevice.isButtonDevice(it.productId)) {
                            throw RuntimeException("Device is not a ButtonDevice that can be edited with this controller.")
                        }
                        ButtonDevice.fromProductId(it.productId)
                    } ?: throw RuntimeException("Device Model was null.")
                }
                .chain {
                    it?.let { buttonDevice ->
                        RuleModelProvider.instance().load().transform {
                            if (it == null) {
                                throw RuntimeException("Button Device is null, cannot continue.")
                            } else {
                                buttonsList = getButtonsForDevice(buttonDevice).toList()
                                val assignableActions = buttonActionController
                                    .getAssignableActionsForDevice(buttonDevice)
                                    .filterNot { it.ruleTemplateId == null }
                                    .toTypedArray()
                                buttonsList.forEach {thisButton ->
                                    if (thisButton.buttonName .equals(buttonName.toUpperCase())) {
                                        selectedButton = thisButton
                                        currentAction = buttonActionController.getCurrentButtonAction(it, selectedDeviceAddress, assignableActions, thisButton)
                                    }
                                }
                                assignableActions.forEachIndexed {index, action ->
                                    if (currentAction == action) {
                                        currentActionIndex = index
                                    }
                                }
                                Pair(assignableActions.toList(), currentActionIndex)
                            }
                        }
                    }?: throw RuntimeException("Transform value was null.")
                }
                .onSuccess(Listeners.runOnUiThread {
                    onlyIfView { presentedView ->
                        presentedView.onButtonsLoaded(it.first, it.second)
                    }
                })
                .onFailure(errorListener)
    }

    override fun setButtonSelection(buttonName: String, newAction: ButtonAction) {
        assignButtonAction(newAction)
    }

    private fun getButtonsForDevice(device: ButtonDevice): Array<Button> {
        return when (device) {
            ButtonDevice.GEN3_FOUR_BUTTON_FOB -> FobButton.constructGen3FourButtonFob() as Array<Button>
            ButtonDevice.GEN2_FOUR_BUTTON_FOB -> FobButton.constructFourButtonFob() as Array<Button>
            ButtonDevice.GEN1_TWO_BUTTON_FOB -> FobButton.constructTwoButtonFob() as Array<Button>
            ButtonDevice.GEN1_SMART_BUTTON, ButtonDevice.GEN2_SMART_BUTTON -> SmartButton.values() as Array<Button>
        }
    }

    fun assignButtonAction(selectedAction: ButtonAction) {
        logger.debug("Assigning action {} to button {} of device {}.", selectedAction, selectedButton, deviceModel)

        // If the last assigned value wasn't the default action, then delete it
        if (!currentAction.isDefaultAction) {
            deleteButtonAction(currentAction, object : ButtonActionDeletionListener {
                override fun onButtonActionDeleted() {
                    completeButtonActionAssignment(selectedAction)
                }
            })
        } else {
            completeButtonActionAssignment(selectedAction)
        }
    }

    private fun completeButtonActionAssignment(selectedAction: ButtonAction) {
        if (!selectedAction.isDefaultAction) {
            // User selected an action other than the default, create the rule
            applyButtonAction(selectedAction)
        } else {
            // User selected 'default action' (i.e., "Activate a rule"); nothing to do
            fireOnComplete()
        }

    }

    private fun applyButtonAction(selectedAction:ButtonAction) {
        logger.debug("Applying action {} to button {} of device {}", selectedAction, selectedButton, deviceModel)

        if (selectedAction.isDefaultAction)
        {
            // Nothing to do if selected action is the default action
            return
        }

        logger.debug("Fetching RuleTemplateModel {} for button action {}.", selectedAction.ruleTemplateId, selectedAction)
        RuleTemplateModelProvider.instance().getModel(Addresses.toObjectAddress(RuleTemplate.NAMESPACE, selectedAction.ruleTemplateId))
                .reload()
                .onSuccess { ruleTemplateModel ->
                    logger.debug("Successfully loaded rule template model {} for button action {}.", selectedAction.ruleTemplateId, selectedAction)
                    applyButtonActionAsRule(selectedAction, ruleTemplateModel)
                }.onFailure { throwable ->
                    logger.error("An error occured loading RuleTemplateModel {} for button action {}.", selectedAction.ruleTemplateId, selectedAction)
                    fireOnError(throwable)
                }
    }

    private fun applyButtonActionAsRule(selectedAction:ButtonAction, ruleTemplateModel:RuleTemplateModel) {
        logger.debug("Applying action {} as rule instance of RuleTemplateModel {}.", selectedAction, selectedAction.ruleTemplateId)

        // Create a new rule for this device
        val request = RuleTemplate.CreateRuleRequest()
        request.address = Addresses.toObjectAddress(RuleTemplate.NAMESPACE, selectedAction.ruleTemplateId)
        request.placeId = CorneaClientFactory.getClient().activePlace.toString()
        request.name = ruleTemplateModel.name
        request.description = ruleTemplateModel.description

        // Fill in the template parameters (selectors)
        val selectors = HashMap<String?, Any>()
        selectors[selectedAction.deviceAddressArgumentName] = selectedDeviceAddress

        // If action doesn't specify a button identifier, then one isn't required
        if (selectedAction.buttonIdArgumentName != null) {
            selectors[selectedAction.buttonIdArgumentName] = selectedButton.toString().toLowerCase()
        }

        request.context = selectors
        CorneaClientFactory.getClient().request(request)
                .onSuccess(object:Listener<ClientEvent> {
                    override fun onEvent(clientEvent: ClientEvent) {
                        logger.debug("Successfully created rule for action {} for button {} on device {}.", selectedAction, selectedButton, deviceModel)
                        currentAction = selectedAction
                        fireOnComplete()
                    }
                })
                .onFailure(object:Listener<Throwable> {
                    override fun onEvent(throwable:Throwable) {
                        logger.error("An error occurred creating rule for action {} for button {} on device {}.", selectedAction, selectedButton, deviceModel)
                        fireOnError(throwable)
                    }
                })
    }

    private fun deleteButtonAction(action: ButtonAction, listener: ButtonActionDeletionListener?) {
        logger.warn("Removing action {} from button {} of device {}.", action, selectedButton, deviceModel)

        RuleModelProvider.instance().load().onSuccess(Listeners.runOnUiThread { logger.warn("Loaded rules; got {} rule instances.", it.size)

            val rule = getRuleAttachedToAction(it, action, selectedButton, selectedDeviceAddress)

            if (rule != null) {
                rule.delete().onSuccess(Listener<Rule.DeleteResponse> {
                    logger.debug("Successfully removed action rule {} from button {} of device {}.", action, selectedButton, deviceModel)
                    listener?.onButtonActionDeleted()
                }).onFailure(Listener<Throwable> { throwable ->
                    logger.error("An error occured removing action {} from button {} of device {}.", action, selectedButton, deviceModel)
                    fireOnError(throwable)
                })
            } else {
                logger.error("Failed to remove action {} from button {} of device {} because the action could not be resolved to a rule. This button may have multiple actions attached to it.", action, selectedButton, deviceModel)
            }
        }).onFailure(Listeners.runOnUiThread{
            logger.error("Failed to load rules due to: {}. Button action not deleted; this button may have multiple actions attached to it.", it.message)
            fireOnError(it)
        })
    }

    private fun getRuleAttachedToAction(rules: List<RuleModel>, action: ButtonAction, button: Button, deviceAddress: String): RuleModel? {

        val buttonAttachedRules = getRulesAttachedToButton(rules, deviceAddress, button)
        logger.warn("Looking for rules attached associated with action {} for button {} on device {}; {} rules attached to button", action, button, deviceAddress, buttonAttachedRules.size)

        for (thisRule in buttonAttachedRules) {
            if (thisRule.getTemplate().equals(action.ruleTemplateId!!, ignoreCase = true)) {
                return thisRule
            }
        }
        return null
    }

    private fun getRulesAttachedToButton(rules: List<RuleModel>, deviceAddress: String, button: Button): List<RuleModel> {
        val buttonAttachedRules = ArrayList<RuleModel>()
        val deviceAttachedRules = getRulesAttachedToDevice(rules, deviceAddress)

        // Don't need to filter device rules on the basis of button when device has only one button
        if (button.isSingleton) {
            return deviceAttachedRules
        }

        // Walk through each rule attached to this device
        for (thisRule in deviceAttachedRules) {

            // Then, for each rule, walk through the template parameter values
            for (thisValue in thisRule.getContext().values) {

                // And if they match this button name, then assume the rule is associated with the button
                if (thisValue.toString().equals(button.buttonName!!, ignoreCase = true)) {
                    buttonAttachedRules.add(thisRule)
                }
            }
        }

        logger.debug("Found {} rules attached to button {} of device {}; {} rules attached to device.", buttonAttachedRules.size, button, deviceAddress, deviceAttachedRules.size)
        return buttonAttachedRules
    }

    private fun getRulesAttachedToDevice(rules: List<RuleModel>, deviceAddress: String): List<RuleModel> {
        val associatedRules = ArrayList<RuleModel>()

        // For each active rule...
        for (thisRule in rules) {

            // Walk through the value assigned to each template field
            for (thisContextValue in thisRule.context.values) {

                // And if it matches this device, we've got a wiener!
                if (thisContextValue.toString().equals(deviceAddress, ignoreCase = true)) {
                    associatedRules.add(thisRule)
                }
            }
        }

        logger.debug("Found {} for rules attached to device {}; {} rules in total.", associatedRules.size, deviceAddress, rules.size)
        return associatedRules
    }

    private fun fireOnError(reason: Throwable) {
        onlyIfView { presentedView ->
            presentedView.showError(reason)
            presentedView.onButtonActionSaved()
        }
    }

    private fun fireOnComplete() {
        onlyIfView { presentedView ->
            presentedView.onButtonActionSaved()
        }
    }

    private interface ButtonActionDeletionListener {
        fun onButtonActionDeleted()
    }

    companion object {
        private val logger = LoggerFactory.getLogger(FobButtonActionPresenterImpl::class.java)
    }

}

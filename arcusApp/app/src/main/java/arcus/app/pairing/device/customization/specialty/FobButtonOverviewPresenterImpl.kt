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

import android.app.Activity
import arcus.cornea.presenter.BasePresenterContract
import arcus.cornea.presenter.KBasePresenter
import arcus.cornea.provider.DeviceModelProvider
import arcus.cornea.provider.PairingDeviceModelProvider
import arcus.cornea.provider.RuleModelProvider
import arcus.cornea.utils.Listeners
import com.iris.client.event.Futures
import com.iris.client.model.DeviceModel
import arcus.app.R
import arcus.app.device.buttons.controller.ButtonActionController
import arcus.app.device.buttons.model.Button
import arcus.app.device.buttons.model.ButtonDevice
import arcus.app.device.buttons.model.FobButton
import arcus.app.device.buttons.model.SmartButton

interface FobButtonOverviewView {

    /**
     * Called to display the list of button actions that can be assigned to the a button.
     *
     *  @param buttons: The list of buttons with name and action text.
     *  @param deviceAddress:  Address of the Fob Button Device
     */
    fun onButtonsLoaded(buttons: List<ButtonWithAction>, deviceAddress: String)

    /**
     * Called when a Exception is thrown by the Presenter for the View to handle
     *
     *  @param throwable: Throwable Exception
     */
    fun showError(throwable: Throwable)
}

interface FobButtonOverviewPresenter : BasePresenterContract<FobButtonOverviewView> {

    /**
     * Loads the device model and calls the onButtonsLoaded method  of the View
     * when the buttons are known.
     *
     * @param pairingDeviceAddress: device address of the Button Fob to be customized.
     */
    fun loadFromPairingDevice(pairingDeviceAddress: String)
}

data class ButtonWithAction(val buttonNameText: String, val buttonActionText: String, val imageResId: Int)

class FobButtonOverviewPresenterImpl (val activity: Activity): FobButtonOverviewPresenter, KBasePresenter<FobButtonOverviewView>() {

    private val errorListener = Listeners.runOnUiThread<Throwable> { error ->
        onlyIfView { view ->
            view.showError(error)
        }
    }
    private var deviceModel: DeviceModel? = null
    private val buttonActionController = ButtonActionController()

    override fun loadFromPairingDevice(pairedDeviceAddress: String) {


        RuleModelProvider.instance().reload().chain {
            PairingDeviceModelProvider
                    .instance()
                    .getModel(pairedDeviceAddress)
                    .load()
                }
                .chain { model ->
                    model?.deviceAddress?.let { address ->
                        DeviceModelProvider
                                .instance()
                                .getModel(address)
                                .load()
                    } ?: Futures.failedFuture(RuntimeException("Could not load null device model."))
                }
                .transform{
                    it?.let {
                        deviceModel = it

                        if (!ButtonDevice.isButtonDevice(it.productId)) {
                            throw RuntimeException("Device is not a ButtonDevice that can be edited with this controller.")
                        }

                        Pair(ButtonDevice.fromProductId(it.productId), it.address)

                    } ?: throw RuntimeException("Could not get Button Device and Device Address.")
                }
                .chain {
                    it?.let { buttonAndAddress ->
                        val buttons = getButtonsForDevice(buttonAndAddress.first) as Array<FobButton>
                        val buttonsList = mutableListOf<ButtonWithAction>()
                        RuleModelProvider.instance().load().transform {
                            if (it == null) {
                                throw RuntimeException("Pair of Button Device and Device Address is null, cannot continue.")
                            } else {
                                val assignableActions = buttonActionController.getAssignableActionsForDevice(buttonAndAddress.first)

                                for (thisButton in buttons) {
                                    val currentAction = buttonActionController.getCurrentButtonAction(it, buttonAndAddress.second, assignableActions, thisButton).toString()
                                    thisButton.setButtonAction(currentAction)
                                }
                                buttons.forEachIndexed { index, button ->
                                    buttonsList.add(
                                        ButtonWithAction(button.buttonName ?: "Unknown",
                                            getButtonActionText(button.buttonAction),
                                            button.imageResId)
                                    )
                                }
                                Pair(buttonsList, buttonAndAddress.second)
                            }
                        }
                    }?: throw RuntimeException("Error getting list of Buttons and Device Address")
                }
                .onSuccess(Listeners.runOnUiThread {
                    onlyIfView { presentedView ->
                        presentedView.onButtonsLoaded(it.first, it.second)
                    }
                })
                .onFailure(errorListener)
    }


    private fun getButtonsForDevice(device: ButtonDevice): Array<Button> {
        when (device) {
            ButtonDevice.GEN3_FOUR_BUTTON_FOB -> return FobButton.constructGen3FourButtonFob() as Array<Button>
            ButtonDevice.GEN2_FOUR_BUTTON_FOB -> return FobButton.constructFourButtonFob() as Array<Button>
            ButtonDevice.GEN1_TWO_BUTTON_FOB -> return FobButton.constructTwoButtonFob() as Array<Button>
            ButtonDevice.GEN1_SMART_BUTTON, ButtonDevice.GEN2_SMART_BUTTON -> return SmartButton.values() as Array<Button>
            else -> throw RuntimeException("Button device not implemented: " + device)
        }
    }

    fun getButtonActionText(buttonAction: String): String {
        var buttonActionText: String = "Unknown"
        when (buttonAction) {
            SECURITY_ON -> buttonActionText = activity.getString(R.string.fob_button_alarm_on)
            SECURITY_OFF -> buttonActionText = activity.getString(R.string.fob_button_alarm_off)
            SECURITY_PARTIAL -> buttonActionText = activity.getString(R.string.fob_button_alarm_partial)
            PLAY_CHIME -> buttonActionText = activity.getString(R.string.fob_button_play_chime)
            ACTIVATE_RULE -> buttonActionText = activity.getString(R.string.fob_button_activate_rule)
        }
        return buttonActionText
    }

    companion object {
        const val SECURITY_ON = "SET_SECURITY_ALARM_TO_ON"
        const val SECURITY_OFF = "SET_SECURITY_ALARM_TO_OFF"
        const val SECURITY_PARTIAL = "SET_SECURITY_ALARM_TO_PARTIAL"
        const val PLAY_CHIME = "PLAY_CHIME"
        const val ACTIVATE_RULE = "ACTIVATE_A_RULE"
    }

}

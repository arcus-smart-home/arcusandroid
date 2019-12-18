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
package arcus.presentation.pairing.device.customization

import android.os.Parcelable
import arcus.cornea.presenter.BasePresenterContract
import arcus.presentation.pairing.device.steps.WebLink
import kotlinx.android.parcel.Parcelize

interface CustomizationView {
    /**
     * Called when the customization steps for a device are loaded and ready to go
     *
     * @param steps the list of customization steps for this device
     */
    fun customizationSteps(steps: List<CustomizationStep>)

    /**
     * Called when there is a terrible failure.
     *
     * @param throwable the error
     */
    fun showError(throwable: Throwable)
}

interface CustomizationPresenter : BasePresenterContract<CustomizationView> {
    /**
     * Loads the pairing device and gets the customizations for it, parses and tells
     * the UI about it
     *
     * @param pairingDeviceAddress address of the device we're trying to customize
     */
    fun loadDevice(pairingDeviceAddress: String)

    /**
     * Calls "AddCustomization" on the pairing device for the current step
     * Name should be the name of the customization step just completed
     *
     * @param type the step to call AddCustomization for
     */
    fun completeCustomization(type: CustomizationType)
}

/**
 * Describes the type of customization step we are performing
 */
enum class CustomizationType {
    // Uncomment as we add screen support for each of these.
    CONTACT_TEST,
    NAME,
    FAVORITE,
//    RULES,
    MULTI_BUTTON_ASSIGNMENT,
    CONTACT_TYPE,
    PRESENCE_ASSIGNMENT,
//    SCHEDULE,
    INFO,
    ROOM,
    WEATHER_RADIO_STATION,
    PROMON_ALARM,
    SECURITY_MODE,
    STATE_COUNTY_SELECT,
    OTA_UPGRADE,
    WATER_HEATER,
    IRRIGATION_ZONE,
    MULTI_IRRIGATION_ZONE,
    UNKNOWN,
    CUSTOMIZATION_COMPLETE,
    ;

    companion object {
        @JvmStatic
        fun fromPlatformType(type: String?): CustomizationType {
            return if (type == null) {
                UNKNOWN
            } else {
                try {
                    valueOf(type)
                } catch (e: Exception) {
                    UNKNOWN
                }
            }
        }
    }
}

/**
 * Customization step representation from the platform.
 *
 * @param id The ID of the step, like 'customization/name'
 *           This is used for loading images when appropriate.
 * @param order The order this step occurs in.
 * @param type Describes the type of screen to show. The client MUST not fail if an
 *             unrecognized type is received, but simply ignore that step.
 * @param header The section header text, which should be used as a primary title
 *               for the overall page, e.g. 'Customize Device', 'Advanced Automation'
 * @param title An optional title. Generally displayed in bold above the image and the description
 *              strings.
 * @param description A list of instructions to display describing the step.
 *                    There will always be at least one entry.
 *                    Generally each entry is displayed as a paragraph below the bolded step title
 * @param info An optional info message. Generally displayed at the bottom of the screen.
 * @param link If a link is included this is the text / url to use for that link.
 * @param choices A list of choices that can be displayed. The context in the choices depends on the
 *                type of action.
 *                For RULES Customization, it will contain the list of rule addresses.
 *                For PROMON_ALARM Customization, it will contain the list of newly available alarms
 *                from the following list: SMOKE, CO, SECURITY, PANIC, WATER
 */
@Parcelize
data class CustomizationStep(
    val id: String,
    val order: Int,
    val type: CustomizationType,
    val header: String? = null,
    val title: String? = null,
    val description: List<String>,
    val info: String? = null,
    val link: WebLink? = null,
    val choices: List<String>? = emptyList()
) : Parcelable

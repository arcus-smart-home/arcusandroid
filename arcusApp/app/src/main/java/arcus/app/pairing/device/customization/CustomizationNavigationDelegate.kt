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
package arcus.app.pairing.device.customization

import arcus.presentation.pairing.device.customization.CustomizationType

interface CustomizationNavigationDelegate {
    /**
     *
     * Navigates to the next step in the customization, or completes customization depending
     * on where we are, and adds the customization to the device.
     *
     * @param type the customization we just completed
     */
    fun navigateForwardAndComplete(type: CustomizationType)

    /**
     * Cancels the customization flow
     */
    fun cancelCustomization()
}
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
package arcus.app.createaccount

import androidx.fragment.app.Fragment

/**
 * Describes Account Creation flow - can flow forward / backward and be finished.
 */
interface CreateAccountFlow {
    /**
     * Goes to the next item in the flow if there is one.
     *
     * @param here where we're navigating from
     */
    fun nextFrom(here: Fragment)

    /**
     * Finishes the flow
     * For example: If this is hosted by an Activity this could finish()
     */
    fun finishFlow()

    /**
     * Shows back button, if possible, and enables hardware back press
     */
    fun showBackButton()

    /**
     * Enables hardware back press, but does not show any toolbar back button
     */
    fun allowBackButton()

    /**
     * Hides the back button, if possible, and disables hardware back press
     */
    fun hideBackButton()

    /**
     * Can be called to indicate that the current view is loading and should show as such
     *
     * This should also disable any back button behavior.
     */
    fun loading(loading: Boolean)
}

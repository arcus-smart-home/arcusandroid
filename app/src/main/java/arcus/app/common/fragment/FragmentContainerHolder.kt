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
package arcus.app.common.fragment

import androidx.fragment.app.Fragment


/**
 * Signifies the implementor is a fragment container
 */
interface FragmentContainerHolder {
    /**
     * Replaces the fragment in the container with the one supplied.
     *
     * @param fragment the fragment to use as a replacement
     * @param addToBackStack if we should add this to the fragment managers back stack
     */
    fun replaceFragmentContainerWith(fragment: Fragment, addToBackStack: Boolean = true)

    /**
     * Adds the fragment to the container with the one supplied.
     *
     * @param fragment the fragment to add to the container
     * @param addToBackStack if we should add this to the fragment managers back stack
     */
    fun addToFragmentContainer(fragment: Fragment, addToBackStack: Boolean = true)

    /**
     * Requests the toolbar owner should, or should not, show the back button on the toolbar.
     */
    fun showBackButtonOnToolbar(show: Boolean)

    /**
     * Requests that the title be set to [title]
     */
    fun setTitle(title: String)
}

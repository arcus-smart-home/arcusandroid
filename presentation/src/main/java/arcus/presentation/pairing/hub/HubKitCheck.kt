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
package arcus.presentation.pairing.hub

import arcus.cornea.presenter.BasePresenterContract

interface HubKitCheckView {
    /**
     * Invoked when it is know if the hub has associated kit items.
     *
     * @param hasItems true if the hub has kit items, false if not
     */
    fun onHubHasKitItems(hasItems: Boolean)
}

interface HubKitCheckPresenter : BasePresenterContract<HubKitCheckView> {
    /**
     * Checks to see if this hub has any kit items associated with it.
     */
    fun checkIfHubHasKitItems()
}

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
package arcus.presentation.settings.list

import arcus.cornea.helpers.await
import arcus.cornea.model.PlacesWithRoles
import arcus.cornea.provider.AvailablePlacesProvider
import arcus.presentation.common.view.ViewState
import arcus.presentation.common.view.ViewStateViewModel

// TODO: Inject...
//  PlacesWithRoles shouldn't be exposed. This should have a model created for it as well, but it touches a lot of
//  so holding off for now.
// TODO: The fragment also hard-codes the entries used on the page... This could be removed as well and the ViewModel
//  could tell the view which items to display.
class SettingsListViewModel(
    private val availablePlacesProvider: AvailablePlacesProvider = AvailablePlacesProvider.instance()
) : ViewStateViewModel<PlacesWithRoles>() {
    override fun loadData() {
        safeLaunch {
            _viewState.value = ViewState.Loading()

            val placesAndRoles = availablePlacesProvider.loadPlacesWithRoles().await()
            _viewState.value = ViewState.Loaded(placesAndRoles)
        }
    }
}

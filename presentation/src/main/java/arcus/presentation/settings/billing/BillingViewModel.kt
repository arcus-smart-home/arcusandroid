/*
 *  Copyright 2020 Arcus Project.
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
package arcus.presentation.settings.billing

import arcus.cornea.CorneaClientFactory
import arcus.cornea.SessionController
import arcus.cornea.helpers.await
import arcus.presentation.R
import arcus.presentation.common.view.ViewState
import arcus.presentation.common.view.ViewStateViewModel
import com.iris.client.capability.Place
import com.iris.client.model.ModelCache
import com.iris.client.model.PlaceModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// TODO: Inject...
class BillingViewModel(
    private val sessionController: SessionController = SessionController.instance(),
    private val modelCache: ModelCache = CorneaClientFactory.getModelCache()
) : ViewStateViewModel<List<BillablePlace>>() {
    override fun loadData() {
        safeLaunch {
            _viewState.value = ViewState.Loading()

            val account = sessionController.accountRef.load().await()
            val listPlaceResponse = account.listPlaces().await()

            @Suppress("UNCHECKED_CAST")
            val places = modelCache.addOrUpdate(listPlaceResponse.places) as List<PlaceModel>

            val names = withContext(Dispatchers.Default) {
                places.map { place ->
                    BillablePlace(
                        place.id.orEmpty(),
                        place.name.orEmpty(),
                        place.streetAddress1.orEmpty(),
                        place.location(),
                        getPlanStringFor(place.serviceLevel),
                        place.serviceAddons?.mapNotNull { getAddonStringFor(it) } ?: emptyList()
                    )
                }
            }

            _viewState.value = ViewState.Loaded(names)
        }
    }

    private fun PlaceModel.location(): String = StringBuilder().apply {
        append(city.orEmpty())
        if (!state.isNullOrEmpty()) {
            append(", ")
            append(state)
        }
        if (!zipCode.isNullOrEmpty()) {
            append(" ")
            append(zipCode)
        }
    }.toString()

    private fun getPlanStringFor(plan: String): Int? = when (plan) {
        Place.SERVICELEVEL_BASIC -> R.string.basic_plan
        Place.SERVICELEVEL_PREMIUM -> R.string.premium_plan
        Place.SERVICELEVEL_PREMIUM_FREE -> R.string.premium_free_plan
        Place.SERVICELEVEL_PREMIUM_PROMON -> R.string.pro_monitoring_plan
        Place.SERVICELEVEL_PREMIUM_PROMON_FREE -> R.string.pro_monitoring_free_plan
        Place.SERVICELEVEL_PREMIUM_PROMON_ANNUAL -> R.string.pro_monitoring_annual_plan
        Place.SERVICELEVEL_PREMIUM_ANNUAL -> R.string.premium_annual_plan
        else -> null
    }

    private fun getAddonStringFor(addon: String): Int? = when (addon) {
        "CELLBACKUP" -> R.string.backup_cellular
        else -> null
    }
}

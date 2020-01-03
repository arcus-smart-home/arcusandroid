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
package arcus.presentation.settings.marketing

import arcus.cornea.SessionController
import arcus.cornea.helpers.await
import arcus.presentation.common.view.ViewState
import arcus.presentation.common.view.ViewStateViewModel
import java.util.Date

// TODO: Inject...
class SettingsMarketingViewModel(
    private val sessionController: SessionController = SessionController.instance()
) : ViewStateViewModel<MarketingSettings>() {
    override fun loadData() {
        safeLaunch {
            _viewState.value = ViewState.Loading()

            val personModel = sessionController.personRef.load().await()
            val consent = personModel.consentOffersPromotions ?: Date(0)
            _viewState.value = ViewState.Loaded(
                MarketingSettings(
                    consentOffersPromotions = OPT_OUT.compareTo(consent) != 0
                )
            )
        }
    }

    fun saveSettings(marketingSettings: MarketingSettings) {
        safeLaunch {
            _viewState.value = ViewState.Loading()
            val personModel = sessionController.personRef.load().await()

            val consentDate = if (marketingSettings.consentOffersPromotions) Date() else OPT_OUT
            personModel.consentOffersPromotions = consentDate
            personModel.commit().await()
            _viewState.value = ViewState.Loaded(marketingSettings)
        }
    }

    companion object {
        private val OPT_OUT = Date(0)
    }
}

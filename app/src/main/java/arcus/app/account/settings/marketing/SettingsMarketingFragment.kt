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
package arcus.app.account.settings.marketing

import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import androidx.lifecycle.Observer
import arcus.app.R
import arcus.app.common.fragments.CoreFragment
import arcus.presentation.common.view.ViewState
import arcus.presentation.settings.marketing.MarketingSettings
import arcus.presentation.settings.marketing.SettingsMarketingViewModel
import arcus.app.common.error.ErrorManager.`in` as errorIn

class SettingsMarketingFragment : CoreFragment<SettingsMarketingViewModel>() {
    private lateinit var offersPromoSelection: RadioButton

    override val viewModelClass: Class<SettingsMarketingViewModel> = SettingsMarketingViewModel::class.java
    override val title: String
        get() = getString(R.string.account_settings_marketing)
    override val layoutId: Int = R.layout.fragment_settings_marketing

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        offersPromoSelection = view.findViewById(R.id.settings_marketing_offers_promo)

        val offersPromoContainer = view.findViewById<View>(R.id.fragment_setting_marketing_offers_container)
        offersPromoContainer.setOnClickListener {
            offersPromoSelection.isChecked = !offersPromoSelection.isChecked
        }

        view.findViewById<View>(R.id.setting_marketing_save_btn).setOnClickListener {
            viewModel.saveSettings(MarketingSettings(offersPromoSelection.isChecked))
        }

        viewModel.viewState.observe(viewLifecycleOwner, Observer {
            when (it) {
                is ViewState.Loaded -> offersPromoSelection.isChecked = it.item.consentOffersPromotions
                is ViewState.Error<*, *> -> errorIn(activity).showGenericBecauseOf(it.error)
            }
        })
    }

    companion object {
        @JvmStatic
        fun newInstance(): SettingsMarketingFragment = SettingsMarketingFragment()
    }
}

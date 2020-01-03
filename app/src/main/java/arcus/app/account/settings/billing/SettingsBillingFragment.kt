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
package arcus.app.account.settings.billing

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import arcus.app.R
import arcus.app.account.registration.AccountBillingInfoFragment
import arcus.app.common.backstack.BackstackManager
import arcus.app.common.error.ErrorManager.`in` as errorIn
import arcus.app.common.fragments.CoreFragment
import arcus.app.common.image.ImageManager
import arcus.app.common.image.picasso.transformation.CropCircleTransformation
import arcus.app.common.utils.ActivityUtils
import arcus.cornea.model.PlacesWithRoles
import arcus.presentation.common.view.ViewState
import arcus.presentation.settings.billing.BillablePlace
import arcus.presentation.settings.billing.BillingViewModel

class SettingsBillingFragment : CoreFragment<BillingViewModel>() {
    private lateinit var servicePlanContainer: LinearLayout
    private lateinit var noServicePlanContainer: View
    private lateinit var haveServicePlansContainer: View
    private lateinit var paymentInfoRL: View
    private lateinit var shopNowButton: Button

    private var placesWithRoles: PlacesWithRoles? = null

    override val title: String
        get() = getString(R.string.account_settings_billing_title)
    override val layoutId: Int = R.layout.fragment_account_settings_billing
    override val viewModelClass: Class<BillingViewModel> = BillingViewModel::class.java

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        placesWithRoles = arguments?.getParcelable(PLACE_ROLE)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        paymentInfoRL = view.findViewById(R.id.payment_info_cell)
        servicePlanContainer = view.findViewById(R.id.service_plan_container)
        noServicePlanContainer = view.findViewById(R.id.no_service_plan_container)
        haveServicePlansContainer = view.findViewById(R.id.have_service_plan_container)
        shopNowButton = view.findViewById(R.id.shop_now)

        paymentInfoRL.setOnClickListener {
            val nextFragment = AccountBillingInfoFragment.newInstance(AccountBillingInfoFragment.ScreenVariant.SETTINGS)
            BackstackManager
                .getInstance()
                .navigateToFragment(nextFragment, true)
        }

        viewModel.viewState.observe(viewLifecycleOwner, Observer {
            when (it) {
                is ViewState.Loaded -> processBillablePlaces(it.item)
                is ViewState.Error<*, *> -> errorIn(requireActivity()).showGenericBecauseOf(it.error)
            }
        })
    }

    private fun processBillablePlaces(places: List<BillablePlace>) {
        servicePlanContainer.removeAllViews()

        if (places.isEmpty()) {
            haveServicePlansContainer.isVisible = false
            noServicePlanContainer.isVisible = true
            shopNowButton.setOnClickListener { ActivityUtils.launchShopNow() }
        } else {
            haveServicePlansContainer.isVisible = true
            for ((index, place) in places.withIndex()) {
                val billablePlaceView = BillablePlaceViewHolder(R.layout.service_plan_item).also {
                    it.bind(place, index != places.lastIndex)
                }

                servicePlanContainer.addView(billablePlaceView.itemView)
            }
        }
    }

    private inner class BillablePlaceViewHolder(layoutResourceId: Int) {
        val itemView: View = LayoutInflater
            .from(activity)
            .inflate(layoutResourceId, servicePlanContainer, false)

        fun bind(place: BillablePlace, hasNext: Boolean) {
            itemView.findViewById<TextView>(R.id.place_name).text = place.placeName
            itemView.findViewById<TextView>(R.id.place_street).text = place.streetAddress
            itemView.findViewById<TextView>(R.id.place_location).text = place.cityStateZip
            itemView.findViewById<View>(R.id.divider).isVisible = hasNext

            place.currentServiceLevel?.let {
                itemView.findViewById<TextView>(R.id.plan_service_level).text = getString(it)
            }

            val imageView = itemView.findViewById<ImageView>(R.id.place_image)
            ImageManager.with(activity)
                .putPlaceImage(place.placeId)
                .withTransform(CropCircleTransformation())
                .into(imageView)
                .execute()

            val addOnText = place.serviceAddons.joinToString(separator = ", ") { getString(it) }
            itemView.findViewById<TextView>(R.id.plan_addons).text = addOnText
        }
    }

    companion object {
        private const val PLACE_ROLE = "PLACE_ROLE"

        @JvmStatic
        fun newInstance(
            placesWithRoles: PlacesWithRoles?
        ): SettingsBillingFragment = SettingsBillingFragment().apply {
            arguments = Bundle(1).apply {
                putParcelable(PLACE_ROLE, placesWithRoles)
            }
        }
    }
}

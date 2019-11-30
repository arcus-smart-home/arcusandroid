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
package arcus.app.dashboard

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import arcus.cornea.SessionController
import arcus.cornea.controller.SubscriptionController
import arcus.cornea.provider.HubModelProvider
import com.iris.client.capability.Hub
import arcus.app.R
import arcus.app.activities.DashboardActivity
import arcus.app.activities.FullscreenFragmentActivity
import arcus.app.activities.GenericConnectedFragmentActivity
import arcus.app.common.backstack.BackstackManager
import arcus.app.common.error.fragment.CarePremiumRequired
import arcus.app.common.fragments.BaseFragment
import arcus.app.common.fragments.ModalErrorBottomSheet
import arcus.app.common.popups.ScleraPopup
import arcus.app.common.utils.ActivityUtils
import arcus.app.common.utils.GlobalSetting
import arcus.app.dashboard.adapter.AddMenuAdapter
import arcus.app.dashboard.adapter.MenuItemClickHandler
import arcus.app.pairing.device.productcatalog.ProductCatalogActivity
import arcus.app.pairing.hub.activation.KitActivationGridFragment
import arcus.app.pairing.hub.kickoff.HubKitFragment
import arcus.app.subsystems.care.fragment.CareListBehaviorFragment
import arcus.app.subsystems.people.PersonAddFragment
import arcus.app.subsystems.place.PlaceAddFragment
import arcus.app.subsystems.place.controller.NewPlaceSequenceController
import arcus.app.subsystems.place.model.PlaceTypeSequence
import arcus.app.subsystems.rules.RuleCategoriesFragment
import arcus.app.subsystems.scenes.catalog.controller.SceneCatalogSequenceController
import arcus.presentation.pairing.hub.activation.DeviceActivationStatus
import arcus.presentation.pairing.hub.activation.KitActivationStatusPresenterImpl
import arcus.presentation.pairing.hub.activation.KitActivationStatusView

class AddMenuFragment : BaseFragment(), MenuItemClickHandler, KitActivationStatusView {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapterCallback : MenuItemClickHandler
    private lateinit var activationStatusPresenter: KitActivationStatusPresenterImpl

    override fun getTitle() = getString(R.string.add_title_lower)

    override fun getLayoutId() = R.layout.fragment_add_menu

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.add_menu_rv)
        recyclerView.layoutManager = LinearLayoutManager(view.context)
        val adapter = recyclerView.adapter
        var newAdapter : AddMenuAdapter? = null

        activity?.let {
            it.title = title
            it.invalidateOptionsMenu()
            newAdapter = AddMenuAdapter(adapterCallback, it)
        }

        if (adapter != null && newAdapter != null) {
            recyclerView.swapAdapter(newAdapter, true)
        } else {
            recyclerView.adapter = newAdapter
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        adapterCallback = this
    }

    override fun onResume() {
        super.onResume()
        activationStatusPresenter = KitActivationStatusPresenterImpl()
        activationStatusPresenter.setView(this)
    }

    override fun onPause() {
        super.onPause()
        activationStatusPresenter.clearView()
    }

    override fun onAddArcusClicked() {
        if (SessionController.instance().isCloneWithPlace) {
            val popup = ScleraPopup.newInstance(
                    R.string.add_place_clone_error_title,
                    R.string.add_place_clone_error_desc,
                    hideBottomButton = true
            )

            val supportActivity = activity as? AppCompatActivity
            supportActivity?.let {
                popup.show(it.supportFragmentManager, ScleraPopup::class.java.name /* tag */)
            }
        } else {
            NewPlaceSequenceController(PlaceTypeSequence.ADD_PLACE_GUEST)
                .startSequence(activity, null, PlaceAddFragment::class.java)
        }
    }

    override fun onAddAHubClicked() {
        activationStatusPresenter.getDeviceActivationStatus()
    }

    override fun onAddADeviceClicked() {
        startActivity(Intent(context, ProductCatalogActivity::class.java))
    }

    override fun onAddARuleClicked() {
        BackstackManager.getInstance().navigateToFragment(RuleCategoriesFragment.newInstance(), true)
    }

    override fun onAddASceneClicked() {
        SceneCatalogSequenceController()
            .startSequence(activity, null)
    }

    override fun onAddAPlaceClicked() {
        NewPlaceSequenceController(PlaceTypeSequence.ADD_PLACE_OWNER)
            .startSequence(activity, null, PlaceAddFragment::class.java)
    }

    override fun onAddAPersonClicked() {
        BackstackManager.getInstance().navigateToFragment(PersonAddFragment.newInstance(), true)
    }

    override fun onAddCareBehaviorClicked() {
        if (SubscriptionController.isPremiumOrPro()) {
        BackstackManager.getInstance().navigateToFragment(CareListBehaviorFragment.newInstance(), true)
        } else {
            FullscreenFragmentActivity.launch(activity!!, CarePremiumRequired::class.java)
        }
    }

    override fun onDeviceActivationStatusUpdate(status: DeviceActivationStatus) {
        if (status.needsActivation > 0) {
            if (HubModelProvider.instance().hubModel?.get(Hub.ATTR_STATE) == Hub.STATE_DOWN) {
                // Set up the ModalErrorBottomSheet Dialog and show it.
                val errorTitle: String = getString(R.string.enhanced_hub_offline_title)
                val errorDescription: String = getString(R.string.enhanced_hub_offline_desc)
                val buttonText: String = getString(R.string.error_modal_get_support)
                val dismissText: String = getString(R.string.dismiss)

                val hubOfflineDialog =
                    ModalErrorBottomSheet.newInstance(errorTitle, errorDescription, buttonText, dismissText)

                hubOfflineDialog.setGetSupportAction {
                    ActivityUtils.launchUrl(GlobalSetting.NO_CONNECTION_HUB_SUPPORT_URL)
                }

                hubOfflineDialog.show(fragmentManager)
            } else {
                context?.let { nnContext ->
                    startActivity(
                        GenericConnectedFragmentActivity
                            .getLaunchIntent(
                                nnContext,
                                KitActivationGridFragment::class.java
                            )
                    )
                }
            }
        }
        else {
            checkAddHubOK()
        }
    }

    /////////////////////////////////// CHECK /////////////////////////////////////
    private fun checkAddHubOK() {
        if (HubModelProvider.instance().hubModel == null) {
            // Refresh happening in getHubModel
            BackstackManager.getInstance().navigateToFragment(HubKitFragment.newInstanceHidingToolbar(false), true)
            (activity as DashboardActivity).setIsHub(true)
        } else {
            HubAlreadyPairedErrorPopup.newInstance().show(fragmentManager)
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(): AddMenuFragment {
            return AddMenuFragment()
        }
    }
}

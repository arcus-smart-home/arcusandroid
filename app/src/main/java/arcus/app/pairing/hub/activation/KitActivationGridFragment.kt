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
package arcus.app.pairing.hub.activation

import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.core.view.ViewCompat
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout

import arcus.app.R
import arcus.app.activities.DashboardActivity
import arcus.app.common.fragment.BackPressInterceptor
import arcus.app.common.fragment.FragmentContainerHolder
import arcus.app.common.popups.ScleraPopup
import arcus.app.common.utils.ActivityUtils
import arcus.app.common.utils.GlobalSetting
import arcus.app.common.utils.ImageUtils
import arcus.app.common.view.ScleraTextView
import arcus.presentation.pairing.hub.activation.DeviceActivationStatus
import arcus.presentation.pairing.hub.activation.KitDeviceActivationPresenterImpl
import arcus.presentation.pairing.hub.activation.KitDeviceActivationView
import arcus.presentation.pairing.hub.activation.KitDevice


class KitActivationGridFragment : Fragment(),
    KitDeviceActivationView,
    BackPressInterceptor {
    private val presenter = KitDeviceActivationPresenterImpl(ImageUtils.screenDensity)
    private lateinit var title : ScleraTextView
    private lateinit var subtitle : ScleraTextView
    private lateinit var kitItemsRV : RecyclerView
    private lateinit var confettiContainer : ViewGroup
    private var rvAdapter : KitActivationRecyclerViewAdapter? = null
    private var fragmentContainer : FragmentContainerHolder? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_kit_activation_grid, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        title = view.findViewById(R.id.title)
        subtitle = view.findViewById(R.id.subtitle)

        confettiContainer = view.findViewById(R.id.confetti_container)

        view.findViewById<View>(R.id.watch_tutorial_banner).setOnClickListener {
            ActivityUtils.launchUrl(Uri.parse(GlobalSetting.KITTING_TUTORIAL_VIDEO_URL))
        }

        view.findViewById<View>(R.id.need_help_link).setOnClickListener {
            ActivityUtils.launchUrl(Uri.parse(GlobalSetting.KITTING_NEED_HELP_URL))
        }

        view.findViewById<View>(R.id.go_to_dashboard_button).setOnClickListener {
            presenter.getDeviceActivationStatus()
        }

        kitItemsRV = view.findViewById(R.id.kit_items_rv)
        kitItemsRV.layoutManager = GridLayoutManager(context, 2)
        ViewCompat.setNestedScrollingEnabled(kitItemsRV, false)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        fragmentContainer = context as? FragmentContainerHolder?
    }

    override fun onResume() {
        super.onResume()

        presenter.setView(this)
        presenter.loadKitItems()
        fragmentContainer?.setTitle(getString(R.string.kit_setup))
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.dismissActivatedKitItems()
    }

    override fun onDeviceActivationStatusUpdate(status: DeviceActivationStatus) {
        when {
            status.needsCustomization > 0 -> {
                ScleraPopup
                    .newInstance(
                        R.string.device_customization_incomplete_title,
                        R.string.device_customization_incomplete_desc,
                        R.string.customize_my_devices
                    )
                    .show(fragmentManager)
            }

            status.needsActivation > 0 -> {
                ScleraPopup
                    .newInstance(
                        R.string.device_activation_incomplete_title,
                        R.string.device_activation_incomplete_desc,
                        R.string.finish_activation
                    )
                    .show(fragmentManager)
            }

            // For now mapping to exit since there are no error messages
            // for when devices are mispaired.
            else -> {
                activity?.let {
                    startActivity(DashboardActivity.getHomeFragmentIntent(it))
                    it.finish()
                }
            }
        }
    }

    override fun onKitItemsLoaded(items: List<KitDevice>, allConfigured: Boolean) {
        val oldAdapter = kitItemsRV.adapter
        rvAdapter = KitActivationRecyclerViewAdapter(items)
        if (oldAdapter != null) {
            kitItemsRV.swapAdapter(rvAdapter, true)
        } else {
            kitItemsRV.adapter = rvAdapter
        }

        if (allConfigured) {
            val compatActivity = activity as AppCompatActivity
            compatActivity.findViewById<LinearLayout>(R.id.watch_tutorial_banner).visibility = View.GONE
            title.text = getString(R.string.congrats_kit_setup_completed)
            subtitle.visibility = View.GONE
        }
    }

    override fun onBackPressed(): Boolean {
        presenter.getDeviceActivationStatus()
        return true
    }
}

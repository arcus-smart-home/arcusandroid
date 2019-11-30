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
package arcus.app.pairing.device.steps.wifismartswitch.location

import android.Manifest
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import arcus.app.R
import arcus.app.activities.PermissionsActivity
import arcus.app.common.utils.GlobalSetting
import arcus.app.common.fragment.TitledFragment
import arcus.app.pairing.device.steps.StepsNavigationDelegate
import arcus.app.pairing.device.steps.ViewPagerSelectedFragment
import java.util.*
import kotlin.properties.Delegates


class WSSLocationFragment : Fragment(),
    TitledFragment,
    ViewPagerSelectedFragment, PermissionsActivity.PermissionCallback {
    private var stepsNavigationDelegate by Delegates.notNull<StepsNavigationDelegate>()
    private val permissionsActivity : PermissionsActivity?
        get() {
            return activity as? PermissionsActivity?
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_wss_location, container, false)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        stepsNavigationDelegate = context as StepsNavigationDelegate
    }

    override fun onPageSelected() {
        permissionsActivity?.setPermissionCallback(this)
        permissionsActivity?.checkPermission(
            listOf(Manifest.permission.ACCESS_COARSE_LOCATION),
            GlobalSetting.PERMISSION_ACCESS_COARSE_LOCATION,
            R.string.permission_rationale_location_swann
        )
    }

    override fun onNotSelected() {
        // No-Op
    }

    override fun getTitle(): String = getString(R.string.wifi_smart_switch_title)

    override fun permissionsUpdate(
        permissionType: Int,
        permissionsDenied: ArrayList<String>,
        permissionsDeniedNeverAskAgain: ArrayList<String>
    ) {
        if (permissionsDeniedNeverAskAgain.contains(Manifest.permission.ACCESS_COARSE_LOCATION)) {
            permissionsActivity?.showSnackBarForPermissions(getString(R.string.permission_rationale_location_swann_snack))
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = WSSLocationFragment()
    }
}

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
package arcus.app.pairing.device.steps.bledevice.location

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
import arcus.app.pairing.device.steps.ViewPagerSelectedFragment
import arcus.app.pairing.device.steps.bledevice.BleStepsNavigationDelegate
import kotlin.properties.Delegates


class BleLocationFragment : Fragment(),
    ViewPagerSelectedFragment,
    PermissionsActivity.PermissionCallback {
    private enum class PermissionStatus {
        OK,
        DENIED,
        DENIED_DONT_ASK
    }

    private var showingPermissions = false
    private var permissionStatus = PermissionStatus.DENIED
    private var bleStepsNavigationDelegate by Delegates.notNull<BleStepsNavigationDelegate>()
    private var pairingDevice = ""
    private val permissionsActivity : PermissionsActivity?
        get() {
            return activity as? PermissionsActivity?
        }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_ble_location, container, false)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        bleStepsNavigationDelegate = context as BleStepsNavigationDelegate
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let { bundle ->
            pairingDevice = if (bundle.getBoolean(ARG_IS_HUB)) {
                getString(R.string.smart_hub)
            } else {
                getString(R.string.bluetooth_camera)
            }
        }
    }

    override fun onPageSelected() {
        permissionsActivity?.setPermissionCallback(this)
        when (permissionStatus) {
            PermissionStatus.DENIED -> {
                bleStepsNavigationDelegate.disableContinue()
                if (!showingPermissions) {
                    showingPermissions = true
                    permissionsActivity?.checkPermission(
                        listOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                        GlobalSetting.PERMISSION_ACCESS_COARSE_LOCATION,
                        R.string.permission_rationale_location_ble_swann
                    )
                }
            }
            PermissionStatus.DENIED_DONT_ASK -> {
                bleStepsNavigationDelegate.disableContinue()
                permissionsActivity?.showSnackBarForPermissions(getString(R.string.permission_rationale_location_ble_swann_snack))
            }
            PermissionStatus.OK -> bleStepsNavigationDelegate.enableContinue()
        }
    }

    override fun onNotSelected() {
        // No-Op
    }

    override fun permissionsUpdate(
        permissionType: Int,
        permissionsDenied: ArrayList<String>,
        permissionsDeniedNeverAskAgain: ArrayList<String>
    ) {
        showingPermissions = false
        permissionStatus = when {
            permissionsDeniedNeverAskAgain.contains(Manifest.permission.ACCESS_COARSE_LOCATION) -> {
                PermissionStatus.DENIED_DONT_ASK
            }
            permissionsDenied.contains(Manifest.permission.ACCESS_COARSE_LOCATION) -> {
                PermissionStatus.DENIED
            }
            else -> {
                PermissionStatus.OK
            }
        }
    }

    companion object {
        private const val ARG_IS_HUB= "ARG_IS_HUB"

        @JvmStatic
        fun newInstance(isHub: Boolean = false) = BleLocationFragment().also {
            with (Bundle()) {
                putBoolean(ARG_IS_HUB, isHub)
                it.arguments = this
            }
        }
    }
}

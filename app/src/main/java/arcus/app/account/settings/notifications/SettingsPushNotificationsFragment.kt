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
package arcus.app.account.settings.notifications

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import androidx.core.view.isEmpty
import androidx.core.view.isGone
import androidx.core.view.isVisible
import arcus.app.R
import arcus.app.account.settings.adapter.MobileDeviceListAdapter
import arcus.app.account.settings.adapter.MobileDeviceListAdapter.OnDeleteListener
import arcus.app.activities.FullscreenFragmentActivity
import arcus.app.common.backstack.BackstackManager
import arcus.app.common.error.ErrorManager
import arcus.app.common.error.type.OtherErrorTypes
import arcus.app.common.fragments.NoViewModelFragment
import arcus.app.common.popups.InfoTextPopup
import com.iris.client.model.MobileDeviceModel

// TODO: This class was just converted to Kotlin and still needs significant work to update it.
class SettingsPushNotificationsFragment : NoViewModelFragment(),
    SettingsPushNotificationsFragmentController.Callbacks,
    OnDeleteListener {

    private lateinit var mobileDeviceList: ListView
    private lateinit var otherDevicesSection: LinearLayout
    private lateinit var currentDeviceSection: LinearLayout
    private lateinit var currentDeviceName: TextView
    private lateinit var currentDeviceType: TextView
    private lateinit var mobileInfoContainer: View

    private var isEditMode = false
    private var hasOtherDevices = false
    private lateinit var mobileDeviceListAdapter: MobileDeviceListAdapter

    override val layoutId: Int = R.layout.fragment_settings_push_notifications
    override val title: String
        get() = getString(R.string.push_notifications)
    override val menuId: Int?
        get() = if (hasOtherDevices) R.menu.menu_edit_done_toggle else null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mobileDeviceList = view.findViewById(R.id.mobileDeviceList)
        otherDevicesSection = view.findViewById(R.id.otherDevicesSection)
        currentDeviceSection = view.findViewById(R.id.currentDeviceSection)
        currentDeviceName = view.findViewById(R.id.deviceName)
        currentDeviceType = view.findViewById(R.id.deviceType)
        mobileInfoContainer = view.findViewById(R.id.mobileNotificationsInfoContainer)

        mobileDeviceListAdapter = MobileDeviceListAdapter(activity)
        mobileDeviceListAdapter.setOnDeleteListener(this)
    }

    override fun onResume() {
        super.onResume()

        mobileInfoContainer.setOnClickListener {
            val popup =
                InfoTextPopup.newInstance(R.string.mobile_disable_push, R.string.more_info_text)
            BackstackManager.getInstance().navigateToFloatingFragment(popup, popup.javaClass.simpleName, true)
        }
        mobileDeviceList.adapter = mobileDeviceListAdapter
        progressContainer.isVisible = true
        SettingsPushNotificationsFragmentController.getInstance().listener = this
        SettingsPushNotificationsFragmentController.getInstance().loadMobileDevices(activity)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        isEditMode = !isEditMode
        mobileDeviceListAdapter.editEnabled = isEditMode
        item.title = if (isEditMode) getString(R.string.card_menu_done) else getString(R.string.card_menu_edit)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        if (menu.isEmpty()) return

        if (isEditMode) {
            menu.getItem(0).title = getString(R.string.card_menu_done)
        } else {
            menu.getItem(0).title = getString(R.string.card_menu_edit)
        }
    }

    override fun onMobileDevicesLoaded(
        currentDevice: MobileDeviceModel?,
        otherDevices: List<MobileDeviceModel>?
    ) {
        progressContainer.isGone = true
        hasOtherDevices = otherDevices != null && otherDevices.isNotEmpty()
        otherDevicesSection.visibility = if (hasOtherDevices) View.VISIBLE else View.GONE

        val hasCurrentDevice = currentDevice != null
        currentDeviceSection.visibility = if (hasCurrentDevice) View.VISIBLE else View.GONE

        if (hasCurrentDevice) {
            currentDeviceName.text = getDeviceName(currentDevice!!)
            currentDeviceType.text = getString(R.string.device_type, currentDevice.deviceModel.toString())
        }

        if (hasOtherDevices) {
            mobileDeviceListAdapter.clear()
            mobileDeviceListAdapter.addAll(otherDevices)
            mobileDeviceListAdapter.notifyDataSetInvalidated()
        }

        // Special case: User has nada devices receiving push notifications
        if (!hasCurrentDevice && !hasOtherDevices) {
            BackstackManager.getInstance().navigateBack()
            FullscreenFragmentActivity.launch(requireActivity(), SettingsTurnOnNotificationsFragment::class.java)
        }

        // Update the edit/done menu visibility on the presence of other devices
        activity?.invalidateOptionsMenu()
    }

    override fun onDeviceRemoved(removedDevice: MobileDeviceModel?) {
        progressContainer.isGone = true
        ErrorManager.`in`(activity).show(OtherErrorTypes.PUSH_NOTIFICATION_HEADS_UP)
    }

    override fun onCorneaError(cause: Throwable) {
        progressContainer.isGone = true
        ErrorManager.`in`(activity).showGenericBecauseOf(cause)
    }

    override fun onDelete(mobileDeviceModel: MobileDeviceModel?) {
        progressContainer.isVisible = true
        SettingsPushNotificationsFragmentController.getInstance().removeMobileDevice(activity, mobileDeviceModel)
    }

    /**
     * Attempts to parse the OS type and version from the strings presents in the MobileDeviceModel.
     * This is a bad idea and should be refactored. No assurances that these string formats won't
     * change in the future.
     *
     * @param deviceModel
     * @return
     */
    fun getDeviceName(deviceModel: MobileDeviceModel): String {
        if ("ios".equals(deviceModel.osType, ignoreCase = true)) { // Assumes iOS version string looks like
            return if (deviceModel.osVersion != null && deviceModel.osVersion.split(" ").toTypedArray().size == 4) {
                activity!!.getString(
                    R.string.device_name,
                    deviceModel.osType.toUpperCase(),
                    deviceModel.osVersion.split(" ").toTypedArray()[1]
                )
            } else {
                deviceModel.osType.toUpperCase()
            }
        } else if ("android".equals(deviceModel.osType, ignoreCase = true)) {
            return if (deviceModel.osVersion != null && deviceModel.osVersion.split(" ").toTypedArray().size == 3) {
                activity!!.getString(
                    R.string.device_name,
                    deviceModel.osType.toUpperCase(),
                    deviceModel.osVersion.split(" ").toTypedArray()[2]
                )
            } else {
                deviceModel.osType.toUpperCase()
            }
        }
        return getString(R.string.device_name_unknown)
    }

    companion object {
        @JvmStatic
        fun newInstance(): SettingsPushNotificationsFragment =
            SettingsPushNotificationsFragment()
    }
}

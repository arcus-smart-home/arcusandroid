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
package arcus.app.pairing.device.productcatalog.popups

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager.NameNotFoundException
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.startActivity
import arcus.app.R
import arcus.app.activities.GenericFragmentActivity
import arcus.app.common.backstack.BackstackManager
import arcus.app.common.fragments.ModalErrorBottomSheet
import arcus.app.common.utils.ActivityUtils
import arcus.app.common.utils.GlobalSetting
import arcus.app.dashboard.HomeFragment
import arcus.app.pairing.device.steps.PairingStepsActivity
import arcus.app.pairing.device.steps.bledevice.BleNotFoundPopup
import arcus.app.pairing.device.steps.bledevice.BlePairingStepsActivity
import arcus.app.pairing.device.steps.wifismartswitch.WSSPairingStepsActivity
import arcus.cornea.provider.DeviceModelProvider
import arcus.cornea.provider.HubModelProvider
import arcus.cornea.provider.ProductModelProvider
import arcus.presentation.pairing.BLE_GS_INDOOR_PLUG_PRODUCT_ID
import arcus.presentation.pairing.BLE_GS_OUTDOOR_PLUG_PRODUCT_ID
import arcus.presentation.pairing.BLE_SWANN_CAMERA_PRODUCT_ID
import arcus.presentation.pairing.WIFI_SMART_SWITCH_PRODUCT_ID
import com.iris.client.capability.Hub

/**
 * While this class doesn't really manage the popups it is designed to centralize the logic for
 * hub required, hub offline and an additional device is required before proceeding.
 *
 * Since these checks are done in several places this keeps the order and logic the same.
 */
class ProductCatalogPopupManager {
    data class ProductEntity(val id: String, private val hubRequired: Boolean, val minVersion: String) {
        fun requiresHub() = hubRequired && HubModelProvider.instance().hubModel == null

        fun requiresOnlineHub() = hubRequired && HubModelProvider.instance().hubModel?.get(Hub.ATTR_STATE) == Hub.STATE_DOWN

        fun requiresAdditionalProduct() : Boolean {
            val productRequired = ProductModelProvider.instance().getByProductIDOrNull(id)?.devRequired

            return if(productRequired?.isNotBlank() == true) {
                // This inversion causes this to be true if they don't have the required device and
                // false if they do have the required device
                !DeviceModelProvider
                        .instance()
                        .store
                        .values()
                        .any {
                            it.productId == productRequired
                        }
            } else {
                false
            }
        }

        fun requiresAppUpdate(activity: Activity) =  isAppVersionOlderThan(activity, minVersion)

        private fun isAppVersionOlderThan(activity: Activity, minimumVersion: String?): Boolean {
            val appVersionString: String
            if (minimumVersion == null) {
                return false
            }
            appVersionString = try {
                val pInfo = activity.packageManager.getPackageInfo(activity.packageName, 0)
                pInfo.versionName
            } catch (e: NameNotFoundException) {
                //logger.error("An error occurred getting the version of this app.", e)
                return true
            }
            val minVersionComponents = minimumVersion.split("\\.").toTypedArray()
            val appVersionComponents = appVersionString.split("\\.").toTypedArray()
            // Get rid of the commit hash
            if (appVersionComponents.size == 3 && appVersionComponents[2].indexOf("-") > 0) {
                appVersionComponents[2] = appVersionComponents[2].substring(0, appVersionComponents[2].indexOf("-"))
            } else if (appVersionComponents.size == 2 && appVersionComponents[1].indexOf("-") > 0) {
                appVersionComponents[1] = appVersionComponents[1].substring(0, appVersionComponents[1].indexOf("-"))
            } else if (appVersionComponents.size == 1 && appVersionComponents[0].indexOf("-") > 0) {
                appVersionComponents[0] = appVersionComponents[0].substring(0, appVersionComponents[0].indexOf("-"))
            }
            return try {
                val appMajor = if (appVersionComponents.isNotEmpty()) appVersionComponents[0].toInt() else 0
                val appMinor = if (appVersionComponents.size >= 2) appVersionComponents[1].toInt() else 0
                val appMaint = if (appVersionComponents.size >= 3) appVersionComponents[2].toInt() else 0
                val minMajor = if (minVersionComponents.isNotEmpty()) minVersionComponents[0].toInt() else 0
                val minMinor = if (minVersionComponents.size >= 2) minVersionComponents[1].toInt() else 0
                val minMaint = if (minVersionComponents.size >= 3) minVersionComponents[2].toInt() else 0
                minMajor > appMajor || minMajor == appMajor && minMinor > appMinor || minMajor == appMajor && minMinor == appMinor && minMaint > appMaint
            } catch (e: NumberFormatException) {
                // logger.error("Failed to parse version numbers. App version: $appVersionComponents Min version: $minVersionComponents", e)
                false
            }
        }
    }

    private fun getProductInfo(productAddress: String) : ProductEntity {
        val productModelSource = ProductModelProvider.instance().getModel(productAddress)
        productModelSource.load()
        val product = productModelSource.get()

        return ProductEntity(product?.id ?: "", product?.hubRequired == true, product?.minAppVersion ?: "")
    }

    fun <T : Activity> showHubPopupsIfRequired(activity: T) : Boolean {
        val productEntity = ProductEntity("", true, "")

        return when {
            productEntity.requiresHub() -> {
                showRequiresHubPopup(activity)
                true
            }
            productEntity.requiresOnlineHub() -> {
                showRequiresHubOnlinePopup(activity)
                true
            }
            productEntity.requiresAppUpdate(activity) -> {
                showRequiresUpdatePopup(activity)
                true
            }
            else -> false
        }
    }

    fun navigateForwardOrShowPopup(activity: Activity, productAddress: String) {
        val productEntity = getProductInfo(productAddress)

        when {
            productEntity.requiresHub() -> showRequiresHubPopup(activity)
            productEntity.requiresOnlineHub() -> showRequiresHubOnlinePopup(activity)
            productEntity.requiresAdditionalProduct() -> showAdditionalDeviceRequiredPopup(activity, productEntity.id)
            productEntity.requiresAppUpdate(activity) -> showRequiresUpdatePopup(activity)
            else -> {
                when {
                    productAddress.endsWith(BLE_SWANN_CAMERA_PRODUCT_ID)
                    || productAddress.endsWith(BLE_GS_INDOOR_PLUG_PRODUCT_ID)
                    || productAddress.endsWith(BLE_GS_OUTDOOR_PLUG_PRODUCT_ID) -> {
                        if (BluetoothAdapter.getDefaultAdapter() != null) {
                            startActivity(activity, BlePairingStepsActivity.createIntent(activity, productAddress), null)
                        } else {
                            (activity as AppCompatActivity?)?.let {
                                BleNotFoundPopup().show(it.supportFragmentManager)
                            }
                        }
                    }
                    productAddress.endsWith(WIFI_SMART_SWITCH_PRODUCT_ID) -> {
                        startActivity(activity, WSSPairingStepsActivity.createIntent(activity, productAddress), null)
                    }
                    else -> {
                        val pairingIntent = Intent(activity, PairingStepsActivity::class.java)
                        pairingIntent.putExtra(PairingStepsActivity.ARG_PRODUCT_ADDRESS, productAddress)
                        startActivity(activity, pairingIntent, null)
                    }
                }
            }
        }
    }

    private fun showRequiresHubPopup(activity: Activity) {
        activity.startActivity(GenericFragmentActivity.getLaunchIntent(
            activity,
            ParingHubRequiredModal::class.java,
            showFullscreen = true
        ))
    }

    private fun showRequiresHubOnlinePopup(activity: Activity) {
        // Set up the ModalErrorBottomSheet Dialog and show it.
        val errorTitle: String = activity.getString(R.string.enhanced_hub_offline_title)
        val errorDescription: String = activity.getString(R.string.enhanced_hub_offline_desc)
        val buttonText: String = activity.getString(R.string.error_modal_get_support)
        val dismissText: String = activity.getString(R.string.dismiss)
        val hubOfflineDialog = ModalErrorBottomSheet.newInstance(errorTitle, errorDescription, buttonText, dismissText)
        
        hubOfflineDialog.setGetSupportAction {
            ActivityUtils.launchUrl(GlobalSetting.NO_CONNECTION_HUB_SUPPORT_URL)
        }
        val appCompatActivity = activity as? AppCompatActivity
        appCompatActivity?.let {
            hubOfflineDialog.show(it.supportFragmentManager, ModalErrorBottomSheet::class.java.name)
        }
    }

    private fun showAdditionalDeviceRequiredPopup(activity: Activity, id: String) {
        val productModel = ProductModelProvider.instance().getByProductIDOrNull(id)
        val requiredDevice = ProductModelProvider.instance().getByProductIDOrNull(productModel?.devRequired)

        requiredDevice?.let {
            val popup = OtherDeviceRequiredPopup
                    .newInstance(
                            it.vendor,
                            it.shortName,
                            it.helpUrl
                    )
            val supportActivity = activity as? AppCompatActivity
            supportActivity?.let { act ->
                popup.show(act.supportFragmentManager, OtherDeviceRequiredPopup::class.java.name /* tag */)
            }
        }
    }

    fun showCustomizeDevicesPopup(
        activity: Activity,
        pairedCorrectly: Boolean,
        description: String,
        listener: (() -> Unit)? = null
    ) {
        val popup = CustomizeDevicesPopup.newInstance(pairedCorrectly, description)
        popup.setExitPairingListener(listener)
        val supportActivity = activity as? AppCompatActivity
        supportActivity?.let {
            popup.show(it.supportFragmentManager, CustomizeDevicesPopup::class.java.name /* tag */)
        }
    }

    fun showRequiresUpdatePopup(activity: Activity) {
        val errorTitle: String = activity.getString(R.string.update_to_latest)
        val errorDescription: String = activity.getString(R.string.app_out_of_date)
        val updateText = activity.getString(R.string.update_app)
        val cancelText = activity.getString(R.string.cancel)
        val appUpdateDialog = ModalErrorBottomSheet.newInstance(errorTitle, errorDescription, updateText, cancelText)

        appUpdateDialog.setGetSupportAction {
            val appPackageName = getSanitizedPackageName(activity)

            BackstackManager.getInstance().popAllFragments()
            BackstackManager.getInstance().navigateToFragment(HomeFragment.newInstance(), true)
            try {
                activity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$appPackageName")))
            } catch (anfe: android.content.ActivityNotFoundException) {
                activity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")))
            }
        }

        val supportActivity = activity as? AppCompatActivity
        supportActivity?.let {
            appUpdateDialog.show(it.supportFragmentManager, ModalErrorBottomSheet::class.java.name /* tag */)
        }
    }

    private fun getSanitizedPackageName(activity: Activity): String {
        var appPackageName = activity.getPackageName() // getPackageName() from Context or Activity object
        appPackageName = appPackageName.replace(".debug", "")
        appPackageName = appPackageName.replace(".qa", "")
        appPackageName = appPackageName.replace(".beta", "")
        return appPackageName
    }
}

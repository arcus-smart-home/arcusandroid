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
package arcus.app.activities

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.Settings
import androidx.annotation.LayoutRes
import com.google.android.material.snackbar.BaseTransientBottomBar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.snackbar.Snackbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.text.Html
import android.view.View
import android.widget.TextView
import com.google.android.gms.auth.api.credentials.Credential
import arcus.app.R
import arcus.app.common.backstack.BackstackManager
import arcus.app.common.image.IntentRequestCode
import arcus.app.common.image.UGCImageIntentResultHandler
import arcus.app.common.popups.AlertPopup
import arcus.app.common.sequence.SequencedFragment
import arcus.app.launch.CredentialResolutionResultHandler
import arcus.app.subsystems.people.PersonIdentityFragment
import arcus.app.subsystems.people.controller.NewPersonSequenceController
import arcus.app.subsystems.people.model.DeviceContact
import arcus.app.subsystems.people.util.DeviceContactUtil
import org.slf4j.LoggerFactory
import java.util.*

open class PermissionsActivity : AppCompatActivity() {
    private lateinit var permissionCallback: PermissionCallback
    private val activityResultListeners = HashSet<ActivityResultListener>()
    private var coordinatorLayout: CoordinatorLayout? = null

    interface PermissionCallback {
        fun permissionsUpdate(permissionType: Int, permissionsDenied: ArrayList<String>, permissionsDeniedNeverAskAgain: ArrayList<String>)
    }

    fun setPermissionCallback(callback: PermissionCallback) {
        permissionCallback = callback
    }

    override fun setContentView(@LayoutRes layoutResID: Int) {
        super.setContentView(layoutResID)
        coordinatorLayout = findViewById<View>(R.id.coordinator_layout) as CoordinatorLayout?
    }

    fun addActivityResultListener(listener: ActivityResultListener) {
        activityResultListeners.add(listener)
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        logger.debug("Handling activity result for requestCode: " + requestCode +
                " -- " + IntentRequestCode.fromRequestCode(requestCode))

        for (thisListener in activityResultListeners) {
            thisListener.onActivityFinished(requestCode, resultCode, data)
        }

        when (IntentRequestCode.fromRequestCode(requestCode)) {
            IntentRequestCode.CREDENTIAL_RETRIEVED -> {
                if (resultCode == Activity.RESULT_OK) {
                    val credential = data?.getParcelableExtra<Credential>(Credential.EXTRA_KEY)
                    CredentialResolutionResultHandler.getInstance().resolvedCredential = credential
                } else {
                    CredentialResolutionResultHandler.getInstance().resolvedCredential = null
                }
                UGCImageIntentResultHandler.getInstance().onCameraImageCapture(this, resultCode, data)
            }

            IntentRequestCode.TAKE_PHOTO -> {
                UGCImageIntentResultHandler.getInstance().onCameraImageCapture(this, resultCode, data)
            }

            IntentRequestCode.SELECT_IMAGE_FROM_GALLERY -> {
                UGCImageIntentResultHandler.getInstance().onImageGallerySelect(this, resultCode, data)
            }

            IntentRequestCode.DEVICE_CONTACT_SELECTION -> if (resultCode == Activity.RESULT_OK) {
                val contactUri = data?.data
                val contentResolver = contentResolver
                val cursor = contentResolver.query(contactUri!!, null, null, null, null)
                if (cursor != null && cursor.moveToFirst()) {
                    val person = DeviceContact()
                    val contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID))

                    DeviceContactUtil.getNameInfo(contentResolver, person, contactId)
                    val hasPhoneNumber = Integer.parseInt(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)))
                    if (hasPhoneNumber > 0) {
                        DeviceContactUtil.getPhoneInfo(this, contentResolver, person, contactId)
                    }
                    DeviceContactUtil.getEmailInfo(this, contentResolver, person, contactId)

                    val bundle = Bundle()
                    bundle.putParcelable(PersonIdentityFragment.DEVICE_CONTACT, person)
                    ((BackstackManager.getInstance().currentFragment as SequencedFragment<*>).controller as NewPersonSequenceController).deviceContact = person
                    (BackstackManager.getInstance().currentFragment as SequencedFragment<*>).goNext(bundle)
                } else {
                    val alertPopup = AlertPopup.newInstance(
                            getString(R.string.people_contacts_not_available_title),
                            getString(R.string.people_contacts_not_available_description), null, null,
                            object : AlertPopup.AlertButtonCallback {
                                override fun topAlertButtonClicked(): Boolean {
                                    return false
                                }

                                override fun bottomAlertButtonClicked(): Boolean {
                                    return false
                                }

                                override fun errorButtonClicked(): Boolean {
                                    return false
                                }

                                override fun close() {
                                    BackstackManager.getInstance().navigateBack()
                                }
                            }
                    )
                    BackstackManager.getInstance().navigateToFloatingFragment(alertPopup, alertPopup.javaClass.canonicalName, true)
                }
            }

            IntentRequestCode.TURN_ON_LOCATION -> when (resultCode) {
                Activity.RESULT_OK -> logger.debug("User agreed to turn on Location: [{}]", requestCode)
                Activity.RESULT_CANCELED -> {
                    logger.debug("User declined to turn on Location: [{}]", requestCode)
                    val alertPopup = AlertPopup.newInstance(
                            getString(R.string.swann_permission_denied_title),
                            getString(R.string.swann_permission_step_desc), null, null,
                            object : AlertPopup.AlertButtonCallback {
                                override fun topAlertButtonClicked(): Boolean {
                                    return false
                                }

                                override fun bottomAlertButtonClicked(): Boolean {
                                    return false
                                }

                                override fun errorButtonClicked(): Boolean {
                                    return false
                                }

                                override fun close() {
                                    BackstackManager.getInstance().navigateBack()
                                }
                            })
                    alertPopup.isCloseButtonVisible = true
                    BackstackManager.getInstance().navigateToFloatingFragment(alertPopup, alertPopup.javaClass.canonicalName, true)
                }
                else -> logger.debug("Received unknown intent request code: [{}]", requestCode)
            }

            else -> logger.debug("Received unknown intent request code: [{}]", requestCode)
        }
    }

    /**
     * Determines if the given permission has been granted by the user. Does not attempt to gain
     * permission by prompting the user.
     *
     * @param permission The permission to check (for example, Manifest.permission.ACCESS_COARSE_LOCATION)
     * @return True if the user has granted the permission, false otherwise.
     */
    fun hasPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
    }

    fun checkPermission(permissions: List<String>, permissionType: Int, rationaleId: Int) {
        val activity = this
        val permissionsToRequest = ArrayList<String>()
        var bShowRationale = false
        for (nInd in permissions.indices) {
            if (ContextCompat.checkSelfPermission(this, permissions[nInd]) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[nInd])) {
                    bShowRationale = true
                }
                permissionsToRequest.add(permissions[nInd])
            }
        }
        if (permissionsToRequest.size == 0) {
            permissionCallback.permissionsUpdate(permissionType, ArrayList(), ArrayList())
        } else {
            if (bShowRationale) {
                AlertDialog
                        .Builder(activity)
                        .setMessage(Html.fromHtml(activity.getString(rationaleId)))
                        .setPositiveButton(getString(R.string.pairing_next)) { _, _ ->
                            try {
                                ActivityCompat.requestPermissions(activity, permissionsToRequest.toTypedArray(), permissionType)
                            } catch (ignored: Exception) {
                                logger.error(ignored.message)
                            }
                        }
                        .setCancelable(false)
                        .create()
                        .show()
            } else {
                ActivityCompat.requestPermissions(this, permissionsToRequest.toTypedArray(), permissionType)
            }
        }
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        try {
            if (grantResults.isNotEmpty()) {
                val permissionsDenied = ArrayList<String>()
                val permissionsDeniedNeverAskAgain = ArrayList<String>()

                for (index in grantResults.indices) {
                    if (grantResults[index] == PackageManager.PERMISSION_DENIED) {
                        val neverAskAgain = Build.VERSION.SDK_INT >= 23 && !shouldShowRequestPermissionRationale(permissions[index])
                        if (neverAskAgain) {
                            permissionsDeniedNeverAskAgain.add(permissions[index])
                        }
                        permissionsDenied.add(permissions[index])
                    }
                }
                permissionCallback.permissionsUpdate(requestCode, permissionsDenied, permissionsDeniedNeverAskAgain)
            } else {
                permissionCallback.permissionsUpdate(requestCode, ArrayList(Arrays.asList(*permissions)), ArrayList())
            }
        } catch (e: IllegalStateException) {
            //in the event that the fragment is gone, we still want to keep from crashing
        }

    }

    fun showSnackbar(makeSnackbar: (CoordinatorLayout) -> BaseTransientBottomBar<*>) {
        coordinatorLayout?.let { layout ->
            makeSnackbar(layout).show()
        }
    }

    fun showSnackBarForPermissions(message: String) {
        val snackbar = coordinatorLayout?.let { Snackbar.make(it, message, 10000) }
        val view = snackbar?.view
        val tv = view?.findViewById<View>(com.google.android.material.R.id.snackbar_text) as TextView
        tv.setTextColor(ContextCompat.getColor(this, R.color.white))
        tv.maxLines = 6
        snackbar.setAction(getString(R.string.action_settings).toUpperCase()) {
            val intent = Intent()
            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            val uri = Uri.fromParts("package", packageName, null)
            intent.data = uri
            startActivity(intent)
        }

        snackbar.show()
    }

    fun showSnackBarForSettings(message: String) {
        val snackbar = coordinatorLayout?.let { Snackbar.make(it, message, 6000) }
        val view = snackbar?.view
        val tv = view?.findViewById<View>(R.id.snackbar_text) as TextView
        tv.setTextColor(ContextCompat.getColor(this, R.color.white))
        tv.maxLines = 6
        snackbar.show()
    }

    companion object {
        @JvmStatic
        private val logger = LoggerFactory.getLogger(PermissionsActivity::class.java)
    }
}

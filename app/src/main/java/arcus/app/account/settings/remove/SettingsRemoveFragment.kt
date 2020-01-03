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
package arcus.app.account.settings.remove

import android.app.Activity
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import arcus.app.R
import arcus.app.activities.LaunchActivity
import arcus.app.common.backstack.BackstackManager
import arcus.app.common.error.ErrorManager.`in` as errorIn
import arcus.app.common.fragments.NoViewModelFragment
import arcus.app.common.popups.InfoButtonPopup
import arcus.app.common.utils.LoginUtils
import arcus.app.common.view.Version1ButtonColor
import arcus.app.dashboard.HomeFragment
import arcus.app.subsystems.alarm.AlertFloatingFragment
import arcus.cornea.SessionController
import arcus.cornea.platformcall.PPARemovalController
import arcus.cornea.platformcall.PPARemovalController.RemovedCallback
import arcus.cornea.utils.CachedModelSource
import arcus.cornea.utils.Listeners
import com.google.android.material.textfield.TextInputLayout
import com.iris.capability.util.Addresses
import com.iris.client.capability.Account
import com.iris.client.capability.Person
import com.iris.client.capability.Place
import com.iris.client.model.AccountModel
import com.iris.client.model.PersonModel
import com.iris.client.model.PlaceModel
import java.util.Locale

// TODO: This class was just converted to Kotlin and still needs significant work to update it.
class SettingsRemoveFragment : NoViewModelFragment(), RemovedCallback {
    private val REMOVE = "REMOVE"
    private val DELETE = "DELETE"
    private var editText: EditText? = null
    private var removeBtn: Button? = null
    private var alertPromptTitle: String? = null
    private var alertButtonTopText: String? = null
    private var alertButtonBottomText: String? = null
    private var alertPopupSubTitle: String? = null
    private var hintKeyword: String? = null
    private var hintText: String? = null
    private var fragmentTitle: String? = null
    private var removeInstructions: String? = null
    private var removePromptFragment: String? = null
    private var popup: Fragment? = null
    private var targetAddress: String? = null
    private var targetPersonAddress: String? = null
    private var ppaRemovalController: PPARemovalController? = null
    private var removalType = 0

    @Transient var accountModel: AccountModel? = null
    @Transient var placeModel: PlaceModel? = null
    @Transient var personModel: PersonModel? = null
    private val removeTypedWatcher: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
        override fun afterTextChanged(s: Editable?) {
            if (s == null || removeBtn == null) {
                return
            }
            removeBtn!!.isEnabled = hintKeyword.equals(s.toString().trim { it <= ' ' }, ignoreCase = true)
        }
    }

    override val title: String
        get() = fragmentTitle.orEmpty()
    override val layoutId: Int = R.layout.fragment_remove

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val args = arguments
        if (args == null) {
            removalType = UNKNOWN
            return
        }
        removalType = args.getInt(TYPE_OF_REMOVAL, UNKNOWN)
        targetAddress = args.getString(TARGET_REMOVAL_ADDRESS, null)
        targetPersonAddress = args.getString(TARGET_PERSON_ADDRESS, null)
    }

    override fun onResume() {
        super.onResume()
        if (removalType == UNKNOWN) {
            return
        }
        alertPromptTitle = getString(R.string.are_you_sure).toUpperCase(Locale.getDefault())
        alertPopupSubTitle = getString(R.string.action_cannot_be_reversed)
        alertButtonTopText = getString(R.string.remove_text).toUpperCase(Locale.getDefault())
        alertButtonBottomText = getString(R.string.cancel_text).toUpperCase(Locale.getDefault())
        hintKeyword = REMOVE
        ppaRemovalController = PPARemovalController(this)
        when (removalType) {
            ACCOUNT -> if (!TextUtils.isEmpty(targetAddress)) {
                loadPlaceAccount()
            }
            PLACE -> if (!TextUtils.isEmpty(targetAddress)) {
                loadPlace()
            }
            ACCESS -> if (!TextUtils.isEmpty(targetAddress) && !TextUtils.isEmpty(
                    targetPersonAddress
                )
            ) {
                loadPersonPlace()
            }
            ACCOUNT_FULL_ACCESS -> {
                targetPersonAddress = SessionController.instance().personId
                if (!TextUtils.isEmpty(targetPersonAddress)) {
                    targetPersonAddress = Addresses.toObjectAddress(
                        Person.NAMESPACE,
                        Addresses.getId(targetPersonAddress)
                    )
                    setupStrings()
                }
            }
            UNKNOWN -> {
            }
            else -> {
            }
        }
        logger.debug("REMOVING the [{}] to [{}] for [{}]", removalType, targetAddress, targetPersonAddress)
    }

    private fun loadPlace() {
        CachedModelSource.get<PlaceModel>(targetAddress).load()
            .onSuccess(Listeners.runOnUiThread { model ->
                placeModel = model
                setupStrings()
            })
    }

    private fun loadAccount(accountID: String?) {
        val accountAddress = Addresses.toObjectAddress(Account.NAMESPACE, Addresses.getId(accountID))
        CachedModelSource.get<AccountModel>(accountAddress).load()
            .onSuccess(Listeners.runOnUiThread { model ->
                accountModel = model
                setupStrings()
            })
    }

    private fun loadPersonPlace() {
        targetPersonAddress = Addresses.toObjectAddress(Person.NAMESPACE, Addresses.getId(targetPersonAddress))
        CachedModelSource.get<PersonModel>(targetPersonAddress).load()
            .onSuccess(Listeners.runOnUiThread { model ->
                personModel = model
                loadPlace()
            })
    }

    private fun loadPlaceAccount() {
        targetAddress = Addresses.toObjectAddress(Place.NAMESPACE, Addresses.getId(targetAddress))
        CachedModelSource.get<PlaceModel>(targetAddress).load()
            .onSuccess(Listeners.runOnUiThread { model ->
                placeModel = model
                loadAccount(placeModel!!.account)
            })
    }

    private fun setupStrings() {
        when (removalType) {
            ACCOUNT -> setupRemoveAccountLayoutElements() // Sets up Strings, Onclick listeners, etc.
            PLACE -> setupRemovePlaceLayoutElements() // Sets up Strings, Onclick listeners, etc.
            ACCESS -> setupRemoveAccessLayoutElements() // Sets up Strings, Onclick listeners, etc.
            ACCOUNT_FULL_ACCESS -> setupRemoveAccessLogoutLayoutElements() // Sets up Strings, Onclick listeners, etc.
            else -> {
            }
        }
        hintText = getString(
            R.string.type_keyword_here,
            hintKeyword
        ) // Set late since account uses DELETE and others use REMOVE
        renderLayout() // applys the text values etc to the UI elements (shows to user what we think we're doing)
    }

    private fun setupRemoveAccountLayoutElements() {
        removeInstructions = getString(R.string.remove_account_instructions)
        removePromptFragment = getString(R.string.remove_account_prompt)
        fragmentTitle = getString(R.string.remove_account_title)
        hintKeyword = DELETE
        popup = AlertFloatingFragment.newInstance(
            alertPromptTitle, removePromptFragment, alertButtonTopText, alertButtonBottomText, alertPopupSubTitle,
            object : AlertFloatingFragment.AlertButtonCallback {
                override fun topAlertButtonClicked(): Boolean {
                    disableFieldsAndShowProgress()
                    ppaRemovalController!!.removeAccountAndLogin(accountModel!!.address)
                    return true
                }

                override fun bottomAlertButtonClicked(): Boolean {
                    return true
                }
            }
        )
    }

    private fun setupRemovePlaceLayoutElements() {
        removeInstructions = getString(R.string.remove_place_top_text, placeModel!!.name)
        removePromptFragment = getString(R.string.settings_remove_place)
        fragmentTitle = getString(R.string.remove_text)
        popup = AlertFloatingFragment.newInstance(
            alertPromptTitle, removePromptFragment, alertButtonTopText, alertButtonBottomText, alertPopupSubTitle,
            object : AlertFloatingFragment.AlertButtonCallback {
                override fun topAlertButtonClicked(): Boolean {
                    disableFieldsAndShowProgress()
                    ppaRemovalController!!.removePlace(placeModel!!.address)
                    return true
                }

                override fun bottomAlertButtonClicked(): Boolean {
                    enableRemoveButton()
                    return true
                }
            }
        ) { enableRemoveButton() }
    }

    private fun enableRemoveButton(delayMs: Long = 750) {
        removeBtn?.postDelayed({
            removeBtn?.isEnabled = true
        }, delayMs)
    }

    private fun setupRemoveAccessLayoutElements() {
        removeInstructions = getString(R.string.remove_access_instructions, placeModel!!.name, hintKeyword)
        removePromptFragment = getString(R.string.remove_access_prompt_text)
        fragmentTitle = getString(R.string.remove_text)
        popup = InfoButtonPopup.newInstance(
            alertPromptTitle!!,
            alertPopupSubTitle,
            removePromptFragment!!,
            alertButtonTopText!!,
            alertButtonBottomText!!,
            Version1ButtonColor.MAGENTA,
            Version1ButtonColor.BLACK
        )
        (popup as InfoButtonPopup?)!!.setCallback { correct ->
            if (correct) {
                disableFieldsAndShowProgress()
                ppaRemovalController!!.removeAccessToPlaceFor(targetAddress, targetPersonAddress)
            }
        }
    }

    private fun setupRemoveAccessLogoutLayoutElements() {
        hintKeyword = DELETE
        removeInstructions = getString(R.string.remove_account_instructions)
        removePromptFragment = getString(R.string.remove_account_last_access_prompt)
        fragmentTitle = getString(R.string.remove_account_title)
        popup = AlertFloatingFragment.newInstance(
            alertPromptTitle, removePromptFragment, alertButtonTopText, alertButtonBottomText, alertPopupSubTitle,
            object : AlertFloatingFragment.AlertButtonCallback {
                override fun topAlertButtonClicked(): Boolean {
                    disableFieldsAndShowProgress()
                    ppaRemovalController!!.deletePersonLogin(targetPersonAddress)
                    return true
                }

                override fun bottomAlertButtonClicked(): Boolean {
                    return true
                }
            }
        )
    }

    private fun renderLayout() {
        val view = view ?: return
        removeBtn = view.findViewById(R.id.fragment_remove_btn)
        editText = view.findViewById(R.id.remove_text_entry)
        editText!!.addTextChangedListener(removeTypedWatcher)
        view.findViewById<TextInputLayout>(R.id.remove_text_entry_layout).hint = hintText
        val topRemoveText = view.findViewById<TextView>(R.id.top_remove_text)
        val bottomRemoveText = view.findViewById<TextView>(R.id.bottom_remove_text)
        topRemoveText.text = removeInstructions
        bottomRemoveText.text = removePromptFragment
        removeBtn!!.isEnabled = false
        removeBtn!!.setOnClickListener {
            removeBtn!!.isEnabled = false
            val popupName = popup?.javaClass?.simpleName ?: "popup"
            BackstackManager.getInstance().navigateToFloatingFragment(popup, popupName, true)
        }
        setTitle()
    }

    private fun disableFieldsAndShowProgress() {
        progressContainer.isVisible = true
        editText!!.removeTextChangedListener(removeTypedWatcher)
        removeBtn!!.isEnabled = false
    }

    private fun logout() {
        SessionController.instance().logout()
        LoginUtils.completeLogout()
        val activity: Activity? = activity
        if (activity != null) {
            LaunchActivity.startLoginScreen(activity)
            activity.finishAffinity()
        }
    }

    override fun onSuccess() {
        progressContainer.isVisible = true
        when (removalType) {
            ACCOUNT -> logout()
            ACCOUNT_FULL_ACCESS -> logout()
            PLACE, ACCESS -> BackstackManager.getInstance().navigateBackToFragment(HomeFragment.newInstance())
            else -> {
                // No-Op??...
            }
        }
    }

    override fun onError(throwable: Throwable) {
        progressContainer.isVisible = false
        if (removeBtn != null && editText != null) {
            removeBtn!!.isEnabled = true
            editText!!.addTextChangedListener(removeTypedWatcher)
        }
        errorIn(activity).showGenericBecauseOf(throwable)
    }

    companion object {
        var TARGET_REMOVAL_ADDRESS = "TARGET_REMOVAL_ADDRESS"
        var TYPE_OF_REMOVAL = "TYPE_OF_REMOVAL"
        var TARGET_PERSON_ADDRESS = "TARGET_PERSON_ADDRESS"
        const val UNKNOWN = 0x0A
        const val ACCOUNT = 0x0B
        const val PLACE = 0x0C
        const val ACCESS = 0x0D
        const val ACCOUNT_FULL_ACCESS = 0x0E

        @JvmStatic
        fun removeAccountInstance(
            placeAddress: String?
        ): SettingsRemoveFragment = SettingsRemoveFragment().apply {
            arguments = Bundle(2).apply {
                putInt(TYPE_OF_REMOVAL, ACCOUNT)
                putString(TARGET_REMOVAL_ADDRESS, placeAddress)
            }
        }

        @JvmStatic
        fun removeFullAccessAccountInstance(): SettingsRemoveFragment = SettingsRemoveFragment().apply {
            arguments = Bundle(1).apply {
                putInt(TYPE_OF_REMOVAL, ACCOUNT_FULL_ACCESS)
            }
        }

        @JvmStatic
        fun removePlace(
            placeAddress: String
        ): SettingsRemoveFragment = SettingsRemoveFragment().apply {
            arguments = Bundle(2).apply {
                putString(TARGET_REMOVAL_ADDRESS, placeAddress)
                putInt(TYPE_OF_REMOVAL, PLACE)
            }
        }

        @JvmStatic
        fun removeAccess(
            placeAddress: String,
            personAddress: String
        ): SettingsRemoveFragment = SettingsRemoveFragment().apply {
            arguments = Bundle(3).apply {
                putString(TARGET_REMOVAL_ADDRESS, placeAddress)
                putString(TARGET_PERSON_ADDRESS, personAddress)
                putInt(TYPE_OF_REMOVAL, ACCESS)
            }
        }
    }
}

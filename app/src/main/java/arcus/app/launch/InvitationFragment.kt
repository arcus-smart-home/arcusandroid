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
package arcus.app.launch

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.core.view.isGone
import androidx.core.view.isVisible
import arcus.app.R
import arcus.app.account.registration.controller.AccountCreationSequenceController
import arcus.app.account.registration.model.AccountTypeSequence
import arcus.app.account.settings.pin.SettingsUpdatePin
import arcus.app.account.settings.list.SideNavSettingsFragment
import arcus.app.activities.LaunchActivity
import arcus.app.common.backstack.BackstackManager
import arcus.app.common.backstack.TransitionEffect
import arcus.app.common.error.ErrorManager.`in` as errorIn
import arcus.app.common.fragments.GenericInformationPopup
import arcus.app.common.fragments.NoViewModelFragment
import arcus.app.common.utils.PreferenceUtils
import arcus.app.common.validation.EmailValidator
import arcus.app.common.validation.NotEmptyValidator
import arcus.app.pairing.hub.popups.Generic1ButtonErrorPopup
import arcus.app.subsystems.people.model.DeviceContact
import arcus.cornea.CorneaClientFactory
import arcus.cornea.SessionController
import arcus.cornea.utils.Listeners
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout
import com.iris.client.exception.ErrorResponseException
import com.iris.client.service.InvitationService
import com.iris.client.service.InvitationService.GetInvitationResponse
import java.util.Locale

// TODO: This class was just converted to Kotlin and still needs significant work to update it.
class InvitationFragment : NoViewModelFragment() {
    private lateinit var etEmailAddress: EditText
    private lateinit var etInvitation: EditText
    private lateinit var emailEntryContainer: TextInputLayout
    private lateinit var invitationCodeContainer: TextInputLayout
    private lateinit var next: Button
    private var emailAddress: String? = null
    private var invitationCode: String? = null
    private var isSettingsVariant = false
    private val genericErrorListener = Listeners.runOnUiThread<Throwable> { throwable ->
        progressContainer.isGone = true
        next.isEnabled = true
        if (throwable is ErrorResponseException) {
            parseErrorException(throwable)
        } else {
            onError(throwable)
        }
    }

    override val title: String
        get() = if (isSettingsVariant) {
            getString(R.string.invitation_code)
        } else {
            resources.getString(R.string.invitation_title)
        }
    override val layoutId: Int
        get() = if (isSettingsVariant) {
            R.layout.invite_settings_fragment
        } else {
            R.layout.fragment_invitation
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let { args ->
            emailAddress = args.getString(EMAIL_ADDRESS)
            invitationCode = args.getString(INVITATION_CODE)
            isSettingsVariant = args.getBoolean(IS_SETTINGS, false)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        etEmailAddress = view.findViewById(R.id.emailEntry)
        etInvitation = view.findViewById(R.id.invitationCodeEntry)
        emailEntryContainer = view.findViewById(R.id.emailContainer)
        invitationCodeContainer = view.findViewById(R.id.invitationCodeContainer)

        etEmailAddress.setText(emailAddress)
        etInvitation.setText(invitationCode)

        next = view.findViewById(R.id.btnNext)
        next.setOnClickListener(View.OnClickListener {
            val emailValid = EmailValidator(emailEntryContainer, etEmailAddress).isValid
            val inviteCodeCheck = NotEmptyValidator(
                invitationCodeContainer,
                etInvitation,
                getString(R.string.people_err_please_enter)
            ).isValid
            if (!emailValid || !inviteCodeCheck) {
                return@OnClickListener
            }
            progressContainer.isVisible = true
            next.isEnabled = false
            if (TextUtils.isEmpty(CorneaClientFactory.getClient().connectionURL)) {
                CorneaClientFactory.getClient().connectionURL = PreferenceUtils.getPlatformUrl()
            }
            CorneaClientFactory
                .getService(InvitationService::class.java)
                .getInvitation(etInvitation.text.toString(), etEmailAddress.text.toString())
                .onSuccess(
                    Listeners.runOnUiThread { getInvitationResponse: GetInvitationResponse ->
                        val invite =
                            getInvitationResponse.invitation
                        if (invite == null) {
                            onError(RuntimeException("Invite was null, but no error from server."))
                            return@runOnUiThread
                        }
                        val first = invite["invitorFirstName"] as String?
                        val last = invite["invitorLastName"] as String?
                        val personName = String.format(
                            "%s %s",
                            first ?: "",
                            last ?: ""
                        )
                        var placeName = invite["placeName"] as String?
                        placeName = placeName ?: ""
                        val inviteeEmail = invite["inviteeEmail"] as String?
                        val placeID = invite["placeId"].toString()
                        if (isSettingsVariant) {
                            showAccept(placeName, personName, placeID, inviteeEmail)
                        } else {
                            val inviteeFirst = invite["inviteeFirstName"] as String?
                            val inviteeLast = invite["inviteeLastName"] as String?
                            val contact = DeviceContact()
                            contact.addEmailAddress(
                                etEmailAddress.text.toString(),
                                resources.getString(R.string.type_home)
                            )
                            contact.firstName = inviteeFirst
                            contact.lastName = inviteeLast
                            contact.placeID = placeID
                            contact.validationCode = etInvitation.text.toString()
                            contact.invitationEmail = inviteeEmail
                            contact.invitedPlaceName = placeName
                            contact.invitorFirstName = first
                            contact.invitorLastName = last
                            BackstackManager
                                .getInstance()
                                .navigateToFragment(InvitationSuccessFragment.newInstance(contact), true)
                        }
                    })
                .onFailure(Listeners.runOnUiThread { showInvalid() })
        })
    }

    private fun showAccept(
        toPlace: String,
        fromPerson: String,
        forPlaceID: String,
        inviteeEmail: String?
    ) {
        progressContainer.isGone = true
        next.isEnabled = true
        val code = etInvitation.text.toString()
        val email = etEmailAddress.text.toString()
        GenericInformationPopup
            .newInstance(
                getString(R.string.accept_invitation_title),
                getString(R.string.accept_invitation_desc, toPlace, fromPerson),
                getString(R.string.accept),
                getString(R.string.decline)
            )
            .setTopButtonListener {
                val personModel = SessionController.instance().person
                if (personModel == null) {
                    onError(RuntimeException("Lost our logged in person. Cannot accept/decline invite."))
                    return@setTopButtonListener
                }
                progressContainer.isVisible = true
                next.isEnabled = false
                personModel
                    .acceptInvitation(code, email)
                    .onFailure(genericErrorListener)
                    .onSuccess(
                        Listeners.runOnUiThread {
                            val contact = DeviceContact()
                            contact.addEmailAddress(
                                etEmailAddress.text.toString(),
                                resources.getString(R.string.type_home)
                            )
                            contact.firstName = personModel.firstName
                            contact.lastName = personModel.lastName
                            contact.placeID = forPlaceID
                            contact.validationCode = etInvitation.text.toString()
                            contact.invitationEmail = inviteeEmail
                            AccountCreationSequenceController(
                                AccountTypeSequence.CURRENT_USER_INVITE_ACCEPT,
                                contact
                            ).startSequence(activity, null, SettingsUpdatePin::class.java)
                        })
            }
            .setBottomButtonListener {
                val personModel = SessionController.instance().person
                if (personModel == null) {
                    onError(RuntimeException("Lost our logged in person. Cannot accept/decline invite."))
                    return@setBottomButtonListener
                }
                progressContainer.isVisible = true
                next.isEnabled = false
                personModel
                    .rejectInvitation(code, email, null) // No "Reason" for now.
                    .onFailure(genericErrorListener)
                    .onSuccess(
                        Listeners.runOnUiThread {
                            // TODO: Should this really go back to here?...
                            progressContainer.isGone = true
                            BackstackManager
                                .withAnimation(TransitionEffect.FADE)
                                .navigateBackToFragment(SideNavSettingsFragment.newInstance())
                        })
            }
            .show(fragmentManager)
    }

    private fun onError(throwable: Throwable?) {
        errorIn(activity).showGenericBecauseOf(throwable)
    }

    private fun parseErrorException(ex: ErrorResponseException) {
        val NOT_FOUND = "request.destination.notfound"
        val prefix = "place"
        val code = ex.code.toString().toLowerCase(Locale.getDefault())
        val reason = ex.errorMessage.toString().toLowerCase(Locale.getDefault()).trim { it <= ' ' }
        if (code == NOT_FOUND && reason.startsWith(prefix)) {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.invite_not_valid_title)
                .setMessage(R.string.invite_not_valid_desc)
                .setPositiveButton(android.R.string.ok, null)
                .create()
                .show()
        } else {
            onError(ex)
        }
    }

    private fun showInvalid() {
        progressContainer.isGone = true
        next.isEnabled = true
        val notValidText = if (isSettingsVariant) {
            getString(R.string.code_not_valid_desc)
        } else {
            getString(R.string.code_not_valid_short_desc)
        }
        Generic1ButtonErrorPopup
            .newInstance(
                getString(R.string.code_not_valid_title),
                notValidText,
                "",
                "",
                getString(android.R.string.ok)
            )
            .show(fragmentManager)
    }

    override fun onPause() {
        super.onPause()
        progressContainer.isGone = true
        next.isEnabled = true
    }

    fun handleBackPress() { // Invoked from BaseActivity only it seems.
        LaunchActivity.startLoginScreen(activity)
    }

    companion object {
        private const val EMAIL_ADDRESS = "EMAIL_ADDRESS"
        private const val INVITATION_CODE = "INVITATION_CODE"
        const val IS_SETTINGS = "IS_SETTINGS"

        @JvmStatic
        fun newInstance(
            emailAddress: String?,
            invitationCode: String?,
            firstName: String?,
            lastName: String?
        ): InvitationFragment = InvitationFragment().apply {
            arguments = with(Bundle(2)) {
                putString(EMAIL_ADDRESS, emailAddress)
                putString(INVITATION_CODE, invitationCode)
                this
            }
        }

        @JvmStatic
        fun newInstanceFromSettings(): InvitationFragment = InvitationFragment().apply {
            arguments = with(Bundle(1)) {
                putBoolean(IS_SETTINGS, true)
                this
            }
        }
    }
}

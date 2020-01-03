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
package arcus.app.account.settings.contact

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.telephony.PhoneNumberFormattingTextWatcher
import android.text.InputFilter
import android.text.InputFilter.LengthFilter
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import androidx.core.view.isVisible
import arcus.app.R
import arcus.app.account.settings.SettingsUpdatePassword
import arcus.app.common.backstack.BackstackManager
import arcus.app.common.error.ErrorManager.`in` as errorIn
import arcus.app.common.error.type.PersonErrorType
import arcus.app.common.fragments.NoViewModelFragment
import arcus.app.common.utils.CorneaUtils
import arcus.app.common.validation.EmailValidator
import arcus.app.common.validation.NotEmptyValidator
import arcus.app.common.validation.PhoneNumberValidator
import arcus.app.createaccount.EMAIL_IN_USE_UPDATE
import arcus.cornea.controller.PersonController
import com.google.android.material.textfield.TextInputLayout
import com.iris.client.capability.Person
import com.iris.client.exception.ErrorResponseException
import com.iris.client.model.PersonModel

// TODO: This class was just converted to Kotlin and still needs significant work to update it.
class SettingsContactInfoFragment : NoViewModelFragment(), PersonController.Callback {
    private var mMenuItem: MenuItem? = null
    private lateinit var firstNameContainer: TextInputLayout
    private lateinit var firstName: EditText
    private lateinit var lastNameContainer: TextInputLayout
    private lateinit var lastName: EditText
    private lateinit var phoneContainer: TextInputLayout
    private lateinit var phone: EditText
    private lateinit var emailContainer: TextInputLayout
    private lateinit var email: EditText
    private lateinit var confirmEmailContainer: TextInputLayout
    private lateinit var confirmEmail: EditText
    private lateinit var changePasswordContainer: View
    private var emailAndPhoneOptional = false
    private val screenVariant: ScreenVariant
        get() = arguments?.getSerializable(SCREEN_VARIANT) as ScreenVariant? ?: ScreenVariant.HIDE_PASSWORD_EDIT

    enum class ScreenVariant {
        SHOW_PASSWORD_EDIT, HIDE_PASSWORD_EDIT
    }

    override val title: String
        get() = getString(R.string.contact_info_title)
    override val layoutId: Int = R.layout.fragment_contact_info
    override val menuId: Int? get() = R.menu.menu_edit

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firstNameContainer = view.findViewById(R.id.fragment_contact_firstName_container)
        firstName = view.findViewById(R.id.fragment_contact_firstName)

        lastNameContainer = view.findViewById(R.id.fragment_contact_lastName_container)
        lastName = view.findViewById(R.id.fragment_contact_lastName)

        phoneContainer = view.findViewById(R.id.fragment_contact_phone_number_container)
        phone = view.findViewById(R.id.fragment_contact_phone_number)
        phone.filters = arrayOf<InputFilter>(LengthFilter(14))
        phone.addTextChangedListener(PhoneNumberFormattingTextWatcher())

        emailContainer = view.findViewById(R.id.fragment_contact_email_container)
        email = view.findViewById(R.id.fragment_contact_email)

        confirmEmailContainer = view.findViewById(R.id.fragment_contact_confirm_email_container)
        confirmEmail = view.findViewById(R.id.fragment_contact_confirm_email)

        changePasswordContainer = view.findViewById<View>(R.id.change_password_container)
        view.findViewById<View>(R.id.fragment_contact_password_star).setOnClickListener {
            BackstackManager.getInstance().navigateToFragment(SettingsUpdatePassword.newInstance(), true)
        }

        arguments?.let { args ->
            if (screenVariant == ScreenVariant.HIDE_PASSWORD_EDIT) {
                changePasswordContainer.isVisible = false
            }

            args.getString(PERSON_ADDRESS)?.let { personAddress ->
                PersonController.instance().edit(personAddress, this)
            }
        }
        enableInput(false)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        mMenuItem = item
        val editString = getString(R.string.contact_info_edit_btn)
        val doneString = getString(R.string.contact_info_done_btn)

        if (item.itemId == R.id.menu_edit_contact) {
            val isEditing = item.title.toString() == editString
            val legit = validate()
            item.title = if (isEditing && legit) doneString else editString
            if (!isEditing) {
                if (saveData()) {
                    item.title = editString
                    enableInput(false)
                } else {
                    item.title = doneString
                    enableInput(true)
                }
            } else {
                item.title = doneString
                enableInput(true)
            }
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun enableInput(isEditing: Boolean) {
        firstName.isEnabled = isEditing
        lastName.isEnabled = isEditing
        phone.isEnabled = isEditing
        email.isEnabled = isEditing
        confirmEmailContainer.isVisible = isEditing
        if (screenVariant == ScreenVariant.SHOW_PASSWORD_EDIT) {
            changePasswordContainer.isVisible = !isEditing
        }
    }

    private fun saveData(): Boolean {
        val legit = validate()
        if (legit) {
            if (emailAndPhoneOptional && phone.text.isEmpty()) {
                errorIn(activity).show(PersonErrorType.CANT_NOTIFY_HOBBIT_WITHOUT_PHONE)
            }
            submit()
        }
        return legit
    }

    private fun validate(): Boolean {
        var dataIsValid = true
        val fistNameValidator = NotEmptyValidator(
            firstNameContainer,
            firstName,
            getString(R.string.account_registration_first_name_blank_error)
        )
        val lastNameValidator = NotEmptyValidator(
            lastNameContainer,
            lastName,
            getString(R.string.account_registration_last_name_blank_error)
        )
        val phoneNumberValidator = PhoneNumberValidator(activity, phoneContainer, phone)
        val emailValidator = EmailValidator(emailContainer, email)

        if (!fistNameValidator.isValid || !lastNameValidator.isValid) {
            dataIsValid = false
        }
        // Not Optional && Not Valid
        // Optional, Populated, and Not Valid
        if (!emailAndPhoneOptional && !phoneNumberValidator.isValid ||
            emailAndPhoneOptional && phone.text.isNotEmpty() && !phoneNumberValidator.isValid
        ) {
            dataIsValid = false
        }
        // Not Optional && Not Valid
        // Optional, Populated, and Not Valid
        if ((!emailAndPhoneOptional || emailAndPhoneOptional && email.text.isNotEmpty()) && !emailValidator.isValid) {
            dataIsValid = false
        } else if (email.text.toString() != confirmEmail.text.toString()) {
            confirmEmailContainer.error = resources.getString(R.string.email_err_not_equal)
            dataIsValid = false
        }
        return dataIsValid
    }

    private fun submit(): Boolean {
        PersonController.instance()[Person.ATTR_FIRSTNAME] = firstName.text.toString()
        PersonController.instance()[Person.ATTR_LASTNAME] = lastName.text.toString()
        PersonController.instance()[Person.ATTR_MOBILENUMBER] = phone.text.toString()
        PersonController.instance()[Person.ATTR_EMAIL] = email.text.toString()
        PersonController.instance().updatePerson()
        return true
    }

    override fun showLoading() {
        progressContainer.isVisible = true
    }

    override fun updateView(personModel: PersonModel) {
        progressContainer.isVisible = false
        updateViews(personModel)
    }

    override fun onModelLoaded(personModel: PersonModel) {
        progressContainer.isVisible = false
        updateViews(personModel)
    }

    override fun onModelsLoaded(personList: List<PersonModel>) { // Nothing to do
    }

    override fun onError(throwable: Throwable) {
        progressContainer.isVisible = false
        if (throwable is ErrorResponseException) {
            when (throwable.code) {
                EMAIL_IN_USE_UPDATE -> {
                    showEmailInUseDialog()
                    // Go to Edit mode and update the Menu label
                    enableInput(true)
                    onOptionsItemSelected(mMenuItem!!)
                }
            }
        } else {
            errorIn(activity).showGenericBecauseOf(throwable)
        }
    }

    override fun createdModelNotFound() { // Nothing to do
    }

    private fun showEmailInUseDialog() {
        activity?.let {
            val dialogBuilder = AlertDialog.Builder(it)
            dialogBuilder.setCancelable(false)
            dialogBuilder.setMessage(getString(R.string.email_not_available_text))
            dialogBuilder.setTitle(getString(R.string.email_already_registered_title))
            dialogBuilder.setNegativeButton(
                getString(R.string.ok)
            ) { dialog: DialogInterface, _: Int -> dialog.dismiss() }
            dialogBuilder.create().show()
        }
    }

    private fun updateViews(personModel: PersonModel) {
        firstName.setText(personModel.firstName)
        lastName.setText(personModel.lastName)
        phone.setText(personModel.mobileNumber)
        email.setText(personModel.email)
        confirmEmail.setText(personModel.email)

        // Hobbits don't require email or phone
        emailAndPhoneOptional = CorneaUtils.isHobbit(personModel)
    }

    companion object {
        private const val PERSON_ADDRESS = "PERSON_ADDRESS"
        private const val SCREEN_VARIANT = "SCREEN_VARIANT"

        @JvmStatic
        fun newInstance(
            personAddress: String?,
            variant: ScreenVariant?
        ): SettingsContactInfoFragment = SettingsContactInfoFragment().apply {
            arguments = Bundle(2).apply {
                putString(PERSON_ADDRESS, personAddress)
                putSerializable(SCREEN_VARIANT, variant)
            }
        }
    }
}

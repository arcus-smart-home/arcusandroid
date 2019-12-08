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
package arcus.app.createaccount.nameandphone

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.telephony.PhoneNumberFormattingTextWatcher
import android.text.InputFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import arcus.app.R
import arcus.app.common.image.ImageCategory
import arcus.app.common.image.ImageManager
import arcus.app.common.image.ImageRepository
import arcus.app.common.image.picasso.transformation.CropCircleTransformation
import arcus.app.common.view.CircularImageView
import android.widget.Button
import android.widget.EditText
import arcus.app.common.utils.clearErrorsOnFocusChangedTo
import arcus.app.common.validation.NotEmptyValidator
import arcus.app.common.validation.PhoneNumberValidator
import arcus.app.createaccount.CreateAccountFlow
import com.google.android.material.textfield.TextInputLayout

class NameAndPhoneEntryFragment : Fragment(), NameAndPhoneEntryView {
    private lateinit var callback : CreateAccountFlow
    private var saveLocation : String? = null
    private val leadingSpaceFilter = InputFilter { source, _, _, dest, _, _ ->
        // If we have 0 characters entered, replace any leading spaces right away
        // otherwise allow any input
        if (dest?.length == 0 && source?.matches(MULTI_SPACES_FROM_START) == true) {
            source.replace(MULTI_SPACES_FROM_START, "")
        } else {
            source
        }
    }

    private lateinit var personImage  : CircularImageView
    private lateinit var nextButton   : Button
    private lateinit var firstName    : EditText
    private lateinit var firstNameContainer : TextInputLayout
    private lateinit var lastName     : EditText
    private lateinit var lastNameContainer : TextInputLayout
    private lateinit var phoneNumber  : EditText
    private lateinit var phoneNumberContainer : TextInputLayout
    private lateinit var focusHog     : View

    private val presenter : NameAndPhoneEntryPresenter = NameAndPhoneEntryPresenterImpl()

    val userInfo : NamePhoneAndImageLocation
        get() = NamePhoneAndImageLocation(
            firstName.text.toString(),
            lastName.text.toString(),
            phoneNumber.text.toString(),
            saveLocation
        )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_name_phone_entry, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        focusHog = view.findViewById(R.id.focus_hog)
        presenter.setView(this)
        personImage = view.findViewById(R.id.person_image)
        view.findViewById<ImageView>(R.id.camera_image).setOnClickListener {
            context?.run {
                ImageManager.with(this)
                    .putUserGeneratedPersonImage(presenter.getGeneratedPersonId())
                    .fromCameraOrGallery()
                    .withSaveLocationListener {
                        saveLocation = it?.toString()
                    }
                    .withTransform(CropCircleTransformation())
                    .into(personImage)
                    .execute()
            }
        }

        if (ImageRepository.imageExists(context, ImageCategory.PERSON, null, presenter.getGeneratedPersonId())) {
            ImageManager
                .with(context)
                .putLargePersonImage(presenter.getGeneratedPersonId())
                .withTransform(CropCircleTransformation())
                .into(personImage)
                .execute()
        }

        firstNameContainer = view.findViewById(R.id.first_name_container)
        firstName = view.findViewById(R.id.first_name)
        firstName.filters = arrayOf(leadingSpaceFilter)
        firstNameContainer clearErrorsOnFocusChangedTo firstName

        lastNameContainer = view.findViewById(R.id.last_name_container)
        lastName = view.findViewById(R.id.last_name)
        lastName.filters = arrayOf(leadingSpaceFilter)
        lastNameContainer clearErrorsOnFocusChangedTo lastName

        phoneNumberContainer = view.findViewById(R.id.phone_number_container)
        phoneNumber = view.findViewById(R.id.phone_number)
        phoneNumber.addTextChangedListener(PhoneNumberFormattingTextWatcher())
        phoneNumberContainer clearErrorsOnFocusChangedTo phoneNumber

        nextButton = view.findViewById(R.id.next_button)
        nextButton.setOnClickListener {
            focusHog.requestFocusFromTouch()
            val firstNameOk = NotEmptyValidator(firstNameContainer, firstName, getString(R.string.missing_first_name)).isValid
            val lastNameOk = NotEmptyValidator(lastNameContainer, lastName, getString(R.string.missing_last_name)).isValid
            val phoneNumberOk = PhoneNumberValidator(context, phoneNumberContainer, phoneNumber).isValid
            if (firstNameOk && lastNameOk && phoneNumberOk) {
                callback.nextFrom(this)
            }
        }

        view.findViewById<Button>(R.id.cancel_button).setOnClickListener {
            callback.finishFlow()
        }
    }

    override fun onStart() {
        super.onStart()
        activity?.setTitle(R.string.lets_get_acquainted)
        callback.allowBackButton()
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.cleanUp()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        check(context is CreateAccountFlow) { "Dev: Make sure the container implements CreateAccountFlow." }
        callback = context
    }

    companion object {
        @JvmStatic
        fun newInstance() = NameAndPhoneEntryFragment()

        @JvmField
        val MULTI_SPACES_FROM_START = "^\\s+".toRegex()
    }
}

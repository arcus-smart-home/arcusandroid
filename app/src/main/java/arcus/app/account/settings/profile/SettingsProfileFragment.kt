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
package arcus.app.account.settings.profile

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import arcus.app.R
import arcus.app.account.registration.AccountSecurityQuestionsFragment
import arcus.app.account.settings.places.SelectPlaceFragment
import arcus.app.account.settings.fingerprint.SettingsFingerprintFragment
import arcus.app.account.settings.terms.SettingsTermsOfUseFragment
import arcus.app.account.settings.billing.SettingsBillingFragment
import arcus.app.account.settings.contact.SettingsContactInfoFragment
import arcus.app.account.settings.marketing.SettingsMarketingFragment
import arcus.app.account.settings.notifications.SettingsPushNotificationsFragment
import arcus.app.account.settings.remove.SettingsRemoveFragment
import arcus.app.common.backstack.BackstackManager
import arcus.app.common.fragments.NoViewModelFragment
import arcus.app.common.image.ImageManager
import arcus.app.common.image.picasso.transformation.CropCircleTransformation
import arcus.app.common.utils.BiometricLoginUtils
import arcus.cornea.SessionController
import arcus.cornea.model.PlacesWithRoles
import com.iris.client.model.PersonModel

// TODO: This class was just converted to Kotlin and still needs significant work to update it.
class SettingsProfileFragment : NoViewModelFragment(), View.OnClickListener {
    private lateinit var personName: TextView
    private lateinit var personImage: ImageView
    private lateinit var settingsButton: ImageView
    private lateinit var contactInfoContainer: View
    private lateinit var securityContainer: View
    private lateinit var pinContainer: View
    private lateinit var fingerPrintContainer: View
    private lateinit var pushContainer: View
    private lateinit var billingContainer: View
    private lateinit var marketingContainer: View
    private lateinit var termsContainer: View
    private lateinit var deleteContainer: View
    private lateinit var placesWithRoles: PlacesWithRoles
    private lateinit var personModel: PersonModel

    override val title: String
        get() = getString(R.string.profile)
    override val layoutId: Int = R.layout.fragment_profile_settings

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { args ->
            placesWithRoles = args.getParcelable(PLACE_ROLE) as PlacesWithRoles
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        personImage = view.findViewById(R.id.fragment_account_camera)
        settingsButton = view.findViewById(R.id.camera_image)
        personName = view.findViewById(R.id.account_settings_person_name)
        contactInfoContainer = view.findViewById(R.id.contact_info_container)
        securityContainer = view.findViewById(R.id.security_questions_container)
        pinContainer = view.findViewById(R.id.pin_code_container)
        fingerPrintContainer = view.findViewById(R.id.fingerprint_container)
        pushContainer = view.findViewById(R.id.push_notifications_container)
        billingContainer = view.findViewById(R.id.billing_container)
        marketingContainer = view.findViewById(R.id.marketing_container)
        termsContainer = view.findViewById(R.id.terms_container)
        deleteContainer = view.findViewById(R.id.delete_container)

        // Don't show if not M AND has fingerprint hardware, or uses Pass
        fingerPrintContainer.isGone = true
        if (BiometricLoginUtils.canFingerprint(requireActivity())) {
            fingerPrintContainer.isVisible = true
            fingerPrintContainer.setOnClickListener(this)
        }

        setupView()
    }

    override fun onResume() {
        super.onResume()

        val person = SessionController.instance().person ?: return
        personModel = person

        contactInfoContainer.setOnClickListener(this)
        securityContainer.setOnClickListener(this)
        pinContainer.setOnClickListener(this)
        pushContainer.setOnClickListener(this)
        billingContainer.setOnClickListener(this)
        marketingContainer.setOnClickListener(this)
        termsContainer.setOnClickListener(this)
        deleteContainer.setOnClickListener(this)

        personName.text = String.format(
            "%s %s",
            personModel.firstName.orEmpty(),
            personModel.lastName.orEmpty()
        )
    }

    private fun setupView() {
        val person = SessionController.instance().person ?: return
        personModel = person

        settingsButton.setOnClickListener {
            ImageManager
                .with(activity)
                .putUserGeneratedPersonImage(personModel.address)
                .fromCameraOrGallery()
                .withTransform(CropCircleTransformation())
                .into(personImage)
                .execute()
        }

        ImageManager
            .with(activity)
            .putLargePersonImage(personModel.address)
            .withTransform(CropCircleTransformation())
            .into(personImage)
            .execute()
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.contact_info_container -> SettingsContactInfoFragment
                .newInstance(
                    personModel.address,
                    SettingsContactInfoFragment.ScreenVariant.SHOW_PASSWORD_EDIT
                )
                .navigateTo()
            R.id.security_questions_container -> AccountSecurityQuestionsFragment
                .newInstance(AccountSecurityQuestionsFragment.ScreenVariant.SETTINGS)
                .navigateTo()
            R.id.pin_code_container -> SelectPlaceFragment.newInstance(
                SelectPlaceFragment.PIN_CODE_SCREEN,
                getString(R.string.pin_code_place_selection),
                placesWithRoles
            )
                .navigateTo()
            R.id.fingerprint_container -> if (isVisible) SettingsFingerprintFragment.newInstance().navigateTo()
            R.id.push_notifications_container -> SettingsPushNotificationsFragment.newInstance().navigateTo()
            R.id.billing_container -> SettingsBillingFragment.newInstance(placesWithRoles).navigateTo()
            R.id.marketing_container -> SettingsMarketingFragment.newInstance().navigateTo()
            R.id.terms_container -> SettingsTermsOfUseFragment.newInstance().navigateTo()
            R.id.delete_container -> if (placesWithRoles.primaryPlace != null) {
                SettingsRemoveFragment.removeAccountInstance(placesWithRoles.primaryPlace.address).navigateTo()
            } else {
                SettingsRemoveFragment.removeFullAccessAccountInstance().navigateTo()
            }
        }
    }

    private fun Fragment.navigateTo() = BackstackManager.getInstance().navigateToFragment(this, true)

    companion object {
        private const val PLACE_ROLE = "PLACE_ROLE"

        @JvmStatic
        fun newInstance(
            placesWithRoles: PlacesWithRoles
        ): SettingsProfileFragment = SettingsProfileFragment().apply {
            arguments = Bundle(1).apply {
                putParcelable(PLACE_ROLE, placesWithRoles)
            }
        }
    }
}

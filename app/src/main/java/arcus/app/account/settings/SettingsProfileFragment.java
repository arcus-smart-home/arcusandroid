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
package arcus.app.account.settings;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import arcus.cornea.SessionController;
import arcus.cornea.model.PlacesWithRoles;
import com.iris.client.model.PersonModel;
import arcus.app.R;
import arcus.app.account.registration.AccountSecurityQuestionsFragment;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.controller.BackstackPopListener;
import arcus.app.common.fragments.BaseFragment;
import arcus.app.common.image.ImageManager;
import arcus.app.common.image.Wallpaper;
import arcus.app.common.image.picasso.transformation.AlphaPreset;
import arcus.app.common.image.picasso.transformation.CropCircleTransformation;
import arcus.app.common.utils.BiometricLoginUtils;
import arcus.app.common.view.CircularImageView;
import arcus.app.common.view.Version1TextView;

public class SettingsProfileFragment extends BaseFragment implements BackstackPopListener, View.OnClickListener {
    static final String PLACE_ROLE = "PLACE_ROLE";
    Version1TextView personName;
    ImageView personImage, settingsButton;
    View rootView, contactInfoContainer, securityContainer, pinContainer, fingerPrintContainer, pushContainer,
            billingContainer,
            marketingContainer, termsContainer, deleteContainer;
    PlacesWithRoles placesWithRoles;
    transient PersonModel personModel;

    @NonNull
    public static SettingsProfileFragment newInstance(PlacesWithRoles placesWithRoles) {
        SettingsProfileFragment fragment = new SettingsProfileFragment();
        Bundle bundle = new Bundle(1);
        bundle.putParcelable(PLACE_ROLE, placesWithRoles);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            placesWithRoles = getArguments().getParcelable(PLACE_ROLE);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        if (view == null) {
            return null;
        }

        personImage = (CircularImageView) view.findViewById(R.id.fragment_account_camera);
        settingsButton = (ImageView) view.findViewById(R.id.camera_image);
        personName = (Version1TextView) view.findViewById(R.id.account_settings_person_name);

        contactInfoContainer = view.findViewById(R.id.contact_info_container);
        securityContainer = view.findViewById(R.id.security_questions_container);
        pinContainer = view.findViewById(R.id.pin_code_container);
        fingerPrintContainer = view.findViewById(R.id.fingerprint_container);
        pushContainer = view.findViewById(R.id.push_notifications_container);
        billingContainer = view.findViewById(R.id.billing_container);
        marketingContainer = view.findViewById(R.id.marketing_container);
        termsContainer = view.findViewById(R.id.terms_container);
        deleteContainer = view.findViewById(R.id.delete_container);

        rootView = view.findViewById(R.id.profile_settings_container);

        contactInfoContainer.setOnClickListener(this);
        securityContainer.setOnClickListener(this);
        pinContainer.setOnClickListener(this);
        pushContainer.setOnClickListener(this);
        billingContainer.setOnClickListener(this);
        marketingContainer.setOnClickListener(this);
        termsContainer.setOnClickListener(this);
        deleteContainer.setOnClickListener(this);


        // Don't show if not M AND has fingerprint hardware, or uses Pass
        fingerPrintContainer.setVisibility(View.GONE);
        if (BiometricLoginUtils.canFingerprint()) {
            fingerPrintContainer.setVisibility(View.VISIBLE);
            fingerPrintContainer.setOnClickListener(this);
        }

        setupView();
        return view;
    }

    public void onPopped() {
        ImageManager.with(getActivity()).setWallpaper(Wallpaper.ofCurrentPlace().darkened());
    }

    @Override public void onResume() {
        super.onResume();
        setTitle();
        updateUserName();
    }

    protected void updateUserName() {
        personModel = SessionController.instance().getPerson();
        if (personModel == null || placesWithRoles == null) {
            return;
        }
        personName.setText(String.format("%s %s", nonNull(personModel.getFirstName()), nonNull(personModel.getLastName())));
        rootView.setVisibility(View.VISIBLE);
    }

    protected void setupView() {
        personModel = SessionController.instance().getPerson();
        if (personModel == null || placesWithRoles == null) {
            return;
        }

        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(@NonNull View v) {
                ImageManager.with(getActivity())
                      .putUserGeneratedPersonImage(personModel.getAddress())
                      .fromCameraOrGallery()
                      .withTransform(new CropCircleTransformation())
                      .useAsWallpaper(AlphaPreset.DARKEN)
                      .into(personImage)
                      .execute();
            }
        });

        ImageManager.with(getActivity())
              .putLargePersonImage(personModel.getAddress())
              .withTransform(new CropCircleTransformation())
              .into(personImage)
              .execute();

        ImageManager.with(getActivity())
              .putPersonBackgroundImage(personModel.getAddress(), SessionController.instance().getPlaceIdOrEmpty())
              .intoWallpaper(AlphaPreset.DARKEN)
              .execute();
    }

    @Override public void onClick(View v) {
        switch(v.getId()) {
            case R.id.contact_info_container:
                BackstackManager.getInstance().navigateToFragment(SettingsContactInfoFragment.newInstance(personModel.getAddress(), SettingsContactInfoFragment.ScreenVariant.SHOW_PASSWORD_EDIT), true);
                break;
            case R.id.security_questions_container:
                BackstackManager.getInstance().navigateToFragment(AccountSecurityQuestionsFragment.newInstance(AccountSecurityQuestionsFragment.ScreenVariant.SETTINGS), true);
                break;
            case R.id.pin_code_container:
                BackstackManager.getInstance().navigateToFragment(SelectPlaceFragment.newInstance(SelectPlaceFragment.PIN_CODE_SCREEN, getString(R.string.pin_code_place_selection), placesWithRoles), true);
                break;
            case R.id.fingerprint_container:
                if (isVisible()) {
                    BackstackManager.getInstance().navigateToFragment(SettingsFingerprintFragment.newInstance(), true);
                 break;}
                else break;
            case R.id.push_notifications_container:
                BackstackManager.getInstance().navigateToFragment(SettingsPushNotificationsFragment.newInstance(), true);
                break;
            case R.id.billing_container:
                BackstackManager.getInstance().navigateToFragment(SettingsBillingFragment.newInstance(placesWithRoles), true);
                break;
            case R.id.marketing_container:
                BackstackManager.getInstance().navigateToFragment(SettingsMarketingFragment.newInstance(), true);
                break;
            case R.id.terms_container:
                BackstackManager.getInstance().navigateToFragment(SettingsTermsOfUseFragment.newInstance(), true);
                break;
            case R.id.delete_container:
                if (placesWithRoles.getPrimaryPlace() != null) {
                    BackstackManager.getInstance().navigateToFragment(SettingsRemoveFragment.removeAccountInstance(placesWithRoles.getPrimaryPlace().getAddress()), true);
                }
                else {
                    BackstackManager.getInstance().navigateToFragment(SettingsRemoveFragment.removeFullAccessAccountInstance(), true);
                }
                break;

            default:
                break;
        }
    }

    @Override public void onPause() {
        super.onPause();
        if (rootView != null) {
            rootView.setVisibility(View.INVISIBLE);
        }
    }

    protected String nonNull(String string) {
        return TextUtils.isEmpty(string) ? "" : string;
    }

    @Override @Nullable public String getTitle() {
        return getString(R.string.profile).toUpperCase();
    }

    @Override public Integer getLayoutId() {
        return R.layout.fragment_profile_settings;
    }

}

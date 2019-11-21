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
package arcus.app.account.registration;

import android.os.Bundle;
import androidx.annotation.NonNull;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import arcus.cornea.SessionController;
import com.iris.client.model.PersonModel;
import arcus.app.R;
import arcus.app.account.registration.controller.task.ArcusTask;
import arcus.app.account.registration.controller.task.SavePersonNameTask;
import arcus.app.common.image.ImageManager;
import arcus.app.common.image.picasso.transformation.AlphaPreset;
import arcus.app.common.image.picasso.transformation.CropCircleTransformation;
import arcus.app.common.validation.PhoneNumberValidator;
import arcus.app.common.view.Version1EditText;
import arcus.app.subsystems.people.model.DeviceContact;

import org.apache.commons.lang3.StringUtils;


public class AccountAboutYouFragment extends AccountCreationStepFragment implements ArcusTask.ArcusTaskListener {

    private Version1EditText firstNameField;
    private Version1EditText lastNameField;
    private Version1EditText mobileNumberField;
    private ImageView cameraBtn;

    @NonNull
    public static AccountAboutYouFragment newInstance(){
        AccountAboutYouFragment fragment = new AccountAboutYouFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view =  super.onCreateView(inflater, container, savedInstanceState);

        firstNameField = (Version1EditText) view.findViewById(R.id.fragment_account_name_firstName);
        lastNameField = (Version1EditText) view.findViewById(R.id.fragment_account_name_lastName);
        mobileNumberField = (Version1EditText) view.findViewById(R.id.fragment_account_phone_number);

        // including the "1 " and "-" marks that get added by the PhoneNumberFormattingTextWatcher
        mobileNumberField.setFilters(new InputFilter[]{new InputFilter.LengthFilter(14)});
        mobileNumberField.addTextChangedListener(new PhoneNumberFormattingTextWatcher());

        cameraBtn = (ImageView) view.findViewById(R.id.fragment_account_camera);
        FrameLayout photoLayout = (FrameLayout) view.findViewById(R.id.photo_layout);

        // Initialize the circular image with a person avatar illustration
        ImageManager.with(getActivity())
                .putDrawableResource(R.drawable.image_user)
                .fit()
                .into(cameraBtn)
                .execute();

        photoLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PersonModel personModel = SessionController.instance().getPerson();
                if (personModel == null) {
                    return;
                }

                ImageManager.with(getActivity())
                        .putUserGeneratedPersonImage(personModel.getId())
                        .fromCameraOrGallery()
                        .withTransform(new CropCircleTransformation())
                        .useAsWallpaper(AlphaPreset.LIGHTEN)
                        .into(cameraBtn)
                        .execute();
            }
        });

        DeviceContact contact = getController().getDeviceContact();
        if(contact != null) {
            firstNameField.setText(contact.getFirstName());
            lastNameField.setText(contact.getLastName());
        }

        return view;
    }

    @Override
    public String getTitle() {
        return getString(R.string.account_registration_your_information);
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_account_name;
    }

    @Override
    public boolean validate() {
        boolean formOk = true;

        if(StringUtils.isBlank(firstNameField.getText())) {
            firstNameField.setError(getActivity().getString(R.string.requiredField, firstNameField.getHint()));
            formOk = false;
        }

        if(StringUtils.isBlank(lastNameField.getText())) {
            lastNameField.setError(getActivity().getString(R.string.requiredField, lastNameField.getHint()));
            formOk = false;
        }

        if (! new PhoneNumberValidator(getActivity(), mobileNumberField).isValid()) {
            formOk = false;
        }

        return formOk;
    }


    @Override
    public boolean submit() {
        registrationContext.setFirstName(firstNameField.getText().toString());
        registrationContext.setLastName(lastNameField.getText().toString());
        registrationContext.setMobileNumber(mobileNumberField.getText().toString());

        new SavePersonNameTask(getActivity(),this,this,getCorneaService(),registrationContext).execute();
        return true;
    }
}

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
package arcus.app.subsystems.people;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import arcus.cornea.controller.PersonController;
import com.iris.client.capability.Person;
import com.iris.client.model.PersonModel;
import arcus.app.R;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.error.ErrorManager;
import arcus.app.common.image.picasso.transformation.BlackWhiteInvertTransformation;
import arcus.app.common.image.picasso.transformation.Invert;
import arcus.app.common.popups.YesNoPopupColored;
import arcus.app.common.sequence.SequencedFragment;
import arcus.app.common.image.ImageManager;
import arcus.app.common.image.picasso.transformation.CropCircleTransformation;
import arcus.app.common.validation.PhoneNumberValidator;
import arcus.app.common.view.CircularImageView;
import arcus.app.common.view.Version1Button;
import arcus.app.common.view.Version1EditText;
import arcus.app.subsystems.people.controller.NewPersonSequenceController;

import org.apache.commons.lang3.StringUtils;

import java.util.List;


public class PersonPhotoFragment extends SequencedFragment<NewPersonSequenceController> implements PersonController.Callback, YesNoPopupColored.Callback {

    private final static String PERSON_ADDRESS = "PERSON_ADDRESS";

    private CircularImageView photo;
    private FrameLayout photoClickRegion;
    private Version1EditText phoneNumber;
    private Version1Button nextButton;
    private String modelID;
    private PersonPhotoFragment fragment;

    @NonNull
    public static PersonPhotoFragment newInstance () {
        return new PersonPhotoFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        photo = (CircularImageView) view.findViewById(R.id.fragment_account_camera);
        photoClickRegion = (FrameLayout) view.findViewById(R.id.photo_layout);
        phoneNumber = (Version1EditText) view.findViewById(R.id.phone_number);
        nextButton = (Version1Button) view.findViewById(R.id.next_button);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle(getTitle());
        fragment = this;

        final String personAddress = getController().getPersonAddress();

        ImageManager.with(getActivity())
                .putLargePersonImage(personAddress)
                .withPlaceholder(R.drawable.icon_user_large_white)
                .withTransformForStockImages(new BlackWhiteInvertTransformation(Invert.WHITE_TO_BLACK))
                .fit()
                .into(photo)
                .execute();

        phoneNumber.setFilters(new InputFilter[]{new InputFilter.LengthFilter(14)});
        phoneNumber.addTextChangedListener(new PhoneNumberFormattingTextWatcher());

        PersonController.instance().edit(personAddress, this);
        photoClickRegion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageManager.with(getActivity())
                        .putUserGeneratedPersonImage(personAddress)
                        .fromCameraOrGallery()
                        .withTransform(new CropCircleTransformation())
                        .into(photo)
                        .execute();
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (StringUtils.isEmpty(phoneNumber.getText().toString())) {
                    YesNoPopupColored popup = YesNoPopupColored.newInstance(getString(R.string.no_phone_number), getString(R.string.no_phone_number_description));
                    popup.setCallback(fragment);
                    BackstackManager.getInstance().navigateToFloatingFragment(popup, popup.getClass().getSimpleName(), true);
                } else {
                    if (isValidInput()) {
                        PersonController.instance().updatePerson();
                    }
                }
            }
        });
    }

    private boolean isValidInput () {

        if (!StringUtils.isEmpty(phoneNumber.getText().toString())) {
            if (new PhoneNumberValidator(getActivity(), phoneNumber).isValid()) {
                PersonController.instance().set(Person.ATTR_MOBILENUMBER, phoneNumber.getText().toString());
                return true;
            }
            return false;
        }

        return true;
    }

    @Override
    public String getTitle() {
        return getString(R.string.people_add_a_person);
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_person_photo;
    }

    @Override
    public void showLoading() {
        hideProgressBar();
        showProgressBarAndDisable(nextButton);
        nextButton.setEnabled(false);
    }

    @Override
    public void updateView(PersonModel personModel) {
        hideProgressBarAndEnable(nextButton);
        goNext();
    }

    @Override
    public void onModelLoaded(@Nullable PersonModel personModel) {
        hideProgressBarAndEnable(nextButton);
        if (personModel != null) {
            modelID = personModel.getId();
        }
        else {
            modelID = "";
        }
    }

    @Override
    public void onError(Throwable throwable) {
        hideProgressBarAndEnable(nextButton);
        ErrorManager.in(getActivity()).showGenericBecauseOf(throwable);
    }

    @Override
    public void onModelsLoaded(@NonNull List<PersonModel> personList) {}

    @Override
    public void createdModelNotFound() {}

    @Override // Disable back press.
    public boolean onBackPressed() {
        return true;
    }

    @Override
    public void yes() {
        BackstackManager.getInstance().navigateBack();
        if (isValidInput()) {
            PersonController.instance().updatePerson();
        }
    }

    @Override
    public void no() {
        BackstackManager.getInstance().navigateBack();
    }
}

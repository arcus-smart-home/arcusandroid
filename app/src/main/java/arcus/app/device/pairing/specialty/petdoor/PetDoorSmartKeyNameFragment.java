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
package arcus.app.device.pairing.specialty.petdoor;

import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import arcus.app.R;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.error.ErrorManager;
import arcus.app.common.image.ImageManager;
import arcus.app.common.image.picasso.transformation.BlackWhiteInvertTransformation;
import arcus.app.common.image.picasso.transformation.CropCircleTransformation;
import arcus.app.common.image.picasso.transformation.Invert;
import arcus.app.common.sequence.SequencedFragment;
import arcus.app.common.view.CircularImageView;
import arcus.app.common.view.Version1Button;
import arcus.app.common.view.Version1ButtonColor;
import arcus.app.common.view.Version1EditText;
import arcus.app.common.view.Version1TextView;
import arcus.app.device.pairing.specialty.petdoor.controller.PetDoorKeyModificationController;
import arcus.app.subsystems.alarm.AlertFloatingFragment;


public class PetDoorSmartKeyNameFragment extends SequencedFragment implements PetDoorKeyModificationController.Callbacks {

    private final static String PET_DOOR_ADDRESS = "PET_DOOR_ADDRESS";
    private final static String KEY_TOKEN_ID = "TOKEN_ID";
    private final static String EDIT_MODE = "EDIT_MODE";

    private Version1Button saveButton;
    private Version1Button removeButton;
    private CircularImageView deviceImage;
    private FrameLayout cameraClickableRegion;
    private Version1EditText petName;
    private Version1TextView petNameCopy;

    public static PetDoorSmartKeyNameFragment newInstance (String petDoorAddress, int tokenId, boolean isEditMode) {
        PetDoorSmartKeyNameFragment instance = new PetDoorSmartKeyNameFragment();
        Bundle arguments = new Bundle();
        arguments.putString(PET_DOOR_ADDRESS, petDoorAddress);
        arguments.putInt(KEY_TOKEN_ID, tokenId);
        arguments.putBoolean(EDIT_MODE, isEditMode);
        instance.setArguments(arguments);

        return instance;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        saveButton = (Version1Button) view.findViewById(R.id.save_button);
        removeButton = (Version1Button) view.findViewById(R.id.remove_button);
        deviceImage = (CircularImageView) view.findViewById(R.id.fragment_account_camera);
        cameraClickableRegion = (FrameLayout) view.findViewById(R.id.photo_layout);
        petName = (Version1EditText) view.findViewById(R.id.pet_name);
        petNameCopy = (Version1TextView) view.findViewById(R.id.pet_name_copy);

        return view;
    }

    @Override
    public void onPause () {
        super.onPause();
        PetDoorKeyModificationController.getInstance().removeListener();
    }

    @Override
    public void onResume () {
        super.onResume();

        // Update color scheme and layout based on edit mode
        removeButton.setColorScheme(isEditMode() ? Version1ButtonColor.WHITE : Version1ButtonColor.BLACK);
        removeButton.setVisibility(isEditMode() ? View.VISIBLE : View.GONE);
        saveButton.setColorScheme(isEditMode() ? Version1ButtonColor.WHITE : Version1ButtonColor.BLACK);
        petNameCopy.setTextColor(isEditMode() ? Color.WHITE : Color.BLACK);
        petName.useLightColorScheme(isEditMode());

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PetDoorKeyModificationController.getInstance().setPetName(getPetDoorAddress(), getTokenId(), petName.getText().toString());
            }
        });

        removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRemoveConfirmationPrompt();
            }
        });

        cameraClickableRegion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageManager.with(getActivity())
                        .putUserGeneratedPetImage(String.valueOf(getTokenId()))
                        .fromCameraOrGallery()
                        .withTransform(new CropCircleTransformation())
                        .into(deviceImage)
                        .execute();
            }
        });

        if (isEditMode()) {
            ImageManager.with(getActivity())
                    .putLargePetImage(String.valueOf(getTokenId()))
                    .withTransformForStockImages(new BlackWhiteInvertTransformation(Invert.BLACK_TO_WHITE))
                    .withTransform(new CropCircleTransformation())
                    .into(deviceImage)
                    .execute();
        } else {
            ImageManager.with(getActivity())
                    .putLargePetImage(String.valueOf(getTokenId()))
                    .withTransform(new CropCircleTransformation())
                    .into(deviceImage)
                    .execute();
        }

        PetDoorKeyModificationController.getInstance().setListener(this);
        PetDoorKeyModificationController.getInstance().loadPetName(getPetDoorAddress(), getTokenId());
    }

    @Nullable
    @Override
    public String getTitle() {
        return getString(R.string.petdoor_smart_key);
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_pet_door_smart_key_name;
    }

    private void showRemoveConfirmationPrompt () {
        final AlertFloatingFragment popup = AlertFloatingFragment.newInstance(getString(R.string.petdoor_are_you_sure), null, getString(R.string.petdoor_are_you_sure_yes), getString(R.string.petdoor_are_you_sure_no), new AlertFloatingFragment.AlertButtonCallback() {
            @Override
            public boolean topAlertButtonClicked() {
                PetDoorKeyModificationController.getInstance().removeSmartKey(getPetDoorAddress(), getTokenId());
                return true;
            }

            @Override
            public boolean bottomAlertButtonClicked() {
                return true;
            }
        });

        BackstackManager.getInstance().navigateToFloatingFragment(popup, popup.getClass().getSimpleName(), true);
    }

    private boolean isEditMode () {
        return getArguments().getBoolean(EDIT_MODE);
    }

    private int getTokenId () {
        return getArguments().getInt(KEY_TOKEN_ID);
    }

    private String getPetDoorAddress () {
        return getArguments().getString(PET_DOOR_ADDRESS);
    }

    @Override
    public void onPetNameLoaded(String petName) {
        this.petName.setText(petName);
    }

    @Override
    public void onSuccess() {
        goNext();
    }

    @Override
    public void onCorneaError(Throwable cause) {
        ErrorManager.in(getActivity()).showGenericBecauseOf(cause);
    }
}

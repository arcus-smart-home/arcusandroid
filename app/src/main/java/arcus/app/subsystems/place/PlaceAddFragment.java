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
package arcus.app.subsystems.place;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import arcus.cornea.CorneaClientFactory;
import arcus.cornea.subsystem.model.TimeZoneModel;
import arcus.cornea.utils.Listeners;
import com.iris.client.bean.StreetAddress;
import com.iris.client.event.Listener;
import com.iris.client.service.PlaceService;
import arcus.app.R;
import arcus.app.activities.BaseActivity;
import arcus.app.common.adapters.SpinnerAdapter;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.error.ErrorManager;
import arcus.app.common.image.ImageManager;
import arcus.app.common.image.UGCImageSelectionListener;
import arcus.app.common.image.picasso.transformation.CropCircleTransformation;
import arcus.app.common.sequence.SequencedFragment;
import arcus.app.common.utils.GlobalSetting;
import arcus.app.common.view.CircularImageView;
import arcus.app.common.view.Version1Button;
import arcus.app.common.view.Version1EditText;
import arcus.app.common.view.Version1TextView;
import arcus.app.device.pairing.post.AddressValidationFragment;
import arcus.app.integrations.Address;
import arcus.app.subsystems.place.controller.NewPlaceSequenceController;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class PlaceAddFragment extends SequencedFragment<NewPlaceSequenceController> implements AddressValidationFragment.AddressValidationCallback, BaseActivity.PermissionCallback {

    private Version1EditText placeName;
    private Version1TextView accountHolderAddr;
    private Version1EditText zipcode;
    private Version1EditText street1;
    private Version1EditText street2;
    private Version1EditText city;
    private FrameLayout cameraClickableRegion;
    private Spinner state;
    private Spinner placeType;
    private CircularImageView placeImage;
    private Version1Button nextButton;
    private ImageView takeAPicture;

    private SpinnerAdapter stateAdapter;

    private PlaceAddFragment fragment;
    private Address address;


    @NonNull
    public static PlaceAddFragment newInstance() {
        return new PlaceAddFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        placeImage = (CircularImageView) view.findViewById(R.id.fragment_account_camera);
        placeImage.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.image_home));

        placeName = (Version1EditText) view.findViewById(R.id.place_name);
        street1 = (Version1EditText) view.findViewById(R.id.fragment_account_billing_street1);
        street1.setTextColor(getResources().getColor(R.color.black));
        street2 = (Version1EditText) view.findViewById(R.id.fragment_account_billing_street2);
        city = (Version1EditText) view.findViewById(R.id.fragment_account_billing_city);
        zipcode = (Version1EditText) view.findViewById(R.id.fragment_account_billing_zipcode);
        state = (Spinner) view.findViewById(R.id.fragment_account_billing_state);
        setUpStateSpinner();

        nextButton = (Version1Button) view.findViewById(R.id.fragment_account_parent_continue_btn);
        takeAPicture = (ImageView) view.findViewById(R.id.camera_image);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle(getTitle());

        fragment = this;
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((BaseActivity)getActivity()).setPermissionCallback(fragment);
                ArrayList<String> permissions = new ArrayList<String>();
                permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
                ((BaseActivity)getActivity()).checkPermission(permissions, GlobalSetting.PERMISSION_ACCESS_COARSE_LOCATION, R.string.permission_rationale_location);
            }
        });

        takeAPicture.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(@NonNull View v) {
                final int id = v.getId();
                switch (id) {
                    case R.id.camera_image:
                        ImageManager.with(getActivity())
                                .putUserGeneratedPersonImage("NEW_PLACE_IMAGE")
                                .withCallback(new UGCImageSelectionListener() {
                                    @Override
                                    public void onUGCImageSelected(Bitmap selectedImage) {
                                        getController().setNewPlaceBitmap(selectedImage);
                                    }
                                })
                                .fromCameraOrGallery()
                                .withTransform(new CropCircleTransformation())
                                .into(placeImage)
                                .execute();
                        break;
                }
            }
        });
    }

    @NonNull
    private Address getAddressFromFormData() {
        address.setStreet(street1.getText().toString());
        address.setStreet2(street2.getText().toString());
        address.setCity(city.getText().toString());
        address.setState(state.getSelectedItem().toString());
        address.setZipCode(zipcode.getText().toString());
        return address;
    }

    @NonNull
    @Override
    public String getTitle() {
        return getResources().getString(R.string.setting_about_your_home);
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_place_add;
    }

    private void setUpStateSpinner() {
        stateAdapter = new SpinnerAdapter(
                getActivity(),
                R.layout.spinner_item_state_closed,
                getResources().getStringArray(R.array.states),
                false);

        state.setAdapter(stateAdapter);
        state.getBackground().setColorFilter(getResources().getColor(R.color.black), PorterDuff.Mode.SRC_ATOP);
    }

    @Override
    public boolean validate() {
        boolean formOk = true;

        TextView errorState;

        if (StringUtils.isBlank(placeName.getText())) {
            placeName.setError(getActivity().getString(R.string.requiredField, placeName.getHint()));
            formOk = false;
        }
        if (StringUtils.isBlank(street1.getText())) {
            street1.setError(getActivity().getString(R.string.requiredField, street1.getHint()));
            formOk = false;
        }

        if (StringUtils.isBlank(city.getText())) {
            city.setError(getActivity().getString(R.string.requiredField, city.getHint()));
            formOk = false;
        }

        if(zipcode.getText().length() < 5) {
            zipcode.setError(getActivity().getString(R.string.requiredField, zipcode.getHint()));
            formOk = false;
        }

        if (state.getSelectedItemPosition() == 0) {
            errorState = (TextView) state.getSelectedView();
            errorState.setError(getActivity().getString(R.string.requiredField, "State"));
            errorState.setTextColor(Color.RED);
            formOk = false;
        }

        return formOk;
    }

    private String getPlaceName() {
        Editable pn = placeName.getText();
        return pn != null ? pn.toString() : "";
    }

    private boolean checkLocationPermission() {
        int res = getContext().checkCallingOrSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION);
        return (res == PackageManager.PERMISSION_GRANTED);
    }

    private Address streetAddressToAddress(StreetAddress input) {
        Address address;
        if(checkLocationPermission()) {
            address = new Address((BaseActivity)getActivity());
        } else {
            address = new Address();
        }
        address.setStreet(input.getLine1());
        if(input.getLine2() != null) {
            address.setStreet2(input.getLine2());
        }
        address.setCity(input.getCity());
        address.setState(input.getState());
        address.setZipCode(input.getZip());
        return address;
    }

    @Override
    public void noSuggestionsPromon() {

    }

    @Override
    public void noSuggestionsNonPromon() {

    }

    @Override
    public void useEnteredAddress(Address address, TimeZoneModel timeZone, boolean navigateBack) {
        hideProgressBar();

        if(timeZone != null) {
            address.setDst(timeZone.isUsesDST());
            address.setTimeZoneName(timeZone.getName());
            address.setTimeZoneId(timeZone.getId());
            address.setUtcOffset(timeZone.getOffset());
        }

        getController().setNewPlaceNickname(placeName.getText().toString());
        getController().setNewPlaceAddressEntered(address);
        getController().addNewPlace(new NewPlaceSequenceController.CreatePlaceCallback() {
            @Override public void onSuccess() {
                hideProgressBar();
                goNext();
            }

            @Override public void onError(Throwable throwable) {
                hideProgressBar();
                ErrorManager.in(getActivity()).showGenericBecauseOf(throwable);
            }
        });
    }

    @Override
    public void useSuggestedAddress(Address address, TimeZoneModel timeZone) {
        hideProgressBar();
        getController().setNewPlaceNickname(placeName.getText().toString());
        getController().setNewPlaceAddressEntered(address);
        getController().addNewPlace(new NewPlaceSequenceController.CreatePlaceCallback() {
            @Override public void onSuccess() {
                hideProgressBar();
                goNext();
            }

            @Override public void onError(Throwable throwable) {
                hideProgressBar();
                ErrorManager.in(getActivity()).showGenericBecauseOf(throwable);
            }
        });
    }

    @Override
    public void permissionsUpdate(int permissionType, ArrayList<String> permissionsDenied, ArrayList<String> permissionsDeniedNeverAskAgain) {
        if(permissionsDenied.contains(Manifest.permission.ACCESS_COARSE_LOCATION)) {
            ((BaseActivity)getActivity()).showSnackBarForPermissions(getString(R.string.permission_location_denied_message));
            address = new Address();
        } else {
            address = new Address((BaseActivity)getActivity());
        }
        getAddressFromFormData();
        if(validate()) {
            StreetAddress addressBean = new StreetAddress();
            addressBean.setLine1(address.getStreet());
            addressBean.setLine2(address.getStreet2());
            addressBean.setCity(address.getCity());
            addressBean.setState(address.getState());
            addressBean.setZip(address.getZipCode());

            PlaceService placeService = CorneaClientFactory.getService(PlaceService.class);
            placeService
                    .validateAddress(null, addressBean.toMap())
                    .onFailure(Listeners.runOnUiThread(new Listener<Throwable>() {
                        @Override
                        public void onEvent(Throwable throwable) {
                            ErrorManager.in(getActivity()).showGenericBecauseOf(throwable);
                        }
                    }))
                    .onSuccess(Listeners.runOnUiThread(new Listener<PlaceService.ValidateAddressResponse>() {
                        @Override
                        public void onEvent(PlaceService.ValidateAddressResponse response) {
                            if(!response.getValid()) {
                                ArrayList<Address> suggestions = new ArrayList<Address>();
                                List<Map<String, Object>> suggestionsResp = response.getSuggestions();
                                for(Map<String, Object> item : suggestionsResp) {
                                    suggestions.add(streetAddressToAddress(new StreetAddress(item)));
                                }

                                AddressValidationFragment addressFragment = AddressValidationFragment.newInstance(address, placeName.getText().toString(), "", "", suggestions, true);
                                addressFragment.setCallback(fragment);
                                BackstackManager.getInstance().navigateToFragment(addressFragment, true);
                            } else {
                                useEnteredAddress(address, null, false);
                            }
                        }
                    }));
        }
    }
}

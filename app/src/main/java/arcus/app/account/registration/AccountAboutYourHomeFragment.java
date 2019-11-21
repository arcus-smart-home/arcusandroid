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

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.NonNull;
import android.text.Editable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import arcus.cornea.CorneaClientFactory;
import arcus.cornea.provider.PlaceModelProvider;
import arcus.cornea.subsystem.model.TimeZoneModel;
import arcus.cornea.utils.Listeners;
import com.iris.client.ClientEvent;
import com.iris.client.bean.StreetAddress;
import com.iris.client.event.Listener;
import com.iris.client.model.PlaceModel;
import com.iris.client.service.PlaceService;
import arcus.app.R;
import arcus.app.account.registration.controller.task.ArcusTask;
import arcus.app.account.registration.controller.task.SaveHomeTask;
import arcus.app.activities.BaseActivity;
import arcus.app.common.adapters.SpinnerAdapter;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.error.ErrorManager;
import arcus.app.common.image.ImageManager;
import arcus.app.common.image.picasso.transformation.AlphaPreset;
import arcus.app.common.image.picasso.transformation.CropCircleTransformation;
import arcus.app.common.utils.GlobalSetting;
import arcus.app.common.view.Version1EditText;
import arcus.app.device.pairing.post.AddressValidationFragment;
import arcus.app.integrations.Address;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class AccountAboutYourHomeFragment extends AccountCreationStepFragment
      implements ArcusTask.ArcusTaskListener, AddressValidationFragment.AddressValidationCallback {

    private Version1EditText homeNickName;
    private Version1EditText street1;
    private Version1EditText street2;
    private Version1EditText city;
    private Spinner state;
    private Version1EditText zipCode;
    private ImageView placeImage;
    private boolean showingTZModal = false;
    private boolean  userEnteredPlace = false;
    private AccountAboutYourHomeFragment fragment;
    private Address address;
    TimeZoneModel timeZone;
    private final Listener<Throwable> errorListener = new Listener<Throwable>() {
        @Override
        public void onEvent(Throwable throwable) {
            ErrorManager.in(getActivity()).showGenericBecauseOf(throwable);
        }
    };
    private final Listener<ClientEvent> successListener = new Listener<ClientEvent>() {
        @Override
        public void onEvent(ClientEvent clientEvent) {
            completed();
        }
    };

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        fragment = this;
        homeNickName = (Version1EditText) view.findViewById(R.id.fragment_account_billing_home_nickname);
        street1 = (Version1EditText) view.findViewById(R.id.fragment_account_billing_street1);
        street2 = (Version1EditText) view.findViewById(R.id.fragment_account_billing_street2);
        city = (Version1EditText) view.findViewById(R.id.fragment_account_billing_city);
        state = (Spinner) view.findViewById(R.id.fragment_account_billing_state);
        zipCode = (Version1EditText) view.findViewById(R.id.fragment_account_billing_zipcode);

        placeImage = (ImageView) view.findViewById(R.id.fragment_account_camera);
        FrameLayout photoLayout = (FrameLayout) view.findViewById(R.id.photo_layout);

        // Initialize circle with home avatar illustration
        ImageManager.with(getActivity())
              .putDrawableResource(R.drawable.image_home)
              .fit()
              .into(placeImage)
              .execute();

        photoLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String placeID = getPlaceId();
                if (!TextUtils.isEmpty(placeID)) {
                    ImageManager.with(getActivity())
                          .putUserGeneratedPlaceImage(placeID)
                          .fromCameraOrGallery()
                          .withTransform(new CropCircleTransformation())
                          .useAsWallpaper(AlphaPreset.LIGHTEN)
                          .into(placeImage)
                          .execute();
                }
            }
        });

        SpinnerAdapter adapter = new SpinnerAdapter(
              //context
              getActivity(),
              //spinner closed state (view)
              R.layout.spinner_item_state_closed,
              //model
              getResources().getStringArray(R.array.states));


        state.setAdapter(adapter);
        continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                continueBtn.setEnabled(false);
                showProgressBar();

                ((BaseActivity)getActivity()).setPermissionCallback(new BaseActivity.PermissionCallback() {
                    @Override
                    public void permissionsUpdate(int permissionType, ArrayList<String> permissionsDenied, ArrayList<String> permissionsDeniedNeverAskAgain) {
                        if(permissionsDenied.contains(Manifest.permission.ACCESS_COARSE_LOCATION)) {
                            ((BaseActivity)getActivity()).showSnackBarForPermissions(getString(R.string.permission_location_denied_message));
                            address = new Address();
                        } else {
                            address = new Address((BaseActivity)getActivity());
                        }
                        try {
                            if(validate()){
                                showProgressBar();
                                submit();
                            }else{
                                hideProgressBar();
                                continueBtn.setEnabled(true);
                            }

                        }catch (Exception e){
                            continueBtn.setEnabled(true);
                            hideProgressBar();
                            ErrorManager.in(getActivity()).showGenericBecauseOf(e);
                        }
                    }
                });
                ArrayList<String> permissions = new ArrayList<String>();
                permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
                ((BaseActivity)getActivity()).checkPermission(permissions, GlobalSetting.PERMISSION_ACCESS_COARSE_LOCATION, R.string.permission_rationale_location);

            }
        });

        return view;
    }

    @NonNull
    public static AccountAboutYourHomeFragment newInstance() {
        return new AccountAboutYourHomeFragment();
    }

    @Override
    public boolean validate() {
        boolean formOk = true;

        TextView errorState;
        // TODO: Do we want to add a length requirement as well?
        if (StringUtils.isBlank(homeNickName.getText())) {
            homeNickName.setError(getActivity().getString(R.string.requiredField, homeNickName.getHint()));
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

        if(zipCode.getText().length() < 5) {
            zipCode.setError(getActivity().getString(R.string.requiredField, zipCode.getHint()));
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

    @Override
    public boolean submit() {
        final Address addressLocal = getAddressFromFormData();
        StreetAddress addressBean = new StreetAddress();
        addressBean.setLine1(addressLocal.getStreet());
        addressBean.setLine2(addressLocal.getStreet2());
        addressBean.setCity(addressLocal.getCity());
        addressBean.setState(addressLocal.getState());
        addressBean.setZip(addressLocal.getZipCode());

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

                            AddressValidationFragment addressFragment = AddressValidationFragment.newInstance(addressLocal, homeNickName.getText().toString(), "", "", suggestions, true);
                            addressFragment.setCallback(fragment);
                            BackstackManager.getInstance().navigateToFragment(addressFragment, true);
                        } else {
                            useEnteredAddress(addressLocal, null, false);
                        }
                    }
                }));
        return false;
    }

    @Override
    public String getTitle() {
        return getString(R.string.account_registration_home_information);
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_account_billing;
    }

    private boolean checkLocationPermission() {
        int res = getContext().checkCallingOrSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION);
        return (res == PackageManager.PERMISSION_GRANTED);
    }

    @NonNull
    private Address getAddressFromFormData() {
        if(address == null) {
            if(checkLocationPermission()) {
                address = new Address((BaseActivity)getActivity());
            } else {
                address = new Address();
            }
        }
        address.setStreet(street1.getText().toString());
        address.setStreet2(street2.getText().toString());
        address.setCity(city.getText().toString());
        address.setState(state.getSelectedItem().toString());
        address.setZipCode(zipCode.getText().toString());
        return address;
    }


    private String getPlaceName() {
        Editable pn = homeNickName.getText();
        return pn != null ? pn.toString() : "";
    }

    @Override public void onComplete(boolean result) {
        hideProgressBar();
        if(timeZone == null) {
            completed();
        } else {
            PlaceModel place = PlaceModelProvider.getCurrentPlace().get();
            if (place == null) {
                return;
            }

            place.setTzUsesDST(timeZone.isUsesDST());
            place.setTzId(timeZone.getId());
            place.setTzName(timeZone.getName());
            place.setTzOffset(timeZone.getOffset());
            place.commit()
                    .onSuccess(Listeners.runOnUiThread(successListener))
                    .onFailure(Listeners.runOnUiThread(errorListener));
        }
    }

    protected void completed() {
        hideProgressBar();
        super.onComplete(true);
    }

    @Override public void onPause() {
        super.onPause();
    }

    @Override public boolean onBackPressed() {
        if (showingTZModal) {
            BackstackManager.getInstance().navigateBack();
            showingTZModal = false;
            return true;
        }
        else {
            return super.onBackPressed();
        }
    }

    private String getPlaceId() {
        PlaceModel placeModel = PlaceModelProvider.getCurrentPlace().get();
        if (placeModel != null) {
            return placeModel.getId();
        }

        return null;
    }

    private Address streetAddressToAddress(StreetAddress input) {
        if(address == null) {
            address = new Address((BaseActivity)getActivity());
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
    public void showProgressBar() {
        //don't show the progress bar on this screen
    }

    @Override
    public void noSuggestionsPromon() {

    }

    @Override
    public void noSuggestionsNonPromon() {

    }

    @Override
    public void useEnteredAddress(Address address, TimeZoneModel timeZone, boolean navigateBack) {
        if(timeZone != null) {
            this.timeZone = timeZone;
        }

        if(navigateBack) {
            BackstackManager.getInstance().navigateBack();
        }
        hideProgressBar();
        new SaveHomeTask(getPlaceName(), address, this).sendToPlatform();
    }

    @Override
    public void useSuggestedAddress(Address address, TimeZoneModel timeZone) {
        hideProgressBar();
        userEnteredPlace = true;
        new SaveHomeTask(getPlaceName(), address, this).sendToPlatform();
    }
}

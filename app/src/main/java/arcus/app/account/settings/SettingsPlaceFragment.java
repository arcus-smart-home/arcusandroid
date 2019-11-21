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

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Spinner;

import arcus.cornea.CorneaClientFactory;
import arcus.cornea.model.PlaceAndRoleModel;
import arcus.cornea.subsystem.model.TimeZoneModel;
import arcus.cornea.utils.CachedModelSource;
import arcus.cornea.utils.Listeners;
import arcus.cornea.utils.TimeZoneLoader;
import com.iris.client.ClientEvent;
import com.iris.client.bean.StreetAddress;
import com.iris.client.event.Listener;
import com.iris.client.exception.ErrorResponseException;
import com.iris.client.model.PlaceModel;
import com.iris.client.service.PlaceService;
import arcus.app.R;
import arcus.app.account.registration.controller.task.ArcusTask;
import arcus.app.account.registration.controller.task.SaveHomeTask;
import arcus.app.activities.BaseActivity;
import arcus.app.common.adapters.SpinnerAdapter;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.error.ErrorManager;
import arcus.app.common.fragments.BaseFragment;
import arcus.app.common.image.ImageManager;
import arcus.app.common.image.Wallpaper;
import arcus.app.common.popups.InvalidAddressPopup;
import arcus.app.common.popups.TimezonePickerPopup;
import arcus.app.common.popups.UnservicedAddressPopup;
import arcus.app.common.utils.GlobalSetting;
import arcus.app.common.view.Version1EditText;
import arcus.app.common.view.Version1TextView;
import arcus.app.device.pairing.post.AddressValidationFragment;
import arcus.app.integrations.Address;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;


public class SettingsPlaceFragment extends BaseFragment
      implements TimezonePickerPopup.Callback, TimeZoneLoader.Callback, AddressValidationFragment.AddressValidationCallback, ArcusTask.ArcusTaskListener, BaseActivity.PermissionCallback {
    Version1EditText placeName, zipCode, street1, street2, city;
    Spinner state;
    Version1TextView timeZoneSelected;
    View timeZoneLayout, timeZoneAndRemoveLayout;
    String edit, done;
    int selectedStateIndex = -1, spinnerColor;
    PlaceAndRoleModel originalPlaceDetails;
    AtomicBoolean isSaving = new AtomicBoolean(false);
    boolean isEditing = false;
    TimeZoneModel timeZoneValidation;
    private SettingsPlaceFragment fragment;
    private Address address;

    transient PlaceModel placeModel;
    final Listener<Throwable> errorListener = Listeners.runOnUiThread(new Listener<Throwable>() {
        @Override public void onEvent(Throwable throwable) {
            isSaving.set(false);
            hideProgressBarAndEnable(timeZoneLayout);
            ErrorManager.in(getActivity()).showGenericBecauseOf(throwable);
        }
    });

    @NonNull public static SettingsPlaceFragment newInstance(PlaceAndRoleModel placeAndRoleModel) {
        SettingsPlaceFragment fragment = new SettingsPlaceFragment();
        Bundle bundle = new Bundle(1);
        bundle.putParcelable("PLACE", placeAndRoleModel);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        originalPlaceDetails = getArguments().getParcelable("PLACE");
    }

    @Override public View onCreateView(
          @NonNull LayoutInflater inflater,
          ViewGroup container,
          Bundle savedInstanceState
    ) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        edit = getString(R.string.card_menu_edit);
        done = getString(R.string.card_menu_done);
        spinnerColor = getResources().getColor(R.color.overlay_white_with_50);

        placeName = (Version1EditText) view.findViewById(R.id.place_name);
        street1 = (Version1EditText) view.findViewById(R.id.fragment_account_billing_street1);
        street2 = (Version1EditText) view.findViewById(R.id.fragment_account_billing_street2);
        city = (Version1EditText) view.findViewById(R.id.fragment_account_billing_city);
        state = (Spinner) view.findViewById(R.id.fragment_account_billing_state);
        zipCode = (Version1EditText) view.findViewById(R.id.fragment_account_billing_zipcode);
        timeZoneSelected = (Version1TextView) view.findViewById(R.id.timezone_display);
        timeZoneAndRemoveLayout = view.findViewById(R.id.time_zone_layout_container);

        timeZoneLayout = view.findViewById(R.id.timezone_layout);
        timeZoneLayout.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (placeModel == null) {
                    return;
                }

                showSaving();
                TimeZoneLoader.instance().setCallback(SettingsPlaceFragment.this);
                TimeZoneLoader.instance().loadTimezones();
            }
        });

        if (originalPlaceDetails != null) {
            showProgressBar();
            CachedModelSource.<PlaceModel>get(originalPlaceDetails.getAddress()).load()
                    .onFailure(errorListener)
                    .onSuccess(Listeners.runOnUiThread(new Listener<PlaceModel>() {
                        @Override public void onEvent(PlaceModel placeModel) {
                            hideProgressBar();
                            onPlaceLoaded(placeModel);
                        }
                    }));
        }

        setHasOptionsMenu(true);
        return view;
    }

    @Override public void onResume() {
        super.onResume();

        fragment = this;
    }

    protected void onPlaceLoaded(PlaceModel model) {
        placeModel = model;
        placeName.setText(placeModel.getName());
        street1.setText(placeModel.getStreetAddress1());
        street2.setText(placeModel.getStreetAddress2());
        city.setText(placeModel.getCity());
        zipCode.setText(placeModel.getZipCode());
        timeZoneSelected.setText(placeModel.getTzName());

        setUpStateSpinner(placeModel);
        enableInput(false);

        ImageManager.with(getActivity()).setWallpaper(Wallpaper.ofPlace(placeModel.getId()).darkened());

        setTitle();
    }

    @NonNull @Override public String getTitle() {
        return getResources().getString(R.string.settings_place_info);
    }

    @Override public Integer getLayoutId() {
        return R.layout.fragment_place_settings;
    }

    @Override public void onPause() {
        super.onPause();

        doneSaving();
        TimeZoneLoader.instance().removeCallbacks();
    }

    @Override public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        Activity activity = getActivity();
        if (activity == null) {
            return;
        }

        activity.getMenuInflater().inflate(R.menu.menu_edit, menu);

        MenuItem item = menu.findItem(R.id.menu_edit_contact);
        if (item != null) {
            item.setTitle(isEditing ? done : edit);
        }
    }

    @Override public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // If we're currently saving data - don't do anything.
        if (isSaving.get()) {
            return true;
        }

        // Don't do anything if we're showing the tz fragment.
        Fragment tzFragment = BackstackManager.getInstance().getFragmentOnStack(TimezonePickerPopup.class);
        if (tzFragment != null && tzFragment instanceof TimezonePickerPopup && tzFragment.isVisible()) {
            return true;
        }

        // If for some reason we clicked on something that shouldn't be there....
        if (item.getItemId() != R.id.menu_edit_contact) {
            return super.onOptionsItemSelected(item);
        }

        // Else update the editing/not editing mode.
        String menuTitle = String.valueOf(item.getTitle());
        boolean editing = edit.equalsIgnoreCase(menuTitle);
        item.setTitle(editing ? done : edit);
        enableInput(editing);

        return true;
    }

    protected void enableInput(boolean enable) {
        timeZoneAndRemoveLayout.setVisibility(enable ? View.GONE : View.VISIBLE);

        isEditing = enable;
        placeName.setEnabled(enable);
        street1.setEnabled(enable);
        street2.setEnabled(enable);
        city.setEnabled(enable);
        zipCode.setEnabled(enable);
        state.setEnabled(enable);

        if (!enable) {
            InputMethodManager imm = (InputMethodManager) placeName.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(placeName.getWindowToken(), 0);
        }

        if (!enable && hasEdited() && state.getSelectedItemPosition() != 0 && fieldsValidated()) {
            ((BaseActivity) getActivity()).setPermissionCallback(this);
            ArrayList<String> permissions = new ArrayList<String>();
            permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
            ((BaseActivity) getActivity()).checkPermission(permissions, GlobalSetting.PERMISSION_ACCESS_COARSE_LOCATION, R.string.permission_rationale_location);
        }
    }

    private boolean fieldsValidated() {
        boolean valid = true;
        if(TextUtils.isEmpty(placeName.getText())) {
            placeName.setError(getActivity().getString(R.string.requiredField, placeName.getHint()));
            valid = false;
        }
        if(TextUtils.isEmpty(street1.getText())) {
            street1.setError(getActivity().getString(R.string.requiredField, street1.getHint()));
            valid = false;
        }

        if(TextUtils.isEmpty(city.getText())) {
            city.setError(getActivity().getString(R.string.requiredField, city.getHint()));
            valid = false;
        }

        if(zipCode.getText().length() < 5) {
            zipCode.setError(getActivity().getString(R.string.requiredField, zipCode.getHint()));
            valid = false;
        }
        return valid;
    }

    protected void setUpStateSpinner(PlaceModel place) {
        String[] states = getResources().getStringArray(R.array.states);
        List<String> stateList = Arrays.asList(states);

        state.setAdapter(new SpinnerAdapter(getActivity(), R.layout.spinner_item_state_closed, states, true));
        state.getBackground().setColorFilter(spinnerColor, PorterDuff.Mode.SRC_ATOP);

        selectedStateIndex = stateList.indexOf(place.getState());
        if (selectedStateIndex != -1) {
            state.setSelection(selectedStateIndex, false);
        }
    }

    protected boolean hasEdited() {
        if (placeModel == null) {
            return false;
        }

        Editable placeNameText = placeName.getText();
        if (placeNameText != null && !placeNameText.toString().equalsIgnoreCase(placeModel.getName())) {
            return true;
        }

        Editable address1Text = street1.getText();
        if (address1Text != null && !address1Text.toString().equalsIgnoreCase(placeModel.getStreetAddress1())) {
            return true;
        }

        Editable address2Text = street2.getText();
        if (address2Text != null && !address2Text.toString().equalsIgnoreCase(placeModel.getStreetAddress2())) {
            return true;
        }

        Editable cityText = city.getText();
        if (cityText != null && !cityText.toString().equalsIgnoreCase(placeModel.getCity())) {
            return true;
        }

        int statePickerSelection = state.getSelectedItemPosition();
        // Came in w/o a selection, and have now chosen something.
        if (selectedStateIndex == -1 && statePickerSelection != 0) {
            return true;
        }

        // Came in w/a selection, and have now chosen something new.
        if (selectedStateIndex != -1 && statePickerSelection != selectedStateIndex) {
            return true;
        }

        Editable zipCodeText = zipCode.getText();
        return zipCodeText != null && !zipCodeText.toString().equalsIgnoreCase(placeModel.getZipCode());
    }

    protected void showSaving() {
        isSaving.set(true);
        showProgressBarAndDisable(timeZoneLayout);
    }

    protected void doneSaving() {
        isSaving.set(false);
        hideProgressBarAndEnable(timeZoneLayout);
    }

    // For Timezone popup
    @Override public void loaded(List<TimeZoneModel> timeZones) {
        doneSaving();
        TimezonePickerPopup popup = TimezonePickerPopup.newInstance(placeModel.getTzId(), timeZones);
        popup.setCallback(this);
        BackstackManager.getInstance().navigateToFloatingFragment(popup, popup.getClass().getSimpleName(), true);
    }

    @Override public void failed(Throwable throwable) {
        doneSaving();
        ErrorManager.in(getActivity()).showGenericBecauseOf(throwable);
    }

    @Override public void timeZoneSelected(final TimeZoneModel zone) {
        if (placeModel == null) {
            return;
        }

        showSaving();
        placeModel.setTzUsesDST(zone.isUsesDST());
        placeModel.setTzId(zone.getId());
        placeModel.setTzName(zone.getName());
        placeModel.setTzOffset(zone.getOffset());
        placeModel.commit()
              .onFailure(errorListener)
              .onSuccess(Listeners.runOnUiThread(new Listener<ClientEvent>() {
                  @Override public void onEvent(ClientEvent clientEvent) {
                      doneSaving();
                      if (placeModel != null && timeZoneSelected != null) {
                          timeZoneSelected.setText(placeModel.getTzName());
                      }
                  }
              }));
    }

    // For Save Home Task
    @Override public void onComplete(boolean result) {
        doneSaving();

        CachedModelSource.<PlaceModel>get(originalPlaceDetails.getAddress()).reload().onFailure(errorListener)
                .onSuccess(Listeners.runOnUiThread(new Listener<PlaceModel>() {
                    @Override public void onEvent(PlaceModel placeModel) {
                        hideProgressBar();
                        onPlaceLoaded(placeModel);
                        if(timeZoneValidation != null) {
                            timeZoneSelected(timeZoneValidation);
                        }
                    }
                }));

        if (placeModel != null && timeZoneSelected != null) {
            timeZoneSelected.setText(placeModel.getTzName());
        }
    }

    @Override public void onError(Exception e) {
        doneSaving();

        if (e instanceof ErrorResponseException) {
            ErrorResponseException ere = (ErrorResponseException) e;

            if (ere.getCode().contains("promonitoring.address.unavailable")) {
                BackstackManager.getInstance().navigateToFloatingFragment(InvalidAddressPopup.newInstance(getString(R.string.not_available),
                        getString(R.string.address_verification_not_available_description)), InvalidAddressPopup.class.getSimpleName(), true);
            } else if (ere.getCode().contains("promonitoring.address.unserviceable")) {
                BackstackManager.getInstance().navigateToFloatingFragment(UnservicedAddressPopup.newInstance(), UnservicedAddressPopup.class.getSimpleName(), true);
            } else {
                ErrorManager.in(getActivity()).showGenericBecauseOf(e);
            }
        }

        else {
            ErrorManager.in(getActivity()).showGenericBecauseOf(e);
        }
    }

    @Override
    public void noSuggestionsPromon() {
        BackstackManager.getInstance().navigateBack();
        BackstackManager.getInstance().navigateToFloatingFragment(InvalidAddressPopup.newInstance(getString(R.string.address_verification_invalid_address),
                getString(R.string.address_verification_invalid_address_description)), InvalidAddressPopup.class.getSimpleName(), true);
    }

    @Override
    public void noSuggestionsNonPromon() {
        BackstackManager.getInstance().navigateBack();
    }

    @Override
    public void useEnteredAddress(Address address, TimeZoneModel timeZone, boolean navigateBack) {
        if(timeZone != null) {
            address.setDst(timeZone.isUsesDST());
            address.setTimeZoneName(timeZone.getName());
            address.setTimeZoneId(timeZone.getId());
            address.setUtcOffset(timeZone.getOffset());
        }
        if(navigateBack) {
            BackstackManager.getInstance().navigateBack();
        }
        timeZoneValidation = timeZone;

        new SaveHomeTask(originalPlaceDetails.getPlaceId(), placeName.getText().toString(), address, this).sendToPlatform();
    }

    @Override
    public void useSuggestedAddress(Address address, TimeZoneModel timeZone) {
        BackstackManager.getInstance().navigateBack();
        timeZoneValidation = timeZone;
        new SaveHomeTask(originalPlaceDetails.getPlaceId(), placeName.getText().toString(), address, this).sendToPlatform();
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
    public void permissionsUpdate(int permissionType, ArrayList<String> permissionsDenied, ArrayList<String> permissionsDeniedNeverAskAgain) {
        //TODO - Permissions: should this be a no-op?
        if(permissionsDenied.contains(Manifest.permission.ACCESS_COARSE_LOCATION)) {
            ((BaseActivity)getActivity()).showSnackBarForPermissions(getString(R.string.permission_location_denied_message));
            address = new Address();
        } else {
            address = new Address((BaseActivity)getActivity());
        }

        final Address addressLocal = this.address;
        addressLocal.setStreet(street1.getText().toString());
        addressLocal.setStreet2(street2.getText().toString());
        addressLocal.setCity(city.getText().toString());
        addressLocal.setState(state.getSelectedItem().toString());
        addressLocal.setZipCode(zipCode.getText().toString());

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

                            AddressValidationFragment addressFragment = AddressValidationFragment.newInstance(addressLocal, placeName.getText().toString(), placeModel.getServiceLevel(), placeModel.getTzId(), suggestions, false);
                            addressFragment.setCallback(fragment);
                            BackstackManager.getInstance().navigateToFragment(addressFragment, true);
                        } else {
                            useEnteredAddress(addressLocal, null, false);
                        }
                    }
                }));

    }
}

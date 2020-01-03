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
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import arcus.app.account.settings.remove.SettingsRemoveFragment;
import arcus.cornea.SessionController;
import arcus.cornea.model.PlaceAndRoleModel;
import arcus.cornea.utils.CachedModelSource;
import arcus.cornea.utils.Listeners;
import com.iris.capability.util.Addresses;
import com.iris.client.bean.PlaceAccessDescriptor;
import com.iris.client.capability.Person;
import com.iris.client.event.Listener;
import com.iris.client.model.PlaceModel;
import arcus.app.ArcusApplication;
import arcus.app.R;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.fragments.BaseFragment;
import arcus.app.common.image.ImageManager;
import arcus.app.common.image.Wallpaper;
import arcus.app.common.image.picasso.transformation.AlphaPreset;
import arcus.app.common.image.picasso.transformation.CropCircleTransformation;
import arcus.app.common.popups.InfoTextPopup;
import arcus.app.common.view.CircularImageView;

import org.apache.commons.lang3.StringUtils;

public class SettingsPlaceOverviewFragment extends BaseFragment {
    public static final String PLACE = "PLACE";
    public static final String TOTAL_PLACES = "TOTAL_PLACES";
    PlaceAndRoleModel placeAndRoleModel;
    String viewingPlaceID;
    CircularImageView placeImage;
    Button removeButton;
    String title;
    View cameraIcon, editPlaceInfoLayout, editProMonInfoLayout;
    TextView placeName, placeLocation, placeStreet;
    boolean isPlaceOwner;
    int totalPlaces;

    public static SettingsPlaceOverviewFragment newInstance(PlaceAndRoleModel placeAndRoleModel, int totalPlaces) {
        SettingsPlaceOverviewFragment fragment = new SettingsPlaceOverviewFragment();
        Bundle args = new Bundle(2);
        args.putParcelable(PLACE, placeAndRoleModel);
        args.putInt(TOTAL_PLACES, totalPlaces);
        fragment.setArguments(args);
        return fragment;
    }

    @Override public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        placeAndRoleModel = getArguments().getParcelable(PLACE);
        totalPlaces = getArguments().getInt(TOTAL_PLACES);
        if (placeAndRoleModel == null) {
            return;
        }

        viewingPlaceID = placeAndRoleModel.getPlaceId();
        if (!TextUtils.isEmpty(viewingPlaceID)) {
            viewingPlaceID = Addresses.getId(viewingPlaceID);
        }
    }

    @Override public void onResume() {
        super.onResume();
        View rootView = getView();
        if (rootView == null || TextUtils.isEmpty(viewingPlaceID)) {
            return;
        }

        isPlaceOwner = placeAndRoleModel != null && PlaceAccessDescriptor.ROLE_OWNER.equals(placeAndRoleModel.getRole());
        placeImage = (CircularImageView) rootView.findViewById(R.id.fragment_account_camera);
        cameraIcon = rootView.findViewById(R.id.camera_image);
        editPlaceInfoLayout = rootView.findViewById(R.id.edit_address_layout);
        editProMonInfoLayout = rootView.findViewById(R.id.edit_promon_layout);
        placeName = (TextView) rootView.findViewById(R.id.place_name);
        placeLocation = (TextView) rootView.findViewById(R.id.place_location);
        placeStreet = (TextView) rootView.findViewById(R.id.place_street);
        removeButton = (Button) rootView.findViewById(R.id.remove_button);

        setTitle();
        loadPlaceImageAndBackground();
        loadEditPlaceInformation();
    }

    protected void loadPlaceImageAndBackground() {
        if (placeAndRoleModel != null) {
            CachedModelSource.<PlaceModel>get(placeAndRoleModel.getAddress())
                  .load()
                  .onSuccess(Listeners.runOnUiThread(new Listener<PlaceModel>() {
                      @Override public void onEvent(PlaceModel placeModel) {
                          if (placeModel != null) {
                              onPlaceModelLoaded(placeModel);
                          }
                      }
                  }));
        }

        ImageManager.with(getActivity())
              .putPlaceImage(viewingPlaceID)
              .withTransform(new CropCircleTransformation())
              .into(placeImage)
              .execute();

        cameraIcon.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                ImageManager.with(getActivity())
                      .putUserGeneratedPlaceImage(viewingPlaceID)
                      .fromCameraOrGallery()
                      .withTransform(new CropCircleTransformation())
                      .useAsWallpaper(AlphaPreset.DARKEN)
                      .into(placeImage)
                      .execute();
            }
        });

        ImageManager.with(ArcusApplication.getContext()).setWallpaper(Wallpaper.ofPlace(viewingPlaceID).darkened());
    }

    protected void onPlaceModelLoaded(@NonNull PlaceModel placeModel) {
        placeName.setText(placeModel.getName());
        placeStreet.setText(String.format("%s %s", placeModel.getStreetAddress1(), StringUtils.stripToEmpty(placeModel.getStreetAddress2())));

        String city = TextUtils.isEmpty(placeModel.getCity()) ? "" : placeModel.getCity();
        String state = TextUtils.isEmpty(placeModel.getState()) ? "" : ", " + placeModel.getState();
        String zip = TextUtils.isEmpty(placeModel.getZipCode()) ? "" : " " + placeModel.getZipCode();
        placeLocation.setText(String.format("%s%s%s", city, state, zip));

        title = placeModel.getName() == null ? "" : placeModel.getName();
        setTitle();
    }

    protected void loadEditPlaceInformation() {
        if (isPlaceOwner) {
            editPlaceInfoLayout.setVisibility(View.VISIBLE);
            editPlaceInfoLayout.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    BackstackManager.getInstance().navigateToFragment(SettingsPlaceFragment.newInstance(placeAndRoleModel), true);
                }
            });
            removeButton.setText(getString(R.string.settings_place_remove_place));
            removeButton.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    navigateToRemoveForPlace(true);
                }
            });
        }
        else {
            editPlaceInfoLayout.setOnClickListener(null);
            editPlaceInfoLayout.setVisibility(View.GONE);
            removeButton.setText(getString(R.string.remove_access));
            removeButton.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    navigateToRemoveForPlace(false);
                }
            });
        }
    }

    protected void navigateToRemoveForPlace(boolean isPlace) {
        if (placeAndRoleModel == null) {
            return;
        }

        if (placeAndRoleModel.isPrimary()) {
            InfoTextPopup popup = InfoTextPopup.newInstance(
                  getString(R.string.primary_cannot_remove_desc, placeAndRoleModel.getName()),
                  R.string.primary_cannot_remove_title
            );
            BackstackManager.getInstance().navigateToFloatingFragment(popup, popup.getClass().getSimpleName(), true);
        }
        else if (totalPlaces == 1) {
            InfoTextPopup popup = InfoTextPopup.newInstance(
                  R.string.one_place_cannot_remove_desc,
                  R.string.one_place_cannot_remove_title
            );
            BackstackManager.getInstance().navigateToFloatingFragment(popup, popup.getClass().getSimpleName(), true);
        }
        else if (SessionController.instance().getPlaceIdOrEmpty().equals(placeAndRoleModel.getPlaceId())) {
            InfoTextPopup popup = InfoTextPopup.newInstance(
                  R.string.logged_in_cannot_remove_desc,
                  R.string.logged_in_cannot_remove_title
            );
            BackstackManager.getInstance().navigateToFloatingFragment(popup, popup.getClass().getSimpleName(), true);
        }
        else {
            if (isPlace) {
                BackstackManager.getInstance().navigateToFragment(SettingsRemoveFragment.removePlace(placeAndRoleModel.getAddress()), true);
            }
            else {
                String personAddress = Addresses.toObjectAddress(Person.NAMESPACE, SessionController.instance().getPersonId());
                BackstackManager.getInstance().navigateToFragment(SettingsRemoveFragment.removeAccess(placeAndRoleModel.getAddress(), personAddress), true);
            }
        }
    }

    @Nullable @Override public String getTitle() {
        return title;
    }

    @Override public Integer getLayoutId() {
        return R.layout.settings_people_and_place;
    }
}

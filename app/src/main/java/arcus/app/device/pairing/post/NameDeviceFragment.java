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
package arcus.app.device.pairing.post;

import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import arcus.app.common.fragments.BaseFragment;
import arcus.app.device.settings.fragment.HaloRoomFragment;
import arcus.cornea.CorneaClientFactory;
import arcus.cornea.SessionController;
import arcus.cornea.device.smokeandco.HaloController;
import arcus.cornea.dto.HubDeviceModelDTO;
import arcus.cornea.provider.DeviceModelProvider;
import com.iris.client.capability.Halo;
import com.iris.client.capability.Presence;
import com.iris.client.model.DeviceModel;
import com.iris.client.model.HubModel;
import com.iris.client.model.PersonModel;
import arcus.app.R;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.error.ErrorManager;
import arcus.app.common.image.ImageCategory;
import arcus.app.common.image.ImageManager;
import arcus.app.common.image.ImageRepository;
import arcus.app.common.image.ImageSuccessCallback;
import arcus.app.common.image.Wallpaper;
import arcus.app.common.image.picasso.transformation.AlphaPreset;
import arcus.app.common.image.picasso.transformation.BlackWhiteInvertTransformation;
import arcus.app.common.image.picasso.transformation.CropCircleTransformation;
import arcus.app.common.image.picasso.transformation.Invert;
import arcus.app.common.models.ListItemModel;
import arcus.app.common.popups.ArcusFloatingFragment;
import arcus.app.common.popups.MultiModelPopup;
import arcus.app.common.utils.CorneaUtils;
import arcus.app.common.utils.StringUtils;
import arcus.app.common.validation.NotEmptyValidator;
import arcus.app.common.view.Version1Button;
import arcus.app.common.view.Version1ButtonColor;
import arcus.app.common.view.Version1EditText;
import arcus.app.common.view.Version1TextView;
import arcus.app.device.model.DeviceType;
import arcus.app.device.pairing.post.controller.NameDeviceFragmentController;

import java.util.ArrayList;
import java.util.List;


public class NameDeviceFragment extends BaseFragment implements NameDeviceFragmentController.Callbacks, HaloController.Callback {

    private static final String DEVICE_NAME = "DEVICE_NAME";
    private static final String DEVICE_ADDRESS = "DEVICE_ADDRESS";

    private Version1Button nextButton;
    private ImageView deviceImage;
    private FrameLayout deviceImageClickableRegion;
    private ImageView cameraButton;
    private Version1EditText deviceName;
    private TextView successLabel;
    private TextView successDescLabel;
    private LinearLayout presenceAssignmentRegion;
    private ImageView assignedUserImage;
    private Version1TextView assignedUserName;
    private Version1TextView assignmentStatus;
    private ImageView assignmentChevron;
    private ImageView cloudImage;
    private View deviceAttributeLayout;
    private Version1TextView deviceAttributeValue;
    private HaloController haloController;

    private boolean takingPicture;
    private final boolean isEditMode = true;

    @Override
    public void onError(Throwable throwable) {
        deviceAttributeLayout.setVisibility(View.GONE);
    }

    @Override
    public void onSuccess(final DeviceModel haloDeviceModel) {
        deviceAttributeLayout.setVisibility(View.VISIBLE);
        deviceAttributeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BackstackManager.getInstance().navigateToFragment(HaloRoomFragment.newInstance(haloDeviceModel.getAddress(), true), true);
            }
        });

        String selection = haloController.getSelectedRoomType();
        if(selection != null) {
            deviceAttributeValue.setText(selection.toUpperCase());
        }
        else {
            deviceAttributeValue.setText(getString(R.string.homenfamily_unassigned).toUpperCase());
        }
    }

    public enum ScreenVariant {
        SETTINGS,
        DEVICE_PAIRING
    }

    public static NameDeviceFragment newInstance (ScreenVariant screenVariant, String deviceName, String deviceAddress) {
        NameDeviceFragment instance = new NameDeviceFragment();

        Bundle arguments = new Bundle();
        arguments.putString(DEVICE_NAME, deviceName);
        arguments.putString(DEVICE_ADDRESS, deviceAddress);
        instance.setArguments(arguments);

        return instance;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        cloudImage = (ImageView) view.findViewById(R.id.cloud_image);
        nextButton = (Version1Button) view.findViewById(R.id.nameDeviceNextBtn);
        deviceImage = (ImageView) view.findViewById(R.id.fragment_account_camera);
        deviceImageClickableRegion = (FrameLayout) view.findViewById(R.id.photo_layout);
        cameraButton = (ImageView) view.findViewById(R.id.camera_image);
        deviceName = (Version1EditText) view.findViewById(R.id.etNameDevice);
        successLabel = (TextView) view.findViewById(R.id.tvSuccess);
        successDescLabel = (TextView) view.findViewById(R.id.tvNameDevice1);
        presenceAssignmentRegion = (LinearLayout) view.findViewById(R.id.assign_layout);
        assignedUserImage = (ImageView) view.findViewById(R.id.imgPic);
        assignedUserName = (Version1TextView) view.findViewById(R.id.tvAssignedName);
        assignmentStatus = (Version1TextView) view.findViewById(R.id.assignment_status);
        assignmentChevron = (ImageView) view.findViewById(R.id.imgChevron);
        deviceAttributeLayout = view.findViewById(R.id.device_attribute_layout);
        deviceAttributeValue = (Version1TextView) view.findViewById(R.id.device_attribute_value);

        return view;
    }

    public void onResume () {
        super.onResume();
        getActivity().setTitle(getTitle());
        getActivity().invalidateOptionsMenu();

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (new NotEmptyValidator(getActivity(), deviceName, R.string.device_more_name_blank_error_msg).isValid()) {
                    getActivity().setTitle(deviceName.getText().toString());
                    NameDeviceFragmentController.getInstance().setName(deviceName.getText().toString());
                }
            }
        });

        // Handle clicks on the camera icon (allowing user to specify user-generated content)
        deviceImageClickableRegion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String deviceAddress = getArguments().getString(DEVICE_ADDRESS);

                takingPicture = true;
                AlphaPreset wallpaper = isEditMode ? AlphaPreset.DARKEN : AlphaPreset.LIGHTEN;
                ImageManager.with(getActivity())
                        .putUserGeneratedDeviceImage(SessionController.instance().getPlaceIdOrEmpty(), deviceAddress)
                        .fromCameraOrGallery()
                        .withTransform(new CropCircleTransformation())
                        .useAsWallpaper(wallpaper)
                        .withSuccessCallback(new ImageSuccessCallback() {
                            @Override public void onImagePlacementSuccess() {
                                takingPicture = false;
                            }
                        })
                        .into(deviceImage)
                        .execute();
            }
        });

        if (isEditMode) {
            successLabel.setVisibility(View.INVISIBLE);
            successDescLabel.setVisibility(View.GONE);
            nextButton.setColorScheme(Version1ButtonColor.WHITE);
            nextButton.setText(R.string.account_setting_save_btn);
            deviceName.useLightColorScheme(true);
            cameraButton.setImageResource(R.drawable.button_camera_white);
        }

        showProgressBar();
        NameDeviceFragmentController.getInstance().setListener(this);
        NameDeviceFragmentController.getInstance().loadModel(getActivity(), getArguments().getString(DEVICE_ADDRESS));
    }

    @Override
    public void onPause () {
        super.onPause();
        NameDeviceFragmentController.getInstance().removeListener();
    }

    @Nullable
    @Override
    public String getTitle() {
        return getArguments().getString(DEVICE_NAME);
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_name_device_add_pic;
    }

    @Override
    public void onDeviceLoaded(final DeviceModel deviceModel, boolean supportsPresence, PersonModel currentAssignment, final List<PersonModel> people) {
        hideProgressBar();

        // Put the current device image into the photo circle
        if (deviceModel != null && cloudImage != null) {
            if (DeviceType.fromHint(deviceModel.getDevtypehint()).isCloudConnected()) {
                cloudImage.setVisibility(View.VISIBLE);
            }
        }

        if(isEditMode && deviceModel instanceof Halo) {
            haloController = new HaloController(
                    DeviceModelProvider.instance().getModel(deviceModel.getAddress() == null ? "DRIV:dev:" : deviceModel.getAddress()),
                    CorneaClientFactory.getClient(),
                    null
            );
            haloController.setCallback(this);
        } else {
            deviceAttributeLayout.setVisibility(View.GONE);
        }


        // If the user is not selecting/taking a picture or the device image is not set, go ahead and set it.
        if (!takingPicture || (deviceImage != null && deviceImage.getDrawable() == null)) {
            // Prevents race condition that occurs when the image is being saved and the below is also called

            // Previously the saving process was taking slightly longer (due to a larger inSampleSize) being calculated
            // and the call to set the image (and bg) in the onClick listener (above) was enqueued,
            // and ran, after this call was made to set the image.
            if (!configuredThermostatImage(deviceModel)) {
                ImageManager.with(getActivity())
                      .putLargeDeviceImage(deviceModel)
                      .withTransformForStockImages(new BlackWhiteInvertTransformation(isEditMode ? Invert.BLACK_TO_WHITE : Invert.WHITE_TO_BLACK))
                      .withTransform(new CropCircleTransformation())
                      .into(deviceImage)
                      .execute();
            }

            if (isEditMode) {
                ImageManager.with(getActivity()).setWallpaper(Wallpaper.ofDevice(deviceModel).darkened());
            }
            else {
                ImageManager.with(getActivity()).setWallpaper(Wallpaper.ofDevice(deviceModel).lightend());
            }
        }

        if (StringUtils.isEmpty(deviceName.getText())) {
            deviceName.setText(deviceModel.getName());
        }

        logger.debug("Device supports presence: {}; current assignment: {}; list of people available for assignment: {}", supportsPresence, currentAssignment, people);
        if (supportsPresence) {
            presenceAssignmentRegion.setVisibility(View.VISIBLE);
            String currentAssignmentName = currentAssignment == null ? null : currentAssignment.getFirstName() + " " + currentAssignment.getLastName();
            String currentAssignmentAddress = currentAssignment == null ? null : currentAssignment.getAddress();
            setPresenceAssignment(deviceModel, currentAssignmentAddress, currentAssignmentName);
        } else {
            presenceAssignmentRegion.setVisibility(View.GONE);
        }

        presenceAssignmentRegion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BackstackManager.getInstance().navigateToFloatingFragment(buildPresenceSelectionPopup(deviceModel, people), MultiModelPopup.class.getSimpleName(), true);
            }
        });
    }

    // Update to check if the device is a thermostat so we don't call the platform for the blank circle and we
    // get the thermostat icon.  Wary about putting this into image manager since this is a very specific use-case
    // and, at least for now, only impacts this screen.
    protected boolean configuredThermostatImage(@Nullable DeviceModel deviceModel) {
        if (deviceModel != null) {
            // If there is a custom image, let the image manager above handle.
            String placeID = SessionController.instance().getPlaceIdOrEmpty();
            String deviceID = deviceModel.getId();
            ImageCategory category = ImageCategory.DEVICE_LARGE;

            if (ImageRepository.imageExists(getContext(), category, placeID, deviceID)) {
                return false; // Not handled here.
            }
        }

        if (!CorneaUtils.isThermostatDevice(deviceModel)) {
            return false; // Not handled here.
        }

        ImageManager
              .with(getContext())
              .putDrawableResource(R.drawable.thermostat_dtype)
              .withTransform(new BlackWhiteInvertTransformation(isEditMode ? Invert.BLACK_TO_WHITE : Invert.WHITE_TO_BLACK))
              .into(deviceImage)
              .execute();
        return true; // Handled here.
    }

    @Override
    public void onHubLoaded(HubModel hubModel) {
        hideProgressBar();

        if (!takingPicture || (deviceImage != null && deviceImage.getDrawable() == null)) {
            // Put the current device image into the photo circle
            ImageManager.with(getActivity())
                  .putLargeDeviceImage((DeviceModel) new HubDeviceModelDTO(hubModel))
                  .withTransformForStockImages(new BlackWhiteInvertTransformation(isEditMode ? Invert.BLACK_TO_WHITE : Invert.WHITE_TO_BLACK))
                  .withTransform(new CropCircleTransformation())
                  .into(deviceImage)
                  .execute();

            // Place the device image background
            if (isEditMode) {
                ImageManager.with(getActivity())
                      .putDeviceBackgroundImage(hubModel)
                      .intoWallpaper(AlphaPreset.DARKEN)
                      .execute();
            }
            else {
                ImageManager.with(getActivity())
                      .putDeviceBackgroundImage(hubModel)
                      .intoWallpaper(AlphaPreset.LIGHTEN)
                      .execute();
            }
        }

        deviceName.setText(hubModel.getName());
    }

    @Override
    public void onSuccess() {
        BackstackManager.getInstance().navigateBack();
    }

    @Override
    public void onCorneaError(Throwable cause) {
        hideProgressBar();
        ErrorManager.in(getActivity()).showGenericBecauseOf(cause);
    }

    private ArcusFloatingFragment buildPresenceSelectionPopup(final DeviceModel deviceModel, List<PersonModel> people) {
        List<String> availableSelections = new ArrayList<>();
        List<String> currentSelection = new ArrayList<>();

        availableSelections.add(deviceModel.getAddress());
        for (PersonModel thisPerson : people) {
            availableSelections.add(thisPerson.getAddress());
        }

        MultiModelPopup popup = MultiModelPopup.newInstance(availableSelections, R.string.homenfamily_assign_to, currentSelection, false);
        popup.setCallback(new MultiModelPopup.Callback() {
            @Override
            public void itemSelectedAddress(ListItemModel itemModel) {
                if (itemModel.isDeviceModel()) {
                    NameDeviceFragmentController.getInstance().unassignDevice(deviceModel);
                    setPresenceAssignment(deviceModel, null, null);
                } else {
                    NameDeviceFragmentController.getInstance().assignPersonToDevice(deviceModel, itemModel.getAddress());
                    setPresenceAssignment(deviceModel, itemModel.getAddress(), itemModel.getText());
                }
            }
        });

        return popup;
    }

    private void setPresenceAssignment(DeviceModel model, String personAddress, String personName) {
        Presence presence = CorneaUtils.getCapability(model, Presence.class);

        if (presence == null || StringUtils.isEmpty(personAddress)) {
            assignedUserName.setText("");
            assignmentStatus.setText(getString(R.string.homenfamily_unassigned));
            ImageManager.with(getActivity())
                    .putSmallDeviceImage(model)
                    .noUserGeneratedImagery()
                    .withTransform(new BlackWhiteInvertTransformation(isEditMode ? Invert.BLACK_TO_WHITE : Invert.WHITE_TO_BLACK))
                    .withTransform(new CropCircleTransformation())
                    .into(assignedUserImage)
                    .execute();
        } else {
            assignedUserName.setText(personName);
            assignmentStatus.setText(getString(R.string.homenfamily_assigned));
            ImageManager.with(getActivity())
                    .putPersonImage(presence.getPerson())
                    .withTransform(new CropCircleTransformation())
                    .withTransformForStockImages(new BlackWhiteInvertTransformation(isEditMode ? Invert.BLACK_TO_WHITE : Invert.WHITE_TO_BLACK))
                    .into(assignedUserImage)
                    .execute();
        }

        assignmentChevron.setImageResource(isEditMode ? R.drawable.chevron_white : R.drawable.chevron);
        assignedUserName.setTextColor(isEditMode ? getResources().getColor(R.color.white_with_35) : getResources().getColor(R.color.black_with_60));
        assignmentStatus.setTextColor(isEditMode ? Color.WHITE : Color.BLACK);
    }
}

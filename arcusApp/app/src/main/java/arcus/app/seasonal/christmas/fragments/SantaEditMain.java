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
package arcus.app.seasonal.christmas.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;

import arcus.cornea.provider.DeviceModelProvider;
import arcus.cornea.utils.Listeners;
import com.iris.client.capability.Motion;
import com.iris.client.event.Listener;
import com.iris.client.event.ListenerRegistration;
import com.iris.client.model.DeviceModel;
import arcus.app.R;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.popups.InfoTextPopup;
import arcus.app.seasonal.christmas.activity.SantaPictureActivity;
import arcus.app.seasonal.christmas.fragments.adapter.SantaHistoryListAdapter;
import arcus.app.seasonal.christmas.util.SantaPhoto;
import arcus.app.seasonal.christmas.model.ChristmasModel;
import arcus.app.seasonal.christmas.util.ChristmasModelUtils;
import arcus.app.seasonal.christmas.model.SantaContactSensor;
import arcus.app.seasonal.christmas.util.SantaEventTiming;
import arcus.app.seasonal.christmas.model.SantaHistoryItemModel;
import arcus.app.seasonal.christmas.model.SantaMotionSensor;
import arcus.app.common.view.Version1TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SantaEditMain extends BaseChristmasFragment {
    private ChristmasModel christmasModel;
    private ListenerRegistration deviceStoreListener;
    private final Comparator<SantaHistoryItemModel> historyItemComparator = new Comparator<SantaHistoryItemModel>() {
        @Override
        public int compare(SantaHistoryItemModel lhs, SantaHistoryItemModel rhs) {
            return lhs.getTitle().compareToIgnoreCase(rhs.getTitle());
        }
    };
    private final Listener<List<DeviceModel>> storeLoaded = Listeners.runOnUiThread(new Listener<List<DeviceModel>>() {
        @Override
        public void onEvent(List<DeviceModel> deviceModels) {
            generateHistoryLogData(deviceModels);
        }
    });

    public static SantaEditMain newInstance() {
        return new SantaEditMain();
    }

    @Override
    public void onResume() {
        super.onResume();

        View rootView = getView();
        if (rootView == null) {
            return;
        }

        christmasModel = ChristmasModelUtils.getModelCacheFromDisk();
        if (!christmasModel.isSetupComplete()) {
            BackstackManager.getInstance().navigateBack();
            return;
        }

        View editContainer = rootView.findViewById(R.id.santa_edit_container);
        View merryChristmasContainer = rootView.findViewById(R.id.merry_christmas_text_container);
        if (editContainer != null) {
            if (SantaEventTiming.instance().hasSantaVisited()) {
                editContainer.setVisibility(View.GONE);
                if (merryChristmasContainer != null) {
                    merryChristmasContainer.setVisibility(View.VISIBLE);
                }
            }
            else {
                if (merryChristmasContainer != null) {
                    merryChristmasContainer.setVisibility(View.GONE);
                }
                editContainer.setVisibility(View.VISIBLE);
                editContainer.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        BackstackManager.getInstance()
                              .navigateToFragment(SantaEditList.newInstance(christmasModel), true);
                    }
                });
            }
        }

        if (christmasModel.hasImageSaved()) {
            loadImages();
        }
        else {
            View view = rootView.findViewById(R.id.santa_picture_container);
            View divider = rootView.findViewById(R.id.layout_divider2);
            if (view != null) {
                view.setVisibility(View.GONE);
            }
            if (divider != null) {
                divider.setVisibility(View.GONE);
            }
        }

        deviceStoreListener = DeviceModelProvider.instance().addStoreLoadListener(storeLoaded);
    }

    public void onPause() {
        super.onPause();
        Listeners.clear(deviceStoreListener);
    }

    public void onDestroy() {
        super.onDestroy();
        Listeners.clear(deviceStoreListener);
    }

    protected void loadImages() {
        View rootView = getView();
        if (rootView == null) {
            return;
        }

        ImageView santaPhoto = (ImageView) rootView.findViewById(R.id.santa_photo_image_view);
        Version1TextView santaSightingText = (Version1TextView) rootView.findViewById(R.id.santa_photo_found_not_found);

        if (santaPhoto != null) {
            SantaPhoto photo = new SantaPhoto();

            santaPhoto.setImageDrawable(new BitmapDrawable(getResources(), photo.getSantaPhoto()));
            santaPhoto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), SantaPictureActivity.class);
                    getActivity().startActivity(intent);
                }
            });

            if (SantaEventTiming.instance().hasSantaVisited()) {
                santaSightingText.setVisibility(View.GONE);
                santaPhoto.setImageBitmap(photo.getSantaOverlayPhoto());
                showEventTimeContainer();
            }
            else {
                View timeContainer = rootView.findViewById(R.id.santa_event_time_container);
                if (timeContainer != null) {
                    timeContainer.setVisibility(View.GONE);
                }
                santaSightingText.setVisibility(View.VISIBLE);
            }
        }

        View divider = rootView.findViewById(R.id.layout_divider2);
        if (divider != null) {
            divider.setVisibility(View.VISIBLE);
        }
    }

    private void showEventTimeContainer() {
        View rootView = getView();
        if (rootView == null) {
            return;
        }

        View timeContainer = rootView.findViewById(R.id.santa_event_time_container);
        Version1TextView timeText = (Version1TextView) rootView.findViewById(R.id.santa_event_time_text);
        if (timeContainer != null) {
            timeContainer.setVisibility(View.VISIBLE);
        }
        if (timeText != null) {
            timeText.setText(SantaEventTiming.instance().getFormattedEventTime());
        }

        final View downloadButton = rootView.findViewById(R.id.santa_picture_save_to_media);
        if (downloadButton == null) {
            return;
        }

        downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bitmap overlay = BitmapFactory.decodeResource(getResources(), R.drawable.santa_blurred_2016);
                SantaPhoto santaPhoto = new SantaPhoto();
                showProgressBarAndDisable(downloadButton);
                downloadButton.setAlpha(0.4f);
                santaPhoto.saveFileToPhotos(santaPhoto.getSantaPhoto(), new SantaPhoto.SaveCallback() {
                    @Override
                    public void onSaveSuccess(File file) {
                        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                        Uri uri = Uri.fromFile(file);
                        intent.setData(uri);
                        getActivity().sendBroadcast(intent);

                        hideProgressBarAndEnable(downloadButton);
                        downloadButton.setAlpha(1f);
                        InfoTextPopup infoTextPopup = InfoTextPopup.newInstance(
                              R.string.santa_picture_saved_desc,
                              R.string.santa_picture_saved_title
                        );
                        BackstackManager.getInstance().navigateToFloatingFragment(
                              infoTextPopup,
                              infoTextPopup.getClass().getCanonicalName(),
                              true
                        );
                    }

                    @Override
                    public void onSaveFailure(Throwable throwable) {
                        hideProgressBarAndEnable(downloadButton);
                        downloadButton.setAlpha(1f);
                        InfoTextPopup infoTextPopup = InfoTextPopup.newInstance(
                              R.string.santa_picture_saved_failure_desc,
                              R.string.santa_picture_saved_failure_title
                        );

                        BackstackManager.getInstance().navigateToFloatingFragment(
                              infoTextPopup,
                              infoTextPopup.getClass().getCanonicalName(),
                              true
                        );
                    }
                });
            }
        });
    }

    private void generateHistoryLogData(List<DeviceModel> deviceModels) {
        View rootView = getView();
        if (rootView == null) {
            return;
        }

        ListView santaHistoryListView = (ListView) rootView.findViewById(R.id.santa_history_list);
        if (santaHistoryListView == null) {
            return;
        }

        List<SantaHistoryItemModel> santaHistoryItemModels = new ArrayList<>();

        // Reindeer Landing
        String landingSpot = christmasModel.getLandingSpot("ROOF") + " SENSOR";
        if (SantaEventTiming.instance().hasSantaVisited()) {
            santaHistoryItemModels.add(new SantaHistoryItemModel(SantaEventTiming.instance().getEventDate(), landingSpot,
                  getString(R.string.santa_reindeer_landing_detected)));
        }
        else {
            santaHistoryItemModels.add(new SantaHistoryItemModel(SantaEventTiming.instance().getEventDate(), landingSpot,
                  getString(R.string.santa_history_not_detected)));
        }

        // Santa entered home, tripped fake contact sensors
        for (String sensor : christmasModel.getContactSensors()) {
            SantaHistoryItemModel item = getSantaSensor(sensor);
            if (item != null) {
                santaHistoryItemModels.add(item);
            }
        }

        // Santa entered home, tripped actual contact sensors
        List<SantaHistoryItemModel> realDeviceList = new ArrayList<>();
        for (String deviceIdentifier : christmasModel.getContactSensors()) {
            SantaHistoryItemModel item = getRealDevice(deviceModels, deviceIdentifier);
            if (item != null) {
                realDeviceList.add(item);
            }
        }

        // Santa moving through home, tripped real motion sensors
        for (String deviceIdentifier : christmasModel.getMotionSensors()) {
            SantaHistoryItemModel item = getRealDevice(deviceModels, deviceIdentifier);
            if (item != null) {
                realDeviceList.add(item);
            }
        }

        // Sort, and Add, Real Devices
        Collections.sort(realDeviceList, historyItemComparator);
        santaHistoryItemModels.addAll(realDeviceList);

        // Santa moving through home, trigged fake Santa sensors (i.e., Milk and Cookies)
        for (String sensor : christmasModel.getMotionSensors()) {
            SantaHistoryItemModel item = getSantaSensor(sensor);
            if (item != null) {
                santaHistoryItemModels.add(item);
            }
        }

        // Add exit event logs if Santa has come
        if (SantaEventTiming.instance().hasSantaVisited()) {

            // Santa exited home, tripped fake contact sensors
            for (String sensor : christmasModel.getContactSensors()) {
                SantaHistoryItemModel item = getSantaSensor(sensor);
                if (item != null) {
                    santaHistoryItemModels.add(item);
                }
            }

            // Reindeer departure
            if (SantaEventTiming.instance().hasSantaVisited()) {
                santaHistoryItemModels.add(new SantaHistoryItemModel(SantaEventTiming.instance().getEventDate(), landingSpot,
                        getString(R.string.santa_reindeer_landing_detected)));
            }
        }

        // Display List
        santaHistoryListView.setAdapter(new SantaHistoryListAdapter(getActivity(), santaHistoryItemModels));
    }

    private @Nullable
    SantaHistoryItemModel getRealDevice(List<DeviceModel> deviceModels, String deviceID) {
        for (DeviceModel device : deviceModels) {
            if (device.getId().equals(deviceID)) {
                String title = String.valueOf(device.getName()).toUpperCase();
                String detection = getString(R.string.santa_history_not_detected);

                if (SantaEventTiming.instance().hasSantaVisited()) {
                    if (device.getCaps().contains(Motion.NAMESPACE)) {
                        detection = getString(R.string.santa_motion_sensor_detected);
                    }
                    else {
                        detection = getString(R.string.santa_contact_sensor_detected);
                    }

                    return new SantaHistoryItemModel(SantaEventTiming.instance().getEventDate(), title, detection);
                }
                else {
                    return new SantaHistoryItemModel(SantaEventTiming.instance().getEventDate(), title, detection);
                }
            }
        }

        return null;
    }

    public @Nullable
    SantaHistoryItemModel getSantaSensor(String device) {
        for (SantaContactSensor sensor : SantaContactSensor.values()) {
            String title = getString(sensor.getTitle());
            if (title.equals(device)) {
                if (SantaEventTiming.instance().hasSantaVisited()) {
                    // TODO: Only one "fake" contact sensor supported (Chimney); will need to modify to support others
                    return new SantaHistoryItemModel(SantaEventTiming.instance().getEventDate(), title, getString(R.string.santa_chimney_sensor_detected));
                }
                else {
                    return new SantaHistoryItemModel(SantaEventTiming.instance().getEventDate(), title, getString(R.string.santa_history_not_detected));
                }
            }
        }

        return getMotionSensor(device);
    }

    public @Nullable
    SantaHistoryItemModel getMotionSensor(String device) {
        for (SantaMotionSensor sensor : SantaMotionSensor.values()) {
            String title = getString(sensor.getTitle());
            if (title.equals(device)) {
                String detection = getString(R.string.santa_history_not_detected);

                if (SantaEventTiming.instance().hasSantaVisited()) {
                    if (SantaMotionSensor.MILK_COOKIE.equals(sensor)) {
                        detection = getString(R.string.santa_milk_and_cookies_detected);
                    }
                    else {
                        detection = getString(R.string.santa_sensor_motion_detected);
                    }
                    return new SantaHistoryItemModel(SantaEventTiming.instance().getEventDate(), title, detection);
                }
                else {
                    return new SantaHistoryItemModel(SantaEventTiming.instance().getEventDate(), title, detection);
                }
            }
        }

        return null;
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.santa_fragment_post_configuration;
    }
}

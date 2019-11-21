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
package arcus.app.device.details;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import androidx.annotation.NonNull;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import arcus.cornea.error.ErrorModel;
import arcus.cornea.subsystem.cameras.model.CameraModel;
import arcus.cornea.subsystem.cameras.model.PlaybackModel;
import arcus.cornea.utils.LooperExecutor;
import com.iris.client.capability.Camera;
import com.iris.client.capability.WiFi;
import arcus.app.R;
import arcus.app.activities.VideoActivity;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.banners.CameraLoadingBanner;
import arcus.app.common.banners.core.BannerManager;
import arcus.app.common.error.ErrorManager;
import arcus.app.common.fragments.IShowedFragment;
import arcus.app.subsystems.alarm.AlertFloatingFragment;
import arcus.presentation.cameras.CameraPlaybackPresenter;
import arcus.presentation.cameras.CameraPlaybackPresenterImpl;
import arcus.presentation.cameras.PlaybackView;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyChangeEvent;


public class CameraFragment extends ArcusProductFragment
        implements View.OnClickListener,
        PlaybackView,
        IShowedFragment {

    private static final Logger logger = LoggerFactory.getLogger(CameraFragment.class);
    private CameraPlaybackPresenter cameraPlaybackPresenter;
    private TextView resolutionTV;
    private TextView frameRateTV;
    private ImageButton streamBtn;
    private ImageButton recordBtn;
    private TextView recText;
    private CameraModel cameraModel;

    @NonNull
    public static CameraFragment newInstance() {
        return new CameraFragment();
    }


    @Override
    public Integer topSectionLayout() {
        return R.layout.camera_top_section;
    }

    @Override
    public void doTopSection() {
        resolutionTV = topView.findViewById(R.id.camera_resolution);
        frameRateTV = topView.findViewById(R.id.camera_frame_rate);

        final Camera camera = getCapability(Camera.class);
        if (camera != null) {
            setResolutionTV(camera.getResolution());
            setFrameRateTV(String.valueOf(camera.getFramerate()));
        }
    }

    @Override
    public void doStatusSection() {
        streamBtn = statusView.findViewById(R.id.camera_stream_btn);
        recordBtn = statusView.findViewById(R.id.camera_record_btn);
        recText   = statusView.findViewById(R.id.streaming_recording_text);

        streamBtn.setOnClickListener(this);
        recordBtn.setOnClickListener(this);
        updateButtonStatus();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (cameraPlaybackPresenter == null) {
            if(getDeviceModel()!=null){
                cameraPlaybackPresenter = CameraPlaybackPresenterImpl.newController(getDeviceModel().getId());
            } else{
                cameraPlaybackPresenter = CameraPlaybackPresenterImpl.newController("");

            }

            cameraPlaybackPresenter.setView(this);
        }
        else {
            cameraPlaybackPresenter.clearView();
            cameraPlaybackPresenter.setView(this);
        }
    }

    public void onPause() {
        super.onPause();

        removeBanner();
        cancelRequest();
        cameraPlaybackPresenter.clearView();
    }

    @Override
    public Integer statusSectionLayout() {
        return R.layout.camera_status;
    }

    private void setResolutionTV(final String resolution){
        resolutionTV.setText(resolution);
    }

    private void setFrameRateTV(final String frameRate){
        frameRateTV.setText(String.format(getString(R.string.camera_fps),frameRate));
    }

    @Override
    protected void propertyUpdated(@NonNull final PropertyChangeEvent event) {
        Activity activity = getActivity();

        switch (event.getPropertyName()) {
            case Camera.ATTR_FRAMERATE:
                if (activity != null) {
                    activity.runOnUiThread(() -> {
                        logger.debug("Camera frame rate changed from {} to {}", event.getOldValue(), event.getNewValue());
                        setFrameRateTV(String.valueOf(event.getNewValue()));
                    });
                }
                break;
            case Camera.ATTR_RESOLUTION:
                if (activity != null) {
                    activity.runOnUiThread(() -> {
                        logger.debug("Camera resolution changed from {} to {}", event.getOldValue(), event.getNewValue());
                        setResolutionTV(String.valueOf(event.getNewValue()));
                    });
                }
                break;
            case WiFi.ATTR_RSSI:
                if (activity != null) {
                    activity.runOnUiThread(this::onShowedFragment);
                }
            default:
                super.propertyUpdated(event);
                break;
        }
    }

    @Override
    public void onClick(@NonNull View v) {
        final int id = v.getId();
        switch (id){
            case R.id.camera_record_btn:
                cameraPlaybackPresenter.startRecording();
                break;
            case R.id.camera_stream_btn:
                cameraPlaybackPresenter.startStream();
                break;
        }
    }

    @Override
    public void showLoading() {
        showLoadingBanner();
        showProgressBar();
    }

    @Override
    public void showPremiumRequired() {
        removeBanner();

        AlertFloatingFragment alert = AlertFloatingFragment.newInstance(
                getString(R.string.camera_record_premium_required),
                getString(R.string.camera_record_premium_required_text),
                null, null, null
        );
        BackstackManager.getInstance().navigateToFloatingFragment(alert, alert.getClass().getCanonicalName(), true);
    }

    @Override
    public void playbackReady(PlaybackModel playbackModel) {
        removeBanner();

        Context context = getContext();
        if (context != null) {
            startActivity(VideoActivity.getLaunchIntent(context, playbackModel));
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == 0 && cameraPlaybackPresenter != null) {
            cameraPlaybackPresenter.stopStreaming();
        }
    }

    @Override public void show(@NonNull CameraModel model) {
        cameraModel = model;
        LooperExecutor.getMainExecutor().execute(() -> {
            try {
                onShowedFragment();
            } catch (Exception ex) { // :/
                logger.debug("Could not update view.", ex);
            }
        });
    }

    private void updateButtonStatus() {
        if (cameraModel == null || cameraModel.isUpgradingFirmware() || !cameraModel.isOnline() || cameraModel.isUnavailable()) {
            enableButtons(false);
            if (cameraModel != null && !cameraModel.isRecordable()) {
                recordBtn.setVisibility(View.GONE);
            } else {
                recordBtn.setVisibility(View.VISIBLE);
            }
            return;
        }

        if (cameraModel.isRecording() && cameraModel.isRecordable()) {
            recText.setText(getString(R.string.camera_recording));
            enableStreamButton(false);
            enableRecordButton(true);
        } else {
            recText.setText((cameraModel.isStreaming()) ? getString(R.string.camera_streaming) : "");
            enableButtons(true);

            if (cameraModel.isRecordable()) {
                recordBtn.setVisibility(View.VISIBLE);
            } else {
                recordBtn.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onShowedFragment() {
        checkConnection();
        updateButtonStatus();

        CameraModel model = cameraModel;
        if (model != null) {
            if (model.shouldShowFPS()) {
                resolutionTV.setVisibility(View.VISIBLE);
                frameRateTV.setVisibility(View.VISIBLE);
                if (statusView != null) {
                    View batteryAndSignalContainer = statusView.findViewById(R.id.battery_and_signal_container);
                    batteryAndSignalContainer.setVisibility(View.GONE);

                    View streamRecordingContainer = statusView.findViewById(R.id.streamingRecordingContainer);
                    streamRecordingContainer.setVisibility(View.VISIBLE);
                }
            } else {
                resolutionTV.setVisibility(View.GONE);
                frameRateTV.setVisibility(View.GONE);

                if (statusView != null) {
                    View batteryStatusContainer = statusView.findViewById(R.id.battery_status_container);
                    TextView batterySource = batteryStatusContainer.findViewById(R.id.top_status_text);
                    TextView batteryLevel = batteryStatusContainer.findViewById(R.id.bottom_status_text);
                    updatePowerSourceAndBattery(batterySource, batteryLevel);

                    View signalStrengthStatusContainer = statusView.findViewById(R.id.signal_strength_status_container);
                    TextView signalText = signalStrengthStatusContainer.findViewById(R.id.top_status_text);
                    signalText.setAllCaps(true);
                    signalText.setText(getString(R.string.signal_strength));

                    TextView signalLevel = signalStrengthStatusContainer.findViewById(R.id.bottom_status_text);
                    signalLevel.setVisibility(View.GONE);

                    ImageView signalImage = signalStrengthStatusContainer.findViewById(R.id.bottom_status_image);
                    signalImage.setVisibility(View.VISIBLE);

                    switch (model.getSignalLevel()) {
                        case 1:
                            signalImage.setImageResource(R.drawable.wifi_white_2_24x20);
                            break;
                        case 2:
                            signalImage.setImageResource(R.drawable.wifi_white_3_24x20);
                            break;
                        case 3:
                            signalImage.setImageResource(R.drawable.wifi_white_4_24x20);
                            break;
                        case 4:
                            signalImage.setImageResource(R.drawable.wifi_white_5_24x20);
                            break;

                        case 0:
                        default:
                            signalImage.setImageResource(R.drawable.wifi_white_1_24x20);
                    }

                    View streamRecordingContainer = statusView.findViewById(R.id.streamingRecordingContainer);
                    streamRecordingContainer.setVisibility(View.GONE);

                    View batteryAndSignalContainer = statusView.findViewById(R.id.battery_and_signal_container);
                    batteryAndSignalContainer.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    @Override
    public void onError(@NonNull ErrorModel error) {
        removeBanner();
        ErrorManager.in(getActivity()).showGenericBecauseOf(new RuntimeException(error.getMessage()));
    }

    private void showLoadingBanner() {
        showProgressBar();
        CameraLoadingBanner b = new CameraLoadingBanner();
        b.setOnClickListener(v -> {
            cancelRequest();
            removeBanner();
        });
        BannerManager.in(getActivity()).showBanner(b);
        enableButtons(false);
    }

    private void enableButtons(boolean enabled) {
        enableRecordButton(enabled);
        enableStreamButton(enabled);
    }

    private void enableStreamButton(boolean enabled) {
        streamBtn.setEnabled(enabled);
        streamBtn.setAlpha(enabled ? BUTTON_ENABLED_ALPHA : BUTTON_DISABLED_ALPHA);
    }

    private void enableRecordButton(boolean enabled) {
        recordBtn.setEnabled(enabled);
        recordBtn.setAlpha(enabled ? BUTTON_ENABLED_ALPHA : BUTTON_DISABLED_ALPHA);
    }

    public void removeBanner() {
        BannerManager.in(getActivity()).removeBanner(CameraLoadingBanner.class);
        enableButtons(true);
        hideProgressBar();
    }

    private void cancelRequest() {
        if (cameraPlaybackPresenter != null) {
            cameraPlaybackPresenter.cancelRecordOrStreamAttempt();
        }
    }
}

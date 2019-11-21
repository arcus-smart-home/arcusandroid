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
package arcus.app.subsystems.camera.controllers;

import android.app.Activity;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.View;

import com.google.common.base.Strings;
import arcus.cornea.error.ErrorModel;
import arcus.cornea.subsystem.cameras.model.CameraModel;
import arcus.cornea.subsystem.cameras.model.PlaybackModel;
import arcus.app.R;
import arcus.app.activities.VideoActivity;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.cards.SimpleDividerCard;
import arcus.app.common.controller.AbstractCardController;
import arcus.app.common.error.ErrorManager;
import arcus.app.subsystems.alarm.AlertFloatingFragment;
import arcus.app.subsystems.camera.cards.DeviceCard;
import arcus.presentation.cameras.CameraPlaybackPresenterImpl;
import arcus.presentation.cameras.PlaybackView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


public class DeviceCardController extends AbstractCardController<SimpleDividerCard> implements DeviceCard.OnClickListener, PlaybackView {

    @NonNull
    private SimpleDateFormat longFormat = new SimpleDateFormat("EEE MMM d, h:mm a", Locale.getDefault());
    @NonNull
    private SimpleDateFormat format = new SimpleDateFormat("h:mm a", Locale.getDefault());
    private CameraPlaybackPresenterImpl presenter;
    private CameraModel cameraModel;
    private String mDeviceId;
    private View loadingBanner;
    private final View.OnClickListener loadingBannerClickListener = v -> {
        cancelRequest();
        dismissBanner();
    };

    public DeviceCardController(Context context, @NonNull CameraModel model) {
        super(context);
        cameraModel = model;
        mDeviceId = cameraModel.getCameraID();

        DeviceCard card = new DeviceCard(context);
        card.setHideButtons(cameraModel.isUpgradingFirmware());
        card.setRecording(cameraModel.isRecording());
        card.setOnClickListener(this);

        if (!Strings.isNullOrEmpty(cameraModel.getCameraName())) {
            card.setTitle(cameraModel.getCameraName());
        }

        card.setCacheFile(model.getPreviewCacheFile());
        card.setDescription(getDescriptionString());
        card.setRecordable(model.isRecordable());
        setCurrentCard(card);
        loadingBanner = ((Activity) getContext()).findViewById(R.id.video_loading_banner);
    }

    @Override
    public void setCallback(Callback delegate) {
        super.setCallback(delegate);

        if (mDeviceId != null) {
            presenter = CameraPlaybackPresenterImpl.newController(mDeviceId);
        }

        presenter.setView(this);
    }

    @Override
    public SimpleDividerCard getCard() {
        return getCurrentCard();
    }

    @Override
    public void removeCallback() {
        super.removeCallback();

        presenter.clearView();
    }

    @NonNull
    private String getDescriptionString() {
        String descriptionString = "";
            if (isToday(cameraModel.getLastPreviewUpdate())) {
                descriptionString = "Today " + format.format(cameraModel.getLastPreviewUpdate());
            }
            else if (isYesterday(cameraModel.getLastPreviewUpdate())) {
                descriptionString = "Yesterday " + format.format(cameraModel.getLastPreviewUpdate());
            }
            else if (cameraModel.getLastPreviewUpdate() != null) {
                descriptionString = longFormat.format(cameraModel.getLastPreviewUpdate());
            }
    return descriptionString;
    }

    private boolean isToday(@Nullable Date date) {
        if (date == null) return false;

        Calendar c1 = Calendar.getInstance(); // today

        Calendar c2 = Calendar.getInstance();
        c2.setTime(date); // your date

        return (c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR)
                && c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR));
    }

    private boolean isYesterday(@Nullable Date date) {
        if (date == null) return false;

        Calendar c1 = Calendar.getInstance(); // today
        c1.add(Calendar.DAY_OF_YEAR, -1); // yesterday

        Calendar c2 = Calendar.getInstance();
        c2.setTime(date); // your date

        return (c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR)
                && c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR));
    }

    /***
     * DeviceCard OnClickListener
     */
    @Override
    public void onPlay() {
        presenter.startStream();
    }

    @Override
    public void onRecord() {
        presenter.startRecording();
    }

    @Override
    public void onStream() {
        if (cameraModel != null && !CameraModel.CameraState.IDLE.equals(cameraModel.getCameraState())) {
            presenter.startStream();
        }
    }

    @Override
    public void showLoading() {
        if (loadingBanner != null) {
            loadingBanner.setVisibility(View.VISIBLE);
            loadingBanner.setOnClickListener(loadingBannerClickListener);
        } else {
            DeviceCard card = (DeviceCard) getCard();
            if (card != null) {
                card.setDescription("Loading...");
            }
        }
    }

    @Override
    public void showPremiumRequired() {
        AlertFloatingFragment alert = AlertFloatingFragment.newInstance(
              getContext().getString(R.string.camera_record_premium_required),
              getContext().getString(R.string.camera_record_premium_required_text),
              null, null, null);
        BackstackManager.getInstance().navigateToFloatingFragment(alert, alert.getClass().getCanonicalName(), true);
    }

    @Override
    public void playbackReady(PlaybackModel playbackModel) {
        dismissBanner();

        Context context = getContext();
        if (context != null) {
            context.startActivity(VideoActivity.getLaunchIntent(context, playbackModel));
        }
    }

    @Override
    public void show(@NonNull CameraModel model) {
        cameraModel = model;
        DeviceCard card = (DeviceCard) getCard();
        if (card == null) {
            return;
        }

        card.setIsOffline(!model.isOnline());
        card.setUnavailable(model.isUnavailable());
        card.setFirmwareUpdating(model.isUpgradingFirmware());
        card.setCacheFile(model.getPreviewCacheFile());
        setCurrentCard(card);
    }


    @Override
    public void onError(@NonNull ErrorModel error) {
        dismissBanner();

        DeviceCard card = (DeviceCard) getCard();
        if (card != null) {
            card.setDescription(getDescriptionString());
        }

        ErrorManager.in((Activity) getContext()).showGenericBecauseOf(new RuntimeException(error.getMessage()));
    }

    private void cancelRequest() {
        if (presenter != null) {
            presenter.cancelRecordOrStreamAttempt();
        }
    }

    private void dismissBanner() {
        loadingBanner.setVisibility(View.GONE);
    }
}

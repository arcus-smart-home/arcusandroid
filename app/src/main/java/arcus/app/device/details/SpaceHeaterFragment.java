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

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import arcus.cornea.device.climate.SpaceHeaterControllerDetailsModel;
import arcus.cornea.device.climate.SpaceHeaterDeviceController;
import arcus.cornea.error.ErrorModel;
import arcus.cornea.subsystem.climate.SpaceHeaterScheduleViewController;
import arcus.cornea.subsystem.climate.model.ScheduleModel;
import arcus.cornea.subsystem.climate.model.ScheduledDay;
import arcus.cornea.utils.Listeners;
import com.iris.client.capability.DeviceAdvanced;
import com.iris.client.capability.SpaceHeater;
import com.iris.client.capability.TwinStar;
import com.iris.client.event.ListenerRegistration;
import arcus.app.R;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.banners.MultipleErrorsBanner;
import arcus.app.common.banners.SingleErrorBanner;
import arcus.app.common.banners.core.BannerManager;
import arcus.app.common.error.fragment.ErrorListFragment;
import arcus.app.common.fragments.IClosedFragment;
import arcus.app.common.fragments.IShowedFragment;
import arcus.app.common.models.FullScreenErrorModel;
import arcus.app.common.utils.GlobalSetting;
import arcus.app.common.view.GlowableImageView;
import arcus.app.common.view.Version1TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


public class SpaceHeaterFragment extends ArcusProductFragment implements IShowedFragment, IClosedFragment, SpaceHeaterDeviceController.Callback, SpaceHeaterScheduleViewController.Callback {

    private Version1TextView nextEventLabel;
    private Version1TextView nextEventDescription;

    private TextView tempBottomText;
    private ImageButton plusButton;
    private ImageButton minusButton;
    private ImageView heatImage;
    private ToggleButton powerToggle;
    private ToggleButton ecomode;
    private View tempControls;
    protected TextView centerTempTextView;

    private SpaceHeaterDeviceController mDeviceController;
    private ListenerRegistration controllerListener;
    private boolean bListenForErrors = false;

    @NonNull
    public static SpaceHeaterFragment newInstance() {
        return new SpaceHeaterFragment();
    }

    @Override
    public void onResume() {

        super.onResume();
        nextEventLabel.setVisibility(View.INVISIBLE);
        nextEventDescription.setVisibility(View.INVISIBLE);

        mDeviceController = SpaceHeaterDeviceController.newController(getDeviceModel().getId());
        controllerListener = mDeviceController.setCallback(this);
    }

    @Override
    public void onShowedFragment() {
        checkConnection();
        if (getDeviceModel() != null) {
            int errorCount = getErrors().size();

            if (errorCount > 1) {
                showMultipleErrorBanner();
            } else if (errorCount == 1) {
                showSingleErrorBanner(getErrors());
            }
            bListenForErrors = true;
        }
    }

    @Override
    public void onClosedFragment() {
        BannerManager.in(getActivity()).removeBanner(SingleErrorBanner.class);
        BannerManager.in(getActivity()).removeBanner(MultipleErrorsBanner.class);
        bListenForErrors = true;
    }

    @Override public void onPause() {
        super.onPause();
        Listeners.clear(controllerListener);
        hideProgressBar();
    }

    @Override
    public Integer topSectionLayout() {
        return R.layout.device_top_schedule;
    }

    @Override
    public void doTopSection() {
        nextEventLabel = (Version1TextView) topView.findViewById(R.id.device_top_schdule_event);
        nextEventLabel.setText(getString(R.string.next_event_label));
        nextEventDescription = (Version1TextView) topView.findViewById(R.id.device_top_schdule_time);
    }

    @Override
    public Integer deviceImageSectionLayout() {
        return R.layout.spaceheater_image_section;
    }

    @Override
    public void doDeviceImageSection() {
        deviceImage = (GlowableImageView) deviceImageView.findViewById(R.id.fragment_device_info_image);
        deviceImage.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.empty_large_circle_size));
        deviceImage.setGlowMode(GlowableImageView.GlowMode.ON_OFF);
        deviceImage.setGlowing(false);
        heatImage = (ImageView) deviceImageView.findViewById(R.id.center_heat_icon);
    }

    @Override
    public void doStatusSection() {
        View tempView = statusView.findViewById(R.id.status_temp);

        TextView tempTopText = (TextView) tempView.findViewById(R.id.top_status_text);
        tempBottomText = (TextView) tempView.findViewById(R.id.bottom_status_text);
        minusButton = (ImageButton) statusView.findViewById(R.id.minus_btn);
        plusButton = (ImageButton) statusView.findViewById(R.id.plus_btn);
        tempControls = statusView.findViewById(R.id.temp_controls);
        centerTempTextView = (TextView) deviceImageView.findViewById(R.id.center_status_temp);

        tempTopText.setText(getString(R.string.set_to));

        minusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDeviceController.leftButtonEvent();
            }
        });
        plusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDeviceController.rightButtonEvent();
            }
        });
        powerToggle = (ToggleButton) statusView.findViewById(R.id.power_toggle);
        powerToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(powerToggle.isChecked()) {
                    //turn power on
                    mDeviceController.updateSpaceHeaterMode(SpaceHeater.HEATSTATE_ON);
                    updateControlLayout();
                }
                else {
                    //turn power off
                    mDeviceController.updateSpaceHeaterMode(SpaceHeater.HEATSTATE_OFF);
                    updateControlLayout();
                }
            }
        });
        ecomode = (ToggleButton) statusView.findViewById(R.id.ecomode_toggle);
        ecomode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(ecomode.isChecked()) {
                    //turn ecomode on
                    mDeviceController.updateEcoMode(TwinStar.ECOMODE_ENABLED);
                    updateControlLayout();
                }
                else {
                    //turn ecomode off
                    mDeviceController.updateEcoMode(TwinStar.ECOMODE_DISABLED);
                    updateControlLayout();
                }
            }
        });
        updateControlLayout();
    }

    private void enableButton(boolean bEnabled, View view) {
        view.setEnabled(bEnabled);
        view.setAlpha(bEnabled ? BUTTON_ENABLED_ALPHA : BUTTON_DISABLED_ALPHA);
    }

    private void updateControlLayout() {
        if((!ecomode.isChecked() && !powerToggle.isChecked()) || ecomode.isChecked()) {
            tempControls.setVisibility(View.INVISIBLE);
        }
        else {
            tempControls.setVisibility(View.VISIBLE);
        }
        heatImage.setVisibility(powerToggle.isChecked() ? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    public Integer statusSectionLayout() {
        return R.layout.spaceheater_status;
    }

    @Override
    public void showDeviceControls(SpaceHeaterControllerDetailsModel model) {
        if(!model.isInOTA() && model.isOnline()) {
            if(bListenForErrors) {
                int errorCount = getErrors().size();
                BannerManager.in(getActivity()).removeBanner(SingleErrorBanner.class);
                BannerManager.in(getActivity()).removeBanner(MultipleErrorsBanner.class);
                if (errorCount > 1) {
                    showMultipleErrorBanner();
                } else if (errorCount == 1) {
                    showSingleErrorBanner(getErrors());
                }
            }
        }

        enableButton(model.isLeftButtonEnabled(), minusButton);
        enableButton(model.isRightButtonEnabled(), plusButton);
        enableButton(model.isBottomButtonEnabled(), ecomode);
        enableButton(model.isBottomButtonEnabled(), powerToggle);

        ecomode.setChecked(model.isDeviceEcoOn());
        powerToggle.setChecked(model.isDeviceModeOn());
        centerTempTextView.setText(String.format(Locale.getDefault(), "%d", model.getCurrentTemp()));
        tempBottomText.setText(String.format(Locale.getDefault(), "%d", model.getSetPoint()));

        updateControlLayout();

        if(model.getNextEventDisplay() == null || model.getNextEventDisplay().trim().equals("")) {
            nextEventLabel.setVisibility(View.INVISIBLE);
            nextEventDescription.setVisibility(View.INVISIBLE);
        }
        else {
            nextEventDescription.setText(model.getNextEventDisplay());
            nextEventLabel.setVisibility(View.VISIBLE);
            nextEventDescription.setVisibility(View.VISIBLE);
        }
    }

    private Map<String, String> getErrors() {
        Map<String, String> errorMap = new HashMap<>();

        if (getDeviceModel() != null && ((DeviceAdvanced)getDeviceModel()).getErrors() != null) {
            errorMap = ((DeviceAdvanced)getDeviceModel()).getErrors();
        }

        return errorMap;
    }

    private void showMultipleErrorBanner() {
        final MultipleErrorsBanner errBanner = new MultipleErrorsBanner(R.layout.multiple_errors_banner);
        errBanner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMultipleErrorFullBanner();
                BannerManager.in(getActivity()).removeBanner(MultipleErrorsBanner.class);
            }
        });
        BannerManager.in(getActivity()).showBanner(errBanner);
    }

    private void showMultipleErrorFullBanner() {
        ArrayList<FullScreenErrorModel> errorList = new ArrayList<>();
        Map<String, String> errorMap = getErrors();

        if (errorMap != null) {
            String errorDesc;

            for (Map.Entry<String, String> entry : errorMap.entrySet()) {
                errorDesc = entry.getValue();
                errorList.add(new FullScreenErrorModel(errorDesc));
            }
        }

        ErrorListFragment fragment = ErrorListFragment.newInstance(errorList, true);
        BackstackManager.getInstance().navigateToFragment(fragment, true);
    }

    private void showSingleErrorBanner(@NonNull Map<String, String> errorMap) {
        String errorId = "";
        String errorDesc = "";
        String newError;

        for (Map.Entry<String, String> entry : errorMap.entrySet()) {
            errorId = entry.getKey();
            errorDesc = entry.getValue();
        }

        newError = errorDesc;
        String phoneNumber = "";
        if("Thermostat Disconnected".equals(errorId) || "Thermostat Broken".equals(errorId)) {
            phoneNumber = GlobalSetting.TWINSTAR_SUPPORT_NUMBER;
        }
        final SingleErrorBanner banner = new SingleErrorBanner(newError, phoneNumber);
        banner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BannerManager.in(getActivity()).removeBanner(SingleErrorBanner.class);
            }
        });
        BannerManager.in(getActivity()).showBanner(banner);
    }

    @Override
    public void errorOccurred(Throwable throwable) {

    }

    @Override
    public void showSchedule(ScheduleModel model) {

    }

    @Override
    public void showSelectedDay(ScheduledDay model) {

    }

    @Override
    public void onError(ErrorModel error) {

    }
}

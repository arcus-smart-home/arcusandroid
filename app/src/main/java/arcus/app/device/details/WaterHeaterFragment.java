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

import android.app.AlertDialog;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.SpannableString;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import arcus.cornea.utils.TemperatureUtils;
import com.iris.client.capability.AOSmithWaterHeaterController;
import com.iris.client.capability.WaterHeater;
import com.iris.client.event.Listener;
import arcus.app.R;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.banners.MultipleErrorsBanner;
import arcus.app.common.banners.SingleErrorBanner;
import arcus.app.common.banners.core.BannerManager;
import arcus.app.common.error.fragment.ErrorListFragment;
import arcus.app.common.fragments.IClosedFragment;
import arcus.app.common.fragments.IShowedFragment;
import arcus.app.common.models.FullScreenErrorModel;
import arcus.app.common.utils.StringUtils;
import arcus.app.common.view.GlowableImageView;
import arcus.app.subsystems.alarm.AlertFloatingFragment;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class WaterHeaterFragment extends ArcusProductFragment implements IShowedFragment, View.OnClickListener, IClosedFragment {

    private static final int MIN_HEAT_TEMP_VAL = 60;
    private static final int MAX_HEAT_TEMP_DEFAULT = 150;
    private static int MAX_HEAT_TEMP_VAL;

    private TextView tempBottomText;

   // private TextView hotWaterLevelText;

    private AlertDialog alertDialog;

    private boolean mUserInteraction = false;

    private Runnable prevHeatRunnable;

    @NonNull
    private Handler handler = new Handler();
    @NonNull
    private Boolean isRunning = false;

    @NonNull
    public static WaterHeaterFragment newInstance() {
        WaterHeaterFragment fragment = new WaterHeaterFragment();
        return fragment;
    }

    @Override
    public Integer topSectionLayout() {
        return R.layout.device_top_schedule;
    }

    @Override
    public Integer deviceImageSectionLayout() {
        return R.layout.water_heater_image_section;
    }

    @Override
    public void doDeviceImageSection() {
        deviceImage = (GlowableImageView) deviceImageView.findViewById(R.id.fragment_device_info_image);
        if(deviceImage != null){
            deviceImage.setGlowMode(GlowableImageView.GlowMode.ON_OFF);
            deviceImage.setGlowing(false);
        }
    }

    @Override
    public void onShowedFragment() {
        checkConnection();
        if(deviceImage != null){
            deviceImage.setVisibility(View.VISIBLE);
            deviceImage.setGlowMode(GlowableImageView.GlowMode.ON_OFF);
            deviceImage.setGlowing(false);
        }

        if (getDeviceModel() != null) {
            // Set the initial values

            String mode = null;
            AOSmithWaterHeaterController waterHeaterController = getCapability(AOSmithWaterHeaterController.class);
            if (waterHeaterController != null) {
                mode = waterHeaterController.getControlmode();
            }

            if (mode == null)
                mode = "STANDARD";

            setMode(mode, false);

            WaterHeater waterHeater = getCapability(WaterHeater.class);

            if (waterHeater != null) {

                setWaterAvailability(waterHeater.getHotwaterlevel(),waterHeater.getHeatingstate());
                tempBottomText = (TextView) statusView.findViewById(R.id.bottom_status_text);

                if (waterHeater.getSetpoint() != null && tempBottomText != null) {
                    int fahrenheit = TemperatureUtils.roundCelsiusToFahrenheit(waterHeater.getSetpoint());
                    String fahrenheitString = fahrenheit + "º";
                    tempBottomText.setText(fahrenheitString);
                }
            }

            int errorCount = getErrors().size();

            if (errorCount > 1) {
                showMultipleErrorBanner();
            } else if (errorCount == 1) {
                showSingleErrorBanner(getErrors());
            }
        }

    }

    @Override
    public void doTopSection() {

    }




    @Override
    public void doStatusSection() {
        View tempView = statusView.findViewById(R.id.water_heater_status_temp);

        tempBottomText = (TextView) tempView.findViewById(R.id.bottom_status_text);

        TextView tempTopText = (TextView) tempView.findViewById(R.id.top_status_text);
        tempTopText.setText("STANDARD");
        tempBottomText.setText("--");

        final ImageButton minusBtn = (ImageButton) statusView.findViewById(R.id.water_heater_minus_btn);
        ImageButton plusBtn = (ImageButton) statusView.findViewById(R.id.water_heater_plus_btn);
        ImageButton modeBtn = (ImageButton) statusView.findViewById(R.id.water_heater_mode_btn);

        minusBtn.setOnClickListener(this);
        plusBtn.setOnClickListener(this);
        modeBtn.setOnClickListener(this);

        WaterHeater device = getCapability(WaterHeater.class);
        MAX_HEAT_TEMP_VAL = MAX_HEAT_TEMP_DEFAULT;
        if (device != null) {
            if(device.getMaxsetpoint() != null) {
                MAX_HEAT_TEMP_VAL = TemperatureUtils.roundCelsiusToFahrenheit(device.getMaxsetpoint());
            }
            if(device.getSetpoint() != null) {
                int fahrenheit = TemperatureUtils.roundCelsiusToFahrenheit(device.getSetpoint());
                String fahrenheitString = fahrenheit + "º";
                tempBottomText.setText(fahrenheitString);
            }
            if(device instanceof AOSmithWaterHeaterController) {
                String mode = ((AOSmithWaterHeaterController)device).getControlmode();
                if(mode != null) {
                    setMode(mode, false);
                }

            }
        }
    }

    @Override
    public Integer statusSectionLayout() {
        return R.layout.water_heater_status;
    }

    @Override
    public void propertyUpdated(@NonNull PropertyChangeEvent event) {
        Handler handler = new Handler(Looper.getMainLooper());

        switch (event.getPropertyName()) {
            case AOSmithWaterHeaterController.ATTR_CONTROLMODE:
                final String newModeValue = (String) event.getNewValue();
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        setMode(newModeValue, false);
                    }
                });
                break;
            case AOSmithWaterHeaterController.ATTR_ERRORS:
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        // Remove Banners
                        BannerManager.in(getActivity()).removeBanner(SingleErrorBanner.class);
                        BannerManager.in(getActivity()).removeBanner(MultipleErrorsBanner.class);
                        if (getErrors().size() > 1) {
                            showMultipleErrorBanner();
                        } else {
                            showSingleErrorBanner(getErrors());
                        }
                    }
                });
                break;
            case WaterHeater.ATTR_HOTWATERLEVEL:
                final String newHotWaterLevelValue = (String) event.getNewValue();
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        onShowedFragment();

                    }
                });
                break;

            case WaterHeater.ATTR_HEATINGSTATE:
                final Boolean newHeaterState = (Boolean) event.getNewValue();
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        onShowedFragment();

                    }
                });
                break;


            case WaterHeater.ATTR_MAXSETPOINT:
                final Number newMaxSetPoint = (Number) event.getNewValue();
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        setMaxSetPoint(newMaxSetPoint.intValue());
                    }
                });
                break;
            case WaterHeater.ATTR_SETPOINT:
                final Number newHeatValue = (Number) event.getNewValue();
                final Number fHeatValue = TemperatureUtils.roundCelsiusToFahrenheit(newHeatValue.doubleValue());
                if (newHeatValue.intValue() != getDisplayedTemp()) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            setDisplayedTemp(fHeatValue.intValue());
                        }
                    });
                }
                break;
            default:
                super.propertyUpdated(event);
                break;
        }
    }

    private void showSingleErrorBanner(@NonNull Map<String, String> errorMap) {
        String errorId = "";
        String errorDesc = "";
        String newError;

        for (Map.Entry<String, String> entry : errorMap.entrySet()) {
            errorId = entry.getKey();
            errorDesc = entry.getValue();
        }

        newError = errorId + ": " + errorDesc;
        final SingleErrorBanner banner = new SingleErrorBanner(newError);
        banner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BannerManager.in(getActivity()).removeBanner(SingleErrorBanner.class);
            }
        });
        BannerManager.in(getActivity()).showBanner(banner);


    }

    @Override
    public void onClosedFragment() {
        BannerManager.in(getActivity()).removeBanner(SingleErrorBanner.class);
        BannerManager.in(getActivity()).removeBanner(MultipleErrorsBanner.class);
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
            String errorId;
            String errorDesc;

            for (Map.Entry<String, String> entry : errorMap.entrySet()) {
                errorId = entry.getKey();
                errorDesc = entry.getValue();
                errorList.add(new FullScreenErrorModel(errorId + ": " + errorDesc));
            }
        }

        ErrorListFragment fragment = ErrorListFragment.newInstance(errorList);
        BackstackManager.getInstance().navigateToFragment(fragment, true);
    }

    private Map<String, String> getErrors() {
        Map<String, String> errorMap = new HashMap<>();

        AOSmithWaterHeaterController device = getCapability(AOSmithWaterHeaterController.class);
        if (device != null && device.getErrors() != null) {
            errorMap = device.getErrors();
        }

        return errorMap;
    }

    private void setMaxSetPoint(int maxSetPoint) {
        MAX_HEAT_TEMP_VAL = maxSetPoint;
    }

    @Override
    public void onClick(@NonNull View v) {
        final int id = v.getId();
        switch (id) {
            case R.id.water_heater_minus_btn:
                decrementActiveProgress();
                break;
            case R.id.water_heater_plus_btn:
                incrementActiveProgress();
                break;
            case R.id.water_heater_mode_btn:
                loadMode();
                break;
            case R.id.water_heater_mode_energy_smart:
                mUserInteraction = true;
                setMode(AOSmithWaterHeaterController.CONTROLMODE_ENERGY_SMART, true);
                alertDialog.dismiss();
                eventDebounce();
                break;
            case R.id.water_heater_mode_standard:
                mUserInteraction = true;
                setMode(AOSmithWaterHeaterController.CONTROLMODE_STANDARD, true);
                alertDialog.dismiss();
                eventDebounce();
                break;
//            case R.id.water_heater_mode_vacation:
//                mUserInteraction = true;
//                setMode(AOSmithWaterHeaterController.CONTROLMODE_VACATION, true);
//                alertDialog.dismiss();
//                eventDebounce();
//                break;
        }

    }

    private void loadMode() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());

        View dialogView = View.inflate(getActivity(), R.layout.water_heater_mode, null);

        dialogBuilder.setView(dialogView);
        Button energySmartBtn = (Button) dialogView.findViewById(R.id.water_heater_mode_energy_smart);
        Button standardBtn = (Button) dialogView.findViewById(R.id.water_heater_mode_standard);
//        Button vacationBtn = (Button) dialogView.findViewById(R.id.water_heater_mode_vacation);

        energySmartBtn.setOnClickListener(this);
        standardBtn.setOnClickListener(this);
//        vacationBtn.setOnClickListener(this);

        alertDialog = dialogBuilder.create();
        alertDialog.setCancelable(true);
        alertDialog.show();

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        Window window = alertDialog.getWindow();
        lp.copyFrom(window.getAttributes());
        lp.width = statusView.getWidth();

        lp.gravity = Gravity.BOTTOM;
        window.setAttributes(lp);
    }

    private void setMode(@Nullable String mode, boolean fromUser) {
        if (mode == null) return;

        SpannableString modeText = new SpannableString("");

        switch (mode) {
            case AOSmithWaterHeaterController.CONTROLMODE_ENERGY_SMART:
                if (fromUser) {
                    showEnergySmartDialog();
                }
                modeText = StringUtils.getSuperscriptSpan(getActivity().getString(R.string.energy_smart), getActivity().getString(R.string.registered_symbol));
                break;
            case AOSmithWaterHeaterController.CONTROLMODE_STANDARD:
                modeText = new SpannableString(getActivity().getString(R.string.standard));
                break;
            case AOSmithWaterHeaterController.CONTROLMODE_VACATION:
                modeText = new SpannableString(getActivity().getString(R.string.vacation));
                break;
        }

        if (fromUser) {
            AOSmithWaterHeaterController waterHeater = getCapability(AOSmithWaterHeaterController.class);
            if (waterHeater != null) {
                waterHeater.setControlmode(mode);
                getDeviceModel().commit().onFailure(new Listener<Throwable>() {
                    @Override
                    public void onEvent(Throwable throwable) {
                        logger.error("Error updating water heater mode.", throwable);
                    }
                });
            }
        }

        // Set text at bottom for mode setting
        final TextView tempTopText = (TextView) getView().findViewById(R.id.top_status_text);
        if (tempTopText != null)
            tempTopText.setText(modeText);
    }

    private void showEnergySmartDialog() {
        AlertFloatingFragment energySmartDialog = AlertFloatingFragment.newInstance(getString(R.string.water_heater_mode_energy_smart_title),
                getString(R.string.water_heater_mode_energy_smart_prompt), null, null, null);

        BackstackManager.getInstance().navigateToFloatingFragment(energySmartDialog, energySmartDialog.getClass().getCanonicalName(), true);
    }

    private void showTempSafetyDialog() {
        AlertFloatingFragment tempSafetyDialog = AlertFloatingFragment.newInstance(getString(R.string.water_heater_oops),
                getString(R.string.water_heater_max_temp_prompt), null, null, null);

        BackstackManager.getInstance().navigateToFloatingFragment(tempSafetyDialog, tempSafetyDialog.getClass().getCanonicalName(), true);
    }

    private void showMinTempSafetyDialog() {
        AlertFloatingFragment tempSafetyDialog = AlertFloatingFragment.newInstance(getString(R.string.water_heater_min_temp_title),
                getString(R.string.water_heater_min_temp_prompt), null, null, null);

        BackstackManager.getInstance().navigateToFloatingFragment(tempSafetyDialog, tempSafetyDialog.getClass().getCanonicalName(), true);
    }


    private void setWaterAvailability(@NonNull String waterAvailability, @NonNull Boolean heatState) {
       // if (hotWaterLevelText == null) return;
        
        switch (waterAvailability) {
            case WaterHeater.HOTWATERLEVEL_HIGH:
              //  hotWaterLevelText.setText(getString(R.string.water_heater_hot_water_available));
                if(heatState) {
                    setDeviceImage(R.drawable.water_heater_available_heat);
                }
                else {
                    setDeviceImage(R.drawable.water_heater_available);
                }
                break;
            case WaterHeater.HOTWATERLEVEL_MEDIUM:
              //  hotWaterLevelText.setText(getString(R.string.water_heater_hot_water_limited));
                if(heatState) {
                    setDeviceImage(R.drawable.water_heater_limited_heat);
                }
                else {
                    setDeviceImage(R.drawable.water_heater_limited);
                }
                break;
            case WaterHeater.HOTWATERLEVEL_LOW:
              //  hotWaterLevelText.setText(getString(R.string.water_heater_hot_water_unavailable));
                if(heatState) {
                    setDeviceImage(R.drawable.water_heater_no_heat);
                }
                else {
                    setDeviceImage(R.drawable.water_heater_no);
                }
                break;
        }
    }

    private void setDeviceImage(int drawable){
        if(deviceImage != null){
            deviceImage.setImageResource(drawable);
        }
    }


    private int getDisplayedTemp() {
        int tempVal = 1;
        if (tempBottomText != null && !tempBottomText.getText().toString().equals("--")) {
            String tempStr = (tempBottomText.getText().toString()).substring(0, tempBottomText.getText().toString().length() - 1);
            if (tempStr != null) {
                tempVal = Integer.parseInt(tempStr);
            }
        }

        return tempVal;
    }

    private void setDisplayedTemp(int tempVal) {
        if (tempBottomText != null) {
            tempBottomText.setText(tempVal + "º");
        }
    }

    // Device Methods
    private void setDeviceHeatsetpoint(Double value) {
        WaterHeater device = getCapability(WaterHeater.class);

        if (device != null) {
            device.setSetpoint(TemperatureUtils.fahrenheitToCelsius(value));
            getDeviceModel().commit().onFailure(new Listener<Throwable>() {
                @Override
                public void onEvent(Throwable throwable) {
                    logger.error("Error updating water heater heatsetpoint.", throwable);
                }
            });
        }
    }

    private int getDeviceHeatsetpoint() {
        WaterHeater device = getCapability(WaterHeater.class);
        if (device != null && device.getSetpoint() != null)
            return TemperatureUtils.roundCelsiusToFahrenheit(device.getSetpoint());

        return -1;
    }

    private void incrementActiveProgress() {
        mUserInteraction = true;

        if (getDisplayedTemp() >= MAX_HEAT_TEMP_VAL) {
            showTempSafetyDialog();
        } else {
            if (getDisplayedTemp() == 60) {
                tempBottomText.setText(80 + "º");
            } else if (getDisplayedTemp() < MAX_HEAT_TEMP_VAL) {
                tempBottomText.setText((getDisplayedTemp() + 1) + "º");
            }
            scheduleUpdate();
        }
    }

    private void decrementActiveProgress() {
        mUserInteraction = true;

        if (getDisplayedTemp() <= MIN_HEAT_TEMP_VAL) {
            showMinTempSafetyDialog();
        }
        else {
            if (getDisplayedTemp() == 80) {
                tempBottomText.setText(60 + "º");
            } else if (getDisplayedTemp() > MIN_HEAT_TEMP_VAL) {
                tempBottomText.setText((getDisplayedTemp() - 1) + "º");
            }
            scheduleUpdate();
        }
    }

    private void updateSetPoints() {

        final int heatVal = getDeviceHeatsetpoint();
        final int heatProgress = getDisplayedTemp();

        int delayHeat;
        if (heatProgress != heatVal) {
            delayHeat = 2000;
            if (prevHeatRunnable != null) handler.removeCallbacks(prevHeatRunnable);
        } else {
            delayHeat = 1000;
        }
        final Runnable heatRunnable = new Runnable() {
            @Override
            public void run() {
                if (heatVal != heatProgress) {
                    setDeviceHeatsetpoint((double) heatProgress);
                }
            }
        };
        handler.postDelayed(heatRunnable, delayHeat);
        prevHeatRunnable = heatRunnable;

    }

    // Stops values from updating on the return
    private void eventDebounce() {
        Runnable runnable = new Runnable() {

            @Override
            public void run() {
                mUserInteraction = false;
            }
        };

        Handler handler = new Handler();

        handler.postAtTime(runnable, 1000);
    }

    // When pushing the + or - buttons, lets the requests queue up before trying to send the value again
    private void scheduleUpdate() {
        if (!isRunning) {
            // We're going to schedule a runnable
            isRunning = true;

            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    // Update to the latest progress value
                    updateSetPoints();

                    // Runnable is over
                    isRunning = false;
                }
            };
            handler.postDelayed(runnable, 500);
        }
    }

}

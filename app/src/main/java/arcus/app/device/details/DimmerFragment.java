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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import arcus.app.common.banners.core.Banner;
import arcus.cornea.controller.LightColorAndTempController;
import com.iris.client.capability.ColorTemperature;
import com.iris.client.capability.DeviceConnection;
import com.iris.client.capability.Dimmer;
import com.iris.client.capability.Light;
import com.iris.client.capability.PowerUse;
import com.iris.client.capability.Switch;
import com.iris.client.event.Listener;
import com.iris.client.model.DeviceModel;
import arcus.app.R;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.banners.core.BannerManager;
import arcus.app.common.error.ErrorManager;
import arcus.app.common.fragments.IShowedFragment;
import arcus.app.common.popups.ColorTemperaturePopup;
import arcus.app.common.utils.CorneaUtils;
import arcus.app.common.utils.DeviceSeekArc;
import arcus.app.common.utils.I2ColorUtils;
import arcus.app.common.utils.ThrottledExecutor;
import arcus.app.device.details.model.LutronDisplayModel;
import arcus.app.device.details.presenters.LutronContract;
import arcus.app.device.details.presenters.LutronPresenter;

import java.beans.PropertyChangeEvent;

public class DimmerFragment extends ArcusProductFragment implements IShowedFragment, LutronContract.LutronBridgeView,
        DeviceSeekArc.OnSeekArcChangeListener, LightColorAndTempController.Callback, ColorTemperaturePopup.Callback {

    private final static int CORNEA_UPDATE_PERIOD_MS = 1000;    // Min delay between repeated cornea updates
    private final static int QUIESCENCE_MS = 5000;              // Start responding to incoming platform updates after this amount of idleness
    private final static int MIN_BRIGHTNESS = 1;                // Min brightness percentage
    private final static int MAX_BRIGHTNESS = 100;              // Max brightness percentage
    private final static int MIN_ARC_OPACITY = 77;              // Min seek arc opacity (0..255)
    private final static int MAX_ARC_OPACITY = 204;             // Max seek arc opacity (0..255)

    private LutronContract.LutronPresenter presenter = new LutronPresenter();
    private TextView brightBottomText;
    private TextView powerBottomText;
    private ToggleButton onOffToggle;
    private ToggleButton colorOnOffToggle;
    private ThrottledExecutor throttle = new ThrottledExecutor(CORNEA_UPDATE_PERIOD_MS);

    //color and temp controls
    private View colorTempControls;
    private View dimmerPower;

    private ImageView colorTempSettings;

    private LightColorAndTempController colorAndTempController;
    private DimmerFragment dimmer;

    @NonNull
    public static DimmerFragment newInstance() {
        return new DimmerFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dimmer = this;
        if (colorAndTempController == null) {
            colorAndTempController = LightColorAndTempController.instance();
            colorAndTempController.setCallback(this);
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        colorAndTempController.setCallback(this);
    }

    @Override
    public void onPause() {
        Activity activity = getActivity();
        if (activity != null) {
            presenter.clearAllBanners(activity);
        }
        presenter.stopPresenting();
        super.onPause();
    }

    @Override
    public Integer topSectionLayout() {
        return R.layout.device_top_schedule;
    }

    @Override
    public void doTopSection() {
        // Nothing to do
    }

    @Override
    public void doStatusSection() {
        onOffToggle = (ToggleButton) statusView.findViewById(R.id.dimmer_switch_toggle_button);
        TextView brightTopText = (TextView) statusView.findViewById(R.id.top_status_text_left);
        brightBottomText = (TextView) statusView.findViewById(R.id.bottom_status_text_left);
        brightTopText.setText(getString(R.string.brightness));

        TextView powerTopText = (TextView) statusView.findViewById(R.id.top_status_text_right);
        powerBottomText = (TextView) statusView.findViewById(R.id.bottom_status_text_right);
        powerTopText.setText(getString(R.string.energy_usage_text));

        colorTempControls = statusView.findViewById(R.id.colortemp_controls);
        colorOnOffToggle = (ToggleButton) statusView.findViewById(R.id.color_toggle_button);
        dimmerPower = statusView.findViewById(R.id.dimmer_power);
        colorTempSettings = (ImageView) statusView.findViewById(R.id.color_temp_settings);

        colorTempSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(getDeviceModel() != null) {
                    ColorTemperaturePopup colorTemperaturePopup = ColorTemperaturePopup.newInstance(getDeviceModel().getId());
                    colorTemperaturePopup.setCallback(dimmer);
                    BackstackManager.getInstance().navigateToFloatingFragment(colorTemperaturePopup, colorTemperaturePopup.getClass().getCanonicalName(), true);
                }
            }
        });


        seekArc.setVisibility(View.VISIBLE);
        seekArc.setRoundedEdges(true);
        seekArc.setOnSeekArcChangeListener(this);
        seekArc.setLeftArcText("");
        seekArc.setRightArcText("");

        if(onOffToggle == null){
            return;
        }

        onOffToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setUiSwitchState(onOffToggle.isChecked());

                throttle.execute(new Runnable() {
                    @Override
                    public void run() {
                        setPlatformSwitchState(onOffToggle.isChecked());
                    }
                });
            }
        });

        colorOnOffToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setUiSwitchState(colorOnOffToggle.isChecked());

                throttle.execute(new Runnable() {
                    @Override
                    public void run() {
                        setPlatformSwitchState(colorOnOffToggle.isChecked());
                    }
                });
            }
        });


        updateColorControls();
        getArcColor();
    }

    /**
     * Gets the opacity value for the color bar around the slider.
     *
     * @param progress current tracked progress
     * @return opacity to use between 30% (Minimum opacity) and 80% (Maximum Opacity)
     */
    private int getOpacity(final int progress) {
        if (progress < 30) {
            return MIN_ARC_OPACITY;
        } else if (progress > 80) {
            return MAX_ARC_OPACITY;
        } else {
            return (int) (255 * ((float) progress / (float) 100));
        }
    }

    @Override
    public void doDeviceImageSection() {
        super.doDeviceImageSection();
        deviceImage.setBevelVisible(false);

        seekArc = (DeviceSeekArc) deviceImageView.findViewById(R.id.seekArc);
        seekArc.setUseFixedSize(true);
    }

    private void updateColorControls() {
        if(getDeviceModel() != null) {

            final com.iris.client.capability.Color color = CorneaUtils.getCapability(getDeviceModel(), com.iris.client.capability.Color.class);
            final ColorTemperature temperature = CorneaUtils.getCapability(getDeviceModel(), ColorTemperature.class);
            final Light light = CorneaUtils.getCapability(getDeviceModel(), Light.class);

            if(color != null || temperature != null) {
                onOffToggle.setVisibility(View.GONE);
                colorOnOffToggle.setVisibility(View.VISIBLE);
                colorTempControls.setVisibility(View.VISIBLE);
            }
            if(color == null && temperature == null) {
                colorTempControls.setVisibility(View.GONE);
                onOffToggle.setVisibility(View.VISIBLE);
                colorOnOffToggle.setVisibility(View.GONE);
            }
        }
    }

    private int getArcColor() {
        if(getDeviceModel() != null && !DeviceConnection.STATE_OFFLINE.equals(getDeviceModel().get(DeviceConnection.ATTR_STATE))) {

            final com.iris.client.capability.Color color = CorneaUtils.getCapability(getDeviceModel(), com.iris.client.capability.Color.class);
            final ColorTemperature temperature = CorneaUtils.getCapability(getDeviceModel(), ColorTemperature.class);

            float[] colorHSV = new float[]{1f, 1f, I2ColorUtils.hsvValue};
            if (Light.COLORMODE_COLOR.equals(getDeviceModel().get(Light.ATTR_COLORMODE))) {
                if (color != null) {
                    colorHSV[0] = (float) color.getHue();
                    colorHSV[1] = (float) color.getSaturation() / 100f;
                }
                return Color.argb(getOpacity(getPlatformBrightness()),
                        Color.red(Color.HSVToColor(colorHSV)),
                        Color.green(Color.HSVToColor(colorHSV)),
                        Color.blue(Color.HSVToColor(colorHSV)));

            } else if (Light.COLORMODE_COLORTEMP.equals(getDeviceModel().get(Light.ATTR_COLORMODE))) {
                if (temperature != null) {
                    int temp = temperature.getColortemp();
                    int minTemp = temperature.getMincolortemp();
                    int maxTemp = temperature.getMaxcolortemp();
                    float percentage = ((float) (temp - minTemp) / (float) (maxTemp - minTemp));
                    colorHSV = I2ColorUtils.getTemperatureColor3Point(percentage);
                }
                return Color.argb(getOpacity(getPlatformBrightness()),
                        Color.red(Color.HSVToColor(colorHSV)),
                        Color.green(Color.HSVToColor(colorHSV)),
                        Color.blue(Color.HSVToColor(colorHSV)));
            }
        }
        return Color.argb(getOpacity(getPlatformBrightness()),
                Color.red(getResources().getColor(R.color.white_with_35)),
                Color.green(getResources().getColor(R.color.white_with_35)),
                Color.blue(getResources().getColor(R.color.white_with_35)));
    }

    @Override
    public Integer statusSectionLayout() {
        return R.layout.dimmer_status;
    }

    @Override
    public Integer deviceImageSectionLayout() {
        return R.layout.dimmer_image_section;
    }

    @Override
    public void onShowedFragment() {
        presenter.startPresenting(this);

        checkConnection();
        if (getDeviceModel() != null) {
            setUiSwitchState(getPlatformSwitchState());
            setUiBrightness(getDisplayBrightness(getPlatformBrightness(), getPlatformSwitchState()));
        }
        presenter.requestUpdate();
    }

    @Override
    public void onProgressChanged(final DeviceSeekArc seekArc, final int thumb, final int progress, final boolean fromUser) {

        final int effectiveBrightness = (int) ((float)(MAX_BRIGHTNESS - MIN_BRIGHTNESS) * (progress / 100.0)) + MIN_BRIGHTNESS;

        setUiBrightness(effectiveBrightness);
        setUiSwitchState(true);

        throttle.execute(new Runnable() {
            @Override
            public void run() {
                setPlatformBrightness(effectiveBrightness);
                setPlatformSwitchState(true);           // Always send swit:state ON anytime we send a brightness value
            }
        });
    }

    @Override
    public void onStartTrackingTouch(final DeviceSeekArc seekArc, final int thumb, final int progress) {
        // Nothing to do
    }

    @Override
    public void onStopTrackingTouch(final DeviceSeekArc seekArc, final int thumb, final int progress) {
        // Nothing to do
    }

    /**
     * Invoked when a platform property is updated. Handles changes to switch state and brightness
     * by updating the UI only after it has reached quiescence.
     *
     * @param event The property change event that occurred.
     */
    @Override
    public void propertyUpdated(@NonNull final PropertyChangeEvent event) {
        switch (event.getPropertyName()) {

            case Dimmer.ATTR_BRIGHTNESS:
            case Switch.ATTR_STATE:
            case PowerUse.ATTR_INSTANTANEOUS:

                // Update UI only have we've reach quiescence (a few seconds after last user interaction)
                throttle.executeAfterQuiescence(new Runnable() {
                    @Override
                    public void run() {

                        // Update switch UI using new value or platform value
                        boolean switchState = getPlatformSwitchState();

                        if (Switch.ATTR_STATE.equals(event.getPropertyName())) {
                            switchState = Switch.STATE_ON.equals(event.getNewValue());
                        }

                        setUiSwitchState(switchState);
                        setUiBrightness(getPlatformBrightness());

                    }
                }, QUIESCENCE_MS);
                break;
            case Light.ATTR_COLORMODE:
            case ColorTemperature.ATTR_COLORTEMP:
            case com.iris.client.capability.Color.ATTR_HUE:
            case com.iris.client.capability.Color.ATTR_SATURATION:
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateColorControls();
                        seekArc.setLowArcColor(getArcColor());
                        checkConnection();
                    }
                });
                break;
            default:
                logger.debug("Received Dimmer update: {} -> {}", event.getPropertyName(), event.getNewValue());
                super.propertyUpdated(event);
                break;
        }
    }

    /**
     * Updates the state of the UI to reflect the given brightness by modifying the seek arc thumb
     * location and the "BRIGHT" percentage text. Has no affect on the platform.
     *
     * @param newBrightness The desired brightness reading, between 0 and 100
     */
    @SuppressLint("DefaultLocale")
    private void setUiBrightness(int newBrightness) {
        // Temporarily remove the listener to prevent the UI update from firing the callback (and
        // creating a loop of platform messages)
        if(seekArc != null){
            seekArc.setOnSeekArcChangeListener(null);
            seekArc.setProgress(DeviceSeekArc.THUMB_LOW, newBrightness);

            if(getDeviceModel() != null) {

                int red = 255;
                int green = 255;
                int blue = 255;

                Light light = CorneaUtils.getCapability(getDeviceModel(), Light.class);
                if(light != null) {
                    if(Light.COLORMODE_COLOR.equals(getDeviceModel().get(Light.ATTR_COLORMODE))) {
                        com.iris.client.capability.Color color = CorneaUtils.getCapability(getDeviceModel(), com.iris.client.capability.Color.class);
                        if(color != null) {
                            float [] colorHSV = new float [] {color.getHue(), color.getSaturation()/100f, I2ColorUtils.hsvValue};
                            red = Color.red(Color.HSVToColor(colorHSV));
                            green = Color.green(Color.HSVToColor(colorHSV));
                            blue = Color.blue(Color.HSVToColor(colorHSV));
                        }
                    } else if(Light.COLORMODE_COLORTEMP.equals(getDeviceModel().get(Light.ATTR_COLORMODE))) {
                        ColorTemperature temperature = CorneaUtils.getCapability(getDeviceModel(), ColorTemperature.class);
                        if(temperature != null){
                            int temp = temperature.getColortemp();
                            int minTemp = temperature.getMincolortemp();
                            int maxTemp = temperature.getMaxcolortemp();
                            float percentage = ((float) (temp - minTemp) / (float) (maxTemp - minTemp));
                            float [] colorHSV = I2ColorUtils.getTemperatureColor3Point(percentage);

                            red = Color.red(Color.HSVToColor(colorHSV));
                            green = Color.green(Color.HSVToColor(colorHSV));
                            blue = Color.blue(Color.HSVToColor(colorHSV));
                        }
                    }
                }
                seekArc.setProgressColor(DeviceSeekArc.THUMB_LOW, Color.argb(getOpacity(newBrightness), red, green, blue));
                powerBottomText.setText("");
                PowerUse power = CorneaUtils.getCapability(getDeviceModel(), PowerUse.class);
                if(power != null && power.getInstantaneous() != null) {
                    dimmerPower.setVisibility(View.VISIBLE);
                    powerBottomText.setText(String.format("%.1f", power.getInstantaneous()));
                }
                else {
                    dimmerPower.setVisibility(View.GONE);
                }

            }
            brightBottomText.setText(String.format("%d%%", newBrightness));

            // Re-enable the callback once we're done
            seekArc.setOnSeekArcChangeListener(this);
        }

    }

    @Override
    public int getColorFilterValue() {
        if (getDeviceModel() == null) {
            return -1;
        }
        int colorOverlay = 0;
        Light light = CorneaUtils.getCapability(getDeviceModel(), Light.class);
        if (light != null) {
            if (Light.COLORMODE_COLOR.equals(getDeviceModel().get(Light.ATTR_COLORMODE))) {
                com.iris.client.capability.Color color = CorneaUtils.getCapability(getDeviceModel(), com.iris.client.capability.Color.class);
                if (color != null) {
                    float [] colorHSV = new float[]{color.getHue(), (color.getSaturation() / 100f), 1f};
                    colorOverlay = Color.HSVToColor(25, colorHSV);
                }
            } else if (Light.COLORMODE_COLORTEMP.equals(getDeviceModel().get(Light.ATTR_COLORMODE))) {
                ColorTemperature temperature = CorneaUtils.getCapability(getDeviceModel(), ColorTemperature.class);
                if (temperature != null) {
                    int temp = temperature.getColortemp();
                    int minTemp = temperature.getMincolortemp();
                    int maxTemp = temperature.getMaxcolortemp();
                    float percentage = ((float) (temp - minTemp) / (float) (maxTemp - minTemp));
                    float [] colorHSV = I2ColorUtils.getTemperatureColor3Point(percentage);
                    colorOverlay = Color.HSVToColor(25, colorHSV);
                }
            }
        }
        return colorOverlay;
    }

    @Override
    protected ColorFilter getOnlineColorFilter() {
        View colorOverlayView = mView.findViewById(R.id.color_overlay);
        if(getDeviceModel() != null && isVisible() && Light.COLORMODE_COLOR.equals(getDeviceModel().get(Light.ATTR_COLORMODE)) && !DeviceConnection.STATE_OFFLINE.equals(getDeviceModel().get(DeviceConnection.ATTR_STATE))) {
            int colorOverlay = getColorFilterValue();
            if(colorOverlay != 0) {
                colorOverlayView.setVisibility(View.VISIBLE);
                colorOverlayView.setBackgroundColor(colorOverlay);
                //colorOverlayView.getBackground().setColorFilter(new PorterDuffColorFilter(colorOverlay, PorterDuff.Mode.SRC_OVER));
            }
            ColorMatrix cm = new ColorMatrix();
            cm.setSaturation(0f);
            return new ColorMatrixColorFilter(cm);
        }
        else {
            colorOverlayView.setVisibility(View.GONE);
        }
        return null;
    }

    /**
     * Updates the state of the UI toggle switch to the provided value and adjusts the brightness
     * slider may be required. Modifies only the UI; has no affect on the platform value(s).
     *
     * @param switchStateOn True when the switch toggle indicates on; false otherwise.
     */
    private void setUiSwitchState(boolean switchStateOn) {

        if(onOffToggle != null){
            onOffToggle.setChecked(switchStateOn);
        }

        if (colorOnOffToggle != null) {
            colorOnOffToggle.setChecked(switchStateOn);
        }
    }

    /**
     * Gets the current brightness level as reported by the platform or 0 if the value cannot be
     * determined.
     *
     * @return
     */
    private int getPlatformBrightness() {
        Dimmer dimmer = getCapability(Dimmer.class);
        if (dimmer != null && dimmer.getBrightness() != null)
            return dimmer.getBrightness();

        return MIN_BRIGHTNESS;
    }

    /**
     * Updates the brightness value on the platform. Has no direct affect on the UI, but will
     * indirectly cause a UI update when the platform responds with a value-change event.
     *
     * @param brightnessPercent The desired brightness, between 0 and 100
     */
    private void setPlatformBrightness(int brightnessPercent) {
        logger.debug("Setting platform brightness to {}%", brightnessPercent);

        Dimmer dimmer = getCapability(Dimmer.class);
        if (dimmer != null) {
            dimmer.setBrightness(brightnessPercent);

            getDeviceModel().commit().onFailure(new Listener<Throwable>() {
                @Override
                public void onEvent(Throwable throwable) {
                    logger.error("Error updating brightness.", throwable);
                }
            });
        }
    }

    /**
     * Updates the switch state on the platform; when the device has no switch capability, it updates
     * the brightness value instead, toggling between 0 and 100%.
     *
     * @param switchStateOn The value of the switch state; true for ON, false for OFF.
     */
    private void setPlatformSwitchState(final boolean switchStateOn) {
        logger.debug("Setting platform switch state to {}", switchStateOn);

        if (getDeviceModel() == null) {
            logger.debug("Unable to access model. Cannot change state. Model: {}", getDeviceModel());
            return;
        }

        // Device has Switch capability
        if (CorneaUtils.hasCapability(getDeviceModel(), Switch.class)) {
            getDeviceModel().set(Switch.ATTR_STATE, switchStateOn ? Switch.STATE_ON : Switch.STATE_OFF);
            getDeviceModel().commit().onFailure(new Listener<Throwable>() {
                @Override
                public void onEvent(Throwable throwable) {
                    logger.error("Could not update switch state from: [{}] to [{}]", !switchStateOn, switchStateOn, throwable);
                }
            });
        }
    }

    /**
     * Gets the switch state as reported by the platform. If the device does not support the switch
     * state, then this method delegates to brightness.
     *
     * @return True if the switch state is ON or, when a device doesn't support the switch capability
     * then if the brightness level is greater than zero.
     */
    private boolean getPlatformSwitchState() {

        // Device supports Switch capability
        if (CorneaUtils.hasCapability(getDeviceModel(), Switch.class)) {
            return Switch.STATE_ON.equals(getDeviceModel().get(Switch.ATTR_STATE));
        }

        // Device does not support switch capability
        else {
            return getPlatformBrightness() > 0;
        }
    }

    private int getDisplayBrightness (int brightness, boolean switchStateOn) {
        return brightness;
    }

    @Override
    public void onPending(@Nullable Integer progressPercent) {
        // Nothing to do
    }

    @Override
    public void onError(final Throwable throwable) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ErrorManager.in(getActivity()).showGenericBecauseOf(throwable);
            }
        });
    }

    @Override
    public void updateView(@NonNull LutronDisplayModel lutronDisplayModel) {

        cloudIcon.setVisibility(lutronDisplayModel.isCloudConnected() ? View.VISIBLE : View.GONE);

        if (lutronDisplayModel.isBannerVisible()) {
            updateBackground(false);
            setBottomViewAlerting(true);
        }
        else {
            updateBackground(true);
            setBottomViewAlerting(false);
        }
    }

    @NonNull
    @Override
    public DeviceModel getLutronDeviceModel() {
        return getDeviceModel();
    }

    @Override
    public void showBanner(@NonNull Banner banner) {
        Activity activity = getActivity();
        if (activity != null) {
            presenter.showBannerHelper(activity, banner);
        }
    }

    @Override
    public void removeBanner(@NonNull Class<? extends Banner> bannerClass) {
        Activity activity = getActivity();
        if (activity != null) {
            presenter.clearBannerHelper(activity, bannerClass);
        }
    }

    @Override
    public void onColorTempSuccess() {
    }

    @Override
    public void selectionComplete() {
        updateColorControls();
    }
}

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

import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import arcus.cornea.device.smokeandco.HaloContract;
import arcus.cornea.device.smokeandco.HaloModel;
import arcus.cornea.device.smokeandco.HaloPresenter;
import arcus.cornea.model.StringPair;
import arcus.app.R;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.banners.EarlySmokeWarningBanner;
import arcus.app.common.banners.MultipleErrorsBanner;
import arcus.app.common.banners.SingleErrorBanner;
import arcus.app.common.banners.core.BannerManager;
import arcus.app.common.error.ErrorManager;
import arcus.app.common.error.fragment.ErrorListFragment;
import arcus.app.common.fragments.IClosedFragment;
import arcus.app.common.fragments.IShowedFragment;
import arcus.app.common.models.FullScreenErrorModel;
import arcus.app.common.popups.ColorTemperaturePopup;
import arcus.app.common.popups.InfoTextPopup;
import arcus.app.common.utils.DeviceSeekArc;
import arcus.app.common.utils.GlobalSetting;
import arcus.app.common.utils.I2ColorUtils;
import arcus.app.common.utils.StringUtils;
import arcus.app.device.model.DeviceType;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HaloFragment extends ArcusProductFragment
      implements IShowedFragment, IClosedFragment, DeviceSeekArc.OnSeekArcChangeListener, ColorTemperaturePopup.Callback, HaloContract.View {
    private final static int MIN_BRIGHTNESS = 1;                // Min brightness percentage
    private final static int MAX_BRIGHTNESS = 100;              // Max brightness percentage
    private final static int MIN_ARC_OPACITY = 77;              // Min seek arc opacity (0..255)
    private final static int MAX_ARC_OPACITY = 204;             // Max seek arc opacity (0..255)

    private TextView humidityStatus, atmosphereStatus, temperatureStatus, powerText, powerTopText, brightnessText;
    private ToggleButton toggleButton;
    private View radioLayoutSection;
    private HaloPresenter haloPresenter;
    private int white60 = 0x99FFFFFF;
    private HaloModel haloModel;
    private ImageButton radioButton;

    @NonNull public static HaloFragment newInstance(){
        return new HaloFragment();
    }

    @Override public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        white60 = getResources().getColor(R.color.white_with_60);
    }

    @Override public Integer topSectionLayout() {
        return R.layout.halo_top_status;
    }

    @Override public void doTopSection() {
        humidityStatus = (TextView) topView.findViewById(R.id.humidity_area);
        atmosphereStatus = (TextView) topView.findViewById(R.id.atmosphere_area);
        temperatureStatus = (TextView) topView.findViewById(R.id.temperature_area);
    }

    @Override public void onShowedFragment() {
        checkConnection(); // Check Firmwareupdating...
        if (haloPresenter == null) {
            haloPresenter = new HaloPresenter(getDeviceModel().getAddress());
        }

        haloPresenter.startPresenting(this);
        haloPresenter.requestRefreshAndClearChanges(false);
    }

    @Override
    public void onClosedFragment() {
        BannerManager.in(getActivity()).removeBanner(EarlySmokeWarningBanner.class);
        removeErrorBanners();
    }

    @SuppressWarnings("ConstantConditions") @Override public void doStatusSection() {
        radioLayoutSection = statusView.findViewById(R.id.radio_layout_section);
        ImageButton colorSettings = (ImageButton) statusView.findViewById(R.id.color_settings_button);
        if (colorSettings == null) {
            return;
        }

        colorSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (haloModel != null && haloModel.isOnBattery()) {
                    showOnBatteryPopup();
                    return;
                }

                if(getDeviceModel() != null) {
                    ColorTemperaturePopup colorTemperaturePopup = ColorTemperaturePopup.newHaloInstance(
                          getDeviceModel().getId(),
                          HaloContract.RED_BOUNDS_HSV,
                          HaloContract.BG_BOUNDS_HSV);
                    colorTemperaturePopup.setCallback(HaloFragment.this);
                    BackstackManager.getInstance().navigateToFloatingFragment(colorTemperaturePopup, colorTemperaturePopup.getClass().getCanonicalName(), true);
                }
            }
        });

        toggleButton = (ToggleButton) statusView.findViewById(R.id.toggle_on_off);
        toggleButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                boolean checked = ((ToggleButton)v).isChecked();
                if (haloModel != null && haloModel.isOnBattery()) {
                    toggleButton.setChecked(!checked); // Invert to previous state.
                    showOnBatteryPopup();
                    return;
                }

                haloPresenter.setSwitchOn(checked);
            }
        });

        radioButton = (ImageButton) statusView.findViewById(R.id.play_radio_button);
        radioButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (haloModel != null && haloModel.isRadioPlaying()) {
                    haloPresenter.stopPlayingRadio();
                }
                else {
                    if (haloModel != null && haloModel.isOnBattery()) {
                        showRadioOnBatteryPopup();
                    }

                    haloPresenter.playCurrentStation();
                }
            }
        });

        if(haloModel != null) {
            if (haloModel.isRadioPlaying()) {
                radioButton.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.stop_btn_white));
            }
            else {
                radioButton.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.play_btn_white));
            }
        }

        powerText = (TextView) statusView.findViewById(R.id.power_text);
        powerTopText = (TextView) statusView.findViewById(R.id.power_top_text);
        brightnessText = (TextView) statusView.findViewById(R.id.brightness_text);
    }

    @Override public Integer statusSectionLayout() {
        return R.layout.halo_status_section;
    }

    @Override public Integer deviceImageSectionLayout() {
        return R.layout.dimmer_image_section;
    }

    @SuppressWarnings("ConstantConditions") @Override public void doDeviceImageSection() {
        super.doDeviceImageSection();
        seekArc = (DeviceSeekArc) deviceImageView.findViewById(R.id.seekArc);
        if (deviceImage == null || seekArc == null) {
            return;
        }

        deviceImage.setBevelVisible(false);
        seekArc.setUseFixedSize(true);
        seekArc.setRoundedEdges(true);
        seekArc.setMinValue(MIN_BRIGHTNESS);
        seekArc.setMaxValue(MAX_BRIGHTNESS);
        seekArc.setVisibility(View.VISIBLE);
    }

    @Override public void onPause() {
        super.onPause();
        if (haloPresenter != null) {
            haloPresenter.stopPresenting();
        }
    }

    @Override public void selectionComplete() {
        // For Color Picker.
    }

    @Override public void onProgressChanged(DeviceSeekArc seekArc, int thumb, int progress, boolean fromUser) {
        seekArc.setProgressColor(DeviceSeekArc.THUMB_LOW, getArcColorUsing(progress));
        if (brightnessText != null) {
            brightnessText.setText(String.format(Locale.ROOT, "%d%%", progress));
        }
    }

    @Override public void onStartTrackingTouch(DeviceSeekArc seekArc, int thumb, int progress) {}

    @Override public void onStopTrackingTouch(DeviceSeekArc seekArc, int thumb, int progress) {
        haloPresenter.setDimmer(progress);
    }

    @Override public void onError(Throwable throwable) {
        ErrorManager.in(getActivity()).showGenericBecauseOf(throwable);
        haloPresenter.requestRefreshAndClearChanges(true);
    }

    @Override
    public void onPending(Integer progressPercentage) {
        // Nothing to do
    }

    @Override public void updateView(HaloModel model) {
        this.haloModel = model;

        if (humidityStatus != null) {
            humidityStatus.setText(StringUtils.getSuperscriptSpan(haloModel.getHumidity(), "%", white60));
        }

        if (atmosphereStatus != null) {
            atmosphereStatus.setText(StringUtils.getSuperscriptSpan(haloModel.getAtmosphericPressure(), "IN", white60));
        }

        if (temperatureStatus != null) {
            temperatureStatus.setText(StringUtils.applyColorSpan(haloModel.getTemperature(), new String(new char[]{0x00B0}), white60));
        }

        if (!haloModel.isHaloPlus() && radioLayoutSection != null) {
            radioLayoutSection.setVisibility(View.GONE);
        }

        if (toggleButton != null) {
            toggleButton.setChecked(haloModel.isLightOn());
        }

        if (radioButton != null) {
            if (haloModel.isRadioPlaying()) {
                radioButton.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.stop_btn_white));
            }
            else {
                radioButton.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.play_btn_white));
            }
        }

        if (seekArc != null) {
            seekArc.setOnSeekArcChangeListener(null);
            seekArc.setUseFixedSize(true);

            int progress = Math.max(1, haloModel.getDimmerPercent()) + 1;
            if (seekArc.getProgress() != progress) {
                seekArc.setProgress(DeviceSeekArc.THUMB_LOW, progress);
            }

            int arcColor = getArcColor();
            seekArc.setLowArcColor(arcColor);
            seekArc.setProgressColor(DeviceSeekArc.THUMB_LOW, arcColor);
            updateBackground(haloModel.isOnline());
            seekArc.setOnSeekArcChangeListener(this);
        }

        if (brightnessText != null) {
            brightnessText.setText(String.format(Locale.ROOT, "%d%%", haloModel.getDimmerPercent()));
        }

        if (powerText != null && powerTopText != null) {
            if (haloModel.isOnBattery()) {
                powerTopText.setText(R.string.battery);
                powerText.setText(haloModel.getBatteryLevel() > 30 ? getString(R.string.ok) : "" + haloModel.getBatteryLevel());
            }
            else {
                powerTopText.setText(R.string.power);
                powerText.setText(getString(R.string.power_source_ac));
            }
        }

        removeErrorBanners();
        if (!haloModel.isOnline()) {
            return;
        }

        if (haloModel.isPreSmoke()) {
            BannerManager.in(getActivity()).showBanner(new EarlySmokeWarningBanner());
        } else if(!haloModel.isFirmwareUpdating() && haloModel.hasErrors()) {
            if (haloModel.hasSingleError()) {
                showSingleErrorBanner();
            } else {
                showMultipleErrorBanner();
            }
        }
    }

    protected int getArcColorUsing(int percent) {
        if(haloModel != null && haloModel.isOnline()) {
            float[] colorHSV = new float[]{ haloModel.getHue(), haloModel.getSaturation() / 100f, I2ColorUtils.hsvValue};
            int rgb = Color.HSVToColor(colorHSV);
            return toArgbFromColor(getOpacity(percent), rgb, rgb, rgb);
        }

        int white35 = getResources().getColor(R.color.white_with_35);
        return toArgbFromColor(getOpacity(percent), white35, white35, white35);
    }

    protected int getArcColor() {
        if (haloModel == null) {
            return getArcColorUsing(50);
        }

        return getArcColorUsing(haloModel.getDimmerPercent());
    }

    // 30% (Minimum opacity) and 80% (Maximum Opacity)
    protected int getOpacity(final int progress) {
        if (progress < 30) {
            return MIN_ARC_OPACITY;
        }
        else if (progress > 80) {
            return MAX_ARC_OPACITY;
        }
        else {
            return (int) (255 * ((float) progress / (float) 100));
        }
    }

    protected int toArgbFromColor(int opacity, int toRed, int toGreen, int toBlue) {
        return Color.argb(opacity, Color.red(toRed), Color.green(toGreen), Color.blue(toBlue));
    }

    @Override
    public int getColorFilterValue() {
        if (haloModel == null) {
            return -1;
        }
        float [] colorHSV = new float[]{ haloModel.getHue(), (haloModel.getSaturation() / 100f), 1f};
        return Color.HSVToColor(25, colorHSV);
    }

    @Override protected ColorFilter getOnlineColorFilter() {
        if (haloModel == null || mView == null) {
            return null;
        }

        View colorOverlayView = mView.findViewById(R.id.color_overlay);
        if (colorOverlayView == null) {
            return null;
        }

        boolean applyFilter = haloModel.isOnline() && isVisible();
        colorOverlayView.setVisibility(applyFilter ? View.VISIBLE : View.GONE);

        if(applyFilter) {
            float [] colorHSV = new float[]{ haloModel.getHue(), (haloModel.getSaturation() / 100f), 1f};
            colorOverlayView.setBackgroundColor(Color.HSVToColor(25, colorHSV));

            ColorMatrix cm = new ColorMatrix();
            cm.setSaturation(0f);
            return new ColorMatrixColorFilter(cm);
        }

        return null;
    }

    private void showMultipleErrorBanner() {
        final MultipleErrorsBanner errBanner = new MultipleErrorsBanner(R.layout.multiple_errors_banner);
        errBanner.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                showMultipleErrorFullBanner();
                removeErrorBanners();
            }
        });
        BannerManager.in(getActivity()).showBanner(errBanner);
    }

    private void showMultipleErrorFullBanner() {
        if (haloModel == null) {
            return;
        }

        ArrayList<FullScreenErrorModel> errorList = new ArrayList<>();
        for (StringPair entry : haloModel.getErrors()) {
            errorList.add(new FullScreenErrorModel(entry.getValue()));
        }

        ErrorListFragment fragment = ErrorListFragment.newInstance(errorList, getString(R.string.halo_multiple_error_title), DeviceType.HALO.toString());
        BackstackManager.getInstance().navigateToFragment(fragment, true);
    }

    private void showSingleErrorBanner() {
        if (haloModel == null) {
            return;
        }

        List<StringPair> errors = haloModel.getErrors();
        if (errors.isEmpty()) {
            return;
        }

        StringPair error = errors.get(0);

        String buttonText;
        Uri buttonLink;

        if ("End of Life".equals(error.getKey())) {
            buttonText = getString(R.string.generic_shop_text);
            buttonLink = Uri.parse(GlobalSetting.SHOP_NOW_URL);
        } else {
            buttonText = getString(R.string.get_support).toUpperCase();
            buttonLink = GlobalSetting.HALO_SUPPORT_URI;
        }

        SingleErrorBanner banner = new SingleErrorBanner(error.getValue(), buttonText, buttonLink);
        BannerManager.in(getActivity()).showBanner(banner);
    }

    protected void removeErrorBanners() {
        BannerManager.in(getActivity()).removeBanner(SingleErrorBanner.class);
        BannerManager.in(getActivity()).removeBanner(MultipleErrorsBanner.class);
        BannerManager.in(getActivity()).removeBanner(EarlySmokeWarningBanner.class);
    }

    protected void showRadioOnBatteryPopup() {
        navigateToTextPopup(R.string.halo_on_battery_radio_popup_desc);
    }

    protected void showOnBatteryPopup() {
        navigateToTextPopup(R.string.halo_on_battery_popup_desc);
    }

    protected void navigateToTextPopup(int detailText) {
        Fragment f = InfoTextPopup.newInstance(detailText, R.string.halo_on_battery_popup_title);
        BackstackManager.getInstance().navigateToFloatingFragment(f, f.getClass().getCanonicalName(), true);
    }
}

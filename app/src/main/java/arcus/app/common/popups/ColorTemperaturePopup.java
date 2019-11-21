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
package arcus.app.common.popups;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import arcus.cornea.controller.LightColorAndTempController;
import arcus.cornea.utils.Listeners;
import com.iris.client.capability.ColorTemperature;
import com.iris.client.capability.Light;
import com.iris.client.event.ListenerRegistration;
import com.iris.client.model.DeviceModel;
import arcus.app.R;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.error.ErrorManager;
import arcus.app.common.models.SessionModelManager;
import arcus.app.common.utils.ColorPickerArc;
import arcus.app.common.utils.ColorPickerHueArc;
import arcus.app.common.utils.ColorPickerSaturationArc;
import arcus.app.common.utils.ColorPickerTemperatureArc;
import arcus.app.common.utils.CorneaUtils;
import arcus.app.common.utils.I2ColorUtils;
import arcus.app.common.utils.ThrottledExecutor;
import arcus.app.common.view.Version1TextView;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;

public class ColorTemperaturePopup extends ArcusFloatingFragment implements ColorPickerArc.OnSeekArcChangeListener, LightColorAndTempController.Callback {
    private static final String MODEL_ID = "MODEL_ID";
    private static final String MAX_HUE = "MAX_HUE";
    private static final String MIN_HUE = "MIN_HUE";
    private static final String HALO_PICKER = "HALO_PICKER";

    private DeviceModel deviceModel;
    private final static int CORNEA_UPDATE_PERIOD_MS = 1000;
    private final static int THROTTLE_QUIESCENCE_MS = 2000;

    private TextView normalTxtButton;
    private TextView colorTxtButton;
    private TextView temperatureTxtButton;
    private TextView tempLabel;

    // Tab content sections
    private RelativeLayout colorLayout;
    private RelativeLayout temperatureLayout;
    private RelativeLayout normalLayout;

    private ListenerRegistration propertyListener;

    ImageView colorPreview;
    ColorPickerHueArc colorPicker;
    ColorPickerSaturationArc saturationPicker;

    ImageView tempPreview;
    ColorPickerTemperatureArc temperaturePicker;

    TextView colorValueRed;
    TextView colorValueGreen;
    TextView colorValueBlue;

    TextView tempValue;

    float initialHue = 1f;
    float initialSaturation = 1f;
    int initialTemp = -1;

    float hue = 1f;
    float saturation = 1f;
    int temp = -1;
    int minTemp = -1;
    int maxTemp = -1;

    boolean bInitialized = false;

    Version1TextView save;

    private LightColorAndTempController colorAndTempController;
    private ThrottledExecutor throttle = new ThrottledExecutor(CORNEA_UPDATE_PERIOD_MS);

    private WeakReference<Callback> callbackRef = new WeakReference<>(null);

    public interface Callback {
        void selectionComplete();
    }

    public void setCallback(Callback callback) {
        callbackRef = new WeakReference<>(callback);
    }

    @NonNull
    public static ColorTemperaturePopup newInstance(String modelId) {
        ColorTemperaturePopup fragment = new ColorTemperaturePopup();

        Bundle data = new Bundle(3);
        data.putString(MODEL_ID, modelId);
        fragment.setArguments(data);

        return fragment;
    }

    @NonNull
    public static ColorTemperaturePopup newHaloInstance(String modelId, float maxHueValue, float minHueValue) {
        ColorTemperaturePopup fragment = new ColorTemperaturePopup();

        Bundle data = new Bundle(3);
        data.putString(MODEL_ID, modelId);
        data.putFloat(MAX_HUE, maxHueValue);
        data.putFloat(MIN_HUE, minHueValue);
        data.putBoolean(HALO_PICKER, true);

        fragment.setArguments(data);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (colorAndTempController == null) {
            colorAndTempController = LightColorAndTempController.instance();
            colorAndTempController.setCallback(this);
        }
    }

    @Override public void onResume() {
        super.onResume();

        showFullScreen(true);

        if (deviceModel != null) {
            propertyListener = deviceModel.addPropertyChangeListener(new PropertyChangeListener() {
                @Override
                public void propertyChange(final PropertyChangeEvent evt) {
                    throttle.executeAfterQuiescence(new Runnable() {
                        @Override
                        public void run() {
                            updateSelections();
                        }
                    }, THROTTLE_QUIESCENCE_MS);
                }
            });
        }

        updateSelections();
    }

    @Override public void onPause() {
        super.onPause();

        showFullScreen(false);
        Listeners.clear(propertyListener);
    }

    @Override
    public void setFloatingTitle() {
        title.setText("");
    }

    @Override
    public void doContentSection() {
        String deviceId = getArguments().getString(MODEL_ID, "");

        deviceModel = SessionModelManager.instance().getDeviceWithId(deviceId, false);

        if (deviceModel == null) {
            BackstackManager.getInstance().navigateBack();
            Callback callback = callbackRef.get();
            if (callback != null) {
                callback.selectionComplete();
            }
        }

        save = (Version1TextView) contentView.findViewById(R.id.save_button);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(colorLayout.getVisibility() == View.VISIBLE) {
                    colorAndTempController.updateColorValue(deviceModel.getAddress(), hue, saturation*100f);
                }
                if(temperatureLayout.getVisibility() == View.VISIBLE) {
                    colorAndTempController.updateTemperatureValue(deviceModel.getAddress(), temp);
                }
                BackstackManager.getInstance().navigateBack();
                Callback callback = callbackRef.get();
                if (callback != null) {
                    callback.selectionComplete();
                }
            }
        });
    }

    private void updateSelections() {
        final com.iris.client.capability.Color color = CorneaUtils.getCapability(deviceModel, com.iris.client.capability.Color.class);
        final ColorTemperature temperature = CorneaUtils.getCapability(deviceModel, ColorTemperature.class);
        if(color != null) {
            createColorView(color);
        }
        if(temperature != null) {
            createTemperatureView(temperature);
        }

        if (deviceModel != null && Light.COLORMODE_COLOR.equals(deviceModel.get(Light.ATTR_COLORMODE))) {
            showColorPicker();
        } else if (deviceModel != null && Light.COLORMODE_COLORTEMP.equals(deviceModel.get(Light.ATTR_COLORMODE))) {
            showTemperaturePicker();
        } else {
            showNormalMode();
        }
    }

    private void createColorView(com.iris.client.capability.Color color) {
        initialHue = color.getHue();
        initialSaturation = color.getSaturation();

        hue = color.getHue();
        saturation = color.getSaturation()/100f;

        colorPreview = (ImageView) contentView.findViewById(R.id.colorPreview);
        colorPicker = (ColorPickerHueArc) contentView.findViewById(R.id.seekArcColor);

        colorPicker.setMaxHueValue(getArguments().getFloat(MAX_HUE, 360f));
        colorPicker.setMinHueValue(getArguments().getFloat(MIN_HUE, 0f));
        colorPicker.setOnSeekArcChangeListener(this);

        saturationPicker = (ColorPickerSaturationArc) contentView.findViewById(R.id.seekArcSaturation);
        saturationPicker.setOnSeekArcChangeListener(this);

        colorValueRed = (TextView) contentView.findViewById(R.id.colorvaluered);
        colorValueGreen = (TextView) contentView.findViewById(R.id.colorvaluegreen);
        colorValueBlue = (TextView) contentView.findViewById(R.id.colorvalueblue);



        //TODO: Russ - this should have the default or existing HSV value
        float[] colorHSV = new float[] { hue, saturation, I2ColorUtils.hsvValue };
        colorPreview.getBackground().setColorFilter(Color.HSVToColor(colorHSV), PorterDuff.Mode.SRC_ATOP);

        colorValueRed.setText(Integer.toString(Color.red(Color.HSVToColor(colorHSV))));
        colorValueGreen.setText(Integer.toString(Color.green(Color.HSVToColor(colorHSV))));
        colorValueBlue.setText(Integer.toString(Color.blue(Color.HSVToColor(colorHSV))));

        colorPicker.setPosition(colorHSV);
        saturationPicker.setPosition(colorHSV);
        colorAndTempController.setCallback(this);
    }

    private void createTemperatureView(ColorTemperature temperature) {
        initialTemp = temperature.getColortemp();

        temp = temperature.getColortemp();
        minTemp = temperature.getMincolortemp();
        maxTemp = temperature.getMaxcolortemp();

        tempPreview = (ImageView) contentView.findViewById(R.id.tempPreview);
        temperaturePicker = (ColorPickerTemperatureArc) contentView.findViewById(R.id.seekArcTemperature);
        //temperaturePicker.setValues(minTemp, maxTemp);
        temperaturePicker.setOnSeekArcChangeListener(this);
        tempLabel = (TextView) contentView.findViewById(R.id.templabel);

        tempValue = (TextView) contentView.findViewById(R.id.tempvalue);
        float percentage = ((float)(temp-minTemp)/(float)(maxTemp-minTemp));
        temperaturePicker.setPosition(percentage);
        colorAndTempController.setCallback(this);

        tempLabel.setText(getTemperatureString(temp));
    }

    private String getTemperatureString(int temp) {
        if(temp < 2700) {
            return getResources().getString(R.string.warm);
        }
        else if(temp <= 3000) {
            return getResources().getString(R.string.warm_white);
        }
        else if(temp <= 3500) {
            return getResources().getString(R.string.neutral);
        }
        else if(temp <= 4700) {
            return getResources().getString(R.string.cool_white);
        }
        else if(temp <= 6500) {
            return getResources().getString(R.string.day_light);
        }
        else {
            return getResources().getString(R.string.more_blue_sky);
        }
    }


    @Override
    public Integer contentSectionLayout() {
        return R.layout.color_temperature_popup;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup parentGroup = (ViewGroup) super.onCreateView(inflater, container, savedInstanceState);

        //Hide the floating close button and title
        parentGroup.findViewById(R.id.fragment_arcus_pop_up_close_btn).setVisibility(View.GONE);
        parentGroup.findViewById(R.id.fragment_arcus_pop_up_title).setVisibility(View.GONE);

        normalTxtButton = (TextView) parentGroup.findViewById(R.id.txtNormal);
        colorTxtButton = (TextView) parentGroup.findViewById(R.id.txtColor);
        temperatureTxtButton = (TextView) parentGroup.findViewById(R.id.txtTemperature);
        colorLayout = (RelativeLayout) parentGroup.findViewById(R.id.layout_color);
        temperatureLayout = (RelativeLayout) parentGroup.findViewById(R.id.layout_temperature);
        normalLayout = (RelativeLayout) parentGroup.findViewById(R.id.layout_normal);

        final com.iris.client.capability.Color color = CorneaUtils.getCapability(deviceModel, com.iris.client.capability.Color.class);
        final ColorTemperature temperature = CorneaUtils.getCapability(deviceModel, ColorTemperature.class);
        if(color == null) {
            colorTxtButton.setVisibility(View.GONE);
        }
        if(temperature == null) {
            temperatureTxtButton.setVisibility(View.GONE);
        }
        if (isHaloPicker()) {
            normalTxtButton.setVisibility(View.GONE);
        }

        if (deviceModel != null && Light.COLORMODE_COLOR.equals(deviceModel.get(Light.ATTR_COLORMODE))) {
            showColorPicker();
        } else if (deviceModel != null && Light.COLORMODE_COLORTEMP.equals(deviceModel.get(Light.ATTR_COLORMODE))) {
            showTemperaturePicker();
        } else {
            showNormalMode();
        }

        normalTxtButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showNormalMode();
            }
        });
        colorTxtButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showColorPicker();
            }
        });

        temperatureTxtButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTemperaturePicker();
            }
        });
        bInitialized = true;

        return parentGroup;
    }

    private void showNormalMode() {
        temperatureLayout.setVisibility(View.INVISIBLE);
        colorLayout.setVisibility(View.INVISIBLE);
        normalLayout.setVisibility(View.VISIBLE);

        colorTxtButton.setBackgroundResource(0);
        temperatureTxtButton.setBackgroundResource(0);
        normalTxtButton.setBackgroundResource(R.drawable.outline_button_style_black);

        if (bInitialized) {
            colorAndTempController.updateColorMode(deviceModel.getAddress(), Light.COLORMODE_NORMAL);
        }
    }

    private void showColorPicker() {
        temperatureLayout.setVisibility(View.GONE);
        normalLayout.setVisibility(View.GONE);
        colorLayout.setVisibility(View.VISIBLE);

        colorTxtButton.setBackgroundResource(R.drawable.outline_button_style_black);
        temperatureTxtButton.setBackgroundResource(0);
        normalTxtButton.setBackgroundResource(0);

        if(bInitialized) {
            colorAndTempController.updateColorValue(deviceModel.getAddress(), hue, saturation*100f);
        }
    }

    private void showTemperaturePicker() {
        temperatureLayout.setVisibility(View.VISIBLE);
        normalLayout.setVisibility(View.GONE);
        colorLayout.setVisibility(View.GONE);

        temperatureTxtButton.setBackgroundResource(R.drawable.outline_button_style_black);
        colorTxtButton.setBackgroundResource(0);
        normalTxtButton.setBackgroundResource(0);

        if(bInitialized) {
            colorAndTempController.updateTemperatureValue(deviceModel.getAddress(), temp);
        }
    }

    @NonNull
    @Override
    public String getTitle() {
        return "";
    }

    @Override
    public void doClose() {
        if (deviceModel == null) {
            return;
        }
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
    public void onColorTempSuccess() {
    }

    @Override
    public void onProgressChanged(ColorPickerArc seekArc, int progress, boolean fromUser) {
        updatePreview(seekArc);
    }

    @Override
    public void onStartTrackingTouch(ColorPickerArc seekArc, int thumb, int progress) {

    }

    @Override
    public void onStopTrackingTouch(ColorPickerArc seekArc, int thumb, int progress) {
        updatePreview(seekArc);
        if(seekArc instanceof ColorPickerTemperatureArc) {
            if(bInitialized) {
                throttle.executeAfterQuiescence(new Runnable() {
                    @Override
                    public void run() {
                        colorAndTempController.updateTemperatureValue(deviceModel.getAddress(), temp);
                    }
                }, THROTTLE_QUIESCENCE_MS);
            }
        }

        if(seekArc instanceof ColorPickerHueArc) {
            if(bInitialized) {
                throttle.executeAfterQuiescence(new Runnable() {
                    @Override
                    public void run() {
                        colorAndTempController.updateColorValue(deviceModel.getAddress(), hue, saturation * 100f);
                    }
                }, THROTTLE_QUIESCENCE_MS);
            }
        }
        else if(seekArc instanceof ColorPickerSaturationArc) {
            if(bInitialized) {
                throttle.executeAfterQuiescence(new Runnable() {
                    @Override
                    public void run() {
                        colorAndTempController.updateColorValue(deviceModel.getAddress(), hue, saturation*100f);
                    }
                }, THROTTLE_QUIESCENCE_MS);
            }
        }
    }

    private void updatePreview(ColorPickerArc seekArc) {
        float[] colorHSV = new float[] { 1f, 1f, I2ColorUtils.hsvValue };
        float percent = 0f;

        if(seekArc instanceof ColorPickerTemperatureArc) {
            colorHSV = ((ColorPickerTemperatureArc)seekArc).getValue();
            percent = ((ColorPickerTemperatureArc)seekArc).getPercentage();
            tempPreview.getBackground().setColorFilter(Color.HSVToColor(colorHSV), PorterDuff.Mode.SRC_ATOP);
            temp = (int)((maxTemp-minTemp)*percent)+minTemp;
            tempValue.setText(String.format("%d", temp));
            tempLabel.setText(getTemperatureString(temp));
        }

        if(seekArc instanceof ColorPickerHueArc) {
            colorHSV[0] = ((ColorPickerHueArc)seekArc).getHue();
            colorHSV[1] = saturation;
            hue = colorHSV[0];
            saturationPicker.setHue(hue);

            colorPreview.getBackground().setColorFilter(Color.HSVToColor(colorHSV), PorterDuff.Mode.SRC_ATOP);

            float [] colorValue = colorHSV;
            colorValue[2] = 1f;
            colorValueRed.setText(Integer.toString(Color.red(Color.HSVToColor(colorHSV))));
            colorValueGreen.setText(Integer.toString(Color.green(Color.HSVToColor(colorHSV))));
            colorValueBlue.setText(Integer.toString(Color.blue(Color.HSVToColor(colorHSV))));
        }
        else if(seekArc instanceof ColorPickerSaturationArc) {
            colorHSV[0] = hue;
            colorHSV[1] = ((ColorPickerSaturationArc)seekArc).getSaturation();
            saturation = colorHSV[1];

            colorPreview.getBackground().setColorFilter(Color.HSVToColor(colorHSV), PorterDuff.Mode.SRC_ATOP);
            float [] colorValue = colorHSV;
            colorValue[2] = 1f;
            colorValueRed.setText(Integer.toString(Color.red(Color.HSVToColor(colorHSV))));
            colorValueGreen.setText(Integer.toString(Color.green(Color.HSVToColor(colorHSV))));
            colorValueBlue.setText(Integer.toString(Color.blue(Color.HSVToColor(colorHSV))));
        }
    }

    private boolean isHaloPicker() {
        return getArguments().getBoolean(HALO_PICKER, false);
    }
}

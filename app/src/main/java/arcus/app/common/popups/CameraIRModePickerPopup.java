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

import android.os.Bundle;
import androidx.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.iris.client.capability.Camera;
import arcus.app.R;
import arcus.app.device.settings.enumeration.CameraIRMode;

import java.util.ArrayList;
import java.util.List;


public class CameraIRModePickerPopup extends HeaderContentPopup {

    private final static String CURRENT_MODE = "CURRENT_MODE";
    private final static String SUPPORTED_MODES = "SUPPORTED_MODES";

    private OnCloseCallback callback;
    private String currentMode;

    private LinearLayout offModeClickRegion, onModeClickRegion, autoModeClickRegion;
    private ImageView offModeCheckbox, onModeCheckbox, autoModeCheckbox;

    public interface OnCloseCallback {
        void onClosed (CameraIRMode selectedMode);
    }

    public static CameraIRModePickerPopup newInstance(String currentMode, ArrayList<String> supportedModes) {
        CameraIRModePickerPopup instance = new CameraIRModePickerPopup();
        Bundle arguments = new Bundle();
        arguments.putString(CURRENT_MODE, currentMode);
        arguments.putStringArrayList(SUPPORTED_MODES, supportedModes);
        instance.setArguments(arguments);
        return instance;
    }

    @Override
    public void doClose() {
        if (callback != null) {
            callback.onClosed(CameraIRMode.fromCapabilityModeString(getCurrentMode()));
        }
    }

    @Override
    public void setupHeaderSection(View view) {
        // Nothing to do
    }

    @Override
    public void setupDividerSection(View view) {
        // Nothing to do
    }

    @Override
    public void setupSubContentSection(View view) {

        offModeClickRegion = (LinearLayout) view.findViewById(R.id.off_mode);
        offModeCheckbox = (ImageView) view.findViewById(R.id.off_mode_checkbox);
        onModeClickRegion = (LinearLayout) view.findViewById(R.id.on_mode);
        onModeCheckbox = (ImageView) view.findViewById(R.id.on_mode_checkbox);
        autoModeClickRegion = (LinearLayout) view.findViewById(R.id.auto_mode);
        autoModeCheckbox = (ImageView) view.findViewById(R.id.auto_mode_checkbox);

        offModeClickRegion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setCurrentMode(Camera.IRLEDMODE_OFF);
            }
        });

        onModeClickRegion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setCurrentMode(Camera.IRLEDMODE_ON);
            }
        });

        autoModeClickRegion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setCurrentMode(Camera.IRLEDMODE_AUTO);
            }
        });

        // Show selection buttons only for supported modes
        offModeClickRegion.setVisibility(getSupportedModes().contains(Camera.IRLEDMODE_OFF) ? View.VISIBLE : View.GONE);
        onModeClickRegion.setVisibility(getSupportedModes().contains(Camera.IRLEDMODE_ON) ? View.VISIBLE : View.GONE);
        autoModeClickRegion.setVisibility(getSupportedModes().contains(Camera.IRLEDMODE_AUTO) ? View.VISIBLE : View.GONE);

        setCurrentMode(getCurrentMode());
    }

    @Nullable
    @Override
    public Integer headerSectionLayout() {
        return R.layout.floating_camera_led_header;
    }

    @Nullable
    @Override
    public Integer subContentSectionLayout() {
        return R.layout.floating_camera_led_picker;
    }

    @Nullable
    @Override
    public String getTitle() {
        return getString(R.string.setting_camera_led_choose_setting_title);
    }

    public void setOnCloseCallback (OnCloseCallback onCloseCallback) {
        this.callback = onCloseCallback;
    }

    private String getCurrentMode() {
        return currentMode == null ? getArguments().getString(CURRENT_MODE, Camera.IRLEDMODE_AUTO) : currentMode;
    }

    private List<String> getSupportedModes() {
        return getArguments().getStringArrayList(SUPPORTED_MODES);
    }

    private void setCurrentMode(String currentMode) {
        this.currentMode = currentMode;

        switch (currentMode) {
            case Camera.IRLEDMODE_AUTO:
                autoModeCheckbox.setImageResource(R.drawable.icon_check);
                onModeCheckbox.setImageResource(R.drawable.icon_uncheck);
                offModeCheckbox.setImageResource(R.drawable.icon_uncheck);
                break;
            case Camera.IRLEDMODE_ON:
                autoModeCheckbox.setImageResource(R.drawable.icon_uncheck);
                onModeCheckbox.setImageResource(R.drawable.icon_check);
                offModeCheckbox.setImageResource(R.drawable.icon_uncheck);
                break;
            case Camera.IRLEDMODE_OFF:
                autoModeCheckbox.setImageResource(R.drawable.icon_uncheck);
                onModeCheckbox.setImageResource(R.drawable.icon_uncheck);
                offModeCheckbox.setImageResource(R.drawable.icon_check);
                break;
        }
    }

}

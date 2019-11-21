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
package arcus.app.device.settings.builder;

import android.app.Activity;
import android.os.Handler;
import androidx.annotation.NonNull;
import android.view.View;

import arcus.cornea.utils.Listeners;
import com.iris.client.capability.Camera;
import com.iris.client.capability.Motion;
import com.iris.client.capability.WiFi;
import com.iris.client.event.Listener;
import com.iris.client.model.DeviceModel;
import arcus.app.R;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.error.ErrorManager;
import arcus.app.common.popups.CameraIRModePickerPopup;
import arcus.app.common.utils.CorneaUtils;
import arcus.app.common.utils.StringUtils;
import arcus.app.device.settings.core.Setting;
import arcus.app.device.settings.core.SettingChangedParcelizedListener;
import arcus.app.device.settings.enumeration.CameraFrameRate;
import arcus.app.device.settings.enumeration.CameraIRMode;
import arcus.app.device.settings.enumeration.WifiSecurityStandard;
import arcus.app.device.settings.fragment.NetworkSettingsFragment;
import arcus.app.device.settings.fragment.CameraLocalStreamingFragment;
import arcus.app.device.settings.fragment.CameraNetworkFragment;
import arcus.app.device.settings.style.AbstractEnumeratedSetting;
import arcus.app.device.settings.style.BinarySetting;
import arcus.app.device.settings.style.EnumSelectionSetting;
import arcus.app.device.settings.style.ListSelectionSetting;
import arcus.app.device.settings.style.OnClickActionSetting;
import arcus.app.device.settings.style.TransitionToFragmentSetting;
import arcus.app.subsystems.alarm.AlertFloatingFragment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;


public class CameraSettingBuilder implements SettingBuilder {

    private static final Logger logger = LoggerFactory.getLogger(CameraSettingBuilder.class);

    private final Activity context;
    private final String title;
    private final String description;
    private Setting setting;

    private CameraSettingBuilder (Activity context, String title, String description) {
        this.context = context;
        this.title = title;
        this.description = description;
    }

    @NonNull
    public static CameraSettingBuilder with (Activity context, String title, String description) {
        return new CameraSettingBuilder(context, title, description);
    }

    @NonNull
    public CameraSettingBuilder buildBleNetworkSetting(@NonNull DeviceModel model) {
        final WiFi wifiCap = CorneaUtils.getCapability(model, WiFi.class);

        if (wifiCap != null) {
            setting = new TransitionToFragmentSetting(title, description, NetworkSettingsFragment.newInstance(model.getAddress()));
        }

        return this;
    }

    @NonNull
    public CameraSettingBuilder buildNetworkSetting(@NonNull DeviceModel model) {
        final WiFi wifiCap = CorneaUtils.getCapability(model, WiFi.class);

        if (wifiCap != null) {
            setting = new TransitionToFragmentSetting(title, description, CameraNetworkFragment.newInstance(model.getAddress()));
        }

        return this;
    }

    @NonNull
    public CameraSettingBuilder buildWifiSecuritySetting (@NonNull DeviceModel model) {
        final WiFi wifiCap = CorneaUtils.getCapability(model, WiFi.class);

        if (wifiCap != null) {
            logger.debug("Building wifi security setting for camera {}.", model.getAddress());

            WifiSecurityStandard currentSetting = WifiSecurityStandard.fromSecurityString(wifiCap.getSecurity());

            setting = new EnumSelectionSetting<>(context, title, description, StringUtils.getAbstract(context, currentSetting), WifiSecurityStandard.class, currentSetting);
            setting.addListener(new SettingChangedParcelizedListener() {
                @Override
                public void onSettingChanged(Setting setting, Object newValue) {
                    setting.setSelectionAbstract(StringUtils.getAbstract(context, newValue));
                }
            });
        }

        return this;
    }

    @NonNull
    public CameraSettingBuilder buildCameraResolutionSetting (@NonNull final DeviceModel model) {
        final Camera cameraCap = CorneaUtils.getCapability(model, Camera.class);

        if (cameraCap != null) {
            List<String> resolutionsSupported;
            if (cameraCap.getResolutionssupported() == null) {
                resolutionsSupported = Collections.emptyList();
            } else {
                resolutionsSupported = cameraCap.getResolutionssupported();
            }

            String currentRes;
            if (cameraCap.getResolution() == null) {
                currentRes = "";
            } else {
                currentRes = cameraCap.getResolution();
            }

            setting = new ListSelectionSetting(title, description, resolutionsSupported, currentRes, currentRes);
            setting.addListener(new SettingChangedParcelizedListener() {
                @Override
                public void onSettingChanged(Setting setting, @NonNull Object newValue) {
                    setting.setSelectionAbstract(StringUtils.getAbstract(context, newValue));
                    cameraCap.setResolution(newValue.toString());
                    model.commit().onFailure(Listeners.runOnUiThread(throwable -> ErrorManager.in(context).showGenericBecauseOf(throwable)));
                }
            });
        }

        return this;
    }

    @NonNull
    public CameraSettingBuilder buildMotionSensitivitySetting(@NonNull final DeviceModel model) {
        final Motion motionCap = CorneaUtils.getCapability(model, Motion.class);
        if (motionCap != null) {
            Set<String> sensitivitiesSupportedTmp = motionCap.getSensitivitiesSupported();
            if (sensitivitiesSupportedTmp == null || sensitivitiesSupportedTmp.isEmpty()) {
                return this;
            }

            List<String> sensitivitiesSupported = new ArrayList<>(sensitivitiesSupportedTmp);
            String currentSensitivity = motionCap.getSensitivity();
            if (currentSensitivity == null) {
                currentSensitivity = sensitivitiesSupported.get(0);
            }

            setting = new ListSelectionSetting(title, description, sensitivitiesSupported, currentSensitivity, currentSensitivity);
            setting.addListener(new SettingChangedParcelizedListener() {
                @Override
                public void onSettingChanged(Setting setting, @NonNull Object newValue) {
                    if (String.valueOf(newValue).equalsIgnoreCase("off") && context != null) {
                        // This races the doClose method of the ArcusFloatingFragment so we need to add in an arbitrary delay...
                        // No it's not optimial (hacky at best....) but open to suggestions as to how to do this differently...
                        new Handler().postDelayed(() -> {
                            AlertFloatingFragment fragment = AlertFloatingFragment.newInstance(
                                    context.getString(R.string.device_remove_are_you_sure),
                                    context.getString(R.string.camera_motion_off_warning_message),
                                    context.getString(R.string.yes_turn_off),
                                    context.getString(R.string.no_cancel),
                                    new AlertFloatingFragment.AlertButtonCallback() {
                                        @Override
                                        public boolean topAlertButtonClicked() {
                                            saveSetting(String.valueOf(newValue));
                                            return true;
                                        }

                                        @Override
                                        public boolean bottomAlertButtonClicked() {
                                            if (setting instanceof AbstractEnumeratedSetting) {
                                                ((AbstractEnumeratedSetting) setting).revertSelection();
                                            }
                                            return true;
                                        }
                                    }
                            );

                            BackstackManager.getInstance().navigateToFloatingFragment(fragment, true);
                        }, 750);

                    } else {
                        saveSetting(String.valueOf(newValue));
                    }
                }

                private void saveSetting(String newValue) {
                    setting.setSelectionAbstract(StringUtils.getAbstract(context, newValue));
                    motionCap.setSensitivity(newValue);
                    model.commit().onFailure(Listeners.runOnUiThread(throwable -> ErrorManager.in(context).showGenericBecauseOf(throwable)));
                }
            });
        }

        return this;
    }

    @NonNull
    public CameraSettingBuilder buildImageQualitySetting (@NonNull final DeviceModel model) {
        final Camera cameraCap = CorneaUtils.getCapability(model, Camera.class);

        if (cameraCap != null) {

            setting = new ListSelectionSetting(title, description, cameraCap.getQualitiessupported(), cameraCap.getQuality(), cameraCap.getQuality());
            setting.addListener(new SettingChangedParcelizedListener() {
                @Override
                public void onSettingChanged(Setting setting, @NonNull Object newValue) {
                    setting.setSelectionAbstract(newValue.toString());
                    cameraCap.setQuality(StringUtils.getAbstract(context, newValue));
                    model.commit().onFailure(Listeners.runOnUiThread(new Listener<Throwable>() {
                        @Override
                        public void onEvent(Throwable throwable) {
                            ErrorManager.in(context).showGenericBecauseOf(throwable);
                        }
                    }));
                }
            });
        }

        return this;
    }

    @NonNull
    public CameraSettingBuilder buildFrameRateSetting (@NonNull final DeviceModel model) {
        final Camera cameraCap = CorneaUtils.getCapability(model, Camera.class);

        if (cameraCap != null) {

            CameraFrameRate currentRate = CameraFrameRate.fromRate(cameraCap.getFramerate());

            setting = new EnumSelectionSetting<>(context, title, description, StringUtils.getAbstract(context, currentRate), CameraFrameRate.class, currentRate);
            setting.addListener(new SettingChangedParcelizedListener() {
                @Override
                public void onSettingChanged(Setting setting, @NonNull Object newValue) {
                    setting.setSelectionAbstract(StringUtils.getAbstract(context, newValue));
                    cameraCap.setFramerate(((CameraFrameRate) newValue).getFps());
                    model.commit().onFailure(Listeners.runOnUiThread(throwable -> ErrorManager.in(context).showGenericBecauseOf(throwable)));
                }
            });
        }

        return this;
    }

    @NonNull public CameraSettingBuilder buildRotateCameraSetting(final DeviceModel model) {
        final Camera cameraCap = CorneaUtils.getCapability(model, Camera.class);
        if (cameraCap == null) {
            return this;
        }

        boolean isRotated = Boolean.TRUE.equals(cameraCap.getFlip()) && Boolean.TRUE.equals(cameraCap.getMirror());
        setting = new BinarySetting(title, description, isRotated);
        setting.addListener(new SettingChangedParcelizedListener() {
            @Override public void onSettingChanged(Setting setting, Object newValue) {
                if (model == null) {
                    return;
                }

                boolean isRotated = (boolean) newValue;
                cameraCap.setFlip(isRotated);
                cameraCap.setMirror(isRotated);
                model.commit().onFailure(Listeners.runOnUiThread(new Listener<Throwable>() {
                    @Override
                    public void onEvent(Throwable throwable) {
                        ErrorManager.in(context).showGenericBecauseOf(throwable);
                    }
                }));
            }
        });

        return this;
    }

    @NonNull public CameraSettingBuilder buildLedSetting(final DeviceModel model) {
        final Camera camera = CorneaUtils.getCapability(model, Camera.class);

        // Hide setting if camera doesn't support LED modes.
        if (camera == null || camera.getIrLedSupportedModes() == null || camera.getIrLedSupportedModes().size() < 2) {
            return this;
        }

        String selectionAbstract = CameraIRMode.fromCapabilityModeString(camera.getIrLedMode()).toString(context);
        setting = new OnClickActionSetting(title, description, selectionAbstract);
        ((OnClickActionSetting)setting).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CameraIRModePickerPopup picker = CameraIRModePickerPopup.newInstance(camera.getIrLedMode(), new ArrayList<>(camera.getIrLedSupportedModes()));
                picker.setOnCloseCallback(new CameraIRModePickerPopup.OnCloseCallback() {
                    @Override
                    public void onClosed(CameraIRMode selectedMode) {
                        setting.setSelectionAbstract(selectedMode.toString(context));
                        camera.setIrLedMode(selectedMode.getCapabilityModeString());
                        model.commit().onFailure(Listeners.runOnUiThread(new Listener<Throwable>() {
                            @Override
                            public void onEvent(Throwable throwable) {
                                ErrorManager.in(context).showGenericBecauseOf(throwable);
                            }
                        }));
                    }
                });

                BackstackManager.getInstance().navigateToFloatingFragment(picker, picker.getClass().getCanonicalName(), true);
            }
        });

        return this;
    }

    @NonNull public CameraSettingBuilder buildLocalUsernamePassword(final DeviceModel deviceModel) {
        final Camera cameraCap = CorneaUtils.getCapability(deviceModel, Camera.class);
        if (cameraCap != null) {
            setting = new TransitionToFragmentSetting(title, description, CameraLocalStreamingFragment.newInstance(deviceModel.getId()));
        }

        return this;
    }

    @Override
    public Setting build() {
        return setting;
    }
}

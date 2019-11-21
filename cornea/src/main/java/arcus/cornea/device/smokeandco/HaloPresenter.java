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
package arcus.cornea.device.smokeandco;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import com.google.common.collect.ImmutableSet;
import arcus.cornea.CorneaClientFactory;
import arcus.cornea.provider.DeviceModelProvider;
import arcus.cornea.utils.Listeners;
import com.iris.client.capability.Atmos;
import com.iris.client.capability.Color;
import com.iris.client.capability.Device;
import com.iris.client.capability.DeviceAdvanced;
import com.iris.client.capability.DeviceConnection;
import com.iris.client.capability.DeviceOta;
import com.iris.client.capability.DevicePower;
import com.iris.client.capability.Dimmer;
import com.iris.client.capability.Halo;
import com.iris.client.capability.RelativeHumidity;
import com.iris.client.capability.Switch;
import com.iris.client.capability.Temperature;
import com.iris.client.capability.WeatherRadio;
import com.iris.client.event.ListenerRegistration;
import com.iris.client.model.DeviceModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Set;

public class HaloPresenter extends BaseHaloPresenter implements HaloContract.Presenter {
    private static final Logger logger = LoggerFactory.getLogger(HaloPresenter.class);

    public static final Set<String> UPDATE_ON = ImmutableSet.of(
          WeatherRadio.ATTR_PLAYINGSTATE,
          WeatherRadio.ATTR_ALERTSTATE, // Radio Alerting? or this from the device?

          Color.ATTR_HUE, // Changed "Glow" color
          Color.ATTR_SATURATION, // Changed "Glow" color

          Device.ATTR_NAME, // User Updated Name

          DevicePower.ATTR_SOURCE, // On/Off Battery

          Dimmer.ATTR_BRIGHTNESS, // Brightness up/down
          Switch.ATTR_STATE, // Light on/off

          RelativeHumidity.ATTR_HUMIDITY, // Humidity

          Atmos.ATTR_PRESSURE, // Atmospheric Pressure

          Temperature.ATTR_TEMPERATURE, // Temp

          DeviceConnection.ATTR_STATE,

          Halo.ATTR_DEVICESTATE,

          DeviceAdvanced.ATTR_ERRORS
    );

    private final HaloController haloController;
    private ListenerRegistration controllerReg;
    private Reference<HaloContract.View> viewRef = new WeakReference<>(null);

    public HaloPresenter(String deviceAddress) {
        this(deviceAddress, UPDATE_ON);
    }

    @VisibleForTesting HaloPresenter(HaloController haloController) {
        this.haloController = haloController;
    }

    public HaloPresenter(String deviceAddress, Set<String> updateOnThese) {
        haloController = new HaloController(
              DeviceModelProvider.instance().getModel(deviceAddress == null ? "DRIV:dev:" : deviceAddress),
              CorneaClientFactory.getClient(),
              updateOnThese
        );
    }

    @Override public void startPresenting(HaloContract.View view) {
        viewRef = new WeakReference<>(view);
        controllerReg = haloController.setCallback(getHaloCallback());
    }

    @Override public void stopPresenting() {
        Listeners.clear(controllerReg);
        if (viewRef != null) {
            viewRef.clear();
        }
    }

    @Override public void playCurrentStation() {
        playWeatherStation(null);
    }

    @Override public void stopPlayingRadio() {
        haloController.stopPlayingRadio();
    }

    @Override public void playWeatherStation(@Nullable Integer station) {
        haloController.playWeatherStation(station, -2);
    }

    @Override public void setSwitchOn(boolean isOn) {
        haloController.setSwitchOn(isOn);
    }

    @Override public void setDimmer(int dimmerPercent) {
        if (dimmerPercent < 1) {
            dimmerPercent = 1;
        }
        else if (dimmerPercent > 100) {
            dimmerPercent = 100;
        }

        haloController.setDimmer(dimmerPercent);
    }

    @Override public void requestRefreshAndClearChanges(boolean clearAnyPendingChanges) {
        if (clearAnyPendingChanges) {
            logger.warn("Clearing any changes to model and refreshing. [{}]", haloController.clearChanges());
        }

        haloController.refreshModel();
    }

    @Override protected HaloContract.View getView() {
        return viewRef.get();
    }

    @Override @SuppressWarnings("ConstantConditions") protected @NonNull HaloModel buildModel(@NonNull DeviceModel halo) {
        if (halo == null) {
            return HaloModel.empty();
        }
        
        HaloModel haloModel = new HaloModel(halo.getAddress());
        haloModel.setName((String) halo.get(Device.ATTR_NAME));
        haloModel.setHaloPlus(nonNullCollection(halo.getCaps()).contains(WeatherRadio.NAMESPACE));

        haloModel.setTemperature(tempString(halo.get(Temperature.ATTR_TEMPERATURE)));
        haloModel.setAtmosphericPressure(stringWithMultiplier(halo.get(Atmos.ATTR_PRESSURE), true, HaloContract.ATMOS_MULTIPLIER));
        haloModel.setHumidity(stringWithMultiplier(halo.get(RelativeHumidity.ATTR_HUMIDITY), false, 1));

        haloModel.setHue(numberOrNull(halo.get(Color.ATTR_HUE)));
        haloModel.setSaturation(numberOrNull(halo.get(Color.ATTR_SATURATION)));

        haloModel.setOnBattery(DevicePower.SOURCE_BATTERY.equals(halo.get(DevicePower.ATTR_SOURCE)));
        haloModel.setBatteryLevel(numberOrNull(halo.get(DevicePower.ATTR_BATTERY)));
        haloModel.setLightOn(Switch.STATE_ON.equals(halo.get(Switch.ATTR_STATE)));
        haloModel.setFirmwareUpdating(DeviceOta.STATUS_INPROGRESS.equals(halo.get(DeviceOta.ATTR_STATUS)));

        haloModel.setDimmerPercent(numberOrNull(halo.get(Dimmer.ATTR_BRIGHTNESS)));

        haloModel.setPreSmoke(Halo.DEVICESTATE_PRE_SMOKE.equals(halo.get(Halo.ATTR_DEVICESTATE)));

        haloModel.setOnline(!DeviceConnection.STATE_OFFLINE.equals(halo.get(DeviceConnection.ATTR_STATE)));

        haloModel.setErrors(mapOrNull(halo.get(DeviceAdvanced.ATTR_ERRORS)));

        boolean radioIsPlaying = WeatherRadio.PLAYINGSTATE_PLAYING.equals(halo.get(WeatherRadio.ATTR_PLAYINGSTATE));
        haloModel.setRadioPlaying(radioIsPlaying);

        return haloModel;
    }

}

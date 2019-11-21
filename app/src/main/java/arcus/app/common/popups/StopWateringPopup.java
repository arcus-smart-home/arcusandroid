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
import androidx.annotation.NonNull;
import android.view.View;

import arcus.app.R;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.view.Version1Button;
import arcus.app.device.details.IrrigationStopEvent;

import java.lang.ref.WeakReference;

import de.greenrobot.event.EventBus;

public class StopWateringPopup extends ArcusFloatingFragment {
    private static final String DEVICE_ID = "DEVICE_ID";
    private static final String TITLE = "TITLE";
    private EventBus bus;

    private WeakReference<Callback> callbackRef = new WeakReference<>(null);
    public interface Callback {
        void onIrrigationStopEvent(IrrigationStopEvent event);
    }

    @NonNull
    public static StopWateringPopup newInstance(String deviceId, String title) {
        StopWateringPopup fragment = new StopWateringPopup();

        Bundle data = new Bundle(2);
        data.putString(DEVICE_ID, deviceId);
        data.putString(TITLE, title);
        fragment.setArguments(data);

        return fragment;
    }

    public void setCallback(Callback callback) {
        callbackRef = new WeakReference<>(callback);
    }

    @Override
    public void setFloatingTitle() {
        title.setText(getArguments().getString(TITLE, ""));
    }

    @Override
    public void doContentSection() {
        Version1Button allZones = (Version1Button) contentView.findViewById(R.id.all_zones);
        allZones.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendEvent("ALLZONES");
            }
        });
        Version1Button currentZoneOnly = (Version1Button) contentView.findViewById(R.id.current_zone_only);
        currentZoneOnly.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendEvent("CURRENTZONE");
            }
        });
    }

    private void sendEvent(String event) {
        String deviceId = getArguments().getString(DEVICE_ID, "");
        bus = EventBus.getDefault();
        bus.postSticky(new IrrigationStopEvent(deviceId, event));

        Callback callback = callbackRef.get();
        if (callback != null) {
            callback.onIrrigationStopEvent(new IrrigationStopEvent(deviceId, event));
        }
        BackstackManager.getInstance().navigateBack();
    }
    @Override
    public Integer contentSectionLayout() {
        return R.layout.fragment_stop_watering_popup;
    }

    @NonNull
    @Override
    public String getTitle() {
        return "";
    }
}

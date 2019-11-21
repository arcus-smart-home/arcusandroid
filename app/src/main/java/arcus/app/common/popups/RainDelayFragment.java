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
import android.text.TextUtils;
import android.view.View;

import arcus.app.R;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.view.Version1Button;
import arcus.app.device.details.IrrigationDelayEvent;

import java.lang.ref.WeakReference;

public class RainDelayFragment extends ArcusFloatingFragment {
    private static final String MODEL_ADDRESS = "MODEL_ADDRESS";
    private static final String TITLE = "TITLE";
    private static final int HALF_DAY_MINUTES = 720;
    private static final int ONE_DAY_MINUTES = HALF_DAY_MINUTES * 2;
    private static final int TWO_DAYS_MINUTES = ONE_DAY_MINUTES * 2;
    private static final int THREE_DAYS_MINUTES = ONE_DAY_MINUTES * 3;

    private WeakReference<Callback> callbackRef = new WeakReference<>(null);
    public interface Callback {
        void onIrrigationDelayEvent(IrrigationDelayEvent event);
    }

    @NonNull
    public static RainDelayFragment newInstance(String modelAddress, String title) {
        RainDelayFragment fragment = new RainDelayFragment();

        Bundle data = new Bundle(2);
        data.putString(MODEL_ADDRESS, modelAddress);
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
        Version1Button tv12 = (Version1Button) contentView.findViewById(R.id.irrigation_water_delay_12);
        tv12.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleIrrigationClose(HALF_DAY_MINUTES);
            }
        });
        Version1Button tv24 = (Version1Button) contentView.findViewById(R.id.irrigation_water_delay_24);
        tv24.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleIrrigationClose(ONE_DAY_MINUTES);
            }
        });
        Version1Button tv48 = (Version1Button) contentView.findViewById(R.id.irrigation_water_delay_48);
        tv48.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleIrrigationClose(TWO_DAYS_MINUTES);
            }
        });
        Version1Button tv72 = (Version1Button) contentView.findViewById(R.id.irrigation_water_delay_72);
        tv72.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleIrrigationClose(THREE_DAYS_MINUTES);
            }
        });
    }

    @Override
    public Integer contentSectionLayout() {
        return R.layout.floating_rain_delay;
    }

    @NonNull
    @Override
    public String getTitle() {
        return "";
    }

    private void handleIrrigationClose(int minutes) {
        String deviceAddress = getArguments().getString(MODEL_ADDRESS, "");
        if(TextUtils.isEmpty(deviceAddress)) {
            return;
        }

        Callback callback = callbackRef.get();
        if (callback != null) {
            callback.onIrrigationDelayEvent(new IrrigationDelayEvent(deviceAddress, minutes));
        }
        BackstackManager.getInstance().navigateBack();
    }
}

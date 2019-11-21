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
package arcus.app.subsystems.weather;

import android.app.Activity;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import arcus.cornea.SessionController;
import arcus.cornea.subsystem.weather.WeatherSubsystemController;
import arcus.cornea.subsystem.weather.model.WeatherSubsystemModel;
import arcus.cornea.utils.Listeners;
import com.iris.client.event.ListenerRegistration;
import com.iris.client.model.PlaceModel;
import arcus.app.R;
import arcus.app.activities.DashboardActivity;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.error.ErrorManager;
import arcus.app.common.fragments.BaseFragment;
import arcus.app.common.utils.PreferenceUtils;
import arcus.app.subsystems.alarm.safety.HaloAlertSnoozed;

import java.util.Map;
import java.util.Set;

public class WeatherWarningFragment extends BaseFragment implements WeatherSubsystemController.Callback, View.OnClickListener {
    private LinearLayout devicesView;
    private TextView placeName;
    private TextView placeAddress;
    private ListenerRegistration listenerRegistration;

    public static WeatherWarningFragment newInstance() {
        return new WeatherWarningFragment();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        devicesView = (LinearLayout) view.findViewById(R.id.device_name_layout);
        placeName = (TextView) view.findViewById(R.id.place_name_view);
        placeAddress = (TextView) view.findViewById(R.id.place_address_view);

        Button continueButton = (Button) view.findViewById(R.id.continue_playing_button);
        if (continueButton != null) {
            continueButton.setOnClickListener(this);
        }

        Button snoozeAlertButton = (Button) view.findViewById(R.id.snooze_alert_button);
        if (snoozeAlertButton != null) {
            snoozeAlertButton.setOnClickListener(this);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.continue_playing_button:
                BackstackManager.getInstance().navigateBack();
                break;

            case R.id.snooze_alert_button:
                WeatherSubsystemController.instance().snoozeAll();
                break;

            default:
                // No-OP
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Listeners.clear(listenerRegistration);
        devicesView.removeAllViews();

        listenerRegistration = WeatherSubsystemController.instance().setCallback(this);

        Activity activity = getActivity();
        if (activity != null && (activity instanceof DashboardActivity)) {
            ((DashboardActivity) activity).setToolbarWeatherWarningColor();
        }

        PlaceModel place = SessionController.instance().getPlace();
        if (place != null) {
            placeName.setText(place.getName());
            placeAddress.setText(place.getStreetAddress1());
        }
        setTitle();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Listeners.clear(listenerRegistration);

        Activity activity = getActivity();
        if (activity != null && (activity instanceof DashboardActivity)) {
            ((DashboardActivity) activity).setToPreviousToolbarColor();
        }
    }

    @Nullable
    @Override
    public String getTitle() {
        return getString(R.string.weather_alert_title);
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.weather_alert_fragment;
    }

    @Override
    public void snoozeSuccessful() {
        BackstackManager.getInstance().navigateBack();
        if(PreferenceUtils.getShowWeatherRadioSnooze()) {
            HaloAlertSnoozed popup = new HaloAlertSnoozed();
            BackstackManager.getInstance().navigateToFloatingFragment(popup, popup.getClass().getCanonicalName(), true);
        }
    }

    @Override
    public void onError(Throwable throwable) {
        ErrorManager.in(getActivity()).showGenericBecauseOf(throwable);
    }

    @Override
    public void onPending(Integer progressPercentage) {
        // Nothing to do
    }

    @Override
    public void updateView(WeatherSubsystemModel model) {
        if(model.alertingDevices.size() == 0) {
            getActivity().getSupportFragmentManager().beginTransaction().remove(this).commit();
        }
        devicesView.removeAllViews();

        LayoutInflater inflater = LayoutInflater.from(devicesView.getContext());
        for (Map.Entry<String, Set<String>> devices : model.alertingDevices.entrySet()) {
            View eventDetailsContainer = inflater.inflate(R.layout.weather_alert_type_title, devicesView, false);
            TextView eventDetails = (TextView) eventDetailsContainer.findViewById(R.id.weather_event_details);
            eventDetails.setText(getString(R.string.weather_alert_issued, devices.getKey(), model.lastAlertTime));
            devicesView.addView(eventDetailsContainer);

            for (String deviceName : devices.getValue()) {
                TextView textView = (TextView) inflater.inflate(R.layout.smoke_detctor_with_name, devicesView, false);
                textView.setText(deviceName);

                devicesView.addView(textView);
            }
        }
    }
}

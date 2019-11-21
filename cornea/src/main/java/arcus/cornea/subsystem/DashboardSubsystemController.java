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
package arcus.cornea.subsystem;

import android.os.Handler;
import android.os.Looper;
import androidx.annotation.Nullable;
import android.util.Log;

import arcus.cornea.subsystem.model.DashboardState;
import arcus.cornea.utils.Listeners;
import arcus.cornea.utils.ModelSource;
import com.iris.client.capability.AlarmSubsystem;
import com.iris.client.capability.CareSubsystem;
import com.iris.client.capability.SafetySubsystem;
import com.iris.client.capability.SecuritySubsystem;
import com.iris.client.capability.Subsystem;
import com.iris.client.capability.WeatherSubsystem;
import com.iris.client.event.Listener;
import com.iris.client.event.ListenerRegistration;
import com.iris.client.model.ModelAddedEvent;
import com.iris.client.model.ModelChangedEvent;
import com.iris.client.model.ModelEvent;
import com.iris.client.model.SubsystemModel;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;


public class DashboardSubsystemController {
    private static final int THROTTLE_MS = 500;
    private static final String TAG = "DashSubsController";

    private static final DashboardSubsystemController INSTANCE = new DashboardSubsystemController(SubsystemController.instance());

    public static DashboardSubsystemController instance() {
        return INSTANCE;
    }

    private WeakReference<Callback> callbackRef = new WeakReference<Callback>(null);

    private Handler handler = new Handler(Looper.getMainLooper());
    private DashboardState state = null;

    private ModelSource<SubsystemModel> security;
    private ModelSource<SubsystemModel> care;
    private ModelSource<SubsystemModel> weather;
    private ModelSource<SubsystemModel> alarm;
    private ModelSource<SubsystemModel> safety;

    DashboardSubsystemController(SubsystemController subsystems) {

        Listener<ModelEvent> modelListener = new Listener<ModelEvent>() {
            @Override
            public void onEvent(ModelEvent event) {
                if (event instanceof ModelAddedEvent) {
                    updateView();
                } else if (event instanceof ModelChangedEvent) {
                    onModelChanged(((ModelChangedEvent) event).getChangedAttributes().keySet());
                }
            }
        };

        this.alarm = subsystems.getSubsystemModel(AlarmSubsystem.NAMESPACE);
        this.alarm.addModelListener(modelListener);

        this.care = subsystems.getSubsystemModel(CareSubsystem.NAMESPACE);
        this.care.addModelListener(modelListener);

        this.weather = subsystems.getSubsystemModel(WeatherSubsystem.NAMESPACE);
        this.weather.addModelListener(modelListener);

        this.safety = subsystems.getSubsystemModel(SafetySubsystem.NAMESPACE);
        this.safety.addModelListener(modelListener);

        this.security = subsystems.getSubsystemModel(SecuritySubsystem.NAMESPACE);
        this.security.addModelListener(modelListener);
    }

    public ListenerRegistration setCallback(Callback callback) {
        this.callbackRef = new WeakReference<>(callback);
        updateView();
        return Listeners.wrap(this.callbackRef);
    }

    protected void onModelChanged(Set<String> keys) {
        if (hasRelevantChanges(keys)) {
            updateView();
        }
    }

    protected boolean hasRelevantChanges(Set<String> keys) {
        return  keys.contains(SafetySubsystem.ATTR_SMOKEPREALERT) ||
                keys.contains(SafetySubsystem.ATTR_SMOKEPREALERTDEVICES) ||
                keys.contains(SafetySubsystem.ATTR_ALARM) ||
                keys.contains(SafetySubsystem.ATTR_LASTSMOKEPREALERTTIME) ||
                keys.contains(SecuritySubsystem.ATTR_ALARMSTATE) ||
                keys.contains(CareSubsystem.ATTR_ALARMSTATE) ||
                keys.contains(AlarmSubsystem.ATTR_CURRENTINCIDENT) ||
                keys.contains(AlarmSubsystem.ATTR_ALARMSTATE) ||
                keys.contains(SafetySubsystem.ATTR_SMOKEPREALERT) ||
                weatherAlertChange(keys);
    }

    protected boolean weatherAlertChange(Set<String> keys) {
        return keys.contains(WeatherSubsystem.ATTR_WEATHERALERT) ||
                keys.contains(WeatherSubsystem.ATTR_LASTWEATHERALERTTIME) ||
                keys.contains(WeatherSubsystem.ATTR_ALERTINGRADIOS);
    }

   protected void updateView() {
        Callback callback = this.callbackRef.get();
        if (callback == null) {
            return;
        }

        SubsystemModel care = this.care.get();
        SubsystemModel weather = this.weather.get();
        SubsystemModel alarm = this.alarm.get();
        SubsystemModel safety = this.safety.get();

        state = getDashboardState(care, weather, alarm, safety);

        SubsystemModel security = this.security.get();

        if(alarm != null && (Subsystem.STATE_SUSPENDED.equals(alarm.getState())||"UNAVAILABLE".equals(alarm.getState()))) {
            if(safety != null && SafetySubsystem.ALARM_ALERT.equals(safety.get(SafetySubsystem.ATTR_ALARM))) {
                state.setSafetyAlarmActivated(true);
                postAlerting();
            }
            if(security != null && SecuritySubsystem.ALARMSTATE_ALERT.equals(security.get(SecuritySubsystem.ATTR_ALARMSTATE))) {
                state.setSecurityAlarmActivated(true);
                postAlerting();
            }
            postDeprecatedAlarmState(state);
        } else {
            postAlarmState(state);
        }

        if (state.isAlerting()) {
            postAlerting();
        }
    }

    protected DashboardState getDashboardState(SubsystemModel care, SubsystemModel weather, SubsystemModel alarm, SubsystemModel safety) {
        DashboardState state = new DashboardState();

        state.setCareAlarmActivated(care != null && CareSubsystem.ALARMSTATE_ALERT.equals(care.get(CareSubsystem.ATTR_ALARMSTATE)));

        state.setWeatherAlertActivated(weather != null && WeatherSubsystem.WEATHERALERT_ALERT.equals(weather.get(WeatherSubsystem.ATTR_WEATHERALERT)));

        state.setAlarmPreAlertActivated(alarm != null && AlarmSubsystem.ALARMSTATE_PREALERT.equals(alarm.get(AlarmSubsystem.ATTR_ALARMSTATE)));
        state.setAlarmAlertActivated(alarm != null && AlarmSubsystem.ALARMSTATE_ALERTING.equals(alarm.get(AlarmSubsystem.ATTR_ALARMSTATE)));
        state.setAlarmIncidentAddress(alarm == null ? null : String.valueOf(alarm.get(AlarmSubsystem.ATTR_CURRENTINCIDENT)));
        List<String> activeAlerts = alarm == null ? new ArrayList<String>() : ((AlarmSubsystem) alarm).getActiveAlerts();
        state.setPrimaryActiveAlarm(activeAlerts.size() == 0 ? null : activeAlerts.get(0));

        state.setPresmokeAlertActivated(safety != null && SafetySubsystem.SMOKEPREALERT_ALERT.equals(safety.get(SafetySubsystem.ATTR_SMOKEPREALERT)));

        return state;
    }

    protected void postAlerting() {
        Callback callback = callbackRef.get();
        if (callback != null) {
            callback.showAlerting();
        }
    }

    protected void postDeprecatedAlarmState(final DashboardState state) {
        handler.removeCallbacksAndMessages(null);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Callback callback = callbackRef.get();
                if (callback == null) {
                    return;
                }

                try {
                    callback.showDeprecatedState(state);
                } catch (Exception ex) {
                    Log.e(TAG, "run: Could not dispatch alarm state.", ex);
                }
            }
        }, THROTTLE_MS);
    }

    /**
     * If we're switching places from A -> B via the side navigation drawer and an alarm (or multiple alarms) are triggered
     * in place B, the app was switching before the new subsystem models were fully loaded in;  This throttles the notification
     * of the alarm to the subscribers to give more time to load;
     * <p>
     * We may want to look at checking each respective alarm that's going off and checkign to see if the subsystem is loaded
     * and if it's not calling a load/reload on it since this isn't the most hermetic (infallible) way of doing this.
     *
     * @param state the alarm state to post to the subscriber.
     */
    protected void postAlarmState(final DashboardState state) {
        handler.removeCallbacksAndMessages(null);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Callback callback = callbackRef.get();
                if (callback == null) {
                    return;
                }

                try {
                    callback.showState(state);
                } catch (Exception ex) {
                    Log.e(TAG, "run: Could not dispatch alarm state.", ex);
                }
            }
        }, THROTTLE_MS);
    }

    @Nullable public DashboardState getDashboardState() {
        return state;
    }

    public interface Callback {
        void showDeprecatedState(DashboardState state);
        void showState(DashboardState state);
        void showAlerting();
    }
}

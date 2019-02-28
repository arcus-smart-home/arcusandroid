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
package arcus.cornea.subsystem.alarm;

import android.os.Handler;
import android.os.Looper;

import arcus.cornea.provider.AlarmIncidentProvider;
import arcus.cornea.subsystem.BaseSubsystemController;
import com.iris.client.capability.AlarmSubsystem;
import com.iris.client.event.Listener;
import com.iris.client.model.AlarmIncidentModel;

import org.apache.commons.lang3.StringUtils;

import java.util.Date;



public class AlarmIncidentPrealertController extends BaseSubsystemController<AlarmIncidentPrealertController.Callback> {

    private final static AlarmIncidentPrealertController instance = new AlarmIncidentPrealertController();

    public interface Callback {
        void onPrealertTimeChanged(int secondsRemaining);
    }

    private AlarmIncidentPrealertController () {
        super(AlarmSubsystem.NAMESPACE);
        init();
    }

    public static AlarmIncidentPrealertController getInstance() {
        return instance;
    }

    public void startPrealertCountdown() {
        AlarmSubsystem model = (AlarmSubsystem) getModel();

        if (model == null) {
            return;
        }

        if (!StringUtils.isEmpty(model.getCurrentIncident())) {
            AlarmIncidentProvider.getInstance().getIncident(model.getCurrentIncident()).onSuccess(new Listener<AlarmIncidentModel>() {
                @Override
                public void onEvent(AlarmIncidentModel incidentModel) {
                    int remainingSeconds = getPrealertRemainingSeconds(incidentModel);

                    if (getCallback() != null) {
                        getCallback().onPrealertTimeChanged(remainingSeconds);
                    }

                    if (remainingSeconds > 0) {
                        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                startPrealertCountdown();
                            }
                        }, 1000);
                    }

                }
            });
        }
    }

    public static int getPrealertRemainingSeconds(AlarmIncidentModel alarmIncidentModel) {
        Date prealertEndTime = alarmIncidentModel.getPrealertEndtime();

        if (prealertEndTime == null || prealertEndTime.before(new Date())) {
            return 0;
        } else {
            return (int)((prealertEndTime.getTime() - new Date().getTime()) / 1000);
        }
    }
}

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

import arcus.cornea.subsystem.BaseSubsystemController;
import com.iris.client.capability.AlarmSubsystem;
import com.iris.client.model.ModelChangedEvent;

import java.util.Date;
import java.util.Set;



public class AlarmExitController extends BaseSubsystemController<AlarmExitController.Callback> {

    private final static AlarmExitController instance = new AlarmExitController();

    private boolean cancel = false;

    public interface Callback {
        void onExitTimeChanged(int secondsRemaining);
    }

    private AlarmExitController() {
        super(AlarmSubsystem.NAMESPACE);
        init();
    }

    public static AlarmExitController getInstance() {
        return instance;
    }

    @Override
    protected void onSubsystemChanged(ModelChangedEvent event) {

        if (getCallback() == null) {
            return;     // Nothing to do
        }

        AlarmSubsystem model = (AlarmSubsystem) getModel();
        Set<String> changes = event.getChangedAttributes().keySet();

        if (changes.contains(AlarmSubsystem.ATTR_SECURITYARMTIME)) {
            startPrealertCountdown();
        }
    }

    public void cancelPrealertCountdown() {
        this.cancel = true;
    }

    public void startPrealertCountdown() {
        this.cancel = false;
        countdown();
    }

    private void countdown() {
        AlarmSubsystem alarmModel = (AlarmSubsystem) getModel();
        if(cancel || alarmModel == null) {
            return;
        }
        Date armTime = alarmModel.getSecurityArmTime();
        int remainingSeconds = getPrealertRemainingSeconds(armTime);
        if (remainingSeconds >= 0) {
            if (getCallback() != null) {
                getCallback().onExitTimeChanged(remainingSeconds);
            }

            if (remainingSeconds > 0) {
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        countdown();
                    }
                }, 1000);
            }
        }
    }

    public static int getPrealertRemainingSeconds(Date armTime) {
        Date prealertEndTime = armTime;

        if (prealertEndTime == null || prealertEndTime.before(new Date())) {
            return 0;
        } else {
            return (int)((prealertEndTime.getTime() - new Date().getTime()) / 1000);
        }
    }
}

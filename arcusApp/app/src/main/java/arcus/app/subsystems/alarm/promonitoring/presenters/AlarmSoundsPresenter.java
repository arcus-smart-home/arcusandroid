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
package arcus.app.subsystems.alarm.promonitoring.presenters;

import android.support.annotation.Nullable;

import arcus.cornea.subsystem.alarm.AlarmSoundsController;
import com.iris.client.capability.AlarmSubsystem;

import java.util.Set;

import static arcus.app.subsystems.alarm.promonitoring.presenters.AlarmSoundsContract.*;



public class AlarmSoundsPresenter extends AlarmProviderOfflinePresenter<AlarmSoundsContract.AlarmSoundsView> implements AlarmSoundsContract.AlarmSoundsPresenter, AlarmSoundsController.Callback {


    @Override
    public void requestUpdate() {
        addListener(AlarmSoundsController.class.getCanonicalName(), AlarmSoundsController.getInstance().setCallback(this));

        AlarmSoundsController.getInstance().updateView();
    }

    @Override
    public void setSecurityPanicSilent(boolean isSilent) {
        addListener(AlarmSoundsController.class.getCanonicalName(), AlarmSoundsController.getInstance().setCallback(this));

        AlarmSoundsController.getInstance().setSilentAlarm(AlarmSubsystem.ACTIVEALERTS_SECURITY, isSilent);
        AlarmSoundsController.getInstance().setSilentAlarm(AlarmSubsystem.ACTIVEALERTS_PANIC, isSilent);
    }

    @Override
    public void setSmokeCoSilent(boolean isSilent) {
        addListener(AlarmSoundsController.class.getCanonicalName(), AlarmSoundsController.getInstance().setCallback(this));

        AlarmSoundsController.getInstance().setSilentAlarm(AlarmSubsystem.ACTIVEALERTS_SMOKE, isSilent);
        AlarmSoundsController.getInstance().setSilentAlarm(AlarmSubsystem.ACTIVEALERTS_CO, isSilent);
    }

    @Override
    public void setWaterSilent(boolean isSilent) {
        addListener(AlarmSoundsController.class.getCanonicalName(), AlarmSoundsController.getInstance().setCallback(this));

        AlarmSoundsController.getInstance().setSilentAlarm(AlarmSubsystem.ACTIVEALERTS_WATER, isSilent);
    }

    @Override
    public void onSilentAlarmChanged(String alarmInstance, boolean isSilent) {
        switch (alarmInstance) {
            case AlarmSubsystem.ACTIVEALERTS_SECURITY:
            case AlarmSubsystem.ACTIVEALERTS_PANIC:
                getPresentedView().updateView(new AlarmSoundsModel(AlarmSoundsModel.SECURITY_AND_PANIC, isSilent));
                break;

            case AlarmSubsystem.ACTIVEALERTS_CO:
            case AlarmSubsystem.ACTIVEALERTS_SMOKE:
                getPresentedView().updateView(new AlarmSoundsModel(AlarmSoundsModel.SMOKE_AND_CO, isSilent));
                break;

            case AlarmSubsystem.ACTIVEALERTS_WATER:
                getPresentedView().updateView(new AlarmSoundsModel(AlarmSoundsModel.WATER_LEAK, isSilent));
                break;
        }
    }

    @Override
    public void onAvailableAlertsChanged(@Nullable Set<String> alarmsAvailable) {
        getPresentedView().onAvailableAlarmsChanged(alarmsAvailable);
    }

    @Override
    public void onError(Throwable t) {
        getPresentedView().onError(t);
    }
}

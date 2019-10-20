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
package arcus.app.subsystems.alarm.promonitoring.util;

import arcus.cornea.subsystem.alarm.model.AlarmModel;
import com.iris.client.capability.AlarmSubsystem;
import com.iris.client.model.AlarmIncidentModel;
import arcus.app.ArcusApplication;
import arcus.app.R;



public class AlarmUtils {

    public static int getIconResIdForAlarmType (AlarmModel alarmModel) {

        switch (alarmModel.getType()) {
            case AlarmSubsystem.ACTIVEALERTS_CO:
                return R.drawable.promon_co_white;
            case AlarmSubsystem.ACTIVEALERTS_SMOKE:
                return R.drawable.promon_smoke_white;
            case AlarmSubsystem.ACTIVEALERTS_WATER:
                return R.drawable.promon_leak_white;
            case AlarmSubsystem.ACTIVEALERTS_PANIC:
            case AlarmSubsystem.ACTIVEALERTS_SECURITY:
                return R.drawable.promon_security_white;

            default:
                throw new IllegalArgumentException("Bug! No icon for service " + alarmModel);
        }
    }

    public static String getAlarmTypeDashboardDisplayString(String alarmType) {
        switch (alarmType) {
            case AlarmSubsystem.ACTIVEALERTS_CO:
                return ArcusApplication.getContext().getString(R.string.alarm_type_co_alarm);
            case AlarmSubsystem.ACTIVEALERTS_WATER:
                return ArcusApplication.getContext().getString(R.string.alarm_type_water_alarm);
            case AlarmSubsystem.ACTIVEALERTS_SMOKE:
                return ArcusApplication.getContext().getString(R.string.alarm_type_smoke_alarm);
            case AlarmSubsystem.ACTIVEALERTS_SECURITY:
                return ArcusApplication.getContext().getString(R.string.alarm_type_security_alarm);
            case AlarmSubsystem.ACTIVEALERTS_PANIC:
                return ArcusApplication.getContext().getString(R.string.alarm_type_panic_alarm);

            default:
                throw new IllegalArgumentException("Bug! No display string for alarm type " + alarmType);
        }
    }

    public static String getAlarmTypeStatusDisplayString(AlarmModel alarmModel) {
        return getAlarmTypeStatusDisplayString(alarmModel.getType());
    }

    public static String getAlarmTypeStatusDisplayString(String alarmTypeString) {
        switch (alarmTypeString) {
            case AlarmSubsystem.ACTIVEALERTS_CO:
                return ArcusApplication.getContext().getString(R.string.alarm_type_co);
            case AlarmSubsystem.ACTIVEALERTS_WATER:
                return ArcusApplication.getContext().getString(R.string.alarm_type_water);
            case AlarmSubsystem.ACTIVEALERTS_SMOKE:
                return ArcusApplication.getContext().getString(R.string.alarm_type_smoke);
            case AlarmSubsystem.ACTIVEALERTS_SECURITY:
                return ArcusApplication.getContext().getString(R.string.alarm_type_security);
            case AlarmSubsystem.ACTIVEALERTS_PANIC:
                return ArcusApplication.getContext().getString(R.string.alarm_type_panic);

            default:
                throw new IllegalArgumentException("Bug! No display string for alarm type " + alarmTypeString);
        }
    }

    public static int getTintForAlert(String alertType) {
        if (alertType.equalsIgnoreCase(AlarmUtils.getAlarmTypeStatusDisplayString(AlarmSubsystem.ACTIVEALERTS_SMOKE)))
            return ArcusApplication.getContext().getResources().getColor(R.color.safety_color);
        else if (alertType.equalsIgnoreCase(AlarmUtils.getAlarmTypeStatusDisplayString(AlarmSubsystem.ACTIVEALERTS_CO)))
            return ArcusApplication.getContext().getResources().getColor(R.color.safety_color);
        else if (alertType.equalsIgnoreCase(AlarmUtils.getAlarmTypeStatusDisplayString(AlarmSubsystem.ACTIVEALERTS_WATER)) || alertType.equalsIgnoreCase(AlarmIncidentModel.ALERT_WATER))
            return ArcusApplication.getContext().getResources().getColor(R.color.waterleak_color);
        else if (alertType.equalsIgnoreCase(AlarmUtils.getAlarmTypeStatusDisplayString(AlarmSubsystem.ACTIVEALERTS_PANIC)))
            return ArcusApplication.getContext().getResources().getColor(R.color.panic_color);
        else if (alertType.equalsIgnoreCase(AlarmUtils.getAlarmTypeStatusDisplayString(AlarmSubsystem.ACTIVEALERTS_SECURITY)))
            return ArcusApplication.getContext().getResources().getColor(R.color.security_color);
        else throw new IllegalArgumentException("Bug! Unsupported alert type: " + alertType);

    }
}

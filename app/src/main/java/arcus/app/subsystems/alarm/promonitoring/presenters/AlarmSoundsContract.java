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

import androidx.annotation.Nullable;

import arcus.cornea.common.PresentedView;
import arcus.cornea.common.Presenter;

import java.util.Set;



public class AlarmSoundsContract {

    public static class AlarmSoundsModel {
        public final static int SECURITY_AND_PANIC = 0, SMOKE_AND_CO = 1, WATER_LEAK= 2;

        public final int alarmType;
        public final boolean isSilent;

        public AlarmSoundsModel(int alarmType, boolean isSilent) {
            this.alarmType = alarmType;
            this.isSilent = isSilent;
        }
    }

    public interface AlarmSoundsPresenter extends Presenter<AlarmSoundsView> {

        void requestUpdate();
        void setSecurityPanicSilent(boolean isSilent);
        void setSmokeCoSilent(boolean isSilent);
        void setWaterSilent(boolean isSilent);
    }

    public interface AlarmSoundsView extends PresentedView<AlarmSoundsModel> {

        void onAvailableAlarmsChanged(@Nullable Set<String> alarmsAvailable);
    }
}

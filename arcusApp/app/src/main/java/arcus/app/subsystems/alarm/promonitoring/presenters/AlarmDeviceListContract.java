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

import arcus.cornea.common.PresentedView;
import arcus.cornea.common.Presenter;
import arcus.app.subsystems.alarm.promonitoring.models.AlertDeviceModel;

import java.util.List;

public class AlarmDeviceListContract {

    public interface AlarmDeviceListView extends PresentedView<List<AlertDeviceModel>> {}

    public interface AlarmDeviceListPresenter extends Presenter<AlarmDeviceListView> {

        /**
         * Request that the presenter call the view with a list of all available history items (i.e,
         * unfiltered).
         */
        void requestUpdate(String forAlarmType);
    }

}

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
package arcus.app.device.details.presenters;

import androidx.annotation.NonNull;

import arcus.cornea.common.PresentedView;
import arcus.cornea.common.Presenter;
import com.iris.client.model.DeviceModel;
import arcus.app.device.details.model.LutronDisplayModel;



public interface LutronContract {

    public interface LutronBridgeView extends PresentedView<LutronDisplayModel> {

        void onError(Throwable throwable);

        /**
         * Invoked by the presenter to get the DeviceModel.
         *
         * @return The DeviceModel. Cannot be null.
         */
        @NonNull DeviceModel getLutronDeviceModel();
    }

    public interface LutronPresenter extends Presenter<LutronContract.LutronBridgeView> {

        /**
         * Requests that the presenter refresh the view.
         */
        void requestUpdate();
    }
}

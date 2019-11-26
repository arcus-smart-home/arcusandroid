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

import arcus.cornea.common.PresentedView;
import arcus.cornea.common.Presenter;
import arcus.cornea.device.blinds.model.ShadeClientModel;


public interface ShadeContract {

    interface ShadeView extends PresentedView<ShadeClientModel> {

    }

    interface ShadePresenter extends Presenter<ShadeView> {

        /**
         * Requests the presenter to fetch data from the platform and refresh the UI via the
         * {@link PresentedView#updateView(Object)} (ShadeModel) method.
         */
        void requestUpdate();
    }
}

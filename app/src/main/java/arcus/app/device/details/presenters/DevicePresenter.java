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

import androidx.annotation.Nullable;

import arcus.cornea.common.BasePresenter;
import arcus.cornea.common.PresentedView;
import com.iris.client.model.DeviceModel;

import java.beans.PropertyChangeListener;



public abstract class DevicePresenter<PresentedViewType extends PresentedView> extends BasePresenter<PresentedViewType> implements PropertyChangeListener {

    public abstract DeviceModel getDeviceModel();

    @Override
    public void startPresenting(@Nullable PresentedViewType presentedView) {
        super.startPresenting(presentedView);
        addListener("devicePropertyListener", getDeviceModel().addPropertyChangeListener(this));
    }

}

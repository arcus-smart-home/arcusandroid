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
package arcus.cornea.device.smokeandco;

import androidx.annotation.Nullable;

import arcus.cornea.common.PresentedView;

public interface HaloContract {
    int PLAY_DURATION_SECONDS = 30;
    double ATMOS_MULTIPLIER = 0.2961339710085;

    float RED_BOUNDS_HSV = 343.5f;
    float BG_BOUNDS_HSV = 14.55f;

    interface View extends PresentedView<HaloModel> {
    }

    interface Presenter extends arcus.cornea.common.Presenter<View> {
        void playCurrentStation();
        void stopPlayingRadio();
        void playWeatherStation(@Nullable Integer station);
        void setSwitchOn(boolean isOn);
        void setDimmer(int dimmerPercent);
        void requestRefreshAndClearChanges(boolean andClearAnyPendingChanges);
    }

    interface DeviceTestPresenter extends arcus.cornea.common.Presenter<View> {
        void testDevice();
    }
}

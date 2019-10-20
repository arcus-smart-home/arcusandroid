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
package arcus.app.device.settings.enumeration;

import arcus.app.R;
import arcus.app.device.settings.core.Localizable;


public enum  CameraImageQuality implements Localizable {
    GOOD(R.string.setting_camera_quality_good),
    BETTER(R.string.setting_camera_quality_better),
    BEST(R.string.setting_camera_quality_best);

    private final int stringResId;

    CameraImageQuality (int stringResId) {
        this.stringResId = stringResId;
    }

    @Override
    public int getStringResId() {
        return stringResId;
    }
}

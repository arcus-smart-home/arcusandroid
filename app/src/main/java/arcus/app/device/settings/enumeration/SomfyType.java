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


import android.content.Context;
import androidx.annotation.NonNull;

import arcus.app.R;
import arcus.app.device.settings.core.Abstractable;
import arcus.app.device.settings.core.Localizable;




public enum SomfyType implements Localizable, Abstractable{
    TILT(R.string.setting_blind_type_tilt),
    ROLLER(R.string.setting_blind_type_raise);

    private final int displayedValueId;

    SomfyType(int displayedValueResId) {
        this.displayedValueId = displayedValueResId;
    }

    public int getStringResId () {
        return this.displayedValueId;
    }

    @Override
    public String getAbstract (@NonNull Context context) {
        return context.getString(displayedValueId);
    }

}


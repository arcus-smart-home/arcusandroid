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
package arcus.app.subsystems.alarm.promonitoring.models;

import android.content.Context;
import androidx.annotation.NonNull;

import arcus.app.R;


public enum PromonitoringFilter {
    SECURITY_AND_PANIC(R.string.promon_filter_security_and_panic),
    SMOKE_AND_CO(R.string.promon_filter_smoke_and_co),
    WATER_LEAK(R.string.promon_filter_water_leak);

    private final int titleStringResId;

    PromonitoringFilter(int titleStringResId) {
        this.titleStringResId = titleStringResId;
    }

    public String getTitle(@NonNull Context context) {
        return context.getString(titleStringResId);
    }
}

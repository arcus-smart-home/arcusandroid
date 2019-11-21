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
package arcus.app.subsystems.history.model;

import android.content.Context;
import androidx.annotation.NonNull;

import arcus.app.R;


public enum HistoryFilter {
    ALL(R.string.history_filter_all),
    DAY(R.string.history_filter_day),
    DEVICE(R.string.history_filter_device);

    private final int titleStringResId;

    HistoryFilter (int titleStringResId) {
        this.titleStringResId = titleStringResId;
    }

    public String getTitle(@NonNull Context context) {
        return context.getString(titleStringResId);
    }
}

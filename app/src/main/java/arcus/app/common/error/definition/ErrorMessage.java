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
package arcus.app.common.error.definition;

import android.content.res.Resources;
import androidx.annotation.NonNull;

import arcus.app.common.error.base.Error;


public class ErrorMessage implements Error {
    private final int titleResId;
    private final int textResId;
    private final Object[] formatArgs;

    public ErrorMessage (int titleResId, int textResId, Object... formatArgs) {
        this.titleResId = titleResId;
        this.textResId = textResId;
        this.formatArgs = formatArgs;
    }

    public ErrorMessage (int titleResId, int textResId) {
        this(titleResId, textResId, null);
    }

    @NonNull
    @Override
    public String getTitle(@NonNull Resources resources) {
        return resources.getString(titleResId);
    }

    @NonNull
    @Override
    public String getText(@NonNull Resources resources) {
        return formatArgs == null ? resources.getString(textResId) : resources.getString(textResId, formatArgs);
    }

    @Override
    public boolean isSystemDialog() {
        return true;
    }
}

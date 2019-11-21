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
package arcus.app.device.settings.style;

import android.content.Context;
import android.content.res.Resources;
import androidx.annotation.NonNull;

import arcus.app.ArcusApplication;
import arcus.app.device.settings.core.Setting;
import arcus.app.device.settings.core.Localizable;

import java.util.ArrayList;

/**
 * Renders a setting cell containing a title, description, chevron (>) and, when available,
 * a selection abstract. Clicking the cells produces a popup window letting the user select an
 * item within the set of enum constants defined by the provided enum class.
 *
 * Differs from {@link ListSelectionSetting} in that this setting takes an Enum class as input;
 * {@link ListSelectionSetting} takes an array of values.
 *
 */
public class EnumSelectionSetting<T extends Localizable> extends AbstractEnumeratedSetting implements Setting {
    private Class<T> enumeration;

    public EnumSelectionSetting(@NonNull Context context, String title, String description, @NonNull Class<T> enumeration, @NonNull T initialValue) {
        this(ArcusApplication.getContext(), title, description, null, enumeration, initialValue);
    }

    public EnumSelectionSetting(@NonNull Context context, String title, String description, String initialSelectionAbstract, @NonNull Class<T> enumeration, @NonNull T initialValue) {
        super(title, description, getEnumeratedStringValues(context.getResources(), enumeration.getEnumConstants()), context.getString(initialValue.getStringResId()), initialSelectionAbstract);
        this.enumeration = enumeration;
    }

    @NonNull
    public Object coerceSelectionValue (@NonNull Object selectedValue) {
        for (Localizable thisConst : enumeration.getEnumConstants()) {
            if (ArcusApplication.getContext().getString(thisConst.getStringResId()).equals(selectedValue.toString())) {
                return thisConst;
            }
        }

        throw new IllegalStateException("Bug! Selected value does not exist in enumeration.");
    }

    @NonNull
    public static ArrayList<String> getEnumeratedStringValues(@NonNull Resources resources, @NonNull Localizable[] enumeratedValues) {

        final ArrayList<String> values = new ArrayList<>();
        for (Localizable thisEnumConstant : enumeratedValues) {
            values.add(resources.getString(thisEnumConstant.getStringResId()));
        }

        return values;
    }
}

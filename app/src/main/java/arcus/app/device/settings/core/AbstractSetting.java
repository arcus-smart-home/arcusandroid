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
package arcus.app.device.settings.core;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import arcus.app.common.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * An abstract Setting providing methods to manage title, description, layout and change SettingChangedAdapters.
 */
public abstract class AbstractSetting implements Setting {

    private final List<SettingChangedParcelizedListener> SettingChangedAdapters = new ArrayList<>();
    private final List<SettingAbstractChangedListener> abstractChangedListeners = new ArrayList<>();

    private final String title;
    private final String description;
    private final int layoutId;
    private String selectionAbstract;
    private boolean useLightColorScheme = true;
    private @DrawableRes @Nullable Integer abstractIconResource;

    public AbstractSetting (String title, String description, int layoutId) {
        this.title = title;
        this.description = description;
        this.layoutId = layoutId;
        this.selectionAbstract = null;
    }

    public AbstractSetting(String title, String description, @Nullable @DrawableRes Integer abstractIcon, int layoutId) {
        this.title = title;
        this.description = description;
        this.layoutId = layoutId;
        this.selectionAbstract = null;
        this.abstractIconResource = abstractIcon;
    }

    public AbstractSetting (String title, String description, String selectionAbstract, int layoutId) {
        this.title = title;
        this.description = description;
        this.layoutId = layoutId;
        this.selectionAbstract = selectionAbstract;
    }

    public boolean hasSelectionAbstract () {
        return !StringUtils.isEmpty(this.selectionAbstract);
    }

    public void setSelectionAbstract (String selectionAbstract) {
        this.selectionAbstract = selectionAbstract;
        fireAbstractChangedListener();
    }
    public String getSelectionAbstract() { return selectionAbstract; }

    public @DrawableRes Integer getSelectionAbstractIconResource() {
        return abstractIconResource;
    }

    public void setSelectionAbstractIconResource(@DrawableRes @Nullable Integer iconResource) {
        this.abstractIconResource = iconResource;
    }

    public boolean hasSelectionIconResource() {
        return abstractIconResource != null;
    }

    @Override
    public boolean addListener (SettingAbstractChangedListener listener) {
        return abstractChangedListeners.add(listener);
    }

    @Override
    public boolean addListener (SettingChangedParcelizedListener listener) {
        return SettingChangedAdapters.add(listener);
    }

    public boolean removeListener(SettingChangedParcelizedListener listener) {
        return SettingChangedAdapters.remove(listener);
    }

    @Override
    public String getTitle() {
        return this.title;
    }

    @Override
    public String getDescription() {
        return this.description;
    }

    public int getLayoutId() {return this.layoutId;}

    protected void fireAbstractChangedListener() {
        for (Object thisListener : abstractChangedListeners.toArray()) {
            ((SettingAbstractChangedListener) thisListener).onSettingAbstractChanged();
        }
    }

    protected void fireSettingChangedAdapter(@NonNull Setting setting, Object newValue) {
        for (Object thisListener : SettingChangedAdapters.toArray()) {
            ((SettingChangedParcelizedListener) thisListener).onSettingChanged(setting, coerceSelectionValue(newValue));
        }
    }

    public boolean isUseLightColorScheme() {
        return useLightColorScheme;
    }

    public void setUseLightColorScheme(boolean useLightColorScheme) {
        this.useLightColorScheme = useLightColorScheme;
    }

    /**
     * Setting classes that extend this abstract class may override this value to convert the selected
     * value into a type more native to the setting. For example, EnumerationSetting can convert a
     * String representation of the selected value into an enum constant.
     *
     * @param selectedValue
     * @return
     */
    public Object coerceSelectionValue (Object selectedValue) {
        return selectedValue;
    }

    public String toString () {
        return "Setting " + getTitle();
    }
}

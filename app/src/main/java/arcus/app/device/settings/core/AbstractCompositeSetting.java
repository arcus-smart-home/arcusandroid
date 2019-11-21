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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * An abstract CompositeSetting class which provides management of child Setting elements.
 */
public abstract class AbstractCompositeSetting extends AbstractSetting implements CompositeSetting {

    private final static Logger logger = LoggerFactory.getLogger(AbstractCompositeSetting.class);
    @NonNull
    private List<Setting> settings = new ArrayList<>();

    public AbstractCompositeSetting (String title, String description, int layoutId) {
        super(title, description, layoutId);
    }

    public AbstractCompositeSetting (String title, String description, String initialSelectionAbstract, int layoutId) {
        super(title, description, initialSelectionAbstract, layoutId);
    }

    @NonNull
    public List<Setting> getSettings() {
        return this.settings;
    }

    public void addSetting(@Nullable Setting setting) {
        if (setting != null) {
            logger.debug("Adding " + setting + " to " + this.toString());
            settings.add(setting);
        }
    }

    @NonNull
    public String toString () {
        return "Composite setting " + getTitle() + " with " + settings.size() + " children.";
    }
}

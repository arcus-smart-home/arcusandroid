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
package arcus.app.seasonal.christmas.model;

import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;

import arcus.app.R;

public enum SantaMotionSensor {
    CHRISTMAS_TREE(R.string.santa_christmas_tree_motion_sensor, R.string.santa_sensor_description, R.drawable.icon_tree),
    MILK_COOKIE(R.string.santa_milk_and_cookies_motion_sensor, R.string.santa_sensor_description, R.drawable.icon_cookie),
    STOCKING(R.string.santa_christmas_stocking_motion_sensor, R.string.santa_sensor_description, R.drawable.icon_stocking);

    private @StringRes int title;
    private @StringRes int description;
    private @DrawableRes int icon;

    SantaMotionSensor(int titleRes, int descriptionRes, int iconRes) {
        this.title = titleRes;
        this.description = descriptionRes;
        this.icon = iconRes;
    }

    public @StringRes int getTitle() {
        return this.title;
    }

    public @StringRes int getDescription() {
        return this.description;
    }

    public @DrawableRes int getIcon() {
        return this.icon;
    }

}

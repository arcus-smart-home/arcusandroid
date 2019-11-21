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
package arcus.app.device.buttons.model;

import androidx.annotation.Nullable;


public enum SmartButton implements Button {
    DEFAULT_BUTTON;

    @Override
    public int getImageResId() {
        throw new IllegalStateException("No image associated with default button.");
    }

    @Override
    public int getStringResId() {
        throw new IllegalStateException("No name associated with default button.");
    }

    @Override
    public boolean isSingleton () {
        return true;
    }

    @Nullable
    public String getButtonName() {
        return null;
    }

}

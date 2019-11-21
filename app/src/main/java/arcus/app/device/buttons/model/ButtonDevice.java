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

import androidx.annotation.NonNull;

import java.util.Arrays;
import java.util.List;


public enum ButtonDevice {
    GEN3_FOUR_BUTTON_FOB("4fcccc"),
    GEN2_FOUR_BUTTON_FOB("4fccbb"),
    GEN1_TWO_BUTTON_FOB("486390"),
    GEN1_SMART_BUTTON("bca135"),
    GEN2_SMART_BUTTON("bbf1cf");

    @NonNull
    private final List<String> productIds;

    ButtonDevice (String... productIds) {
        this.productIds = Arrays.asList(productIds);
    }

    @NonNull
    public static ButtonDevice fromProductId (@NonNull String productId) {
        for (ButtonDevice thisButton : ButtonDevice.values()) {
            if (thisButton.productIds.contains(productId.toLowerCase())) {
                return thisButton;
            }
        }

        throw new IllegalArgumentException("No button device mapped to id " + productId);
    }

    public static boolean isButtonDevice (@NonNull String productId) {
        if (productId == null) {
            return false;
        }

        for (ButtonDevice thisButton : ButtonDevice.values()) {
            if (thisButton.productIds.contains(productId.toLowerCase())) {
                return true;
            }
        }

        return false;
    }
}

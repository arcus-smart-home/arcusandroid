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
package arcus.app.common.image;

import androidx.annotation.NonNull;

/**
 * An enumeration of known Android {@link android.content.Intent} request codes.
 */
// Why is this an enum?
public enum IntentRequestCode {

    TAKE_PHOTO(101),
    SELECT_IMAGE_FROM_GALLERY(102),
    DEVICE_CONTACT_SELECTION(103),
    FULLSCREEN_FRAGMENT(104),
    TURN_ON_LOCATION(105),
    CREDENTIAL_RETRIEVED(106),
    HUB_WIFI_PAIRING_REQUEST(107),
    EMAIL_SENT_SUCCESS(1010),
    UNKNOWN(9999)
    ;

    public final int requestCode;

    IntentRequestCode(int requestCode) {
        this.requestCode = requestCode;
    }

    @NonNull
    public static IntentRequestCode fromRequestCode(int requestCode) {
        for (IntentRequestCode thisResult : IntentRequestCode.values()) {
            if (thisResult.requestCode == requestCode) {
                return thisResult;
            }
        }

        return UNKNOWN;
    }
}

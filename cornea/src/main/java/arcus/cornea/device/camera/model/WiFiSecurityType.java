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
package arcus.cornea.device.camera.model;

import androidx.annotation.NonNull;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public enum  WiFiSecurityType {
    NONE("NONE"),
    WEP("WEP"),
    WPA_PSK("WPA"),
    WPA2_PSK("WPA2 PERSONAL"),
    WPA_ENTERPRISE("WPA ENTERPRISE"),
    WPA2_ENTERPRISE("WPA2 ENTERPRISE");

    public String label() {
        return StringUtils.capitalize(StringUtils.lowerCase(name().replace("_"," ")));
    }

    private final String name;

    WiFiSecurityType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @NonNull
    public static List<String> getNames() {
        List<String> list = new ArrayList<>();

        for (WiFiSecurityType type : values()) {
            list.add(type.getName());
        }

        return list;
    }

    @NonNull
    public static WiFiSecurityType fromSecurityString (String value) {
        if (value.equals(NONE.getName()))
            return NONE;
        else if (value.equals(WEP.getName()))
            return WEP;
        else if (value.equals(WPA_PSK.getName()))
            return WPA_PSK;
        else if (value.equals(WPA2_PSK.getName()))
            return WPA2_PSK;
        else if (value.equals(WPA_ENTERPRISE.getName()))
            return WPA_ENTERPRISE;
        else if (value.equals(WPA2_ENTERPRISE.getName()))
            return WPA2_ENTERPRISE;

        return NONE;
    }
}

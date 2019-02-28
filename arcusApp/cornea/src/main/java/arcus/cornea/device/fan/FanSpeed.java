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
package arcus.cornea.device.fan;

import java.util.HashMap;
import java.util.Map;


public enum FanSpeed {
    HIGH("HIGH",3), MEDIUM("MEDIUM",2),LOW("LOW",1);

    FanSpeed(String mode,int speed){
        this.speed = speed;
        this.mode = mode;
    }

    private static Map<Integer, FanSpeed> map = new HashMap<Integer, FanSpeed>();

    static {
        for (FanSpeed fanSpeed : FanSpeed.values()) {
            map.put(fanSpeed.getSpeed(), fanSpeed);
        }
    }

    public static FanSpeed valueOf(int speed){
        return map.get(speed);
    }

    private String mode;
    private int speed;

    public String getMode() {
        return mode;
    }

    public int getSpeed() {
        return speed;
    }

    public FanSpeed next() {
        switch (this) {
            case HIGH:
                return LOW;
            case MEDIUM:
                return HIGH;
            case LOW:
                return MEDIUM;
        }

        return this;
    }

    public FanSpeed previous() {
        switch (this) {
            case HIGH:
                return MEDIUM;
            case MEDIUM:
                return LOW;
            case LOW:
                return HIGH;
        }

        return this;
    }
}

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
package arcus.app.device.pairing.steps.model;


public class PairingStepInput {

    private String value;

    private final String name;
    private final String label;
    private final Integer requiredLength;
    private final boolean visible;

    /**
     * Constructs a "hidden" input field consisting only of a name/value pair
     * @param name
     * @param value
     */
    public PairingStepInput (String name, String value) {
        this(name, null, value, null, false);
    }

    /**
     * Constructs an editable input field
     *
     * @param name
     * @param label
     * @param requiredLength
     */
    public PairingStepInput (String name, String label, Integer requiredLength) {
        this(name, label, "", requiredLength, true);
    }

    public PairingStepInput (String name, String label, String value, Integer requiredLength, boolean visible) {
        this.name = name;
        this.label = label;
        this.value = value;
        this.requiredLength = requiredLength;
        this.visible = visible;
    }

    public String getName() {
        return this.name;
    }

    public String getValue() {
        return this.value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getLabel() {
        return this.label;
    }

    public boolean isVisible () {
        return this.visible;
    }

    public int getRequiredLength () {
        return this.requiredLength == null ? 0 : this.requiredLength;
    }

    public boolean isValid() {
        return requiredLength == null || getValue().length() == requiredLength;
    }

}

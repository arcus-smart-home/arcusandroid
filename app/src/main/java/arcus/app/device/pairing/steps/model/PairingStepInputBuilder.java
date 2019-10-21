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

import org.apache.commons.lang3.math.NumberUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class PairingStepInputBuilder {

    private final static String TYPE_FIELD = "type";
    private final static String NAME_FIELD = "name";
    private final static String VALUE_FIELD = "value";
    private final static String LABEL_FIELD = "label";
    private final static String MAX_LENGTH_FIELD = "maxlen";
    private final static String REQUIRED_FIELD = "required";

    private final static String HIDDEN_VALUE = "HIDDEN";
    private final static String TEXT_VALUE = "TEXT";

    public static List<PairingStepInput> from (List<Map<String,Object>> inputFieldsData) {
        List<PairingStepInput> inputFields = new ArrayList<>();

        if (inputFieldsData != null) {
            for (Map<String,Object> inputFieldData : inputFieldsData) {
                inputFields.add(from(inputFieldData));
            }
        }

        return inputFields;
    }

    public static PairingStepInput from (Map<String, Object> pairingStepInputsData) {

        String inputType = (String) pairingStepInputsData.get(TYPE_FIELD);

        // This indicates a bug in the product catalog!
        if (inputType == null) {
            throw new IllegalStateException("Pairing step input field type must be specified; was null.");
        }

        switch (inputType.toUpperCase()) {
            case HIDDEN_VALUE: return buildHiddenInput(pairingStepInputsData);
            case TEXT_VALUE: return buildTextInput(pairingStepInputsData);

            default:
                throw new IllegalArgumentException("Bug! Pairing step input field type not supported/implemented: " + inputType);
        }
    }

    private static PairingStepInput buildTextInput (Map<String,Object> fieldData) {

        String name = String.valueOf(fieldData.get(NAME_FIELD));
        String label = String.valueOf(fieldData.get(LABEL_FIELD));
        boolean required = Boolean.valueOf(String.valueOf(fieldData.get(REQUIRED_FIELD)));
        int requiredLength = (int) NumberUtils.toFloat(String.valueOf(fieldData.get(MAX_LENGTH_FIELD)), Float.MAX_VALUE);

        return new PairingStepInput(name, label, required ? requiredLength : null);
    }

    private static PairingStepInput buildHiddenInput (Map<String,Object> fieldData) {

        String name = String.valueOf(fieldData.get(NAME_FIELD));
        String value = String.valueOf(fieldData.get(VALUE_FIELD));

        return new PairingStepInput(name, value);
    }
}

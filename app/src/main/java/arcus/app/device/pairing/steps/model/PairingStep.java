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

import android.net.Uri;
import androidx.annotation.NonNull;

import arcus.app.common.utils.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class PairingStep implements Serializable {

    private final static Logger logger = LoggerFactory.getLogger(PairingStep.class);

    private final Map<String,Object> attributes;
    private final int stepNumber;
    private final String deviceName;
    private final String productId;
    private final String tutorialVideoUrl;
    private List<PairingStepInput> inputs;

    public PairingStep(String productId, String deviceName, String tutorialVideoUrl, int stepNumber, Map<String,Object> attributes) {
        this.deviceName = deviceName;
        this.attributes = attributes;
        this.stepNumber = stepNumber;
        this.tutorialVideoUrl = tutorialVideoUrl;
        this.productId = productId;
    }

    public String getDeviceName () { return this.deviceName; }
    public int getStepNumber () { return this.stepNumber; }
    public String getProductId () { return this.productId; }

    @NonNull
    public String getText() {
        return (String) attributes.get("text");
    }

    @NonNull
    public String getType() {
        return (String) attributes.get("type");
    }

    public List<PairingStepInput> getInputs () {
        return PairingStepInputBuilder.from((List<Map<String,Object>>) attributes.get("inputs"));
    }

    public boolean requiresInput () {
        return getInputs().size() > 0;
    }

    public boolean hasTutorialVideo () { return !StringUtils.isEmpty(tutorialVideoUrl); }

    public Uri getTutorialVideoUri () { return Uri.parse(tutorialVideoUrl); }
}

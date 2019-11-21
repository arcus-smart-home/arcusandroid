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

import arcus.app.R;


public enum TwoButtonFobButtonAction implements ButtonAction {

    SET_SECURITY_ALARM_TO_ON(R.string.fob_rule_alarm_on, "smartfobgen1-arm-on"),
    SET_SECURITY_ALARM_TO_OFF(R.string.fob_rule_alarm_off, "smartfobgen1-disarm"),
    SET_SECURITY_ALARM_TO_PARTIAL(R.string.fob_rule_alarm_partial, "smartfobgen1-arm-partial"),
    PLAY_CHIME(R.string.fob_rule_play_chime, "smartfobgen1-chime"),
    ACTIVATE_A_RULE(R.string.fob_rule_activate_rule, null);

    private final int stringResId;
    private final String assignedTemplate;

    TwoButtonFobButtonAction (int stringResId, String assignedTemplate) {
        this.stringResId = stringResId;
        this.assignedTemplate = assignedTemplate;
    }

    @Override
    public int getStringResId() {
        return stringResId;
    }

    @Override
    public boolean isDefaultAction() {
        return this == ACTIVATE_A_RULE;
    }

    @Override
    public String getRuleTemplateId () {
        return assignedTemplate;
    }

    @Override
    public String getDeviceAddressArgumentName() {
        return "key fob";
    }

    @Override
    @Nullable
    public String getButtonIdArgumentName() {
        return "button";
    }

}

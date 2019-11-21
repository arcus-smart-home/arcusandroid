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


public enum SmartButtonAction implements ButtonAction {
    BUTTON_PANIC(R.string.button_rule_panic, "button-panic"),
    PLAY_CHIME(R.string.fob_rule_play_chime, "button-chime"),
    ACTIVATE_A_RULE(R.string.fob_rule_activate_rule, null);

    private final int stringResId;
    private final String ruleTemplateId;

    SmartButtonAction (int stringResId, String ruleTemplateId) {
        this.stringResId = stringResId;
        this.ruleTemplateId = ruleTemplateId;
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
        return ruleTemplateId;
    }

    @Override
    public String getDeviceAddressArgumentName() {
        return "button";
    }

    @Override
    @Nullable
    public String getButtonIdArgumentName() {
        return null;
    }
}

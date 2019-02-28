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
package arcus.cornea.model.editors;

import arcus.cornea.model.RuleEditorCallbacks;

import java.util.List;

public class ShowModelListEditor implements ShowEditor {
    private List<String> identifiers;

    public ShowModelListEditor(List<String> identifiers) {
        this.identifiers = identifiers;
    }

    @Override
    public void show(RuleEditorCallbacks callbacks) {
        callbacks.showModelListSelector(identifiers);
    }
}


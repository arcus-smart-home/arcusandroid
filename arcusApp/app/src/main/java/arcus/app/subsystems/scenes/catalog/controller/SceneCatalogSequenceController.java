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
package arcus.app.subsystems.scenes.catalog.controller;

import android.app.Activity;

import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.sequence.AbstractSequenceController;
import arcus.app.common.sequence.Sequenceable;

import arcus.app.subsystems.scenes.catalog.SceneCatalogFragment;
import arcus.app.subsystems.scenes.editor.controller.SceneEditorSequenceController;


public class SceneCatalogSequenceController extends AbstractSequenceController {

    private Sequenceable lastSequence;
    private boolean engageUser = false;

    @Override
    public void goNext(Activity activity, Sequenceable from, Object... data) {
        String sceneTemplateAddress = unpackArgument(0, String.class, data);
        navigateForward(activity, new SceneEditorSequenceController(), sceneTemplateAddress);
    }

    @Override
    public void goBack(Activity activity, Sequenceable from, Object... data) {
        endSequence(activity, true);
    }

    @Override
    public void endSequence(Activity activity, boolean isSuccess, Object... data) {
        if (lastSequence != null) {
            navigateBack(activity, lastSequence, data);
        } else {
            BackstackManager.getInstance().navigateBack();
        }
    }

    @Override
    public void startSequence(Activity activity, Sequenceable from, Object... data) {
        this.lastSequence = from;
        navigateForward(activity, SceneCatalogFragment.newInstance());
    }

    public void setShouldEngage(boolean engageUser) {
        this.engageUser = engageUser;
    }

    public boolean shouldEngage() {
        return engageUser;
    }
}

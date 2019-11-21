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
package arcus.app.subsystems.scenes.editor;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.common.base.Strings;
import arcus.cornea.utils.LooperExecutor;
import arcus.app.R;
import arcus.app.common.error.ErrorManager;
import arcus.app.common.sequence.SequencedFragment;
import arcus.app.common.view.Version1Button;
import arcus.app.common.view.Version1ButtonColor;
import arcus.app.common.view.Version1EditText;
import arcus.app.subsystems.scenes.editor.controller.SceneEditorFragmentController;
import arcus.app.subsystems.scenes.editor.controller.SceneEditorSequenceController;


public class SceneNameEditorFragment extends SequencedFragment<SceneEditorSequenceController> implements SceneEditorFragmentController.Callbacks {

    private Version1EditText sceneName;
    private Version1Button saveButton;

    public static SceneNameEditorFragment newInstance() {
        SceneNameEditorFragment instance = new SceneNameEditorFragment();
        return instance;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        sceneName = (Version1EditText) view.findViewById(R.id.scene_name);
        saveButton = (Version1Button) view.findViewById(R.id.save_button);

        return view;
    }

    @Override
    public void onResume () {
        super.onResume();

        sceneName.useLightColorScheme(getController().isEditMode());
        saveButton.setColorScheme(getController().isEditMode() ? Version1ButtonColor.WHITE : Version1ButtonColor.BLACK);
        String sceneAddress = getController().getSceneAddress();
        SceneEditorFragmentController.getInstance().setListener(this);
        SceneEditorFragmentController.getInstance().editScene(sceneAddress);
    }

    @Override
    public void onPause() {
        super.onPause();
        SceneEditorFragmentController.getInstance().removeListener();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        SceneEditorFragmentController.getInstance().removeListener();
    }

    @Nullable
    @Override
    public String getTitle() {
        return "";
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_scene_name_editor;
    }

    @Override
    public void onSceneLoaded(String template, String sceneAddress, String name, boolean notificationsEnabled, boolean isFavorite, boolean hasActions) {
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sceneName.getText() != null && !Strings.isNullOrEmpty(sceneName.getText().toString())) {
                    getController().setHasEdited(true);
                    SceneEditorFragmentController.getInstance().setName(sceneName.getText().toString());
                    goNext();
                }
            }
        });

        if (!Strings.isNullOrEmpty(name)) {
            sceneName.setText(name);
            getActivity().invalidateOptionsMenu();
            getActivity().setTitle(name);
        }
    }

    @Override
    public void onSceneDeleted() {}

    @Override
    public void onCorneaError(final Throwable cause) {
        LooperExecutor.getMainExecutor().execute(new Runnable() {
            @Override
            public void run() {
                hideProgressBar();
                ErrorManager.in(getActivity()).showGenericBecauseOf(cause);
            }
        });
    }
}

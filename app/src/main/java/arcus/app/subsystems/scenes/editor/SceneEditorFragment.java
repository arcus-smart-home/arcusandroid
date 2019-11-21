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

import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import arcus.cornea.utils.LooperExecutor;
import arcus.app.R;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.error.ErrorManager;
import arcus.app.common.error.definition.TryAgainOrCancelError;
import arcus.app.common.error.listener.DismissListener;
import arcus.app.common.popups.InfoTextPopup;
import arcus.app.common.sequence.SequencedFragment;
import arcus.app.common.view.Version1Button;
import arcus.app.common.view.Version1ButtonColor;
import arcus.app.device.settings.adapter.SettingsListAdapter;
import arcus.app.device.settings.core.Setting;
import arcus.app.device.settings.core.SettingChangedParcelizedListener;
import arcus.app.device.settings.core.SettingsList;
import arcus.app.device.settings.style.BinarySetting;
import arcus.app.device.settings.style.OnClickActionSetting;
import arcus.app.subsystems.scenes.editor.controller.SceneEditorFragmentController;
import arcus.app.subsystems.scenes.editor.controller.SceneEditorSequenceController;
import arcus.app.subsystems.scenes.editor.model.LearnMoreCopyFactory;
import arcus.app.subsystems.scenes.schedule.controller.SceneScheduleFragmentController;

import java.util.concurrent.atomic.AtomicReference;


public class SceneEditorFragment extends SequencedFragment<SceneEditorSequenceController> implements SceneEditorFragmentController.Callbacks, SceneScheduleFragmentController.Callbacks {
    private static final String SCENE_TMPL_PREFIX = "SERV:scenetmpl:";

    private AtomicReference<String> createdModelAddress = new AtomicReference<>("");
    private ListView sceneProperties;
    private RelativeLayout learnMoreRegion;
    private TextView learnMoreTextView;
    private Setting scheduleSetting;
    private Version1Button saveButton;
    private boolean hasActions;

    public static SceneEditorFragment newInstance() {
        SceneEditorFragment instance = new SceneEditorFragment();
        return instance;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        sceneProperties = (ListView) view.findViewById(R.id.scene_properties);
        learnMoreRegion = (RelativeLayout) view.findViewById(R.id.learn_more_region);
        learnMoreTextView = (TextView) view.findViewById(R.id.learn_more_button_text);
        saveButton = (Version1Button) view.findViewById(R.id.save_button);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        showProgressBar();
        SceneEditorFragmentController.getInstance().setListener(this);
        SceneScheduleFragmentController.getInstance().setListener(this);

        String sceneAddress = getController().getSceneAddress();
        if (sceneAddress.startsWith(SCENE_TMPL_PREFIX)) {
            SceneEditorFragmentController.getInstance().createScene(sceneAddress);
        } else {
            SceneEditorFragmentController.getInstance().editScene(sceneAddress);
        }
        learnMoreRegion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String moreInfoText = LearnMoreCopyFactory.createLearnMoreCopy(getController().getTemplateID(), "");
                BackstackManager.getInstance().navigateToFloatingFragment(
                        InfoTextPopup.newInstance(moreInfoText, R.string.scenes_learn_more),
                        InfoTextPopup.class.getSimpleName(),
                        true);
            }
        });

        if (getController().isEditMode()) {
            saveButton.setVisibility(View.GONE);
        } else {
            saveButton.setVisibility(View.VISIBLE);
            saveButton.setEnabled(false);
            saveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    endSequence(true, true);
                }
            });
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        hideProgressBar();
        SceneEditorFragmentController.getInstance().removeListener();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        hideProgressBar();
        SceneEditorFragmentController.getInstance().removeListener();
    }

    @Override
    @Nullable
    public String getTitle() {
        return "";
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_scene_editor;
    }

    private Setting buildActionsSetting() {
        OnClickActionSetting actionSetting = new OnClickActionSetting(getString(R.string.scenes_actions_title), getString(R.string.scenes_actions_desc), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getController().goActionEditor(getActivity(), SceneEditorFragment.this, createdModelAddress.get());
            }
        });
        actionSetting.setUseLightColorScheme(getController().isEditMode());

        return actionSetting;
    }

    private Setting buildScheduleSetting() {
        OnClickActionSetting scheduleSetting = new OnClickActionSetting(getString(R.string.scenes_schedule_title), getString(R.string.scenes_schedule_desc), "", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getController().goWeeklyScheduleEditor(getActivity(), SceneEditorFragment.this, createdModelAddress.get());
            }
        });
        scheduleSetting.setUseLightColorScheme(getController().isEditMode());

        return scheduleSetting;
    }

    private Setting buildNotificationSetting(boolean notificationEnabled) {
        String title = getString(R.string.scenes_notification_title);
        String description = getString(R.string.scenes_notification_desc);
        BinarySetting notificationSetting = new BinarySetting(title, description, notificationEnabled);
        notificationSetting.addListener(new SettingChangedParcelizedListener() {
            @Override
            public void onSettingChanged(Setting setting, Object newValue) {
                getController().setHasEdited(true);
                SceneEditorFragmentController.getInstance().setNotificationEnabled(Boolean.TRUE.equals(newValue));
            }
        });
        notificationSetting.setUseLightColorScheme(getController().isEditMode());

        return notificationSetting;
    }

    private Setting buildFavoriteSetting(boolean isFavorite) {
        String title = getString(R.string.scenes_favorites_title);
        String description = getString(R.string.scenes_favorites_desc);
        BinarySetting favoriteSetting = new BinarySetting(title, description, isFavorite);
        favoriteSetting.addListener(new SettingChangedParcelizedListener() {
            @Override
            public void onSettingChanged(Setting setting, Object newValue) {
                getController().setHasEdited(true);
                SceneEditorFragmentController.getInstance().setFavorite(Boolean.TRUE.equals(newValue));
            }
        });
        favoriteSetting.setUseLightColorScheme(getController().isEditMode());

        return favoriteSetting;
    }

    private Setting buildEditNameSetting(String existingName) {
        OnClickActionSetting editNameSetting = new OnClickActionSetting(getString(R.string.scenes_edit_name_title), null, existingName, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getController().goNameEditor(getActivity(), SceneEditorFragment.this, createdModelAddress.get());
            }
        });
        editNameSetting.setUseLightColorScheme(getController().isEditMode());

        return editNameSetting;
    }

    public void confirmDeletion() {

        // Scene has actions; ask before we delete
        if (hasActions) {
            ErrorManager.in(getActivity())
                    .withDialogDismissedListener(new DismissListener() {
                        @Override
                        public void dialogDismissedByReject() {
                            // Nothing to do
                        }

                        @Override
                        public void dialogDismissedByAccept() {
                            getController().setHasEdited(false);
                            SceneEditorFragmentController.getInstance().deleteScene();
                            endSequence(true);
                        }
                    })
                    .show(new TryAgainOrCancelError(R.string.scene_confirm_delete, R.string.scene_confirm_delete_desc, R.string.scene_confirm_delete_affirmative));
        }

        // Scene has no actions; delete right away
        else {
            getController().setHasEdited(false);
            SceneEditorFragmentController.getInstance().deleteScene();
            endSequence(true);
        }
    }

    @Override
    public void onSceneLoaded(String template, String sceneAddress, String name, boolean notificationsEnabled, boolean isFavorite, boolean hasActions) {
        hideProgressBar();

        logger.debug("Set light color scheme is [{}]", getController().isEditMode());
        createdModelAddress.set(sceneAddress);
        getController().setTemplateID(template);
        getController().setSceneAddress(sceneAddress);

        scheduleSetting = buildScheduleSetting();

        SettingsList settings = new SettingsList();
        settings.add(buildActionsSetting());
        settings.add(scheduleSetting);
        settings.add(buildFavoriteSetting(isFavorite));
        settings.add(buildNotificationSetting(notificationsEnabled));
        settings.add(buildEditNameSetting(name));

        getActivity().setTitle(name);
        getActivity().invalidateOptionsMenu();

        if (getController().isEditMode()) {
            saveButton.setColorScheme(Version1ButtonColor.WHITE);
            learnMoreTextView.setTextColor(Color.WHITE);
            learnMoreTextView.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.outline_button_style));
        }
        sceneProperties.setAdapter(new SettingsListAdapter(getActivity(), settings));
        saveButton.setEnabled(hasActions);
        this.hasActions = hasActions;

        SceneScheduleFragmentController.getInstance().loadScheduleAbstract(getActivity(), getController().getSceneAddress());
    }

    @Override
    public void onSceneDeleted() {
        hideProgressBar();
        endSequence(true);
    }

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

    @Override
    public void onScheduleAbstractLoaded(boolean hasSchedule, boolean scheduleEnabled, String scheduleAbstract, String sceneAddress) {
        if (hasSchedule) {
            scheduleSetting.setSelectionAbstract(scheduleAbstract);
        } else {
            scheduleSetting.setSelectionAbstract(getString(R.string.scene_none));
        }

        ((ArrayAdapter) sceneProperties.getAdapter()).notifyDataSetChanged();
    }

}


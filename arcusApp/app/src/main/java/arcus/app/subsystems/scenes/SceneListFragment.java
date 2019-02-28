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
package arcus.app.subsystems.scenes;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;

import arcus.cornea.SessionController;
import com.iris.client.model.PlaceModel;
import arcus.app.R;
import arcus.app.account.settings.data.WalkthroughType;
import arcus.app.account.settings.walkthroughs.WalkthroughBaseFragment;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.error.ErrorManager;
import arcus.app.common.fragments.BaseFragment;
import arcus.app.common.image.ImageManager;
import arcus.app.common.image.picasso.transformation.AlphaPreset;
import arcus.app.common.utils.PreferenceCache;
import arcus.app.common.utils.PreferenceUtils;
import arcus.app.common.view.Version1TextView;
import arcus.app.subsystems.scenes.active.controller.SceneListController;
import arcus.app.subsystems.scenes.active.model.SceneListModel;
import arcus.app.subsystems.scenes.adapters.SceneListAdapter;
import arcus.app.subsystems.scenes.editor.controller.SceneEditorSequenceController;

import java.util.ArrayList;
import java.util.List;

public class SceneListFragment extends BaseFragment implements SceneListController.Callback, SceneListAdapter.OnClickListener {

    private SceneListAdapter sceneListAdapter;
    private boolean isEditMode = false;

    SceneListController controller;

    private LinearLayout noScenesLayout;
    private LinearLayout sceneLayout;
    private ListView sceneList;
    private Version1TextView sceneCount;

    private ArrayList<SceneListModel> scenes;

    @NonNull
    public static SceneListFragment newInstance() {
        return new SceneListFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        noScenesLayout = view.findViewById(R.id.no_scenes_layout);
        sceneLayout = view.findViewById(R.id.scenes_layout);
        sceneList = view.findViewById(R.id.scenes_list);
        sceneCount = view.findViewById(R.id.scenes_count);
    }

    private void initializeNoScenesView() {
        noScenesLayout.setVisibility(View.VISIBLE);
        sceneLayout.setVisibility(View.GONE);
    }

    private void initializeScenesView(@NonNull List<SceneListModel> scenes) {
        noScenesLayout.setVisibility(View.GONE);
        sceneLayout.setVisibility(View.VISIBLE);

        if (sceneListAdapter == null || sceneList.getAdapter() == null) {
            sceneListAdapter = new SceneListAdapter(getActivity(), this.scenes);
            sceneListAdapter.setListener(this);
            sceneList.setAdapter(sceneListAdapter);
        } else {
            sceneListAdapter.notifyDataSetChanged();
        }

        sceneCount.setText(String.valueOf(scenes.size()));
    }

    @Override
    public void onResume() {
        super.onResume();
        setTitle();

        if (getCorneaService() != null) {
            showProgressBar();
            if (controller == null) {
                controller = SceneListController.instance();
            }

            controller.setCallback(this);
            controller.listScenes();

            PlaceModel model = SessionController.instance().getPlace();
            if (model != null) {
                ImageManager.with(getActivity())
                        .putPlaceImage(model.getId())
                        .intoWallpaper(AlphaPreset.DARKEN)
                        .execute();
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if (controller != null)
            controller.clearCallbacks();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        hideProgressBar();
    }

    @Override
    public String getTitle() {
        return getString(R.string.scenes_scenes);
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_scene_list;
    }

    @Nullable
    @Override
    public Integer getMenuId() {
        return (this.scenes != null && this.scenes.size() > 0) ? R.menu.menu_edit_done_toggle : null;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        isEditMode = !isEditMode;

        sceneListAdapter.setEditMode(isEditMode);
        item.setTitle(isEditMode ? getResources().getString(R.string.card_menu_done) : getResources().getString(R.string.card_menu_edit));

        return true;
    }

    public void removeScene(String address) {
        for (SceneListModel model : scenes) {
            if (model.getModelAddress().equals(address)) {
                scenes.remove(model);
                break;
            }
        }

        if (scenes.size() == 0) {
            initializeNoScenesView();
        } else {
            initializeScenesView(scenes);
        }
    }

    /***
     * Scene List Controller Callbacks
     */

    @Override
    public void scenesLoaded(List<SceneListModel> scenes) {
        hideProgressBar();
        if (scenes.size() == 0) {
            initializeNoScenesView();
        } else {
            if (this.scenes == null)
                this.scenes = new ArrayList<>(scenes);
            else {
                this.scenes.clear();
                this.scenes.addAll(scenes);
            }

            initializeScenesView(this.scenes);
        }

        // Update the options menu when scenes load (as this effects the presence of the edit menu)
        Activity activity = getActivity();
        if (activity != null) {
            activity.invalidateOptionsMenu();
        }

        if (!PreferenceCache.getInstance().getBoolean(PreferenceUtils.SCENES_WALKTHROUGH_DONT_SHOW_AGAIN, false)) {
            WalkthroughBaseFragment scenesWalkthroughFragment = WalkthroughBaseFragment.newInstance(WalkthroughType.SCENES);
            BackstackManager.getInstance().navigateToFloatingFragment(scenesWalkthroughFragment, scenesWalkthroughFragment.getClass().getName(), true);
        }
    }

    @Override
    public void modelDeleted(String addressDeleted) {
        // Remove model from list
        removeScene(addressDeleted);
    }

    @Override
    public void onError(Throwable throwable) {
        hideProgressBar();
        ErrorManager.in(getActivity()).showGenericBecauseOf(throwable);
    }

    /***
     * Scene List Adapter Callback
     */

    @Override
    public void onItemClicked(SceneListModel scene) {
        new SceneEditorSequenceController().startSequence(getActivity(), null, scene.getModelAddress());
    }

    @Override
    public void onDeleteClicked(SceneListModel scene) {
        controller.deleteScene(scene.getModelAddress());
    }
}
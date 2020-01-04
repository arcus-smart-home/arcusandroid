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
package arcus.app.subsystems.scenes.catalog;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import arcus.app.R;
import arcus.app.account.settings.WalkthroughType;
import arcus.app.account.settings.walkthroughs.WalkthroughBaseFragment;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.error.ErrorManager;
import arcus.app.common.sequence.SequencedFragment;
import arcus.app.common.utils.PreferenceCache;
import arcus.app.common.utils.PreferenceUtils;
import arcus.app.subsystems.scenes.catalog.adapter.SceneCategoryAdapter;
import arcus.app.subsystems.scenes.catalog.controller.SceneCatalogFragmentController;
import arcus.app.subsystems.scenes.catalog.controller.SceneCatalogSequenceController;
import arcus.app.subsystems.scenes.catalog.model.SceneCategory;

import java.util.List;


public class SceneCatalogFragment extends SequencedFragment<SceneCatalogSequenceController>
      implements SceneCatalogFragmentController.Callbacks, WalkthroughBaseFragment.DestroyedCallback {

    private ListView categoriesList;
    private SceneCategoryAdapter categoriesAdapter;

    public static SceneCatalogFragment newInstance() {
        return new SceneCatalogFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        categoriesList = (ListView) view.findViewById(R.id.scene_categories);
        return view;
    }

    @Override
    public void onResume () {
        super.onResume();
        getActivity().setTitle(getTitle());
        getActivity().invalidateOptionsMenu();

        categoriesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                goNext(categoriesAdapter.getCategoryAt(position).getTemplateAddress());
            }
        });

        showProgressBar();
        SceneCatalogFragmentController.instance().setListener(this);
        SceneCatalogFragmentController.instance().loadCategories();
    }

    @Override
    public void walkthroughFragmentDestroyed() {
    }

    @Override
    public void onPause () {
        super.onPause();

        hideProgressBar();
        SceneCatalogFragmentController.instance().removeListener();
    }

    @Nullable
    @Override
    public String getTitle() {
        return getString(R.string.scenes_choose_a_scene);
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_scene_catalog;
    }

    @Override
    public void onCorneaError(Throwable throwable) {
        hideProgressBar();
        ErrorManager.in(getActivity()).showGenericBecauseOf(throwable);
    }

    @Override
    public void onCategoriesLoaded(List<SceneCategory> categories) {
        hideProgressBar();

        categoriesAdapter = new SceneCategoryAdapter(getActivity(), categories);
        categoriesAdapter.setUseLightColorScheme(false);
        categoriesList.setAdapter(categoriesAdapter);

        if (!PreferenceCache.getInstance().getBoolean(PreferenceUtils.SCENES_WALKTHROUGH_DONT_SHOW_AGAIN, false)) {
            WalkthroughBaseFragment scenesWalkthroughFragment = WalkthroughBaseFragment.newInstance(WalkthroughType.SCENES);
            scenesWalkthroughFragment.setCallback(this);
            BackstackManager.getInstance().navigateToFloatingFragment(scenesWalkthroughFragment, scenesWalkthroughFragment.getClass().getName(), true);
        } else {
            walkthroughFragmentDestroyed(); // Check if we should engage.
        }
    }
}

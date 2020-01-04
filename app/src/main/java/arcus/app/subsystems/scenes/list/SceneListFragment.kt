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
package arcus.app.subsystems.scenes.list

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.Group
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import arcus.app.R
import arcus.app.account.settings.WalkthroughType
import arcus.app.account.settings.walkthroughs.WalkthroughBaseFragment
import arcus.app.common.backstack.BackstackManager
import arcus.app.common.fragments.CoreFragment
import arcus.app.common.utils.PreferenceCache
import arcus.app.common.utils.PreferenceUtils
import arcus.app.subsystems.scenes.editor.controller.SceneEditorSequenceController
import arcus.presentation.common.view.ViewState
import arcus.presentation.scenes.Scene
import arcus.presentation.scenes.SceneListItems
import arcus.presentation.scenes.list.SceneListViewModel
import arcus.app.common.error.ErrorManager.`in` as errorIn

class SceneListFragment : CoreFragment<SceneListViewModel>(), SceneListAdapter.ClickListener {
    private lateinit var sceneListAdapter: SceneListAdapter
    private lateinit var noScenesLayout: Group
    private lateinit var scenesLayout: Group
    private lateinit var sceneList: RecyclerView
    private lateinit var sceneSectionCount: TextView

    private var isEditMode = false

    override val viewModelClass: Class<SceneListViewModel> = SceneListViewModel::class.java
    override val title: String
        get() = getString(R.string.scenes_scenes)
    override val layoutId: Int
        get() = R.layout.fragment_scene_list
    override val menuId: Int?
        get() = if (scenesLayout.isVisible) R.menu.menu_edit_done_toggle else null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<TextView>(R.id.sectionName).text = getString(R.string.scenes_all)
        noScenesLayout = view.findViewById(R.id.noScenesLayoutGroup)
        scenesLayout = view.findViewById(R.id.scenesLayoutGroup)
        sceneList = view.findViewById(R.id.scenesList)
        sceneSectionCount = view.findViewById(R.id.sectionCount)
        sceneListAdapter = SceneListAdapter(this, requireFragmentManager())
        sceneList.adapter = sceneListAdapter
        viewModel.viewState.observe(
            viewLifecycleOwner,
            Observer<ViewState<SceneListItems>> {
                when (it) {
                    is ViewState.Loaded -> {
                        when (it.item) {
                            is SceneListItems.Empty -> initializeNoScenesView()
                            is SceneListItems.SceneItems -> initializeScenesView(it.item as SceneListItems.SceneItems)
                        }
                    }
                    is ViewState.Error<*, *> -> errorIn(activity).showGenericBecauseOf(it.error)
                }
            }
        )
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        isEditMode = !isEditMode
        item.title = if (isEditMode) getString(R.string.card_menu_done) else getString(R.string.card_menu_edit)
        sceneListAdapter.isEditMode = isEditMode
        return true
    }

    override fun onResume() {
        super.onResume()
        if (!PreferenceCache.getInstance().getBoolean(PreferenceUtils.SCENES_WALKTHROUGH_DONT_SHOW_AGAIN, false)) {
            val scenesWalkThroughFragment =
                WalkthroughBaseFragment.newInstance(WalkthroughType.SCENES)
            BackstackManager.getInstance().navigateToFloatingFragment(
                scenesWalkThroughFragment,
                scenesWalkThroughFragment.javaClass.name,
                true
            )
        }
    }

    private fun initializeNoScenesView() {
        noScenesLayout.isVisible = true
        scenesLayout.isGone = true
        activity?.invalidateOptionsMenu()
    }

    private fun initializeScenesView(item: SceneListItems.SceneItems) {
        noScenesLayout.isGone = true
        scenesLayout.isVisible = true
        sceneSectionCount.text = item.count.toString()
        sceneListAdapter.submitList(item.models)
    }

    override fun onSceneCheckAreaClick(item: Scene) {
        viewModel.handleSceneCheckAreaClick(item, isEditMode)
    }

    override fun onSceneItemAreaClick(item: Scene) {
        SceneEditorSequenceController().startSequence(activity, null, item.address)
    }

    companion object {
        @JvmStatic
        fun newInstance(): SceneListFragment = SceneListFragment()
    }
}

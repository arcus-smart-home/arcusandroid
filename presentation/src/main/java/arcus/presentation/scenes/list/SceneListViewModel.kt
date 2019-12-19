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
package arcus.presentation.scenes.list

import arcus.cornea.CorneaClientFactory
import arcus.cornea.helpers.chainNonNull
import arcus.cornea.provider.SceneModelProvider
import arcus.cornea.provider.SchedulerModelProvider
import arcus.presentation.common.view.ViewError
import arcus.presentation.common.view.ViewState
import arcus.presentation.common.view.ViewStateViewModel
import arcus.presentation.scenes.Scene
import arcus.presentation.scenes.SceneListItems
import com.iris.client.bean.Action
import com.iris.client.capability.Schedule
import com.iris.client.model.ModelCache
import com.iris.client.model.SceneModel
import com.iris.client.model.SchedulerModel
import com.iris.client.service.SchedulerService

// TODO: Inject...
class SceneListViewModel(
    private val sceneModelProvider: SceneModelProvider = SceneModelProvider.instance(),
    private val scheduleModelProvider: SchedulerModelProvider = SchedulerModelProvider.instance(),
    private val modelCache: ModelCache = CorneaClientFactory.getModelCache(),
    private val schedulerService: SchedulerService = CorneaClientFactory.getService(SchedulerService::class.java)
) : ViewStateViewModel<SceneListItems>() {
    override fun loadData() {
        _viewState.postValue(ViewState.Loading())
        scheduleModelProvider
            .load()
            .transform { schedulers ->
                schedulers?.map { it.target!! to it }?.toMap() ?: emptyMap()
            }
            .chainNonNull { schedulers ->
                sceneModelProvider.load().transform { Pair(it!!, schedulers) }
            }
            .onSuccess { (scenes, schedulers) ->
                val sceneList = scenes.map { it.toScene(schedulers[it.address]) }

                if (sceneList.isEmpty()) {
                    _viewState.postLoadedValue(SceneListItems.Empty)
                } else {
                    _viewState.postLoadedValue(SceneListItems.SceneItems(sceneList.size, sceneList))
                }
            }
    }

    fun handleSceneCheckAreaClick(scene: Scene, isEditMode: Boolean) {
        if (isEditMode) {
            delete(scene)
        } else {
            toggleEnabled(scene)
        }
    }

    private fun delete(scene: Scene) {
        val cache = modelCache[scene.address] as? SceneModel?
        cache?.let { model ->
            model
                .delete()
                .onSuccess { loadData() }
                .onFailure { _viewState.postValue(ViewState.Error(it, ViewError.GENERIC)) }
        } ?: _viewState.postValue(ViewState.Error(RuntimeException("Unable to locate model."), ViewError.GENERIC))
    }

    private fun toggleEnabled(scene: Scene) {
        schedulerService
            .getScheduler(scene.address)
            .chainNonNull {
                val schedulerModel = modelCache.addOrUpdate(it.scheduler) as SchedulerModel
                schedulerModel[ATTR_ENABLED] = !scene.isEnabled
                schedulerModel.commit()
            }
            .onSuccess { loadData() }
            .onFailure { _viewState.postValue(ViewState.Error(it, ViewError.GENERIC)) }
    }

    private fun SceneModel.toScene(schedulerModel: SchedulerModel?): Scene = if (schedulerModel == null) {
        toFullSceneModel(isEnabled = false, hasScheduler = false)
    } else {
        val isEnabled = schedulerModel[ATTR_ENABLED]?.toString()?.toBoolean() ?: false
        val hasScheduler = !schedulerModel.commands.isNullOrEmpty()
        toFullSceneModel(isEnabled, hasScheduler)
    }

    private fun SceneModel.toFullSceneModel(isEnabled: Boolean, hasScheduler: Boolean): Scene {
        return Scene(
            id = id.orEmpty(),
            address = address.orEmpty(),
            name = name.orEmpty(),
            isEnabled = isEnabled,
            hasSchedule = hasScheduler,
            actionCount = actionCount(),
            type = Scene.Type.from(template)
        )
    }

    private fun SceneModel.actionCount(): Int = actions?.count { !Action(it).context.isNullOrEmpty() } ?: 0

    companion object {
        private const val ATTR_ENABLED = "${Schedule.ATTR_ENABLED}:FIRE"
    }
}

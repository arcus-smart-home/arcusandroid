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
package arcus.presentation.cameras.storage

import arcus.cornea.CorneaClientFactory
import arcus.cornea.SessionController
import arcus.cornea.helpers.onFailureMain
import arcus.cornea.helpers.onSuccessMain
import arcus.cornea.subsystem.BaseSubsystemController
import arcus.cornea.subsystem.SubsystemController
import arcus.cornea.utils.ModelSource
import com.iris.client.capability.CamerasSubsystem
import com.iris.client.capability.Place
import com.iris.client.model.SubsystemModel
import com.iris.client.service.VideoService

class VideoStoragePresenterImpl
    @JvmOverloads
    internal constructor(
        model: ModelSource<SubsystemModel> = SubsystemController
            .instance()
            .getSubsystemModel(CamerasSubsystem.NAMESPACE),
        private val activePlace: String? = SessionController.instance().activePlace,
        private val videoService: VideoService =  CorneaClientFactory.getService(VideoService::class.java),
        private val serviceLevel: String? = SessionController.instance().place?.get(Place.ATTR_SERVICELEVEL) as String?
    ) : BaseSubsystemController<VideoStorageView>(model),
    VideoStoragePresenter {
    override fun updateView(callback: VideoStorageView) {
        if (!isLoaded) {
            return
        }
    }

    override fun setView(view: VideoStorageView) {
        super.setCallback(view)
        serviceLevel?.let {
            when (it) {
                Place.SERVICELEVEL_BASIC -> {
                    callback?.showBasicConfiguration(BASIC_STORAGE_DAYS)
                }
                Place.SERVICELEVEL_PREMIUM_FREE,
                Place.SERVICELEVEL_PREMIUM -> {
                    callback?.showPremiumConfiguration(
                        PREMIUM_STORAGE_DAYS,
                        PINNED_CLIP_STORAGE_DAYS
                    )
                }
                else -> {
                    callback?.showBasicConfiguration(BASIC_STORAGE_DAYS)
                }
            }
        }
    }

    override fun clearView() {
        super.clearCallback()
    }

    override fun deleteAll() {
        deleteClips(true)
    }

    override fun cleanUp() {
        deleteClips(false)
    }

    private fun deleteClips(includeFavorites: Boolean) {
        videoService
            .deleteAll(activePlace, includeFavorites)
            .onFailureMain { throwable ->
                callbackRef.get()?.showError(throwable)
            }
            .onSuccessMain { _ ->
                callbackRef.get()?.deleteAllSuccess()
            }
    }

    companion object {
        private const val BASIC_STORAGE_DAYS = 1
        private const val PREMIUM_STORAGE_DAYS = 30
        private const val PINNED_CLIP_STORAGE_DAYS = 150
    }
}

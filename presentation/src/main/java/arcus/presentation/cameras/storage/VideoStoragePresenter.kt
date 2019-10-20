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

import arcus.cornea.presenter.BasePresenterContract

interface VideoStoragePresenter : BasePresenterContract<VideoStorageView> {
    /**
     * Calls the video service to delete all clips.
     */
    fun deleteAll()

    /**
     * Calls the video service to delete all but favorited (pinned) clips.
     */
    fun cleanUp()
}

interface VideoStorageView {
    /**
     * Called when there is a request in flight to the server.
     */
    fun onLoading()

    /**
     * Invoked to indicate the view should show the Basic user content.
     *
     * @param daysStored Number of days the videos are stored on the server before nuka-cola
     */
    fun showBasicConfiguration(daysStored: Int)

    /**
     * Invoked to indicate the view should show the Premium user content.
     *
     * @param daysStored Number of days the videos are stored on the server before nuka-cola
     * @param pinnedCap The maximum number of pinned videos the user can pin before we say no
     */
    fun showPremiumConfiguration(daysStored: Int, pinnedCap: Int)

    /**
     * Invoked to indicate the view should show the Premium Promon user content.
     *
     * @param daysStored Number of days the videos are stored on the server before nuka-cola
     * @param pinnedCap The maximum number of pinned videos the user can pin before we say no
     */
    fun showPremiumPromonConfiguration(daysStored: Int, pinnedCap: Int)

    /**
     * Invoked when the last delete call was successful.
     */
    fun deleteAllSuccess()

    /**
     * Called when the last call resulted in an error.
     */
    fun showError(throwable: Throwable)
}

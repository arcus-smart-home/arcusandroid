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
package arcus.app.createaccount.almostfinished

import arcus.cornea.CorneaClientFactory
import arcus.cornea.utils.Listeners
import com.iris.client.IrisClient
import com.iris.client.event.Futures
import com.iris.client.model.ModelCache
import arcus.app.createaccount.AbstractPresenter

class AlmostFinishedPresenterImpl(
    private val client : IrisClient = CorneaClientFactory.getClient(),
    private val cache  : ModelCache = CorneaClientFactory.getModelCache()
) : AbstractPresenter<AlmostFinishedView>(), AlmostFinishedPresenter {
    override fun logout() {
        client
            .logout()
            .chain {
                cache.clearCache()
                Futures.succeededFuture(true)
            }
            .onCompletion(Listeners.runOnUiThread {
                onlyIfView { view ->
                    view.onLoggedOut()
                }
            })
    }
}
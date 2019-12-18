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
package arcus.presentation.pairing.hub

import arcus.cornea.helpers.onFailureMain
import arcus.cornea.helpers.onSuccessMain
import arcus.cornea.presenter.KBasePresenter
import arcus.cornea.subsystem.pairing.PairingSubsystemController
import arcus.cornea.subsystem.pairing.PairingSubsystemControllerImpl
import org.slf4j.LoggerFactory

class HubKitCheckPresenterImpl(
    private val pairingSubsystemController: PairingSubsystemController = PairingSubsystemControllerImpl
) : HubKitCheckPresenter, KBasePresenter<HubKitCheckView>() {
    override fun checkIfHubHasKitItems() {
        pairingSubsystemController
            .getKitInformation()
            .onSuccessMain { kitItems ->
                onlyIfView { view ->
                    view.onHubHasKitItems(kitItems.isNotEmpty())
                }
            }
            .onFailureMain {
                onlyIfView { view ->
                    view.onHubHasKitItems(false)
                }

                logger.error("Failed getting kit devices for the hub.", it)
            }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(HubKitCheckPresenterImpl::class.java)
    }
}

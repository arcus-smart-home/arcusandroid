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
package arcus.presentation.pairing.device.factoryreset

import arcus.cornea.presenter.KBasePresenter
import arcus.cornea.provider.ProductModelProvider
import arcus.cornea.subsystem.pairing.PairingSubsystemController
import arcus.cornea.subsystem.pairing.PairingSubsystemControllerImpl
import arcus.cornea.utils.Listeners
import com.iris.client.capability.PairingSubsystem
import com.iris.client.event.ClientFuture
import com.iris.client.model.ModelChangedEvent

class FactoryResetWarningPresenterImpl :
    FactoryResetWarningPresenter,
    KBasePresenter<FactoryResetWarningView>() {

    private val pairingSubsystemController: PairingSubsystemController = PairingSubsystemControllerImpl
    private val changedListener = Listeners.runOnUiThread<ModelChangedEvent> {
        it.changedAttributes.forEach { entry ->
            when (entry.key) {
                PairingSubsystem.ATTR_PAIRINGMODE -> {
                    if (entry.value == PairingSubsystem.PAIRINGMODE_IDLE) {
                        onlyIfView { view ->
                            if (!hasFactoryResetStarted) {
                                view.onPairingModeTimedOut()
                            }
                        }
                    }
                }
            }
        }
    }
    private var controllerListener = Listeners.empty()
    private var hasFactoryResetStarted: Boolean = false

    override fun setView(view: FactoryResetWarningView) {
        clearView()
        super.setView(view)

        controllerListener = pairingSubsystemController.setChangedListenerFor(
            changedListener,
            PairingSubsystem.ATTR_PAIRINGMODE
        )
        hasFactoryResetStarted = false
    }

    override fun factoryReset() {
        hasFactoryResetStarted = true
        pairingSubsystemController.getFactoryResetSteps()
            .onSuccessMain {
                val factoryResetStepList = arrayListOf<FactoryResetStep>()
                it.run {
                    this.resetSteps.forEach {
                        factoryResetStepList.add(
                            FactoryResetStep(
                                it.id,
                                it.info,
                                it.instructions,
                                it.title
                            )
                        )
                    }
                }
                onlyIfView { presentedView ->
                    presentedView.onFactoryResetStarted(factoryResetStepList)
                }
            }
            .onFailureMain {
                onlyIfView { presentedView ->
                    presentedView.onGetResetStepsError(it)
                }
            }
    }

    override fun getProductName() {
        val searchContext = pairingSubsystemController.getSearchContext()
        val productModel = ProductModelProvider.instance().getModel(searchContext)
        productModel
            .load()
            .onSuccess {
                onlyIfView { presentedView ->
                    presentedView.onProductNameRetrieved(it.vendor + " " + it.shortName)
                }
            }
            .onFailure {
                onlyIfView { presentedView ->
                    presentedView.onProductNameRetrieved("Device")
                }
            }
    }

    override fun isInPairingMode(): Boolean {
        return pairingSubsystemController.isInPairingMode()
    }

    override fun clearView() {
        super.clearView()
        Listeners.clear(controllerListener)
    }

    private inline fun <T> ClientFuture<T>.onSuccessMain(crossinline handler: (T) -> Unit): ClientFuture<T> {
        return this.onSuccess(Listeners.runOnUiThread {
            handler(it)
        })
    }

    private inline fun <T> ClientFuture<T>.onFailureMain(crossinline handler: (Throwable) -> Unit): ClientFuture<T> {
        return this.onFailure(Listeners.runOnUiThread {
            handler(it)
        })
    }
}

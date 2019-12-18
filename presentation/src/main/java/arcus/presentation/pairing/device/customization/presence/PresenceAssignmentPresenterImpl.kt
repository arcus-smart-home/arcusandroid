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
package arcus.presentation.pairing.device.customization.presence

import arcus.cornea.presenter.KBasePresenter
import arcus.cornea.provider.DeviceModelProvider
import arcus.cornea.provider.PairingDeviceModelProvider
import arcus.cornea.provider.PersonModelProvider
import arcus.cornea.utils.Listeners
import com.iris.client.capability.Presence
import com.iris.client.event.Futures
import com.iris.client.model.DeviceModel
import com.iris.client.model.PersonModel

class PresenceAssignmentPresenterImpl : PresenceAssignmentPresenter, KBasePresenter<PresenceAssignmentView>() {
    private val UNASSIGNED_NAME = "UNSET"
    private val errorListener = Listeners.runOnUiThread<Throwable> { error ->
        onlyIfView { view ->
            view.showError(error)
        }
    }
    private var deviceModel: DeviceModel? = null

    override fun setAssignment(to: AssignmentOption) {
        when (to) {
            is PersonAssignmentOption -> { assignPersonToDevice(to.personAddress) }
            is UnassignedAssignmentOption -> { unassignDevice() }
        }
    }

    override fun loadFromPairingDevice(pairedDeviceAddress: String) {
        PersonModelProvider
                .instance()
                .load()
                .chain {
                    PairingDeviceModelProvider
                            .instance()
                            .getModel(pairedDeviceAddress)
                            .load()
                }
                .chain { model ->
                    model?.deviceAddress?.let { address ->
                        DeviceModelProvider
                                .instance()
                                .getModel(address)
                                .load()
                    } ?: Futures.failedFuture(RuntimeException("Cannot load null pairing/device model."))
                }
                .transform {
                    it?.let {
                        deviceModel = it

                        // Create Assignment Options Here
                        val assignmentOptionList = mutableListOf<AssignmentOption>()
                        assignmentOptionList.add(
                            UnassignedAssignmentOption(
                                "Unassigned"
                            )
                        )
                        PersonModelProvider
                                .instance()
                                .store
                                .values()
                                .sortedWith(
                                        compareBy<PersonModel, String>(String.CASE_INSENSITIVE_ORDER) {
                                                it.firstName ?: ""
                                            }
                                            .thenBy {
                                                it.lastName ?: ""
                                            }
                                )
                                .mapTo(assignmentOptionList) {
                                    PersonAssignmentOption(
                                        it.address,
                                        "${it.firstName} ${it.lastName}"
                                    )
                                }
                        Pair(assignmentOptionList, it)
                    } ?: throw RuntimeException("Transform value was null.")
                }
                .transform {
                    it?.let {
                        // Get the current person assignment figured out here
                        val personAssignment = it.second[Presence.ATTR_PERSON] as String? ?: ""
                        val assignedTo = PersonModelProvider
                                .instance()
                                .store
                                .get(personAssignment)

                        val currentAssignment = if (assignedTo == null) {
                            UnassignedAssignmentOption(
                                UNASSIGNED_NAME
                            )
                        } else {
                            PersonAssignmentOption(
                                assignedTo.address,
                                "${assignedTo.firstName} ${assignedTo.lastName}"
                            )
                        }

                        Triple(it.first, currentAssignment, it.second.name ?: "")
                    } ?: throw RuntimeException("Transform value was null.")
                }
                .onSuccess(Listeners.runOnUiThread {
                    onlyIfView { presentedView ->
                        presentedView.onAssignmentOptionsLoaded(it.first, it.second, it.third)
                    }
                })
                .onFailure(errorListener)
    }

    private fun unassignDevice() {
        (deviceModel as? Presence)?.let {
            it.person = UNASSIGNED_NAME
            it.usehint = Presence.USEHINT_UNKNOWN
            deviceModel?.commit()?.onFailure(errorListener)
        }
    }

    private fun assignPersonToDevice(personAddress: String) {
        (deviceModel as? Presence)?.let {
            it.person = personAddress
            it.usehint = Presence.USEHINT_PERSON
            deviceModel?.commit()?.onFailure(errorListener)
        }
    }
}

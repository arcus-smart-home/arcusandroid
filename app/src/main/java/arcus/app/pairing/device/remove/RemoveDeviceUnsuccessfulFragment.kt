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
package arcus.app.pairing.device.remove

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import arcus.app.R
import arcus.app.common.utils.ActivityUtils
import arcus.app.common.fragment.TitledFragment
import arcus.presentation.pairing.device.remove.force.ForceRemoveDevicePresenter
import arcus.presentation.pairing.device.remove.force.ForceRemoveDevicePresenterImpl
import arcus.presentation.pairing.device.remove.force.ForceRemoveDeviceView
import arcus.app.pairing.device.remove.force.ForceRemoveImproperlyPairedSuccessFragment
import arcus.app.pairing.device.remove.instructions.RemoveDeviceInstructionsFragment
import arcus.app.pairing.device.remove.popups.ConfirmForceRemovePopup
import arcus.presentation.pairing.device.remove.DeviceRemovalStep

class RemoveDeviceUnsuccessfulFragment : Fragment(),
    TitledFragment,
    ForceRemoveDeviceView {
    private var pairingDeviceAddress: String = ""
    private val presenter : ForceRemoveDevicePresenter =
        ForceRemoveDevicePresenterImpl()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { bundle ->
            pairingDeviceAddress = bundle.getString(ARG_PAIRING_DEVICE_ADDRESS)!!
        }
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_device_removal_unsuccessful, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        presenter.setView(this)

        view.findViewById<View>(R.id.call_support).setOnClickListener {
            ActivityUtils.callSupport()
        }

        view.findViewById<View>(R.id.next_button).setOnClickListener {
            presenter.retryRemove(pairingDeviceAddress)
        }

        view.findViewById<View>(R.id.cancel_button).setOnClickListener{
            val popup = ConfirmForceRemovePopup()
            popup.clickedForceRemoveListener = {
                presenter.forceRemove(pairingDeviceAddress)
            }

            popup.show(fragmentManager)
        }

        view.findViewById<View>(R.id.cancel).setOnClickListener {
            activity?.finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.clearView()
    }

    override fun onForceRemoveSuccess() {
        fragmentManager
            ?.beginTransaction()
            ?.replace(
                    R.id.container,
                    ForceRemoveImproperlyPairedSuccessFragment.newInstance()
            )
            ?.commit()
    }

    override fun onForceRemoveFailed() {
        // NO-OP
    }

    override fun onRetryRemoveFailed() {
        // NO-OP
    }

    override fun onRetryRemoveSuccess(steps: List<DeviceRemovalStep>) {
        fragmentManager
                ?.beginTransaction()
                ?.replace(
                        R.id.container,
                        RemoveDeviceInstructionsFragment.newInstance(pairingDeviceAddress, steps)
                )
                ?.commit()

    }

    override fun getTitle(): String = resources.getString(R.string.remove_device_title)

    companion object {
        const val ARG_PAIRING_DEVICE_ADDRESS = "ARG_PAIRING_DEVICE_ADDRESS"

        @JvmStatic
        fun newInstance(pairingDeviceAddress: String) : RemoveDeviceUnsuccessfulFragment {
            val fragment = RemoveDeviceUnsuccessfulFragment()
            with (fragment) {
                val args = android.os.Bundle()
                args.putString(ARG_PAIRING_DEVICE_ADDRESS, pairingDeviceAddress)
                arguments = args
                retainInstance = true
            }
            return fragment
        }
    }
}

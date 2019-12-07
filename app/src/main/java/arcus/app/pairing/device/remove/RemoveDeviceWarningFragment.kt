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
import arcus.app.common.utils.commitAndExecute
import arcus.app.common.utils.enterFromRightExitToRight
import android.widget.Button
import arcus.app.common.view.ScleraTextView
import arcus.app.common.fragment.TitledFragment
import arcus.app.pairing.device.remove.instructions.RemoveDeviceInstructionsFragment
import arcus.app.pairing.device.remove.popups.ConfirmRemovePopup
import arcus.presentation.pairing.device.remove.DeviceRemovalStep
import arcus.presentation.pairing.device.remove.RemoveDevicePresenter
import arcus.presentation.pairing.device.remove.RemoveDevicePresenterImpl
import arcus.presentation.pairing.device.remove.RemoveDeviceView

class RemoveDeviceWarningFragment : Fragment(),
    TitledFragment,
    RemoveDeviceView {
    private var pairingDeviceAddress : String = ""
    private val presenter : RemoveDevicePresenter =
        RemoveDevicePresenterImpl()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_remove_device_warning, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        presenter.setView(this)
        arguments?.let {
            pairingDeviceAddress = it.getString(ARG_DEVICE_ADDRESS)!!
        }

        presenter.checkForMispairedHue(pairingDeviceAddress)

        view.findViewById<Button>(R.id.ok_button).setOnClickListener {
            val popup = ConfirmRemovePopup()
            popup.clickedRemoveListener = {
                presenter.removePairingDevice(pairingDeviceAddress)
            }
            popup.show(fragmentManager)
        }

        view.findViewById<Button>(R.id.cancel_button).setOnClickListener {
            activity?.finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.clearView()
    }

    override fun onRemoveFailed() {
        fragmentManager?.let {
            it.beginTransaction()
                .replace(
                        R.id.container,
                    RemoveDeviceUnsuccessfulFragment.newInstance(pairingDeviceAddress)
                )
                .enterFromRightExitToRight()
                .commitAndExecute(it)
        }
    }

    override fun getTitle() = getString(R.string.remove)

    override fun onRemovalStepsLoaded(steps: List<DeviceRemovalStep>) {
        fragmentManager?.let {
            it.beginTransaction()
                    .replace(
                            R.id.container,
                            RemoveDeviceInstructionsFragment.newInstance(pairingDeviceAddress, steps)
                    )
                    .enterFromRightExitToRight()
                    .commitAndExecute(it)
        }
    }

    override fun onHueDeviceMispaired(shortName: String) {
        view?.let {
            it.findViewById<ScleraTextView>(R.id.remove_subtitle)
                    .text = getString(R.string.remove_improperly_paired_hue_description, shortName)
        }
    }

    companion object {
        private const val ARG_DEVICE_ADDRESS = "ARG_DEVICE_ADDRESS"

        @JvmStatic
        fun newInstance(pairingDeviceAddress : String) : RemoveDeviceWarningFragment {
            val bundle = Bundle()
            val fragment = RemoveDeviceWarningFragment()
            bundle.putString(ARG_DEVICE_ADDRESS, pairingDeviceAddress)
            fragment.arguments = bundle
            fragment.retainInstance = true
            return fragment
        }
    }
}

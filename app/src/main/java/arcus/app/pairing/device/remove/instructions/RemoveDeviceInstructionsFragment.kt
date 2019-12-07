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
package arcus.app.pairing.device.remove.instructions

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import arcus.app.R
import arcus.app.common.utils.enterFromRightExitToRight
import android.widget.Button
import arcus.app.common.view.ScleraTextView
import arcus.app.common.fragment.TitledFragment
import arcus.presentation.pairing.device.remove.DeviceRemovalStep
import arcus.app.pairing.device.remove.DeviceRemovalStepsAdapter
import arcus.app.pairing.device.remove.RemoveDeviceUnsuccessfulFragment
import arcus.app.pairing.device.remove.RemoveImproperlyPairedSuccessFragment
import arcus.presentation.pairing.device.remove.instructions.AndroidRemoveDeviceInstructionsPresenterImpl
import arcus.presentation.pairing.device.remove.instructions.RemoveDeviceInstructionsPresenter
import arcus.presentation.pairing.device.remove.instructions.RemoveDeviceInstructionsView
import kotlin.properties.Delegates

class RemoveDeviceInstructionsFragment : Fragment(),
    TitledFragment, RemoveDeviceInstructionsView {
    private var pairingDeviceAddress: String = ""
    private var steps by Delegates.notNull<List<DeviceRemovalStep>>()

    private val presenter : RemoveDeviceInstructionsPresenter = AndroidRemoveDeviceInstructionsPresenterImpl()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_remove_device_instructions, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let {
            @Suppress("UNCHECKED_CAST")
            pairingDeviceAddress = it.getString(ARG_PAIRING_DEVICE_ADDRESS)!!
            steps = it.getParcelableArrayList(ARG_REMOVE_INSTRUCTIONS)!!

            if(steps.isEmpty() || steps[0].instructions.isEmpty()){
                view.findViewById<ScleraTextView>(R.id.alternate).visibility = View.VISIBLE
                view.findViewById<ScleraTextView>(R.id.title).visibility = View.GONE
            }
            else {
                view.findViewById<ScleraTextView>(R.id.alternate).visibility = View.GONE
                view.findViewById<ScleraTextView>(R.id.title).visibility = View.VISIBLE

                view.findViewById<ScleraTextView>(R.id.title).text = getString(R.string.attempting_to_remove_device)

                val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view)
                recyclerView.layoutManager = LinearLayoutManager(view.context)
                activity?.let {
                    recyclerView.adapter = DeviceRemovalStepsAdapter(it, steps)
                }
            }

            view.findViewById<Button>(R.id.cancel_button).setOnClickListener {
                activity?.finish()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        presenter.setView(this)
        presenter.loadDeviceFromPairingAddress(pairingDeviceAddress)
    }
    override fun onDeviceRemoved() {
        fragmentManager?.let {
            it.beginTransaction()
                    .replace(
                            R.id.container,
                            RemoveImproperlyPairedSuccessFragment.newInstance()
                    )
                    .enterFromRightExitToRight()
                    .commit()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.clearView()
    }

    override fun getTitle() = getString(R.string.remove_device_title)

    override fun onDeviceRemoveFailed() {
        fragmentManager?.let {
            it.beginTransaction()
                    .replace(
                            R.id.container,
                            RemoveDeviceUnsuccessfulFragment.newInstance(pairingDeviceAddress)
                    )
                    .enterFromRightExitToRight()
                    .commit()
        }
    }

    companion object {
        private const val ARG_PAIRING_DEVICE_ADDRESS = "ARG_PAIRING_DEVICE_ADDRESS"
        private const val ARG_REMOVE_INSTRUCTIONS = "ARG_REMOVE_INSTRUCTIONS"

        @JvmStatic
        fun newInstance(
                pairingDeviceAddress: String,
                steps: List<DeviceRemovalStep>
        ) = RemoveDeviceInstructionsFragment().also { fragment ->
            fragment.arguments = createArgumentBundle(
                    pairingDeviceAddress,
                    steps
            )
        }

        @JvmStatic
    fun createArgumentBundle(
                pairingDeviceAddress: String,
                steps: List<DeviceRemovalStep>
        ) = Bundle().also { args ->
            args.putString(ARG_PAIRING_DEVICE_ADDRESS, pairingDeviceAddress)
            args.putParcelableArrayList(ARG_REMOVE_INSTRUCTIONS, ArrayList(steps))
        }
    }
}

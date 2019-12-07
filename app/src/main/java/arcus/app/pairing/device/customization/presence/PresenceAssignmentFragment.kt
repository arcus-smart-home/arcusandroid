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
package arcus.app.pairing.device.customization.presence

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import arcus.app.R
import android.widget.Button
import arcus.app.common.view.ScleraTextView
import arcus.app.pairing.device.customization.CustomizationNavigationDelegate
import arcus.presentation.pairing.device.customization.CustomizationStep
import arcus.presentation.pairing.device.customization.CustomizationType
import arcus.app.common.fragment.TitledFragment
import arcus.presentation.pairing.device.customization.presence.*
import org.slf4j.LoggerFactory


class PresenceAssignmentFragment : Fragment(),
    TitledFragment,
    PresenceAssignmentView,
    PresenceAssignmentAdapter.PresenceAssignmentAdapterCallback {

    private val UNASSIGNED_NAME = "UNSET"

    private var pairingDeviceAddress: String? = null
    private var customizationStep: CustomizationStep? = null
    private var nextButtonText: Int = R.string.pairing_next
    private var showCancelButton: Boolean = false
    private var presenceAssignmentOption : AssignmentOption =
        UnassignedAssignmentOption(
            UNASSIGNED_NAME
        )

    private lateinit var assignmentOptionTitle: ScleraTextView
    private lateinit var assignmentOptionDescription: ScleraTextView
    private lateinit var nextButton: Button
    private lateinit var cancelButton: Button
    private lateinit var presenceRecyclerView : RecyclerView
    private lateinit var presenceAssignmentAdapter: PresenceAssignmentAdapter
    private lateinit var presenceAssignmentsList : List<AssignmentOption>

    private lateinit var mCallback: CustomizationNavigationDelegate
    private lateinit var presenceAdapterCallback : PresenceAssignmentAdapter.PresenceAssignmentAdapterCallback
    private val presenter : PresenceAssignmentPresenter =
        PresenceAssignmentPresenterImpl()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let{ bundle ->
            pairingDeviceAddress = bundle.getString(ARG_PAIRING_DEVICE_ADDRESS)
            customizationStep = bundle.getParcelable(ARG_ASSIGNMENT_OPTION_STEP)
            showCancelButton = bundle.getBoolean(ARG_CANCEL_BUTTON_PRESENT)
            nextButtonText = bundle.getInt(ARG_NEXT_BUTTON_TEXT)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_presence_assignment, container, false)

        assignmentOptionTitle = view.findViewById(R.id.customization_assignment_option_title)
        assignmentOptionDescription = view.findViewById(R.id.customization_assignment_option_desc)

        presenceRecyclerView = view.findViewById(R.id.customization_assignment_recycler_view)
        presenceRecyclerView.layoutManager = LinearLayoutManager(activity)
        presenceRecyclerView.setHasFixedSize(false)

        nextButton = view.findViewById(R.id.next_button)
        nextButton.setText(nextButtonText)
        nextButton.setOnClickListener {
            presenter.setAssignment(presenceAssignmentOption)
            mCallback.navigateForwardAndComplete(CustomizationType.CONTACT_TYPE)
        }

        cancelButton = view.findViewById(R.id.cancel_button)
        if(showCancelButton){
            cancelButton.visibility = View.VISIBLE
            cancelButton.setOnClickListener {
                mCallback.cancelCustomization()
            }
        } else {
            cancelButton.visibility = View.GONE
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        presenter.setView(this)

        pairingDeviceAddress?.let {
            presenter.loadFromPairingDevice(it)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        presenceAdapterCallback = this
        activity?.let {
            try {
                mCallback = it as CustomizationNavigationDelegate
            } catch (exception: ClassCastException) {
                logger.debug(it.toString() +
                        " must implement CustomizationNavigationDelegate: \n" +
                        exception.message)
                throw (exception)
            }
        }
    }

    override fun onSelectionChanged() {
        presenceAssignmentOption = presenceAssignmentsList[presenceAssignmentAdapter.selectedPosition]
    }

    override fun getTitle() = customizationStep?.header ?: getString(R.string.customization_assignment_option_header)

    override fun onAssignmentOptionsLoaded(options: List<AssignmentOption>, selected: AssignmentOption, deviceName: String) {
        presenceAssignmentsList = options
        assignmentOptionTitle.text = customizationStep?.title ?: getString(R.string.customization_assignment_option_title, deviceName)

        customizationStep?.description?.let {
            if (it.isNotEmpty()) {
                assignmentOptionDescription.text = it.joinToString(separator = "\n\n")
            } else {
                assignmentOptionDescription.text = getString(R.string.customization_assignment_option_desc)
            }
        }

        val adapter = presenceRecyclerView.adapter
        activity?.let{
            presenceAssignmentAdapter  =
                    PresenceAssignmentAdapter(
                        options,
                        presenceAdapterCallback
                    )
        }
        if (adapter != null && presenceAssignmentAdapter != null) {
            presenceRecyclerView.swapAdapter(presenceAssignmentAdapter, true)
        } else {
            presenceRecyclerView.adapter = presenceAssignmentAdapter
        }
    }

    override fun showError(throwable: Throwable) {
        logger.error("Assignment Option Customization", "Received error: ", throwable)
    }

    companion object {
        const val ARG_PAIRING_DEVICE_ADDRESS = "ARG_PAIRING_DEVICE_ADDRESS"
        const val ARG_ASSIGNMENT_OPTION_STEP = "ARG_ASSIGNMENT_OPTION_STEP"
        const val ARG_CANCEL_BUTTON_PRESENT = "ARG_CANCEL_BUTTON_PRESENT"
        const val ARG_NEXT_BUTTON_TEXT = "ARG_NEXT_BUTTON_TEXT"

        @JvmStatic
        private val logger = LoggerFactory.getLogger(PresenceAssignmentFragment::class.java)

        @JvmStatic
        fun newInstance(pairingDeviceAddress: String, step: CustomizationStep, cancelPresent: Boolean, nextButtonText: Int) : PresenceAssignmentFragment {
            val fragment =
                PresenceAssignmentFragment()
            with (fragment) {
                val args = Bundle()
                args.putString(ARG_PAIRING_DEVICE_ADDRESS, pairingDeviceAddress)
                args.putParcelable(ARG_ASSIGNMENT_OPTION_STEP, step)
                args.putBoolean(ARG_CANCEL_BUTTON_PRESENT, cancelPresent)
                args.putInt(ARG_NEXT_BUTTON_TEXT, nextButtonText)
                arguments = args
                retainInstance = true
            }
            return fragment
        }
    }

}

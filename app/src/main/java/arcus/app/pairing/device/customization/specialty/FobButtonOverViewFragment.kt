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
package arcus.app.pairing.device.customization.specialty

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
import arcus.app.common.fragment.TitledFragment
import arcus.presentation.pairing.device.customization.CustomizationStep
import arcus.presentation.pairing.device.customization.CustomizationType
import org.slf4j.LoggerFactory


class FobButtonOverviewFragment : Fragment(),
    TitledFragment, FobButtonOverviewView {

    private var pairingDeviceAddress: String? = null
    private var customizationStep: CustomizationStep? = null
    private var nextButtonText: Int = R.string.pairing_next
    private var showCancelButton: Boolean = false

    private lateinit var buttonOverviewTitle: ScleraTextView
    private lateinit var nextButton: Button
    private lateinit var cancelButton: Button
    private lateinit var buttonOverviewRecyclerView: RecyclerView
    private lateinit var buttonOverviewAdapter: FobButtonOverviewAdapter
    private lateinit var mCallback: CustomizationNavigationDelegate
    private lateinit var presenter : FobButtonOverviewPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let{ bundle ->
            pairingDeviceAddress = bundle.getString(ARG_PAIRING_DEVICE_ADDRESS)
            customizationStep = bundle.getParcelable(ARG_CUSTOMIZATION_STEP)
            showCancelButton = bundle.getBoolean(ARG_CANCEL_BUTTON_PRESENT)
            nextButtonText = bundle.getInt(ARG_NEXT_BUTTON_TEXT)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_fob_button_overview, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        buttonOverviewTitle = view.findViewById(R.id.fob_button_overview_title)

        buttonOverviewRecyclerView = view.findViewById(R.id.fob_button_overview_recycler_view)
        buttonOverviewRecyclerView.layoutManager = LinearLayoutManager(activity)
        buttonOverviewRecyclerView.setHasFixedSize(false)

        nextButton = view.findViewById(R.id.next_button)
        nextButton.setText(nextButtonText)
        nextButton.setOnClickListener {
            mCallback.navigateForwardAndComplete(CustomizationType.MULTI_BUTTON_ASSIGNMENT)
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
        activity?.let {
            try {
                mCallback = it as CustomizationNavigationDelegate
            } catch (exception: ClassCastException) {
                logger.debug(it.toString() +
                        " must implement CustomizationNavigationDelegate: \n" +
                        exception.message)
                throw (exception)
            }
            presenter = FobButtonOverviewPresenterImpl(it)
        }
    }

    override fun getTitle() = customizationStep?.header ?: getString(R.string.fob_button_overview_header)

    override fun onButtonsLoaded(buttons: List<ButtonWithAction>, deviceAddress: String) {
        buttonOverviewTitle.text = customizationStep?.title ?: getString(R.string.fob_button_overview_title)

        val adapter = buttonOverviewRecyclerView.adapter
        activity?.let{
            buttonOverviewAdapter  = FobButtonOverviewAdapter(deviceAddress, buttons, it)
        }
        if (adapter != null && buttonOverviewAdapter != null) {
            buttonOverviewRecyclerView.swapAdapter(buttonOverviewAdapter, true)
        } else {
            buttonOverviewRecyclerView.adapter = buttonOverviewAdapter
        }
   }

    override fun showError(throwable: Throwable) {
        logger.error("Fob Buttons Overview", "Received error: ", throwable)
    }

    companion object {
        const val ARG_PAIRING_DEVICE_ADDRESS = "ARG_PAIRING_DEVICE_ADDRESS"
        const val ARG_CUSTOMIZATION_STEP = "ARG_CUSTOMIZATION_STEP"
        const val ARG_CANCEL_BUTTON_PRESENT = "ARG_CANCEL_BUTTON_PRESENT"
        const val ARG_NEXT_BUTTON_TEXT = "ARG_NEXT_BUTTON_TEXT"

        @JvmStatic
        private val logger = LoggerFactory.getLogger(FobButtonOverviewFragment::class.java)

        @JvmStatic
        fun newInstance(pairingDeviceAddress: String, step: CustomizationStep, cancelPresent: Boolean, nextButtonText: Int) : FobButtonOverviewFragment {
            val fragment = FobButtonOverviewFragment()
            with (fragment) {
                val args = Bundle()
                args.putString(ARG_PAIRING_DEVICE_ADDRESS, pairingDeviceAddress)
                args.putParcelable(ARG_CUSTOMIZATION_STEP, step)
                args.putBoolean(ARG_CANCEL_BUTTON_PRESENT, cancelPresent)
                args.putInt(ARG_NEXT_BUTTON_TEXT, nextButtonText)
                arguments = args
                retainInstance = true
            }
            return fragment
        }
    }

}

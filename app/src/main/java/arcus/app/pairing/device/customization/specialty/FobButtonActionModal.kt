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
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import arcus.app.R
import android.widget.Button
import arcus.app.common.view.ScleraTextView
import arcus.app.device.buttons.model.ButtonAction
import arcus.app.common.fragment.TitledFragment
import org.slf4j.LoggerFactory

class FobButtonActionModal : Fragment(),
    TitledFragment, FobButtonActionView, FobButtonActionAdapter.FobButtonActionAdapterCallback {

    private val presenter : FobButtonActionPresenter = FobButtonActionPresenterImpl()

    private lateinit var buttonName: String
    private lateinit var deviceAddress: String
    private lateinit var buttonActionTitle: ScleraTextView
    private lateinit var saveButton: Button
    private lateinit var cancelButton: Button
    private lateinit var selectedAction: ButtonAction
    private lateinit var buttonActionsList: List<ButtonAction>
    private lateinit var buttonActionRecyclerView: RecyclerView
    private lateinit var buttonActionAdapter: FobButtonActionAdapter
    private lateinit var adapterCallback : FobButtonActionAdapter.FobButtonActionAdapterCallback

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            buttonName = it.getString(ARG_BUTTON_NAME)!!
            deviceAddress = it.getString(ARG_DEVICE_ADDRESS)!!
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.popup_fob_button_action, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        buttonActionTitle = view.findViewById(R.id.fob_button_action_title)
        saveButton = view.findViewById(R.id.save_button)

        buttonActionRecyclerView = view.findViewById(R.id.fob_button_action_recycler_view)
        buttonActionRecyclerView.layoutManager = LinearLayoutManager(activity)
        buttonActionRecyclerView.setHasFixedSize(false)
        ViewCompat.setNestedScrollingEnabled(buttonActionRecyclerView, false)

        saveButton = view.findViewById(R.id.save_button)

        cancelButton = view.findViewById(R.id.cancel_button)
        cancelButton.setOnClickListener {
            activity?.finish()
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        adapterCallback = this
    }

    override fun onResume() {
        super.onResume()
        activity?.title = getTitle()
        buttonActionTitle.text = getString(R.string.fob_button_action_title, buttonName)
        presenter.setView(this)
        presenter.loadFromDeviceAddress(deviceAddress, buttonName)
    }

    override fun getTitle(): String = buttonName

    override fun onButtonsLoaded(buttonActions: List<ButtonAction>, currentActionIndex: Int) {
        buttonActionsList = buttonActions
        val adapter = buttonActionRecyclerView.adapter
        activity?.let{
            buttonActionAdapter  = FobButtonActionAdapter(currentActionIndex, buttonActions, adapterCallback, it)
        }
        if (adapter != null && buttonActionAdapter != null) {
            buttonActionRecyclerView.swapAdapter(buttonActionAdapter, true)
        } else {
            buttonActionRecyclerView.adapter = buttonActionAdapter
        }
    }

    override fun onSelectionChanged() {
        selectedAction = buttonActionsList[buttonActionAdapter.currentPosition]
        saveButton.setOnClickListener {
            presenter.setButtonSelection(buttonName, selectedAction)
        }
    }

    override fun onButtonActionSaved() {
        activity?.finish()
    }

    override fun showError(throwable: Throwable) {
        logger.error("Fob Button Action", "Received error: ", throwable)
    }

    companion object {
        private const val ARG_BUTTON_NAME = "ARG_BUTTON_NAME"
        private const val ARG_DEVICE_ADDRESS = "ARG_DEVICE_ADDRESS"

        private val logger = LoggerFactory.getLogger(FobButtonActionModal::class.java)

        @JvmStatic
        fun newInstance(
                buttonName: String,
                deviceAddress: String
        ) = FobButtonActionModal().also { fragment ->
            fragment.arguments = createArgumentBundle(
                    buttonName,
                    deviceAddress)

            fragment.retainInstance = true
        }

       @JvmStatic
        fun createArgumentBundle(
               buttonName: String,
               deviceAddress: String
       ) = Bundle().also { args ->
           args.putString(ARG_BUTTON_NAME, buttonName)
           args.putString(ARG_DEVICE_ADDRESS, deviceAddress)
       }
    }
}

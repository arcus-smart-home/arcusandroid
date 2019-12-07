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
package arcus.app.pairing.device.customization.halo.room

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
import arcus.app.pairing.device.customization.CustomizationNavigationDelegate
import arcus.app.pairing.device.customization.favorite.FavoritesFragment
import arcus.app.common.fragment.TitledFragment
import arcus.presentation.pairing.device.customization.CustomizationStep
import arcus.presentation.pairing.device.customization.CustomizationType
import arcus.presentation.pairing.device.customization.halo.room.HaloRoom
import arcus.presentation.pairing.device.customization.halo.room.HaloRoomContract
import arcus.presentation.pairing.device.customization.halo.room.HaloRoomPresenterImpl
import org.slf4j.LoggerFactory

class HaloRoomFragment : Fragment(),
    TitledFragment,
    HaloRoomContract.View,
    HaloRoomAdapter.HaloRoomAdapterCallback {

    private lateinit var pairingDeviceAddress: String
    private lateinit var customizationStep: CustomizationStep
    private lateinit var stepTitle: ScleraTextView
    private lateinit var stepDescription: ScleraTextView
    private lateinit var nextButton: Button
    private lateinit var cancelButton: Button
    private lateinit var adapterCallback : HaloRoomAdapter.HaloRoomAdapterCallback
    private lateinit var mCallback: CustomizationNavigationDelegate
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter : HaloRoomAdapter
    private lateinit var haloRooms : List<HaloRoom>
    private val presenter : HaloRoomContract.Presenter =
        HaloRoomPresenterImpl()
    private var cancelPresent: Boolean = false
    private var nextButtonText: Int = R.string.pairing_next
    private var selectedRoom : HaloRoom? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let{ bundle ->
            pairingDeviceAddress = bundle.getString(FavoritesFragment.ARG_PAIRING_DEVICE_ADDRESS)!!
            customizationStep = bundle.getParcelable(FavoritesFragment.ARG_CUSTOMIZATION_STEP)!!
            cancelPresent = bundle.getBoolean(FavoritesFragment.ARG_CANCEL_PRESENT)
            nextButtonText = bundle.getInt(FavoritesFragment.ARG_NEXT_BUTTON_TEXT)
        }
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_halo_room, container, false)

        stepTitle = view.findViewById(R.id.step_title)
        stepDescription = view.findViewById(R.id.step_description)

        recyclerView = view.findViewById(R.id.step_modes_list)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.setHasFixedSize(false)
        ViewCompat.setNestedScrollingEnabled(recyclerView, false)

        nextButton = view.findViewById(R.id.next_btn)
        nextButton.setOnClickListener {
            mCallback.navigateForwardAndComplete(CustomizationType.ROOM)
            selectedRoom?.let {
                presenter.setRoom(it)
            }
        }

        cancelButton = view.findViewById(R.id.cancel_btn)
        if (cancelPresent) {
            cancelButton.visibility = View.VISIBLE
            cancelButton.setOnClickListener {
                mCallback.cancelCustomization()
            }
        } else {
            cancelButton.visibility = View.GONE
        }

        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        adapterCallback = this
        activity?.let {
            try {
                mCallback = it as CustomizationNavigationDelegate
            } catch (exception: ClassCastException){
                logger.debug(it.toString() +
                        " must implement CustomizationNavigationDelegate: \n" +
                        exception.message)
                throw (exception)
            }
        }
    }

    override fun onResume() {
        super.onResume()

        presenter.setView(this)
        presenter.loadFromPairingDevice(pairingDeviceAddress)

        stepTitle.text = customizationStep.title ?: getString(R.string.halo_title_default)
        stepDescription.text = if(customizationStep.description.isNotEmpty()) {
            customizationStep.description.joinToString("\n")
        } else {
            resources.getString(R.string.halo_description_default)
        }
    }

    override fun getTitle(): String = customizationStep.header ?: getString(R.string.halo_header_default)

    override fun onRoomsLoaded(rooms: List<HaloRoom>) {
        haloRooms = rooms
        val firstSelection = haloRooms.indexOf(presenter.getCurrentRoomSelection())

        adapter = HaloRoomAdapter(
            firstSelection,
            haloRooms,
            adapterCallback
        )
        recyclerView.adapter = adapter
    }

    override fun onSelectionChanged() {
        selectedRoom = haloRooms[adapter.lastSelection]
    }

    override fun onError(throwable: Throwable) {
        logger.error("Halo Room Selection Customization", "Error Received", throwable)
    }

    companion object {
        private const val ARG_PAIRING_DEVICE_ADDRESS = "ARG_PAIRING_DEVICE_ADDRESS"
        private const val ARG_CUSTOMIZATION_STEP = "ARG_CUSTOMIZATION_STEP"
        private const val ARG_CANCEL_PRESENT = "ARG_CANCEL_PRESENT"
        private const val ARG_NEXT_BUTTON_TEXT = "ARG_NEXT_BUTTON_TEXT"

        @JvmStatic
        private val logger = LoggerFactory.getLogger(HaloRoomFragment::class.java)

        @JvmStatic
        fun newInstance(pairingDeviceAddress: String,
                        step: CustomizationStep,
                        cancelPresent: Boolean = false,
                        nextButtonText: Int = R.string.pairing_next
        ) : HaloRoomFragment {
            val fragment =
                HaloRoomFragment()
            with (fragment) {
                val args = Bundle()
                args.putString(ARG_PAIRING_DEVICE_ADDRESS, pairingDeviceAddress)
                args.putParcelable(ARG_CUSTOMIZATION_STEP, step)
                args.putBoolean(ARG_CANCEL_PRESENT, cancelPresent)
                args.putInt(ARG_NEXT_BUTTON_TEXT, nextButtonText)
                arguments = args
                retainInstance = true
            }
            return fragment
        }
    }
}

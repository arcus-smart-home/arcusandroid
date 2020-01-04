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
package arcus.app.device.list

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import arcus.app.R
import arcus.app.common.backstack.BackstackManager
import arcus.app.common.fragments.CoreFragment
import arcus.app.device.details.DeviceDetailParentFragment
import arcus.app.device.list.DeviceListAdapter.ItemClickListener
import arcus.app.device.zwtools.controller.ZWaveToolsSequence
import arcus.presentation.common.view.ViewState
import arcus.presentation.device.list.DeviceListViewModel

class DeviceListingFragment : CoreFragment<DeviceListViewModel>() {
    private lateinit var deviceListAdapter: DeviceListAdapter
    private lateinit var numOfDevices: TextView

    override val title: String get() = getString(R.string.sidenav_devices_title)
    override val layoutId: Int = R.layout.fragment_device_listing
    override val viewModelClass: Class<DeviceListViewModel> = DeviceListViewModel::class.java

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        numOfDevices = view.findViewById(R.id.numberOfDevices)

        val deviceList: RecyclerView = view.findViewById(R.id.deviceList)
        deviceListAdapter = DeviceListAdapter(object : ItemClickListener {
            override fun itemClicked(position: Int) {
                BackstackManager
                        .getInstance()
                        .navigateToFragment(
                                DeviceDetailParentFragment.newInstance(position),
                                true
                        )
            }

            override fun footerClicked() {
                ZWaveToolsSequence().startSequence(activity, null)
            }
        })
        deviceList.adapter = deviceListAdapter
        viewModel.viewState.observe(viewLifecycleOwner, Observer {
            when (it) {
                is ViewState.Loaded -> {
                    val (devices, deviceListItems) = it.item
                    numOfDevices.text = devices.toString()
                    deviceListAdapter.submitList(deviceListItems)
                }
            }
        })
    }
}

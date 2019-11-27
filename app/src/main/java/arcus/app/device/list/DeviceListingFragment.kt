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
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import arcus.app.R
import arcus.app.common.backstack.BackstackManager
import arcus.app.common.fragments.BaseFragment
import arcus.app.device.details.DeviceDetailParentFragment
import arcus.app.device.list.DeviceListAdapter.ItemClickListener
import arcus.app.device.zwtools.controller.ZWaveToolsSequence

class DeviceListingFragment : BaseFragment() {
    private lateinit var deviceListAdapter: DeviceListAdapter
    private lateinit var numOfDevices: TextView
    private lateinit var viewModel: DeviceListViewModel

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
        viewModel = ViewModelProviders.of(this).get(DeviceListViewModel::class.java)
        viewModel.devices.observe(this, Observer { (devices, deviceList1) ->
            numOfDevices.text = devices.toString()
            deviceListAdapter.submitList(deviceList1)
        })
    }

    override fun onResume() {
        super.onResume()
        setTitle()
        viewModel.loadDevices()
    }

    override fun getTitle(): String = getString(R.string.sidenav_devices_title)
    override fun getLayoutId(): Int = R.layout.fragment_device_listing
}

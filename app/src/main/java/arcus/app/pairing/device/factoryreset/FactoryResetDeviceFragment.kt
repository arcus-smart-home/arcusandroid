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
package arcus.app.pairing.device.factoryreset

import android.content.Intent
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
import arcus.app.common.fragment.TitledFragment
import arcus.app.pairing.device.productcatalog.ProductCatalogActivity
import arcus.presentation.pairing.device.factoryreset.FactoryResetStep
import org.slf4j.LoggerFactory

class FactoryResetDeviceFragment : Fragment(),
    TitledFragment {

    private var factoryResetStepList: ArrayList<FactoryResetStep>? = null
    private lateinit var factoryResetTitle: ScleraTextView
    private lateinit var factoryResetButton: Button
    private lateinit var factoryResetRecyclerView: RecyclerView
    private lateinit var factoryResetAdapter: FactoryResetDeviceAdapter
    private lateinit var productName: String

    override fun getTitle() = getString(R.string.factory_reset_warning_header)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let{ bundle ->
            factoryResetStepList = bundle.getParcelableArrayList<FactoryResetStep>(
                ARG_FACTORY_RESET_STEP_LIST
            )
            productName = bundle.getString(ARG_PRODUCT_NAME)!!
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_factory_reset_device, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        factoryResetTitle = view.findViewById(R.id.fragment_factory_reset_title)
        factoryResetTitle.text = getString(R.string.factory_reset_device_title, productName)

        factoryResetRecyclerView = view.findViewById(R.id.fragment_factory_reset_recycler_view)
        factoryResetRecyclerView.layoutManager = LinearLayoutManager(activity)
        factoryResetRecyclerView.setHasFixedSize(false)

        factoryResetButton = view.findViewById(R.id.factory_reset_pair_again_button)
        factoryResetButton .setOnClickListener {
            val intent = Intent(activity, ProductCatalogActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            activity?.finish()
        }

        factoryResetRecyclerView = view.findViewById(R.id.fragment_factory_reset_recycler_view)
        val adapter = factoryResetRecyclerView.adapter

        factoryResetStepList?.let{
            factoryResetAdapter = FactoryResetDeviceAdapter(it)
        }

        if (adapter != null) {
            factoryResetRecyclerView.swapAdapter(factoryResetAdapter, false)

        } else {
            factoryResetRecyclerView.adapter = factoryResetAdapter
        }
    }

    companion object {
        private const val ARG_FACTORY_RESET_STEP_LIST = "ARG_FACTORY_RESET_STEP_LIST"
        private const val ARG_PRODUCT_NAME = "ARG_PRODUCT_NAME"

        val logger = LoggerFactory.getLogger(FactoryResetDeviceFragment::class.java)

        @JvmStatic
        fun newInstance(factoryResetStepList: ArrayList<FactoryResetStep>, productName: String) = FactoryResetDeviceFragment()
                .also { fragment ->
                    fragment.arguments = createArgumentBundle(factoryResetStepList, productName)
                    fragment.retainInstance = true
        }

        @JvmStatic
        fun createArgumentBundle(factoryResetStepList: ArrayList<FactoryResetStep>, productName: String) = Bundle()
                .also { args ->
                    args.putParcelableArrayList(ARG_FACTORY_RESET_STEP_LIST, factoryResetStepList)
                    args.putString(ARG_PRODUCT_NAME, productName)
                 }
    }

}

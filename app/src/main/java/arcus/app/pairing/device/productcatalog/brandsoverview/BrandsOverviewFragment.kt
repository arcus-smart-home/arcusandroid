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
package arcus.app.pairing.device.productcatalog.brandsoverview

import android.os.Bundle
import com.google.android.material.appbar.AppBarLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import arcus.app.R
import arcus.app.common.utils.ImageUtils
import arcus.app.common.utils.commitAndExecute
import arcus.app.common.utils.enterFromRightExitToRight
import arcus.app.common.view.ScleraTextView
import arcus.app.pairing.device.productcatalog.devicesoverview.BrandDevicesOverviewFragment
import arcus.app.pairing.device.productcatalog.popups.FilterSelection
import arcus.app.pairing.device.productcatalog.popups.ProductCatalogFilterInteraction
import arcus.presentation.pairing.device.productcatalog.brandsoverview.*

import org.slf4j.LoggerFactory

// Define the events that the fragment will use to communicate
interface BrandSelectedListener {
    // This can be any number of events to be sent to the activity
    fun onBrandSelected(brandSelected: String)
}

class BrandsOverviewFragment : Fragment(), ProductCatalogFilterInteraction,
    BrandsCatalogContractView,
    BrandSelectedListener {

    private val presenter : BrandsCatalogContractPresenter =
        BrandsOverviewPresenterImpl(
            ImageUtils.screenDensity
        )
    private var filterSelection : FilterSelection = FilterSelection.ALL_PRODUCTS
    private lateinit var resultCountContainer : AppBarLayout
    private lateinit var resultsText : ScleraTextView
    private lateinit var recyclerView : RecyclerView
    private var removeAllViews = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            filterSelection = it.getSerializable(ARG_FILTER_SELECTION) as FilterSelection
        }
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_add_a_device, container, false)
        resultCountContainer = view.findViewById(R.id.brands_result_count_container)
        resultsText = view.findViewById(R.id.brands_results_text)
        recyclerView = view.findViewById(R.id.brands_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(view.context)

        return view
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)

        savedInstanceState?.let {
            filterSelection = it.getSerializable(ARG_FILTER_SELECTION) as FilterSelection
        }
    }

    override fun onResume() {
        super.onResume()
        presenter.setView(this)

        updateFilterSelection(filterSelection, false)

        activity?.let { nonNullActivity ->
            nonNullActivity.title = getString(R.string.add_a_device)
            nonNullActivity.invalidateOptionsMenu()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putSerializable(ARG_FILTER_SELECTION, filterSelection)
    }

    override fun onPause() {
        super.onPause()
        presenter.clearView()
    }

    override fun displayBrands(count: Int, brands: List<BrandCategoryProxyModel>) {

        logger.info("displayBrands() called: {[]}", this.javaClass.canonicalName)

        activity?.let {
            resultsText.text = resources.getQuantityString(R.plurals.results_plural, count, count)
            resultCountContainer.setExpanded(true, true)

            val adapter = recyclerView.adapter
            val newAdapter = BrandsCatalogAdapter(it, brands, this)
            if(adapter != null){
                recyclerView.swapAdapter(newAdapter, removeAllViews)
            } else {
                recyclerView.adapter = newAdapter
            }
        }
    }

    override fun updateFilterSelection(selection: FilterSelection, resetPosition: Boolean) {
        removeAllViews = resetPosition
        filterSelection = selection
        when (filterSelection) {
            FilterSelection.ALL_PRODUCTS    -> presenter.getAllBrands()
            FilterSelection.HUB_REQUIRED    -> presenter.getBrandsHubFiltered(true)
            FilterSelection.NO_HUB_REQUIRED -> presenter.getBrandsHubFiltered(false)
        }
    }


    override fun onBrandSelected(brandSelected: String) {
        activity?.let {
            it.supportFragmentManager
                .beginTransaction()
                .addToBackStack(null)
                .enterFromRightExitToRight()
                .replace(
                    R.id.container,
                    BrandDevicesOverviewFragment.newInstance(brandSelected, filterSelection),
                    "BRAND_SELECTED"
                )
                .commitAndExecute(it.supportFragmentManager)
        }
    }

    companion object {
        const val ARG_FILTER_SELECTION = "ARG_FILTER_SELECTION"

    @JvmStatic
    private val logger = LoggerFactory.getLogger(BrandsOverviewFragment::class.java)

        @JvmOverloads
        @JvmStatic
        fun newInstance(
                filterSelection: FilterSelection = FilterSelection.ALL_PRODUCTS
        ): BrandsOverviewFragment {
            val fragment = BrandsOverviewFragment()
            with (fragment) {
                val args = Bundle()
                args.putSerializable(ARG_FILTER_SELECTION, filterSelection)

                arguments = args
                retainInstance = true
            }
            return fragment
        }
    }
}

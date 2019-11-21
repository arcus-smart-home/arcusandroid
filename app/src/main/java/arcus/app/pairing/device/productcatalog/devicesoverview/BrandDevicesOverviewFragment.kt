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
package arcus.app.pairing.device.productcatalog.devicesoverview

import android.os.Bundle
import com.google.android.material.appbar.AppBarLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout

import arcus.app.R
import arcus.app.common.utils.ImageUtils
import arcus.app.common.view.ScleraTextView
import arcus.app.pairing.device.productcatalog.popups.FilterSelection
import arcus.app.pairing.device.productcatalog.popups.ProductCatalogFilterInteraction
import arcus.presentation.pairing.device.productcatalog.devicesoverview.BrandDevicesOverviewPresenter
import arcus.presentation.pairing.device.productcatalog.devicesoverview.BrandDevicesOverviewPresenterImpl
import arcus.presentation.pairing.device.productcatalog.devicesoverview.BrandDevicesOverviewView
import arcus.presentation.pairing.device.productcatalog.devicesoverview.ProductEntry

class BrandDevicesOverviewFragment : Fragment(), ProductCatalogFilterInteraction,
    BrandDevicesOverviewView {
    private val presenter : BrandDevicesOverviewPresenter =
        BrandDevicesOverviewPresenterImpl(
            ImageUtils.screenDensity
        )
    private var brandSelection : String = ""
    private var filterSelection : FilterSelection = FilterSelection.ALL_PRODUCTS
    private lateinit var recyclerView : RecyclerView
    private lateinit var noResults    : LinearLayout
    private lateinit var resultCountContainer : AppBarLayout
    private lateinit var resultsText : ScleraTextView
    private var removeAllViews = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            filterSelection = it.getSerializable(ARG_FILTER_SELECTION) as FilterSelection
            brandSelection = it.getString(ARG_BRAND_SELECTION, "")
        }
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_brands_overview, container, false)
        recyclerView = view.findViewById(R.id.brands_overview_rv)
        recyclerView.layoutManager = LinearLayoutManager(view.context)
        noResults = view.findViewById(R.id.noResults)
        resultCountContainer = view.findViewById(R.id.result_count_container)
        resultsText = view.findViewById(R.id.results_text)

        return view
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)

        savedInstanceState?.let {
            filterSelection = it.getSerializable(ARG_FILTER_SELECTION) as FilterSelection
            brandSelection  = it.getString(ARG_BRAND_SELECTION, "")
        }
    }

    override fun onResume() {
        super.onResume()
        presenter.setView(this)

        updateFilterSelection(filterSelection, false)

        activity?.let { nonNullActivity ->
            nonNullActivity.title = brandSelection
            nonNullActivity.invalidateOptionsMenu()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putString(ARG_BRAND_SELECTION, brandSelection)
        outState.putSerializable(ARG_FILTER_SELECTION, filterSelection)
    }

    override fun onPause() {
        super.onPause()
        presenter.clearView()
    }

    override fun displayProducts(products: List<ProductEntry>) {
        activity?.let {
            if (products.isEmpty()) {
                resultCountContainer.visibility = View.GONE
                recyclerView.visibility = View.GONE
                recyclerView.adapter = null
                noResults.visibility = View.VISIBLE

            } else {
                resultCountContainer.visibility = View.VISIBLE
                resultCountContainer.setExpanded(true, true)
                recyclerView.visibility = View.VISIBLE
                noResults.visibility = View.GONE

                val items = products.size
                val adapter = recyclerView.adapter
                val newAdapter = BrandsOverviewAdapter(it, products)
                resultsText.text = resources.getQuantityString(R.plurals.results_plural, items, items)

                if (adapter != null) {
                    recyclerView.swapAdapter(newAdapter, removeAllViews)
                } else {
                    recyclerView.adapter = newAdapter
                }
            }
        }
    }

    override fun updateFilterSelection(selection: FilterSelection, resetPosition: Boolean) {
        removeAllViews = resetPosition
        filterSelection = selection
        when (filterSelection) {
            FilterSelection.ALL_PRODUCTS    -> presenter.getAllProductsFor(brandSelection)
            FilterSelection.HUB_REQUIRED    -> presenter.getAllProductsFor(brandSelection, true)
            FilterSelection.NO_HUB_REQUIRED -> presenter.getAllProductsFor(brandSelection, false)
        }
    }


    companion object {
        const val ARG_BRAND_SELECTION  = "ARG_BRAND_SELECTION"
        const val ARG_FILTER_SELECTION = "ARG_FILTER_SELECTION"

        @JvmOverloads
        @JvmStatic
        fun newInstance(
                brandSelection: String,
                selection: FilterSelection = FilterSelection.ALL_PRODUCTS
        ): BrandDevicesOverviewFragment {
            val fragment = BrandDevicesOverviewFragment()
            with (fragment) {
                val args = Bundle()
                args.putString(ARG_BRAND_SELECTION, brandSelection)
                args.putSerializable(ARG_FILTER_SELECTION, selection)

                arguments = args
                retainInstance = true
            }
            return fragment
        }
    }
}

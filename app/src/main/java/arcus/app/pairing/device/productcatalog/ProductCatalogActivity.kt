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
package arcus.app.pairing.device.productcatalog

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.os.Build
import android.os.Bundle
import android.os.Handler
import com.google.android.material.tabs.TabLayout
import androidx.loader.app.LoaderManager
import androidx.loader.content.Loader
import androidx.cursoradapter.widget.SimpleCursorAdapter
import androidx.appcompat.widget.SearchView
import android.view.Menu
import arcus.cornea.provider.HubModelProvider
import com.iris.client.capability.Hub
import arcus.app.R
import arcus.app.activities.ConnectedActivity
import arcus.app.activities.GenericConnectedFragmentActivity
import arcus.app.common.utils.ActivityUtils
import arcus.app.common.utils.GlobalSetting
import arcus.app.pairing.device.post.zwaveheal.ZWaveRebuildActivity
import arcus.app.pairing.device.productcatalog.brandsoverview.BrandsOverviewFragment
import arcus.app.pairing.device.productcatalog.popups.FilterSelection
import arcus.app.pairing.device.productcatalog.popups.ProductCatalogFilterInteraction
import arcus.app.pairing.device.productcatalog.popups.ProductCatalogPopupManager
import arcus.app.pairing.device.searching.DeviceSearchingActivity
import arcus.app.pairing.hub.activation.KitActivationGridFragment
import arcus.app.subsystems.camera.Generic2ButtonErrorPopup
import arcus.presentation.pairing.device.productcatalog.ProductCatalogPresenterImpl
import arcus.presentation.pairing.device.productcatalog.ProductCatalogView
import arcus.presentation.pairing.hub.activation.DeviceActivationStatus
import arcus.presentation.pairing.hub.activation.KitActivationStatusPresenterImpl
import arcus.presentation.pairing.hub.activation.KitActivationStatusView

class ProductCatalogActivity : ConnectedActivity(), ProductCatalogView, KitActivationStatusView, LoaderManager.LoaderCallbacks<Cursor> {

    private lateinit var allProductsText : String
    private lateinit var hubRequiredText : String
    private lateinit var noHubRequiredText : String
    private lateinit var searchView : SearchView

    private val presenter = ProductCatalogPresenterImpl()
    private val kitPresenter = KitActivationStatusPresenterImpl()
    private val handler = Handler()
    private val finishListener = {
        presenter.exitPairing()
    }
    private val kitDevicesListener = {
        goToGrid()
    }
    private val hubDevicesListener = {
        goToSearching()
    }

    private var kitDevices = 0
    private var hubDevices = 0
    private var suppressMustardModal = false
    private var filterSelection = FilterSelection.ALL_PRODUCTS

    private var finishToActivityIntent: Intent? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_catalog)
        setSupportActionBar(findViewById(R.id.toolbar))

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        supportFragmentManager.addOnBackStackChangedListener {
            supportFragmentManager.fragments?.lastOrNull()?.let {
                if (it is ProductCatalogFilterInteraction) {
                    it.updateFilterSelection(filterSelection, true)
                }
            }
        }

        filterSelection = intent?.getSerializableExtra(ARG_FILTER_SELECTION) as? FilterSelection ?: FilterSelection.ALL_PRODUCTS
        supportFragmentManager
                .beginTransaction()
                .addToBackStack(null)
                .replace(R.id.container, BrandsOverviewFragment.newInstance(filterSelection), "ADD_A_DEVICE")
                .commit()

        allProductsText = getString(R.string.filter_all_products)
        hubRequiredText = getString(R.string.filter_hub_required)
        noHubRequiredText = getString(R.string.filter_no_hub_required)

        val tabLayout = findViewById<TabLayout>(R.id.category_filter_items)
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab) {} // No-Op
            override fun onTabUnselected(tab: TabLayout.Tab) {} // No-Op

            override fun onTabSelected(tab: TabLayout.Tab) {
                filterSelection = fromText(tab.text ?: "")
                supportFragmentManager.fragments?.lastOrNull()?.let {
                    if (it is ProductCatalogFilterInteraction) {
                        it.updateFilterSelection(filterSelection, true)
                    }
                }
            }

            private fun fromText(text: CharSequence?) : FilterSelection = when (text) {
                allProductsText -> FilterSelection.ALL_PRODUCTS
                hubRequiredText -> FilterSelection.HUB_REQUIRED
                noHubRequiredText -> FilterSelection.NO_HUB_REQUIRED
                else -> FilterSelection.ALL_PRODUCTS
            }
        })

        tabLayout.getTabAt(filterSelection.ordinal)?.select()
        presenter.dismissAll() // Ignore results from this
        finishToActivityIntent = intent.getParcelableExtra(ARGS_FINISH_TO_ACTIVITY_INTENT)
    }

    override fun onResume() {
        super.onResume()
        presenter.setView(this)
        kitPresenter.setView(this)
        presenter.stopPairing()
        presenter.getMisparedDevicesCount()
        kitPresenter.getDeviceActivationStatus()
    }

    override fun onPause() {
        super.onPause()
        presenter.clearView()
        kitPresenter.clearView()
    }

    override fun displayMispairedDeviceCount(count: Int) {
        hubDevices += count
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the options menu from XML
        menuInflater.inflate(R.menu.options_menu, menu)

        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val searchInfo = searchManager.getSearchableInfo(componentName)
        val suggestionAdapter = SimpleCursorAdapter(this,
                android.R.layout.simple_list_item_1,
                null,
                arrayOf(SearchManager.SUGGEST_COLUMN_TEXT_1),
                intArrayOf(android.R.id.text1),
                0)

        searchView = menu.findItem(R.id.search).actionView as SearchView
        searchView.setSearchableInfo(searchInfo)
        searchView.suggestionsAdapter = suggestionAdapter

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true // No-Op
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                handler.removeCallbacksAndMessages(null)

                if (!newText.isNullOrEmpty()) {
                    handler.postDelayed({
                        if (isDestroyed || isFinishing) {
                            return@postDelayed
                        }

                        with(Bundle()) {
                            putString(BUN_SEARCH_STRING, newText)
                            supportLoaderManager.restartLoader(R.id.search_loader_id, this, this@ProductCatalogActivity)
                        }
                    }, SEARCH_THROTTLE_MS)
                } else {
                    searchView.suggestionsAdapter.swapCursor(null)
                }
                return false
            }
        })

        searchView.setOnQueryTextFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                searchView.suggestionsAdapter.swapCursor(null)
            }
        }

        return true
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
        val searchString = args?.getString(BUN_SEARCH_STRING,"NULL") ?: "NULL"
        return SearchSuggestionsAsyncLoader(this, searchString)
    }

    override fun onLoadFinished(loader: Loader<Cursor>, data: Cursor?) {
        searchView.suggestionsAdapter.swapCursor(data)
    }

    override fun onLoaderReset(loader: Loader<Cursor>) {} // No-Op

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        if (Intent.ACTION_SEARCH == intent?.action) {
            val productAddress = intent.getStringExtra(SearchManager.EXTRA_DATA_KEY)
            ProductCatalogPopupManager().navigateForwardOrShowPopup(this, productAddress)
        } else {
            while (supportFragmentManager.backStackEntryCount > 0) {
                supportFragmentManager.popBackStackImmediate()
            }

            val tabLayout = findViewById<TabLayout>(R.id.category_filter_items)
            tabLayout.getTabAt(0)?.select()
            filterSelection = FilterSelection.ALL_PRODUCTS

            supportFragmentManager
                .beginTransaction()
                .addToBackStack(null)
                .replace(R.id.container, BrandsOverviewFragment.newInstance(), "ADD_A_DEVICE")
                .commit()
        }
    }

    override fun navigateUpTo(upIntent: Intent?): Boolean {
        onBackPressed() // Simulate a hardware back press
        return false // We didn't redeliver the intent
    }

    override fun onBackPressed() {
        if (supportFragmentManager.isStateSaved && Build.VERSION.SDK_INT <= Build.VERSION_CODES.N_MR1) {
            // Older versions will throw an exception from the framework
            // FragmentManager.popBackStackImmediate(), so we'll just
            // return here. The Activity is likely already on its way out
            // since the fragmentManager has already been saved.
            return
        }

        if (supportFragmentManager.backStackEntryCount > 1) {
            supportFragmentManager.popBackStackImmediate()
        } else {
            if(!promptCustomizeBeforeExit()) {
                presenter.clearView()
                finishToActivityIntent?.let { finishIntent ->
                    startActivity(finishIntent)
                }
                finish()
            }
        }
    }

    private fun promptCustomizeBeforeExit(): Boolean {
        if(presenter.getMispairedOrMisconfigured()) {
            // show mispaired popup
            ProductCatalogPopupManager().showCustomizeDevicesPopup(
                this,
                false,
                getString(R.string.customize_problem_paired_devices),
                finishListener
            )
            return true

        } else if (presenter.hasPairedDevices()){
            // show customize popup
            ProductCatalogPopupManager().showCustomizeDevicesPopup(
                this,
                true,
                getString(R.string.customize_paired_devices),
                finishListener)
            return true
        }
        return false
    }

    override fun onDeviceActivationStatusUpdate(status: DeviceActivationStatus) {
        kitDevices = status.needsActivation
        hubDevices += status.needsAttention
        showMustardModal()
        suppressMustardModal = true
    }

    private fun goToSearching() {
        val intent = Intent(this, DeviceSearchingActivity::class.java)
        intent.putExtra(DeviceSearchingActivity.ARG_START_SEARCHING_BOOL, false)
        intent.putExtra(DeviceSearchingActivity.ARG_DISABLE_BACK_PRESS_BOOL, true)
        startActivity(intent)
    }


    private fun goToGrid() {
        // If Hub is offline, how a hub Offline popup
        if (HubModelProvider.instance().hubModel?.get(Hub.ATTR_STATE) == Hub.STATE_DOWN) {
            val popup = Generic2ButtonErrorPopup.newInstance(
                    getString(R.string.enhanced_hub_offline_title),
                    getString(R.string.enhanced_hub_offline_desc),
                    getString(R.string.error_modal_get_support),
                    getString(R.string.dismiss)
            )
            popup.setTopButtonListener {
                ActivityUtils.launchUrl(GlobalSetting.NO_CONNECTION_HUB_SUPPORT_URL)
            }
            popup.setBottomButtonListener {
                popup.dismiss()
            }
            popup.show(supportFragmentManager)
        } else {
            startActivity(
                    GenericConnectedFragmentActivity
                            .getLaunchIntent(
                                    this,
                                    KitActivationGridFragment::class.java
                            )
            )
            this.finish()
        }
    }

    private fun showMustardModal(){
        if(!suppressMustardModal) {
            if (kitDevices != 0 || hubDevices != 0) {

                val mustardModal = ModalMustardSheet.newInstance(kitDevices, hubDevices)
                mustardModal.setKitButtonAction(kitDevicesListener)
                mustardModal.setHubButtonAction(hubDevicesListener)
                mustardModal.show(this.supportFragmentManager, ModalMustardSheet::class.java.name /* tag */)
            }
        }
    }

    override fun dismissWithZwaveRebuild() {
        val intent = Intent(this, ZWaveRebuildActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
    }

    override fun dismissNormally() {
        finish()
    }

    companion object {
        const val ARG_FILTER_SELECTION = "ARG_FILTER_SELECTION"
        const val ARGS_FINISH_TO_ACTIVITY_INTENT = "ARGS_FINISH_TO_ACTIVITY_INTENT"
        const val BUN_SEARCH_STRING    = "BUN_SEARCH_STRING"
        const val SEARCH_THROTTLE_MS   = 250L

        @JvmStatic
        fun createIntent(
            from: Context,
        finishToActivityIntent: Intent? = null) = Intent(from, ProductCatalogActivity::class.java).also {
            it.putExtra(ARGS_FINISH_TO_ACTIVITY_INTENT, finishToActivityIntent)
        }

        @JvmStatic
        fun createIntentForNoHubProducts(from: Context) = createIntent(from).also {
            it.putExtra(ARG_FILTER_SELECTION, FilterSelection.NO_HUB_REQUIRED)
        }

        @JvmOverloads
        @JvmStatic
        fun createIntentClearTop(
            from: Context,
        finishToActivityIntent: Intent? = null) = createIntent(from, finishToActivityIntent).also {
            it.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
    }
}

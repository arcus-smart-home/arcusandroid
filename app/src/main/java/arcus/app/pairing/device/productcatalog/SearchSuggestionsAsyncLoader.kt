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
import android.database.MatrixCursor
import android.provider.BaseColumns
import androidx.loader.content.AsyncTaskLoader

import arcus.cornea.CorneaClientFactory
import arcus.cornea.SessionController
import com.iris.client.capability.Place
import com.iris.client.capability.Product
import com.iris.client.service.ProductCatalogService
import arcus.app.R

class SearchSuggestionsAsyncLoader(context: Context, private val searchString: String) : AsyncTaskLoader<Cursor>(context) {
    // Note: This is using the Async Tasks thread pool
    private var resultsCursor : Cursor? = null
    private val columns = arrayOf(
            BaseColumns._ID,
            SearchManager.SUGGEST_COLUMN_TEXT_1,
            SearchManager.SUGGEST_COLUMN_INTENT_ACTION,
            SearchManager.EXTRA_DATA_KEY,
            SearchManager.SUGGEST_COLUMN_INTENT_EXTRA_DATA
    )

    override fun loadInBackground(): Cursor? {
        try {
            val cursor = MatrixCursor(columns)
            val activePlace = SessionController.instance().activePlace
            if (activePlace == null) {
                addNoResultsCursor(cursor)
            } else {
                CorneaClientFactory.getService(ProductCatalogService::class.java).findProducts(
                        SERVICE_ADDRESS_FORMAT.format(activePlace), searchString
                ).onSuccess { response ->
                    if (response.products.isEmpty()) {
                        addNoResultsCursor(cursor)
                    } else {
                        response.products.forEachIndexed { index, map ->
                            cursor.addRow(
                                    arrayOf(
                                            index,
                                            "${map[Product.ATTR_VENDOR]} ${map[Product.ATTR_NAME]}",
                                            Intent.ACTION_SEARCH,
                                            SearchManager.EXTRA_DATA_KEY,
                                            map[Product.ATTR_ADDRESS]
                                    )
                            )
                        }
                    }
                }.get()
            }

            return cursor
        } catch (ex: Exception) {
            return null
        }
    }

    // This is used in a lambda, keeping visibility "kotlin protected" so we don't create synthetics
    @Suppress("MemberVisibilityCanBePrivate")
    internal fun addNoResultsCursor(cursor: MatrixCursor) {
        cursor.addRow(arrayOf(
                1,
                context.resources.getString(R.string.no_results_search),
                Intent.ACTION_VIEW,
                SearchManager.EXTRA_DATA_KEY,
                "None" // Not used but required in payload.
        ))
    }

    override fun onStartLoading() {
        if (resultsCursor != null) {
            deliverResult(resultsCursor)
        }

        if (takeContentChanged() || resultsCursor == null) {
            forceLoad()
        }
    }

    override fun deliverResult(data: Cursor?) {
        if (isReset) {
            data?.close() // An async query came in while the loader is stopped
            return
        }

        val oldCursor = resultsCursor
        resultsCursor = data

        if (isStarted) {
            super.deliverResult(data)
        }

        if (oldCursor !== data) {
            oldCursor?.close()
        }
    }

    override fun onStopLoading() {
        cancelLoad()
    }

    override fun onCanceled(data: Cursor?) {
        data?.takeIf { !it.isClosed }?.close()
    }

    companion object {
        const val SERVICE_ADDRESS_FORMAT = "SERV:${Place.NAMESPACE}:%s"
    }
}

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
package arcus.presentation.pairing.device.productcatalog.brandsoverview

import arcus.cornea.SessionController
import arcus.cornea.helpers.onSuccessMain
import arcus.cornea.helpers.transformNonNull
import arcus.cornea.presenter.KBasePresenter
import arcus.cornea.provider.ProductModelProvider
import arcus.presentation.pairing.NON_ALPHA_NUMERIC
import com.iris.client.model.ProductModel
import org.slf4j.LoggerFactory

/**
 * Presenter for Add a Device
 *
 * @param screenDensity Screen density - used in URL Generation
 */
class BrandsOverviewPresenterImpl(
    private val screenDensity: String,
    private val srsBaseURL: String = SessionController.instance().staticResourceBaseUrl
) : KBasePresenter<BrandsCatalogContractView>(),
    BrandsCatalogContractPresenter {
    override fun getAllBrands() {
        getProducts()
    }

    override fun getBrandsHubFiltered(hubRequired: Boolean) {
        getProducts { it.hubRequired == hubRequired }
    }

    private fun setMainUrlFor(name: String): String {
        val brandName = name.replace(NON_ALPHA_NUMERIC, "")
        return BRANDS_URL_FORMAT.format(srsBaseURL, brandName.toLowerCase(), screenDensity)
    }

    private fun setBlackWhiteUrlFor(name: String): String {
        val brandName = name.replace(NON_ALPHA_NUMERIC, "")
        return BW_BRANDS_URL_FORMAT.format(srsBaseURL, brandName.toLowerCase(), screenDensity)
    }

    private fun getProducts(additionalFilter: (ProductModel) -> Boolean = { true }) {
        ProductModelProvider
            .instance()
            .load()
            .transformNonNull {
                val brands = it
                    .filterNot { it.vendor.isNullOrEmpty() }
                    .filter { it.canSearch && it.canBrowse }
                    .filter { additionalFilter(it) }
                    .groupingBy { it.vendor }
                    .eachCount()
                    .filter { it.value > 0 }
                    .map { (key, value) ->
                        val mainUrl = setMainUrlFor(key)
                        BrandCategoryProxyModel(
                            value,
                            key,
                            mainUrl,
                            setBlackWhiteUrlFor(key),
                            mainUrl
                        )
                    }
                    .sortedWith(
                        compareByDescending<BrandCategoryProxyModel> {
                            it.name.equals("arcus", ignoreCase = true)
                        }.thenBy {
                            it.name.toLowerCase()
                        }
                    )
                val count = brands.sumBy { it.count }
                Pair(count, brands)
            }
            .onSuccessMain {
                onlyIfView { view ->
                    view.displayBrands(it.first, it.second)
                }
            }
            .onFailure { error ->
                logger.error("Failed to load catalog", error)
            }
    }

    companion object {
        private const val BRANDS_URL_FORMAT = "%s/o/brands/%s/brand_small_color-and-%s.png"
        private const val BW_BRANDS_URL_FORMAT = "%s/o/brands/%s/brand_small-and-%s.png"

        private val logger = LoggerFactory.getLogger(BrandsOverviewPresenterImpl::class.java)
    }
}

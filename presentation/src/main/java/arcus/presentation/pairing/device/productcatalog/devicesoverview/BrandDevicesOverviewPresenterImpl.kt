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
package arcus.presentation.pairing.device.productcatalog.devicesoverview

import arcus.cornea.SessionController
import arcus.cornea.helpers.onSuccessMain
import arcus.cornea.helpers.transformNonNull
import arcus.cornea.presenter.KBasePresenter
import arcus.cornea.provider.ProductModelProvider
import com.iris.client.model.ProductModel

/**
 * Presenter for a Brands Overview "Page"
 *
 * @param screenDensity Screen density - used in URL Generation
 * @param productModelProvider Product Model Provider - one of the "Model"s in MVP - fetches our ProductModel's from
 *                             the platform.
 */
class BrandDevicesOverviewPresenterImpl(
    private val screenDensity: String,
    private val productModelProvider: ProductModelProvider = ProductModelProvider.instance(),
    private val srsBaseURL: String = SessionController.instance().staticResourceBaseUrl
) : BrandDevicesOverviewPresenter, KBasePresenter<BrandDevicesOverviewView>() {
    override fun getAllProductsFor(brand: String) {
        productModelProvider
            .load()
            .transformNonNull { it
                .filter {
                    val notHub = it.id != HUB_PRODUCT_ID
                    val containsBrand = it.vendor?.equals(brand, true) == true
                    val isBrowsable = it.canBrowse ?: false

                    notHub && containsBrand && isBrowsable
                }.map {
                    toProductEntry(it)
                }
            }
            .onSuccessMain { productModels ->
                onlyIfView { viewRef ->
                    viewRef.displayProducts(productModels)
                }
            }
    }

    override fun getAllProductsFor(brand: String, hubRequired: Boolean) {
        productModelProvider
            .load()
            .transformNonNull { it
                    .filter {
                        val notHub = it.id != HUB_PRODUCT_ID
                        val hubRequiredMatches = it.hubRequired == hubRequired
                        val containsBrand = it.vendor?.equals(brand, true) == true
                        val isBrowsable = it.canBrowse ?: false

                        notHub && hubRequiredMatches && containsBrand && isBrowsable
                    }.map {
                        toProductEntry(it)
                    }
            }
            .onSuccessMain { productModels ->
                onlyIfView { viewRef ->
                    viewRef.displayProducts(productModels)
                }
            }
    }

    private fun toProductEntry(model: ProductModel) =
        ProductEntry(
            model.id,
            model.address,
            model.name,
            model.vendor,
            getMainUrlFor(model),
            getBackupUrlFor(model),
            model.hubRequired
        )

    private fun getMainUrlFor(productModel: ProductModel): String {
        val safeProductId = productModel.id?.replace(NON_ALPHA_NUMERIC, "") ?: ""
        return PRODUCTS_URL_FORMAT.format(srsBaseURL, safeProductId, screenDensity)
    }

    private fun getBackupUrlFor(productModel: ProductModel): String {
        val safeScreen = productModel.screen?.replace(NON_ALPHA_NUMERIC, "") ?: ""
        return D_TYPES_URL_FORMAT.format(srsBaseURL, safeScreen.toLowerCase(), screenDensity)
    }

    companion object {
        private const val HUB_PRODUCT_ID = "dee000"
        private const val PRODUCTS_URL_FORMAT = "%s/o/products/%s/product_small-and-%s.png"
        private const val D_TYPES_URL_FORMAT = "%s/o/dtypes/%s/type_small-and-%s.png"

        private val NON_ALPHA_NUMERIC = "[^a-zA-Z0-9]".toRegex()
    }
}

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

import arcus.cornea.presenter.BasePresenterContract

interface BrandsCatalogContractView {
    /**
     * Display a list of Brands
     *
     * *Note: Will be called from a worker thread, UI should post to appropriate thread
     */
    fun displayBrands(count: Int, brands: List<BrandCategoryProxyModel>)
}

interface BrandsCatalogContractPresenter : BasePresenterContract<BrandsCatalogContractView> {
    fun getAllBrands()

    fun getBrandsHubFiltered(hubRequired: Boolean)
}

data class BrandCategoryProxyModel(
    val count: Int,
    val name: String,
    val mainUrl: String,
    val backupUrl: String,
    val cachedImageUrl: String
)

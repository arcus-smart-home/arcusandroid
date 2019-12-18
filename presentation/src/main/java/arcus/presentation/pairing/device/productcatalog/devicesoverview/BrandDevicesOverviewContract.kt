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

import arcus.cornea.presenter.BasePresenterContract

interface BrandDevicesOverviewView {
    /**
     * Display this list of products
     *
     * *Note: Will be called from a worker thread, UI should post to appropriate thread
     */
    fun displayProducts(products: List<ProductEntry>)
}

interface BrandDevicesOverviewPresenter : BasePresenterContract<BrandDevicesOverviewView> {
    fun getAllProductsFor(brand: String)

    fun getAllProductsFor(brand: String, hubRequired: Boolean)
}

data class ProductEntry(
    val id: String,
    val address: String,
    val title: String,
    val subTitle: String,
    val iconUrl: String,
    val backupIconUrl: String,
    val isHubRequired: Boolean
)

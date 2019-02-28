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
package arcus.app.pairing.device.productcatalog.popups

enum class FilterSelection {
    ALL_PRODUCTS,
    HUB_REQUIRED,
    NO_HUB_REQUIRED
}

/**
 * Interface to specify actions that have been taken on the product catalog filter.
 *
 * Currently there are only 3 options provided by the [FilterSelection]
 */
interface ProductCatalogFilterInteraction {
    /**
     * Indicates that the selected filter has been selected by the user.
     *
     * @param selection Current [FilterSelection]
     * @param resetPosition if the list should reset it's position back to 0
     */
    fun updateFilterSelection(selection: FilterSelection, resetPosition: Boolean)
}
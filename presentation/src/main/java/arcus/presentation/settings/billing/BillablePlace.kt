/*
 *  Copyright 2020 Arcus Project.
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
package arcus.presentation.settings.billing

import androidx.annotation.StringRes

/**
 * A billable place representation.
 *
 * @param placeId The place ID.
 * @param placeName The name of the place.
 * @param streetAddress The street address of this place.
 * @param cityStateZip The formatted city/state/zip of this place.
 * @param currentServiceLevel The current service level string resource to be loaded.
 * @param serviceAddons The service addon string resources to be loaded.
 */
data class BillablePlace(
    val placeId: String,
    val placeName: String,
    val streetAddress: String,
    val cityStateZip: String,
    @StringRes val currentServiceLevel: Int?,
    @StringRes val serviceAddons: List<Int>
)

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
package arcus.app.subsystems.feature.cards

import android.content.Context
import arcus.app.R
import arcus.app.common.cards.SimpleDividerCard

class FeatureCard(context: Context) : SimpleDividerCard(context) {

    init {
        showDivider()
    }

    override fun getTitle(): String { return getString(R.string.card_feature_title) }

    override fun getDescription(): String { return getString(R.string.card_feature_desc) }

    fun getResourceId(): Int { return R.drawable.feature_light_23x20 }
}
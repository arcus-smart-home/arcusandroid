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
package arcus.presentation.scenes

import android.os.Parcelable
import arcus.presentation.common.view.ContentsComparable
import kotlinx.android.parcel.Parcelize

sealed class SceneListItems {
    abstract val count: Int

    object Empty : SceneListItems() {
        override val count: Int = 0
    }

    @Parcelize
    data class SceneItems(
        override val count: Int,
        val models: List<Scene>
    ) : Parcelable, SceneListItems()
}

@Parcelize
data class Scene(
    val id: String,
    val address: String,
    val name: String,
    val isEnabled: Boolean,
    val hasSchedule: Boolean,
    val actionCount: Int,
    val type: Type
) : Parcelable, ContentsComparable<Scene> {
    enum class Type {
        AWAY,
        HOME,
        MORNING,
        NIGHT,
        VACATION,
        CUSTOM
        ;

        companion object {
            fun from(name: String): Type = values()
                .firstOrNull {
                    it.name.equals(name, true)
                } ?: CUSTOM
        }
    }

    override fun areItemsTheSame(newItem: Scene): Boolean = id == newItem.id
    override fun areContentsTheSame(newItem: Scene): Boolean = this == newItem
}

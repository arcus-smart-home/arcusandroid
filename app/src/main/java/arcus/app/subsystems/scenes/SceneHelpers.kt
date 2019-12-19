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
@file:JvmName("SceneHelpers")
package arcus.app.subsystems.scenes

import androidx.annotation.DrawableRes
import arcus.app.R
import arcus.presentation.scenes.Scene

@DrawableRes
fun Scene.Type.drawableRes(): Int = when (this) {
    Scene.Type.AWAY -> R.drawable.scene_icon_away
    Scene.Type.HOME -> R.drawable.scene_icon_home
    Scene.Type.MORNING -> R.drawable.scene_icon_good_morning
    Scene.Type.NIGHT -> R.drawable.scene_icon_good_night
    Scene.Type.VACATION -> R.drawable.scene_icon_vacation
    else -> R.drawable.scene_icon_custom
}

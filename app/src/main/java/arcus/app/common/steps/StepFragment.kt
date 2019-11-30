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
package arcus.app.common.steps

import android.content.Context
import androidx.fragment.app.Fragment


/**
 * Base class for Steps so we don't have to remember to cast the step container each time, and
 * where to cast it.
 */
abstract class StepFragment<T : Any> : Fragment() {
    protected lateinit var stepContainer : T

    @Suppress("UNCHECKED_CAST") // We want to crash if it's not a [T] (in dev...)
    override fun onAttach(context: Context) {
        super.onAttach(context)
        stepContainer = parentFragment as T
    }
}

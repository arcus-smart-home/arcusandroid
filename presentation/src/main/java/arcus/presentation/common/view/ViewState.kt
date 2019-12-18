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
package arcus.presentation.common.view

/**
 * Represents the different states a view will go through when loading data.
 */
sealed class ViewState<T> {
    /**
     * The view is currently loading.
     */
    class Loading<T> : ViewState<T>()

    /**
     * The data is loaded and there is a result [item].
     */
    data class Loaded<T>(val item: T) : ViewState<T>()

    /**
     * There was an [error] of some sorts loading the data of type [errorType].
     */
    data class Error<T, R : Enum<R>>(
        val error: Throwable,
        val errorType: R
    ) : ViewState<T>()
}

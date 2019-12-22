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
package arcus.app.common.fragments

import android.os.Bundle
import android.view.View
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import arcus.presentation.common.view.ViewState
import arcus.presentation.common.view.ViewStateViewModel

/**
 * Used for classes that have been updated to use a view model. Once all classes are updated  to have a ViewModel this
 * should have everything from [NoViewModelFragment] consolidated into this [CoreFragment] and [NoViewModelFragment]
 * removed.
 */
abstract class CoreFragment<T : ViewStateViewModel<*>> : NoViewModelFragment() {
    /**
     * The fragments [viewModel].
     */
    protected lateinit var viewModel: T

    /**
     * The view model class this fragment should load.
     */
    abstract val viewModelClass: Class<T>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(viewModelClass)
        viewModel.viewState.observe(viewLifecycleOwner, Observer {
            when (it) {
                is ViewState.Loading -> progressContainer.isVisible = true
                else -> progressContainer.isGone = true
            }
        })
    }
}

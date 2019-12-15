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
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewStub
import androidx.annotation.LayoutRes
import androidx.annotation.MenuRes
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import arcus.app.R
import arcus.presentation.common.view.ViewState
import arcus.presentation.common.view.ViewStateViewModel
import org.slf4j.Logger
import org.slf4j.LoggerFactory

abstract class CoreFragment<T : ViewStateViewModel<*>> : Fragment() {
    protected val logger: Logger = LoggerFactory.getLogger(this.javaClass)

    /**
     * The fragments [viewModel].
     */
    protected lateinit var viewModel: T

    /**
     * A container holding a progress spinner (indeterminate) that can be shown while an action
     * is pending.
     */
    protected lateinit var progressContainer: View

    /**
     * The view model class this fragment should load.
     */
    abstract val viewModelClass: Class<T>

    /**
     * The title of this fragment that should be set on an action bar (if applicable).
     */
    abstract val title: String

    /**
     * The layout to be inflated in this Fragment.
     */
    abstract val layoutId: Int
        @LayoutRes get

    /**
     * The menu to inflate, if any.
     */
    open val menuId: Int? = null
        @MenuRes get

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_core, container, false)
        view?.findViewById<ViewStub>(R.id.coreViewContainer)?.let { stub ->
            stub.layoutResource = layoutId
            stub.inflate()
        }

        setHasOptionsMenu(true)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        progressContainer = view.findViewById(R.id.progressContainer)
        viewModel = ViewModelProviders.of(this).get(viewModelClass)
        viewModel.viewState.observe(viewLifecycleOwner, Observer {
            when (it) {
                is ViewState.Loading -> progressContainer.isVisible = true
                else -> progressContainer.isGone = true
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menuId?.let { id -> inflater.inflate(id, menu) }
    }

    override fun onResume() {
        super.onResume()
        setTitle()
    }

    /**
     * Call to set the title of the view to the [title].
     */
    protected fun setTitle() {
        requireActivity().let {
            it.title = title
            it.invalidateOptionsMenu()
        }
    }
}

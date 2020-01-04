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
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import arcus.app.R
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Used for classes that do not have a view model yet and still need to be updated to use one. However, this will
 * allow for the core concepts to be reused. Once all classes are updated this should be consolidated into
 * [CoreFragment].
 */
abstract class NoViewModelFragment : Fragment() {
    protected val logger: Logger = LoggerFactory.getLogger(this.javaClass)

    /**
     * A container holding a progress spinner (indeterminate) that can be shown while an action
     * is pending.
     */
    protected lateinit var progressContainer: View

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

    // TODO: This should really use the callback dispatcher, but leaving for now...
    open fun onBackPressed(): Boolean = false // Allow "normal" handling of this...

    protected fun showActionBar() = showHideActionBar(false)
    protected fun hideActionBar() = showHideActionBar(true)

    private fun showHideActionBar(hide: Boolean) {
        val currentActivity = activity
        if (currentActivity is AppCompatActivity) {
            if (hide) {
                currentActivity.supportActionBar?.hide()
            } else {
                currentActivity.supportActionBar?.show()
            }
        } else {
            if (hide) {
                currentActivity?.actionBar?.hide()
            } else {
                currentActivity?.actionBar?.show()
            }
        }
    }
}

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
package arcus.app.common.steps.container

import android.content.Context
import android.os.Bundle
import androidx.annotation.CallSuper
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import arcus.app.common.fragment.BackPressInterceptor
import arcus.app.common.fragment.FragmentContainerHolder
import arcus.app.common.fragment.FragmentVisibilityListener
import arcus.app.common.fragment.TitledFragment
import kotlin.properties.Delegates

/**
 * Hosting fragment for a series of steps (fragments) that a user should be shown
 *
 * This Fragment will handle notifying any [FragmentVisibilityListener]s of their
 * visible/not visible status as well as getting, and setting, the title of any
 * [TitledFragment]s
 *
 */
abstract class StepContainerFragment : Fragment(),
    BackPressInterceptor,
    StepContainer {
    protected var fragmentStepContainer by Delegates.notNull<FragmentContainerHolder>()
    private lateinit var viewPager : ViewPager
    protected val pageChangedListener = object : ViewPager.OnPageChangeListener {
        private var lastPageSelected = 0

        override fun onPageScrollStateChanged(state: Int) {}

        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

        override fun onPageSelected(position: Int) {
            if (position != lastPageSelected) {
                val previous = getFragmentAt(lastPageSelected)
                if (previous is FragmentVisibilityListener) {
                    previous.onFragmentNotVisible()
                }
            }

            val current = getFragmentAt(position)
            lastPageSelected = position
            if (current is FragmentVisibilityListener) {
                current.onFragmentVisible()
            }

            if (current is TitledFragment) {
                setTitle(current.getTitle())
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(getLayoutResource(), container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewPager = getViewPager(view)
        viewPager.addOnPageChangeListener(pageChangedListener)
        viewPager.addOnAdapterChangeListener { viewPager, _, _ ->
            pageChangedListener.onPageSelected(viewPager.currentItem)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        fragmentStepContainer = context as FragmentContainerHolder
    }

    /**
     * Since the layout is dynamic and we can't guarantee the id of the view pager, this is
     * called to get the element from the view so we have a reference to it.
     *
     * @return the ViewPager for this host to use
     */
    abstract fun getViewPager(view: View) : ViewPager

    /**
     * Invoked to get the fragment at the current position, as indicated by the viewpager.
     */
    abstract fun getFragmentAt(position: Int) : Fragment

    /**
     * Gets the layout resource containing the view pager you want to use.
     */
    @LayoutRes
    abstract fun getLayoutResource() : Int

    /**
     * Checks if the view pager has an adapter set already or not
     */
    fun viewPagerHasAdapter() = viewPager.adapter != null

    /**
     * Sets the active fragments for this view pager
     */
    fun setPagerAdapter(adapter: PagerAdapter) {
        viewPager.adapter = adapter
    }

    fun addPageChangedListener(listener: ViewPager.OnPageChangeListener) {
        viewPager.addOnPageChangeListener(listener)
    }

    override fun replaceFragmentContainerWith(fragment: Fragment, addToBackStack: Boolean) {
        fragmentStepContainer.replaceFragmentContainerWith(fragment, addToBackStack)
    }

    override fun addToFragmentContainer(fragment: Fragment, addToBackStack: Boolean) {
        fragmentStepContainer.addToFragmentContainer(fragment, addToBackStack)
    }

    @CallSuper
    override fun onBackPressed() : Boolean {
        return if (viewPager.currentItem > 0) {
            viewPager.setCurrentItem(viewPager.currentItem - 1, true)
            true
        } else {
            false
        }
    }
}

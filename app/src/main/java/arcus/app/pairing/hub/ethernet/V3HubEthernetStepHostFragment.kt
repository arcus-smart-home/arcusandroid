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
package arcus.app.pairing.hub.ethernet

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import android.view.View
import android.widget.Button
import arcus.app.R
import arcus.app.common.fragment.ValidationFragment
import arcus.app.common.steps.container.StepContainerFragment
import arcus.app.common.utils.VideoUtils
import arcus.app.pairing.hub.V3HubSearchingFragment
import com.viewpagerindicator.CirclePageIndicator


class V3HubEthernetStepHostFragment : StepContainerFragment(),
    V3HubPairingStepContainer {
    private lateinit var pageIndicator: CirclePageIndicator
    private lateinit var nextButton: Button
    private lateinit var viewPager: ViewPager

    override var hubId: String = ""
    private var fragments = listOf<Fragment>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        pageIndicator = view.findViewById(R.id.page_indicator)
        nextButton = view.findViewById(R.id.next_button)
        nextButton.setOnClickListener {
            navigateForward()
        }

        viewPager = view.findViewById(R.id.view_pager)
        view.findViewById<View>(R.id.watch_tutorial_banner).setOnClickListener {
            // TODO: Replace with correct link.
            VideoUtils.launchV3HubTutorial()
        }

    }

    override fun onStart() {
        super.onStart()
        if (fragments.isEmpty()) {
            fragments = listOf(
                V3HubPairingStepFragment.newInstance(
                    R.drawable.hubpairing_illustration_step2,
                    R.string.v3_hub_step_1_pairing_text
                ),
                V3HubPairingStepFragment.newInstance(
                    R.drawable.v3hub_ethernet_step2_220x220,
                    R.string.v3_hub_step_2_pairing_text
                ),
                V3HubPairingInputStepFragment.newInstance()
            )
        }

        if (!viewPagerHasAdapter()) {
            setPagerAdapter(object : FragmentPagerAdapter(childFragmentManager) {
                override fun getItem(position: Int) = fragments[position]
                override fun getCount(): Int = fragments.size
            })

            pageIndicator.setOnPageChangeListener(pageChangedListener)
            pageIndicator.setViewPager(viewPager, viewPager.currentItem)
        }

        setTitle(getString(R.string.pairing_hub))
        showBackButtonOnToolbar(true)
    }

    override fun getFragmentAt(position: Int): Fragment = fragments[position]
    override fun getViewPager(view: View): ViewPager = view.findViewById(R.id.view_pager)
    override fun getLayoutResource(): Int = R.layout.fragment_v3_hub_ethernet_pairing_container

    override fun enableStepForward(enable: Boolean) {
        nextButton.isEnabled = enable
    }

    override fun showBackButtonOnToolbar(show: Boolean) {
        fragmentStepContainer.showBackButtonOnToolbar(show)
    }

    override fun setTitle(title: String) {
        fragmentStepContainer.setTitle(title)
    }

    override fun navigateForward() {
        val currentPage = viewPager.currentItem
        if (currentPage != (fragments.size - 1)) {
            viewPager.setCurrentItem(currentPage + 1, true)
        } else {
            val current = fragments[viewPager.currentItem]
            if (current !is ValidationFragment || current.inValidState()) {
                fragmentStepContainer.replaceFragmentContainerWith(
                    V3HubSearchingFragment.newInstance(
                        hubId
                    )
                )
            }
        }
    }

    override fun navigateBackward() {
        onBackPressed()
    }

    companion object {
        @JvmStatic
        fun newInstance() = V3HubEthernetStepHostFragment()
    }
}

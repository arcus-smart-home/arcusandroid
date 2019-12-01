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
package arcus.app.pairing.hub.kickoff

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import android.view.View
import android.widget.Button
import arcus.app.R
import arcus.app.activities.GenericConnectedFragmentActivity
import arcus.app.common.steps.container.StepContainerFragment
import arcus.app.common.utils.GlobalSetting
import arcus.app.common.utils.VideoUtils
import arcus.app.pairing.device.steps.bledevice.BlePairingStepsActivity
import arcus.app.pairing.hub.ethernet.V3HubEthernetStepHostFragment
import com.viewpagerindicator.CirclePageIndicator

class V3HubKickoffStepHostFragment : StepContainerFragment(),
        V3HubKickoffStepContainer {

    private lateinit var pageIndicator: CirclePageIndicator
    private lateinit var nextButton: Button
    private lateinit var ethernetButton: Button
    private lateinit var viewPager: ViewPager

    private var fragments = listOf<Fragment>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        pageIndicator = view.findViewById(R.id.page_indicator)
        viewPager = view.findViewById(R.id.view_pager)
        ethernetButton = view.findViewById(R.id.ethernet_button)
        nextButton = view.findViewById(R.id.next_button)
        nextButton.setOnClickListener {
            navigateForward()
        }

        ethernetButton.setOnClickListener {
            activity?.let {
                it.startActivity(GenericConnectedFragmentActivity.getLaunchIntent(it, V3HubEthernetStepHostFragment::class.java))
            }
        }

        view.findViewById<View>(R.id.watch_tutorial_banner).setOnClickListener {
            VideoUtils.launchV3HubTutorial()
        }
    }

    override fun onStart() {
        super.onStart()

        loadFragments()

        if (!viewPagerHasAdapter()) {
            setPagerAdapter(object: FragmentPagerAdapter(childFragmentManager) {
                override fun getItem(position: Int) = fragments[position]
                override fun getCount(): Int = fragments.size
            })

            addPageChangedListener(this)
            pageIndicator.setViewPager(viewPager, viewPager.currentItem)
        }

        setTitle(getString(R.string.hub_title))
        showBackButtonOnToolbar(true)
    }

    override fun getViewPager(view: View): ViewPager = view.findViewById(R.id.view_pager)

    override fun getFragmentAt(position: Int): Fragment = fragments[position]

    override fun getLayoutResource(): Int = R.layout.fragment_v3_hub_kickoff_container

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
        }
    }

    override fun navigateBackward() {
        onBackPressed()
    }

    override fun onPageSelected(position: Int) {
        super.onPageSelected(position)

        if (position < fragments.size - 1) {
            nextButton.text = getString(R.string.pairing_next)
            nextButton.setOnClickListener {
                navigateForward()
            }

            ethernetButton.visibility = View.GONE
        }
        else {
            nextButton.text = getString(R.string.connect_to_wifi)
            nextButton.setOnClickListener {
                activity?.let {
                    startActivity(BlePairingStepsActivity.createIntentForV3Hub(it))
                }
            }

            ethernetButton.visibility = View.VISIBLE
        }
    }

    private fun loadFragments() {
        if (fragments.isEmpty()) {
            fragments = listOf(
                V3HubKickoffStepFragment.newInstance(
                        R.drawable.v3hub_step1_220x220,
                        R.string.v3_hub_kickoff_step1
                ),
                V3HubKickoffStepFragment.newInstance(
                        R.drawable.hubpairing_illustration_step5,
                        R.string.hub_step_plug_into_outlet

                ),
                V3HubKickoffStepFragment.newInstance(
                        R.drawable.v3hub_step3_220x220,
                        R.string.v3_hub_blue_ring,
                        linkTextRes = R.string.v3_halo_not_blue,
                        linkUri = GlobalSetting.RING_NOT_PURPLE.toString()
                ),
                V3HubKickoffStepFragment.newInstance(
                        R.drawable.wifi_90x90,
                        R.string.v3_hub_connecting_to_wifi,
                        R.string.v3_hub_how_to_connect
                )
            )
        }
    }

    companion object {
        @JvmStatic
    fun newInstance() = V3HubKickoffStepHostFragment()
    }
}

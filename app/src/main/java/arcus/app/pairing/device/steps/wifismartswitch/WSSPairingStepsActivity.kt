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
package arcus.app.pairing.device.steps.wifismartswitch

import android.content.Context
import android.content.Intent
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import android.view.View
import arcus.cornea.network.NetworkConnectionMonitor
import arcus.app.R
import arcus.app.common.utils.ActivityUtils
import arcus.app.common.utils.GlobalSetting
import arcus.app.common.view.DisableSwipeViewPager
import arcus.app.common.fragment.TitledFragment
import arcus.app.pairing.device.steps.PairingStepsActivity
import arcus.app.pairing.device.steps.ViewPagerSelectedFragment
import arcus.app.pairing.device.steps.wifismartswitch.connect.WSSConnectFragment
import arcus.app.pairing.device.steps.wifismartswitch.informational.WSSSmartNetworkSwitchFragment
import kotlin.properties.Delegates


class WSSPairingStepsActivity : PairingStepsActivity(), WSSStepsNavigationDelegate {
    private var viewPagerFragments by Delegates.notNull<List<Fragment>>()

    override fun onResume() {
        super.onResume()
        NetworkConnectionMonitor.getInstance().suppressEvents(true)
    }

    override fun onPause() {
        super.onPause()
        NetworkConnectionMonitor.getInstance().suppressEvents(false)
    }

    override fun getPageChangedListener(fragments: List<Fragment>): ViewPager.OnPageChangeListener {
        viewPagerFragments = fragments

        return object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
                // No-Op
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                // No-Op
            }

            override fun onPageSelected(position: Int) {
                circlePageIndicator.setCurrentItem(position)
                if (snackBar?.isShownOrQueued() == true) {
                    snackBar?.dismiss()
                }

                val currentFragment = fragments[position]
                when (currentFragment) {
                    is WSSConnectFragment -> {
                        updateWSSConnectButtons(currentFragment)
                    }
                    is WSSSmartNetworkSwitchFragment -> {
                        updateWithWSSLearnMoreButton()
                        enableContinue()
                    }
                    else -> {
                        setupButton.visibility = View.GONE
                        updateNextButtonText(position)
                        enableContinue()
                    }
                }

                // Notify the previous one it is not selected anymore.
                val previousIndex = position - 1
                if (previousIndex >= 0 && fragments[previousIndex] is ViewPagerSelectedFragment) {
                    (fragments[previousIndex] as ViewPagerSelectedFragment).onNotSelected()
                }

                // Notify the current one it is selected.
                if (currentFragment is ViewPagerSelectedFragment) {
                    currentFragment.onPageSelected()
                }

                // Notify the next one it is not selected anymore.
                val nextIndex = position + 1
                if (nextIndex <= fragments.lastIndex && fragments[nextIndex] is ViewPagerSelectedFragment) {
                    (fragments[nextIndex] as ViewPagerSelectedFragment).onNotSelected()
                }

                if (currentFragment is TitledFragment) {
                    title = currentFragment.getTitle()
                }
            }
        }
    }

    fun updateWSSConnectButtons(fragment: WSSConnectFragment) {
        nextButton.text = getString(R.string.connect)
        setupButton.visibility = View.GONE

        nextButton.setOnClickListener {
            fragment.connectSmartPlug()
        }
    }

    fun updateWithWSSLearnMoreButton() {
        setupButton.text = getString(R.string.learn_more)
        nextButton.text = getString(R.string.pairing_next)
        setupButton.visibility = View.VISIBLE

        nextButton.setOnClickListener { _ ->
            viewPager.setCurrentItem(viewPager.currentItem + 1, true)
        }
        setupButton.setOnClickListener { _ ->
            ActivityUtils.launchUrl(GlobalSetting.SWANN_PAIRING_SUPPORT_URI)
        }
    }

    override fun enableContinue() {
        super.enableContinue()
        (viewPager as? DisableSwipeViewPager)?.disableSwipe = DisableSwipeViewPager.DisableSwipe.NONE
    }

    override fun disableContinue() {
        super.disableContinue()
        (viewPager as? DisableSwipeViewPager)?.disableSwipe = DisableSwipeViewPager.DisableSwipe.FORWARD
    }

    override fun getNextFragment(): Fragment? {
        val nextItem = viewPager.currentItem + 1
        if (nextItem < viewPagerFragments.size) {
            return viewPagerFragments[nextItem]
        }

        return null
    }

    companion object {
        @JvmStatic
        fun createIntent(context: Context, productAddress: String) = Intent(
            context,
            WSSPairingStepsActivity::class.java
        ).also {
            it.putExtra(ARG_PRODUCT_ADDRESS, productAddress)
        }
    }
}

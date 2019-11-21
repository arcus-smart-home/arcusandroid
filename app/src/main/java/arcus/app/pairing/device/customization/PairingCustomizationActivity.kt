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
package arcus.app.pairing.device.customization

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager
import android.util.Log
import arcus.app.R
import arcus.app.common.fragment.TitledFragment
import arcus.app.activities.ConnectedActivity
import arcus.presentation.pairing.device.customization.*

class PairingCustomizationActivity : ConnectedActivity(), CustomizationNavigationDelegate,
    CustomizationView {
    private lateinit var pairingDeviceAddress : String
    private lateinit var viewPager : ViewPager
    private val presenter : CustomizationPresenter =
        PairingCustomizationPresenterImpl()
    private var fragments = listOf<TitledFragment>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pairing_customization)
        setSupportActionBar(findViewById(R.id.toolbar))

        pairingDeviceAddress = intent.getStringExtra(ARG_PAIRING_DEVICE_ADDRESS) ?: "_UNKNOWN_"

        viewPager = findViewById(R.id.customization_view_pager)
        viewPager.offscreenPageLimit = 2
        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
                // No-Op
            }

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                // No-Op
            }

            override fun onPageSelected(position: Int) {
                supportActionBar?.setDisplayHomeAsUpEnabled(position != 0)
                setTitleFromActivePage(fragments[position])
            }
        })
    }

    override fun onResume() {
        super.onResume()
        presenter.setView(this)
        if (viewPager.adapter == null || viewPager.adapter?.count == 0) {
            presenter.loadDevice(pairingDeviceAddress)
        }
    }

    override fun navigateUpTo(upIntent: Intent?): Boolean {
        onBackPressed() // Simulate a hardware back press
        return false // We didn't redeliver the intent
    }

    override fun onBackPressed() {
        if (viewPager.currentItem == 0 || viewPager.adapter == null) {
            finish()
        } else {
            viewPager.setCurrentItem(viewPager.currentItem - 1, true)
        }
    }

    private fun setTitleFromActivePage(fragment: TitledFragment) {
        title = fragment.getTitle()
    }

    override fun navigateForwardAndComplete(type: CustomizationType) {
        presenter.completeCustomization(type)
        if ((viewPager.currentItem + 1) == viewPager.adapter?.count) {
            presenter.completeCustomization(CustomizationType.CUSTOMIZATION_COMPLETE)
            finish()
        } else {
            viewPager.setCurrentItem(viewPager.currentItem + 1, true)
        }
    }

    override fun cancelCustomization() {
        finish()
    }

    override fun customizationSteps(steps: List<CustomizationStep>) {
        Log.d("Customization", "Steps received: $steps")

        fragments = CustomizationStepFragmentFactory.forCustomizationStepList(
            pairingDeviceAddress,
            steps
        )

        viewPager.adapter = object : FragmentStatePagerAdapter(supportFragmentManager) {
            override fun getItem(position: Int) = fragments[position] as Fragment
            override fun getCount() = fragments.size
        }
        setTitleFromActivePage(fragments[0])
    }

    override fun showError(throwable: Throwable) {
        Log.e("Customization", "Error Received", throwable)
    }

    companion object {
        const val ARG_PAIRING_DEVICE_ADDRESS = "ARG_PAIRING_DEVICE_ADDRESS"

        @JvmStatic
        fun newIntent(context: Context, address: String) = Intent(context, PairingCustomizationActivity::class.java).also {
            it.putExtra(ARG_PAIRING_DEVICE_ADDRESS, address)
        }
    }

}

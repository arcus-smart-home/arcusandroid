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
package arcus.app.pairing.device.steps

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import com.google.android.material.snackbar.BaseTransientBottomBar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.core.content.ContextCompat
import androidx.viewpager.widget.ViewPager
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import arcus.app.R
import arcus.app.activities.DashboardActivity
import arcus.app.activities.GenericFragmentActivity
import arcus.app.common.fragments.ModalBottomSheet
import arcus.app.common.utils.ActivityUtils
import android.widget.Button
import arcus.app.common.view.ScleraTextView
import arcus.app.pairing.device.searching.DeviceSearchingActivity
import com.viewpagerindicator.CirclePageIndicator
import android.text.Spannable
import android.text.style.ForegroundColorSpan
import android.text.SpannableString
import arcus.app.activities.ConnectedActivity
import arcus.app.common.utils.GlobalSetting
import arcus.app.pairing.device.steps.deviceadded.DeviceAddedSnackBar
import arcus.app.pairing.device.steps.fragments.AssistantPairingStepFragment
import arcus.app.pairing.device.steps.fragments.DataFragment
import arcus.app.pairing.device.steps.fragments.OAuthPairingStepFragment
import arcus.app.pairing.device.steps.fragments.SimplePairingStepFragment
import arcus.app.pairing.device.steps.timeout.NoDevicesPairingTimeout
import arcus.app.pairing.device.steps.timeout.WithDevicesPairingTimeout
import arcus.presentation.pairing.device.steps.PairingStepsView
import arcus.presentation.pairing.device.steps.PairingStepsPresenter
import arcus.presentation.pairing.device.steps.PairingStepsPresenterImpl
import arcus.presentation.pairing.device.steps.ParsedPairingStep

open class PairingStepsActivity : ConnectedActivity(), PairingStepsView, StepsNavigationDelegate {
    private lateinit var productAddress : String
    private var presenter : PairingStepsPresenter = PairingStepsPresenterImpl(GlobalSetting.V3_HUB_TUTORIAL)
    private var popupShowing : ModalBottomSheet? = null
    var snackBar  : BaseTransientBottomBar<*>? = null
        private set
    lateinit var viewPager : ViewPager
        private set
    lateinit var circlePageIndicator : CirclePageIndicator
        private set
    lateinit var nextButton : Button
        private set
    lateinit var setupButton : Button
        private set

    private lateinit var watchTutorialBanner : LinearLayout
    private var adapter : FragmentStatePagerAdapter? = null
    private val formData = hashMapOf<String, String>()

    protected val isForReconnect by lazy {
        intent?.getBooleanExtra(ARG_IS_RECONNECT, false) ?: false
    }

    private val startPairingCallback = {
        presenter.startPairing(productAddress)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pairing_steps)
        setSupportActionBar(findViewById(R.id.toolbar))

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        productAddress = intent.getStringExtra(ARG_PRODUCT_ADDRESS) ?: "_ERROR_NO_ADDRESS_FOUND_"
        viewPager = findViewById(R.id.view_pager)
        circlePageIndicator = findViewById(R.id.circle_page_indicator)
        nextButton = findViewById(R.id.next_button)
        setupButton = findViewById(R.id.setup_button)
        watchTutorialBanner = findViewById(R.id.watch_tutorial_banner)

        nextButton.isEnabled = false
        viewPager.offscreenPageLimit = 1
    }

    override fun onResume() {
        super.onResume()

        presenter.setView(this)
        presenter.startPairing(productAddress, isForReconnect)
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.clearView()
        presenter.stopPairing()
    }

    override fun onPause() {
        super.onPause()
        dismissAndClearPopupShowing()
    }

    override fun navigateUpTo(upIntent: Intent?): Boolean {
        // Was delivering an intent with CLEAR_TOP set - which was causing the underlying activity
        // to get recreated - this preserves the underlying activity provided the system doesn't
        // kill it.

        onBackPressed() // Simulate a hardware back press
        return false // We didn't redeliver the intent
    }

    override fun onBackPressed() {
        when {
            viewPager.adapter == null || viewPager.currentItem == 0 -> super.onBackPressed()
            else -> viewPager.setCurrentItem(viewPager.currentItem - 1, true)
        }
    }


    // <editor-fold desc="Normal pairing steps logic here.">
    override fun updateView(pageTitle: String, steps: List<ParsedPairingStep>) {
        title = pageTitle
        setupSteps(steps)
        hideVideoUrlBanner()
    }

    override fun updateView(pageTitle: String, steps: List<ParsedPairingStep>, videoUrl: String) {
        title = pageTitle
        setupSteps(steps)
        showVideoUrlBanner(videoUrl)
    }

    private fun setupSteps(steps: List<ParsedPairingStep>) {
        if (viewPager.adapter == null) {
            nextButton.isEnabled = true

            val hasLocationPermission =
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            val fragments = StepFragmentFactory.forStepList(steps, hasLocationPermission)

            adapter = object : FragmentStatePagerAdapter(supportFragmentManager) {
                override fun getItem(position: Int): Fragment = fragments[position]
                override fun getCount(): Int = fragments.size
            }
            viewPager.adapter = adapter

            circlePageIndicator.setViewPager(viewPager)
            circlePageIndicator.setOnPageChangeListener(getPageChangedListener(fragments))
            updateNextButtonText(0)
        }
    }

    open fun getPageChangedListener(fragments: List<Fragment>) : ViewPager.OnPageChangeListener {
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
                if(currentFragment is AssistantPairingStepFragment) {
                    updateWithInstructionsButton(position)
                } else if (currentFragment is SimplePairingStepFragment && currentFragment.step.oAuthDetails?.oAuthUrl != null) {
                    updateNextButtonForOAuth(currentFragment)
                } else {
                    updateNextButtonText(position)
                    if (currentFragment is DataFragment) {
                        if (currentFragment.shouldEnableContinueButton()) {
                            enableContinue()
                        } else {
                            disableContinue()
                        }
                    } else {
                        enableContinue()
                    }
                }
            }
        }
    }

    fun updateWithInstructionsButton(selectedPosition: Int = 0 /* 0 Based */) {
        if (viewPager.adapter?.count == (selectedPosition + 1 /* convert to 1 based for compare */)) {
            setupButton.visibility = View.VISIBLE
            nextButton.text = getString(R.string.done_pairing)
            nextButton.setOnClickListener {
                presenter.clearView()
                presenter.stopPairing()
                val newIntent = Intent(this, DashboardActivity::class.java)
                newIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(newIntent)
            }
        }
    }

    fun updateNextButtonForOAuth(fragment: SimplePairingStepFragment) {
        nextButton.text = getString(R.string.pairing_next)
        nextButton.setOnClickListener {
            val bundle = Bundle()
            bundle.putParcelable(OAuthPairingStepFragment.OAUTH_DETAILS, fragment.step.oAuthDetails)

            startActivity(GenericFragmentActivity.getLaunchIntent(
                this,
                OAuthPairingStepFragment::class.java,
                fragmentArguments = bundle,
                allowBackPress = false
            ))
        }
    }

    open fun updateNextButtonText(selectedPosition: Int = 0 /* 0 Based */) {
        if (viewPager.adapter?.count == (selectedPosition + 1 /* convert to 1 based for compare */)) {
            nextButton.text = getString(R.string.start_searching)
            nextButton.setOnClickListener {
                addFormData()

                val intent = Intent(this@PairingStepsActivity, DeviceSearchingActivity::class.java)
                intent.putExtra(DeviceSearchingActivity.ARG_USER_INPUT_STEPS, formData)
                startActivity(intent)
            }
        } else {
            nextButton.text = getString(R.string.pairing_next)
            setupButton.visibility = View.GONE
            nextButton.setOnClickListener {
                addFormData()

                viewPager.setCurrentItem(viewPager.currentItem + 1, true)
            }
        }
    }

    private fun hideVideoUrlBanner() {
        watchTutorialBanner.visibility = View.GONE
    }

    private fun showVideoUrlBanner(videoUrl: String) {
        watchTutorialBanner.visibility = View.VISIBLE
        watchTutorialBanner.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(videoUrl)))
        }
    }
    // </editor-fold>

    // <editor-fold desc="Device Added - Banner">
    override fun devicesPaired(count: Int) {
        val rootContainer = findViewById<CoordinatorLayout>(R.id.coordinator_layout)
        val newSnackBar = DeviceAddedSnackBar
                .make(rootContainer)
                .setCount(count)
                .setAction {
                    startActivity(Intent(this@PairingStepsActivity, DeviceSearchingActivity::class.java))
                }
        dismissSnackBar()
        snackBar = newSnackBar
        newSnackBar.show()
    }
    // </editor-fold>

    // <editor-fold desc="Error logic handling here">
    override fun errorReceived(throwable: Throwable) {
        renderError(throwable)
    }

    private fun renderError(throwable: Throwable) {
        Log.e(TAG, "Rut row. Something went wrong!", throwable)
        setupErrorScreen()
    }

    private fun setupErrorScreen() {
        title = getString(R.string.product_name_placeholder)
        val text = SpannableString(title)
        text.setSpan(ForegroundColorSpan(ContextCompat.getColor(this, R.color.sclera_text_color_light_gray)), 0, text.length, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
        title = text

        watchTutorialBanner.visibility = View.GONE

        // This displays the mustard banner
        findViewById<LinearLayout>(R.id.sclera_banner).visibility = View.VISIBLE
        findViewById<ImageView>(R.id.chevron).visibility = View.GONE
        findViewById<ScleraTextView>(R.id.banner_text).text = getString(R.string.loading_delayed_try_again)

        if (viewPager.adapter == null) {
            val fragment = FailedToLoadStepsFragment()

            adapter = object : FragmentStatePagerAdapter(supportFragmentManager) {
                override fun getItem(position: Int): Fragment = fragment
                override fun getCount(): Int = 1
            }

            viewPager.adapter = adapter

            circlePageIndicator.visibility = View.GONE

            nextButton.text = ""
            disableContinue()
        }
    }

    override fun pairingTimedOut(hasDevicesPaired: Boolean) {
        if (hasDevicesPaired) {
            showWithDevicesPairingTimedOutPopup()
        } else {
            showNoDevicesPairingTimedOutPopup()
        }
    }
    // </editor-fold>

    // <editor-fold desc="Enable / Disable callbacks">
    override fun enableContinue() {
        nextButton.isEnabled = true
    }

    override fun disableContinue() {
        nextButton.isEnabled = false
    }

    override fun displaySecondButton(buttonText: String, instructionsUrl: Uri) {
        nextButton.text = resources.getString(R.string.done_pairing)
        setupButton.visibility = View.VISIBLE
        setupButton.text = buttonText
        setupButton.setOnClickListener {
            ActivityUtils.launchUrl(instructionsUrl)
        }
    }
    // </editor-fold>

    private fun showWithDevicesPairingTimedOutPopup() {
        dismissAndClearPopupShowing()
        val fragment = WithDevicesPairingTimeout()
        fragment.isCancelable = false
        fragment.setYesKeepSearchingListener(startPairingCallback)
        popupShowing = fragment
        fragment.show(supportFragmentManager, WithDevicesPairingTimeout::class.java.name)
    }

    private fun showNoDevicesPairingTimedOutPopup() {
        dismissAndClearPopupShowing()
        val fragment = NoDevicesPairingTimeout()
        fragment.isCancelable = false
        fragment.setKeepSearchingListener(startPairingCallback)
        popupShowing = fragment
        fragment.show(supportFragmentManager, NoDevicesPairingTimeout::class.java.name)
    }

    private fun dismissAndClearPopupShowing() {
        popupShowing?.cleanUp()
        popupShowing?.dismiss()
        popupShowing = null
    }

    private fun dismissSnackBar() {
        snackBar?.dismiss()
        snackBar = null
    }

    private fun addFormData() {
        adapter?.run {
            val current = getItem(viewPager.currentItem)
            if (current is DataFragment) {
                formData.putAll(current.formValues)
            }
        }
    }

    companion object {
        const val TAG = "PairingStepsActivity"
        const val ARG_PRODUCT_ADDRESS = "ARG_PRODUCT_ADDRESS"
        const val ARG_IS_RECONNECT = "ARG_IS_RECONNECT"
    }
}

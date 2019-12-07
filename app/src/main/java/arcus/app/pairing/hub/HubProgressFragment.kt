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
package arcus.app.pairing.hub

import android.animation.Animator
import android.animation.ValueAnimator
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Looper
import androidx.fragment.app.Fragment
import androidx.appcompat.widget.AppCompatImageView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.LinearLayout
import android.widget.ProgressBar
import arcus.cornea.utils.AndroidExecutor
import arcus.cornea.utils.ScheduledExecutor
import arcus.app.R
import arcus.app.activities.DashboardActivity
import arcus.app.activities.GenericConnectedFragmentActivity
import arcus.app.common.image.picasso.transformation.BlackWhiteInvertTransformation
import arcus.app.common.image.picasso.transformation.Invert
import arcus.app.common.utils.ActivityUtils
import arcus.app.common.utils.Errors
import arcus.app.common.view.ProgressBarFromToAnimation
import android.widget.Button
import arcus.app.common.view.ScleraLinkView
import arcus.app.common.view.ScleraTextView
import arcus.app.pairing.hub.customization.HubNameAndPictureFragment
import arcus.app.pairing.hub.popups.Generic2ButtonTextDescriptionPopup
import arcus.presentation.pairing.device.steps.blehub.HubFirmwareStatus
import arcus.presentation.pairing.device.steps.blehub.PairingHubPresenterImpl
import arcus.presentation.pairing.device.steps.blehub.PairingHubView
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

class HubProgressFragment : Fragment(), PairingHubView {

    private val presenter = PairingHubPresenterImpl()
    private val scheduledExecutor: ScheduledExecutor = AndroidExecutor(Looper.myLooper()!!)
    private val downloadingRunnable = {
        scheduledExecutor.clearExecutor()
        updateErrorState(Errors.Hub.INSTALL_TIMEDOUT)
    }
    private val applyingRunnable = {
        scheduledExecutor.clearExecutor()
        updateErrorState(Errors.Hub.FWUPGRADE_FAILED)
    }
    private val goToDashboardListener = {
        activity?.finish()
        presenter.cancelHubRegistration()
        startActivity(DashboardActivity.getHomeFragmentIntent(activity))
    }

    private lateinit var errorBanner: LinearLayout
    private lateinit var errorBannerIcon: AppCompatImageView
    private lateinit var errorBannerText: ScleraTextView
    private lateinit var progressBar: ProgressBar
    private lateinit var progressBarSpinner: ProgressBar
    private lateinit var progressPercent: ScleraTextView
    private lateinit var title: ScleraTextView
    private lateinit var description: ScleraTextView
    private lateinit var exitPairing: ScleraLinkView
    private lateinit var supportButton: Button

    private lateinit var hubId : String

    private var timerStarted = false
    private var applyingStarted = false
    private var animator : Animator? = null
    private val lastFirmwareStatus = AtomicReference<HubFirmwareStatus?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hubId = arguments?.getString(ARG_HUB_ID) ?: ""
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_hub_progress, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?.title = getString(R.string.update_available)

        errorBanner = view.findViewById(R.id.error_banner)
        errorBannerIcon = view.findViewById(R.id.error_banner_icon)
        errorBannerText = view.findViewById(R.id.error_banner_text)
        progressBar = view.findViewById(R.id.progress)
        progressBarSpinner = view.findViewById(R.id.spinner)
        progressPercent = view.findViewById(R.id.progress_percent)
        title = view.findViewById(R.id.hub_action_title)
        description = view.findViewById(R.id.hub_action_description)
        exitPairing = view.findViewById(R.id.exit_pairing)
        supportButton = view.findViewById(R.id.call_support_button)

        exitPairing.setOnClickListener {
            Generic2ButtonTextDescriptionPopup
                .newInstance(
                    getString(R.string.firmware_update_in_progess),
                    getString(R.string.devices_will_be_offline),
                    getString(R.string.no_continue_set_up),
                    getString(R.string.yes_dashboard)
                )
                .setBottomButtonListener(goToDashboardListener)
                .show(fragmentManager)
        }

        supportButton.setOnClickListener { ActivityUtils.callSupport() }
        presenter.registerHub(hubId)
    }

    override fun onResume() {
        super.onResume()
        presenter.setView(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopAnimatorAndClear()
        timerStarted = false
        scheduledExecutor.clearExecutor()
        presenter.cancelHubRegistration()
        presenter.clearView()
    }

    override fun onHubPairEvent() {
        updateProgressBar(100)
        title.postDelayed({
            // go to success screen
            activity?.let {
                startActivity(
                    GenericConnectedFragmentActivity.getLaunchIntent(
                        it,
                        HubNameAndPictureFragment::class.java,
                        allowBackPress = false
                    )
                )
                it.finish()
            }
        }, DELAY_AFTER_100_PERCENT_MS)
    }

    override fun onHubFirmwareStatusChange(status: HubFirmwareStatus, percentComplete: Int) {
        errorBanner.visibility = View.GONE
        val isStatusChange = lastFirmwareStatus.getAndSet(status) != status

        when (status) {
            HubFirmwareStatus.DOWNLOADING -> onDownloadingStatus(percentComplete, isStatusChange)
            HubFirmwareStatus.APPLYING -> {
                if (!applyingStarted) {
                    onApplyingStatus(isStatusChange)
                }
            }
        }
    }

    private fun onDownloadingStatus(percentComplete: Int, isStatusChange: Boolean) {
        // If the platform doesn't send us a percent value, show the indeterminate progressbar and generic text
        if(percentComplete == 0) {
            showIndeterminateProgress()
        } else {
            if (!timerStarted || isStatusChange) {
                resetProgressBar()
                timerStarted = true

                // After 11 minutes, if we haven't proceeded to Success screen, download failed
                scheduledExecutor.executeDelayed(MN_DOWNLOAD_TIMEOUT, command = downloadingRunnable)
            }

            updateProgressBar(percentComplete)
            title.text = getString(R.string.hub_update_available_title)
            description.text = getString(R.string.hub_update_available_description)
        }
    }

    private fun onApplyingStatus(isStatusChange: Boolean) {
        if (!timerStarted || isStatusChange) {
            resetProgressBar()
            timerStarted = true

            // After 5 minutes, if we haven't proceeded to Success screen, install failed
            scheduledExecutor.executeDelayed(MN_INSTALL_TIMEOUT, command = applyingRunnable)
        }

        // Then show the applying animation
        startApplyingAnimator()

        title.text = getString(R.string.hub_applying_update_title)
        description.text = getString(R.string.hub_applying_update_description)
    }

    override fun onHubPairError(error: String) {
        if (error == Errors.Process.CANCELLED) {
            activity?.onBackPressed()
        } else {
            updateErrorState(error)
        }
    }

    override fun onHubPairTimeout() {
        // TODO: What do?
    }

    private fun updateErrorState(error: String){
        resetProgressBar()

        exitPairing.visibility = View.VISIBLE
        progressBar.visibility = View.GONE
        progressPercent.visibility = View.GONE
        description.visibility = View.GONE
        supportButton.visibility = View.VISIBLE

        title.text = getString(R.string.hub_tap_support_description)

        when (error) {
            Errors.Hub.FWUPGRADE_FAILED -> {
                showDownloadFailed()
            }
            Errors.Hub.INSTALL_TIMEDOUT -> {
                showUpgradeFailed()
            }
        }
    }

    // <editor-fold desc="Showing/hiding views here">
    private fun showDownloadFailed() {
        presenter.cancelHubRegistration()
        exitPairing.setOnClickListener {
            Generic2ButtonTextDescriptionPopup
                .newInstance(
                    getString(R.string.exit_pairing_title),
                    getString(R.string.encourage_latest_firmware),
                    getString(R.string.yes_dashboard),
                    getString(R.string.cancel)
                )
                .setTopButtonListener(goToDashboardListener)
                .show(fragmentManager)
        }

        // Error banner - download failed
        showHubErrorBanner(R.string.hub_download_failed_error)

        activity?.title = getString(R.string.download_failed)
    }

    private fun showUpgradeFailed() {
        presenter.cancelHubRegistration()
        exitPairing.setOnClickListener {
            Generic2ButtonTextDescriptionPopup
                .newInstance(
                    getString(R.string.exit_pairing_title),
                    getString(R.string.encourage_apply_latest_firmware),
                    getString(R.string.yes_dashboard),
                    getString(R.string.cancel)
                )
                .setTopButtonListener(goToDashboardListener)
                .show(fragmentManager)
        }

        // Error banner - install failed
        showHubErrorBanner(R.string.hub_applying_update_failed_error)

        activity?.title = getString(R.string.install_failed)
    }

    private fun showHubErrorBanner(bannerText: Int) {
        errorBanner.visibility = View.VISIBLE
        val btw = BlackWhiteInvertTransformation(Invert.BLACK_TO_WHITE)
        val bitmap = BitmapFactory.decodeResource(context!!.resources, R.drawable.icon_alert_noconnection_outline)

        errorBannerIcon.setImageBitmap(btw.transform(bitmap))
        errorBannerText.text = getString(bannerText)
        activity?.title = getString(R.string.hub_error)
    }
    // </editor-fold>

    // <editor-fold desc="ProgressBar animation here.">
    private fun updateProgressBar(responseProgress: Int) {
        val progress = responseProgress / 10 * 10
        progressBar.visibility = View.VISIBLE
        progressPercent.visibility = View.VISIBLE
        val anim = ProgressBarFromToAnimation(progressBar, progressBar.progress.toFloat(), progress.toFloat())
        anim.duration = 100
        progressBar.progress = progress
        progressBar.startAnimation(anim)
        progressPercent.text = getString(R.string.lightsnswitches_percentage, progress)
    }

    private fun resetProgressBar(){
        hideIndeterminateProgress()
        timerStarted = false
        activity?.runOnUiThread {
            try {
                progressBar.progress = 0
                progressPercent.text = getString(R.string.lightsnswitches_percentage, 0)
            } catch (exception: IllegalStateException) {
                // Replace with external logger.
            }
        }
    }

    // We don't get progress updates for applying firmware, so we give it a 5 minute progress bar
    private fun startApplyingAnimator() {
        animator?.cancel()
        animator = getProgressAnimation(MN_INSTALL_TIMEOUT).also {
            it.start()
        }
        applyingStarted = true
    }
    private fun getProgressAnimation(
            animationDuration: Long,
            start: Int = 0,
            end: Int = 100
    ) = with (ValueAnimator.ofInt(start, end)) {
        duration = animationDuration

        interpolator = LinearInterpolator()

        addUpdateListener {
            val update = it.animatedValue as Int
            val current = progressBar.progress
            if (current != update) {
                progressBar.progress = update
                progressPercent.text = getString(R.string.lightsnswitches_percentage, update)
            }
        }
        this
    }

    private fun stopAnimatorAndClear() {
        animator = animator?.let {
            if (it.isStarted) {
                it.cancel()
            }

            null
        }
    }

    private fun showIndeterminateProgress() {
        progressBar.visibility = View.GONE
        progressPercent.visibility = View.GONE
        progressBarSpinner.visibility = View.VISIBLE
        title.text = getString(R.string.v3_hub_update_indeterminate_progressbar_title)
        description.text = getString(R.string.v3_hub_update_indeterminate_progressbar_description)
    }

    private fun hideIndeterminateProgress() {
        progressBar.visibility = View.VISIBLE
        progressPercent.visibility = View.VISIBLE
        progressBarSpinner.visibility = View.GONE
        title.text = getString(R.string.hub_update_available_title)
        description.text = getString(R.string.hub_update_available_description)
    }
    // </editor-fold>

    companion object {
        private const val DELAY_AFTER_100_PERCENT_MS = 1_250L
        private val MN_DOWNLOAD_TIMEOUT = TimeUnit.MINUTES.toMillis(11)
        private val MN_INSTALL_TIMEOUT = TimeUnit.MINUTES.toMillis(10)

        const val ARG_HUB_ID = "ARG_HUB_ID"

        @JvmStatic
        fun newInstance(hubId: String) = HubProgressFragment().also {
            it.arguments = createArgumentBundle(hubId)
            it.retainInstance = true
        }

        @JvmStatic
        fun createArgumentBundle(hubId: String) = Bundle().also {
            it.putString(ARG_HUB_ID, hubId)
        }
    }
}

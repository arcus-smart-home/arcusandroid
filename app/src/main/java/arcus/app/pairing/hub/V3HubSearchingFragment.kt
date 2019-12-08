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
import android.app.Activity
import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*

import arcus.app.R
import arcus.app.activities.GenericConnectedFragmentActivity
import arcus.app.common.fragment.BackPressInterceptor
import arcus.app.common.fragment.FragmentContainerHolder
import arcus.app.common.image.picasso.transformation.BlackWhiteInvertTransformation
import arcus.app.common.image.picasso.transformation.Invert
import arcus.app.common.utils.ActivityUtils
import arcus.app.common.utils.Errors
import arcus.app.common.utils.GlobalSetting
import android.widget.Button
import arcus.app.common.view.ScleraLinkView
import arcus.app.pairing.hub.customization.HubNameAndPictureFragment
import arcus.presentation.pairing.device.steps.blehub.HubFirmwareStatus
import arcus.presentation.pairing.device.steps.blehub.PairingHubPresenterImpl
import arcus.presentation.pairing.device.steps.blehub.PairingHubView
import com.google.android.material.textfield.TextInputLayout
import java.util.concurrent.TimeUnit
import kotlin.properties.Delegates

class V3HubSearchingFragment : Fragment(),
    BackPressInterceptor,
    PairingHubView {
    private val hubIdWatcher = HubIdInputTextWatcher()
    private val presenter = PairingHubPresenterImpl()

    private lateinit var hubTakingAWhileMustardBanner : TextView
    private lateinit var hubErrorsPinkBanner : LinearLayout
    private lateinit var errorBannerIcon: ImageView
    private lateinit var errorBannerText: TextView

    private lateinit var needHelpViewSwitcher : ViewSwitcher
    private lateinit var searchForNewHubButton: Button
    private lateinit var hubIdEntry: EditText
    private lateinit var hubIdEntryContainer: TextInputLayout

    private lateinit var progressBar: ProgressBar
    private lateinit var progressText: TextView
    private lateinit var hubIdConnecting: TextView

    private lateinit var factoryResetLink: ScleraLinkView
    private lateinit var callSupportButton: Button

    private var containerHost : FragmentContainerHolder? = null

    private var consumeBackPress = true
        set (value) {
            field = value
            containerHost?.showBackButtonOnToolbar(!consumeBackPress)
        }
    private var currentHubId by Delegates.notNull<String>()
    private var animator : Animator? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_v3_hub_searching, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        currentHubId = arguments?.getString(ARG_HUB_ID) ?: ""

        hubTakingAWhileMustardBanner = view.findViewById(R.id.hub_taking_a_while)
        hubErrorsPinkBanner = view.findViewById(R.id.error_banner)
        needHelpViewSwitcher = view.findViewById(R.id.need_help_view_switcher)
        errorBannerIcon = view.findViewById(R.id.error_banner_icon)
        errorBannerText = view.findViewById(R.id.error_banner_text)
        factoryResetLink = view.findViewById(R.id.factory_reset_link)
        factoryResetLink.setOnClickListener{
            ActivityUtils.launchUrl(Uri.parse(GlobalSetting.HUB_FACTORY_RESET_STEPS_URL))
        }
        callSupportButton = view.findViewById(R.id.call_support_button)
        callSupportButton.setOnClickListener {
            ActivityUtils.callSupport()
        }
        searchForNewHubButton = view.findViewById(R.id.search_for_new_hub_button)
        searchForNewHubButton.setOnClickListener {
            searchForNewHubId(hubIdEntry.text.toString())
        }

        hubIdEntry = view.findViewById(R.id.hub_id_entry)
        hubIdEntryContainer = view.findViewById(R.id.hub_id_entry_container)
        hubIdEntry.addTextChangedListener(hubIdWatcher)
        hubIdEntry.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                searchForNewHubButton.isEnabled = s?.toString() != currentHubId
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { /* Nop Nop */ }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { /* Nop Nop */ }
        })
        hubIdEntry.filters = hubIdInputFilers
        hubIdEntry.setOnEditorActionListener { _, actionId, _ ->
            if (hubIdWatcher.isValid && actionId == EditorInfo.IME_ACTION_DONE) {
                hubIdEntryContainer.error = null
                searchForNewHubId(hubIdEntry.text.toString())
                hideKeyboard()
            } else {
                hubIdEntryContainer.error = getString(hubIdWatcher.errorRes)
            }

            true
        }
        hubIdEntry.setText(currentHubId, TextView.BufferType.EDITABLE)

        view.findViewById<View>(R.id.need_help_link).setOnClickListener {
            ActivityUtils.launchUrl(GlobalSetting.HUB_TIMEOUT_HELP)
        }

        progressBar = view.findViewById(R.id.progress_bar)
        progressText = view.findViewById(R.id.progress_percent_text)
        progressBar.progress = 0
        progressText.text = String.format(PROGRESS_FORMAT, 0)

        hubIdConnecting = view.findViewById(R.id.hub_id_connecting)
        hubIdConnecting.text = getString(R.string.hub_searching_title, currentHubId)

        presenter.registerHub(currentHubId)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        containerHost = context as? FragmentContainerHolder?
        presenter.setView(this)
    }

    override fun onStart() {
        super.onStart()

        if (animator == null) {
            consumeBackPress = true
            startDefaultAnimator()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        stopAnimatorAndClear()
        presenter.cancelHubRegistration()
        presenter.clearView()
    }

    override fun onDetach() {
        super.onDetach()
        containerHost = null
    }

    override fun onHubPairEvent() {
        // If the hub is already paired (no firmware update), go to the success screen
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
    }

    override fun onHubFirmwareStatusChange(status: HubFirmwareStatus, percentComplete: Int) {
        // Either way we want to move forward to the status screen - it'll pick up where
        // this one left off.
        presenter.cancelHubRegistration()
        // Go to the hub progress fragment
        val args = Bundle()
        args.putString(HubProgressFragment.ARG_HUB_ID, currentHubId)

        activity?.let {
            startActivity(
                GenericConnectedFragmentActivity.getLaunchIntent(
                    it,
                    HubProgressFragment::class.java,
                    args,
                    allowBackPress = false
                )
            )
            it.finish()
        }
    }

    override fun onHubPairError(error: String) {
        consumeBackPress = false
        activity?.title = getString(R.string.hub_error)

        val bitmap = BitmapFactory.decodeResource(context!!.resources, R.drawable.icon_alert_noconnection_outline)
        val btw = BlackWhiteInvertTransformation(Invert.BLACK_TO_WHITE)
        errorBannerIcon.setImageBitmap(btw.transform(bitmap))

        hubTakingAWhileMustardBanner.visibility = View.GONE
        progressBar.visibility = View.GONE
        progressText.visibility = View.GONE
        needHelpViewSwitcher.visibility = View.GONE
        hubErrorsPinkBanner.visibility = View.VISIBLE

        callSupportButton.visibility = View.VISIBLE

        when (error){
            Errors.Hub.ALREADY_REGISTERED -> {      // E01
                errorBannerText. text = getString(R.string.hub_reporting_error_code, currentHubId, "E01")
                hubIdConnecting.text = getString(R.string.hub_error_description, currentHubId)
            }
            Errors.Hub.ORPHANED_HUB -> {    // E02
                factoryResetLink.visibility = View.VISIBLE     // Only for E02
                errorBannerText. text = getString(R.string.hub_reporting_error_code, currentHubId, "E02")
                hubIdConnecting.text = getString(R.string.v3_hub_error_description_orphaned, currentHubId)
            }
            Errors.Process.CANCELLED -> {
                hubErrorsPinkBanner.visibility = View.GONE
                // TODO - how do we handle this error case?
                activity?.onBackPressed()
            }
            else -> {
                // TODO: what do here? For now, recommend factory reset
                hubErrorsPinkBanner.visibility = View.GONE
                hubIdConnecting.text = getString(R.string.hub_error_description, currentHubId)
            }
        }
    }

    override fun onHubPairTimeout() {
        showHubTakingTooLong()
    }

    private fun hideKeyboard() {
        val imm = hubIdEntry.context?.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager?
        imm?.hideSoftInputFromWindow(hubIdEntry.windowToken, 0)
    }

    private fun startDefaultAnimator() {
        animator?.cancel()
        animator = getProgressAnimation(ANIMATION_MAX_DURATION).also {
            it.start()
        }
    }

    private fun stopAnimatorAndClear() {
        animator = animator?.let {
            if (it.isStarted) {
                it.cancel()
            }

            null
        }
    }

    private fun searchForNewHubId(hubId: String) {
        if (hubIdWatcher.isValid) {
            hubIdEntryContainer.error = null
            activity?.title = getString(R.string.pairing_hub)
            presenter.registerHub(hubId)

            currentHubId = hubId
            hubIdConnecting.text = getString(R.string.hub_searching_title, currentHubId)
            hubTakingAWhileMustardBanner.visibility = View.GONE
            needHelpViewSwitcher.showNext()

            startDefaultAnimator()
            consumeBackPress = true
        } else {
            hubIdEntryContainer.error = getString(hubIdWatcher.errorRes)
        }
    }

    private fun showHubTakingTooLong() {
        consumeBackPress = false
        activity?.title = getString(R.string.searching_for_hub)
        hubTakingAWhileMustardBanner.text = getString(R.string.v3_hub_longsearching_error, currentHubId)
        hubTakingAWhileMustardBanner.visibility = View.VISIBLE
        needHelpViewSwitcher.showNext()
    }

    private fun getProgressAnimation(
        animationDuration: Long,
        start: Int = 0,
        end: Int = 90
    ) = with (ValueAnimator.ofInt(start, end)) {
        duration = animationDuration

        interpolator = LinearInterpolator()

        addUpdateListener {
            val update = it.animatedValue as Int
            val current = progressBar.progress
            if (current != update) {
                progressBar.progress = update
                progressText.text = String.format(PROGRESS_FORMAT, update)
            }
        }
        this
    }

    override fun onBackPressed(): Boolean = consumeBackPress

    companion object {
        private val ANIMATION_MAX_DURATION = TimeUnit.MINUTES.toMillis(10)
        private const val ARG_HUB_ID = "ARG_HUB_ID"
        private const val PROGRESS_FORMAT = "%d%%"

        @JvmStatic
        fun newInstance(hubId: String) = V3HubSearchingFragment().also { fragment ->
            with (Bundle()) {
                putString(ARG_HUB_ID, hubId)
                fragment.arguments = this
            }
        }
    }
}

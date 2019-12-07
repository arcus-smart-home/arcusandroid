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
package arcus.app.pairing.device.post.zwaveheal

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import arcus.app.R
import arcus.app.common.fragments.ModalBottomSheet
import arcus.app.common.view.ProgressBarFromToAnimation
import android.widget.Button
import arcus.app.common.fragment.TitledFragment
import arcus.app.common.utils.inflate
import arcus.app.pairing.device.post.zwaveheal.popups.ConfirmCancelZWaveRebuildPopup
import arcus.app.pairing.device.post.zwaveheal.popups.ZWaveRebuildNotePopup
import arcus.presentation.pairing.device.post.zwaveheal.ZWaveRebuildPresenter
import arcus.presentation.pairing.device.post.zwaveheal.ZWaveRebuildPresenterImpl
import arcus.presentation.pairing.device.post.zwaveheal.ZWaveRebuildView
import org.slf4j.LoggerFactory

class RebuildingZWaveFragment : Fragment(),
    TitledFragment,
    ZWaveRebuildView {
    private var popupShowing : ModalBottomSheet? = null
    private lateinit var fragmentFlowCallback: FragmentFlow
    private lateinit var progressBar : ProgressBar
    private lateinit var progressPercentText: TextView
    private val successScreenNavigation = {
        if (isAdded && !isDetached) {
            fragmentFlowCallback.navigateTo(ZWaveRebuildSuccessFragment.newInstance())
        }
    }
    private val presenter : ZWaveRebuildPresenter = ZWaveRebuildPresenterImpl()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = container?.inflate(R.layout.fragment_zwave_rebuilding)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        progressBar = view.findViewById(R.id.progress)
        progressPercentText = view.findViewById(R.id.progress_percent)

        // Clicked Continue To Dashboard
        view.findViewById<Button>(R.id.continue_button).setOnClickListener{
            popupShowing?.dismiss()
            val popup = ZWaveRebuildNotePopup()
            popup.isCancelable = false
            popupShowing = popup
            popup.show(fragmentManager)
        }

        // Clicked Cancel Rebuild
        view.findViewById<Button>(R.id.cancel_button).setOnClickListener{
            popupShowing?.dismiss()
            val popup = ConfirmCancelZWaveRebuildPopup()
            popup.clickedCancelZwaveRebuildListener = {
                presenter.cancelRebuild()
                fragmentFlowCallback.navigateTo(ZWaveRebuildLaterFragment.newInstance())
            }
            popupShowing = popup
            popup.show(fragmentManager)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        fragmentFlowCallback = context as FragmentFlow
    }

    override fun onResume() {
        super.onResume()
        presenter.setView(this)
        presenter.startRebuild()
    }

    override fun onDestroy() {
        super.onDestroy()
        progressBar.removeCallbacks(successScreenNavigation)
        presenter.clearView()
    }

    override fun getTitle() = getString(R.string.zw_rebuild_header_default)

    override fun onProgressUpdated(percent: Int) {
        val anim = ProgressBarFromToAnimation(progressBar, progressBar.progress.toFloat(), percent.toFloat())
        anim.duration = 500
        progressBar.progress = percent
        progressBar.startAnimation(anim)

        if (percent == 100) {
            popupShowing?.dismiss()
            presenter.clearView()
            progressBar.progress = percent
            progressBar.postDelayed(successScreenNavigation, 750)
        }

        progressPercentText.text = "%d %%".format(percent)
    }

    override fun onUnhandledError() {
        logger.error("Something bad happened during Z-Wave Rebuild. Proceeding to completion.")
        onProgressUpdated(100)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(RebuildingZWaveFragment::class.java)

        @JvmStatic
        fun newInstance() = RebuildingZWaveFragment()
    }
}

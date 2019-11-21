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
package arcus.app.createaccount.almostfinished

import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import arcus.app.R
import arcus.app.common.view.ScleraTextView
import android.text.TextPaint
import android.text.style.MetricAffectingSpan
import arcus.app.activities.LaunchActivity
import arcus.app.common.utils.FontUtils


class AlmostFinishedFragmentV2 : Fragment(), AlmostFinishedView {
    private val presenter : AlmostFinishedPresenter = AlmostFinishedPresenterImpl()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_almost_finished_v2, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val loggedInAs = getString(R.string.logged_in_as) + " "
        val fullText = loggedInAs + arguments?.getString(ARG_PERSON_NAME) + ","
        val ssb = SpannableStringBuilder(fullText)
        ssb.setSpan(DemiBoldSpan(), loggedInAs.length, fullText.length, Spannable.SPAN_EXCLUSIVE_INCLUSIVE)

        view.findViewById<ScleraTextView>(R.id.name_text).text = ssb

        view.findViewById<ScleraTextView>(R.id.email_address).text = arguments?.getString(ARG_PERSON_EMAIL) ?: ""

        view.findViewById<ScleraTextView>(R.id.logout_link).setOnClickListener {
            presenter.logout()
        }
    }

    override fun onStart() {
        super.onStart()
        activity?.setTitle(R.string.account_almost_ready)
        presenter.setView(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.cleanUp()
    }

    override fun onLoggedOut() {
        val intent = Intent(context, LaunchActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        activity?.finish()

        startActivity(intent)
    }

    companion object {
        const val ARG_PERSON_NAME  = "ARG_PERSON_NAME"
        const val ARG_PERSON_EMAIL = "ARG_PERSON_EMAIL"

        @JvmStatic
        fun newInstance(personName: String, personEmail: String) : AlmostFinishedFragmentV2 {
            val fragment = AlmostFinishedFragmentV2()
            with (Bundle()) {
                putString(ARG_PERSON_NAME, personName)
                putString(ARG_PERSON_EMAIL, personEmail)
                fragment.arguments = this
            }
            return fragment
        }
    }

    private inner class DemiBoldSpan : MetricAffectingSpan() {
        override fun updateDrawState(ds: TextPaint) {
            applyDemiBold(ds)
        }

        override fun updateMeasureState(paint: TextPaint) {
            applyDemiBold(paint)
        }

        private fun applyDemiBold(paint: Paint) {
            paint.typeface = FontUtils.getDemi()
        }
    }
}

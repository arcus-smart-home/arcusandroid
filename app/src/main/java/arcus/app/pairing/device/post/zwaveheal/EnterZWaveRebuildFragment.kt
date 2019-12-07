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
import arcus.app.R
import android.widget.Button
import arcus.app.common.fragment.TitledFragment
import arcus.app.pairing.device.post.zwaveheal.popups.ConfirmCancelZWaveRebuildPopup

class EnterZWaveRebuildFragment : Fragment(),
    TitledFragment {
    private lateinit var mCallback: FragmentFlow

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_enter_zwave_rebuild, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Clicked REBUILD NOW
        view.findViewById<Button>(R.id.continue_button).setOnClickListener{
            mCallback.navigateTo(StartZWaveRebuildFragment.newInstance())
        }

        // Clicked REBUILD LATER
        view.findViewById<Button>(R.id.cancel_rebuild_button).setOnClickListener{
            val popup = ConfirmCancelZWaveRebuildPopup()
            popup.clickedCancelZwaveRebuildListener = {
                mCallback.navigateTo(ZWaveRebuildLaterFragment.newInstance())
            }
            popup.show(fragmentManager)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mCallback = context as FragmentFlow
    }

    override fun getTitle() = getString(R.string.zw_rebuild_header_default)

    companion object {
        @JvmStatic
        fun newInstance() = EnterZWaveRebuildFragment()
    }
}

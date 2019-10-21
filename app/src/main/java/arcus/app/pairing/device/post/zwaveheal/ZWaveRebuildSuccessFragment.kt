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
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import arcus.app.R
import arcus.app.common.view.ScleraButton
import arcus.app.common.fragment.TitledFragment

class ZWaveRebuildSuccessFragment : Fragment(),
    TitledFragment {
    private lateinit var mCallback: FragmentFlow

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_zwave_rebuild_success, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<ScleraButton>(R.id.continue_button).setOnClickListener {
            mCallback.completeFlow()
        }
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        mCallback = context as FragmentFlow
    }

    override fun getTitle() = getString(R.string.zw_rebuild_header_default)

    companion object {
        @JvmStatic
        fun newInstance() =
            ZWaveRebuildSuccessFragment()
    }
}
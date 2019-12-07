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
import org.slf4j.LoggerFactory

class StartZWaveRebuildFragment : Fragment(),
    TitledFragment {
    private lateinit var mCallback: FragmentFlow

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        logger.info("Started ZWave Rebuild")
        return inflater.inflate(R.layout.fragment_start_zwave_rebuild, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<Button>(R.id.rebuild_button).setOnClickListener{
            mCallback.navigateTo(RebuildingZWaveFragment.newInstance())
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mCallback = context as FragmentFlow
    }

    override fun getTitle() = getString(R.string.zw_rebuild_header_default)

    companion object {

        @JvmStatic
        private val logger = LoggerFactory.getLogger(StartZWaveRebuildFragment::class.java)

        @JvmStatic
        fun newInstance() =
            StartZWaveRebuildFragment()
    }
}

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
package arcus.app.pairing.hub.kickoff

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import arcus.app.ArcusApplication
import arcus.app.R
import arcus.app.activities.BaseActivity
import arcus.app.common.error.ErrorManager
import arcus.app.pairing.hub.kickoff.adapters.HubKitAdapter
import arcus.presentation.kits.HubKitPresenter
import arcus.presentation.kits.HubKitPresenterImpl
import arcus.presentation.kits.HubKitView
import arcus.presentation.kits.ProductAddModel

class HubKitFragment : Fragment(), HubKitView {

    private val presenter: HubKitPresenter = HubKitPresenterImpl()
    private lateinit var adapter: HubKitAdapter
    private lateinit var hubKitsList: RecyclerView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_hub_or_kit, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        hubKitsList = view.findViewById(R.id.hubs_and_kits)
        hubKitsList.layoutManager = LinearLayoutManager(context)
        ViewCompat.setNestedScrollingEnabled(hubKitsList, false)
    }

    override fun onResume() {
        super.onResume()

        presenter.setView(this)
        presenter.showHubKits(ArcusApplication.getContext())

        if (arguments?.getBoolean(ARG_SHOULD_HIDE_ACTION_BAR) == true) {
            val actionBar = (context as BaseActivity).supportActionBar
            actionBar?.hide()
        }
    }

    override fun onHubKitsReceived(hubs: List<ProductAddModel>) {
        adapter = HubKitAdapter(context, hubs)
        hubKitsList.adapter = adapter
    }

    fun onError(throwable: Throwable) {
        ErrorManager.`in`(activity).showGenericBecauseOf(throwable)
    }


    companion object {
        private const val ARG_SHOULD_HIDE_ACTION_BAR = "ARG_SHOULD_HIDE_ACTION_BAR"

        @JvmStatic
        fun newInstanceHidingToolbar(shouldHideActionBar: Boolean = true) = HubKitFragment().also { fragment ->
            with(Bundle()) {
                putBoolean(ARG_SHOULD_HIDE_ACTION_BAR, shouldHideActionBar)
                fragment.arguments = this
            }
        }
    }
}

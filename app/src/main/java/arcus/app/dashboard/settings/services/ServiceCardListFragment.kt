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
package arcus.app.dashboard.settings.services

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.h6ah4i.android.widget.advrecyclerview.animator.RefactoredDefaultItemAnimator
import com.h6ah4i.android.widget.advrecyclerview.draggable.RecyclerViewDragDropManager
import arcus.cornea.subsystem.SubsystemController
import com.iris.client.capability.Alarm
import com.iris.client.capability.AlarmSubsystem
import com.iris.client.model.SubsystemModel
import arcus.app.R
import arcus.app.common.fragment.FragmentContainerHolder
import arcus.app.common.utils.GlobalSetting
import arcus.app.common.utils.inflate
import android.widget.Button
import arcus.app.common.view.ScleraLinkView
import arcus.app.dashboard.settings.adapters.InactiveServicesListAdapter
import arcus.app.dashboard.settings.adapters.ServiceCardListAdapter
import java.util.*


class ServiceCardListFragment : Fragment() {

    private var activeAdapter: ServiceCardListAdapter? = null
    private var mInactiveAdapter: InactiveServicesListAdapter? = null

    val title: String
        get() = getString(R.string.cards)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? = container?.inflate(R.layout.fragment_service_card_list)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?.title = title
        val container = activity
        if (container is FragmentContainerHolder) {
            container.showBackButtonOnToolbar(true)
        }

        val activeCardsList = getView()?.findViewById<View>(R.id.active_list) as RecyclerView
        val mRecyclerViewDragDropManager = RecyclerViewDragDropManager()

        activity?.run {
            activeAdapter = ServiceCardListAdapter(this, ServiceListDataProvider(this))
            activeCardsList.layoutManager = LinearLayoutManager(this)
        }

        activeAdapter?.let { activeAdapter ->
            activeCardsList.adapter = mRecyclerViewDragDropManager.createWrappedAdapter(activeAdapter)
            activeAdapter.setVisibleItemsChecked()
        }

        activeCardsList.itemAnimator = RefactoredDefaultItemAnimator()
        mRecyclerViewDragDropManager.attachRecyclerView(activeCardsList)


        val mInactiveRecyclerView = getView()?.findViewById<View>(R.id.inactive_list) as RecyclerView
        mInactiveRecyclerView.setHasFixedSize(false)

        activity?.run {
            val inactiveServices = getInactiveServices()
            mInactiveAdapter = InactiveServicesListAdapter(this, inactiveServices)
            mInactiveRecyclerView.layoutManager = LinearLayoutManager(this)
            mInactiveRecyclerView.isNestedScrollingEnabled = false
            mInactiveRecyclerView.adapter = mInactiveAdapter
        }

        val makeCardsActive: ScleraLinkView = view.findViewById(R.id.make_cards_active)
        makeCardsActive.setLinkTextAndTarget(getString(R.string.make_cards_active), GlobalSetting.ACTIVATE_CARDS_URL)

        view.findViewById<Button>(R.id.close_button).setOnClickListener {
            activity?.finish()
        }
    }

    private fun getInactiveServices(): List<ServiceListItemModel> {
        val inactiveServices = arrayListOf<ServiceListItemModel>()

        // Get the saved order of service cards
        val serviceOrder = Arrays.asList(*ServiceCard.values())

        for (thisCard in serviceOrder) {
            val data = ServiceListItemModel(context?.resources?.getString(thisCard.titleStringResId), thisCard)

            // Check the checkbox for previously visible cards
            val index = data.serviceCard.name.split("_".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray().size - 1
            val cardName = data.serviceCard.name.split("_".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[index]

            val inactiveSubsystems = ArrayList<SubsystemModel>()

            for (subsystemModel in SubsystemController.instance().subsystems.values()) {
                if (!subsystemModel.available || subsystemModel[AlarmSubsystem.ATTR_ALARMSTATE] == Alarm.ALERTSTATE_INACTIVE) {
                    inactiveSubsystems.add(subsystemModel)
                }
            }

            val notComingSoonTitle = thisCard.descriptionStringResId != R.string.card_energy_desc
            val notComingSoonDescription = thisCard.descriptionStringResId != R.string.card_windows_and_blinds_desc

            if (notComingSoonDescription && notComingSoonTitle) {
                for (inactiveSubsystem in inactiveSubsystems) {
                    val subsystemName = inactiveSubsystem.name.replace("Subsystem", "").toUpperCase()

                    if (subsystemName.contains(cardName)) {
                        inactiveServices.add(data)
                    }
                }
            }
        }

        return inactiveServices
    }
}

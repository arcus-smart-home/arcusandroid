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
package arcus.app.account.settings.list

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import arcus.app.R
import arcus.app.account.settings.places.SelectPlaceFragment
import arcus.app.account.settings.profile.SettingsProfileFragment
import arcus.app.common.backstack.BackstackManager
import arcus.app.common.fragments.CoreFragment
import arcus.app.launch.InvitationFragment
import arcus.cornea.model.PlacesWithRoles
import arcus.presentation.common.view.ViewState
import arcus.presentation.settings.list.SettingsListViewModel
import arcus.app.common.error.ErrorManager.`in` as errorIn

class SideNavSettingsFragment : CoreFragment<SettingsListViewModel>() {
    private lateinit var profileContainer: View
    private lateinit var peopleContainer: View
    private lateinit var placesContainer: View
    private lateinit var invitationContainer: View
    private lateinit var sideNavSettingsContainer: View

    override val viewModelClass: Class<SettingsListViewModel> = SettingsListViewModel::class.java
    override val title: String
        get() = getString(R.string.sidenav_settings_title)
    override val layoutId: Int = R.layout.fragment_sidenav_settings

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        profileContainer = view.findViewById(R.id.profileContainer)
        peopleContainer = view.findViewById(R.id.peopleContainer)
        placesContainer = view.findViewById(R.id.placesContainer)
        invitationContainer = view.findViewById(R.id.invitationContainer)
        sideNavSettingsContainer = view.findViewById(R.id.side_nav_settings_container)

        viewModel.viewState.observe(viewLifecycleOwner, Observer {
            when (it) {
                is ViewState.Loaded -> setupView(it.item)
                is ViewState.Error<*, *> -> errorIn(activity).showGenericBecauseOf(it.error)
            }
        })
    }

    private fun setupView(placesWithRoles: PlacesWithRoles) {
        profileContainer.setOnClickListener {
            SettingsProfileFragment.newInstance(placesWithRoles).navigateTo()
        }
        peopleContainer.setOnClickListener {
            val nextScreen = SelectPlaceFragment.PEOPLE_SCREEN
            SelectPlaceFragment.newInstance(nextScreen, null, placesWithRoles).navigateTo()
        }
        placesContainer.setOnClickListener {
            val nextScreen = SelectPlaceFragment.PLACES_SCREEN
            val topText = getString(R.string.select_place_to_manage)
            SelectPlaceFragment.newInstance(nextScreen, topText, placesWithRoles).navigateTo()
        }
        invitationContainer.setOnClickListener {
            InvitationFragment.newInstanceFromSettings().navigateTo()
        }
    }

    private fun Fragment.navigateTo() {
        BackstackManager.getInstance().navigateToFragment(this, true)
    }

    companion object {
        @JvmStatic
        fun newInstance(): SideNavSettingsFragment = SideNavSettingsFragment()
    }
}

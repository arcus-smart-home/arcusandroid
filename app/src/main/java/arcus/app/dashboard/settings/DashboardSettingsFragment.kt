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
package arcus.app.dashboard.settings

import android.os.Bundle
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import arcus.app.BuildConfig
import arcus.app.ArcusApplication
import arcus.app.R
import arcus.app.activities.GenericConnectedFragmentActivity
import arcus.app.activities.GenericFragmentActivity
import arcus.app.common.fragment.FragmentContainerHolder
import arcus.app.common.image.ImageManager
import arcus.app.common.image.picasso.transformation.AlphaPreset
import arcus.app.common.utils.inflate
import arcus.app.dashboard.settings.favorites.FavoritesListFragment
import arcus.app.dashboard.settings.services.ServiceCardListFragment
import arcus.app.subsystems.debug.DebugMenuFragment

class DashboardSettingsFragment : Fragment() {

    private lateinit var favoritesView: ConstraintLayout
    private lateinit var cardsView: ConstraintLayout
    private lateinit var backgroundPhotoView: ConstraintLayout
    private lateinit var devView: ConstraintLayout

    val title: String
        get() = getString(R.string.dashboard_settings_title)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? = container?.inflate(R.layout.fragment_dashboard_settings)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?.title = title

        val containerHolder: FragmentContainerHolder? = activity as FragmentContainerHolder
        containerHolder?.showBackButtonOnToolbar(true)

        favoritesView = view.findViewById(R.id.favorites_settings_view)
        cardsView = view.findViewById(R.id.cards_settings_view)
        backgroundPhotoView = view.findViewById(R.id.background_photo_settings_view)
        devView = view.findViewById(R.id.dev_settings_view)

        favoritesView.setOnClickListener {
            context?.run {
                startActivity(
                    GenericConnectedFragmentActivity.getLaunchIntent(
                        this,
                        FavoritesListFragment::class.java))
            }
        }

        cardsView.setOnClickListener {
            context?.run {
                startActivity(
                    GenericConnectedFragmentActivity.getLaunchIntent(
                        this,
                        ServiceCardListFragment::class.java))
            }
        }

        backgroundPhotoView.setOnClickListener {

            ImageManager.with(activity)
                .putUserGeneratedPlaceImage(ArcusApplication.getRegistrationContext()?.placeModel?.id)
                .fromCameraOrGallery()
                .intoWallpaper(AlphaPreset.DARKEN)
                .execute()
        }

        if (BuildConfig.DEBUG) {
            devView.setOnClickListener {
                context?.run { startActivity(GenericFragmentActivity.getLaunchIntent(this, DebugMenuFragment::class.java)) }
            }

            devView.visibility = View.VISIBLE
        }
    }
}

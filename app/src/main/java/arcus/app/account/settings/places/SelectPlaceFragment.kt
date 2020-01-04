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
package arcus.app.account.settings.places

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.IntDef
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import arcus.app.R
import arcus.app.account.settings.SettingsPeopleDetailsList
import arcus.app.account.settings.SettingsPlaceOverviewFragment
import arcus.app.account.settings.adapter.PeopleAndPlacesRVAdapter
import arcus.app.account.settings.adapter.PeopleAndPlacesRVAdapter.OnItemClicked
import arcus.app.account.settings.pin.SettingsUpdatePin
import arcus.app.common.backstack.BackstackManager
import arcus.app.common.controller.PlacesAndPeopleController
import arcus.app.common.error.ErrorManager
import arcus.app.common.fragments.NoViewModelFragment
import arcus.app.common.image.ImageManager
import arcus.app.common.image.picasso.transformation.CropCircleTransformation
import arcus.app.common.models.ListItemModel
import arcus.app.common.models.ModelTypeListItem
import arcus.cornea.SessionController
import arcus.cornea.model.PersonModelProxy
import arcus.cornea.model.PlaceAndRoleModel
import arcus.cornea.model.PlacesWithRoles
import arcus.cornea.provider.AvailablePlacesProvider
import arcus.cornea.utils.Listeners
import com.google.common.collect.Iterators
import com.iris.capability.util.Addresses
import com.iris.client.model.PersonModel
import java.util.ArrayList
import java.util.Locale

// TODO: This class was just converted to Kotlin and still needs significant work to update it.
class SelectPlaceFragment : NoViewModelFragment(), PlacesAndPeopleController.Callback {
    var nextFrag = 0
    private lateinit var topLL: View
    private lateinit var pinPlaceContainer: View
    private lateinit var personPlaceListing: RecyclerView
    private lateinit var placesContainer: LinearLayout
    private lateinit var placesWithRoles: PlacesWithRoles
    private var topTextString: String? = null
    private var topText: TextView? = null
    private var personsMap: Map<PlaceAndRoleModel, List<PersonModelProxy>> = emptyMap()

    @Transient
    var personModel: PersonModel? = null

    @Retention(AnnotationRetention.SOURCE)
    @IntDef(
        PIN_CODE_SCREEN,
        PLACES_SCREEN,
        PEOPLE_SCREEN
    )
    annotation class NextScreenType

    override val title: String
        get() = if (nextFrag == PEOPLE_SCREEN) getString(R.string.people_people) else getString(R.string.choose_place)
    override val layoutId: Int = R.layout.fragment_text_list_view

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val args = arguments
        if (args == null) {
            nextFrag =
                PIN_CODE_SCREEN
            topText = null
            return
        }
        nextFrag = args.getInt(
            NEXT_FRAG,
            PIN_CODE_SCREEN
        )
        topTextString = args.getString(TOP_TEXT, null)

        val placesAndRoles = args.getParcelable(PLACE_ROLE) as PlacesWithRoles?
        if (placesAndRoles != null) {
            placesWithRoles = placesAndRoles
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        topText = view.findViewById(R.id.text_view1)
        topLL = view.findViewById(R.id.text_view_linear_layout)
        placesContainer = view.findViewById(R.id.places_container)
        personPlaceListing = view.findViewById(R.id.people_and_places_rv)
        pinPlaceContainer = view.findViewById(R.id.pin_or_place_listing_container)
    }

    override fun onResume() {
        super.onResume()
        personModel = SessionController.instance().person ?: return

        if (TextUtils.isEmpty(topTextString)) {
            topLL.isGone = true
            topText?.text = null
        } else {
            topLL.isVisible = true
            topText?.text = topTextString
        }
        setTitle()

        if (::placesWithRoles.isInitialized) {
            setupView()
        } else {
            // TODO: Shouldn't this always be passed in since the previous fragment had this already?...
            progressContainer.isVisible = true
            AvailablePlacesProvider.instance().loadPlacesWithRoles()
                .onFailure(Listeners.runOnUiThread { throwable ->
                    progressContainer.isGone = true
                    ErrorManager.`in`(activity).showGenericBecauseOf(throwable)
                })
                .onSuccess(Listeners.runOnUiThread { roles ->
                    progressContainer.isGone = true
                    placesWithRoles = roles
                    setupView()
                })
        }
    }

    override fun onPause() {
        super.onPause()
        progressContainer.isGone = true
    }

    private fun setupView() {
        if (PEOPLE_SCREEN == nextFrag) {
            progressContainer.isVisible = true
            PlacesAndPeopleController(placesWithRoles, this).getPeopleAtEachPlace()
        } else {
            personPlaceListing.isGone = true
            pinPlaceContainer.isVisible = true
            val size = placesWithRoles.ownedPlaces.size + placesWithRoles.unownedPlaces.size + 3
            val placeItems: MutableList<ListItemModel> = ArrayList(size)
            if (placesWithRoles.ownsPlaces()) {
                val item = ListItemModel(getString(R.string.account_owner))
                item.setIsHeadingRow(true)
                placeItems.add(item)
                for (place in placesWithRoles.sortedOwnedPlaces) {
                    placeItems.add(getListItem(place))
                }
            }
            if (placesWithRoles.hasGuestAccess()) {
                val item = ListItemModel(getString(R.string.people_guest))
                item.setIsHeadingRow(true)
                placeItems.add(item)
                for (place in placesWithRoles.sortedUnownedPlaces) {
                    placeItems.add(getListItem(place))
                }
            }
            modelsParsed(placeItems)
        }
    }

    private fun modelsParsed(placeItems: List<ListItemModel>) {
        placesContainer.removeAllViews()
        val it = Iterators.peekingIterator(placeItems.iterator())
        while (it.hasNext()) {
            val next = it.next()
            val convertView = if (next.isHeadingRow) {
                val convertView = LayoutInflater
                    .from(activity)
                    .inflate(R.layout.section_heading_with_count, placesContainer, false)
                configureViewForHeading(next, convertView)
                convertView
            } else {
                val convertView = LayoutInflater
                    .from(activity)
                    .inflate(R.layout.icon_text_and_abstract_item, placesContainer, false)
                configureViewForNonHeader(next, convertView, it.hasNext() && it.peek().isHeadingRow, !it.hasNext())
                convertView
            }
            convertView.setOnClickListener {
                val fragment: Fragment
                when (nextFrag) {
                    PLACES_SCREEN -> {
                        fragment =
                            SettingsPlaceOverviewFragment.newInstance(
                                next.data as PlaceAndRoleModel,
                                placesWithRoles.totalPlaces
                            )
                    }
                    PIN_CODE_SCREEN -> fragment = SettingsUpdatePin.newInstance(
                        SettingsUpdatePin.ScreenVariant.SETTINGS,
                        personModel!!.address,
                        next.address
                    )
                    else -> fragment = SettingsUpdatePin.newInstance(
                        SettingsUpdatePin.ScreenVariant.SETTINGS,
                        personModel!!.address,
                        next.address
                    )
                }
                BackstackManager.getInstance().navigateToFragment(fragment, true)
            }
            placesContainer.addView(convertView)
        }
    }

    private fun getListItem(
        place: PlaceAndRoleModel
    ): ListItemModel = ListItemModel(place.name, place.streetAddress1).apply {
        address = place.address
        data = place
    }

    private fun configureViewForNonHeader(
        item: ListItemModel,
        view: View,
        nextIsHeader: Boolean,
        isLastRow: Boolean
    ) {
        val text = view.findViewById<TextView>(R.id.title)
        val subText = view.findViewById<TextView>(R.id.list_item_description)
        val subText2 = view.findViewById<TextView>(R.id.list_item_sub_description1)
        val imageView = view.findViewById<View>(R.id.image_icon) as ImageView
        text.text = item.text.toString().toUpperCase(Locale.getDefault())
        val model = item.data as PlaceAndRoleModel
        subText.text = String.format(
            "%s %s",
            model.streetAddress1,
            model.streetAddress2?.trim().orEmpty()
        )
        subText2.text = model.cityStateZip
        subText2.visibility = View.VISIBLE
        ImageManager.with(activity)
            .putPlaceImage(Addresses.getId(item.address))
            .withTransform(CropCircleTransformation())
            .into(imageView)
            .execute()
        if (nextIsHeader || isLastRow) {
            view.findViewById<View>(R.id.bottom_divider).isGone = true
        } else {
            view.findViewById<View>(R.id.bottom_divider).isVisible = true
        }
    }

    private fun configureViewForHeading(headingData: ListItemModel, view: View) {
        val headingLeft = view.findViewById<TextView>(R.id.sectionName)
        headingLeft.text = headingData.text
        view.isEnabled = false
    }

    override fun onError(throwable: Throwable) {
        progressContainer.isGone = true
        ErrorManager.`in`(activity).showGenericBecauseOf(throwable)
    }

    override fun onSuccess(
        persons: List<ModelTypeListItem>,
        personsMapping: Map<PlaceAndRoleModel, List<PersonModelProxy>>
    ) {
        progressContainer.isGone = true
        personsMap = personsMapping
        personPlaceListing.isVisible = true
        pinPlaceContainer.isGone = true
        personPlaceListing.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        val adapter = PeopleAndPlacesRVAdapter(requireActivity(), persons, true)
        adapter.setPersonClickedCallback(OnItemClicked { item ->
            if (item.additionalData !is PersonModelProxy || personsMap.isEmpty()) {
                return@OnItemClicked
            }
            val models = personsMap[item.associatedPlaceModel] ?: return@OnItemClicked
            val person = item.additionalData as PersonModelProxy?
            BackstackManager.getInstance().navigateToFragment(
                SettingsPeopleDetailsList.newInstance(
                    models,
                    person,
                    item.associatedPlaceModel
                ),
                true
            )
        })
        if (personPlaceListing.adapter != null) {
            personPlaceListing.swapAdapter(adapter, true)
        } else {
            personPlaceListing.adapter = adapter
        }
    }

    companion object {
        private const val TOP_TEXT = "TOP_TEXT"
        private const val NEXT_FRAG = "NEXT_FRAG"
        private const val PLACE_ROLE = "PLACE_ROLE"
        const val PIN_CODE_SCREEN = 0x0A
        const val PLACES_SCREEN = 0x0B
        const val PEOPLE_SCREEN = 0x0C

        @JvmStatic
        fun newInstance(
            @NextScreenType nextFragment: Int,
            topText: String?,
            placesWithRoles: PlacesWithRoles?
        ): SelectPlaceFragment = SelectPlaceFragment().apply {
            arguments = with(Bundle(3)) {
                putInt(NEXT_FRAG, nextFragment)
                putString(TOP_TEXT, topText)
                putParcelable(PLACE_ROLE, placesWithRoles)
                this
            }
        }
    }
}

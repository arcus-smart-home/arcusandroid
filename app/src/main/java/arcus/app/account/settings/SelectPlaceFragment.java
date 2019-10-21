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
package arcus.app.account.settings;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.common.collect.Iterators;
import com.google.common.collect.PeekingIterator;
import arcus.cornea.SessionController;
import arcus.cornea.model.PersonModelProxy;
import arcus.cornea.model.PlaceAndRoleModel;
import arcus.cornea.model.PlacesWithRoles;
import arcus.cornea.provider.AvailablePlacesProvider;
import arcus.cornea.utils.Listeners;
import com.iris.capability.util.Addresses;
import com.iris.client.event.Listener;
import com.iris.client.model.PersonModel;
import arcus.app.ArcusApplication;
import arcus.app.R;
import arcus.app.account.settings.adapter.PeopleAndPlacesRVAdapter;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.controller.BackstackPopListener;
import arcus.app.common.controller.PlacesAndPeopleController;
import arcus.app.common.error.ErrorManager;
import arcus.app.common.fragments.BaseFragment;
import arcus.app.common.image.ImageManager;
import arcus.app.common.image.Wallpaper;
import arcus.app.common.image.picasso.transformation.CropCircleTransformation;
import arcus.app.common.models.ListItemModel;
import arcus.app.common.models.ModelTypeListItem;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SelectPlaceFragment extends BaseFragment implements BackstackPopListener, PlacesAndPeopleController.Callback {
    private static String TOP_TEXT = "TOP_TEXT", NEXT_FRAG = "NEXT_FRAG", PLACE_ROLE = "PLACE_ROLE";
    public  static final int PIN_CODE_SCREEN = 0x0A, PLACES_SCREEN = 0x0B, PEOPLE_SCREEN = 0x0C;

    int nextFrag;
    View topLL, pinPlaceContainer;
    RecyclerView personPlaceListing;
    LinearLayout placesContainer;
    TextView topText;
    String topTextString;
    PlacesWithRoles placesWithRoles;
    Map<PlaceAndRoleModel, List<PersonModelProxy>> personsMap;
    transient PersonModel personModel;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({ PIN_CODE_SCREEN, PLACES_SCREEN, PEOPLE_SCREEN })
    public @interface NextScreenType {}

    public static SelectPlaceFragment newInstance(@NextScreenType int nextFragment, @Nullable String topText, PlacesWithRoles placesWithRoles) {
        SelectPlaceFragment fragment = new SelectPlaceFragment();
        Bundle args = new Bundle(3);

        args.putInt(NEXT_FRAG, nextFragment);
        args.putString(TOP_TEXT, topText);
        args.putParcelable(PLACE_ROLE, placesWithRoles);

        fragment.setArguments(args);
        return fragment;
    }

    @Override public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args == null) {
            nextFrag = PIN_CODE_SCREEN;
            topText = null;
            return;
        }

        nextFrag  = args.getInt(NEXT_FRAG, PIN_CODE_SCREEN);
        topTextString = args.getString(TOP_TEXT, null);
        placesWithRoles = args.getParcelable(PLACE_ROLE);
    }

    @Override public void onResume() {
        super.onResume();
        View rootView = getView();
        personModel = SessionController.instance().getPerson();
        if (rootView == null || personModel == null) {
            return;
        }

        topText  = (TextView) rootView.findViewById(R.id.text_view1);
        topLL = rootView.findViewById(R.id.text_view_linear_layout);
        placesContainer = (LinearLayout) rootView.findViewById(R.id.places_container);
        personPlaceListing = (RecyclerView) rootView.findViewById(R.id.people_and_places_rv);
        pinPlaceContainer = rootView.findViewById(R.id.pin_or_place_listing_container);
        ImageManager.with(ArcusApplication.getContext()).setWallpaper(Wallpaper.ofCurrentPlace().darkened());

        if (TextUtils.isEmpty(topTextString)) {
            topLL.setVisibility(View.GONE);
            topText.setText(null);
        }
        else {
            topLL.setVisibility(View.VISIBLE);
            topText.setText(topTextString);
        }

        setTitle();
        if (placesWithRoles != null) {
            setupView();
        }
        else {
            showProgressBar();
            AvailablePlacesProvider.instance().loadPlacesWithRoles()
                  .onFailure(Listeners.runOnUiThread(new Listener<Throwable>() {
                      @Override public void onEvent(Throwable throwable) {
                          hideProgressBar();
                          ErrorManager.in(getActivity()).showGenericBecauseOf(throwable);
                      }
                  }))
                  .onSuccess(Listeners.runOnUiThread(new Listener<PlacesWithRoles>() {
                      @Override public void onEvent(PlacesWithRoles roles) {
                          hideProgressBar();
                          placesWithRoles = roles;
                          setupView();
                      }
                  }));
        }
    }

    @Override public void onPause() {
        super.onPause();
        hideProgressBar();
    }

    public void setupView() {
        if (PEOPLE_SCREEN == nextFrag) {
            showProgressBar();
            new PlacesAndPeopleController(placesWithRoles, this).getPeopleAtEachPlace();
        }
        else {
            personPlaceListing.setVisibility(View.GONE);
            pinPlaceContainer.setVisibility(View.VISIBLE);
            int size = placesWithRoles.getOwnedPlaces().size() + placesWithRoles.getUnownedPlaces().size() + 3;
            final List<ListItemModel> placeItems = new ArrayList<>(size);

            if (placesWithRoles.ownsPlaces()) {
                ListItemModel item = new ListItemModel(getString(R.string.account_owner));
                item.setIsHeadingRow(true);
                placeItems.add(item);

                for (PlaceAndRoleModel place : placesWithRoles.getSortedOwnedPlaces()) {
                    placeItems.add(getListItem(place));
                }
            }

            if (placesWithRoles.hasGuestAccess()) {
                ListItemModel item = new ListItemModel(getString(R.string.people_guest));
                item.setIsHeadingRow(true);
                placeItems.add(item);

                for (PlaceAndRoleModel place : placesWithRoles.getSortedUnownedPlaces()) {
                    placeItems.add(getListItem(place));
                }
            }

            modelsParsed(placeItems);
        }
    }

    @Override public void onPopped() {
        ImageManager.with(ArcusApplication.getContext()).setWallpaper(Wallpaper.ofCurrentPlace().darkened());
    }

    protected void modelsParsed(List<ListItemModel> placeItems) {
        placesContainer.removeAllViews();
        PeekingIterator<ListItemModel> it = Iterators.peekingIterator(placeItems.iterator());
        while (it.hasNext()) {
            final ListItemModel next = it.next();
            int layout = next.isHeadingRow() ? R.layout.heading_item : R.layout.icon_text_and_abstract_item;
            final View convertView = LayoutInflater.from(getActivity()).inflate(layout, placesContainer, false);
            if (next.isHeadingRow()) {
                configureViewForHeading(next, convertView);
            }
            else {
                configureViewForNonHeader(next, convertView, it.hasNext() && it.peek().isHeadingRow(), !it.hasNext());
            }
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    Fragment fragment;
                    switch (nextFrag) {
                        case PLACES_SCREEN:
                            fragment = SettingsPlaceOverviewFragment.newInstance((PlaceAndRoleModel) next.getData(), placesWithRoles.getTotalPlaces());
                            placesWithRoles = null;
                            break;

                        default:
                        case PIN_CODE_SCREEN:
                            fragment = SettingsUpdatePin.newInstance(SettingsUpdatePin.ScreenVariant.SETTINGS, personModel.getAddress(), next.getAddress());
                            break;
                    }

                    BackstackManager.getInstance().navigateToFragment(fragment, true);
                }
            });
            placesContainer.addView(convertView);
        }
    }

    protected ListItemModel getListItem(PlaceAndRoleModel place) {
        ListItemModel item = new ListItemModel(place.getName(), place.getStreetAddress1());
        item.setAddress(place.getAddress());
        item.setData(place);

        return item;
    }

    @Nullable @Override public String getTitle() {
        return nextFrag == PEOPLE_SCREEN ? getString(R.string.people_people) : getString(R.string.choose_place);
    }

    @Override public Integer getLayoutId() {
        return R.layout.fragment_text_list_view;
    }

    protected void configureViewForNonHeader(@NonNull ListItemModel item, View view, boolean nextIsHeader, boolean isLastRow) {
        TextView text = (TextView) view.findViewById(R.id.title);
        TextView subText = (TextView) view.findViewById(R.id.list_item_description);
        TextView subText2 = (TextView) view.findViewById(R.id.list_item_sub_description1);
        ImageView imageView = (ImageView) view.findViewById(R.id.image_icon);

        text.setText(String.valueOf(item.getText()).toUpperCase());

        PlaceAndRoleModel model = (PlaceAndRoleModel) item.getData();
        subText.setText(String.format("%s %s", model.getStreetAddress1(), org.apache.commons.lang3.StringUtils.stripToEmpty(model.getStreetAddress2())));
        subText2.setText(model.getCityStateZip());
        subText2.setVisibility(View.VISIBLE);

        ImageManager.with(getActivity())
              .putPlaceImage(Addresses.getId(item.getAddress()))
              .withTransform(new CropCircleTransformation())
              .into(imageView)
              .execute();
        view.findViewById(R.id.bottom_divider).setVisibility((nextIsHeader || isLastRow) ? View.GONE : View.VISIBLE);
    }

    protected void configureViewForHeading(@NonNull ListItemModel headingData, View view) {
        TextView headingLeft = (TextView) view.findViewById(R.id.heading_text);
        headingLeft.setTextColor(Color.WHITE);
        headingLeft.setText(headingData.getText());
        view.setEnabled(false);
    }

    @Override public void onError(Throwable throwable) {
        hideProgressBar();
        ErrorManager.in(getActivity()).showGenericBecauseOf(throwable);
    }

    @Override public void onSuccess(@NonNull List<ModelTypeListItem> persons, Map<PlaceAndRoleModel, List<PersonModelProxy>> personsMapping) {
        hideProgressBar();
        this.personsMap = personsMapping;
        personPlaceListing.setVisibility(View.VISIBLE);
        pinPlaceContainer.setVisibility(View.GONE);
        personPlaceListing.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        PeopleAndPlacesRVAdapter adapter = new PeopleAndPlacesRVAdapter(getActivity(), persons, true);
        adapter.setPersonClickedCallback(new PeopleAndPlacesRVAdapter.OnItemClicked() {
            @Override public void itemClicked(ModelTypeListItem item) {
                if (!(item.getAdditionalData() instanceof PersonModelProxy) || personsMap == null) {
                    return;
                }

                List<PersonModelProxy> models = personsMap.get(item.getAssociatedPlaceModel());
                if (models == null) {
                    return;
                }

                PersonModelProxy person = (PersonModelProxy) item.getAdditionalData();
                BackstackManager.getInstance().navigateToFragment(
                      SettingsPeopleDetailsList.newInstance(models, person, item.getAssociatedPlaceModel()),
                      true
                );
            }
        });

        if (personPlaceListing.getAdapter() != null) {
            personPlaceListing.swapAdapter(adapter, true);
        }
        else {
            personPlaceListing.setAdapter(adapter);
        }
    }
}

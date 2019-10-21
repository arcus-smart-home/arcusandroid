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

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.common.collect.ImmutableMap;
import arcus.cornea.model.PlacesWithRoles;
import arcus.cornea.platformcall.BillableEntitiesController;
import com.iris.client.capability.Place;
import com.iris.client.model.PlaceModel;
import arcus.app.R;
import arcus.app.account.registration.AccountBillingInfoFragment;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.error.ErrorManager;
import arcus.app.common.fragments.BaseFragment;
import arcus.app.common.image.ImageManager;
import arcus.app.common.image.picasso.transformation.CropCircleTransformation;
import arcus.app.common.utils.ActivityUtils;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class SettingsBillingFragment extends BaseFragment {
    static final String PLACE_ROLE = "PLACE_ROLE";
    LinearLayout servicePlanContainer;
    View noServicePlanContainer, haveServicePlansContainer, paymentInfoRL, shopNowButton;
    PlacesWithRoles placesWithRoles;
    private final Map<String, Integer> serviceMappings = ImmutableMap.<String, Integer>builder()
            .put(Place.SERVICELEVEL_BASIC, R.string.basic_plan)
            .put(Place.SERVICELEVEL_PREMIUM, R.string.premium_plan)
            .put(Place.SERVICELEVEL_PREMIUM_FREE, R.string.premium_free_plan)
            .put(Place.SERVICELEVEL_PREMIUM_PROMON, R.string.pro_monitoring_plan)
            .put(Place.SERVICELEVEL_PREMIUM_PROMON_FREE, R.string.pro_monitoring_free_plan)
            .put(Place.SERVICELEVEL_PREMIUM_PROMON_ANNUAL, R.string.pro_monitoring_annual_plan)
            .put(Place.SERVICELEVEL_PREMIUM_ANNUAL, R.string.premium_annual_plan)
            .build();
    private final Map<String, Integer> addonMappings = ImmutableMap.of(
            "CELLBACKUP", R.string.backup_cellular
    );

    @NonNull
    public static SettingsBillingFragment newInstance(PlacesWithRoles placesWithRoles) {
        SettingsBillingFragment fragment = new SettingsBillingFragment();
        Bundle bundle = new Bundle(1);
        bundle.putParcelable(PLACE_ROLE, placesWithRoles);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            placesWithRoles = getArguments().getParcelable(PLACE_ROLE);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        paymentInfoRL = view.findViewById(R.id.payment_info_cell);
        servicePlanContainer = (LinearLayout) view.findViewById(R.id.service_plan_container);
        noServicePlanContainer = view.findViewById(R.id.no_service_plan_container);
        haveServicePlansContainer = view.findViewById(R.id.have_service_plan_container);
        shopNowButton = view.findViewById(R.id.shop_now);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        setTitle();
        servicePlanContainer.removeAllViews();
        noServicePlanContainer.setVisibility(View.GONE);
        if (placesWithRoles == null) {
            return;
        }

        showProgressBar();
        BillableEntitiesController.listBillablePlaces(placesWithRoles, new BillableEntitiesController.Callback() {
            @Override
            public void onError(Throwable throwable) {
                hideProgressBar();
                ErrorManager.in(getActivity()).showGenericBecauseOf(throwable);
            }

            @Override
            public void onSuccess(@Nullable List<PlaceModel> places) {
                hideProgressBar();
                if (places != null) {
                    processPlaces(places);
                }
            }
        });
    }

    protected void processPlaces(@NonNull List<PlaceModel> places) {
        Iterator<PlaceModel> placeIterator = places.iterator();
        if (placeIterator.hasNext()) {
            haveServicePlansContainer.setVisibility(View.VISIBLE);
            noServicePlanContainer.setVisibility(View.GONE);
            paymentInfoRL.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    BackstackManager.getInstance().navigateToFragment(
                            AccountBillingInfoFragment.newInstance(AccountBillingInfoFragment.ScreenVariant.SETTINGS), true
                    );
                }
            });
            while (placeIterator.hasNext()) {
                PlaceModel place = placeIterator.next();
                View container = LayoutInflater.from(getActivity()).inflate(R.layout.service_plan_item, servicePlanContainer, false);
                createServiceLevelView(container, place, placeIterator.hasNext());
                servicePlanContainer.addView(container);
            }
        } else { // Empty list, show them 'hey setup your own place info'
            haveServicePlansContainer.setVisibility(View.GONE);
            noServicePlanContainer.setVisibility(View.VISIBLE);
            shopNowButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ActivityUtils.launchShopNow();
                }
            });
        }
    }

    protected void createServiceLevelView(View rootView, PlaceModel place, boolean hasNext) {
        ImageView imageView = (ImageView) rootView.findViewById(R.id.place_image);
        TextView placeName = (TextView) rootView.findViewById(R.id.place_name);
        TextView placeStreet = (TextView) rootView.findViewById(R.id.place_street);
        TextView placeLocation = (TextView) rootView.findViewById(R.id.place_location);
        TextView serviceLevel = (TextView) rootView.findViewById(R.id.plan_service_level);
        TextView addons = (TextView) rootView.findViewById(R.id.plan_addons);
        View divider = rootView.findViewById(R.id.divider);

        ImageManager.with(getActivity())
                .putPlaceImage(place.getId())
                .withTransform(new CropCircleTransformation())
                .into(imageView)
                .execute();

        placeName.setText(String.valueOf(place.getName()).toUpperCase());
        placeStreet.setText(place.getStreetAddress1());
        placeLocation.setText(getLocationFromPlace(place));
        Integer service = serviceMappings.get(place.getServiceLevel());
        serviceLevel.setText((service != null) ? getString(service) : place.getServiceLevel());

        Set<String> addonSet = place.getServiceAddons();
        if (addonSet == null || addonSet.isEmpty()) {
            addons.setVisibility(View.GONE);
        } else {
            Set<String> sortedAddons = new TreeSet<>();
            for (String s : addonSet) {
                Integer string = addonMappings.get(s);
                sortedAddons.add((string != null) ? getString(string) : s);
            }
            addons.setText(TextUtils.join(", ", sortedAddons));
        }

        divider.setVisibility(hasNext ? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    public void onPause() {
        super.onPause();
        hideProgressBar();
    }

    protected String getLocationFromPlace(PlaceModel place) {
        String result = TextUtils.isEmpty(place.getCity()) ? "" : place.getCity();
        result += TextUtils.isEmpty(place.getState()) ? "" : ", " + place.getState();
        return result + (TextUtils.isEmpty(place.getZipCode()) ? "" : " " + place.getZipCode());
    }

    @Override
    public String getTitle() {
        return getString(R.string.account_settings_billing_title);
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_account_settings_billing;
    }
}

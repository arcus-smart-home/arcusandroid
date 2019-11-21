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
package arcus.app.common.cards.view;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import arcus.cornea.SessionController;
import arcus.cornea.model.PlaceAndRoleModel;
import arcus.cornea.provider.AvailablePlacesProvider;
import arcus.cornea.utils.Listeners;
import com.iris.capability.util.Addresses;
import com.iris.client.event.Listener;
import com.iris.client.event.ListenerRegistration;
import com.iris.client.model.PlaceModel;
import com.iris.client.session.SessionActivePlaceSetEvent;
import arcus.app.R;
import arcus.app.activities.GenericConnectedFragmentActivity;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.cards.TopImageCard;
import arcus.app.common.events.PlaceChangeRequestedEvent;
import arcus.app.common.image.ImageManager;
import arcus.app.common.popups.SelectPlacePopup;
import arcus.app.common.utils.LoginUtils;
import arcus.app.dashboard.settings.DashboardSettingsFragment;

import java.util.List;

import de.greenrobot.event.EventBus;


public class TopImageCardItemView extends RecyclerView.ViewHolder {

    View selectPlaceContainer;
    Context context;
    ImageView settingsButton;
    ImageView homeImage;
    TextView placeName;

    ListenerRegistration placeChangedListener;
    final SessionController.ActivePlaceCallback activePlaceCallback =
            new SessionController.ActivePlaceCallback() {
                @Override public void activePlaceChanged() {
                    Listeners.clear(placeChangedListener);
                    LoginUtils.completeLogin(); // To rewrite active place ID
                    setUpPlaceSpinner();
                }

                @Override public void onError(Throwable throwable) {
                    Listeners.clear(placeChangedListener);
                    //ErrorManager.in(getActivity()).showGenericBecauseOf(throwable);
                }
            };
    public TopImageCardItemView(View view) {
        super(view);
        settingsButton = (ImageView) view.findViewById(R.id.settings_image);
        selectPlaceContainer = view.findViewById(R.id.select_place_container);
        homeImage = (ImageView) view.findViewById(R.id.home_image);
        placeName = (TextView) view.findViewById(R.id.place_name);
        context = view.getContext();
    }


    public void onEvent(SessionActivePlaceSetEvent event) {
        setUpPlaceSpinner();
    }

    public void build(@NonNull TopImageCard card) {

        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(@NonNull View v) {
                final int id = v.getId();
                switch (id) {
                    case R.id.settings_image:
                        context.startActivity(GenericConnectedFragmentActivity.getLaunchIntent(context, DashboardSettingsFragment.class));
                        break;
                }
            }
        });
        settingsButton.setVisibility(card.isHideSettingsGear() ? View.GONE: View.VISIBLE);

        String placeID = card.getPlaceID();
        if (!TextUtils.isEmpty(placeID)) {
            card.setPlaceID(placeID);
            setCenterImage(card);
        }

            String name = card.getPlaceName();
            if (name != null) {
                placeName.setText(name);
                selectPlaceContainer.setVisibility(View.VISIBLE);
            }
            else {
                selectPlaceContainer.setVisibility(View.GONE);
            }

        setUpPlaceSpinner();
    }

    protected void setCenterImage(TopImageCard card) {

        ImageManager.with(context)
              .putPlaceImage(card.getPlaceID())
              .fit()
              .centerCrop()
              .into(homeImage)
              .execute();
    }

    public void setUpPlaceSpinner(){
        if (context == null) {
            return;
        }

        final PlaceModel placeModel = SessionController.instance().getPlace();
        if (placeModel == null) {
            return;
        }
        selectPlaceContainer.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                selectPlaceContainer.setEnabled(false);
                AvailablePlacesProvider.instance().load()
                    .onFailure(Listeners.runOnUiThread(new Listener<Throwable>() {
                        @Override public void onEvent(Throwable throwable) {
                            selectPlaceContainer.setEnabled(true);
                            //ErrorManager.in(getActivity()).showGenericBecauseOf(throwable);
                        }
                    }))
                    .onSuccess(Listeners.runOnUiThread(new Listener<List<PlaceAndRoleModel>>() {
                        @Override public void onEvent(List<PlaceAndRoleModel> placeAndRoleModels) {
                            selectPlaceContainer.setEnabled(true);

                            SelectPlacePopup popup = SelectPlacePopup.newInstance(placeAndRoleModels, placeModel.getAddress());
                            popup.setCallback(new SelectPlacePopup.Callback() {
                                @Override public void itemSelectedAddress(String placeAddress) {
                                    if (!placeModel.getAddress().equals(placeAddress)) {
                                        EventBus.getDefault().post(new PlaceChangeRequestedEvent(placeAddress));
                                        placeChangedListener = SessionController.instance().setCallback(activePlaceCallback);
                                        SessionController.instance().changeActivePlace(Addresses.getId(placeAddress));
                                    }
                                }
                            });

                            BackstackManager.getInstance().navigateToFloatingFragment(popup, popup.getClass().getCanonicalName(), true);
                        }
                    }));
            }
        });
    }

}

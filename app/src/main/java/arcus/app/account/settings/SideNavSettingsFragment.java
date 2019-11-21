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
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import arcus.cornea.model.PlacesWithRoles;
import arcus.cornea.provider.AvailablePlacesProvider;
import arcus.cornea.utils.Listeners;
import com.iris.client.event.Listener;
import arcus.app.R;
import arcus.app.common.controller.BackstackPopListener;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.error.ErrorManager;
import arcus.app.common.fragments.BaseFragment;
import arcus.app.common.image.ImageManager;
import arcus.app.common.image.Wallpaper;
import arcus.app.launch.InvitationFragment;

public class SideNavSettingsFragment extends BaseFragment implements BackstackPopListener {
    View profileContainer, peopleContainer, placesContainer,invitationContainer, sideNavSettingsContainer;
    final Listener<Throwable> errorListener = Listeners.runOnUiThread(new Listener<Throwable>() {
        @Override public void onEvent(Throwable throwable) {
            hideProgressBar();
            ErrorManager.in(getActivity()).showGenericBecauseOf(throwable);
        }
    });

    @NonNull public static SideNavSettingsFragment newInstance() {
        return new SideNavSettingsFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        if (view != null) {
            profileContainer = view.findViewById(R.id.profile_container);
            peopleContainer = view.findViewById(R.id.people_container);
            placesContainer = view.findViewById(R.id.places_container);
            invitationContainer = view.findViewById(R.id.invitation_container);
            sideNavSettingsContainer = view.findViewById(R.id.side_nav_settings_container);
        }

        return view;
    }

    public void onPopped() {
        ImageManager.with(getActivity()).setWallpaper(Wallpaper.ofCurrentPlace().darkened());
    }

    @Override public void onResume() {
        super.onResume();
        setTitle();
        if (sideNavSettingsContainer == null) {
            return;
        }

        showProgressBar();
        AvailablePlacesProvider.instance().loadPlacesWithRoles()
              .onFailure(errorListener)
              .onSuccess(Listeners.runOnUiThread(new Listener<PlacesWithRoles>() {
                  @Override public void onEvent(PlacesWithRoles roles) {
                      setupView(roles);
                  }
              }));
    }

    protected void setupView(final PlacesWithRoles placesWithRoles) {
        profileContainer.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                BackstackManager.getInstance().navigateToFragment(SettingsProfileFragment.newInstance(placesWithRoles), true);
            }
        });
        peopleContainer.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                BackstackManager.getInstance().navigateToFragment(SelectPlaceFragment.newInstance(SelectPlaceFragment.PEOPLE_SCREEN, null, placesWithRoles), true);
            }
        });
        placesContainer.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                BackstackManager.getInstance().navigateToFragment(SelectPlaceFragment.newInstance(SelectPlaceFragment.PLACES_SCREEN, getString(R.string.select_place_to_manage), null), true);
            }
        });
        invitationContainer.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                BackstackManager.getInstance().navigateToFragment(InvitationFragment.newInstanceFromSettings(), true);
            }
        });

        hideProgressBar();
        sideNavSettingsContainer.setVisibility(View.VISIBLE);
    }

    @Override public void onPause() {
        super.onPause();
        hideProgressBar();
        if (sideNavSettingsContainer != null) {
            sideNavSettingsContainer.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public String getTitle() {
        return getString(R.string.sidenav_settings_title);
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_sidenav_settings;
    }

}

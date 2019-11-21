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
package arcus.app.subsystems.homenfamily;

import android.os.Bundle;
import androidx.annotation.NonNull;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import arcus.cornea.subsystem.presence.PresenceStatusController;
import arcus.cornea.subsystem.presence.model.PresenceModel;
import com.iris.client.event.ListenerRegistration;
import arcus.app.R;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.error.ErrorManager;
import arcus.app.common.fragments.BaseFragment;
import arcus.app.common.fragments.IClosedFragment;
import arcus.app.common.fragments.IShowedFragment;
import arcus.app.common.image.ImageManager;
import arcus.app.common.image.Wallpaper;
import arcus.app.common.models.PicListItemModel;
import arcus.app.device.pairing.post.NameDeviceFragment;
import arcus.app.subsystems.homenfamily.adapters.PicListDataAdapter;
import arcus.app.subsystems.homenfamily.controllers.HomeNFamilyFragmentController;

import org.apache.commons.lang3.ObjectUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class HomeNFamilyStatusFragment extends BaseFragment implements PresenceStatusController.Callback, HomeNFamilyFragmentController.Callbacks, IShowedFragment, IClosedFragment {

    private ListView allPeopleList;
    private ListenerRegistration mPresenceListener;
    private List<PicListItemModel> homeItems = new ArrayList<>();
    private List<PicListItemModel> awayItems = new ArrayList<>();

    @NonNull
    public static HomeNFamilyStatusFragment newInstance() {
        return new HomeNFamilyStatusFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        setHasOptionsMenu(true);
        allPeopleList = (ListView) view.findViewById(android.R.id.list);
        return view;
    }

    @NonNull
    @Override
    public String getTitle() {
        return getActivity().getString(R.string.homenfamily_title);
    }

    @Override
    public Integer getLayoutId() {
        return android.R.layout.list_content;
    }

    // Presence Callbacks

    @Override
    public void showHome(List<PresenceModel> home) {
        HomeNFamilyFragmentController.getInstance().setListener(this);
        HomeNFamilyFragmentController.getInstance().getPicListItemsForPresence(home, HomeNFamilyFragmentController.PresenceTag.HOME);
    }

    @Override
    public void showAway(List<PresenceModel> away) {
        HomeNFamilyFragmentController.getInstance().setListener(this);
        HomeNFamilyFragmentController.getInstance().getPicListItemsForPresence(away, HomeNFamilyFragmentController.PresenceTag.AWAY);
    }

    @Override
    public void onPicListItemModelsLoaded(List<PicListItemModel> picListItems, @NonNull final HomeNFamilyFragmentController.PresenceTag tag) {
        if (picListItems == null) {
            picListItems = Collections.emptyList();
        }

        switch (tag) {
            case HOME:
                homeItems = new ArrayList<>(picListItems);
                break;

            case AWAY:
                awayItems = new ArrayList<>(picListItems);
                break;
        }

        if (homeItems == null) {
            homeItems = Collections.emptyList();
        }
        if (awayItems == null) {
            awayItems = Collections.emptyList();
        }

        List<PicListItemModel> allPeople = new ArrayList<>(homeItems.size() + awayItems.size() + 3);
        // Iterate through the items starting with HOME then followed by AWAY
        // Next iterate through the Home people. (After adding the header)
        PicListItemModel homeHeader = new PicListItemModel(getString(R.string.homenfamily_home), homeItems.size());
        allPeople.add(homeHeader);
        if (homeItems.isEmpty()) {
            allPeople.add(new PicListItemModel(getString(R.string.homenfamily_home_list_empty)));
        } else {
            Collections.sort(homeItems, ORDER);
            allPeople.addAll(homeItems);
        }

        // Next iterate through the away people. (After adding the header)
        PicListItemModel awayHeader = new PicListItemModel(getString(R.string.homenfamily_away), awayItems.size());
        allPeople.add(awayHeader);
        if (awayItems.isEmpty()) {
            allPeople.add(new PicListItemModel(getString(R.string.homenfamily_away_list_empty)));
        } else {
            Collections.sort(awayItems, ORDER);
            allPeople.addAll(awayItems);
        }

        allPeopleList.setAdapter(new PicListDataAdapter(getActivity(), allPeople, false));
        allPeopleList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (!(parent.getAdapter() instanceof PicListDataAdapter)) {
                    return;
                }

                PicListDataAdapter adapter = ((PicListDataAdapter) parent.getAdapter());
                PicListItemModel item = adapter.getItem(position);
                if (item.isHeaderRow() || item.isBlurb()) {
                    return;
                }

                String deviceAddress = item.getDeviceModel().getAddress();
                String deviceName = item.getDeviceName();
                BackstackManager.getInstance().navigateToFragment(NameDeviceFragment.newInstance(NameDeviceFragment.ScreenVariant.SETTINGS, deviceName, deviceAddress), true);
            }
        });
    }

    @Override
    public void onCorneaError(Throwable cause) {
        ErrorManager.in(getActivity()).showGenericBecauseOf(cause);
    }

    @Override
    public void onShowedFragment() {
        getActivity().setTitle(getTitle());

        if (mPresenceListener != null) mPresenceListener.remove();
        mPresenceListener = PresenceStatusController.instance().setCallback(this);

        ImageManager.with(getActivity()).setWallpaper(Wallpaper.ofCurrentPlace().darkened());
    }

    @Override
    public void onClosedFragment() {
        mPresenceListener.remove();
        HomeNFamilyFragmentController.getInstance().removeListener();
    }

    private static final Comparator<PicListItemModel> ORDER = new Comparator<PicListItemModel>() {
        @Override
        public int compare(PicListItemModel firstModel, PicListItemModel secondModel) {
            String firstDisplayName;
            String secondDisplayName;

            if (!TextUtils.isEmpty(firstModel.getPersonId())) {
                firstDisplayName = firstModel.getPersonName();
            } else {
                firstDisplayName = firstModel.getDeviceName();
            }

            if (!TextUtils.isEmpty(secondModel.getPersonId())) {
                secondDisplayName = secondModel.getPersonName();
            } else {
                secondDisplayName = secondModel.getDeviceName();
            }

            return ObjectUtils.compare(firstDisplayName.toUpperCase(), secondDisplayName.toUpperCase());
        }
    };

}

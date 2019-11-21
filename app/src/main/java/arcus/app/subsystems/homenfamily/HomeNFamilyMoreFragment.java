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
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import arcus.cornea.subsystem.presence.PresenceMoreController;
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class HomeNFamilyMoreFragment extends BaseFragment implements PresenceMoreController.Callback, HomeNFamilyFragmentController.Callbacks, IShowedFragment, IClosedFragment {

    private static final Logger logger = LoggerFactory.getLogger(HomeNFamilyMoreFragment.class);

    private ListenerRegistration mListener;
    private ListView moreListView;
    private PicListDataAdapter picListDataAdapter;

    public static HomeNFamilyMoreFragment newInstance() {
        HomeNFamilyMoreFragment fragment = new HomeNFamilyMoreFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        moreListView = (ListView) view.findViewById(R.id.lv_homenfamily_more);

        return view;
    }

    @Nullable
    @Override
    public String getTitle() {
        return getString(R.string.homenfamily_title);
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_home_nfamily_more;
    }

    @Override
    public void showModels(List<PresenceModel> models) {
        HomeNFamilyFragmentController.getInstance().setListener(this);
        HomeNFamilyFragmentController.getInstance().getPicListItemsForPresence(models, HomeNFamilyFragmentController.PresenceTag.ALL);
    }

    @Override
    public void onPicListItemModelsLoaded(List<PicListItemModel> picListItems, HomeNFamilyFragmentController.PresenceTag tag) {
        picListDataAdapter = new PicListDataAdapter(getActivity(), picListItems, true);
        moreListView.setAdapter(picListDataAdapter);
        picListDataAdapter.notifyDataSetChanged();
    }

    @Override
    public void onCorneaError(Throwable cause) {
        ErrorManager.in(getActivity()).showGenericBecauseOf(cause);
    }

    @Override
    public void onShowedFragment() {
        getActivity().setTitle(getTitle());

        moreListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                PicListDataAdapter adapter = (PicListDataAdapter) parent.getAdapter();

                String deviceAddress = adapter.getItem(position).getDeviceModel().getAddress();
                String deviceName = adapter.getItem(position).getDeviceName();

                BackstackManager.getInstance().navigateToFragment(NameDeviceFragment.newInstance(NameDeviceFragment.ScreenVariant.SETTINGS, deviceName, deviceAddress), true);
            }
        });

        ImageManager.with(getActivity()).setWallpaper(Wallpaper.ofCurrentPlace().darkened());

        if (mListener != null) mListener.remove();
        mListener = PresenceMoreController.instance().setCallback(this);
    }

    @Override
    public void onClosedFragment() {
        if (mListener != null) {
            mListener.remove();
        }
    }
}

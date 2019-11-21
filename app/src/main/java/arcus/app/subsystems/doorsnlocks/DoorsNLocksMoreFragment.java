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
package arcus.app.subsystems.doorsnlocks;

import android.os.Bundle;
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;

import arcus.cornea.error.ErrorModel;
import arcus.cornea.subsystem.doorsnlocks.DoorsNLocksMoreController;
import arcus.cornea.subsystem.doorsnlocks.model.ChimeConfig;
import com.iris.client.event.ListenerRegistration;
import arcus.app.R;
import arcus.app.common.fragments.BaseFragment;
import arcus.app.common.utils.ActivityUtils;
import arcus.app.common.view.Version1TextView;
import arcus.app.subsystems.doorsnlocks.adapter.DoorsNLocksChimeListAdapter;

import java.util.List;


public class DoorsNLocksMoreFragment extends BaseFragment implements DoorsNLocksMoreController.Callback{

    private DoorsNLocksMoreController doorsNLocksMoreController;
    private ListenerRegistration mCallbackListener;

    private ListView chimeList;
    private DoorsNLocksChimeListAdapter adapter;
    private Version1TextView shopButton;
    private LinearLayout layoutList, layoutNoItems;

    @NonNull
    public static DoorsNLocksMoreFragment newInstance(){
        DoorsNLocksMoreFragment fragment = new DoorsNLocksMoreFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        chimeList = (ListView) view.findViewById(R.id.chime_list);
        layoutList = (LinearLayout) view.findViewById(R.id.layout_list);
        layoutNoItems = (LinearLayout) view.findViewById(R.id.layout_no_items);

        shopButton = (Version1TextView) view.findViewById(R.id.shop_button);
        shopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityUtils.launchShopNow();
            }
        });

        adapter = new DoorsNLocksChimeListAdapter(getActivity());
        chimeList.setAdapter(adapter);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(doorsNLocksMoreController ==null){
            doorsNLocksMoreController = DoorsNLocksMoreController.instance();
        }
        mCallbackListener = doorsNLocksMoreController.setCallback(this);

        adapter.setController(doorsNLocksMoreController);
    }

    private void toggleListOn(boolean bTurnOn){
        if (bTurnOn){
            layoutList.setVisibility(View.VISIBLE);
            layoutNoItems.setVisibility(View.GONE);
        } else{
            layoutList.setVisibility(View.GONE);
            layoutNoItems.setVisibility(View.VISIBLE);

        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if(mCallbackListener ==null || !mCallbackListener.isRegistered()){
            return;
        }
        mCallbackListener.remove();
    }

    @NonNull
    @Override
    public String getTitle() {
        return "";
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_doors_and_locks_more;
    }

    @Override
    public void showConfigs(List<ChimeConfig> configs) {
        toggleListOn(true);
        logger.debug("Got chime config list: {}", configs);
        adapter.clear();
        adapter.showChimeList(configs);
    }

    @Override
    public void updateConfig(@NonNull ChimeConfig config) {
        toggleListOn(true);
        logger.debug("Update chime config: {}", config);
        adapter.updateChimeList(config);
    }

    @Override
    public void showError(ErrorModel error) {
        logger.debug("Error occurred: {}", error);
    }

    @Override
    public void showNoDevices() {
        toggleListOn(false);
        logger.debug("Show no devices copy");
        adapter.clear();
    }
}

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
package arcus.app.device.zwtools;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.common.collect.Lists;
import com.iris.client.capability.HubZwave;
import arcus.app.R;
import arcus.app.common.adapters.IconizedChevronListAdapter;
import arcus.app.common.models.ListItemModel;
import arcus.app.common.sequence.SequencedFragment;
import arcus.app.common.utils.CorneaUtils;
import arcus.app.device.zwtools.controller.ZWaveToolsSequence;
import arcus.app.device.zwtools.presenter.ZWaveToolsContract;
import arcus.app.device.zwtools.presenter.ZWaveToolsPresenter;


public class ZWaveToolsFragment extends SequencedFragment<ZWaveToolsSequence> implements ZWaveToolsContract.ZWaveToolsView {

    private ZWaveToolsPresenter presenter;

    private ListView toolsList;
    private IconizedChevronListAdapter adapter;
    private ListItemModel removeDevices;
    private ListItemModel repairNetwork;

    public static ZWaveToolsFragment newInstance() {
        return new ZWaveToolsFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        toolsList = (ListView) view.findViewById(R.id.tools_list);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        super.setTitle();

        removeDevices = new ListItemModel(getString(R.string.zwtools_remove_devices));
        repairNetwork = new ListItemModel(getString(R.string.zwtools_repair_network));

        adapter = new IconizedChevronListAdapter(getContext(), CorneaUtils.isZWaveNetworkRebuildSupported() ? Lists.newArrayList(removeDevices, repairNetwork) : Lists.newArrayList(removeDevices));
        toolsList.setAdapter(adapter);

        toolsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (adapter.getItem(position) == removeDevices) {
                    goNext(ZWaveToolsSequence.ZWaveTool.REMOVE_DEVICE);
                } else {
                    goNext(ZWaveToolsSequence.ZWaveTool.REPAIR_NETWORK);
                }
            }
        });

        if (presenter == null) {
            presenter = new ZWaveToolsPresenter();
        }

        presenter.startPresenting(this);
        presenter.requestUpdate();
    }

    @Override
    public void onPause() {
        super.onPause();
        presenter.stopPresenting();
    }

    @Nullable
    @Override
    public String getTitle() {
        return getString(R.string.zwtools_zwave_tools);
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_zwave_tools;
    }

    @Override
    public void onError(Throwable throwable) {
        // Nothing to do
    }

    @Override
    public void onPending(Integer progressPercentage) {
        // Nothing to do
    }

    @Override
    public void updateView(final HubZwave model) {

        if (model.getHealInProgress() != null && model.getHealInProgress()) {
            repairNetwork.setSubText(getString(R.string.zwtools_repair_in_progress, (int) (model.getHealPercent() * 100)));
        } else {
            repairNetwork.setSubText(null);
        }

        adapter.notifyDataSetInvalidated();
        toolsList.invalidateViews();
    }
}

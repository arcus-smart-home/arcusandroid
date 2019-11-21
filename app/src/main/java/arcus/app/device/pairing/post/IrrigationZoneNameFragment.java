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
package arcus.app.device.pairing.post;

import android.os.Bundle;
import androidx.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import arcus.cornea.subsystem.lawnandgarden.LawnAndGardenDeviceMoreController;
import arcus.cornea.subsystem.lawnandgarden.model.LawnAndGardenControllerModel;
import arcus.cornea.subsystem.lawnandgarden.model.LawnAndGardenControllerZoneDetailModel;
import arcus.cornea.utils.Listeners;
import com.iris.client.event.ListenerRegistration;
import arcus.app.R;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.error.ErrorManager;
import arcus.app.common.models.ListItemModel;
import arcus.app.common.sequence.SequencedFragment;
import arcus.app.device.pairing.post.controller.PostPairingSequenceController;
import arcus.app.subsystems.lawnandgarden.adapter.IrrigationZoneListAdapter;
import arcus.app.subsystems.lawnandgarden.fragments.LawnAndGardenEditZoneInfoFragment;

import java.util.ArrayList;
import java.util.List;

public class IrrigationZoneNameFragment
      extends SequencedFragment<PostPairingSequenceController>
      implements LawnAndGardenDeviceMoreController.Callback
{
    private static final String DEVICE_TO_CONFIGURE = "DEVICE_TO_CONFIGURE";
    private ListenerRegistration listener;
    protected IrrigationZoneListAdapter irrigationZoneListAdapter;
    protected ListView listView;

    public static IrrigationZoneNameFragment newInstance(String deviceAddress) {
        IrrigationZoneNameFragment fragment = new IrrigationZoneNameFragment();

        Bundle bundle = new Bundle(1);
        bundle.putString(DEVICE_TO_CONFIGURE, deviceAddress);
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override public void onResume() {
        super.onResume();
        View rootView = getView();
        if (rootView == null) {
            return; // onCrete returned null
        }

        listView = (ListView) rootView.findViewById(R.id.zones_list_view);
        listView.setDivider(null);
        listener = LawnAndGardenDeviceMoreController.instance().setCallback(this);

        View nextButton = rootView.findViewById(R.id.next_button);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                goNext();
            }
        });
    }

    @Override public void onPause() {
        super.onPause();
        Listeners.clear(listener);
    }

    @Override public void showDevices(List<LawnAndGardenControllerModel> controllers) {
        if (listView == null) {
            return;
        }

        String deviceAddress = getArguments().getString(DEVICE_TO_CONFIGURE);
        if (TextUtils.isEmpty(deviceAddress)) {
            return;
        }

        List<ListItemModel> items = new ArrayList<>(controllers.size());
        for (LawnAndGardenControllerModel controller : controllers) {
            if (!controller.getDeviceAddress().equals(deviceAddress)) {
                continue;
            }

            controller.sortZoneDetailsDsc();
            for (LawnAndGardenControllerZoneDetailModel zone : controller.getZoneDetails()) {
                ListItemModel detailItem = new ListItemModel();
                detailItem.setIsHeadingRow(false);
                if (TextUtils.isEmpty(zone.getZoneName())) {
                    detailItem.setText(String.format("Zone %s", zone.getZoneNumber()));
                }
                else {
                    detailItem.setText(zone.getZoneName());
                    detailItem.setSubText(getString(R.string.generic_zone_with_number, zone.getZoneNumber()));
                }
                detailItem.setCount(zone.getDefaultWateringTime());
                detailItem.setAddress(zone.getDeviceAddress());
                detailItem.setData(zone);

                items.add(detailItem);
            }
            break;
        }

        irrigationZoneListAdapter = new IrrigationZoneListAdapter(getActivity(), items, false);
        listView.setAdapter(irrigationZoneListAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (irrigationZoneListAdapter != null) {
                    ListItemModel item = irrigationZoneListAdapter.getItem(position);
                    if (item.getData() == null || !(item.getData() instanceof LawnAndGardenControllerZoneDetailModel)) {
                        return;
                    }

                    LawnAndGardenControllerZoneDetailModel data = (LawnAndGardenControllerZoneDetailModel) item.getData();
                    BackstackManager
                          .getInstance()
                          .navigateToFragment(LawnAndGardenEditZoneInfoFragment.newInstance(data, false, false), true);
                }
            }
        });
    }

    @Override public void onError(Throwable throwable) {
        ErrorManager.in(getActivity()).showGenericBecauseOf(throwable);
    }

    @Nullable @Override public String getTitle() {
        return null;
    }

    @Override public Integer getLayoutId() {
        return R.layout.irrigation_zone_name_fragment;
    }
}

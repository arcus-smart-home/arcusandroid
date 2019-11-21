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
package arcus.app.subsystems.lawnandgarden.fragments;

import android.app.Activity;
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
import arcus.app.common.fragments.BaseFragment;
import arcus.app.common.models.ListItemModel;
import arcus.app.subsystems.lawnandgarden.adapter.IrrigationZoneListAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LawnAndGardenMoreListFragment extends BaseFragment implements LawnAndGardenDeviceMoreController.Callback {

    private static int scrollPosition = 0;

    private ListenerRegistration listener;
    protected IrrigationZoneListAdapter irrigationZoneListAdapter;
    protected ListView listView;

    public static LawnAndGardenMoreListFragment newInstance() {
        return new LawnAndGardenMoreListFragment();
    }

    @Override public void onResume() {
        super.onResume();
        View rootView = getView();
        if (rootView == null) {
            return; // onCreate returned null
        }

        listView = (ListView) rootView.findViewById(android.R.id.list);
        listView.setDivider(null);
        listener = LawnAndGardenDeviceMoreController.instance().setCallback(this);

        listView.post(new Runnable() {
            @Override
            public void run() {
                listView.setSelection(scrollPosition);
            }
        });
    }

    @Override public void onPause() {
        super.onPause();
        Listeners.clear(listener);
        scrollPosition = listView.getFirstVisiblePosition();
    }

    @Override public void showDevices(List<LawnAndGardenControllerModel> controllers) {
        if (listView == null) {
            return;
        }

        Activity activity = getActivity();
        if (activity != null) {
            activity.setTitle(getTitle());
            activity.invalidateOptionsMenu();
        }

        Collections.sort(controllers); // Sort by Controller name first.
        List<ListItemModel> items = new ArrayList<>(controllers.size());

        for (LawnAndGardenControllerModel controller : controllers) {
            ListItemModel headerItem = new ListItemModel();
            headerItem.setIsHeadingRow(true);
            headerItem.setText(controller.getControllerName());

            int zones = controller.getZoneCount();
            headerItem.setSubText(getResources().getQuantityString(R.plurals.lng_zone_plural, zones, zones));
            items.add(headerItem);

            controller.sortZoneDetailsDsc(); // Then sort by Zone name.
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
        }

        irrigationZoneListAdapter = new IrrigationZoneListAdapter(getActivity(), items);
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
                          .navigateToFragment(LawnAndGardenEditZoneInfoFragment.newInstance(data), true);
                }
            }
        });
    }

    @Override public void onError(Throwable throwable) {
        ErrorManager.in(getActivity()).showGenericBecauseOf(throwable);
    }

    @Nullable @Override public String getTitle() {
        return getString(R.string.lng_zones_more_title).toUpperCase();
    }

    @Override public Integer getLayoutId() {
        return android.R.layout.list_content;
    }
}

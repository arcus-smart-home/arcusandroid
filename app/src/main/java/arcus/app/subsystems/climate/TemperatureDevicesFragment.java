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
package arcus.app.subsystems.climate;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dexafree.materialList.controller.RecyclerItemClickListener;
import com.dexafree.materialList.model.Card;
import com.dexafree.materialList.model.CardItemView;
import com.dexafree.materialList.view.MaterialListView;
import arcus.cornea.subsystem.climate.TemperatureDeviceController;
import arcus.cornea.subsystem.climate.model.DeviceTemperatureModel;
import com.iris.client.event.ListenerRegistration;
import arcus.app.R;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.cards.LeftTextCard;
import arcus.app.common.controller.AbstractCardController;
import arcus.app.common.fragments.BaseFragment;
import arcus.app.common.models.SessionModelManager;
import arcus.app.device.details.DeviceDetailParentFragment;

import java.util.List;

public class TemperatureDevicesFragment extends BaseFragment implements TemperatureDeviceController.Callback, AbstractCardController.Callback {
    private TemperatureDeviceController temperatureDeviceController;
    private List<DeviceTemperatureModel> deviceList;
    private MaterialListView materialListView;
    private ListenerRegistration controllerListener;
    private View noDevicesView;

    @NonNull
    public static final TemperatureDevicesFragment newInstance() {
        return new TemperatureDevicesFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        materialListView = (MaterialListView) view.findViewById(R.id.material_listview);
        noDevicesView = view.findViewById(R.id.climate_devices_no_device_container);
        materialListView.addOnItemTouchListener(new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(CardItemView cardItemView, int i) {
                DeviceTemperatureModel model = deviceList.get(i);
                if (model != null) {
                    int position;
                    position = SessionModelManager.instance().indexOf(model.getDeviceId(), true);
                    if (position == -1) return;
                    BackstackManager.getInstance()
                          .navigateToFragment(DeviceDetailParentFragment.newInstance(position), true);
                }
            }

            @Override
            public void onItemLongClick(CardItemView cardItemView, int i) {
                // No-Op
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (temperatureDeviceController == null) {
            temperatureDeviceController = TemperatureDeviceController.instance();
        }

        controllerListener = temperatureDeviceController.setCallback(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        controllerListener.remove();
    }

    @Nullable
    @Override
    public String getTitle() { return null; }

    @Override
    public Integer getLayoutId() { return R.layout.fragment_climate; }

    @Override
    public void updateCard(Card c) {
        materialListView.clear();
        boolean first = true;

        for (DeviceTemperatureModel model : deviceList) {
            LeftTextCard card = new LeftTextCard(getActivity());

            card.setTitle(model.getName());
            card.showChevron();
            if (model.hasHumidity()) {
                card.setDescription(model.getHumidity() + getString(R.string.percent_humidity));
            }
            card.setRightText(model.getTemperature() + getString(R.string.degree_symbol));
            card.setDeviceID(model.getDeviceId());
            card.showDivider();

            materialListView.add(card);
        }
    }

    @Override
    public void updateTemperatureDevice(@NonNull DeviceTemperatureModel device) {
        // Easier way than redrawing the whole list?...
        for (int i = 0; i < deviceList.size(); i++) {
            if (deviceList.get(i).getDeviceId().equals(device.getDeviceId())) {
                deviceList.set(i, device);
                updateCard(null);
                break;
            }
        }
    }

    @Override
    public void showTemperatureDevices(List<DeviceTemperatureModel> devices) {
        deviceList = devices;
        if(deviceList.size()>0){
            materialListView.setVisibility(View.VISIBLE);
            noDevicesView.setVisibility(View.GONE);
            updateCard(null);
        } else {
            materialListView.setVisibility(View.GONE);
            noDevicesView.setVisibility(View.VISIBLE);
        }
    }
}

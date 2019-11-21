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
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dexafree.materialList.model.Card;
import com.dexafree.materialList.view.MaterialListView;
import arcus.cornea.subsystem.doorsnlocks.DoorsNLocksControlController;
import arcus.cornea.subsystem.doorsnlocks.model.DoorsNLocksDevice;
import com.iris.client.event.ListenerRegistration;
import arcus.app.R;
import arcus.app.common.controller.AbstractCardController;
import arcus.app.common.fragments.BaseFragment;
import arcus.app.subsystems.doorsnlocks.controllers.ContactSensorCardController;
import arcus.app.subsystems.doorsnlocks.controllers.DoorLockCardController;
import arcus.app.subsystems.doorsnlocks.controllers.MotorizedDoorCardController;
import arcus.app.subsystems.doorsnlocks.controllers.PetDoorCardController;

import java.util.ArrayList;
import java.util.List;


public class DoorsNLocksDeviceFragment extends BaseFragment implements DoorsNLocksControlController.Callback, AbstractCardController.Callback{

    private DoorsNLocksControlController doorsNLocksControlController;
    private ListenerRegistration mCallbackListener;
    private MaterialListView mListView;

    private List<AbstractCardController> mCardControllers;
    private List<DoorsNLocksDevice> mDevices;

    @NonNull
    public static DoorsNLocksDeviceFragment newInstance(){
        DoorsNLocksDeviceFragment fragment = new DoorsNLocksDeviceFragment();

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        mListView = (MaterialListView) view.findViewById(R.id.material_listview);

        mCardControllers = new ArrayList<>();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        if(doorsNLocksControlController ==null){
            doorsNLocksControlController = DoorsNLocksControlController.instance();
        }

        mCallbackListener = doorsNLocksControlController.setCallback(this);
    }

    @Override
    public void onPause() {
        super.onPause();

        if(mCallbackListener ==null || !mCallbackListener.isRegistered()){
            return;
        }
        mCallbackListener.remove();
        for (AbstractCardController controller : mCardControllers) {
            controller.removeCallback();
        }
    }

    private void populateCard(@Nullable List<DoorsNLocksDevice> devices){
        mListView.clear();

        if(devices ==null || devices.size() == 0) return;

        mCardControllers = new ArrayList<>();

        for (DoorsNLocksDevice model : devices) {
            AbstractCardController controller = null;

            // Need a device id to continue
            if (model.getId() == null) continue;

            switch (model.getType()) {
                case LOCK:
                    controller = new DoorLockCardController(model.getId(), getActivity());
                    break;
                case GARAGE_DOOR:
                    controller = new MotorizedDoorCardController(model.getId(), getActivity());
                    break;
                case DOOR_SENSOR:
                    controller = new ContactSensorCardController(model.getId(), getActivity());
                    break;
                case PET_DOOR:
                   controller = new PetDoorCardController(model.getId(), getActivity());

                    break;
            }

            if (controller != null) {
                controller.setCallback(this);
                mCardControllers.add(controller);
                mListView.add(controller.getCard());
            }
        }
    }

    @NonNull
    @Override
    public String getTitle() {
        return "";
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_doors_and_locks_device;
    }

    @Override
    public void showDevices(List<DoorsNLocksDevice> devices) {
        logger.debug("Got doors and locks device list: {}", devices);
        mDevices = devices;
        populateCard(devices);
    }

    @Override
    public void updateCard(Card c) {
        populateCard(mDevices);
    }
}

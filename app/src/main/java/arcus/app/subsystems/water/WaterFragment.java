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
package arcus.app.subsystems.water;

import android.os.Bundle;
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dexafree.materialList.model.Card;
import com.dexafree.materialList.view.MaterialListView;
import arcus.cornea.subsystem.water.ControlDeviceController;
import arcus.cornea.subsystem.water.model.DeviceControlModel;
import com.iris.client.event.ListenerRegistration;
import arcus.app.R;
import arcus.app.common.cards.DeviceControlCard;
import arcus.app.common.controller.AbstractCardController;
import arcus.app.common.fragments.BaseFragment;
import arcus.app.subsystems.water.controllers.ValveCardController;
import arcus.app.subsystems.water.controllers.WaterHeaterCardController;
import arcus.app.subsystems.water.controllers.WaterSoftenerCardController;

import java.util.ArrayList;
import java.util.List;



public class WaterFragment extends BaseFragment implements ControlDeviceController.Callback,AbstractCardController.Callback{


    public enum Type {HEATER, VALVE, SOFTENER}


    private MaterialListView mListView;

    private ControlDeviceController mController;
    ListenerRegistration mListener;

    private List<AbstractCardController> mCardControllers;

    private ArrayList<DeviceControlCard> mDeviceCardList;

    @NonNull
    public static WaterFragment newInstance() {
        WaterFragment fragment = new WaterFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (mController == null) {
            mController = ControlDeviceController.instance();
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        setHasOptionsMenu(true);

        mListView = (MaterialListView) view.findViewById(R.id.material_listview);

        mCardControllers = new ArrayList<>();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        mListener = mController.setCallback(this);

        getActivity().setTitle(getTitle());
    }

    @Override
    public void onPause() {
        super.onPause();
        mListener.remove();
        for (AbstractCardController controller : mCardControllers) {
            controller.removeCallback();
        }
    }

    private void populateCards(@NonNull List<DeviceControlModel> controls) {
        mListView.clear();
        mCardControllers = new ArrayList<>();

        if (mDeviceCardList == null || mDeviceCardList.size() <= 0)
            mDeviceCardList = new ArrayList<>();

        for (DeviceControlModel model : controls) {
            AbstractCardController controller = null;

            // Need a device id to continue
            if (model.getDeviceId() == null) continue;

            switch (model.getType()) {
                case WATER_SOFTENER:
                    controller = new WaterSoftenerCardController(model.getDeviceId(), getActivity());
                    break;
                case WATER_VALVE:
                    controller = new ValveCardController(model.getDeviceId(), getActivity());
                    break;
                case WATERHEATER:
                    controller = new WaterHeaterCardController(model.getDeviceId(), getActivity());
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
        return getActivity().getString(R.string.water);
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_climate;
    }

    /*
     * Climate Callback
     */

    @Override
    public void showDeviceControls(@NonNull List<DeviceControlModel> controls) {
        logger.debug("Got climate device control models: {}", controls);
        populateCards(controls);
    }

    @Override
    public void updateCard(Card c) {

    }
}

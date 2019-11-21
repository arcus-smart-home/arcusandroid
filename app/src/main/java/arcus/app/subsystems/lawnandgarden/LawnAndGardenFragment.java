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
package arcus.app.subsystems.lawnandgarden;

import android.os.Bundle;
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dexafree.materialList.model.Card;
import com.dexafree.materialList.view.MaterialListView;
import arcus.cornea.subsystem.lawnandgarden.LawnAndGardenDeviceController;
import arcus.cornea.subsystem.lawnandgarden.model.LawnAndGardenDeviceControlModel;
import com.iris.client.event.ListenerRegistration;
import arcus.app.R;
import arcus.app.common.cards.DeviceControlCard;
import arcus.app.common.controller.AbstractCardController;
import arcus.app.common.fragments.BaseFragment;
import arcus.app.subsystems.lawnandgarden.controllers.IrrigationDeviceCardController;

import java.util.ArrayList;
import java.util.List;



public class LawnAndGardenFragment extends BaseFragment implements LawnAndGardenDeviceController.Callback,AbstractCardController.Callback{

    private MaterialListView mListView;

    ListenerRegistration mListener;

    private List<AbstractCardController> mCardControllers;

    private ArrayList<DeviceControlCard> mDeviceCardList;

    @NonNull
    public static LawnAndGardenFragment newInstance() {
        LawnAndGardenFragment fragment = new LawnAndGardenFragment();
        return fragment;
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

        mListener = LawnAndGardenDeviceController.instance().setCallback(this);

        getActivity().setTitle(getTitle());
    }

    @Override
    public void onPause() {
        super.onPause();
        mListener.remove();
        removeExistingCallbacks();
    }

    private void populateCards(@NonNull List<LawnAndGardenDeviceControlModel> controls) {
        mListView.clear();
        mCardControllers = new ArrayList<>();

        if (mDeviceCardList == null || mDeviceCardList.size() <= 0)
            mDeviceCardList = new ArrayList<>();

        for (LawnAndGardenDeviceControlModel model : controls) {
            AbstractCardController controller;

            // Need a device id to continue
            if (model.getDeviceId() == null) continue;

            controller = new IrrigationDeviceCardController(model.getDeviceId(), getActivity());

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
        return getActivity().getString(R.string.lawn_and_garden);
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_lawn_and_garden;
    }

    /*
     * Lawn And Garden Callback
     */

    @Override
    public void showDeviceControls(@NonNull List<LawnAndGardenDeviceControlModel> controls) {
        logger.debug("Got irrigation device control models: {}", controls);
        removeExistingCallbacks();
        populateCards(controls);
    }

    // Remove existing callbacks; This way, if the entire device list is recalculated (ie, a new device is added while viewing the list)
    // we will remove any callbacks, and cancel any existing countdown timers.
    protected void removeExistingCallbacks() {
        if (mCardControllers == null) {
            return;
        }

        try {
            for (AbstractCardController controller : mCardControllers) {
                controller.removeCallback();
            }
        } catch (Exception ignored) {}
    }

    @Override
    public void updateCard(Card c) {

    }
}

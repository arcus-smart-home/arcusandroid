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
package arcus.app.subsystems.camera;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.View;

import com.dexafree.materialList.model.Card;
import com.dexafree.materialList.view.MaterialListView;
import arcus.cornea.subsystem.cameras.CameraDeviceListController;
import arcus.cornea.subsystem.cameras.model.CameraModel;
import arcus.cornea.utils.Listeners;
import com.iris.client.event.ListenerRegistration;
import arcus.app.R;
import arcus.app.common.controller.AbstractCardController;
import arcus.app.common.fragments.BaseFragment;
import arcus.app.subsystems.camera.controllers.DeviceCardController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class CameraFragment extends BaseFragment implements CameraDeviceListController.Callback, AbstractCardController.Callback {

    private CameraDeviceListController mController;
    private ListenerRegistration mCallbackListener;

    private MaterialListView mListView;

    private Map<String, AbstractCardController> mCardControllers = new HashMap<>(10);

    @NonNull
    public static CameraFragment newInstance() {
        CameraFragment fragment = new CameraFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mListView = view.findViewById(R.id.material_listview);
    }

    @NonNull
    @Override
    public String getTitle() {
        return "";
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_camera_devices;
    }

    @Override
    public void onResume() {
        super.onResume();

        if(mController == null) {
            mController = CameraDeviceListController.instance();
        }

        mCallbackListener = mController.setCallback(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        removeCardCallbacks();
        mCallbackListener = Listeners.clear(mCallbackListener);
    }

    private void populateCards(@NonNull List<CameraModel> models) {
        mListView.clear();

        for (CameraModel model : models) {
            DeviceCardController controller = new DeviceCardController(getActivity(), model);
            controller.setCallback(this);
            mListView.add(controller.getCard());

            AbstractCardController old = mCardControllers.put(model.getCameraID(), controller);
            if (old != null) {
                old.removeCallback();
            }
        }
    }

    private void removeCardCallbacks() {
        for (Map.Entry<String, AbstractCardController> entry : mCardControllers.entrySet()) {
            entry.getValue().removeCallback();
        }
    }

    @Override
    public void showDevices(@NonNull List<CameraModel> cameraModelIDList) {
        populateCards(cameraModelIDList);
    }

    /***
     * AbstractCardController Callbacks
     */

    @Override
    public void updateCard(Card c) {

    }
}

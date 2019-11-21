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
package arcus.app.subsystems.care.fragment;

import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import arcus.cornea.CorneaClientFactory;
import arcus.cornea.subsystem.care.CareBehaviorController;
import arcus.cornea.subsystem.care.model.CareBehaviorModel;
import arcus.cornea.subsystem.care.model.CareBehaviorTemplateModel;
import arcus.cornea.utils.Listeners;
import com.iris.client.event.ListenerRegistration;
import com.iris.client.model.DeviceModel;
import com.iris.client.model.Model;
import arcus.app.R;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.error.ErrorManager;
import arcus.app.common.fragments.BaseFragment;
import arcus.app.common.image.ImageManager;
import arcus.app.common.image.Wallpaper;
import arcus.app.common.models.ListItemModel;
import arcus.app.common.popups.NumberPickerPopup;
import arcus.app.subsystems.care.adapter.CareOpenClosedDeviceAdapter;
import arcus.app.subsystems.care.util.CareUtilities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CareSelectOpenDevicesFragment extends BaseFragment
      implements CareBehaviorController.Callback,
      CareOpenClosedDeviceAdapter.Callback
{
    private static final String ID      = "ID";
    private static final String IS_EDIT = "IS_EDIT";
    private static final Integer MIN_OPEN = 1;
    private static final Integer MAX_OPEN = 50;
    private static final Integer DEFAULT_START = 5;

    private boolean isEditMode;
    private CareOpenClosedDeviceAdapter careOpenClosedDeviceAdapter;
    private ListView behaviorListView;
    private ListenerRegistration listener;
    private CareBehaviorModel careModel;

    public static CareSelectOpenDevicesFragment newInstance(String templateId, boolean isEditMode) {
        CareSelectOpenDevicesFragment fragment = new CareSelectOpenDevicesFragment();
        Bundle args = new Bundle();

        args.putString(ID, templateId);
        args.putBoolean(IS_EDIT, isEditMode);

        fragment.setArguments(args);
        return fragment;
    }

    @Nullable @Override public View onCreateView(
          LayoutInflater inflater,
          @Nullable ViewGroup container,
          @Nullable Bundle savedInstanceState)
    {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        if (view == null) {
            return view;
        }

        Bundle args = getArguments();
        if (args == null) {
            args = new Bundle(1);
        }
        isEditMode = args.getBoolean(IS_EDIT, false);

        TextView header = (TextView) view.findViewById(R.id.behavior_device_list_header);
        if (header != null) {
            header.setTextColor(isEditMode ? Color.WHITE : Color.BLACK);
            header.setVisibility(View.VISIBLE);
        }
        View divider = view.findViewById(R.id.divider_item);
        if (divider != null) {
            divider.setBackgroundColor(
                  isEditMode ?
                        getResources().getColor(R.color.overlay_white_with_20) :
                        getResources().getColor(R.color.black_with_20)
            );
            divider.setVisibility(View.VISIBLE);
        }

        behaviorListView = (ListView) view.findViewById(R.id.care_behavior_lv);
        setWallpaper();
        return view;
    }

    protected void setWallpaper() {
        Wallpaper wallpaper = Wallpaper.ofCurrentPlace();
        wallpaper = isEditMode ? wallpaper.darkened() : wallpaper.lightend();
        ImageManager.with(getActivity()).setWallpaper(wallpaper);
    }

    @Override public void onResume() {
        super.onResume();
        Bundle args = getArguments();
        if (args == null) {
            return; // Not sure what to do - don't have required args...
        }

        showProgressBar();
        listener = CareBehaviorController.instance().setCallback(this);
        if (isEditMode) {
            CareBehaviorController.instance().editExistingBehaviorByID(args.getString(ID, ""));
        }
        else {
            CareBehaviorController.instance().addBehaviorByTemplateID(args.getString(ID, ""));
        }
    }

    @Override public void onPause() {
        super.onPause();
        Listeners.clear(listener);
    }
    @Override public void unsupportedTemplate() {
        hideProgressBar();
    }

    @Override public void editTemplate(final CareBehaviorModel editingModel, final CareBehaviorTemplateModel templateModel) {
        hideProgressBar();
        careModel = editingModel;

        List<ListItemModel> models = new ArrayList<>(templateModel.getAvailableDevices().size());
        for (String device : templateModel.getAvailableDevices()) {
            Model model = CorneaClientFactory.getModelCache().get(device);
            if (model == null || !(model instanceof DeviceModel)) {
                continue;
            }

            DeviceModel deviceModel = (DeviceModel) model;
            ListItemModel dataModel = new ListItemModel();
            dataModel.setAddress(device);
            dataModel.setText(deviceModel.getName());
            dataModel.setSubText(deviceModel.getVendor());
            dataModel.setData(deviceModel);
            boolean shouldCheck = editingModel.getOpenCounts().containsKey(device);
            if (shouldCheck) {
                dataModel.setChecked(true);
                dataModel.setCount(editingModel.getOpenCounts().get(device));
            }
            models.add(dataModel);
        }

        behaviorListView.setDivider(null);
        Collections.sort(models, CareUtilities.listItemModelComparatorByName(CareUtilities.Sort.DSC));
        careOpenClosedDeviceAdapter = new CareOpenClosedDeviceAdapter(getActivity(), models, isEditMode);
        behaviorListView.post(new Runnable() {
            @Override public void run() {
                careOpenClosedDeviceAdapter.setCallback(CareSelectOpenDevicesFragment.this);
            }
        });
        behaviorListView.setAdapter(careOpenClosedDeviceAdapter);
    }

    @Override public void checkBoxAreaClicked(ListItemModel listItemModel) {
        if (listItemModel.getCount() < 1) {
            // If nothing was picked, show the user they have to pick something to check this...
            numericPickerAreaClicked(listItemModel);
            return;
        }

        listItemModel.setChecked(!listItemModel.isChecked());
        careOpenClosedDeviceAdapter.notifyDataSetChanged();
        updateModel(careModel, listItemModel);
    }

    @Override public void numericPickerAreaClicked(final ListItemModel listItemModel) {
        int startAt = listItemModel.getCount();
        if (startAt == 0) {
            startAt = DEFAULT_START;
        }
        NumberPickerPopup popup = NumberPickerPopup.newInstance(NumberPickerPopup.NumberPickerType.TIMES, MIN_OPEN, MAX_OPEN, startAt);
        popup.setOnValueChangedListener(new NumberPickerPopup.OnValueChangedListener() {
            @Override
            public void onValueChanged(int value) {
                if (listItemModel.getCount() == 0) {
                    listItemModel.setChecked(true);
                }
                listItemModel.setCount(value);
                careOpenClosedDeviceAdapter.notifyDataSetChanged();
                updateModel(careModel, listItemModel);
            }
        });
        BackstackManager.getInstance().navigateToFloatingFragment(popup, popup.getClass()
              .getCanonicalName(), true);

    }

    protected void updateModel(CareBehaviorModel editingModel, ListItemModel listItemModel) {
        if (listItemModel.isChecked()) {
            editingModel.getOpenCounts().put(listItemModel.getAddress(), listItemModel.getCount());
        }
        else {
            editingModel.getOpenCounts().remove(listItemModel.getAddress());
        }
    }

    @Override public void onError(Throwable error) {
        hideProgressBar();
        ErrorManager.in(getActivity()).showGenericBecauseOf(error);
    }

    @Nullable @Override public String getTitle() {
        return null;
    }

    @Override public Integer getLayoutId() {
        return R.layout.care_list_behavior;
    }

}

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
package arcus.app.subsystems.care.view;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import arcus.cornea.CorneaClientFactory;
import arcus.app.R;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.models.ListItemModel;
import arcus.app.common.popups.MultiModelPopup;
import arcus.app.common.popups.adapter.MultiModelAdapter;
import arcus.app.subsystems.care.util.CareUtilities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CareMultiModelPopup extends MultiModelPopup {
    public static final String MODELS_SELECTED = "MODEL.SELECTED";
    public static final String MODEL_LIST  = "MODEL.LIST";
    public static final String POPUP_TITLE = "TITLE.RES";
    public static final String MULTIPLE_MODELS_SELECTABLE = "MULTIPLE.MODELS.SELECTABLE";

    private Set<String> selected;
    private Callback callback;

    public interface Callback {
        void itemSelectedAddress(Set<String> addressesSelected);
    }

    @NonNull
    public static CareMultiModelPopup newInstance(List<String> modelIDs,
                                              @StringRes @Nullable Integer titleRes,
                                              @Nullable List<String> preSelected,
                                              @Nullable Boolean allowMultipleSelections) {
        CareMultiModelPopup fragment = new CareMultiModelPopup();
        ArrayList<String> modelsSelectedList = new ArrayList<>();
        if (preSelected != null) {
            modelsSelectedList.addAll(preSelected);
        }

        ArrayList<String> modelAddressList = new ArrayList<>();
        if (modelIDs != null) {
            modelAddressList.addAll(modelIDs);
        }

        Bundle bundle = new Bundle(2);
        bundle.putInt(POPUP_TITLE, titleRes == null ? R.string.choose_devices_text : titleRes);
        bundle.putStringArrayList(MODELS_SELECTED, modelsSelectedList);
        bundle.putStringArrayList(MODEL_LIST, modelAddressList);
        bundle.putBoolean(MULTIPLE_MODELS_SELECTABLE, Boolean.TRUE.equals(allowMultipleSelections));
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public void setFloatingTitle() {
        Integer titleRes = R.string.choose_devices_text;
        if (getArguments() != null) {
            titleRes = getArguments().getInt(POPUP_TITLE, R.string.choose_devices_text);
        }

        title.setText(getResources().getString(titleRes));
    }

    @Override public void doContentSection() {
        if (callback == null || getArguments() == null || !CorneaClientFactory.isConnected()) {
            logger.debug("Popping. Reason -> Callback Null [{}], Arguments Null [{}], Client IS NOT Connected [{}]",
                  callback == null, getArguments() == null, !CorneaClientFactory.isConnected());
            BackstackManager.getInstance().navigateBack();
            return;
        }

        List<String> modelList = getArguments().getStringArrayList(MODEL_LIST);
        if (modelList == null || modelList.isEmpty()) {
            return;
        }

        Boolean multiple = Boolean.TRUE.equals(getArguments().getBoolean(MULTIPLE_MODELS_SELECTABLE));
        List<ListItemModel> models = new ArrayList<>(modelList.size() + 1);
        List<String> selectedModels = getArguments().getStringArrayList(MODELS_SELECTED);

        ListItemModel allDevicesItem = new ListItemModel();
        allDevicesItem.setText("All Devices");
        allDevicesItem.setSubText("Select all devices.");

        if (selectedModels != null) {
            allDevicesItem.setChecked(selectedModels.size() == modelList.size());
            selected = new HashSet<>(selectedModels);
        }
        else {
            selected = new HashSet<>(modelList.size() + 1);
        }

        allDevicesItem.setImageResId(R.drawable.side_menu_devices);
        models.add(allDevicesItem);

        List<ListItemModel> sortedList = getModelListForAdapter(modelList);
        Collections.sort(sortedList, CareUtilities.listItemModelComparatorByName(CareUtilities.Sort.DSC));
        models.addAll(sortedList);

        final MultiModelAdapter multiModelAdapter = new MultiModelAdapter(getActivity(), models, multiple, R.layout.floating_device_picker_item_with_divider);
        ListView listView = (ListView) contentView.findViewById(R.id.floating_list_view);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    boolean nowChecked = multiModelAdapter.selectItem(0);
                    for (int i = 1, size = multiModelAdapter.getCount(); i < size; i++) {
                        ListItemModel itemModel = multiModelAdapter.getItem(i);
                        if (itemModel.isChecked() != nowChecked) {
                            multiModelAdapter.selectItem(i);
                        }
                        if (nowChecked) {
                            selected.add(multiModelAdapter.getItem(i).getAddress());
                        }
                        else {
                            selected.remove(multiModelAdapter.getItem(i).getAddress());
                        }
                    }
                }
                else {
                    if (multiModelAdapter.selectItem(position)) {
                        selected.add(multiModelAdapter.getItem(position).getAddress());
                    }
                    else {
                        selected.remove(multiModelAdapter.getItem(position).getAddress());
                    }

                    ListItemModel allDevices = multiModelAdapter.getItem(0);
                    if (selected.size() == multiModelAdapter.getCount()) {
                        multiModelAdapter.selectItem(0);
                    }
                    else if (allDevices.isChecked()) {
                        multiModelAdapter.selectItem(0);
                    }
                }
            }
        });
        listView.setAdapter(multiModelAdapter);
    }

    @Override public void doClose() {
        if (callback != null && selected != null) {
            callback.itemSelectedAddress(selected);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        showFullScreen(true);
    }


    @Override
    public void onPause() {
        super.onPause();
        showFullScreen(false);
    }

    @Override public Integer contentSectionLayout() {
        return R.layout.care_floating_list_picker;
    }

    @Override @NonNull public String getTitle() {
        return "";
    }

    @Override public Integer getLayoutId() {
        return R.layout.fullscreen_arcus_popup_fragment_nopadding;
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }
}

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
package arcus.app.common.popups;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import arcus.cornea.CorneaClientFactory;
import com.iris.client.model.DeviceModel;
import com.iris.client.model.Model;
import com.iris.client.model.PersonModel;
import com.iris.client.model.PlaceModel;
import com.iris.client.model.SceneModel;
import arcus.app.R;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.models.ListItemModel;
import arcus.app.common.popups.adapter.MultiModelAdapter;
import arcus.app.common.utils.CorneaUtils;
import arcus.app.subsystems.scenes.catalog.model.SceneCategory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public class MultiModelPopup extends ArcusFloatingFragment {
    public static final String MODELS_SELECTED = "MODEL.SELECTED";
    public static final String MODEL_LIST  = "MODEL.LIST";
    public static final String POPUP_TITLE = "TITLE.RES";
    public static final String MULTIPLE_MODELS_SELECTABLE = "MULTIPLE.MODELS.SELECTABLE";

    private Callback callback;

    public interface Callback {
        void itemSelectedAddress(ListItemModel itemModel);
    }

    @NonNull
    public static MultiModelPopup newInstance(List<String> modelIDs,
                                              @StringRes @Nullable Integer titleRes,
                                              @Nullable List<String> preSelected,
                                              @Nullable Boolean allowMultipleSelections) {
        MultiModelPopup fragment = new MultiModelPopup();
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

    @Override
    public void doContentSection() {
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
        List<ListItemModel> models = getModelListForAdapter(modelList);
        final MultiModelAdapter multiModelAdapter = new MultiModelAdapter(getActivity(), models, multiple);
        ListView listView = (ListView) contentView.findViewById(R.id.floating_list_view);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                multiModelAdapter.selectItem(position);
                if (callback != null) {
                    callback.itemSelectedAddress(multiModelAdapter.getItem(position));
                }
                logger.debug("\"Checking\" item at position: [{}] {}", position, multiModelAdapter.getItem(position));
            }
        });
        listView.setAdapter(multiModelAdapter);
    }

    protected List<ListItemModel> getModelListForAdapter(List<String> modelList) {
        List<String> modelsSelectedList = getArguments().getStringArrayList(MODELS_SELECTED);
        if (modelsSelectedList == null) {
            modelsSelectedList = new LinkedList<>();
        }

        List<ListItemModel> modelItemList = new LinkedList<>();
        for (String modelAddress : modelList) {
            Model model = CorneaClientFactory.getModelCache().get(modelAddress);
            if (model == null) {
                continue;
            }

            ListItemModel itemModel = new ListItemModel();
            itemModel.setAddress(model.getAddress());
            if (!itemModel.isSupportedModel()) {
                continue;
            }

            itemModel.setChecked(modelsSelectedList.contains(modelAddress));
            if (itemModel.isDeviceModel()) {
                DeviceModel deviceModel = (DeviceModel) model;
                itemModel.setText(deviceModel.getName());
                itemModel.setSubText(deviceModel.getVendor());
                itemModel.setData(deviceModel);
            }
            else if (itemModel.isPersonModel()) {
                PersonModel personModel = (PersonModel) model;
                itemModel.setText(CorneaUtils.getPersonDisplayName(personModel));
                // ITWO-5928: Hide relationship subtext
                // itemModel.setSubText(CorneaUtils.getPersonRelationship(personModel));
                itemModel.setData(personModel);
            }
            else if (itemModel.isPlaceModel()) {
                PlaceModel placeModel = (PlaceModel) model;
                itemModel.setText(placeModel.getName());
                itemModel.setData(placeModel);
            }
            else if (itemModel.isSceneModel()) {
                SceneModel sceneModel = (SceneModel) model;
                SceneCategory category = SceneCategory.fromSceneModel(sceneModel);
                itemModel.setText(category.getName());
                itemModel.setSubText(null);
                itemModel.setData(sceneModel);
            }

            modelItemList.add(itemModel);
        }

        //sort the items right here
        Collections.sort(modelItemList, new Comparator<ListItemModel>() {
            @Override
            public int compare(ListItemModel lm1, ListItemModel lm2) {
                if(lm1 == null){
                    lm1 = new ListItemModel();
                }
                if(lm2 == null){
                    lm2 = new ListItemModel();
                }
                return lm1.getText().compareTo(lm2.getText());
            }
        });
        return modelItemList;
    }

    @Override
    public Integer contentSectionLayout() {
        return R.layout.floating_list_picker;
    }

    @Override @NonNull
    public String getTitle() {
        return "";
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.floating_list_picker_fragment;
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }


}

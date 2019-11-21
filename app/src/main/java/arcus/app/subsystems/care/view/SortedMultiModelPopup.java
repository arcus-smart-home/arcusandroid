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
import java.util.List;

public class SortedMultiModelPopup extends MultiModelPopup {
    private Callback callback;

    public interface Callback {
        void itemSelectedAddress(ListItemModel itemModel);
    }

    @NonNull public static SortedMultiModelPopup newInstance(
          List<String> modelIDs,
          @StringRes @Nullable Integer titleRes,
          @Nullable List<String> preSelected,
          @Nullable Boolean allowMultipleSelections
    ) {
        SortedMultiModelPopup fragment = new SortedMultiModelPopup();
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
        Collections.sort(models, CareUtilities.listItemModelComparatorByName(CareUtilities.Sort.DSC));

        final MultiModelAdapter multiModelAdapter = new MultiModelAdapter(getActivity(), models, multiple);
        ListView listView = (ListView) contentView.findViewById(R.id.floating_list_view);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                multiModelAdapter.selectItem(position);
                if (callback != null) {
                    callback.itemSelectedAddress(multiModelAdapter.getItem(position));
                }
            }
        });
        listView.setAdapter(multiModelAdapter);
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }
}

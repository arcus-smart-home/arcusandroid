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
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import arcus.cornea.model.PlaceAndRoleModel;
import arcus.app.R;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.models.ListItemModel;
import arcus.app.common.popups.adapter.MultiModelAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public class SelectPlacePopup extends ArcusFloatingFragment {
    public static final String MODELS_SELECTED  = "MODEL.SELECTED";
    public static final String MODEL_LIST       = "MODEL.LIST";
    public static final String POPUP_TITLE      = "TITLE.RES";

    private Callback callback;
    String selectedAddress;

    public interface Callback {
        void itemSelectedAddress(String placeAddress);
    }

    @SuppressWarnings({"ConstantConditions"}) @NonNull public static SelectPlacePopup newInstance(
          @NonNull List<PlaceAndRoleModel> places,
          @Nullable String preSelected,
          @StringRes @Nullable Integer titleRes
          ) {
        SelectPlacePopup fragment = new SelectPlacePopup();

        ArrayList<PlaceAndRoleModel> placeAndRoleModels = (places == null) ? new ArrayList<PlaceAndRoleModel>() : new ArrayList<>(places);

        Bundle bundle = new Bundle(3);
        bundle.putInt(POPUP_TITLE, titleRes == null ? R.string.choose_a_place : titleRes);
        bundle.putString(MODELS_SELECTED, preSelected);
        bundle.putParcelableArrayList(MODEL_LIST, placeAndRoleModels);
        fragment.setArguments(bundle);

        return fragment;
    }

    @NonNull public static SelectPlacePopup newInstance(
          @NonNull List<PlaceAndRoleModel> places,
          @Nullable String preSelected
    ) {
        return newInstance(places, preSelected, null);
    }

    @Override public void setFloatingTitle() {
        Integer titleRes = R.string.choose_a_place;
        if (getArguments() != null) {
            titleRes = getArguments().getInt(POPUP_TITLE, R.string.choose_a_place);
        }

        title.setText(getResources().getString(titleRes));
    }

    @Override public void doContentSection() {
        Bundle args = getArguments();
        if (args == null) {
            BackstackManager.getInstance().navigateBack();
            return;
        }

        List<PlaceAndRoleModel> modelList = args.getParcelableArrayList(MODEL_LIST);
        if (modelList == null || modelList.isEmpty()) {
            return;
        }

        List<ListItemModel> models = getModelListForAdapter(modelList, args);
        final MultiModelAdapter multiModelAdapter = new MultiModelAdapter(getActivity(), models, false, R.layout.floating_place_picker_item);
        ListView listView = (ListView) contentView.findViewById(R.id.floating_list_view);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                multiModelAdapter.selectItem(position);
                if (callback != null) {
                    callback.itemSelectedAddress(multiModelAdapter.getItem(position).getAddress());
                    callback = null;
                }
                BackstackManager.getInstance().navigateBack();
            }
        });
        listView.setAdapter(multiModelAdapter);

        contentView.findViewById(R.id.padding_element).setVisibility(View.VISIBLE);
    }

    protected List<ListItemModel> getModelListForAdapter(List<PlaceAndRoleModel> placeList, Bundle args) {
        selectedAddress = args.getString(MODELS_SELECTED, "");

        List<ListItemModel> modelItemList = new LinkedList<>();
        for (PlaceAndRoleModel place : placeList) {
            ListItemModel itemModel = new ListItemModel();
            itemModel.setAddress(place.getAddress());
            itemModel.setText(getStringOrEmpty(place.getName()));
            itemModel.setSubText(place.getStreetAddress1());
            itemModel.setChecked(selectedAddress.equals(place.getAddress()));
            modelItemList.add(itemModel);
        }

        Collections.sort(modelItemList, new Comparator<ListItemModel>() {
            @Override
            public int compare(ListItemModel lm1, ListItemModel lm2) {
                if(lm1 == null){
                    lm1 = new ListItemModel();
                }
                if(lm2 == null){
                    lm2 = new ListItemModel();
                }
                return lm1.getText().toUpperCase().compareTo(lm2.getText().toUpperCase());
            }
        });
        return modelItemList;
    }

    protected String getStringOrEmpty(String from) {
        if (TextUtils.isEmpty(from)) {
            return "";
        }

        return from;
    }

    @Override public void onResume() {
        super.onResume();
        showFullScreen(true);
    }

    @Override public void onPause() {
        super.onPause();
        showFullScreen(false);
    }

    @Override public Integer contentSectionLayout() {
        return R.layout.floating_list_picker;
    }

    @Override @NonNull public String getTitle() {
        return "";
    }

    @Override public Integer getLayoutId() {
        return R.layout.fullscreen_arcus_popup_fragment;
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }
}

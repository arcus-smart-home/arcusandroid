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
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import arcus.cornea.model.StringPair;
import arcus.app.R;
import arcus.app.activities.BaseActivity;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.models.ListItemModel;
import arcus.app.common.popups.adapter.MultiModelAdapter;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class StringPairPopup extends ArcusFloatingFragment {
    private static final String LIST = "LIST";
    private Reference<Callback> callbackRef;

    public interface Callback {
        void selectedItems(StringPair selection);
    }

    public static StringPairPopup newInstance(@Nullable List<StringPair> stringList) {
        StringPairPopup popup = new StringPairPopup();
        Bundle bundle = new Bundle(1);
        if (stringList == null) {
            stringList = new ArrayList<>();
        }
        ArrayList<StringPair> list = new ArrayList<>(stringList);
        bundle.putSerializable(LIST, list);

        popup.setArguments(bundle);
        return popup;
    }

    public void setCallback(Callback callback) {
        this.callbackRef = new WeakReference<>(callback);
    }

    @Override public void setFloatingTitle() {
        title.setText(getTitle());
    }

    @Override public void onResume() {
        super.onResume();
        ((BaseActivity)getActivity()).getSupportActionBar().hide();
    }

    @Override @SuppressWarnings({"unchecked"}) public void doContentSection() {
        ListView listView = (ListView) contentView.findViewById(R.id.floating_list_view);
        listView.setFooterDividersEnabled(false);
        List<StringPair> items = (List<StringPair>) getArguments().getSerializable(LIST);
        if (items == null) {
            items = new ArrayList<>();
        }

        List<ListItemModel> itemModelList = new ArrayList<>(items.size());
        for (StringPair item : items) {
            itemModelList.add(new ListItemModel(item.getKey(), item.getValue()));
        }

        listView.setAdapter(new MultiModelAdapter(getActivity(), itemModelList, false));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MultiModelAdapter adapter = (MultiModelAdapter) parent.getAdapter();
                Callback callback = callbackRef.get();
                if (callback != null) {
                    ListItemModel selected = adapter.getItem(position);
                    callback.selectedItems(new StringPair(selected.getText(), selected.getSubText()));
                }
                BackstackManager.getInstance().navigateBack();
            }
        });
    }

    @Override public void onPause() {
        super.onPause();
        ((BaseActivity)getActivity()).getSupportActionBar().show();
    }

    @Override public Integer contentSectionLayout() {
        return R.layout.floating_list_picker;
    }

    @NonNull @Override public String getTitle() {
        String choose = getString(R.string.choose_devices_text);
        return choose.substring(0, choose.length() - 1);
    }
}

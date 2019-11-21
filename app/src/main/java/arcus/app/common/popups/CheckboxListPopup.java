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
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import arcus.app.R;
import arcus.app.common.popups.adapter.CheckboxItemAdapter;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class CheckboxListPopup extends ArcusFloatingFragment {

    private final static String TITLE_KEY = "title";
    private final static String ITEMS_KEY = "items";
    private final static String CHECKED_ITEMS_KEY = "checked-items";
    private final static String SINGLE_SELECTION_KEY = "single-selection-mode";
    private final static String FULL_SCREEN_KEY = "full-screen-mode";

    private WeakReference<Callback> callbackReference = new WeakReference<>(null);

    public interface Callback {
        void onItemsSelected(List<String> selections);
    }

    public static CheckboxListPopup newInstance(
            String title,
            @NonNull ArrayList<String> items,
            @NonNull ArrayList<String> checkedItems,
            boolean singleSelection) {

        return newInstance(title, items, checkedItems, singleSelection, false);
    }

    public static CheckboxListPopup newInstance(
            String title,
            @NonNull ArrayList<String> items,
            @NonNull ArrayList<String> checkedItems,
            boolean singleSelection,
            boolean isFullScreen) {

        CheckboxListPopup instance = new CheckboxListPopup();

        Bundle arguments = new Bundle();
        arguments.putString(TITLE_KEY, title);
        arguments.putStringArrayList(ITEMS_KEY, items);
        arguments.putStringArrayList(CHECKED_ITEMS_KEY, checkedItems);
        arguments.putBoolean(SINGLE_SELECTION_KEY, singleSelection);
        arguments.putBoolean(FULL_SCREEN_KEY, isFullScreen);
        instance.setArguments(arguments);

        return instance;
    }

    public void setCallback(Callback callback) {
        callbackReference = new WeakReference<>(callback);
    }

    @Override
    public void setFloatingTitle() {
        title.setText(getTitle());
    }

    @Override
    public void doContentSection() {

        ListView listView = (ListView) contentView.findViewById(R.id.floating_list_view);

        final CheckboxItemAdapter adapter = new CheckboxItemAdapter<String>(getContext(), getItems(), getCheckedItems()) {
            @Override
            public void setItemText(TextView textView, String item) {
                textView.setText(item);
            }
        };
        adapter.setSingleSelectionMode(getSingleSelection());

        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                List<String> checkedItems = adapter.toggleCheck(position);
                if (callbackReference.get() != null) {
                    callbackReference.get().onItemsSelected(checkedItems);
                }
            }
        });
    }

    @Override public void onResume() {
        super.onResume();
        if (getArguments().getBoolean(FULL_SCREEN_KEY)) {
            showFullScreen(true);
        }
    }

    @Override public void onPause() {
        super.onPause();
        if (getArguments().getBoolean(FULL_SCREEN_KEY)) {
            showFullScreen(false);
        }
    }

    @Override
    public Integer contentSectionLayout() {
        return R.layout.floating_list_picker;
    }

    @NonNull
    @Override
    public String getTitle() {
        return getArguments().getString(TITLE_KEY);
    }

    @Override
    public Integer getLayoutId() {
        if (getArguments().getBoolean(FULL_SCREEN_KEY)) {
            return R.layout.fullscreen_arcus_popup_fragment;
        }
        else {
            return super.getLayoutId();
        }
    }

    private boolean getSingleSelection() {
        return getArguments().getBoolean(SINGLE_SELECTION_KEY);
    }

    private ArrayList<String> getItems() {
        return getArguments().getStringArrayList(ITEMS_KEY);
    }

    private ArrayList<String> getCheckedItems() {
        return getArguments().getStringArrayList(CHECKED_ITEMS_KEY);
    }
}

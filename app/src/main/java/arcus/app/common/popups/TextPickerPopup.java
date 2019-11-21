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
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.NumberPicker;

import arcus.app.R;
import arcus.app.common.adapters.TextListItemAdapter;

import java.util.ArrayList;
import java.util.List;

public class TextPickerPopup extends ArcusFloatingFragment {
    private ListView picker;
    private TextListItemAdapter adapter;
    private Callback callback;
    private static String screenTitle;
    private boolean bShowDone = false;

    @NonNull
    private static List<String> values = new ArrayList<>();

    @NonNull
    public static TextPickerPopup newInstance() {
        return new TextPickerPopup();
    }

    @NonNull
    public static TextPickerPopup newInstance(List<String> items, String popupTitle) {
        TextPickerPopup instance = new TextPickerPopup();
        values = items;
        screenTitle = popupTitle;
        return instance;
    }

    public void showDone() {
        this.bShowDone = true;
    }

    @Override
    public void onResume() {
        super.onResume();
        setHasDoneButton(bShowDone);
    }

    @Override
    public void setFloatingTitle() {
        title.setText(screenTitle);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        closeBtn.setOnClickListener(this);
        if(doneBtn != null) {
            doneBtn.setOnClickListener(this);
        }

        view.setFocusableInTouchMode(true);
        view.requestFocus();
        view.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
                    doClose();
                    return true;
                } else {
                    return false;
                }
            }
        });

        return view;
    }

    @Override
    public void doContentSection() {

        picker = (ListView) contentView.findViewById(R.id.floating_text_picker);
        picker.setVisibility(View.GONE);
        picker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        adapter = new TextListItemAdapter(getActivity(), values);


        picker.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (callback != null) {
                    callback.selectedItem(values.get(position));
                }
            }
        });

        picker.setAdapter(adapter);
        picker.setVisibility(View.VISIBLE);
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.floating_list_picker_fragment;
    }

    @Override
    public Integer contentSectionLayout() {
        return R.layout.floating_text_picker;
    }

    @NonNull
    @Override
    public String getTitle() {
        return "";
    }

    @Override
    public void onClick(@NonNull View v) {
        final int id = v.getId();
        switch (id){
            case R.id.fragment_arcus_pop_up_close_btn:
            case R.id.fragment_arcus_pop_up_done:
                doClose();
                break;
        }
    }

    @Override
    public void doClose() {
        if (callback != null) {
            callback.close();
        }
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public interface Callback {
        void selectedItem(String id);
        void close();
    }
}

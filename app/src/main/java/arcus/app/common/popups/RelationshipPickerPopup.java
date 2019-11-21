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
import android.view.WindowManager;
import android.widget.ListView;

import arcus.app.R;
import arcus.app.subsystems.people.adapter.PersonRelationshipAdapter;
import arcus.app.subsystems.people.model.PersonRelationship;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class RelationshipPickerPopup extends ArcusFloatingFragment implements PersonRelationshipAdapter.SelectionChangedListener {
    private static final String ITEMS = "ITEMS";
    private Reference<Callback> callbackRef = new WeakReference<>(null);
    private String selection;
    private String selectionType;

    @Override
    public void onSelectionChanged(String selectionType, String selectionName) {
        this.selectionType = selectionType;
        selection = selectionName;
    }

    @Override
    public void onUpdateChildText(String selectionType, String selectionName) {

    }

    public interface Callback {
        void updatedValue(String selectionType, String message);
    }

    public static RelationshipPickerPopup newInstance(ArrayList<PersonRelationship> items) {
        RelationshipPickerPopup popup = new RelationshipPickerPopup();
        Bundle bundle = new Bundle(1);
        bundle.putParcelableArrayList(ITEMS, items);

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
        hideActionBar();
    }

    @Override @SuppressWarnings({"unchecked"}) public void doContentSection() {
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        ArrayList<PersonRelationship> relationships = getArguments().getParcelableArrayList(ITEMS);

        PersonRelationshipAdapter adapter = new PersonRelationshipAdapter(getActivity(), relationships);
        adapter.setListener(this);

        ListView tagList = (ListView) contentView.findViewById(R.id.list);
        tagList.setAdapter(adapter);

    }

    @Override public void onPause() {
        super.onPause();
        showActionBar();
    }

    @Override public Integer contentSectionLayout() {
        return R.layout.popup_relationship_picker;
    }

    @NonNull @Override public String getTitle() {
        return getResources().getString(R.string.choose_relationship);
    }

    @Override
    public Integer getMenuId() {
        return R.menu.menu_close;
    }

    @Override
    public void doClose() {
        Callback callback = callbackRef.get();
        if(callback != null) {
            callback.updatedValue(selectionType, selection);
        }
    }

    @Override
    public boolean onBackPressed() {
        Callback callback = callbackRef.get();
        if(callback != null) {
            callback.updatedValue(selectionType, selection);
        }
        return super.onBackPressed();
    }
}

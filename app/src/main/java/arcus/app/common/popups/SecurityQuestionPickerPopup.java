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
import android.widget.ListView;

import arcus.app.R;
import arcus.app.activities.BaseActivity;
import arcus.app.common.adapters.CheckableListAdapter;
import arcus.app.common.backstack.BackstackManager;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

public class SecurityQuestionPickerPopup extends ArcusFloatingFragment {

    public static final String ITEM_LIST_KEY = "ITEM LIST KEY";
    public static final String POP_UP_TITLE = "POP UP TITLE";
    public static final String DEFAULTSELECTION = "DEFAULTSELECTION";
    public static final String DISABLE_ONE = "DISABLEONE";
    public static final String DISABLE_TWO = "DISABLETWO";
    public static final String QUESTIONUPDATED = "QUESTIONUPDATED";

    private String mPopupTitle;
    private String[] questions;
    private int defaultSelection;
    private int disableOne;
    private int disableTwo;
    private int questionUpdated;
    @Nullable
    private CheckableListAdapter adapter;
    private Reference<Callback> callbackRef;
    private ListView listView;

    public interface Callback {
        void updatedValue(int updatedQuestion, int selection);
    }

    public void setCallback(Callback callback) {
        this.callbackRef = new WeakReference<>(callback);
    }

    @NonNull
    public static SecurityQuestionPickerPopup newInstance(String popupTitle, String[] questions,
                                                          int defaultSelection, int disabledOne, int disableTwo,
                                                          int questionBeingUpdated) {
        SecurityQuestionPickerPopup popup = new SecurityQuestionPickerPopup();

        Bundle bundle = new Bundle();
        bundle.putString(POP_UP_TITLE,popupTitle);
        bundle.putInt(DEFAULTSELECTION, defaultSelection);
        bundle.putSerializable(ITEM_LIST_KEY, questions);
        bundle.putInt(DISABLE_ONE, disabledOne);
        bundle.putInt(DISABLE_TWO, disableTwo);
        bundle.putInt(QUESTIONUPDATED, questionBeingUpdated);
        popup.setArguments(bundle);

        return popup;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Bundle arguments = getArguments();
        if (arguments != null) {
            mPopupTitle = arguments.getString(POP_UP_TITLE,"");
            defaultSelection = arguments.getInt(DEFAULTSELECTION);
            questions = (String[]) arguments.getSerializable(ITEM_LIST_KEY);
            disableOne = arguments.getInt(DISABLE_ONE);
            disableTwo = arguments.getInt(DISABLE_TWO);
            questionUpdated = arguments.getInt(QUESTIONUPDATED);
            adapter = new CheckableListAdapter(getActivity(), R.layout.cell_checkable_item, questions, defaultSelection);
            adapter.setDisabledItems(disableOne, disableTwo);
        }
        else {
            BackstackManager.getInstance().navigateBack();
        }
    }

    @Override public void onResume() {
        super.onResume();
        ((BaseActivity)getActivity()).getSupportActionBar().hide();
    }

    @Override public void onPause() {
        super.onPause();
        ((BaseActivity)getActivity()).getSupportActionBar().show();
    }

    @Override
    public void setFloatingTitle() {
        title.setText(mPopupTitle);
    }

    @Override
    public void doContentSection() {
        listView = (ListView) contentView.findViewById(R.id.list);
        listView.setAdapter(adapter);
    }

    @Override
    public Integer contentSectionLayout() {
        return R.layout.floating_security_question_picker;
    }

    @NonNull
    @Override
    public String getTitle() {
        return "";
    }

    @Override
    public void doClose() {
        if(callbackRef != null) {
            callbackRef.get().updatedValue(questionUpdated, getAdapterSelection());
        }
    }

    @Override
    public boolean onBackPressed() {
        doClose();
        BackstackManager.getInstance().navigateBack();
        return true;
    }

    private int getAdapterSelection() {
        return ((CheckableListAdapter)listView.getAdapter()).getSelectedItem();
    }

}


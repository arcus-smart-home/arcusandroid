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

import arcus.app.R;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.popups.adapter.PopupButtonAdapter;

import java.util.ArrayList;
import java.util.List;


public class MultiButtonPopup extends ArcusFloatingFragment {

    public static final String BUTTON_LIST_KEY = "BUTTON LIST KEY";
    public static final String POP_UP_TITLE = "POP UP TITLE";
    @Nullable
    private List<String> mButtonList;
    private String mPopupTitle;
    @Nullable
    private OnButtonClickedListener onButtonClickedListener;

    @NonNull
    public static MultiButtonPopup newInstance(String popupTitle, @Nullable ArrayList<String> buttonList) {
        MultiButtonPopup popup = new MultiButtonPopup();

        if (buttonList != null) {
            Bundle bundle = new Bundle();
            bundle.putString(POP_UP_TITLE,popupTitle);
            bundle.putStringArrayList(BUTTON_LIST_KEY,buttonList);
            popup.setArguments(bundle);
        }

        return popup;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Bundle arguments = getArguments();
        if (arguments != null) {
            mPopupTitle = arguments.getString(POP_UP_TITLE,"");
            mButtonList = arguments.getStringArrayList(BUTTON_LIST_KEY);
        }
    }

    @Override
    public void setFloatingTitle() {
        title.setText(mPopupTitle);
    }

    @Override
    public void doContentSection() {
        ListView listView = (ListView) contentView.findViewById(R.id.floating_list_view);
        listView.setAdapter(new PopupButtonAdapter(getActivity(), mButtonList));
        listView.setDivider(null);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mButtonList != null) {
                    if (onButtonClickedListener != null) {
                        onButtonClickedListener.onButtonClicked(mButtonList.get(position));
                    }
                    BackstackManager.getInstance().navigateBack();
                    removeOnButtonClickedListener();
                }
            }
        });
    }


    @Override
    public Integer contentSectionLayout() {
        return R.layout.floating_list_picker;
    }

    public void setOnButtonClickedListener(@Nullable OnButtonClickedListener onButtonClickedListener) {
        this.onButtonClickedListener = onButtonClickedListener;
    }

    public void removeOnButtonClickedListener(){
        this.onButtonClickedListener = null;
    }

    @NonNull
    @Override
    public String getTitle() {
        return "";
    }

    public interface OnButtonClickedListener{
        void onButtonClicked(String buttonValue);
    }
}

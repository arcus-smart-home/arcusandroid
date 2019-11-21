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
import android.widget.TextView;

import arcus.app.R;
import arcus.app.common.events.ButtonSelected;
import arcus.app.common.popups.adapter.PopupButtonAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import de.greenrobot.event.EventBus;

public class ButtonListPopup extends ArcusFloatingFragment {
    private static final String BUTTON_MAP = "BUTTON.MAP";
    private static final String TITLE_ARG = "TITLE.ARG";
    private static final String SORTED = "SORTED";
    private static final String DESCRIPTION_RES = "DESCRIPTION.RES";
    @NonNull
    private List<String> buttonText = new ArrayList<>();
    @Nullable
    private Map<String, String> buttonMap;
    private Callback callback;
    private static final Comparator<String> sortedByNumberOrName = new Comparator<String>() {
        @Override
        public int compare(@NonNull String lhs, @NonNull String rhs) {
            try {
                Double lhsDouble = Double.parseDouble(lhs);
                Double rhsDouble = Double.parseDouble(rhs);
                return lhsDouble.compareTo(rhsDouble);
            }
            catch (Exception ex) {
                return lhs.compareToIgnoreCase(rhs);
            }
        }
    };

    public interface Callback {
        void buttonSelected(String buttonKeyValue);
    }

    @NonNull
    public static ButtonListPopup newInstance(@Nullable LinkedHashMap<String, String> buttonMap) {
        ButtonListPopup popup = new ButtonListPopup();

        Bundle bundle = new Bundle(2);
        if (buttonMap != null) {
            LinkedHashMap<String, String> buttons = new LinkedHashMap<>(buttonMap);
            bundle.putSerializable(BUTTON_MAP, buttons);
        }
        bundle.putBoolean(SORTED, false);
        popup.setArguments(bundle);

        return popup;
    }

    @NonNull
    public static ButtonListPopup newInstance(@Nullable Map<String, String> buttonMap, int title, int description) {
        ButtonListPopup popup = new ButtonListPopup();

        Bundle bundle = new Bundle(4);
        if (buttonMap != null) {
            LinkedHashMap<String, String> buttons = new LinkedHashMap<>(buttonMap);
            bundle.putSerializable(BUTTON_MAP, buttons);
        }
        bundle.putInt(DESCRIPTION_RES, description);
        bundle.putBoolean(SORTED, false);
        bundle.putInt(TITLE_ARG, title);
        popup.setArguments(bundle);

        return popup;
    }

    @Override
    public void setFloatingTitle() {
        title.setText(getTitle());
    }

    @Override @SuppressWarnings({"unchecked"})
    public void doContentSection() {
        setDescriptionText();

        buttonMap = (LinkedHashMap<String, String>) getNonNullBundle().getSerializable(BUTTON_MAP);
        if (buttonMap != null) {
            buttonText.addAll(buttonMap.keySet());
            if (shouldSort()) {
                Collections.sort(buttonText, sortedByNumberOrName);
            }
        }

        ListView listView = (ListView) contentView.findViewById(R.id.floating_list_view);
        listView.setAdapter(new PopupButtonAdapter(getActivity(), buttonText));
        listView.setDivider(null);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (buttonMap != null) {
                    if (callback != null) {
                        callback.buttonSelected(buttonMap.get(buttonText.get(position)));
                    }
                    else {
                        EventBus.getDefault().post(new ButtonSelected(buttonMap.get(buttonText.get(position))));
                    }
                }
            }
        });
    }

    @Override
    public Integer contentSectionLayout() {
        return R.layout.floating_list_picker;
    }

    @Override
    public String getTitle() {
        return getString(getNonNullBundle().getInt(TITLE_ARG, R.string.choose_button_action_text));
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.floating_list_picker_fragment;
    }

    public void setDescriptionText() {
        int descriptionRes = getNonNullBundle().getInt(DESCRIPTION_RES, -1);
        if (descriptionRes != -1) {
            TextView view = (TextView) contentView.findViewById(R.id.fragment_arcus_pop_up_description);
            if (view != null) {
                view.setVisibility(View.VISIBLE);
                view.setText(getString(descriptionRes));
            }
        }
    }
    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public boolean shouldSort() {
        return getNonNullBundle().getBoolean(SORTED, true);
    }

    private Bundle getNonNullBundle() {
        Bundle bundle = getArguments();
        if (bundle == null) {
            return new Bundle(1);
        }
        return bundle;
    }
}

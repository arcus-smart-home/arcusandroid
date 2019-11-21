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
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import android.text.TextUtils;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.TextView;

import arcus.cornea.model.StringPair;
import arcus.app.R;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class TupleSelectorPopup extends ArcusFloatingFragment {
    private static final String SELECTIONS                  = "SELECTIONS";
    private static final String SELECTED                    = "SELECTED";
    private static final String SELECTED_LABEL_TRANSFORM    = "SELECTED_LABEL_TRANSFORM";
    private static final String TITLE                       = "TITLE";
    private static final String TITLE_STRING                = "TITLE_STRING";
    private static final String ABSTRACT                    = "ABSTRACT";
    private static final String DIVIDER                     = "DIVIDER";

    private ArrayList<StringPair> bundleStrings;
    private NumberPicker picker;
    private Callback callback;
    private boolean bShowDone = false;

    public interface Callback {
        void selectedItem(StringPair selected);
    }

    public interface DurationTransformer extends Serializable {
        /**
         * Takes a selector key/value pair and transforms it for display purposes. For example,
         * an input may provide {300,Minutes} but for human readable display, this method could
         * be used to transform it to {5,Hours}.
         *
         * @param value Input key (duration) and value (units) to be transformed
         * @return
         */
        StringPair transformDuration(StringPair value);
    }

    public static TupleSelectorPopup newInstance(List<StringPair> selections, @StringRes int title, @Nullable String selected) {
        return newInstance(selections, title, selected, null);
    }

    public static TupleSelectorPopup newInstance(
          List<StringPair> selections,
          @StringRes int title,
          @Nullable String selected,
          @Nullable Boolean showDivider
    ) {
        TupleSelectorPopup popup = new TupleSelectorPopup();

        Bundle bundle = new Bundle(4);

        ArrayList<StringPair> values;
        if (selections == null) {
            values = new ArrayList<>();
        }
        else {
            values = new ArrayList<>(selections);
        }
        bundle.putSerializable(SELECTIONS, values);
        bundle.putInt(TITLE, title);
        bundle.putString(SELECTED, selected);
        bundle.putBoolean(DIVIDER, Boolean.TRUE.equals(showDivider));

        popup.setArguments(bundle);
        return popup;
    }

    public static TupleSelectorPopup newInstance(
          List<StringPair> selections,
          String title,
          @Nullable String selected,
          @Nullable String abstractText,
          @Nullable Boolean showDivider
    ) {
        TupleSelectorPopup popup = new TupleSelectorPopup();

        Bundle bundle = new Bundle(5);

        ArrayList<StringPair> values;
        if (selections == null) {
            values = new ArrayList<>();
        }
        else {
            values = new ArrayList<>(selections);
        }
        bundle.putSerializable(SELECTIONS, values);
        bundle.putString(TITLE_STRING, title);
        bundle.putString(SELECTED, selected);
        bundle.putString(ABSTRACT, abstractText);
        bundle.putBoolean(DIVIDER, Boolean.TRUE.equals(showDivider));

        popup.setArguments(bundle);
        return popup;
    }

    public static TupleSelectorPopup newInstance(
            List<StringPair> selections,
            String title,
            @Nullable String selected,
            @Nullable DurationTransformer selectedLabelTransform,
            @Nullable Boolean showDivider
    ) {
        TupleSelectorPopup popup = new TupleSelectorPopup();

        Bundle bundle = new Bundle(5);

        ArrayList<StringPair> values;
        if (selections == null) {
            values = new ArrayList<>();
        }
        else {
            values = new ArrayList<>(selections);
        }
        bundle.putSerializable(SELECTIONS, values);
        bundle.putString(TITLE_STRING, title);
        bundle.putString(SELECTED, selected);
        bundle.putSerializable(SELECTED_LABEL_TRANSFORM, selectedLabelTransform);
        bundle.putBoolean(DIVIDER, Boolean.TRUE.equals(showDivider));

        popup.setArguments(bundle);
        return popup;
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
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
        if (getTitle() == null) {
            title.setVisibility(View.GONE);
        }
        else {
            title.setText(getTitle());
        }
    }

    @Override @SuppressWarnings({"unchecked"})
    public void doContentSection() {
        Bundle args = getArguments();
        if (args == null) {
            return; // Nothing to show...
        }

        final DurationTransformer transform = (DurationTransformer) getArguments().getSerializable(SELECTED_LABEL_TRANSFORM);

        try {
            bundleStrings = (ArrayList<StringPair>) args.getSerializable(SELECTIONS);
            if (bundleStrings == null) {
                return; // Again, nothing to show.
            }
        }
        catch (Exception ex) {
            logger.error("Could not deserialize SELECTIONS", ex);
            return;
        }

        String selectedValue = args.getString(SELECTED);
        int selectedIndex = -1;
        int bundleSize = bundleStrings.size();
        String[] pickerValues = new String[bundleSize];
        for (int i = 0; i < bundleSize; i++) {
            pickerValues[i] = bundleStrings.get(i).getValue();
            if (transform != null) {
                pickerValues[i] = transform.transformDuration(bundleStrings.get(i)).getValue();
            }

            if (bundleStrings.get(i).getKey().equals(selectedValue)) {
                selectedIndex = i;
            }
        }

        boolean showDivider = args.getBoolean(DIVIDER, false);
        View divider = contentView.findViewById(R.id.picker_title_divider);
        if (showDivider) {
            if (divider != null) {
                divider.setVisibility(View.VISIBLE);
            }
        }
        else {
            if (divider != null) {
                divider.setVisibility(View.GONE);
            }
        }

        picker = (NumberPicker) contentView.findViewById(R.id.floating_day_number_picker);
        picker.setMinValue(0);
        picker.setMaxValue(bundleSize - 1);
        picker.setDisplayedValues(pickerValues);
        picker.setValue(selectedIndex != -1 ? selectedIndex : 0);

        final TextView abstractTextView = (TextView) contentView.findViewById(R.id.day_number_picker_abstract);

        picker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                if (transform != null && bundleStrings != null && abstractTextView != null) {
                    abstractTextView.setVisibility(View.VISIBLE);
                    abstractTextView.setText(transform.transformDuration(bundleStrings.get(newVal)).getKey());
                }
            }
        });

        String abstractText = args.getString(ABSTRACT);
        if (!TextUtils.isEmpty(abstractText) && abstractTextView != null) {
            abstractTextView.setText(abstractText);
            abstractTextView.setVisibility(View.VISIBLE);
        } else if (transform != null && abstractTextView != null) {
            abstractTextView.setText(transform.transformDuration(bundleStrings.get(picker.getValue())).getKey());
            abstractTextView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public Integer contentSectionLayout() {
        return R.layout.floating_day_picker;
    }

    @Override
    public String getTitle() {
        Bundle args = getArguments();
        if (args == null) {
            return null;
        }

        int titleRes = args.getInt(TITLE, 0);
        if (titleRes != 0) {
            return getString(titleRes);
        }
        else {
            String title = getArguments().getString(TITLE_STRING);
            if (!TextUtils.isEmpty(title)) {
                return title;
            }
        }

        return null;
    }

    @Override
    public void doClose() {
        if (callback == null || bundleStrings == null) {
            return;
        }

        int selectedIndex = picker.getValue();
        if (selectedIndex >= bundleStrings.size() || selectedIndex < 0) {
            return;
        }

        callback.selectedItem(bundleStrings.get(selectedIndex));
    }
}

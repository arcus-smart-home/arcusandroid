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
package arcus.app.subsystems.lawnandgarden.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;

import arcus.cornea.model.StringPair;
import arcus.cornea.subsystem.lawnandgarden.LawnAndGardenDeviceMoreController;
import arcus.cornea.subsystem.lawnandgarden.model.LawnAndGardenControllerZoneDetailModel;
import arcus.cornea.subsystem.lawnandgarden.utils.LNGDefaults;
import arcus.cornea.utils.Listeners;
import com.iris.client.event.ListenerRegistration;
import arcus.app.R;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.error.ErrorManager;
import arcus.app.common.fragments.BaseFragment;
import arcus.app.common.popups.TupleSelectorPopup;
import arcus.app.common.view.Version1Button;
import arcus.app.common.view.Version1ButtonColor;
import arcus.app.common.view.Version1EditText;

import java.util.concurrent.TimeUnit;

public class LawnAndGardenEditZoneInfoFragment extends BaseFragment
      implements LawnAndGardenDeviceMoreController.SaveCallback, View.OnClickListener
{
    private static final String ZONE_MODEL_KEY = "ZONE_MODEL_KEY";
    private static final int MAX_NAME_LENGTH = 25;
    private static final String EDIT_MODE_KEY = "EDIT_MODE_KEY";
    private static final String SHOULD_SET_TITLE = "SHOULD_SET_TITLE";

    private LawnAndGardenControllerZoneDetailModel zoneDetailModel;
    private ListenerRegistration listener;

    protected Version1EditText zoneNameEdit;
    protected Version1Button saveButton;
    protected TextView defaultDuration;
    protected final TextWatcher textWatcher = new TextWatcher() {
        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
        @Override public void afterTextChanged(Editable s) {
            if (zoneNameEdit == null || saveButton == null) { // Can happen when we're building the fragment.
                return;
            }

            if (s == null || s.length() == 0) {
                zoneNameEdit.setError(getString(R.string.lng_zone_name_error));
                saveButton.setEnabled(false);
            }
            else {
                saveButton.setEnabled(true);
                zoneNameEdit.setError(null);
            }
        }
    };
    protected final TupleSelectorPopup.Callback tupleCallback = new TupleSelectorPopup.Callback() {
        @Override public void selectedItem(StringPair selected) {
            if (zoneDetailModel == null || defaultDuration == null) {
                return;
            }

            int minutes = Integer.valueOf(selected.getKey());
            zoneDetailModel.setDefaultWateringTime(minutes);

            int hrs = (int) TimeUnit.MINUTES.toHours(minutes);
            if (hrs != 0) {
                defaultDuration.setText(getResources().getQuantityString(R.plurals.care_hours_plural, hrs, hrs));
            }
            else {
                defaultDuration.setText(getResources().getQuantityString(R.plurals.care_minutes_plural, minutes, minutes));
            }
        }
    };

    public static LawnAndGardenEditZoneInfoFragment newInstance(
          LawnAndGardenControllerZoneDetailModel zoneModel
    ) {
        return newInstance(zoneModel, true, true);
    }

    public static LawnAndGardenEditZoneInfoFragment newInstance(
          LawnAndGardenControllerZoneDetailModel zoneModel,
          boolean useEditColorScheme,
          boolean shouldSetTitle
    ) {
        LawnAndGardenEditZoneInfoFragment fragment = new LawnAndGardenEditZoneInfoFragment();

        Bundle args = new Bundle(3);
        args.putParcelable(ZONE_MODEL_KEY, zoneModel);
        args.putBoolean(EDIT_MODE_KEY, useEditColorScheme);
        args.putBoolean(SHOULD_SET_TITLE, shouldSetTitle);
        fragment.setArguments(args);

        return fragment;
    }

    @Override public void onResume() {
        super.onResume();
        View rootView = getView();
        if (rootView == null) {
            return;
        }

        listener = LawnAndGardenDeviceMoreController.instance().setSaveCallback(this);
        Bundle args = getArguments();
        if (args == null) {
            args = new Bundle();
        }

        boolean useLightColorScheme = args.getBoolean(EDIT_MODE_KEY, true);

        View durationContainer = rootView.findViewById(R.id.zone_default_duration_container);
        defaultDuration = (TextView) rootView.findViewById(R.id.abstract_text);
        int editTextID = useLightColorScheme ? R.id.zone_name_edit_light : R.id.zone_name_edit_dark;
        zoneNameEdit = (Version1EditText) rootView.findViewById(editTextID);
        zoneNameEdit.setVisibility(View.VISIBLE); // b/c of the way these are init'ed and the "X" to clear is drawn. it's easier to have two views for this.
        zoneNameEdit.setFilters(new InputFilter[]{new InputFilter.LengthFilter(MAX_NAME_LENGTH)});

        zoneNameEdit.useLightColorScheme(true);
        zoneNameEdit.removeTextChangedListener(textWatcher);
        zoneNameEdit.addTextChangedListener(textWatcher);

        zoneDetailModel = args.getParcelable(ZONE_MODEL_KEY);
        if (zoneDetailModel != null) {
            if (TextUtils.isEmpty(zoneDetailModel.getZoneName())) {
                zoneNameEdit.setText(String.format("Zone %s", zoneDetailModel.getZoneNumber()));
            }
            else {
                zoneNameEdit.setText(zoneDetailModel.getZoneName());
            }
            defaultDuration.setText(String.format("%d Min", zoneDetailModel.getDefaultWateringTime()));

            Activity activity = getActivity();
            if (activity != null && args.getBoolean(SHOULD_SET_TITLE, true)) {
                activity.setTitle(getString(R.string.generic_zone_with_number, zoneDetailModel.getZoneNumber()));
                activity.invalidateOptionsMenu();
            }
        }

        saveButton = (Version1Button) rootView.findViewById(R.id.zone_edit_save_button);

        ImageView chevron = (ImageView) rootView.findViewById(R.id.imageView2);
        TextView titleCopy = (TextView) rootView.findViewById(R.id.title);
        TextView descriptionCopy = (TextView) rootView.findViewById(R.id.description);
        zoneNameEdit.useLightColorScheme(useLightColorScheme);
        if (useLightColorScheme) {
            titleCopy.setTextColor(getResources().getColor(R.color.white));
            descriptionCopy.setTextColor(getResources().getColor(R.color.overlay_white_with_60));
            defaultDuration.setTextColor(getResources().getColor(R.color.overlay_white_with_60));
            saveButton.setColorScheme(Version1ButtonColor.WHITE);
            chevron.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.chevron_white));
        }
        else {
            titleCopy.setTextColor(getResources().getColor(R.color.black));
            descriptionCopy.setTextColor(getResources().getColor(R.color.black_with_60));
            defaultDuration.setTextColor(getResources().getColor(R.color.black_with_60));
            saveButton.setColorScheme(Version1ButtonColor.BLACK);
            chevron.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.chevron));
        }

        durationContainer.setOnClickListener(this);
        saveButton.setOnClickListener(this);
    }

    @Override public void onPause() {
        super.onPause();

        Listeners.clear(listener);
    }

    @Override public void onClick(View v) {
        if (v == null || zoneDetailModel == null || zoneNameEdit == null) {
            return;
        }

        switch (v.getId()) {
            case R.id.zone_default_duration_container:
                zoneNameEdit.clearFocus();
                InputMethodManager imm = (InputMethodManager) zoneNameEdit.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(zoneNameEdit.getWindowToken(), 0);

                TupleSelectorPopup popup = TupleSelectorPopup.newInstance(
                      LNGDefaults.wateringTimeOptions(),
                      R.string.irrigation_duration,
                      String.valueOf(zoneDetailModel.getDefaultWateringTime()), true
                );
                popup.setCallback(tupleCallback);
                BackstackManager.getInstance().navigateToFloatingFragment(popup, popup.getClass().getCanonicalName(), true);
                break;

            case R.id.zone_edit_save_button:
                zoneDetailModel.setZoneName(zoneNameEdit.getText().toString().trim());
                LawnAndGardenDeviceMoreController.instance().updateZone(zoneDetailModel);
                break;

            default:
                break; /* no-op */
        }
    }

    @Override public void onError(Throwable throwable) {
        ErrorManager.in(getActivity()).showGenericBecauseOf(throwable);
    }

    @Override public void onSuccess() {
        BackstackManager.getInstance().navigateBack();
    }

    @Nullable @Override public String getTitle() {
        return null;
    }

    @Override public Integer getLayoutId() {
        return R.layout.lawn_and_garden_zone_edit_info;
    }
}

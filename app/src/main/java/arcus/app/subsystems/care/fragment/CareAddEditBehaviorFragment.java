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
package arcus.app.subsystems.care.fragment;

import android.app.Activity;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import arcus.cornea.events.TimeSelectedEvent;
import arcus.cornea.model.StringPair;
import arcus.cornea.subsystem.care.CareBehaviorController;
import arcus.cornea.subsystem.care.model.CareBehaviorModel;
import arcus.cornea.subsystem.care.model.CareBehaviorTemplateModel;
import arcus.cornea.subsystem.care.model.CareBehaviorType;
import arcus.cornea.subsystem.care.model.DurationType;
import arcus.cornea.utils.Listeners;
import com.iris.client.event.ListenerRegistration;
import arcus.app.R;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.error.ErrorManager;
import arcus.app.common.error.definition.TryAgainOrCancelError;
import arcus.app.common.error.listener.DismissListener;
import arcus.app.common.fragments.BaseFragment;
import arcus.app.common.image.ImageManager;
import arcus.app.common.image.Wallpaper;
import arcus.app.common.models.ListItemModel;
import arcus.app.common.popups.AMPMTimePopupWithHeader;
import arcus.app.common.popups.NumberPickerPopup;
import arcus.app.common.popups.TupleSelectorPopup;
import arcus.app.common.utils.ActivityUtils;
import arcus.app.common.utils.PreferenceCache;
import arcus.app.common.utils.PreferenceUtils;
import arcus.app.common.utils.StringUtils;
import arcus.app.common.view.Version1Button;
import arcus.app.common.view.Version1ButtonColor;
import arcus.app.dashboard.HomeFragment;
import arcus.app.device.settings.adapter.SettingsListAdapter;
import arcus.app.device.settings.core.Setting;
import arcus.app.device.settings.core.SettingsList;
import arcus.app.device.settings.style.OnClickActionSetting;
import arcus.app.subsystems.alarm.AlertFloatingFragment;
import arcus.app.subsystems.care.schedule.CareBehaviorScheduleFragment;
import arcus.app.subsystems.care.view.SortedMultiModelPopup;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import de.greenrobot.event.EventBus;

public class CareAddEditBehaviorFragment extends BaseFragment
      implements CareBehaviorController.Callback, CareBehaviorController.SaveCallback, TupleSelectorPopup.DurationTransformer {
    private static final String  ID          = "ID";
    private static final String  IS_EDIT     = "IS_EDIT";
    private static final String  DESCRIPTION = "DESCRIPTION";
    private static final String  TEMPERATURE_FORMAT = "%d\u00B0";
    private static final Integer MIN_LOW = 60;
    private static final Integer MAX_HIGH = 90;
    private static final Integer DEGREE_VARIANCE = 3;
    private static final Integer DEFAULT_LOW_TEMPERATURE = 66;
    private static final Integer DEFAULT_HIGH_TEMPERATURE = 82;
    private static final Integer MAX_LENGTH_NAME = 15;
    private static final int CURFEW_DEFAULT_START_HOUR = 21;
    private static final int CURFEW_DEFAULT_START_MIN = 0;

    private TextView subText;
    private String modelIDToEdit;
    private ListView behaviorSettingsListView;
    private Version1Button addEditButton;
    private View oopsText;
    private OnClickActionSetting timePickerPopup;

    private boolean isEditMode;

    private ListenerRegistration listener;
    private ListenerRegistration saveListener;
    private CareBehaviorModel careBehaviorModel;
    private CareBehaviorTemplateModel careBehaviorTemplateModel;
    private final List<Setting> settings = new ArrayList<>(15);
    enum TempType {
        LOW,
        HIGH
    }

    public static CareAddEditBehaviorFragment newInstance(@NonNull String id, @NonNull String description, @Nullable Boolean isEdit) {

        CareAddEditBehaviorFragment fragment = new CareAddEditBehaviorFragment();
        Bundle args = new Bundle(3);

        args.putString(ID, id);
        args.putString(DESCRIPTION, description);
        args.putBoolean(IS_EDIT, Boolean.TRUE.equals(isEdit));

        fragment.setArguments(args);
        return fragment;
    }

    @Nullable @Override public View onCreateView(
          LayoutInflater inflater,
          @Nullable ViewGroup container,
          @Nullable Bundle savedInstanceState)
    {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        if (view == null) {
            return view;
        }


        TextView addEditTitle = (TextView) view.findViewById(R.id.care_add_edit_title);
        Bundle arguments = getArguments();
        if (arguments != null) {
            isEditMode = arguments.getBoolean(IS_EDIT, false);
            modelIDToEdit = arguments.getString(ID);
        }
        if (addEditTitle != null && arguments != null) {
            addEditTitle.setText(arguments.getString(DESCRIPTION, ""));
            addEditTitle.setTextColor(isEditMode ? getResources().getColor(R.color.white) : getResources().getColor(R.color.black));
        }

        setWallpaper();

        addEditButton = (Version1Button) view.findViewById(R.id.care_add_edit_save);
        if (addEditButton != null) {
            addEditButton.setColorScheme(isEditMode ? Version1ButtonColor.WHITE : Version1ButtonColor.BLACK);
        }

        behaviorSettingsListView = (ListView) view.findViewById(R.id.care_add_edit_behavior_lv);
        behaviorSettingsListView.setHeaderDividersEnabled(false);
        behaviorSettingsListView.setFooterDividersEnabled(false);

        oopsText = view.findViewById(R.id.behavior_ineligible);

        View divider = view.findViewById(R.id.divider);
        if (divider != null && isEditMode) {
            divider.setBackground(ContextCompat.getDrawable(getActivity(), R.color.overlay_white_with_10));
        }

        subText = (TextView) view.findViewById(R.id.care_add_edit_sub_title);
        if (isEditMode) {
            subText.setTextColor(getResources().getColor(R.color.overlay_white_with_60));
        }
        return view;
    }

    private void setWallpaper() {
        Wallpaper wallpaper = Wallpaper.ofCurrentPlace();
        wallpaper = isEditMode ? wallpaper.darkened() : wallpaper.lightend();
        ImageManager.with(getActivity()).setWallpaper(wallpaper);
    }

    @Override public void onResume() {
        super.onResume();

        if (TextUtils.isEmpty(modelIDToEdit)) {
            return;
        }

        Activity activity = getActivity();
        String title = getTitle();
        if (!TextUtils.isEmpty(title) && activity != null) {
            activity.setTitle(title);
        }

        showProgressBar();
        listener = CareBehaviorController.instance().setCallback(this);
        saveListener = CareBehaviorController.instance().setSaveCallback(this);
        if (isEditMode) {
            CareBehaviorController.instance().editExistingBehaviorByID(modelIDToEdit);
        }
        else {
            CareBehaviorController.instance().addBehaviorByTemplateID(modelIDToEdit);
        }

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @Override public void onPause() {
        super.onPause();
        Listeners.clear(listener);
        Listeners.clear(saveListener);
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    @Override public boolean onBackPressed() {
        if (careBehaviorModel.hasChanges()) {
            ErrorManager
                  .in(getActivity())
                  .withDialogDismissedListener(new DismissListener() {
                      @Override public void dialogDismissedByReject() {}

                      @Override public void dialogDismissedByAccept() {
                          popIt();
                      }
                  })
                  .show(
                        new TryAgainOrCancelError(
                              R.string.care_discard_title,
                              R.string.care_discard_desc,
                              R.string.care_discard_changes
                        )
                  );
        }
        else {
            popIt();
        }
        return true;
    }

    protected void popIt() {
        CareBehaviorController.instance().hardReset();
        BackstackManager.getInstance().navigateBack();
    }

    @Nullable @Override public String getTitle() {
        return getString(R.string.care_add_behavior_activity_title);
    }

    @Override public Integer getLayoutId() {
        return R.layout.care_add_edit_behavior;
    }

    @Override public void onError(Throwable error) {
        hideProgressBar();
        checkCanSave();
        ErrorManager.in(getActivity()).showGenericBecauseOf(error);
    }

    @Override public void editTemplate(CareBehaviorModel editingModel, CareBehaviorTemplateModel templateModel) {
        careBehaviorModel = editingModel;
        careBehaviorTemplateModel = templateModel;

        setActivityName(careBehaviorModel.getName());

        if (subText != null && CareBehaviorType.PRESENCE.equals(careBehaviorModel.getBehaviorType())) {
            subText.setVisibility(View.VISIBLE);
        }

        if (careBehaviorTemplateModel.isSatisfiable()) {
            settings.clear();
            buildParticipatingDevices();
            buildNoActivityFor();
            buildTemperature(TempType.LOW);
            buildTemperature(TempType.HIGH);
            buildDaysTimes();
            buildEditName();

            if (!isEditMode) {
                careBehaviorModel.setActive(true);
                careBehaviorModel.setEnabled(true);
            }

            behaviorSettingsListView.setAdapter(new SettingsListAdapter(getActivity(), new SettingsList(settings)));

            addEditButton.setText(getText(R.string.generic_save_text));
            addEditButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showProgressBarAndDisable(addEditButton);
                    CareBehaviorController.instance().save();
                }
            });
            checkCanSave();
        }
        else {
            oopsText.setVisibility(View.VISIBLE);
            behaviorSettingsListView.setVisibility(View.GONE);
            addEditButton.setText(getText(R.string.generic_shop_text));
            addEditButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ActivityUtils.launchShopNow();
                }
            });
        }
    }

    protected void setActivityName(String name) {
        hideProgressBar();
        Activity activity = getActivity();
        if (activity != null) {
            if (!TextUtils.isEmpty(name)) {
                if (name.length() > MAX_LENGTH_NAME) {
                    name = String.format("%s...", name.substring(0, MAX_LENGTH_NAME - 1));
                }
            }
            activity.setTitle(name);
            activity.invalidateOptionsMenu();
        }
    }

    @Override public void unsupportedTemplate() {
        hideProgressBar();
    }

    protected void buildParticipatingDevices() {
        final OnClickActionSetting actionSetting = new OnClickActionSetting(
              String.valueOf(careBehaviorTemplateModel.getParticipatingDevicesTitle()).toUpperCase(),
              careBehaviorTemplateModel.getParticipatingDevicesDescription(),
              selectedDevicesAbstract()
        );
        actionSetting.setUseLightColorScheme(isEditMode);
        actionSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (CareBehaviorType.OPEN_COUNT.equals(careBehaviorModel.getBehaviorType())) {
                    String id = isEditMode ? careBehaviorModel.getBehaviorID() : careBehaviorModel.getTemplateID();
                    BackstackManager.getInstance().navigateToFragment(
                          CareSelectOpenDevicesFragment.newInstance(id, isEditMode), true
                    );
                }
                else {
                    SortedMultiModelPopup popup = SortedMultiModelPopup.newInstance(
                          new LinkedList<>(careBehaviorTemplateModel.getAvailableDevices()), null,
                          new LinkedList<>(careBehaviorModel.getSelectedDevices()), true
                    );
                    popup.setCallback(new SortedMultiModelPopup.Callback() {
                        @Override
                        public void itemSelectedAddress(ListItemModel itemModel) {
                            updateSelectedDevices(itemModel.getAddress(), itemModel.isChecked());
                            actionSetting.setSelectionAbstract(selectedDevicesAbstract());
                            checkCanSave();
                        }
                    });
                    String stackName = popup.getClass().getCanonicalName();
                    BackstackManager.getInstance().navigateToFloatingFragment(popup, stackName, true);
                }
            }
        });

        settings.add(actionSetting);
    }

    protected void buildNoActivityFor() {
        String title = careBehaviorTemplateModel.getInactivityTitle();
        String description = careBehaviorTemplateModel.getInactivityDescription();

        if (TextUtils.isEmpty(title)) {
            return;
        }

        final Integer duration = careBehaviorModel.getDurationSecs();
        final OnClickActionSetting actionSetting = new OnClickActionSetting(
              title.toUpperCase(),
              description,
              getSelectionAbstract()
        );
        actionSetting.setUseLightColorScheme(isEditMode);
        actionSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] values = careBehaviorTemplateModel.getDurationUnitValuesArray();
                if (values.length <= 1) {
                    return;
                }

                final Integer duration = careBehaviorModel.getDurationSecs();
                TupleSelectorPopup popup = TupleSelectorPopup.newInstance(
                        getTupleListValue(values),
                        getString(R.string.care_choose_duration),
                        duration == null ? null : String.valueOf(duration),
                        CareAddEditBehaviorFragment.this,
                        true
                );
                popup.setCallback(new TupleSelectorPopup.Callback() {
                    @Override
                    public void selectedItem(StringPair selected) {
                        careBehaviorModel.setDurationSecs(Integer.valueOf(selected.getKey()));
                        actionSetting.setSelectionAbstract(getSelectionAbstract());
                        checkCanSave();
                    }
                });
                BackstackManager.getInstance().navigateToFloatingFragment(popup, popup.getClass().getSimpleName(), true);
            }
        });

        settings.add(actionSetting);
    }

    protected void buildTimePicker() {
        timePickerPopup = new OnClickActionSetting(
              String.valueOf(careBehaviorTemplateModel.getTimeWindowTitle()).toUpperCase(),
              careBehaviorTemplateModel.getTimeWindowDescription(),
              getTimeAbstract(careBehaviorModel.getPresenceTime())
        );

        timePickerPopup.setUseLightColorScheme(isEditMode);
        timePickerPopup.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                AMPMTimePopupWithHeader popup = getTimePopup();
                String stackName = popup.getClass().getCanonicalName();
                BackstackManager.getInstance().navigateToFloatingFragment(popup, stackName, true);

                checkCanSave();
            }
        });

        settings.add(timePickerPopup);
    }

    protected void buildTemperature(final TempType tempType) {
        String title;
        Integer value;
        if (TempType.LOW.equals(tempType)) {
            title = careBehaviorTemplateModel.getLowTempTitle();
            value = careBehaviorModel.getLowTemp();
        }
        else {
            title = careBehaviorTemplateModel.getHighTempTitle();
            value = careBehaviorModel.getHighTemp();
        }

        if (TextUtils.isEmpty(title)) {
            return;
        }

        String abstractText = null;
        if (value != null) {
            abstractText = String.format(TEMPERATURE_FORMAT, value);
        }
        final OnClickActionSetting actionSetting = new OnClickActionSetting(title.toUpperCase(), null, abstractText);

        actionSetting.setUseLightColorScheme(isEditMode);
        actionSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int[] minMaxSelected = getMinMaxSelectedTemperatures(tempType);
                NumberPickerPopup npp = NumberPickerPopup.newInstance(
                      NumberPickerPopup.NumberPickerType.MIN_MAX,
                      minMaxSelected[0],
                      minMaxSelected[1],
                      minMaxSelected[2]
                );

                npp.setOnValueChangedListener(new NumberPickerPopup.OnValueChangedListener() {
                    @Override
                    public void onValueChanged(int value) {
                        if (TempType.LOW.equals(tempType)) {
                            careBehaviorModel.setLowTemp(value);
                        }
                        else {
                            careBehaviorModel.setHighTemp(value);
                        }
                        actionSetting.setSelectionAbstract(String.format(TEMPERATURE_FORMAT, value));
                        checkCanSave();
                    }
                });
                BackstackManager.getInstance().navigateToFloatingFragment(npp, npp.getClass().getSimpleName(), true);
            }
        });

        settings.add(actionSetting);
    }

    protected void buildDaysTimes() {
        if (!careBehaviorTemplateModel.supportsTimeWindows()) {
            return;
        }

        if (careBehaviorTemplateModel.isNoDurationType()) {
            buildTimePicker();
            return;
        }

        final OnClickActionSetting actionSetting = new OnClickActionSetting(
              String.valueOf(careBehaviorTemplateModel.getTimeWindowTitle()).toUpperCase(),
              careBehaviorTemplateModel.getTimeWindowDescription(),
              careBehaviorModel.hasScheduleEvents() ? R.drawable.schedule : null
        );
        actionSetting.setUseLightColorScheme(isEditMode);
        actionSetting.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                String id = isEditMode ? careBehaviorModel.getBehaviorID() : careBehaviorModel.getTemplateID();
                BackstackManager.getInstance().navigateToFragment(
                      CareBehaviorScheduleFragment.newInstance(id, isEditMode, null), true
                );
            }
        });
        settings.add(actionSetting);
    }

    protected void buildEditName() {
        OnClickActionSetting actionSetting = new OnClickActionSetting(
              careBehaviorTemplateModel.getEditNameLabel(),
              null,
              careBehaviorModel.getName()
        );

        actionSetting.setUseLightColorScheme(isEditMode);
        actionSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String id = isEditMode ? careBehaviorModel.getBehaviorID() : careBehaviorModel.getTemplateID();
                BackstackManager.getInstance().navigateToFragment(
                      CareBehaviorNameEditorFragment.newInstance(id, isEditMode), true
                );
            }
        });

        settings.add(actionSetting);
    }


    protected int[] getMinMaxSelectedTemperatures(TempType tempType) {
        int min, max, selected;
        Integer high = careBehaviorModel.getHighTemp();
        Integer low = careBehaviorModel.getLowTemp();
        int[] minMaxLow  = careBehaviorTemplateModel.getLowTemperatureUnitValuesArray();
        int[] minMaxHigh = careBehaviorTemplateModel.getHighTemperatureUnitValuesArray();
        if (TempType.LOW.equals(tempType)) { // User is selecting the low temp
            selected = low == null ? DEFAULT_LOW_TEMPERATURE : low; // Get what the user picked previously, or default value.
            // User can to to the max bound - DEGREE_VARIANCE if nothing was selected for the high temp already,
            // or highTemp - DEGREE_VARIANCE if it was
            // User should be able to go to the lowest bound;
            min = minMaxLow.length == 2 ? minMaxLow[0] : MIN_LOW;
            if (high == null) {
                max = (minMaxHigh.length == 2 ? minMaxHigh[1] : MAX_HIGH) - DEGREE_VARIANCE;
            }
            else {
                max = high - DEGREE_VARIANCE;
            }
        }
        else { // User is selecting the high temp
            selected = high == null ? DEFAULT_HIGH_TEMPERATURE : high;
            // User should be able to go to the highest bound;
            max = minMaxHigh.length == 2 ? minMaxHigh[1] : MAX_HIGH;

            // User can to to the min bound + DEGREE_VARIANCE if nothing was selected for the low temp
            // already, or lowTemp + DEGREE_VARIANCE if it was
            if (low == null) {
                min = (minMaxLow.length == 2 ? minMaxLow[0] : MIN_LOW) + DEGREE_VARIANCE;
            }
            else {
                min = low + DEGREE_VARIANCE;
            }
        }

        return new int[] { min, max, selected };
    }

    protected @NonNull String getSelectionAbstract() {
        Integer durationSecs = careBehaviorModel.getDurationSecs();
        if (durationSecs == null) {
            return "";
        }

        StringPair duration = transformDuration(new StringPair(String.valueOf(durationSecs), ""));
        return getResources().getString(R.string.care_behavior_duration_abstract, duration.getKey(), duration.getValue());
    }

    protected String selectedDevicesAbstract() {
        if (CareBehaviorType.OPEN_COUNT.equals(careBehaviorModel.getBehaviorType())) {
            int size = careBehaviorModel.getOpenCounts().size();
            if (size > 0) {
                return String.format("%d", size);
            }
            else {
                return StringUtils.EMPTY_STRING;
            }
        }
        else {
            Set<String> selected = careBehaviorModel.getSelectedDevices();
            return selected.isEmpty() ? StringUtils.EMPTY_STRING : String.format("%d", selected.size());
        }
    }

    protected void updateSelectedDevices(String address, boolean add) {
        Set<String> selected = careBehaviorModel.getSelectedDevices();
        selected.remove(address);
        if (add) {
            selected.add(address);
        }
    }

    protected List<StringPair> getTupleListValue(String[] values) {
        List<StringPair> tuple = new ArrayList<>(values.length + 1);
        DurationType type = careBehaviorTemplateModel.getDurationType();

        for (String displayValue : values) {
            Integer secondValue = careBehaviorModel.convertToPlatformSecondsValue(displayValue, type);
            if (secondValue != null) {
                tuple.add(new StringPair(String.valueOf(secondValue), displayValue));
            }
        }

        return tuple;
    }

    protected void checkCanSave() {
        addEditButton.setEnabled(careBehaviorModel.canSave());
    }

    @Override public void saveSuccessful(String behaviorID) {
        hideProgressBar();

        if (isEditMode) {
            BackstackManager.getInstance().rewindToFragment(HomeFragment.newInstance());
            return;
        }

        if (!PreferenceCache.getInstance().getBoolean(PreferenceUtils.CARE_BEHAVIORS_DONT_SHOW_AGAIN, false)) {
            CareManagePopupNotification popup = CareManagePopupNotification.newInstance();
            BackstackManager.getInstance().navigateToFloatingFragment(popup, popup.getClass().getName(), true);

        }
        else {
            BackstackManager.getInstance().rewindToFragment(HomeFragment.newInstance());
        }
    }

    @Override public void invalidName() {
        hideProgressBar();
        checkCanSave();

        AlertFloatingFragment alert = AlertFloatingFragment.newInstance(
              getString(R.string.care_name_in_use_title),
              getString(R.string.care_name_in_use_desc),
              "", "", null
        );

        BackstackManager.getInstance().navigateToFloatingFragment(alert, alert.getClass().getCanonicalName(), true);
    }

    @Override public void invalidSchedule() {
        hideProgressBar();
        checkCanSave();

        AlertFloatingFragment alert = AlertFloatingFragment.newInstance(
              getString(R.string.care_schedule_error_title),
              getString(R.string.care_schedule_error_desc),
              "", "", null
        );

        BackstackManager.getInstance().navigateToFloatingFragment(alert, alert.getClass().getCanonicalName(), true);
    }

    protected AMPMTimePopupWithHeader getTimePopup() {
        if (TextUtils.isEmpty(careBehaviorModel.getPresenceTime())) {
            return AMPMTimePopupWithHeader.newInstanceAsTimeOnly(CURFEW_DEFAULT_START_HOUR, CURFEW_DEFAULT_START_MIN);
        }

        String currentSelection = careBehaviorModel.getPresenceTime();
        if (TextUtils.isEmpty(currentSelection)) {
            return AMPMTimePopupWithHeader.newInstanceAsTimeOnly(CURFEW_DEFAULT_START_HOUR, CURFEW_DEFAULT_START_MIN);
        }

        String[] selection = currentSelection.split(":");
        try {
            int hour = Integer.parseInt(selection[0]);
            int minute = Integer.parseInt(selection[1]);
            return AMPMTimePopupWithHeader.newInstanceAsTimeOnly(hour, minute);
        }
        catch (Exception ignored) {
            // 8PM default selection
            return AMPMTimePopupWithHeader.newInstanceAsTimeOnly(CURFEW_DEFAULT_START_HOUR, CURFEW_DEFAULT_START_MIN);
        }
    }

    // Is used for "NODURATION" based rules - Currently only CURFEW
    public void onEvent(TimeSelectedEvent event) {
        if (event == null || timePickerPopup == null) {
            return;
        }

        String time = event.getAsTime();
        careBehaviorModel.setPresenceTime(time);
        timePickerPopup.setSelectionAbstract(getTimeAbstract(time));
        checkCanSave();
    }

    protected @Nullable String getTimeAbstract(@Nullable String time) {
        if (TextUtils.isEmpty(time)) {
            return null;
        }

        try {
            SimpleDateFormat parseFormat = new SimpleDateFormat("hh:mm:ss", Locale.getDefault());
            SimpleDateFormat renderFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
            return renderFormat.format(parseFormat.parse(time));
        }
        catch (Exception ex) {
            logger.error("Could not parse time back.", ex);
            return null;
        }
    }

    public StringPair transformDuration(StringPair value) {

        final int SECS_PER_MIN = 60;
        final int SECS_PER_HOUR = SECS_PER_MIN * 60;
        final int SECS_PER_DAY = SECS_PER_HOUR * 24;

        try {
            int durationSec = Integer.valueOf(value.getKey());
            if (durationSec > SECS_PER_DAY) {
                return new StringPair(getString(R.string.duration_days), String.valueOf(durationSec / SECS_PER_DAY));
            } else if (durationSec == SECS_PER_DAY) {
                return new StringPair(getString(R.string.duration_day), String.valueOf(durationSec / SECS_PER_DAY));
            } else if (durationSec > SECS_PER_HOUR) {
                return new StringPair(getString(R.string.duration_hours), String.valueOf(durationSec / SECS_PER_HOUR));
            } else if (durationSec == SECS_PER_HOUR) {
                return new StringPair(getString(R.string.duration_hour), String.valueOf(durationSec / SECS_PER_HOUR));
            } else if (durationSec > SECS_PER_MIN) {
                return new StringPair(getString(R.string.duration_mins), String.valueOf(durationSec / SECS_PER_MIN));
            } else if (durationSec == SECS_PER_MIN) {
                return new StringPair(getString(R.string.duration_min), String.valueOf(durationSec / SECS_PER_MIN));
            } else if (durationSec > 1) {
                return new StringPair(getString(R.string.duration_secs), String.valueOf(durationSec));
            } else {
                return new StringPair(getString(R.string.duration_sec), String.valueOf(durationSec));
            }
        } catch (NumberFormatException e) {
            return value;
        }
    }
}

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
package arcus.app.subsystems.climate;

import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dexafree.materialList.model.Card;
import com.dexafree.materialList.view.MaterialListView;
import arcus.cornea.CorneaClientFactory;
import arcus.cornea.device.thermostat.ThermostatMode;
import arcus.cornea.error.ErrorModel;
import arcus.cornea.subsystem.climate.ScheduleEditController;
import arcus.cornea.subsystem.climate.model.ScheduledSetPoint;
import arcus.cornea.utils.DateUtils;
import arcus.cornea.utils.DayOfWeek;
import arcus.cornea.utils.TimeOfDay;
import com.iris.client.event.ListenerRegistration;
import arcus.app.R;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.cards.CenteredTextCard;
import arcus.app.common.cards.PopupCard;
import arcus.app.common.controller.AbstractCardController;
import arcus.app.common.error.ErrorManager;
import arcus.app.common.fragments.BaseFragment;
import arcus.app.common.image.ImageManager;
import arcus.app.common.image.Wallpaper;
import arcus.app.common.popups.ButtonListPopup;
import arcus.app.common.popups.DayOfTheWeekPopup;
import arcus.app.common.popups.ArcusFloatingFragment;
import arcus.app.common.popups.NumberPickerPopup;
import arcus.app.common.popups.SunriseSunsetPicker;
import arcus.app.common.view.Version1Button;
import arcus.app.common.view.Version1ButtonColor;
import arcus.app.subsystems.alarm.AlertFloatingFragment;

import java.io.Serializable;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public class ScheduleAddEditFragment extends BaseFragment implements ScheduleEditController.Callback, AbstractCardController.Callback {
    private static final String DAY_OF_WEEK = "DAY_OF_WEEK";
    private static final String SET_POINT = "SET_POINT";
    private static final String SCHEDULER = "SCHEDULER";
    private static final String THERMOSTAT_MODE = "THERMOSTAT_MODE";
    private static final String EDIT_SELECTED = "EDIT_SELECTED";
    private static final String EDIT_ALL = "EDIT_ALL";
    private ScheduleEditController controller;
    private MaterialListView listView;
    private Version1Button saveButton;
    private Version1Button removeButton;
    private ListenerRegistration callbackRegistration;
    @Nullable
    private ScheduledSetPoint setPoint;
    @NonNull
    private Mode mode = Mode.ADD;
    private Map<String, String> editChoices;
    private String placeID;

    enum Mode {
        ADD,
        EDIT
    }

    @NonNull
    public static ScheduleAddEditFragment newInstance(DayOfWeek dayOfWeek, String schedulerAddress, ScheduledSetPoint scheduledSetPoint) {
        ScheduleAddEditFragment fragment = new ScheduleAddEditFragment();

        Bundle bundle = new Bundle(3);
        bundle.putSerializable(DAY_OF_WEEK, dayOfWeek);
        bundle.putSerializable(SET_POINT, scheduledSetPoint);
        bundle.putString(SCHEDULER, schedulerAddress);
        fragment.setArguments(bundle);

        return fragment;
    }

    @NonNull
    public static ScheduleAddEditFragment newInstance(DayOfWeek dayOfWeek, String schedulerAddress, ThermostatMode thermostatMode) {
        ScheduleAddEditFragment fragment = new ScheduleAddEditFragment();

        Bundle bundle = new Bundle(3);
        bundle.putSerializable(DAY_OF_WEEK, dayOfWeek);
        bundle.putSerializable(THERMOSTAT_MODE, thermostatMode);
        bundle.putString(SCHEDULER, schedulerAddress);
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public String getTitle() {
        return getString(R.string.climate_add_event_title);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        setHasOptionsMenu(true);

        listView = (MaterialListView) view.findViewById(R.id.material_listview);
        saveButton = (Version1Button) view.findViewById(R.id.save_button);
        removeButton = (Version1Button) view.findViewById(R.id.remove_button);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        placeID = CorneaClientFactory.getClient().getActivePlace().toString();
        editChoices = new HashMap<>();
        editChoices.put(getString(R.string.climate_edit_all_days), EDIT_ALL);
        editChoices.put(getString(R.string.climate_edit_selected_day), EDIT_SELECTED);

        if (controller == null) {
            controller = ScheduleEditController.instance();
        }

        setPoint = getScheduleSetPoint();
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                controller.save();
            }
        });

        if (setPoint != null) {
            mode = Mode.EDIT;
            saveButton.setColorScheme(Version1ButtonColor.WHITE);
            removeButton.setColorScheme(Version1ButtonColor.MAGENTA);
            removeButton.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    controller.delete();
                }
            });
            if (getDayOfWeek() != null) {
                getActivity().setTitle(getDayOfWeek().name().toUpperCase());
            }

            callbackRegistration = controller.edit(getDayOfWeek(), setPoint, getSchedulerAddress(), this);
        }
        else {
            mode = Mode.ADD;
            saveButton.setColorScheme(Version1ButtonColor.BLACK);
            removeButton.setVisibility(View.GONE);
            getActivity().setTitle(getTitle());
            ImageManager.with(getActivity()).setWallpaper(Wallpaper.ofPlace(placeID).lightend());

            callbackRegistration = controller.add(getDayOfWeek(), getThermostatMode(), getSchedulerAddress(), this);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        callbackRegistration.remove();
    }

    @Override
    public void updateCard(Card c) {
        listView.clear();

        addStartTimeCard();
        if (setPoint != null && setPoint.isAutoSetPoint()) {
            addHighTemperatureCard();
            addLowTemperatureCard();
        } else {
            addSingleTemperatureCard();
        }

        if (setPoint.isRepeating()) {
            addRepeatingCard();
        } else {
            addNonRepeatingCard();
        }
    }

    @Nullable
    public DayOfWeek getDayOfWeek() {
        return (DayOfWeek) getArguments().getSerializable(DAY_OF_WEEK);
    }

    @Nullable
    public String getSchedulerAddress() {
        return getArguments().getString(SCHEDULER);
    }

    public boolean inAddMode() {
        return Mode.ADD.equals(mode);
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_climate_add_edit;
    }

    @Nullable
    public ScheduledSetPoint getScheduleSetPoint() {
        Serializable object = getArguments().getSerializable(SET_POINT);
        if (object == null) {
            return null;
        }

        return (ScheduledSetPoint) object;
    }

    @Nullable
    public ThermostatMode getThermostatMode() {
        Serializable object = getArguments().getSerializable(THERMOSTAT_MODE);
        if (object == null) {
            return null;
        }

        return (ThermostatMode) object;
    }

    @Override
    public void showAdd(DayOfWeek day, @NonNull ScheduledSetPoint setPoint) {
        this.setPoint = setPoint;
        if (setPoint.getMode() == null) {
            setPoint.setMode(getThermostatMode());
        }
        updateCard(null);
    }

    @Override
    public void showEdit(DayOfWeek day, ScheduledSetPoint setPoint) {
        this.setPoint = setPoint;
        updateCard(null);
    }

    @Override
    public void showError(@NonNull ErrorModel error) {
        hideProgressBarAndEnable(saveButton, removeButton);
        ErrorManager.in(getActivity()).showGenericBecauseOf(new RuntimeException(error.getMessage()));
    }

    @Override
    public void showSaving() {
        hideProgressBarAndEnable(saveButton, removeButton);
        showProgressBarAndDisable(saveButton, removeButton);
    }

    @Override
    public void promptEditWhichDay() {
        final ButtonListPopup popup = ButtonListPopup.newInstance(
              editChoices,
              R.string.climate_edit_event_title,
              R.string.climate_edit_event_description);
        popup.setCallback(new ButtonListPopup.Callback() {
            @Override public void buttonSelected(String buttonKeyValue) {
                if (EDIT_ALL.equals(buttonKeyValue)) {
                    controller.saveAllDays();
                }
                else {
                    controller.saveSelectedDay();
                }
                BackstackManager.getInstance().navigateBack();
            }
        });

        showPopup(popup);
    }

    @Override
    public void promptDeleteWhichDay() {
        AlertFloatingFragment popup = AlertFloatingFragment.newInstance(
              getString(R.string.climate_edit_event_error_title),
              getString(R.string.climate_edit_event_error_description),
              getString(R.string.climate_edit_selected_day),
              getString(R.string.climate_edit_all_days),
              new AlertFloatingFragment.AlertButtonCallback() {
                    @Override public boolean topAlertButtonClicked() {
                        controller.deleteSelectedDay();
                        return true;
                    }

                    @Override public boolean bottomAlertButtonClicked() {
                        controller.deleteAllDays();
                        return true;
                    }
                }
        );

        showPopup(popup);
    }

    @Override
    public boolean onBackPressed() {
        ImageManager.with(getActivity()).setWallpaper(Wallpaper.ofPlace(placeID).darkened());
        return super.onBackPressed();
    }

    @Override
    public void done() {
        hideProgressBar();
        ImageManager.with(getActivity()).setWallpaper(Wallpaper.ofPlace(placeID).darkened());
        BackstackManager.getInstance().navigateBack();
    }


    protected void addStartTimeCard() {
        PopupCard timeCard = new PopupCard(getActivity());
        timeCard.setTitle(getString(R.string.climate_schedule_start_time));
        if (setPoint != null && setPoint.getTimeOfDay() == null) {
            setPoint.setTimeOfDay(new TimeOfDay(6));
        }

        if (setPoint != null) {
            timeCard.setRightText(DateUtils.format(setPoint.getTimeOfDay(), false));
        }
        timeCard.showDivider();
        timeCard.setDarkColorScheme(Mode.ADD.equals(mode));
        timeCard.setClickListener(new PopupCard.ClickListener() {
            @Override public void cardClicked(View view) {
                SunriseSunsetPicker popup = SunriseSunsetPicker.newInstance(setPoint.getTimeOfDay());
                popup.setCallback(new SunriseSunsetPicker.Callback() {
                    @Override public void selection(TimeOfDay selected) {
                        controller.setStartTime(selected);
                    }
                });
                showPopup(popup);
            }
        });

        listView.add(timeCard);
    }

    protected void addHighTemperatureCard() {
        PopupCard highTemperatureCard = new PopupCard(getActivity());
        highTemperatureCard.setTitle(getString(R.string.climate_cool_to));
        highTemperatureCard.setDescription(getString(R.string.auto_mode_high_schedule_description));
        if (setPoint != null && setPoint.getCoolSetPoint() == 0) {
            setPoint.setCoolSetPoint(78);
        }
        if (setPoint != null) {
            highTemperatureCard.setRightText(setPoint.getCoolSetPoint() + getString(R.string.degree_symbol));
        }
        highTemperatureCard.showDivider();
        highTemperatureCard.setDarkColorScheme(inAddMode());
        highTemperatureCard.setClickListener(new PopupCard.ClickListener() {
            @Override public void cardClicked(View view) {
                setTemperaturePoint(NumberPickerPopup.NumberPickerType.HIGH);
            }
        });

        listView.add(highTemperatureCard);
    }

    protected void addLowTemperatureCard() {
        PopupCard lowTemperatureCard = new PopupCard(getActivity());
        lowTemperatureCard.setTitle(getString(R.string.climate_heat_to));
        lowTemperatureCard.setDescription(getString(R.string.auto_mode_low_schedule_description));
        if (setPoint != null && setPoint.getHeatSetPoint() == 0) {
            setPoint.setHeatSetPoint(68);
        }
        if (setPoint != null) {
            lowTemperatureCard.setRightText(setPoint.getHeatSetPoint() + getString(R.string.degree_symbol));
        }
        lowTemperatureCard.showDivider();
        lowTemperatureCard.setDarkColorScheme(inAddMode());
        lowTemperatureCard.setClickListener(new PopupCard.ClickListener() {
            @Override public void cardClicked(View view) {
                setTemperaturePoint(NumberPickerPopup.NumberPickerType.LOW);
            }
        });

        listView.add(lowTemperatureCard);
    }

    protected void addSingleTemperatureCard() {
        PopupCard temperatureCard = new PopupCard(getActivity());
        temperatureCard.setTitle(getString(R.string.climate_more_temperature_title));
        if (setPoint != null && setPoint.getCurrentSetPoint() == 0) {
            setPoint.setCurrentSetPoint(72);
        }
        if (setPoint != null) {
            temperatureCard.setRightText(setPoint.getCurrentSetPoint() + getString(R.string.degree_symbol));
        }
        temperatureCard.showDivider();
        temperatureCard.setDarkColorScheme(inAddMode());
        temperatureCard.setClickListener(new PopupCard.ClickListener() {
            @Override public void cardClicked(View view) {
                setTemperaturePoint(NumberPickerPopup.NumberPickerType.MIN_MAX);
            }
        });

        listView.add(temperatureCard);
    }

    protected void addRepeatingCard() {
        PopupCard repeatCard = new PopupCard(getActivity());
        repeatCard.setTitle(getString(R.string.climate_schedule_repeat_on));
        if (setPoint != null) {
            repeatCard.setRightText(setPoint.getRepetitionText());
        }
        repeatCard.setDarkColorScheme(inAddMode());
        repeatCard.setClickListener(new PopupCard.ClickListener() {
            @Override public void cardClicked(View view) {
                selectDayOfWeek(EnumSet.copyOf(setPoint.getRepeatsOn()));
            }
        });

        listView.add(repeatCard);
    }

    protected void addNonRepeatingCard() {
        CenteredTextCard notInitiallyRepeatingCard = new CenteredTextCard(getActivity());
        notInitiallyRepeatingCard.setTitle(getString(R.string.climate_schedule_repeat_text));
        notInitiallyRepeatingCard.setDescription(getString(R.string.climate_schedule_repeat));
        notInitiallyRepeatingCard.useTransparentBackground(true);
        notInitiallyRepeatingCard.setDescriptionBackground(inAddMode() ? R.drawable.outline_button_style_black : R.drawable.outline_button_style);
        notInitiallyRepeatingCard.setDescriptionColor(inAddMode() ? Color.BLACK : Color.WHITE);
        notInitiallyRepeatingCard.setTitleColor(inAddMode() ? Color.BLACK : Color.WHITE);
        notInitiallyRepeatingCard.setCallaback(new CenteredTextCard.OnClickCallaback() {
            @Override public void onTitleClicked() {}

            @Override public void onDescriptionClicked() {
                selectDayOfWeek(EnumSet.noneOf(DayOfWeek.class));
            }
        });

        listView.add(notInitiallyRepeatingCard);
    }

    public void setTemperaturePoint(@NonNull final NumberPickerPopup.NumberPickerType type) {
        int currentValue;
        switch (type) {
            case LOW:
                currentValue = setPoint != null ? setPoint.getHeatSetPoint() : 0;
                break;
            case HIGH:
                currentValue = setPoint != null ? setPoint.getCoolSetPoint() : 0;
                break;
            default:
                currentValue = setPoint != null ? setPoint.getCurrentSetPoint() : 0;
                break;
        }

        NumberPickerPopup popup = NumberPickerPopup.newInstance(type, currentValue);
        popup.setCoolSetPoint(setPoint.getCoolSetPoint());
        popup.setHeatSetPoint(setPoint.getHeatSetPoint());
        popup.setOnValueChangedListener(new NumberPickerPopup.OnValueChangedListener() {
            @Override
            public void onValueChanged(int value) {
                switch (type) {
                    case LOW:
                        controller.setHeatPoint(value);
                        break;
                    case HIGH:
                        controller.setCoolPoint(value);
                        break;
                    default:
                        controller.setCurrentSetPoint(value);
                        break;
                }
            }
        });
        showPopup(popup);
    }

    public void selectDayOfWeek(EnumSet<DayOfWeek> selectedDays) {
        DayOfTheWeekPopup popup = DayOfTheWeekPopup.newInstance(selectedDays);
        popup.setCallback(new DayOfTheWeekPopup.Callback() {
            @Override public void selectedItems(EnumSet<DayOfWeek> dayOfWeek) {
                controller.enableRepetitions(dayOfWeek);
                updateCard(null);
            }
        });
        showPopup(popup);
    }

    public <T extends ArcusFloatingFragment> void showPopup(@NonNull T popup) {
        BackstackManager.getInstance().navigateToFloatingFragment(popup, popup.getClass().getSimpleName(), true);
    }
}

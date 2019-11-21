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

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.dexafree.materialList.controller.RecyclerItemClickListener;
import com.dexafree.materialList.model.Card;
import com.dexafree.materialList.model.CardItemView;
import com.dexafree.materialList.view.MaterialListView;
import arcus.cornea.device.thermostat.ThermostatMode;
import arcus.cornea.error.ErrorModel;
import arcus.cornea.subsystem.climate.ScheduleViewController;
import arcus.cornea.subsystem.climate.model.ScheduleModel;
import arcus.cornea.subsystem.climate.model.ScheduledDay;
import arcus.cornea.subsystem.climate.model.ScheduledSetPoint;
import arcus.cornea.utils.DayOfWeek;
import com.iris.client.event.ListenerRegistration;
import arcus.app.R;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.controller.AbstractCardController;
import arcus.app.common.error.ErrorManager;
import arcus.app.common.error.type.DeviceErrorType;
import arcus.app.common.fragments.BaseFragment;
import arcus.app.common.popups.ArcusFloatingFragment;
import arcus.app.common.popups.MultiButtonPopup;
import arcus.app.common.view.Version1Button;
import arcus.app.common.view.Version1ButtonColor;
import arcus.app.subsystems.climate.cards.ThermostatScheduleCard;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;


public class ScheduleViewFragment extends BaseFragment implements ScheduleViewController.Callback,AbstractCardController.Callback,MultiButtonPopup.OnButtonClickedListener{

    private static final String DEVICE_NAME_KEY = "DEVICE NAME KEY";
    private static final String DEVICE_ADDRESS_KEY = "DEVICE ID KEY";
    private ListenerRegistration mListener;
    private ScheduleViewController mController;
    private String mDeviceName;
    private String mDeviceAddress;

    private TextView modeText;
    private TextView descriptionText;
    private TextView nextEventLabel;
    private Version1Button addEventBtn;
    private RadioGroup mRadioGroup;
    private RadioButton mRadioButtonMon, mRadioButtonTue,mRadioButtonWed,mRadioButtonThu,
            mRadioButtonFri, mRadioButtonSat, mRadioButtonSun;
    private MaterialListView materialListView;
    private View scheduleOff;
    private View scheduleView;

    private ScheduleModel mScheduleModel;
    private ScheduledDay mScheduledDay;

    private DayOfWeek selectedDay = DayOfWeek.MONDAY;

    @NonNull
    public static ScheduleViewFragment newInstance(String name, String deviceId) {
        ScheduleViewFragment fragment = new ScheduleViewFragment();
        Bundle bundle = new Bundle();
        bundle.putString(DEVICE_NAME_KEY, name);
        bundle.putString(DEVICE_ADDRESS_KEY,deviceId);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Bundle arguments = getArguments();
        if (arguments != null) {
            mDeviceName = arguments.getString(DEVICE_NAME_KEY, "");
            mDeviceAddress = arguments.getString(DEVICE_ADDRESS_KEY,"");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mController == null) {
            mController = ScheduleViewController.instance();
        }
        View view = super.onCreateView(inflater, container, savedInstanceState);

        addEventBtn = (Version1Button) view.findViewById(R.id.fragment_climate_schedule_view_add_event_btn);
        addEventBtn.setColorScheme(Version1ButtonColor.WHITE);
        addEventBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mScheduledDay == null || mScheduleModel == null) {
                    return;
                }

                BackstackManager.getInstance().navigateToFragment(ScheduleAddEditFragment.newInstance(mScheduledDay.getDayOfWeek(), mScheduleModel.getSchedulerAddress(), mScheduleModel.getMode()), true);
            }
        });

        modeText = (TextView) view.findViewById(R.id.fragment_climate_schedule_view_mode_text);
        modeText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadMultiButtonPopup();
            }
        });
        descriptionText = (TextView) view.findViewById(R.id.fragment_climate_schedule_view_des);
        nextEventLabel = (TextView) view.findViewById(R.id.fragment_climate_schedule_view_label);

        mRadioGroup = (RadioGroup) view.findViewById(R.id.day_of_week_radio_group);
        mRadioGroup.clearCheck();
        mRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.radio_monday:
                        mController.selectDay(DayOfWeek.MONDAY);
                        break;
                    case R.id.radio_tuesday:
                        mController.selectDay(DayOfWeek.TUESDAY);
                        break;
                    case R.id.radio_wednesday:
                        mController.selectDay(DayOfWeek.WEDNESDAY);
                        break;
                    case R.id.radio_thursday:
                        mController.selectDay(DayOfWeek.THURSDAY);
                        break;
                    case R.id.radio_friday:
                        mController.selectDay(DayOfWeek.FRIDAY);
                        break;
                    case R.id.radio_saturday:
                        mController.selectDay(DayOfWeek.SATURDAY);
                        break;
                    case R.id.radio_sunday:
                        mController.selectDay(DayOfWeek.SUNDAY);
                        break;
                }
            }
        });

        mRadioButtonMon = (RadioButton) view.findViewById(R.id.radio_monday);
        mRadioButtonTue = (RadioButton) view.findViewById(R.id.radio_tuesday);
        mRadioButtonWed = (RadioButton) view.findViewById(R.id.radio_wednesday);
        mRadioButtonThu = (RadioButton) view.findViewById(R.id.radio_thursday);
        mRadioButtonFri = (RadioButton) view.findViewById(R.id.radio_friday);
        mRadioButtonSat = (RadioButton) view.findViewById(R.id.radio_saturday);
        mRadioButtonSun = (RadioButton) view.findViewById(R.id.radio_sunday);

        materialListView = (MaterialListView) view.findViewById(R.id.material_listview);
        materialListView.addOnItemTouchListener(new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(CardItemView cardItemView, int i) {
                BackstackManager.getInstance().navigateToFragment(ScheduleAddEditFragment.newInstance(mScheduledDay.getDayOfWeek(), mScheduleModel.getSchedulerAddress(), mScheduledDay.getSetPoints().get(i)), true);
            }

            @Override
            public void onItemLongClick(CardItemView cardItemView, int i) {
                // No-Op
            }
        });

        scheduleOff = view.findViewById(R.id.fragment_climate_schedule_off);
        scheduleOff.setVisibility(View.GONE);
        scheduleView = view.findViewById(R.id.fragment_climate_schedule_view);
        scheduleView.setVisibility(View.GONE);

        return view;
    }

    public <T extends ArcusFloatingFragment> void showPopup(@NonNull T popup) {
        BackstackManager.getInstance().navigateToFloatingFragment(popup, popup.getClass().getSimpleName(), true);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mController == null) {
            mController = ScheduleViewController.instance();
        }

        getActivity().setTitle(mDeviceName);
        mListener = mController.select(mDeviceAddress,this,selectedDay);
    }

    @Override
    public void onPause() {
        super.onPause();
        mListener.remove();
        if(mScheduledDay !=null) {
            selectedDay = mScheduledDay.getDayOfWeek();
        }
    }

    @NonNull
    @Override
    public String getTitle() {
        return "";
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_climate_schedule_view;
    }

    @Override
    public void updateCard(Card c) {
        materialListView.clear();

        if(mScheduledDay !=null){
            for(ScheduledSetPoint point : mScheduledDay.getSetPoints()){
                ThermostatScheduleCard card = new ThermostatScheduleCard(getActivity());
                card.setScheduledSetPoint(point);
                materialListView.add(card);
            }
        }
    }

    private void updateMode(){
        resetVisibility();
        if(mScheduleModel.getNextEvent().equals("")) {
            nextEventLabel.setVisibility(View.INVISIBLE);
            descriptionText.setVisibility(View.INVISIBLE);
        }
        else {
            descriptionText.setText(mScheduleModel.getNextEvent());
            nextEventLabel.setVisibility(View.VISIBLE);
            descriptionText.setVisibility(View.VISIBLE);
        }

        switch (mScheduleModel.getMode()){
            case OFF:
                modeText.setText(ThermostatMode.OFF.name());
                break;
            case HEAT:
                modeText.setText(ThermostatMode.HEAT.name());
                break;
            case COOL:
                modeText.setText(ThermostatMode.COOL.name());
                break;
            case AUTO:
                modeText.setText(ThermostatMode.AUTO.name());
                break;
        }
    }

    private void resetVisibility(){
        scheduleView.setVisibility(View.VISIBLE);
        scheduleOff.setVisibility(View.GONE);
    }

    private void setRadioButtonCirclesVisibility(Map<DayOfWeek, ScheduledDay> daysOfWeekSchedules) {

        Iterator<Map.Entry<DayOfWeek,ScheduledDay>> schedulesIterator = daysOfWeekSchedules.entrySet().iterator();

        while(schedulesIterator.hasNext()) {
            Map.Entry<DayOfWeek,ScheduledDay> schedule = schedulesIterator.next();

            switch (schedule.getKey()) {
                case MONDAY:
                    showIfDayHasSchedule(mRadioButtonMon ,schedule.getValue().getSetPoints().size());
                    break;
                case TUESDAY:
                    showIfDayHasSchedule(mRadioButtonTue ,schedule.getValue().getSetPoints().size());
                    break;
                case WEDNESDAY:
                    showIfDayHasSchedule(mRadioButtonWed ,schedule.getValue().getSetPoints().size());
                    break;
                case THURSDAY:
                    showIfDayHasSchedule(mRadioButtonThu ,schedule.getValue().getSetPoints().size());
                    break;
                case FRIDAY:
                    showIfDayHasSchedule(mRadioButtonFri ,schedule.getValue().getSetPoints().size());
                    break;
                case SATURDAY:
                    showIfDayHasSchedule(mRadioButtonSat ,schedule.getValue().getSetPoints().size());
                    break;
                case SUNDAY:
                    showIfDayHasSchedule(mRadioButtonSun ,schedule.getValue().getSetPoints().size());
                    break;
            }

        }
    }

    private void showIfDayHasSchedule(View radioButton, int scheduleListSize) {
        if (scheduleListSize > 0) {
            radioButton.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.day_of_week_radio_drawable_white));
        } else {
            radioButton.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.day_of_week_radio_drawable_no_circle));
        }
    }

    @Override
    public void showScheduleDisabled(ScheduleModel model) {
        logger.debug("show schedule disabled:{}", model);
        showSchedule(model);
    }

    @Override public void showScheduleOff(ScheduleModel model) {
        logger.debug("show schedule off:{}", model);
        scheduleView.setVisibility(View.GONE);
        scheduleOff.setVisibility(View.VISIBLE);
        View offBtn = scheduleOff.findViewById(R.id.fragment_climate_schedule_off_btn);
        offBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                loadMultiButtonPopup();
            }
        });
    }

    private void loadMultiButtonPopup(){
        ArrayList<String> buttons = new ArrayList<>();
        buttons.add(getString(R.string.hvac_cool));
        buttons.add(getString(R.string.hvac_heat));
        buttons.add(getString(R.string.hvac_auto));
        buttons.add(getString(R.string.hvac_off));
        MultiButtonPopup popup = MultiButtonPopup.newInstance(getString(R.string.hvac_mode_selection), buttons);
        popup.setOnButtonClickedListener(ScheduleViewFragment.this);
        BackstackManager.getInstance().navigateToFloatingFragment(popup, popup.getClass().getSimpleName(), true);
    }

    @Override
    public void showSchedule(ScheduleModel model) {
        logger.debug("show schedule heat:{}", model);
        mScheduleModel = model;
        updateMode();
        resetVisibility();
    }


    @Override
    public void showSelectedDay(ScheduledDay model) {
        logger.debug("show selected day:{}", model);
        if(mRadioGroup.getCheckedRadioButtonId() == -1){
            mRadioGroup.check(R.id.radio_monday);
        }
        mScheduledDay = model;
        updateCard(null);
    }

    @Override
    public void showIfDaysHaveSchedules(Map<DayOfWeek,ScheduledDay> weekScheduledDayMap){
        setRadioButtonCirclesVisibility(weekScheduledDayMap);
    }

    @Override
    public void onError(ErrorModel error) {
        logger.error("Got error:{}",error);
        ErrorManager.in(getActivity()).show(DeviceErrorType.UNABLE_TO_SAVE_CHANGES);
    }

    //thermostat mode selected call back
    @Override
    public void onButtonClicked(String buttonValue) {
        mController.selectMode(ThermostatMode.valueOf(buttonValue));
    }
}

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

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.dexafree.materialList.model.CardItemView;
import arcus.cornea.subsystem.lawnandgarden.schedule.LawnAndGardenScheduleController;
import arcus.cornea.utils.Listeners;
import arcus.cornea.utils.LooperExecutor;
import arcus.cornea.utils.PropertyChangeMonitor;
import com.iris.client.ClientEvent;
import com.iris.client.bean.IrrigationSchedule;
import com.iris.client.capability.IrrigationController;
import com.iris.client.capability.LawnNGardenSubsystem;
import com.iris.client.event.Listener;
import com.iris.client.model.DeviceModel;
import arcus.app.R;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.error.ErrorManager;
import arcus.app.common.fragments.BaseFragment;
import arcus.app.common.image.ImageManager;
import arcus.app.common.image.picasso.transformation.BlackWhiteInvertTransformation;
import arcus.app.common.image.picasso.transformation.Invert;
import arcus.app.common.popups.AlertPopup;
import arcus.app.common.popups.NumberPickerPopup;
import arcus.app.common.schedule.GenericMaterialListView;
import arcus.app.common.schedule.RecyclerItemGenericListener;
import arcus.app.common.utils.CorneaUtils;
import arcus.app.common.view.Version1TextView;
import arcus.app.subsystems.lawnandgarden.cards.IrrigationModeSelectionCard;
import arcus.app.subsystems.lawnandgarden.schedule.IrrigationWeeklyScheduleFragment;


public class LawnAndGardenModeSelectionFragment extends BaseFragment implements LawnAndGardenScheduleController.Callback {
    private final static String DEVICE_ID = "DEVICEID";
    private final static int MIN_PERCENTAGE = 0;
    private final static int MIN_SETTABLE_PERCENTAGE = 10;
    private final static int MAX_PERCENTAGE = 100;
    private final static int STEP_PERCENTAGE = 10;
    private String deviceId;
    private DeviceModel deviceModel;
    private Version1TextView waterSaverPercentage;
    private ImageView waterSaverImage;
    boolean showWeeklyCalendar = false;
    boolean showEvenCalendar = false;
    boolean showOddCalendar = false;
    boolean showIntervalCalendar = false;

    private LawnAndGardenScheduleController controller;

    private GenericMaterialListView mListView;

    @NonNull
    public static LawnAndGardenModeSelectionFragment newInstance(String deviceId) {
        LawnAndGardenModeSelectionFragment fragment = new LawnAndGardenModeSelectionFragment();

        Bundle bundle = new Bundle(1);
        bundle.putString(DEVICE_ID, deviceId);
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    private final Listener<Throwable> onFailure = Listeners.runOnUiThread(new Listener<Throwable>() {
        @Override
        public void onEvent(Throwable throwable) {
            hideProgressBar();
        }
    });

    private final Listener<ClientEvent> onBudgetSuccess = Listeners.runOnUiThread(new Listener<ClientEvent>() {
        @Override
        public void onEvent(ClientEvent event) {
            hideProgressBar();
        }
    });

    private final Listener<Throwable> failedShowErrorListener = Listeners.runOnUiThread(new Listener<Throwable>() {
        @Override public void onEvent(Throwable throwable) {
            onError(throwable);
        }
    });

    private final PropertyChangeMonitor.Callback propertyChangeMonitorCallback = new PropertyChangeMonitor.Callback() {
        @Override public void requestTimedOut(String address, String attribute) {
            LooperExecutor.getMainExecutor().execute(new Runnable() {
                @Override public void run() {
                    ErrorManager.in(getActivity()).got(new RuntimeException("Unable to change modes. Timed Out."));
                }
            });
        }

        @Override public void requestSucceeded(String address, String attribute) {
            LooperExecutor.getMainExecutor().execute(new Runnable() {
                @Override public void run() {
                    hideProgressBar();
                }
            });
        }
    };
    private String subsystemAddress = null;


    private void createCard(String title, String subtext, String detailText, boolean selected, boolean showChevron, boolean showCalendarImage) {
        IrrigationModeSelectionCard card = new IrrigationModeSelectionCard(getActivity());
        card.showDivider();
        card.setTitle(title);
        card.setDescription(subtext);
        card.setRadioChecked(selected);
        if(!TextUtils.isEmpty(detailText)) {
            card.setRightText(detailText);
        }
        card.setChevronShown(showChevron);
        card.setShowScheduleIcon(showCalendarImage);
        mListView.add(card);
    }

    public void updateView() {
        mListView.clear();
        String mode = controller.getSelectedScheduleType(deviceModel.getAddress());
        waterSaverPercentage.setText(controller.getBudget((IrrigationController) deviceModel) + "%");

        boolean weekly = false;
        boolean interval = false;
        boolean odd = false;
        boolean even = false;
        boolean manual = false;
        if(mode.equals(IrrigationSchedule.TYPE_WEEKLY)) {
            weekly = true;
        }
        else if(mode.equals(IrrigationSchedule.TYPE_INTERVAL)) {
            interval = true;
        }
        else if(mode.equals(IrrigationSchedule.TYPE_ODD)) {
            odd = true;
        }
        else if(mode.equals(IrrigationSchedule.TYPE_EVEN)) {
            even = true;
        }
        else {
            manual = true;
        }
        showWeeklyCalendar = controller.getWeeklySchedule(deviceModel.getAddress()).size() == 0 ? false : true;
        showEvenCalendar = controller.getEvenSchedule(deviceModel.getAddress()).size() == 0 ? false : true;
        showOddCalendar = controller.getOddSchedule(deviceModel.getAddress()).size() == 0 ? false : true;
        showIntervalCalendar = controller.getIntervalSchedule(deviceModel.getAddress()).size() == 0 ? false : true;

        createCard(getString(R.string.irrigation_schedule_weekly), getString(R.string.irrigation_schedule_weekly_description), "", weekly, true, showWeeklyCalendar);
        createCard(getString(R.string.irrigation_schedule_interval), getString(R.string.irrigation_schedule_interval_description), "3 days", interval, true, showIntervalCalendar);
        createCard(getString(R.string.irrigation_schedule_odd_days), getString(R.string.irrigation_schedule_odd_days_description), "", odd, true, showOddCalendar);
        createCard(getString(R.string.irrigation_schedule_even_days), getString(R.string.irrigation_schedule_even_days_description), "", even, true, showEvenCalendar);
        createCard(getString(R.string.irrigation_schedule_manual), getString(R.string.irrigation_schedule_manual_description), "", manual, false, false);
    }

    @Override
    public void subsystemUpdate() {
        updateView();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        mListView = (GenericMaterialListView) view.findViewById(R.id.material_listview);
        setHasOptionsMenu(true);
        if(deviceId == null) {
            deviceId = getArguments().getString(DEVICE_ID);
        }
        if(deviceModel == null) {
            deviceModel = getCorneaService().getStore(DeviceModel.class).get(deviceId);
        }
        if (controller == null) {
            controller = LawnAndGardenScheduleController.instance();
            if (controller.getLawnNGardenSubsystem() != null) {
                subsystemAddress = controller.getLawnNGardenSubsystem().getAddress();
            }
        }
        LawnAndGardenScheduleController.instance().setCallback(this);
        controller.setAddress(deviceModel.getAddress());
        mListView.addOnItemTouchListener(new RecyclerItemGenericListener.OnItemClickListener() {
            @Override
            public void onItemClick(CardItemView cardItemView, int position, MotionEvent event) {
                if (hasRequestInFlight()) {
                    return;
                }
                if (deviceModel == null) {
                    deviceModel = getCorneaService().getStore(DeviceModel.class).get(deviceId);
                }

                String mode = controller.getSelectedScheduleType(deviceModel.getAddress());
                ImageView radButton = (ImageView) cardItemView.findViewById(R.id.rad_button);
                if (event.getX() < (radButton.getRight() + 75)) { // Increase hit area.
                    boolean validSelection = false;
                    switch (position) {
                        case 0:
                            if(showWeeklyCalendar) {
                                validSelection = true;
                                if (!IrrigationSchedule.TYPE_WEEKLY.equals(mode)) {
                                    changeControllerMode(LawnNGardenSubsystem.SwitchScheduleModeRequest.MODE_WEEKLY);
                                }
                            }
                            break;
                        case 1:
                            if(showIntervalCalendar) {
                                validSelection = true;
                                if (!IrrigationSchedule.TYPE_INTERVAL.equals(mode)) {
                                    changeControllerMode(LawnNGardenSubsystem.SwitchScheduleModeRequest.MODE_INTERVAL);
                                }
                            }
                            break;
                        case 2:
                            if(showOddCalendar) {
                                validSelection = true;
                                if (!IrrigationSchedule.TYPE_ODD.equals(mode)) {
                                    changeControllerMode(LawnNGardenSubsystem.SwitchScheduleModeRequest.MODE_ODD);
                                }
                            }
                            break;
                        case 3:
                            if(showEvenCalendar) {
                                validSelection = true;
                                if (!IrrigationSchedule.TYPE_EVEN.equals(mode)) {
                                    changeControllerMode(LawnNGardenSubsystem.SwitchScheduleModeRequest.MODE_EVEN);
                                }
                            }
                            break;
                        case 4:
                            validSelection = true;
                            if (!TextUtils.isEmpty(mode)) {
                                changeControllerMode(null); // Manual Mode
                            }
                            break;
                    }
                    if(!validSelection) {
                        hideProgressBar();
                        AlertPopup popup = AlertPopup.newInstance(getActivity().getString(R.string.water_schedule_no_events),
                                getActivity().getString(R.string.water_schedule_no_events_sub), null, null, new AlertPopup.AlertButtonCallback() {
                                    @Override
                                    public boolean topAlertButtonClicked() {
                                        return false;
                                    }

                                    @Override
                                    public boolean bottomAlertButtonClicked() {
                                        return false;
                                    }

                                    @Override
                                    public boolean errorButtonClicked() {
                                        return false;
                                    }

                                    @Override
                                    public void close() {
                                        BackstackManager.getInstance().navigateBack();
                                        getActivity().invalidateOptionsMenu();
                                    }
                                });
                        BackstackManager.getInstance().navigateToFloatingFragment(popup, popup.getClass().getSimpleName(), true);
                    }
                } else {

                    switch (position) {
                        case 0:
                            BackstackManager.getInstance().navigateToFragment(IrrigationWeeklyScheduleFragment.newInstance(CorneaUtils.getDeviceAddress(deviceModel.getId()), deviceModel.getName(), IrrigationSchedule.TYPE_WEEKLY), true);
                            break;
                        case 1:
                            BackstackManager.getInstance().navigateToFragment(IrrigationWeeklyScheduleFragment.newInstance(CorneaUtils.getDeviceAddress(deviceModel.getId()), deviceModel.getName(), IrrigationSchedule.TYPE_INTERVAL), true);
                            break;
                        case 2:
                            BackstackManager.getInstance().navigateToFragment(IrrigationWeeklyScheduleFragment.newInstance(CorneaUtils.getDeviceAddress(deviceModel.getId()), deviceModel.getName(), IrrigationSchedule.TYPE_ODD), true);
                            break;
                        case 3:
                            BackstackManager.getInstance().navigateToFragment(IrrigationWeeklyScheduleFragment.newInstance(CorneaUtils.getDeviceAddress(deviceModel.getId()), deviceModel.getName(), IrrigationSchedule.TYPE_EVEN), true);
                            break;
                        case 4:
                            if (!TextUtils.isEmpty(mode)) {
                                changeControllerMode(null); // Manual Mode
                            }
                            break;
                    }
                }
            }

            @Override
            public void onItemLongClick(CardItemView view, int position) {

            }
        });



        waterSaverImage = (ImageView) view.findViewById(R.id.irrigation_device_image);
        waterSaverPercentage = (Version1TextView) view.findViewById(R.id.water_saver_percentage);
        View waterSaver = view.findViewById(R.id.irrigation_water_saver);
        waterSaver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String currentPercentage = waterSaverPercentage.getText().toString();
                currentPercentage = currentPercentage.substring(0, currentPercentage.length() - 1);
                NumberPickerPopup percentPicker = NumberPickerPopup.newInstance(NumberPickerPopup.NumberPickerType.PERCENT, MIN_SETTABLE_PERCENTAGE, MAX_PERCENTAGE,
                        Integer.parseInt(currentPercentage), STEP_PERCENTAGE);
                percentPicker.setFloatingTitle(getResources().getString(R.string.water_saver));
                percentPicker.setDescription(getResources().getString(R.string.irrigation_water_saver_popup_description));
                percentPicker.setOnValueChangedListener(new NumberPickerPopup.OnValueChangedListener() {
                    @Override
                    public void onValueChanged(int value) {
                        showProgressBar();
                        controller.updateBudget(value, deviceModel, onBudgetSuccess, onFailure);
                    }
                });
                BackstackManager.getInstance().navigateToFloatingFragment(percentPicker, percentPicker.getClass().getSimpleName(), true);
            }
        });

        if(deviceModel != null) {
            waterSaverPercentage.setText(controller.getBudget((IrrigationController) deviceModel) + "%");
            ImageManager.with(getActivity())
                    .putDrawableResource(R.drawable.icon_cat_water)
                    .fit()
                    .withTransform(new BlackWhiteInvertTransformation(Invert.BLACK_TO_WHITE))
                    .into(waterSaverImage)
                    .execute();
        }

        updateView();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        deviceId = getArguments().getString(DEVICE_ID);
        deviceModel = getCorneaService().getStore(DeviceModel.class).get(deviceId);
        getActivity().setTitle(getTitle());
    }

    @Override
    public void onPause() {
        super.onPause();
        if (!TextUtils.isEmpty(subsystemAddress)) {
            PropertyChangeMonitor.instance().removeAllFor(subsystemAddress);
        }
        hideProgressBar();
    }

    @NonNull
    @Override
    public String getTitle() {
        return getActivity().getString(R.string.irrigation_mode);
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_lawn_and_garden_mode_selection;
    }

    protected void onError(Throwable throwable) {
        try {
            hideProgressBar();
            ErrorManager.in(getActivity()).got(throwable);
        } catch (Exception ignored) {}
    }

    // Pass null to set to "manual" mode.
    protected void changeControllerMode(@Nullable String toThisMode) {
        if (controller == null || deviceModel == null) {
            return;
        }

        if (!TextUtils.isEmpty(subsystemAddress)) {
            PropertyChangeMonitor.instance().startMonitorFor(subsystemAddress,
                  LawnNGardenSubsystem.ATTR_SCHEDULESTATUS, 30_000, propertyChangeMonitorCallback, null, null);
        }

        showProgressBar();
        if (TextUtils.isEmpty(toThisMode)) {
            controller.disableScheduling(deviceModel.getAddress(), failedShowErrorListener);
        }
        else {
            controller.updateScheduleStatus(deviceModel.getAddress(), toThisMode, failedShowErrorListener);
        }
    }

    protected boolean hasRequestInFlight() {
        return !TextUtils.isEmpty(subsystemAddress) && PropertyChangeMonitor.instance().hasAnyChangesFor(subsystemAddress);
    }
}

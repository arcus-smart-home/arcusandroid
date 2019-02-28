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
package arcus.app.device.details;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.iris.client.model.DeviceModel;
import arcus.app.R;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.error.ErrorManager;
import arcus.app.common.fragments.IClosedFragment;
import arcus.app.common.fragments.IShowedFragment;
import arcus.app.common.popups.MultiButtonPopup;
import arcus.app.common.utils.DeviceSeekArc;
import arcus.app.common.view.GlowableImageView;
import arcus.app.common.view.Version1TextView;
import arcus.app.device.details.model.ThermostatDisplayModel;
import arcus.app.device.details.model.ThermostatOperatingMode;
import arcus.app.device.details.presenters.HoneywellThermostatPresenter;
import arcus.app.device.details.presenters.NestThermostatPresenter;
import arcus.app.device.details.presenters.StandardThermostatPresenter;
import arcus.app.device.details.presenters.ThermostatPresenterContract;
import arcus.app.device.model.DeviceType;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class NestThermostatFragment extends ArcusProductFragment implements ThermostatPresenterContract.ThermostatControlView, IShowedFragment, IClosedFragment {

    private ThermostatPresenterContract.ThermostatPresenter presenter;

    private TextView modeText;
    private TextView tempText;
    private TextView humidityText;
    private LinearLayout humidityRegion;
    private ImageButton plusButton;
    private ImageButton minusButton;
    private Version1TextView nextEventLabel;
    private Version1TextView nextEventDescription;
    private ImageButton modeBtn;
    private ImageView leafIcon;

    public static NestThermostatFragment newInstance() {
        return new NestThermostatFragment();
    }

    @Override
    public Integer topSectionLayout() {
        return R.layout.device_top_schedule;
    }

    @Override
    public Integer deviceImageSectionLayout() {
        return R.layout.nest_thermostat_image_section;
    }

    @Override
    public Integer statusSectionLayout() {
        return R.layout.nest_thermostat_status;
    }

    @Override
    public void doTopSection() {
        nextEventLabel = (Version1TextView) topView.findViewById(R.id.device_top_schdule_event);
        nextEventLabel.setText(getString(R.string.next_event_label));
        nextEventDescription = (Version1TextView) topView.findViewById(R.id.device_top_schdule_time);
    }

    @Override
    public void doDeviceImageSection() {
        seekArc = (DeviceSeekArc) deviceImageView.findViewById(R.id.seekArc);
        tempText = (TextView) deviceImageView.findViewById(R.id.thermostat_center_status_temp);
        modeText = (TextView) deviceImageView.findViewById(R.id.thermostat_center_status_mode);
        humidityText = (TextView) deviceImageView.findViewById(R.id.humidity);
        humidityRegion = (LinearLayout) deviceImageView.findViewById(R.id.humidity_region);
        leafIcon = (ImageView) deviceImageView.findViewById(R.id.leaf_icon);

        seekArc.setUseFixedSize(true);
        seekArc.setDrawLabelsInsideThumbs(false);
        seekArc.setRoundedEdges(true);
        seekArc.setTouchInSide(false);
        seekArc.setTextEnabled(true);
        seekArc.setClickToSeek(false);
        seekArc.setLabelTextTransformer(new DeviceSeekArc.LabelTextTransform() {
            private final String DEGREE_SYMBOL = getString(R.string.degree_symbol);
            @Override
            public CharSequence getDisplayedLabel(String forValue) {
                return forValue + DEGREE_SYMBOL;
            }
        });

        tempText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                presenter.toggleActiveSetpoint();
            }
        });

        seekArc.setOnSeekArcChangeListener(new DeviceSeekArc.OnSeekArcChangeListener() {
            @Override
            public void onProgressChanged(@NonNull DeviceSeekArc seekArc, int selectedThumb, int progress, boolean fromUser) {
                // If value is less than 3 away, push the other value back one
                if (selectedThumb == DeviceSeekArc.THUMB_LOW && seekArc.isRangeEnabled() && fromUser) {
                    if (progress >= seekArc.getProgress(DeviceSeekArc.THUMB_HIGH) - seekArc.getMinRangeDistance()) {
                        seekArc.setProgress(DeviceSeekArc.THUMB_HIGH, progress + seekArc.getMinRangeDistance(), true);
                        seekArc.setActiveProgress(DeviceSeekArc.THUMB_LOW);
                    }
                }

                if (selectedThumb == DeviceSeekArc.THUMB_HIGH && seekArc.isRangeEnabled() && fromUser) {
                    if (progress <= seekArc.getProgress(DeviceSeekArc.THUMB_LOW) + seekArc.getMinRangeDistance()) {
                        seekArc.setProgress(DeviceSeekArc.THUMB_LOW, progress - seekArc.getMinRangeDistance(), true);
                        seekArc.setActiveProgress(DeviceSeekArc.THUMB_HIGH);
                    }
                }

                presenter.notifyAdjustmentInProgress();
            }

            @Override
            public void onStartTrackingTouch(DeviceSeekArc seekArc, int selectedThumb, int progress) {
                // Nothing to do
            }

            @Override
            public void onStopTrackingTouch(DeviceSeekArc seekArc, int selectedThumb, int progress) {
                if (seekArc.isRangeEnabled()) {
                    presenter.setSetpointRange(seekArc.getProgress(DeviceSeekArc.THUMB_HIGH), seekArc.getProgress(DeviceSeekArc.THUMB_LOW));
                } else {
                    presenter.setSetpoint(seekArc.getProgress(DeviceSeekArc.THUMB_LOW));
                }
            }
        });

        deviceImage = (GlowableImageView) deviceImageView.findViewById(R.id.fragment_device_info_image);
        deviceImage.setImageDrawable(getResources().getDrawable(R.drawable.thermostat_ring));
        deviceImage.setVisibility(View.VISIBLE);
        deviceImage.setGlowMode(GlowableImageView.GlowMode.OFF);
        deviceImage.setGlowing(false);
    }

    @Override
    public void doStatusSection() {
        minusButton = (ImageButton) statusView.findViewById(R.id.thermostat_minus_btn);
        plusButton = (ImageButton) statusView.findViewById(R.id.thermostat_plus_btn);
        modeBtn = (ImageButton) statusView.findViewById(R.id.thermostat_mode_btn);


        minusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                presenter.decrementSetpoint();
            }
        });

        plusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                presenter.incrementSetpoint();
            }
        });

        modeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                presenter.selectMode();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter = getPresenterForDevice();
    }

    @Override
    public void onPause() {
        super.onPause();
        presenter.stopPresenting();
    }

    @Override
    public void onShowedFragment() {
        presenter.startPresenting(this);
        presenter.updateView();
    }

    @Override
    public void onClosedFragment() {
        presenter.stopPresenting();
    }

    @Override
    public void onPending(@Nullable Integer progressPercent) {
        showProgressBar();
    }

    @Override
    public void onError(@NonNull Throwable throwable) {
        hideProgressBar();
        ErrorManager.in(getActivity()).showGenericBecauseOf(throwable);
    }

    @Override
    public void updateView(@NonNull ThermostatDisplayModel model) {
        // Ensure that the fragment has an activity
        if(!isAdded() || isDetached()) {
            return;
        }

        hideProgressBar();

        super.setEnabled(!model.isControlDisabled());
        modeText.setText(getModeText(model));

        seekArc.bringToFront();
        seekArc.setMinValue(model.getMinSetpoint());
        seekArc.setMaxValue(model.getMaxSetpoint());
        seekArc.setMaximumStopValue(model.getMaxSetpointStopValue());
        seekArc.setMinimumStopValue(model.getMinSetpointStopValue());
        seekArc.setMinRangeDistance(model.getMinSetpointSeparation());
        seekArc.setRangeEnabled(model.getOperatingMode() == ThermostatOperatingMode.AUTO);
        seekArc.setMarkerPosition(model.getCurrentTemperature());
        seekArc.setArcHiliteGradient(getArcHilite(model));
        seekArc.setEnabled(!model.isControlDisabled() && (model.getOperatingMode() != ThermostatOperatingMode.OFF && model.getOperatingMode() != ThermostatOperatingMode.ECO));
        seekArc.setDrawThumbsWhenDisabled(model.getOperatingMode() != ThermostatOperatingMode.OFF && model.getOperatingMode() != ThermostatOperatingMode.ECO);

        switch (model.getOperatingMode()) {
            case AUTO:
                seekArc.setProgress(DeviceSeekArc.THUMB_LOW, model.getHeatSetpoint());
                seekArc.setProgress(DeviceSeekArc.THUMB_HIGH, model.getCoolSetpoint());
                break;
            case HEAT:
                seekArc.setProgress(DeviceSeekArc.THUMB_LOW, model.getHeatSetpoint());
                break;
            case COOL:
                seekArc.setProgress(DeviceSeekArc.THUMB_LOW, model.getCoolSetpoint());
                break;
            default:
                // Nothing to do
        }

        plusButton.setVisibility(getPlusMinusButtonsVisiblility(model));
        minusButton.setVisibility(getPlusMinusButtonsVisiblility(model));
        tempText.setText(model.getSetpointsText());
        cloudIcon.setVisibility(model.isCloudConnected() ? View.VISIBLE : View.GONE);
        leafIcon.setVisibility(model.hasLeaf() ? View.VISIBLE : View.GONE);

        if (model.getRelativeHumidity() != null) {
            humidityRegion.setVisibility(View.VISIBLE);
            humidityText.setText(getString(R.string.percent_number, (int) model.getRelativeHumidity()));
        } else {
            humidityRegion.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDisplayModeSelection(List<ThermostatOperatingMode> availableModes, boolean useNestTerminology) {
        ArrayList<String> buttons = new ArrayList<>();
        for (ThermostatOperatingMode thisMode : availableModes) {
            buttons.add(getString(thisMode.getStringResId(useNestTerminology)).toUpperCase());
        }

        MultiButtonPopup popup = MultiButtonPopup.newInstance(getString(R.string.hvac_mode_selection), buttons);
        popup.setOnButtonClickedListener(new MultiButtonPopup.OnButtonClickedListener() {
            @Override
            public void onButtonClicked(String buttonValue) {
                presenter.setOperatingMode(ThermostatOperatingMode.fromDisplayString(buttonValue));
            }
        });

        BackstackManager.getInstance().navigateToFloatingFragment(popup, popup.getClass().getSimpleName(), true);
    }

    @Override
    public DeviceModel getThermostatDeviceModel() {
        return getDeviceModel();
    }

    @Override
    public void onShowScheduleEvent(String eventText) {
        if (StringUtils.isEmpty(eventText)) {
            nextEventLabel.setVisibility(View.INVISIBLE);
            nextEventDescription.setVisibility(View.INVISIBLE);
        } else {
            nextEventDescription.setText(eventText);
            nextEventLabel.setVisibility(View.VISIBLE);
            nextEventDescription.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onUpdateFooterState(boolean inAlertState) {
        setEnabledRecursively(bottomView, true);
        setEnabledRecursively(justAMoment, true);

        setBottomViewAlerting(inAlertState);
    }

    @Override
    public void setWaitingIndicatorVisible(boolean isVisible, boolean isControlDisabled) {
        justAMoment.setVisibility(isVisible ? View.VISIBLE : View.GONE);
        waitingLabel.setVisibility(isVisible ? View.VISIBLE : View.GONE);

        super.setEnabled(!isControlDisabled);
    }

    private String getModeText(ThermostatDisplayModel model) {
        switch (model.getOperatingMode()) {
            case HEAT:
            case COOL:
            case AUTO:
                return getResourceString(model.getOperatingMode().getStringResId(model.isUseNestTerminology())).toUpperCase();
            default:
                return null;
        }
    }

    private ThermostatPresenterContract.ThermostatPresenter getPresenterForDevice() {
        DeviceType devType = DeviceType.fromHint(getDeviceModel().getDevtypehint());

        switch (devType) {
            case THERMOSTAT:
                return new StandardThermostatPresenter();
            case TCC_THERM:
                return new HoneywellThermostatPresenter();
            case NEST_THERMOSTAT:
                return new NestThermostatPresenter();
            default:
                throw new IllegalStateException("Bug! This fragment cannot be used to render a " + devType);
        }

    }

    @ViewVisibility
    private int getPlusMinusButtonsVisiblility(ThermostatDisplayModel model) {
        boolean visible =  model.getOperatingMode() != ThermostatOperatingMode.OFF &&
                model.getOperatingMode() != ThermostatOperatingMode.ECO;

        return visible ? View.VISIBLE : View.GONE;
    }

    private DeviceSeekArc.ArcHiliteGradient getArcHilite(ThermostatDisplayModel model) {

        if (model.isCoolRunning()) {
            return DeviceSeekArc.ArcHiliteGradient.forCooling(model.getOperatingMode() == ThermostatOperatingMode.AUTO);
        } else if (model.isHeatRunning()) {
            return DeviceSeekArc.ArcHiliteGradient.forHeating();
        } else if (model.getOperatingMode() == ThermostatOperatingMode.HEAT) {
            return DeviceSeekArc.ArcHiliteGradient.forNotHeating();
        } else if (model.getOperatingMode() == ThermostatOperatingMode.COOL) {
            return DeviceSeekArc.ArcHiliteGradient.forNotCooling();
        }

        return null;        // No hilite for current mode
    }
}

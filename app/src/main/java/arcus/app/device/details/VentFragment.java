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


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.View;
import android.widget.TextView;

import com.iris.client.capability.Temperature;
import com.iris.client.capability.Vent;
import com.iris.client.event.Listener;
import arcus.app.R;
import arcus.app.common.fragments.IShowedFragment;
import arcus.app.common.utils.DeviceSeekArc;
import arcus.app.common.utils.ThrottledExecutor;

import java.beans.PropertyChangeEvent;


public class VentFragment extends ArcusProductFragment implements IShowedFragment, DeviceSeekArc.OnSeekArcChangeListener {

    private static final int THROTTLE_PERIOD_MS = 1000;
    private static final int QUIESCENT_MS = 5000;
    private int mCurrentVentLevel = 0;

    private final ThrottledExecutor throttle = new ThrottledExecutor(THROTTLE_PERIOD_MS);

    private TextView openBottomText;
    private TextView tempBottomText;

    @NonNull
    public static VentFragment newInstance() {
        return new VentFragment();
    }

    @Override
    public Integer statusSectionLayout() {
        return R.layout.vent_sensor_status;
    }

    @Override
    public Integer topSectionLayout() {
        return R.layout.device_top_schedule;
    }

    @Override
    public Integer deviceImageSectionLayout() {
        //  reusing dimmer image section with modifications
        return R.layout.dimmer_image_section;
    }

    @Override
    public void doTopSection() {
        // Nothing to do
    }

    @Override
    public void onShowedFragment() {
        checkConnection();
        Integer level = getPlatformVentLevel();
        if (level != null) {
            if(seekArc != null){
                seekArc.setProgress(DeviceSeekArc.THUMB_LOW, level);
                setUiVentLevel(level);
                mCurrentVentLevel = level;
            }
        }
    }

    @Override
    public void propertyUpdated(@NonNull final PropertyChangeEvent event) {

        switch (event.getPropertyName()) {

            case Vent.ATTR_LEVEL:    // Cast to Number
                throttle.executeAfterQuiescence(new Runnable() {
                    @Override
                    public void run() {
                        int newPercentageOpen = ((Number) event.getNewValue()).intValue();
                        setUiVentLevel(newPercentageOpen);

                        seekArc.setOnSeekArcChangeListener(null);
                        seekArc.setProgress(DeviceSeekArc.THUMB_LOW, newPercentageOpen);
                        seekArc.setOnSeekArcChangeListener(VentFragment.this);
                    }
                }, QUIESCENT_MS);

                break;

            case Temperature.ATTR_TEMPERATURE:
                updateTemperatureTextView(tempBottomText, event.getNewValue());
                break;

            default:
                logger.debug("Received Vent update: {} -> {}", event.getPropertyName(), event.getNewValue());
                super.propertyUpdated(event);
                break;
        }
    }


    @Override
    public void doStatusSection() {

        View ventOpeningView = statusView.findViewById(R.id.vent_sensor_opening_status);
        View ventTempView = statusView.findViewById(R.id.vent_sensor_temp_status);

        TextView openTopText = (TextView) ventOpeningView.findViewById(R.id.top_status_text);
        openBottomText = (TextView) ventOpeningView.findViewById(R.id.bottom_status_text);
        openTopText.setText(getActivity().getResources().getString(R.string.vent_open_text));

        TextView tempTopText = (TextView) ventTempView.findViewById(R.id.top_status_text);
        tempBottomText = (TextView) ventTempView.findViewById(R.id.bottom_status_text);
        tempTopText.setText(getActivity().getResources().getString(R.string.vent_temp_text));

        seekArc.setUseFixedSize(true);
        seekArc.setVisibility(View.VISIBLE);
        seekArc.setRoundedEdges(true);
        seekArc.setLeftArcText(getActivity().getResources().getString(R.string.vent_arc_text_shut));
        seekArc.setRightArcText(getActivity().getResources().getString(R.string.vent_arc_text_open));

        updateTemperatureTextView(tempBottomText, getPlatformTemperature());
        setUiVentLevel(seekArc.getProgress());

        seekArc.setOnSeekArcChangeListener(this);
    }

    @Override
    public void onProgressChanged(final DeviceSeekArc seekArc, final int thumb, final int progress, final boolean fromUser) {
        // Nothing to do
    }

    @Override
    public void onStartTrackingTouch(final DeviceSeekArc seekArc, final int thumb, final int progress) {
        // Nothing to do
        mCurrentVentLevel = progress;
    }

    @Override
    public void onStopTrackingTouch(final DeviceSeekArc seekArc, final int thumb, final int progress) {
        if(mCurrentVentLevel != progress) {
            // Update the percentage text indicator immediately ...
            setUiVentLevel(progress);
            setPlatformVentLevel(progress);
            mCurrentVentLevel = progress;
        }
    }

    @Override
    public void doDeviceImageSection() {

        super.doDeviceImageSection();
        deviceImage.setBevelVisible(false);

        seekArc = (DeviceSeekArc) deviceImageView.findViewById(R.id.seekArc);
    }

    private void setUiVentLevel (int percentOpen) {
        openBottomText.setText(getString(R.string.lightsnswitches_percentage, percentOpen));
    }

    private void setPlatformVentLevel(int percentOpen) {
        Vent vent = getCapability(Vent.class);
        if (vent != null) {
            vent.setLevel(percentOpen);
            getDeviceModel().commit().onFailure(new Listener<Throwable>() {
                @Override
                public void onEvent(Throwable throwable) {
                    logger.error("Error updating vent level.", throwable);
                }
            });
        }
    }

    @Nullable
    private Integer getPlatformVentLevel() {
        Vent vent = getCapability(Vent.class);
        return vent == null ? null : vent.getLevel();
    }

    @Nullable
    private Double getPlatformTemperature() {
        Temperature temperature = getCapability(Temperature.class);
        return temperature == null ? null : temperature.getTemperature();
    }

}

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
import android.widget.RelativeLayout;
import android.widget.TextView;

import arcus.cornea.device.blinds.model.ShadeClientModel;
import arcus.app.R;
import arcus.app.common.utils.DeviceSeekArc;
import arcus.app.device.details.presenters.ShadeContract;
import arcus.app.device.details.presenters.ShadePresenter;


public class ShadeFragment extends ArcusProductFragment implements DeviceSeekArc.OnSeekArcChangeListener, ShadeContract.ShadeView {

    private static final double LAYOUT_WIDTH = 0.8;
    private static final int ARC_WIDTH = 64;
    private int openLevel = 0;

    private TextView openBottomText;
    private TextView powerBottomText;
    private TextView powerTopText;
    private ShadePresenter presenter;

    @NonNull
    public static ShadeFragment newInstance() {
        return new ShadeFragment();
    }

    @Override
    public void onResume() {
        super.onResume();

        if(presenter == null) {
            if(getDeviceModel() != null) {
                presenter = new ShadePresenter(getDeviceModel().getAddress());
            }
        }

        if(presenter != null) {
            presenter.startPresenting(this);
            presenter.requestUpdate();
        }

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
        return R.layout.dimmer_image_section;
    }

    @Override
    public void doTopSection() {
        // Nothing to do
    }

    @Override
    public void doStatusSection() {

        View openingView = statusView.findViewById(R.id.vent_sensor_opening_status);
        View ventTempView = statusView.findViewById(R.id.vent_sensor_temp_status);

        TextView openTopText = (TextView) openingView.findViewById(R.id.top_status_text);
        openBottomText = (TextView) openingView.findViewById(R.id.bottom_status_text);
        openTopText.setText(getActivity().getResources().getString(R.string.vent_open_text));

        powerTopText = (TextView) ventTempView.findViewById(R.id.top_status_text);
        powerBottomText = (TextView) ventTempView.findViewById(R.id.bottom_status_text);

        seekArc.setVisibility(View.VISIBLE);
        seekArc.setArcWidth(ARC_WIDTH);
        seekArc.setRoundedEdges(true);
        seekArc.setProgressWidth(ARC_WIDTH);
        seekArc.setLeftArcText(getActivity().getResources().getString(R.string.vent_arc_text_shut));
        seekArc.setRightArcText(getActivity().getResources().getString(R.string.vent_arc_text_open));
        seekArc.setOnSeekArcChangeListener(this);
    }

    @Override
    public void onProgressChanged(final DeviceSeekArc seekArc, final int thumb, final int progress, final boolean fromUser) {

    }

    @Override
    public void onStartTrackingTouch(final DeviceSeekArc seekArc, final int thumb, final int progress) {
        openLevel = progress;
    }

    @Override
    public void onStopTrackingTouch(final DeviceSeekArc seekArc, final int thumb, final int progress) {
        if(openLevel != progress) {
            setLevel(progress, true);
            openLevel = progress;
        }
    }

    @Override
    public void doDeviceImageSection() {

        super.doDeviceImageSection();
        deviceImage.setBevelVisible(false);

        seekArc = (DeviceSeekArc) deviceImageView.findViewById(R.id.seekArc);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) seekArc.getLayoutParams();
        params.width = (int)(getActivity().getResources().getDisplayMetrics().widthPixels * LAYOUT_WIDTH);
        seekArc.setLayoutParams(params);

    }

    private void setLevel(int percentOpen, boolean fromUser) {
        openBottomText.setText(getString(R.string.lightsnswitches_percentage, percentOpen));
        if(fromUser) {
            presenter.setLevel(percentOpen);
        }
    }

    @Override
    public void onPending(@Nullable Integer progressPercent) {

    }

    @Override
    public void onError(@NonNull Throwable throwable) {

    }

    @Override
    public void updateView(@NonNull ShadeClientModel model) {
        if(seekArc == null || !isAdded() || isDetached()) {
            return;
        }

        seekArc.setProgress(DeviceSeekArc.THUMB_LOW, model.getLevel());
        setLevel(model.getLevel(), false);
        openLevel = model.getLevel();

        updatePowerSourceAndBattery(powerTopText, powerBottomText);
    }
}

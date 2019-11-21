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
import android.view.View;
import android.widget.TextView;

import com.iris.client.capability.DevicePower;
import com.iris.client.capability.Tilt;
import arcus.app.R;
import arcus.app.common.fragments.IShowedFragment;
import arcus.app.common.utils.GlobalSetting;
import arcus.app.common.view.GlowableImageView;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyChangeEvent;


public class TiltSensorFragment extends ArcusProductFragment implements IShowedFragment {

    private static final Logger logger = LoggerFactory.getLogger(TiltSensorFragment.class);
    private TextView openBottomText;
    private TextView batteryBottomText;




    @NonNull
    public static TiltSensorFragment newInstance() {
        TiltSensorFragment fragment = new TiltSensorFragment();
        return fragment;
    }

    @Override
    public Integer topSectionLayout() {
        return R.layout.device_top_schedule;
    }

    @Override
    public void doTopSection() {

    }

    @Override
    public void doStatusSection() {
        View openCLoseView = statusView.findViewById(R.id.tilt_sensor_status_close_open);
        View batteryView = statusView.findViewById(R.id.tilt_sensor_status_battery);

        TextView openTopText = (TextView) openCLoseView.findViewById(R.id.top_status_text);
        openBottomText = (TextView) openCLoseView.findViewById(R.id.bottom_status_text);

        openTopText.setText(getString(R.string.tilt_state_text));

        TextView batteryTopText = (TextView) batteryView.findViewById(R.id.top_status_text);
        batteryBottomText = (TextView) batteryView.findViewById(R.id.bottom_status_text);
        updatePowerSourceAndBattery(batteryTopText, batteryBottomText);

        setImageGlowMode(GlowableImageView.GlowMode.OPEN_CLOSE);

        updateTextandGlow(getTiltState());
    }

    @Override
    public void propertyUpdated(@NonNull PropertyChangeEvent event) {
        switch (event.getPropertyName()) {
            case DevicePower.ATTR_SOURCE:
            case DevicePower.ATTR_BATTERY:
                updatePowerSourceAndBattery(null, batteryBottomText);
                break;
            case Tilt.ATTR_TILTSTATE:
                updateTextandGlow(event.getNewValue().toString());
                logger.debug("Tilt state changed from :{} to :{}", event.getOldValue(), event.getNewValue());
                break;
            default:
                super.propertyUpdated(event);
                break;
        }
    }


    private String getTiltState() {
        final Tilt tilt = getCapability(Tilt.class);
        if (tilt != null && tilt.getTiltstate() != null) {
            return tilt.getTiltstate();
        }
        //TODO possible refactor
        return Tilt.TILTSTATE_FLAT;
    }

    private void updateTextandGlow(String strValue) {
        final Tilt tilt = getCapability(Tilt.class);


        //deMorgan's equivalent: tilt == null || tilt.getTags() == null; using bang to allow shortcircuit
        if (!(tilt != null && tilt.getTags() != null )) {
            return;
        }
        //HORIZONTALLY CLOSING DEVICE
            if (!tilt.getTags().contains(GlobalSetting.VERTICAL_TILT_TAG)) {

                if (Tilt.TILTSTATE_UPRIGHT.equals(strValue)) {
                    updateTextView(openBottomText, getString(R.string.tilt_display_state_open));
                    updateGlow(deviceImage, true);
                } else {
                    updateTextView(openBottomText, getString(R.string.tilt_display_state_close));
                    updateGlow(deviceImage, false);
                }
                //VERTICALLY CLOSING DEVICE
            } else {
                if (Tilt.TILTSTATE_FLAT.equals(strValue)) {
                    updateTextView(openBottomText, getString(R.string.tilt_display_state_open));
                    updateGlow(deviceImage, true);
                } else {
                    updateTextView(openBottomText, getString(R.string.tilt_display_state_close));
                    updateGlow(deviceImage, false);


                }
            }

    }


    public final void updateGlow(@NonNull final GlowableImageView view, final boolean bGlow) {

              getActivity().runOnUiThread(new Runnable() {
                  @Override
                  public void run() {
                      if(view != null) {
                          view.setGlowing(bGlow);
                      }
                  }
              });

    }

    @Override
    public Integer statusSectionLayout() {
        return R.layout.tilt_sensor_status;
    }

    @Override
    public void onShowedFragment() {
        checkConnection();
        updateTextandGlow(getTiltState());

    }
}

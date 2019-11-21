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

import android.os.Looper;
import androidx.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import arcus.cornea.utils.LooperExecutor;
import com.iris.client.capability.DeviceConnection;
import com.iris.client.capability.DevicePower;
import com.iris.client.capability.Presence;
import arcus.app.R;
import arcus.app.common.banners.core.BannerManager;
import arcus.app.common.banners.NoConnectionBanner;
import arcus.app.common.fragments.IShowedFragment;
import arcus.app.common.view.GlowableImageView;
import arcus.app.device.buttons.controller.ButtonActionSequenceController;
import arcus.app.device.buttons.model.ButtonDevice;
import arcus.app.device.buttons.model.ButtonSequenceVariant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyChangeEvent;


public class KeyFobFragment extends ArcusProductFragment implements IShowedFragment {
    private static final Logger logger = LoggerFactory.getLogger(KeyFobFragment.class);

    private TextView batteryTopText;
    private TextView batteryBottomText;
    private TextView eventTextView;
    private ImageView btnSettings;
    private ImageView homeIcon;

    @NonNull
    public static KeyFobFragment newInstance() {
        return new KeyFobFragment();
    }

    @Override
    public Integer topSectionLayout() {
        return R.layout.device_top_schedule;
    }

    @Override
    public void doTopSection() {
        eventTextView = (TextView) topView.findViewById(R.id.device_top_schdule_event);
        homeIcon = (ImageView) topView.findViewById(R.id.device_top_home_away);

        updateArrivedAway(getPresence());
    }

    @Override
    public void doStatusSection() {
        batteryTopText = (TextView) statusView.findViewById(R.id.top_status_text);
        batteryBottomText = (TextView) statusView.findViewById(R.id.bottom_status_text);

        updatePowerSourceAndBattery(batteryTopText, batteryBottomText);

        btnSettings = (ImageView) statusView.findViewById(R.id.btn_keyfob_settings);
        if (getDeviceModel() != null) {
            if (ButtonDevice.isButtonDevice(getDeviceModel().getProductId())) {
                btnSettings.setVisibility(View.VISIBLE);
                btnSettings.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (getDeviceModel() != null) {
                            new ButtonActionSequenceController(getActivity(), ButtonSequenceVariant.SETTINGS,
                                    getDeviceModel().getAddress()).startSequence(getActivity(), null);
                        }
                    }
                });
            } else {
                btnSettings.setVisibility(View.GONE);
            }
        }
    }


    @Override
    public void propertyUpdated(@NonNull PropertyChangeEvent event) {
        switch (event.getPropertyName()) {
            case DeviceConnection.ATTR_STATE:
                updateArrivedAway(String.valueOf(event.getNewValue()));
                break;

            case DevicePower.ATTR_SOURCE:
            case DevicePower.ATTR_BATTERY:
                updatePowerSourceAndBattery(batteryTopText, batteryBottomText);
                break;

            case Presence.ATTR_PRESENCE:
                updateArrivedAway((String) event.getNewValue());
                break;

            case Presence.ATTR_PRESENCECHANGED:
                updateArrivedAway(getPresence());
                break;

            default:
                break;
        }
    }

    private void updateArrivedAway(@NonNull final String state) {

        new LooperExecutor(Looper.getMainLooper()).execute(new Runnable() {
            @Override
            public void run() {
                homeIcon.setVisibility(View.VISIBLE);

                switch (state) {
                    case DeviceConnection.STATE_OFFLINE:
                    case Presence.PRESENCE_ABSENT:
                        updateTextView(eventTextView, getResourceString(R.string.care_pendant_away));
                        homeIcon.setImageResource(R.drawable.away);
                        break;
                    case DeviceConnection.STATE_ONLINE:
                    case Presence.PRESENCE_PRESENT:
                        updateTextView(eventTextView, getResourceString(R.string.care_pendant_home));
                        homeIcon.setImageResource(R.drawable.home);
                        break;
                }

                updateImageGlow();
            }
        });
    }


    private void updateState() {

        DeviceConnection connection = getCapability(DeviceConnection.class);

        Presence presence = getCapability(Presence.class);

        // might need to updateBackground here [updateBackground(true)]

        if (connection != null && presence != null) {

            //  this order might need to change.  will state be needed?

            if (connection.getState() != null && connection.getState().equals(DeviceConnection.STATE_OFFLINE)) {
                updateArrivedAway(String.valueOf(DeviceConnection.STATE_OFFLINE));
                BannerManager.in(getActivity()).removeBanner(NoConnectionBanner.class);
            }

            else if (presence.getPresence().equals(Presence.PRESENCE_ABSENT)) {
                updateArrivedAway(String.valueOf(Presence.PRESENCE_ABSENT));
                BannerManager.in(getActivity()).removeBanner(NoConnectionBanner.class);
            }

            else  if (presence.getPresence().equals(Presence.PRESENCE_PRESENT)) {
                updateArrivedAway(String.valueOf(Presence.PRESENCE_PRESENT));
                BannerManager.in(getActivity()).removeBanner(NoConnectionBanner.class);
            }
            else if (connection.getState() != null && connection.getState().equals(DeviceConnection.STATE_ONLINE)) {
                updateArrivedAway(String.valueOf(DeviceConnection.STATE_ONLINE));
                BannerManager.in(getActivity()).removeBanner(NoConnectionBanner.class);
            }

            updateImageGlow();
        }

    }


    @Override
    public Integer statusSectionLayout() {
        return R.layout.key_fob_status;
    }


    @Override
    public void onShowedFragment() {
        setImageGlowMode(GlowableImageView.GlowMode.OPEN_CLOSE);
        updateState();
        checkConnection();
    }


    @Override
    public boolean shouldGlow() {
        return getPresence().equals(Presence.PRESENCE_ABSENT);
    }


    private String getPresence() {
        Presence presence = getCapability(Presence.class);
        if (presence != null && presence.getPresence() != null) {
            return presence.getPresence();
        }

        logger.warn("Could not determine presence from capability. Presence: {}", presence);
        return Presence.PRESENCE_ABSENT;
    }

}

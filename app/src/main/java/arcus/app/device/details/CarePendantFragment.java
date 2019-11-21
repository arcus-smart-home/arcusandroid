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

import android.os.Bundle;
import androidx.annotation.NonNull;
import android.widget.ImageView;
import android.widget.TextView;

import com.iris.client.capability.Button;
import com.iris.client.capability.DeviceConnection;
import com.iris.client.capability.DevicePower;
import com.iris.client.capability.Presence;
import arcus.app.R;
import arcus.app.common.banners.core.BannerManager;
import arcus.app.common.banners.NoConnectionBanner;
import arcus.app.common.fragments.IShowedFragment;
import arcus.app.common.view.GlowableImageView;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyChangeEvent;


public class CarePendantFragment extends ArcusProductFragment implements IShowedFragment{

    private static final Logger logger = LoggerFactory.getLogger(CarePendantFragment.class);

    private TextView batteryBottomText;
    private ImageView homeIcon;
    private TextView presenceText;

    @NonNull
    public static CarePendantFragment newInstance() {
        CarePendantFragment fragment = new CarePendantFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public Integer topSectionLayout() {
        return R.layout.care_pendant_top;
    }

    @Override
    public void doTopSection() {
        //todo: missing home/away icons
        presenceText = (TextView) topView.findViewById(R.id.care_pendant_presence);
        homeIcon = (ImageView) topView.findViewById(R.id.device_top_home_away);

        updateState();
    }

    @Override
    public void doStatusSection() {
        TextView batteryTopText = (TextView) statusView.findViewById(R.id.top_status_text);
        batteryBottomText = (TextView) statusView.findViewById(R.id.bottom_status_text);
        updatePowerSourceAndBattery(batteryTopText, batteryBottomText);

        setImageGlowMode(GlowableImageView.GlowMode.OPEN_CLOSE);
        updateImageGlow();
    }

    @Override
    public Integer statusSectionLayout() {
        return R.layout.care_pendant_status;
    }

    @Override
    public boolean shouldGlow() {
        return getPresence().equals(Presence.PRESENCE_ABSENT);
    }


    private String getPresence(){
        final Presence presence = getCapability(Presence.class);
        if(presence!=null && presence.getPresence()!=null){
            return presence.getPresence();
        }
        return Presence.PRESENCE_PRESENT;
    }


    private void updateState() {

        DeviceConnection connection = getCapability(DeviceConnection.class);

        Presence presence = getCapability(Presence.class);

        // might need to updateBackground here [updateBackground(true)]

        if (connection != null && presence != null && presenceText != null) {

            //  this order might need to change.  will state be needed?

            if (connection.getState() != null && connection.getState().equals(DeviceConnection.STATE_OFFLINE)) {
                homeIcon.setImageResource(R.drawable.away);
                presenceText.setText(getHomeOrAway(DeviceConnection.STATE_OFFLINE));
                BannerManager.in(getActivity()).removeBanner(NoConnectionBanner.class);
            }

            else if (presence.getPresence().equals(Presence.PRESENCE_ABSENT)) {
                homeIcon.setImageResource(R.drawable.away);
                presenceText.setText(getHomeOrAway(Presence.PRESENCE_ABSENT));
                BannerManager.in(getActivity()).removeBanner(NoConnectionBanner.class);
            }

            else  if (presence.getPresence().equals(Presence.PRESENCE_PRESENT)) {
                homeIcon.setImageResource(R.drawable.home);
                presenceText.setText(getHomeOrAway(Presence.PRESENCE_PRESENT));
                BannerManager.in(getActivity()).removeBanner(NoConnectionBanner.class);
            }
            else if (connection.getState() != null && connection.getState().equals(DeviceConnection.STATE_ONLINE)) {
                homeIcon.setImageResource(R.drawable.home);
                presenceText.setText(getHomeOrAway(DeviceConnection.STATE_ONLINE));
                BannerManager.in(getActivity()).removeBanner(NoConnectionBanner.class);
            }

            updateImageGlow();
        }

    }


    private String getHomeOrAway(@NonNull final String presenceOrState){

        if (presenceOrState.equalsIgnoreCase(DeviceConnection.STATE_OFFLINE)) {
            return getResources().getString(R.string.care_pendant_away);
        }
        else if (presenceOrState.equalsIgnoreCase(Presence.PRESENCE_PRESENT)){
            return getResources().getString(R.string.care_pendant_home);
        }
        else if (presenceOrState.equalsIgnoreCase(Presence.PRESENCE_ABSENT)) {
            return getResources().getString(R.string.care_pendant_away);
        }
        else if (presenceOrState.equalsIgnoreCase(DeviceConnection.STATE_ONLINE)) {
            return getResources().getString(R.string.care_pendant_home);
        }
        else {
            return getResources().getString(R.string.care_pendant_away);
        }
    }


    @Override
    public void propertyUpdated(@NonNull final PropertyChangeEvent event) {
        switch (event.getPropertyName()) {
            case DeviceConnection.ATTR_STATE:
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateState();
                        updateImageGlow();
                    }
                });
                break;
            case DevicePower.ATTR_BATTERY:
                updatePowerSourceAndBattery(null, batteryBottomText);
                break;
            case Button.ATTR_STATE:
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateImageGlow();
                    }
                });

                break;
            case Presence.ATTR_PRESENCE:
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateState();
                    }
                });
                updateImageGlow();
                logger.debug("Care pendant presence changed from {} to {}", event.getOldValue(), event.getNewValue());
                break;
            default:
                // super.propertyUpdated(event);
                break;
        }
    }

    @Override
    public void onShowedFragment() {
        checkConnection();
        updateState();
    }
}

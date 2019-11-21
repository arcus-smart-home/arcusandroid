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

import arcus.cornea.utils.ConversionUtils;
import arcus.cornea.utils.TimeOfDay;
import com.iris.client.capability.Flow;
import com.iris.client.capability.WaterSoftener;
import arcus.app.R;
import arcus.app.common.banners.LowSaltBanner;
import arcus.app.common.banners.core.BannerManager;
import arcus.app.common.fragments.IClosedFragment;
import arcus.app.common.fragments.IShowedFragment;
import arcus.app.common.utils.StringUtils;

import java.beans.PropertyChangeEvent;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;


public class WaterSoftenerFragment extends ArcusProductFragment implements IShowedFragment, IClosedFragment {

    private static final int LOW_SALT_PERCENTAGE = 20;
    @NonNull
    private DateFormat timeParseFormat = new SimpleDateFormat("H:mm:ss", Locale.getDefault());
    @NonNull
    private DateFormat timeDisplayFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
    private TextView saltLevelBottomText;
    private TextView currentLevelBottomText;
    private TextView rechargeText;
    private TextView rechargeTime;

    @NonNull
    public static WaterSoftenerFragment newInstance(){
        WaterSoftenerFragment fragment = new WaterSoftenerFragment();
        return fragment;
    }

    @Override
    public Integer topSectionLayout() {
        return R.layout.water_softener_top_schedule;
    }

    @Override
    public void doTopSection() {
        rechargeText = (TextView) topView.findViewById(R.id.water_softener_top_recharge_text);
        rechargeTime = (TextView) topView.findViewById(R.id.water_softener_top_recharge_time);
    }

    @Override
    public void doStatusSection() {
        View saltView = statusView.findViewById(R.id.salt_status);
        View currentView = statusView.findViewById(R.id.current_status);

        TextView saltLevelTopText = (TextView) saltView.findViewById(R.id.top_status_text);
        saltLevelBottomText = (TextView) saltView.findViewById(R.id.bottom_status_text);

        saltLevelTopText.setText(getString(R.string.water_softener_salt_level));

        TextView currentLevelTopText = (TextView) currentView.findViewById(R.id.top_status_text);
        currentLevelBottomText = (TextView) currentView.findViewById(R.id.bottom_status_text);

        currentLevelTopText.setText(getString(R.string.water_softener_current));

    }

    @Override
    protected void propertyUpdated(@NonNull final PropertyChangeEvent event) {
        switch(event.getPropertyName()) {
            case WaterSoftener.ATTR_RECHARGESTATUS:
            case WaterSoftener.ATTR_RECHARGESTARTTIME:
            case WaterSoftener.ATTR_RECHARGETIMEREMAINING:
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        checkRechargeStatus();
                    }
                });
                break;
            case WaterSoftener.ATTR_CURRENTSALTLEVEL:
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateSaltLevel();
                    }
                });
                break;
            case Flow.ATTR_FLOW:
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateCurrent();
                    }
                });
            default:
                super.propertyUpdated(event);
                break;
        }
    }

    private void updateSaltLevel(){
        saltLevelBottomText.setText(String.format("%s%%", getSaltPercentage()));
    }

    private void updateCurrent() {
        currentLevelBottomText.setText(StringUtils.getSuperscriptSpan(getCurrent() + " ", "GPM"));
    }

    private int getSaltPercentage(){
        WaterSoftener waterSoftener = getCapability(WaterSoftener.class);
        return ConversionUtils.waterSoftenerSaltLevel(waterSoftener);
    }

    private int getCurrent() {
        Flow flow = getCapability(Flow.class);

        if (flow != null && flow.getFlow() != null) {
            return flow.getFlow().intValue();
        }

        return 0;
    }

    private void checkLowSalt(){
        if(getSaltPercentage() <LOW_SALT_PERCENTAGE){
            BannerManager.in(getActivity()).showBanner(new LowSaltBanner());
        }
    }

    private void checkRechargeStatus(){
        WaterSoftener waterSoftener = getCapability(WaterSoftener.class);
        if(waterSoftener !=null && waterSoftener.getRechargeStatus()!=null){
            switch (waterSoftener.getRechargeStatus()){
                case WaterSoftener.RECHARGESTATUS_RECHARGE_SCHEDULED:
                    setRechargeStartTime(waterSoftener.getRechargeStartTime());
                    break;
                case WaterSoftener.RECHARGESTATUS_RECHARGING:
                    setTimeRemaining(waterSoftener.getRechargeTimeRemaining());
                    break;
                case WaterSoftener.RECHARGESTATUS_READY:
                    if(waterSoftener.getRechargeStartTime()>0){
                        setRechargeStartTime(waterSoftener.getRechargeStartTime());
                    }else {
                        rechargeText.setText(WaterSoftener.RECHARGESTATUS_READY);
                        rechargeTime.setText("");
                    }
                    break;
                default:
                    setRechargeStartTime(waterSoftener.getRechargeStartTime());
                    break;
            }
        }
    }

    private void setTimeRemaining(int remaining){
        rechargeText.setText("RECHARGING");
        String timeRemaining = String.format("%d:%02d", remaining/60, remaining%60);
        rechargeTime.setText(timeRemaining);

    }

    private void setRechargeStartTime(int startTime){
        rechargeText.setText(getString(R.string.water_softener_recharge_time_title));
        rechargeTime.setText(formatRechargeHour(startTime));
    }

    private String formatRechargeHour(int hour){
        try {
            TimeOfDay timeOfDay = new TimeOfDay(hour,0,0);
            return timeDisplayFormat.format(timeParseFormat.parse(timeOfDay.toString()));
        }catch (Exception e){
            logger.error("Cannot parse time.  [{}]", hour);
            return "N/A";
        }
    }

    @Override
    public Integer statusSectionLayout() {
        return R.layout.water_softener_status;
    }

    @Override
    public void onShowedFragment() {
        updateSaltLevel();
        updateCurrent();
        checkLowSalt();
        checkRechargeStatus();
        checkConnection();
    }

    @Override
    public void onClosedFragment() {
        BannerManager.in(getActivity()).removeBanner(LowSaltBanner.class);
    }
}

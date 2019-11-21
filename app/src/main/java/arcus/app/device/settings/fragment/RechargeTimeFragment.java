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
package arcus.app.device.settings.fragment;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import arcus.cornea.events.TimeSelectedEvent;
import arcus.cornea.utils.TimeOfDay;
import com.iris.client.capability.WaterSoftener;
import com.iris.client.model.DeviceModel;
import arcus.app.R;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.sequence.SequencedFragment;
import arcus.app.common.models.SessionModelManager;
import arcus.app.common.popups.AMPMTimePopupWithHeader;
import arcus.app.common.utils.CorneaUtils;
import arcus.app.common.view.Version1TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

import de.greenrobot.event.EventBus;


public class RechargeTimeFragment extends SequencedFragment {

    @NonNull
    private DateFormat timeParseFormat = new SimpleDateFormat("H:mm:ss", Locale.getDefault());
    @NonNull
    private DateFormat timeDisplayFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
    private Version1TextView rechargeTime;
    private static final String DEVICE_ID_KEY = "WATER SOFTENER DEVICE ID";
    @Nullable
    private String mDeviceId;
    @Nullable
    private DeviceModel mModel;

    @NonNull
    public static RechargeTimeFragment newInstance(String deviceId){
        RechargeTimeFragment fragment = new RechargeTimeFragment();
        Bundle bundle = new Bundle();
        bundle.putString(DEVICE_ID_KEY, deviceId);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Bundle arguments = getArguments();
        if (arguments != null) {
            mDeviceId = arguments.getString(DEVICE_ID_KEY);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        rechargeTime = (Version1TextView) view.findViewById(R.id.fragment_recharge_time_text);
        RelativeLayout rl = (RelativeLayout) view.findViewById(R.id.fragment_recharge_time);

        rl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AMPMTimePopupWithHeader popup = AMPMTimePopupWithHeader.newInstanceAsHourOnly();
                BackstackManager.getInstance().navigateToFloatingFragment(popup, popup.getClass().getSimpleName(), true);
            }
        });
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        mModel = SessionModelManager.instance().getDeviceWithId(mDeviceId,true);
        WaterSoftener waterSoftener = CorneaUtils.getCapability(mModel,WaterSoftener.class);
        if(waterSoftener !=null && waterSoftener.getRechargeStatus()!=null){
            rechargeTime.setText(formatRechargeTime(waterSoftener.getRechargeStartTime()));
        }
    }


    @Override
    public void onPause() {
        super.onPause();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    public void onEvent(@NonNull TimeSelectedEvent event) {
        try {
            rechargeTime.setText(formatRechargeTime(event.getHourValue()));
            if(mModel !=null){
                final WaterSoftener softener = CorneaUtils.getCapability(mModel, WaterSoftener.class);
                if(softener!=null){
                    softener.setRechargeStartTime(event.getHourValue());
                    mModel.commit();
                }
            }
        }catch (Exception e){
            logger.error("Cannot parse time.  [{}]", event.toString());
        }
    }

    private String formatRechargeTime(int hour){
        try {
            TimeOfDay timeOfDay = new TimeOfDay(hour, 0, 0);
            return timeDisplayFormat.format(timeParseFormat.parse(timeOfDay.toString()));
        }catch (Exception e){
            logger.error("Cannot parse time.  [{}]", hour);
            return "N/A";
        }
    }

    @Override
    public String getTitle() {
        return getString(R.string.water_softener_recharge_time_title);
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_recharge_time;
    }
}

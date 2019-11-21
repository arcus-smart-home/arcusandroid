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

import com.iris.client.ClientEvent;
import com.iris.client.ClientRequest;
import com.iris.client.IrisClientFactory;
import com.iris.client.capability.WaterSoftener;
import com.iris.client.event.ClientFuture;
import com.iris.client.event.Listener;
import com.iris.client.model.DeviceModel;
import com.iris.client.util.Result;
import arcus.app.R;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.sequence.SequencedFragment;
import arcus.app.common.models.SessionModelManager;
import arcus.app.common.popups.ArcusFloatingFragment;
import arcus.app.common.utils.CorneaUtils;
import arcus.app.common.view.Version1Button;
import arcus.app.common.view.Version1ButtonColor;
import arcus.app.subsystems.alarm.AlertFloatingFragment;


public class RechargeNowFragment extends SequencedFragment {

    private Version1Button rechargeNowBtn;
    private static final String DEVICE_ID_KEY = "WATER SOFTENER DEVICE ID";
    @Nullable
    private String mDeviceId;
    @Nullable
    private DeviceModel mModel;

    @NonNull
    public static RechargeNowFragment newInstance(String deviceId){
        RechargeNowFragment fragment = new RechargeNowFragment();
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
        rechargeNowBtn = (Version1Button) view.findViewById(R.id.fragment_recharge_now_btn);
        rechargeNowBtn.setColorScheme(Version1ButtonColor.WHITE);
        rechargeNowBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkRechargeStatus();
            }
        });
        return view;
    }

    private void checkRechargeStatus(){
        mModel = SessionModelManager.instance().getDeviceWithId(mDeviceId,true);
        WaterSoftener waterSoftener = CorneaUtils.getCapability(mModel, WaterSoftener.class);
        if(waterSoftener !=null && waterSoftener.getRechargeStatus()!=null){
            if(waterSoftener.getRechargeStatus().equals(WaterSoftener.RECHARGESTATUS_RECHARGING)){
                promptAlreadyRecharging();
            }else{
                promptAreYourSure(waterSoftener);
            }
        }
    }

    private void promptAreYourSure(final WaterSoftener waterSoftener){
        final AlertFloatingFragment popup = AlertFloatingFragment.newInstance(
                getString(R.string.recharge_now_are_you_sure),
                getString(R.string.recharge_now_instr),
                getString(R.string.recharge_now_continue_btn),
                getString(R.string.recharge_now_cancel_btn),
                new AlertFloatingFragment.AlertButtonCallback() {
                    @Override
                    public boolean topAlertButtonClicked() {
                        //todo recharge now
                        ClientRequest request = new ClientRequest();
                        request.setAddress(waterSoftener.getAddress());
                        request.setCommand("watersoftener:rechargeNow");

                        ClientFuture<ClientEvent> future = IrisClientFactory.getClient().request(request);
                        future.onCompletion(new Listener<Result<ClientEvent>>() {
                            @Override
                            public void onEvent(Result<ClientEvent> clientEventResult) {
                                logger.debug("Got recharge now response:{}", clientEventResult);
                            }
                        });
                        return true;
                    }

                    @Override
                    public boolean bottomAlertButtonClicked() {
                        return true;
                    }
                }
        );

        showPopup(popup);
    }

    private void promptAlreadyRecharging(){
        AlertFloatingFragment popup = AlertFloatingFragment.newInstance(
                getString(R.string.recharge_now_already_recharging),
                getString(R.string.recharge_now_already_recharging_des),
                null, null, null);
        showPopup(popup);
    }

    private <T extends ArcusFloatingFragment> void showPopup(@NonNull T popup) {
        BackstackManager.getInstance().navigateToFloatingFragment(popup, popup.getClass().getSimpleName(), true);
    }

    @Override
    public String getTitle() {
        return getString(R.string.water_softener_recharge_now_title);
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_recharge_now;
    }
}

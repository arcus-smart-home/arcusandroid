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

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.iris.client.capability.WaterHardness;
import com.iris.client.model.DeviceModel;
import arcus.app.R;
import arcus.app.common.sequence.SequencedFragment;
import arcus.app.common.models.SessionModelManager;
import arcus.app.common.utils.CorneaUtils;
import arcus.app.common.utils.GlobalSetting;
import arcus.app.common.view.Version1Button;
import arcus.app.common.view.Version1ButtonColor;


public class WaterHardnessLevelFragment extends SequencedFragment {

    private Version1Button buyBtn;
    private TextView waterHardnessInstr;
    private static final String DEVICE_ID_KEY = "WATER SOFTENER DEVICE ID";
    @Nullable
    private String mDeviceId;
    @Nullable
    private DeviceModel mModel;

    @NonNull
    public static WaterHardnessLevelFragment newInstance(String deviceId){
        WaterHardnessLevelFragment fragment = new WaterHardnessLevelFragment();
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
        buyBtn = (Version1Button) view.findViewById(R.id.fragment_water_hardness_buy_btn);
        buyBtn.setColorScheme(Version1ButtonColor.WHITE);
        buyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(GlobalSetting.CHECK_WATER_HARDNESS_URL)));
            }
        });
        waterHardnessInstr = (TextView) view.findViewById(R.id.fragment_water_hardness_instr);
        getHardnessLevel();
        return view;
    }

    private void getHardnessLevel(){
        String hardnessText = waterHardnessInstr.getText().toString();
        mModel = SessionModelManager.instance().getDeviceWithId(mDeviceId,true);
        WaterHardness waterHardness = CorneaUtils.getCapability(mModel, WaterHardness.class);
        if(waterHardness !=null && waterHardness.getHardness()!=null){
            waterHardnessInstr.setText(hardnessText.replace("<device default value>",
                    String.valueOf(waterHardness.getHardness())));
        }
    }

    @Override
    public String getTitle() {
        return getString(R.string.water_softener_water_hardness_level_title);
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_water_hardness_level;
    }
}

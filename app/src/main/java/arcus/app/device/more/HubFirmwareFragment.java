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
package arcus.app.device.more;

import android.os.Bundle;
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import com.iris.client.capability.HubAdvanced;
import com.iris.client.capability.HubNetwork;
import com.iris.client.model.HubModel;
import arcus.app.R;
import arcus.app.common.fragments.BaseFragment;
import arcus.app.common.models.SessionModelManager;
import arcus.app.common.view.Version1Button;
import arcus.app.common.view.Version1TextView;


public class HubFirmwareFragment extends BaseFragment {
    private Version1TextView hubIPAddress;
    private Version1TextView hubFirmwareAddress;
    private Version1Button updateBtn;

    @NonNull
    public static HubFirmwareFragment newInstance() {
        return new HubFirmwareFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        updateBtn = (Version1Button) view.findViewById(R.id.hub_firmware_update_btn);
        updateBtn.setVisibility(View.GONE);

        hubIPAddress = (Version1TextView) view.findViewById(R.id.hub_firmware_internal_ip_address);
        hubFirmwareAddress = (Version1TextView) view.findViewById(R.id.hub_firmware_version);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle(getTitle());
        HubModel hubModel = SessionModelManager.instance().getHubModel();
        if (hubModel == null) {
            return;
        }

        if (hubModel.getCaps().contains(HubNetwork.NAMESPACE)) {
            HubNetwork hubNetwork = (HubNetwork) hubModel;
            hubIPAddress.setText(hubNetwork.getIp());
        }

        if (hubModel.getCaps().contains(HubAdvanced.NAMESPACE)) {
            HubAdvanced hubAdvanced = (HubAdvanced) hubModel;
            hubFirmwareAddress.setText(String.format(getString(R.string.hub_version_text), hubAdvanced.getOsver()));
        }
    }

    @NonNull
    @Override
    public String getTitle() {
        return "FIRMWARE";
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_hub_firmware;
    }
}

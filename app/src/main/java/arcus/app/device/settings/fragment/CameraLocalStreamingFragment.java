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
import androidx.annotation.Nullable;
import android.view.View;
import android.widget.TextView;

import arcus.app.R;
import arcus.app.common.error.ErrorManager;
import arcus.app.common.fragments.BaseFragment;
import arcus.app.device.settings.fragment.contract.CameraLocalStreaming;
import arcus.app.device.settings.fragment.presenter.CameraLocalStreamingPresenter;

public class CameraLocalStreamingFragment extends BaseFragment implements CameraLocalStreaming.UserView {
    private static final String DEVICE_ID = "DEVICE_ID";

    String deviceID;
    TextView usernameView, passwordView, ipView;
    CameraLocalStreaming.Presenter presenter;

    public static CameraLocalStreamingFragment newInstance(String deviceID) {
        CameraLocalStreamingFragment fragment = new CameraLocalStreamingFragment();
        Bundle args = new Bundle(1);
        args.putString(DEVICE_ID, deviceID);
        fragment.setArguments(args);
        return fragment;
    }

    @Override public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() == null) {
            return;
        }

        deviceID = getArguments().getString(DEVICE_ID, "");
    }

    @Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        usernameView = (TextView) view.findViewById(R.id.streaming_username);
        passwordView = (TextView) view.findViewById(R.id.streaming_password);
        ipView = (TextView) view.findViewById(R.id.streaming_ip);
    }

    @Override public void onResume() {
        super.onResume();
        presenter = new CameraLocalStreamingPresenter();
        presenter.setView(this);
        presenter.getCredentials(deviceID);
        setTitle();
    }

    @Override public void showCredentials(String username, String password, String ip) {
        usernameView.setText(username);
        passwordView.setText(password);
        ipView.setText(ip);
    }

    @Override public void onDestroy() {
        super.onDestroy();
        presenter = null;
    }

    @Override public void showError(Throwable throwable) {
        ErrorManager.in(getActivity()).showGenericBecauseOf(throwable);
    }

    @Nullable @Override public String getTitle() {
        return getString(R.string.camera_local_stream_title);
    }

    @Override public Integer getLayoutId() {
        return R.layout.local_streaming_fragment;
    }
}

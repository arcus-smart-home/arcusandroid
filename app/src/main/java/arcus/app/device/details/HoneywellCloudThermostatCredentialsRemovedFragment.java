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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import arcus.app.R;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.fragments.BaseFragment;
import arcus.app.common.view.Version1Button;
import arcus.app.common.view.Version1ButtonColor;
import arcus.app.common.view.Version1TextView;
import arcus.app.device.pairing.specialty.honeywelltcc.HoneywellWebView;
import arcus.app.device.pairing.specialty.honeywelltcc.HoneywellWebViewClientSequence;


public class HoneywellCloudThermostatCredentialsRemovedFragment extends BaseFragment implements HoneywellWebViewClientSequence.Callback {
    IHoneywellCredentials callback;
    HoneywellCloudThermostatCredentialsRemovedFragment frag;
    @NonNull
    public static HoneywellCloudThermostatCredentialsRemovedFragment newInstance() {
        return new HoneywellCloudThermostatCredentialsRemovedFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(getLayoutId(), container, false);

        frag = this;
        Version1TextView title = (Version1TextView) view.findViewById(R.id.title);
        title.setText(getString(R.string.cloud_honeywell_credentials_title));
        Version1TextView description = (Version1TextView) view.findViewById(R.id.description);
        description.setText(getString(R.string.cloud_honeywell_credentials_description));

        Version1Button buttonNext = (Version1Button) view.findViewById(R.id.button);
        buttonNext.setText(getString(R.string.pairing_next));
        buttonNext.setColorScheme(Version1ButtonColor.WHITE);
        buttonNext.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                HoneywellWebView webView = new HoneywellWebView();
                webView.setCallback(HoneywellCloudThermostatCredentialsRemovedFragment.this);
                BackstackManager.getInstance().navigateToFragment(webView, true);
                return false;
            }
        });

        return view;
    }

    @Nullable
    @Override
    public String getTitle() {
        return getString(R.string.cloud_honeywell_credentials);
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_title_description_button;
    }

    @Override
    public void redirected() {
        if (callback != null) {
            callback.authComplete();
        }
    }

    @Override
    public void pageLoading() {}

    @Override
    public void pageLoaded() {}

    @Override
    public void errorEncountered(int errorCode, String description, String failingUrl) {
        if (callback != null) {
            callback.errorEncountered(errorCode, description, failingUrl);
        }
    }

    public void setCallback(HoneywellCloudThermostatCredentialsRemovedFragment.IHoneywellCredentials callback) {
        this.callback = callback;
    }

    public interface IHoneywellCredentials {
        void authComplete();
        void errorEncountered(int errorCode, String description, String failingUrl);
    }
}

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
package arcus.app.common.error.popup;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import arcus.app.R;
import arcus.app.common.fragments.BaseFragment;


public class NoNetworkConnectionPopup extends BaseFragment {

    @NonNull
    public static NoNetworkConnectionPopup newInstance() {
        return new NoNetworkConnectionPopup();
    }

    @Nullable
    @Override
    public String getTitle() {
        return null;
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_no_network_connection;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        hideActionBar();
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public boolean onBackPressed() {
        // Block back presses.
        return true;
    }

    @Override
    public void onPause() {
        showActionBar();
        super.onPause();
    }
}

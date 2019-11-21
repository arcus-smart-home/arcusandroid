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
package arcus.app.common.wifi;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import arcus.app.R;
import arcus.app.common.sequence.SequencedFragment;
import arcus.app.common.validation.SsidValidator;
import arcus.app.common.view.Version1EditText;


public class ManualSsidEntryFragment extends SequencedFragment<ManualSsidEntrySequenceController> {

    private Version1EditText ssid;
    private Version1EditText confirmSsid;

    public static ManualSsidEntryFragment newInstance() {
        return new ManualSsidEntryFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        ssid = (Version1EditText) view.findViewById(R.id.ssid);
        confirmSsid = (Version1EditText) view.findViewById(R.id.confirm_ssid);

        return view;
    }

    @Override
    public Integer getMenuId() {
        return R.menu.menu_done;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (new SsidValidator(ssid, confirmSsid).isValid()) {
            endSequence(true, ssid.getText().toString());
        }

        return true;
    }

    @Nullable
    @Override
    public String getTitle() {
        return getString(R.string.swann_ssid_info);
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_manual_ssid_entry;
    }
}

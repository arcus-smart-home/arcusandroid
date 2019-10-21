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
package arcus.app.device.pairing.specialty.honeywelltcc;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import arcus.app.R;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.view.Version1Button;
import arcus.app.common.view.Version1ButtonColor;
import arcus.app.dashboard.HomeFragment;
import arcus.app.device.pairing.catalog.controller.ProductCatalogFragmentController;
import arcus.app.device.pairing.steps.AbstractPairingStepFragment;

public class HoneywellError extends AbstractPairingStepFragment {

    public static HoneywellError newInstance() {
        return new HoneywellError();
    }

    @Nullable @Override public View onCreateView(
          LayoutInflater inflater,
          @Nullable ViewGroup container,
          @Nullable Bundle savedInstanceState
    ) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        if (view == null) {
            return view;
        }

        Version1Button button = (Version1Button) view.findViewById(R.id.honeywell_retry_button);
        if (button == null) {
            return view;
        }

        button.setColorScheme(Version1ButtonColor.WHITE);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                theEndAll();
            }
        });
        return view;
    }

    @Override
    public boolean onBackPressed() {
        theEndAll(); // Don't let back take em' back.
        return true;
    }

    private void theEndAll() {
        ProductCatalogFragmentController.instance().stopPairing();
        BackstackManager.getInstance().navigateBackToFragment(HomeFragment.newInstance());
    }

    @Nullable @Override public String getTitle() {
        return null;
    }

    @Override public Integer getLayoutId() {
        return R.layout.fragment_honeywell_pairing_error;
    }
}

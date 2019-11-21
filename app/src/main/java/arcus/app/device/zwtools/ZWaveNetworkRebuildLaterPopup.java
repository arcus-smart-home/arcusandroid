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
package arcus.app.device.zwtools;

import androidx.annotation.Nullable;
import android.view.View;

import arcus.app.R;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.popups.ArcusFloatingFragment;
import arcus.app.common.view.Version1Button;
import arcus.app.dashboard.HomeFragment;



public class ZWaveNetworkRebuildLaterPopup extends ArcusFloatingFragment {

    private Version1Button continueToDashboard;

    public static ZWaveNetworkRebuildLaterPopup newInstance() {
        return new ZWaveNetworkRebuildLaterPopup();
    }

    @Nullable
    @Override
    public String getTitle() {
        return getString(R.string.zwtools_rebuild_later);
    }

    @Override
    public void setFloatingTitle() {
        title.setVisibility(View.GONE);
        setTitle();
    }

    @Override
    public void doContentSection() {
        continueToDashboard = (Version1Button) contentView.findViewById(R.id.continue_button);

        continueToDashboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BackstackManager.getInstance().navigateBack();      // Close this popup
                BackstackManager.getInstance().navigateBackToFragment(HomeFragment.class);
            }
        });
    }

    @Override
    public Integer contentSectionLayout() {
        return R.layout.fragment_zwave_network_rebuild_later;
    }
}

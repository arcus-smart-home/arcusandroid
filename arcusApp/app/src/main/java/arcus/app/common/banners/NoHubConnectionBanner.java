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
package arcus.app.common.banners;

import android.view.View;

import com.iris.client.model.HubModel;
import arcus.app.R;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.banners.core.ClickableBanner;
import arcus.app.common.models.SessionModelManager;
import arcus.app.device.details.DeviceDetailParentFragment;


public class NoHubConnectionBanner extends ClickableBanner {

    public NoHubConnectionBanner() {
        super(R.layout.hub_no_connection_banner);

        setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final HubModel hubModel = SessionModelManager.instance().getHubModel();
                if (hubModel != null) {
                    BackstackManager.getInstance()
                            .navigateToFragment(DeviceDetailParentFragment.newInstance(0), true);
                }
            }
        });
    }
}

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

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;

import arcus.app.R;
import arcus.app.common.banners.core.ClickableBanner;
import arcus.app.common.utils.GlobalSetting;


public class NoConnectionBanner extends ClickableBanner {
    Boolean isHub = false;
    Intent launchBrowser;

    public NoConnectionBanner() {
        super(R.layout.no_connection_banner);
    }

    public NoConnectionBanner(Boolean isHub) {
        super(R.layout.no_connection_banner);
        this.isHub = isHub;
    }

    @Override
    public View getBannerView(ViewGroup parent) {
        if(isHub) {
            launchBrowser = new Intent(Intent.ACTION_VIEW, GlobalSetting.NO_CONNECTION_HUB_SUPPORT_URL);
        } else {
            launchBrowser = new Intent(Intent.ACTION_VIEW, GlobalSetting.NO_CONNECTION_SUPPORT_URL);
        }

        setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Activity a = getActivity();
                if (a != null){
                    a.startActivity(launchBrowser);
                }
            }
        });

        return super.getBannerView(parent);
    }
}

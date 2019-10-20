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
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;

import arcus.app.R;
import arcus.app.common.banners.core.ClickableBanner;


public class LutronBridgeErrorBanner extends ClickableBanner {

    Intent launchBrowser;
    String supportUrl;

    public LutronBridgeErrorBanner(String supportUrl) {
        super(R.layout.banner_lutron_bridge_error);
        this.supportUrl = supportUrl;
    }


    @Override
    public View getBannerView(ViewGroup parent) {

        launchBrowser = new Intent(Intent.ACTION_VIEW, Uri.parse(supportUrl));

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

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
import androidx.core.content.ContextCompat;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import arcus.app.R;
import arcus.app.common.banners.core.ClickableBanner;
import arcus.app.common.utils.GlobalSetting;


public class HueNotPairedBanner extends ClickableBanner {
    Intent launchBrowser;
    String message = "";
    String uri = "";
    TextView bannerTitle;
    TextView link;
    ImageView image;
    ImageView chevron;

    int backgroundColor = 0;
    boolean lightMode = false;

    public HueNotPairedBanner() {
        super(R.layout.hue_not_paired);
    }

    public HueNotPairedBanner(String message, String uri) {
        super(R.layout.hue_not_paired);
        this.message = message;
        this.uri = uri;
    }

    public HueNotPairedBanner(String message, String uri, int backgroundColor) {
        super(R.layout.hue_not_paired);
        this.message = message;
        this.uri = uri;
        this.backgroundColor = backgroundColor;
    }

    public HueNotPairedBanner(String message, String uri, int backgroundColor, boolean lightMode) {
        super(R.layout.hue_not_paired);
        this.message = message;
        this.uri = uri;
        this.backgroundColor = backgroundColor;
        this.lightMode = lightMode;
    }

    @Override
    public View getBannerView(ViewGroup parent) {
        View view = super.getBannerView(parent);
        bannerTitle = (TextView) view.findViewById(R.id.banner_title);
        link = (TextView) view.findViewById(R.id.alert_close_btn);
        image = (ImageView) view.findViewById(R.id.image);
        chevron = (ImageView) view.findViewById(R.id.chevron);

        if(lightMode) {
            bannerTitle.setTextColor(ContextCompat.getColor(getActivity(), R.color.black));
            link.setTextColor(ContextCompat.getColor(getActivity(), R.color.black_with_60));
            image.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.icon_alert_noconnection_outline));
            chevron.setColorFilter(ContextCompat.getColor(getActivity(), R.color.black));
        }

        if(backgroundColor != 0) {
            view.setBackgroundColor(backgroundColor);
        }

        if(!TextUtils.isEmpty(message)) {
            bannerTitle.setText(message);
        }

        if(TextUtils.isEmpty(uri)) {
            launchBrowser = new Intent(Intent.ACTION_VIEW, Uri.parse(GlobalSetting.HUE_IMPROPERLY_PAIRED_DEVICE_URL));
            setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Activity a = getActivity();
                    if (a != null){
                        a.startActivity(launchBrowser);
                    }
                }
            });
        } else {
            launchBrowser = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Activity a = getActivity();
                    if (a != null){
                        a.startActivity(launchBrowser);
                    }
                }
            });
        }


        return view;
    }
}

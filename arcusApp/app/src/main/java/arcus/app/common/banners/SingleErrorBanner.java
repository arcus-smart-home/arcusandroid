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

import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import arcus.app.R;
import arcus.app.common.banners.core.ClickableBanner;
import arcus.app.common.utils.GlobalSetting;

public class SingleErrorBanner extends ClickableBanner {

    private String bannerText;
    private String customNumber;
    private String buttonText;
    private Uri buttonUri;

    public SingleErrorBanner(String bannerText) {
        super(R.layout.single_error_banner);
        this.bannerText = bannerText;
    }

    public SingleErrorBanner(String bannerText, String customNumber) {
        super(R.layout.single_error_banner);
        this.bannerText = bannerText;
        this.customNumber = customNumber;
    }

    public SingleErrorBanner(String bannerText, String buttonText, Uri buttonUri) {
        super(R.layout.single_error_banner);
        this.customNumber = "";
        this.bannerText = bannerText;
        this.buttonText = buttonText;
        this.buttonUri = buttonUri;
    }

    @Override
    public View getBannerView(ViewGroup parent) {
        View view = super.getBannerView(parent);

        TextView errorText = (TextView) view.findViewById(R.id.error_message);
        errorText.setText(bannerText);

        if(customNumber == null) {
            //water heater
            LinearLayout callArcus = (LinearLayout) view.findViewById(R.id.call_arcus);
            callArcus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent call = new Intent(Intent.ACTION_DIAL, GlobalSetting.WATERHEATER_HEATER_SUPPORT_NUMBER);
                    if (getActivity() != null) {
                        getActivity().startActivity(call);
                    }
                }
            });
        }
        else if(customNumber.equals("")) {
            LinearLayout callArcus = (LinearLayout) view.findViewById(R.id.call_arcus);

            if(TextUtils.isEmpty(buttonText)) {
                callArcus.setVisibility(View.GONE);
            }
            else {
                TextView shopButton = (TextView) view.findViewById(R.id.call_number);
                shopButton.setText(buttonText);
                shopButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        getActivity().startActivity(new Intent(Intent.ACTION_VIEW, buttonUri));
                    }
                });
            }
        } else {
            TextView phoneNumber = (TextView) view.findViewById(R.id.call_number);
            phoneNumber.setText(customNumber);
            LinearLayout callArcus = (LinearLayout) view.findViewById(R.id.call_arcus);
            callArcus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String contactString = customNumber.replace("-", "");
                    Uri callThis = Uri.parse("tel:"+contactString);
                    Intent call = new Intent (Intent.ACTION_DIAL, callThis);
                    if(getActivity()!=null){
                        getActivity().startActivity(call);
                    }
                }
            });
        }

        return view;
    }


}

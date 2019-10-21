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
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import arcus.app.ArcusApplication;
import arcus.app.R;
import arcus.app.common.banners.core.ClickableBanner;
import arcus.app.common.utils.GlobalSetting;

public class ServiceSuspendedBanner extends ClickableBanner {
    public ServiceSuspendedBanner() {
        super(R.layout.call_support_banner_with_text);
    }

    @Override public View getBannerView(ViewGroup parent) {
        View v = super.getBannerView(parent);
        if (v == null ) {
            return null;
        }

        TextView messageText = (TextView) v.findViewById(R.id.message_text);
        if (messageText != null) {
            messageText.setText(R.string.call_support_service_suspended);
        }

        View getSupport = v.findViewById(R.id.get_support_click);
        if (getSupport == null) {
            return v;
        }

        getSupport.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                Intent todo = new Intent(Intent.ACTION_DIAL, GlobalSetting.SUPPORT_NUMBER_URI);
                if (todo.resolveActivity(ArcusApplication.getContext().getPackageManager()) != null) {
                    ArcusApplication.getContext().startActivity(todo);
                }
            }
        });
        return v;
    }
}

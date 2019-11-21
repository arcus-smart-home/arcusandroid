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
package arcus.app.common.popups;

import androidx.annotation.Nullable;
import android.view.View;

import arcus.app.R;
import arcus.app.common.utils.ActivityUtils;
import arcus.app.common.utils.GlobalSetting;
import arcus.app.common.view.Version1Button;

public class UnservicedAddressPopup extends ArcusFloatingFragment {

    public static UnservicedAddressPopup newInstance () {
        return new UnservicedAddressPopup();
    }

    @Override
    public void onResume() {
        super.onResume();
        showFullScreen(true);
    }

    @Override
    public void setFloatingTitle() {
        title.setVisibility(View.GONE);
    }

    @Override
    public void doContentSection() {
        Version1Button callSupportButton = (Version1Button) contentView.findViewById(R.id.call_support_button);
        callSupportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityUtils.launchUrl(GlobalSetting.SUPPORT_NUMBER_URI);
            }
        });
    }

    @Override
    public Integer contentSectionLayout() {
        return R.layout.popup_unserviced_address;
    }

    @Override
    public Integer floatingBackgroundColor() {
        return getResources().getColor(R.color.pink_banner);
    }

    @Nullable
    @Override
    public String getTitle() {
        return "";
    }
}

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
import android.view.ViewGroup;
import android.widget.TextView;

import arcus.app.ArcusApplication;
import arcus.app.R;
import arcus.app.common.banners.core.AbstractBanner;



public class TemperatureLockBanner extends AbstractBanner {

    private final int coolSetpointF;
    private final int heatSetpointF;

    public TemperatureLockBanner(int heatSetpointF, int coolSetpointF) {
        super(R.layout.banner_temperature_lock);

        this.coolSetpointF = coolSetpointF;
        this.heatSetpointF = heatSetpointF;
    }

    @Override
    public View getBannerView(ViewGroup parent) {
        View bannerView = super.getBannerView(parent);

        TextView lockText = (TextView) bannerView.findViewById(R.id.temperature_lock_text);
        lockText.setText(ArcusApplication.getContext().getString(R.string.thermostat_temp_locked, heatSetpointF, coolSetpointF));

        return bannerView;
    }

}

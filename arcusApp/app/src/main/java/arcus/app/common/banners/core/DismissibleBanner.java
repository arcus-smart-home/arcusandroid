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
package arcus.app.common.banners.core;

import android.view.View;
import android.view.ViewGroup;

import arcus.app.R;


public abstract class DismissibleBanner extends AbstractBanner {

    public DismissibleBanner(int viewResourceId) {
        super(viewResourceId);
    }

    @Override
    public View getBannerView(ViewGroup parent) {
        if (getActivity() == null) {
            throw new IllegalStateException("Please call setActivity() first.");
        }

        View view = getActivity().getLayoutInflater().inflate(getViewResourceId(), parent, false);

        final View closeBtn = view.findViewById(R.id.alert_close_btn);
        if (closeBtn != null) {
            view.bringToFront();
            closeBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (getBannerAdapter() != null) {
                        getBannerAdapter().remove(DismissibleBanner.this);
                    }
                }
            });
        }

        return view;
    }

}

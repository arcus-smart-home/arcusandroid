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

import arcus.app.R;
import arcus.app.common.banners.core.ClickableBanner;



public class ClickableAbstractTextBanner extends ClickableBanner {

    private final int errorTextResId;
    private final int abstractTextResId;

    public ClickableAbstractTextBanner(int errorTextResId, int abstractTextResId) {
        super(R.layout.banner_clickable_abstract);

        this.errorTextResId = errorTextResId;
        this.abstractTextResId = abstractTextResId;
    }

    @Override
    public View getBannerView(ViewGroup parent) {
        View view = super.getBannerView(parent);

        TextView errorText = (TextView) view.findViewById(R.id.primary_text);
        TextView abstractText = (TextView) view.findViewById(R.id.abstract_text);

        errorText.setText(getActivity().getString(errorTextResId));
        abstractText.setText(getActivity().getString(abstractTextResId));

        return view;
    }

}

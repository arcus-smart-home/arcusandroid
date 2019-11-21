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
package arcus.app.device.pairing.catalog;

import androidx.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import arcus.app.R;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.image.ImageManager;
import arcus.app.common.image.picasso.transformation.BlackWhiteInvertTransformation;
import arcus.app.common.image.picasso.transformation.Invert;
import arcus.app.common.popups.ArcusFloatingFragment;
import arcus.app.device.pairing.catalog.controller.ProductCatalogSequenceController;


public class HubRequiredPopup extends ArcusFloatingFragment {

    private ImageView hubIcon;
    private LinearLayout noHubClickRegion;

    public static HubRequiredPopup newInstance () {
        return new HubRequiredPopup();
    }

    @Override
    public void setFloatingTitle() {
        title.setVisibility(View.GONE);
    }

    @Override
    public void doContentSection() {
        showFullScreen(true);
        floatingContainer.setBackgroundResource(R.drawable.purple_blue_gradient);

        hubIcon = (ImageView) contentView.findViewById(R.id.hub_icon);
        noHubClickRegion = (LinearLayout) contentView.findViewById(R.id.no_hub_click_region);

        ImageManager.with(getActivity())
                .putDrawableResource(R.drawable.add_hub)
                .withTransform(new BlackWhiteInvertTransformation(Invert.BLACK_TO_WHITE))
                .into(hubIcon)
                .execute();

        ImageManager.with(getActivity())
                .putDrawableResource(R.drawable.button_close_box_white)
                .into(closeBtn)
                .execute();

        noHubClickRegion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Close this sheet and show the "paired-down" product catalog (with only hubless devices present)
                BackstackManager.getInstance().navigateBack();
                new ProductCatalogSequenceController().startSequence(getActivity(), null, true);
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        showFullScreen(false);
    }

    @Override
    public Integer contentSectionLayout() {
        return R.layout.popup_hub_required;
    }

    @Nullable
    @Override
    public String getTitle() {
        return null;        // Sheet has no title
    }
}

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
package arcus.app.subsystems.learnmore.view;

import android.content.Context;
import androidx.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import arcus.cornea.controller.SubscriptionController;
import arcus.app.R;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.utils.ActivityUtils;
import arcus.app.common.view.DashboardFlipViewHolder;
import arcus.app.common.view.Version1TextView;
import arcus.app.dashboard.settings.services.ServiceCard;
import arcus.app.subsystems.care.fragment.CareDevicesRequiredFragment;
import arcus.app.subsystems.learnmore.cards.CardType;
import arcus.app.subsystems.learnmore.cards.LearnMoreCard;

;


public class LearnMoreCardView extends DashboardFlipViewHolder {

    private ImageView serviceImage;
    private Version1TextView serviceName;
    private TextView additionalInfo;
    private Context context;
    private ImageView proBadge;

    public LearnMoreCardView(View view) {
        super(view);
        serviceImage = (ImageView) view.findViewById(R.id.service_image);
        serviceName = (Version1TextView) view.findViewById(R.id.service_name);
        additionalInfo = (TextView) view.findViewById(R.id.additional_text);
        proBadge = (ImageView) view.findViewById(R.id.promon_badge);

        this.context = view.getContext();
    }

    public void build(@NonNull final LearnMoreCard card) {
        proBadge.setVisibility(card.isPro() ? View.VISIBLE : View.GONE);
        serviceName.setText(card.getServiceTitle());
        serviceImage.setImageResource(card.getServiceIconId());
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (card.getServiceType() == CardType.CARE) {
                    if (SubscriptionController.isPremiumOrPro()) {
                        BackstackManager.getInstance().navigateToFragment(CareDevicesRequiredFragment.newInstance(), true);
                    } else {
                        ActivityUtils.launchShopCareNow();
                    }
                } else if (card.getServiceCard() == ServiceCard.SECURITY_ALARM) {
                    // Removed ProMonitoring hook here, saving conditional for future use
                } else if (card.getServiceType() == CardType.LIGHTS) {
                    ActivityUtils.launchShopLightsNSwitchesNow();
                } else if (card.getServiceType() == CardType.CLIMATE) {
                    ActivityUtils.launchShopClimateNow();
                } else if (card.getServiceType() == CardType.DOORS) {
                    ActivityUtils.launchShopDoorsNLocksNow();
                } else if (card.getServiceType() == CardType.HOME) {
                    ActivityUtils.launchShopHomeNFamilyNow();
                } else if (card.getServiceType() == CardType.CAMERAS) {
                    ActivityUtils.launchShopCamerasNow();
                } else if (card.getServiceType() == CardType.LAWN) {
                    ActivityUtils.launchShopLawnNGardenNow();
                } else if (card.getServiceType() == CardType.WATER) {
                    ActivityUtils.launchShopWaterNow();
                } else {
                    ActivityUtils.launchLearnMore();
                }
            }
        });
    }
}

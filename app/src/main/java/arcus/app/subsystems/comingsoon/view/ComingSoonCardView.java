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
package arcus.app.subsystems.comingsoon.view;

import android.content.Context;
import androidx.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import arcus.app.R;
import arcus.app.common.view.DashboardFlipViewHolder;
import arcus.app.common.view.Version1TextView;
import arcus.app.subsystems.comingsoon.cards.ComingSoonCard;

public class ComingSoonCardView extends DashboardFlipViewHolder {

    ImageView serviceImage;
    Version1TextView serviceName;
    TextView additionalInfo;
    Context context;

    public ComingSoonCardView(View view) {
        super(view);
        serviceImage = (ImageView) view.findViewById(R.id.service_image);
        serviceName = (Version1TextView) view.findViewById(R.id.service_name);
        additionalInfo = (TextView) view.findViewById(R.id.additional_text);
        this.context = view.getContext();
    }

    public void build(@NonNull ComingSoonCard card) {
        serviceName.setText(card.getServiceTitle());
        serviceImage.setImageResource(card.getServiceIconId());

        /*CardView cardView = (CardView) findViewById(R.id.cardView);
        if (cardView != null) {
            cardView.setCardBackgroundColor(getContext().getResources().getColor(R.color.overlay_white_with_10));
        }
        if (card.isDividerShown()) {
            showDivider();
        }

        TextView serviceTitleView = (TextView) findViewById(R.id.service_title);
        TextView serviceDescriptionView = (TextView) findViewById(R.id.service_description);
        ImageView serviceIconView = (ImageView) findViewById(R.id.service_icon);
        TextView comingSoonButton = (TextView) findViewById(R.id.coming_soon_button);

        serviceTitleView.setText(card.getServiceTitle());
        serviceDescriptionView.setText(card.getServiceDescription());

        final Context context = getContext();
        comingSoonButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // do nothing
            }
        });

        serviceIconView.setImageResource(card.getServiceIconId());*/
    }
}

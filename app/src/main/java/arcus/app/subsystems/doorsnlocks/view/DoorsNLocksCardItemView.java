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
package arcus.app.subsystems.doorsnlocks.view;

import android.content.Context;
import androidx.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;

import arcus.cornea.subsystem.doorsnlocks.model.StateSummary;
import arcus.app.R;
import arcus.app.common.view.DashboardFlipViewHolder;
import arcus.app.common.view.Version1TextView;
import arcus.app.subsystems.doorsnlocks.cards.DoorsNLocksCard;

import java.util.Locale;


public class DoorsNLocksCardItemView extends DashboardFlipViewHolder {

    ImageView serviceImage;
    Version1TextView serviceName;
    Context context;

    private View lockContainer;
    private View garageContainer;
    private View doorContainer;

    private ImageView lockIcon;
    private ImageView garageIcon;
    private ImageView doorIcon;

    private Version1TextView lockSummary;
    private Version1TextView garageSummary;
    private Version1TextView doorSummary;

    public DoorsNLocksCardItemView(View view) {
        super(view);
        serviceImage = (ImageView) view.findViewById(R.id.service_image);
        serviceName = (Version1TextView) view.findViewById(R.id.service_name);
        context = view.getContext();

        lockIcon = (ImageView) view.findViewById(R.id.card_lock_icon);
        lockSummary = (Version1TextView) view.findViewById(R.id.card_lock_summary);

        garageIcon = (ImageView) view.findViewById(R.id.card_garage_icon);
        garageSummary = (Version1TextView) view.findViewById(R.id.card_garage_summary);

        doorIcon = (ImageView) view.findViewById(R.id.card_door_icon);
        doorSummary = (Version1TextView) view.findViewById(R.id.card_door_summary);

        lockContainer = view.findViewById(R.id.card_lock_container);
        garageContainer = view.findViewById(R.id.card_garage_container);
        doorContainer = view.findViewById(R.id.card_door_container);
    }

    public void build(@NonNull DoorsNLocksCard card) {
        serviceName.setText(context.getString(R.string.card_doors_and_locks_title));
        serviceImage.setImageResource(R.drawable.dashboard_doorslocks);

        showImages(card);
    }

    private void showImages(@NonNull final DoorsNLocksCard card){

        StateSummary stateSummary = card.getStateSummary();
        if(stateSummary !=null){
            lockSummary.setText(String.format(Locale.getDefault(), "%d", stateSummary.getLockUnlockedCount()));
            garageSummary.setText(String.format(Locale.getDefault(), "%d", stateSummary.getGarageOpenCount()));
            doorSummary.setText(String.format(Locale.getDefault(), "%d", stateSummary.getDoorOpenCount()));

            if(stateSummary.getLockUnlockedCount() == 0) {
                lockContainer.setVisibility(View.GONE);
            } else {
                lockContainer.setVisibility(View.VISIBLE);
            }
            if(stateSummary.getGarageOpenCount() == 0) {
                garageContainer.setVisibility(View.GONE);
            } else {
                garageContainer.setVisibility(View.VISIBLE);
            }
            if(stateSummary.getDoorOpenCount() == 0) {
                doorContainer.setVisibility(View.GONE);
            } else {
                doorContainer.setVisibility(View.VISIBLE);
            }
        }
    }

}

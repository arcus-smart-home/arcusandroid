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
package arcus.app.subsystems.alarm.cards.internal;

import android.content.Context;
import android.graphics.Color;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import arcus.app.R;
import arcus.app.common.image.ImageManager;
import arcus.app.common.image.picasso.transformation.BlackWhiteInvertTransformation;
import arcus.app.common.image.picasso.transformation.CropCircleTransformation;
import arcus.app.common.image.picasso.transformation.Invert;
import arcus.app.common.view.Version1TextView;
import arcus.app.subsystems.alarm.cards.AlarmActiveCard;

/**
 *
 * THIS IS BEING USED IN MULTIPLE PLACES!!!
 * CHECK YO SELF BEFORE YOU WRECK YO SELF!
 *
 */
public class AlarmActiveCardView extends RecyclerView.ViewHolder {
    private Context context;
    CardView cardView;
    View defaultView;
    View header;
    TextView tv;
    Version1TextView time;
    Version1TextView title;
    Version1TextView description;
    View divider;
    ImageView imageView;

    public AlarmActiveCardView(View view) {
        super(view);
        context = view.getContext();
        cardView = (CardView) view.findViewById(R.id.card_view);
        defaultView = view.findViewById(R.id.alarm_active_card);
        header = view.findViewById(R.id.card_header);
        tv = (TextView) view.findViewById(R.id.heading_text);
        time = (Version1TextView)view.findViewById(R.id.alert_time);
        title = (Version1TextView)view.findViewById(R.id.title_text);
        description = (Version1TextView)view.findViewById(R.id.description_text);
        divider = view.findViewById(R.id.divider_alarm_active);
        imageView = (ImageView) view.findViewById(R.id.image);
    }

    public void build(@NonNull AlarmActiveCard card) {
        Invert inversion = null;
        int textColorTop;
        int textColorBottom;

        //Detects card color
        if(card.changeColor()){
            textColorTop = Color.WHITE;
            textColorBottom = Color.rgb(220,220,220);
            inversion = Invert.BLACK_TO_WHITE;

        }
        else{
            textColorTop = Color.BLACK;
            textColorBottom = Color.BLACK;
            inversion =Invert.WHITE_TO_BLACK;
        }


        if (cardView != null) {
            if(card.changeColor()) cardView.setCardBackgroundColor(Color.TRANSPARENT);
            else cardView.setCardBackgroundColor(Color.WHITE);

        }



        //TODO: make code a little more readable during refactor
        //TODO: make generic cards for refactor
        //TODO: Kill super devil for his motorcycle. It looks awesome. But really, see GOTO:67
        if(card.getShowHeader()){
            header.setVisibility(View.VISIBLE);
            defaultView.setVisibility(View.GONE);
            tv.setText(card.getAlertTime());
        } else{
            if(card.getAlertTime() != ""){
                time.setText(card.getAlertTime());
                if(card.changeColor()) time.setTextColor(textColorBottom);
            } else {
                time.setText("");
            }

            title.setText(card.getTitle());
            if(card.changeColor()) title.setTextColor(textColorTop);

            description.setText(card.getDescription());
            if(card.changeColor()) description.setTextColor(textColorBottom);

            if (card.isDividerShown()) {
                divider.setVisibility(View.VISIBLE);
                if(card.changeColor()) divider.setBackgroundColor(textColorBottom);
                } else{
                divider.setVisibility(View.GONE);
                }

            if(card.getImageResource() != null) {
                imageView.setImageResource(card.getImageResource());
            } else {
                imageView.setImageDrawable(null);
            }

            if (card.getDeviceModel() != null) {
                ImageManager.with(context)
                        .putSmallDeviceImage(card.getDeviceModel())
                        .withTransformForStockImages(new BlackWhiteInvertTransformation(inversion))
                        .withTransform(new CropCircleTransformation())
                        .withPlaceholder(R.drawable.device_list_placeholder)
                        .withError(R.drawable.device_list_placeholder)
                        .into(imageView)
                        .execute();
            }
            else {
                    ImageManager.with(context)
                        .putDrawableResource(card.getIconRes())
                        .withTransformForStockImages(new BlackWhiteInvertTransformation(inversion))
                        .withTransform(new CropCircleTransformation())
                        .withError(R.drawable.device_list_placeholder)
                        .into(imageView)
                        .execute();
                    }

                }


            }


        }

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
package arcus.app.subsystems.camera.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import arcus.app.R;
import arcus.app.common.cards.view.BaseCardItemView;
import arcus.app.common.view.Version1TextView;
import arcus.app.subsystems.camera.cards.DeviceCard;


public class DeviceCardItemView extends BaseCardItemView<DeviceCard> {

    ImageView previewImage;

    public DeviceCardItemView(Context context) {
        super(context);
    }

    public DeviceCardItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DeviceCardItemView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override public void build(@NonNull final DeviceCard card) {
        super.build(card);
        if (card.isOffline()) {
            showCardOffline(card);
        }
        else if (card.isFirmwareUpdating()) {
            showCardFirmwareUpdating(card);
        }
        else if(card.isUnavailable()) {
            showCardUnavailable(card);
        }
        else {
            showCardOnline(card);
        }
    }

    protected void showCardUnavailable(@NonNull final DeviceCard card) {
        CardView cardView = findViewById(R.id.cardView);
        if (cardView == null) {
            return;
        }

        if (card.isDividerShown()) {
            showDivider(true);
        }

        View unavailableContainer = findViewById(R.id.camera_controls_unavailable_container);
        View onlineContainer = findViewById(R.id.camera_controls_container);
        View noPreviewImage = findViewById(R.id.preview_image_unavailable);

        unavailableContainer.setVisibility(VISIBLE);
        onlineContainer.setVisibility(GONE);
        noPreviewImage.setVisibility(VISIBLE);

        TextView offlineName = findViewById(R.id.device_name_unavailable);
        offlineName.setText(card.getTitle());

        previewImage = findViewById(R.id.preview_image);
        if (card.getCacheFile() != null) {
            previewImage.setImageBitmap(null);
            cardView.setCardBackgroundColor(Color.TRANSPARENT);
        }
        else {
            cardView.setCardBackgroundColor(getContext().getResources().getColor(R.color.black_with_20));
        }
    }

    protected void showCardOnline(@NonNull final DeviceCard card) {
        CardView cardView = findViewById(R.id.cardView);
        if (cardView == null) {
            return;
        }

        cardView.setCardBackgroundColor(Color.TRANSPARENT);
        if (card.isDividerShown()) {
            showDivider(false);
        }

        View unavailableContainer = findViewById(R.id.camera_controls_unavailable_container);
        View firmwareUpdateContainer = findViewById(R.id.camera_controls_firmware_container);
        View offlineContainer = findViewById(R.id.camera_controls_offline_container);
        View onlineContainer = findViewById(R.id.camera_controls_container);
        View noPreviewImage = findViewById(R.id.preview_image_unavailable);

        unavailableContainer.setVisibility(GONE);
        firmwareUpdateContainer.setVisibility(GONE);
        offlineContainer.setVisibility(GONE);
        onlineContainer.setVisibility(VISIBLE);
        noPreviewImage.setVisibility(GONE);

        previewImage = findViewById(R.id.preview_image);
        if (card.getCacheFile() != null) {
            noPreviewImage.setVisibility(VISIBLE);
            Bitmap bitmap = BitmapFactory.decodeFile(card.getCacheFile().getAbsolutePath());
            if (bitmap != null) {
                previewImage.setImageBitmap(bitmap);
                noPreviewImage.setVisibility(GONE);
            }
        } else {
            noPreviewImage.setVisibility(VISIBLE);
        }

        Version1TextView title = findViewById(R.id.title);
        title.setText(card.getTitle());

        Version1TextView desc = findViewById(R.id.description);
        desc.setText(card.getDescription());

        ImageView play = findViewById(R.id.play_image);
        View recordingText = findViewById(R.id.recording_text);
        if (card.shouldHideButtons()) {
            play.setVisibility(INVISIBLE);
        }
        else if (card.isRecording() && card.isRecordable()) {
            recordingText.setVisibility(VISIBLE);
            recordingText.setOnClickListener(v -> {
                DeviceCard.OnClickListener clickListener = card.getOnClickListener();
                if (clickListener != null) {
                    clickListener.onRecord();
                }
            });
            play.setVisibility(GONE);
            play.setOnClickListener(null);
        } else {
            play.setVisibility(VISIBLE);
            recordingText.setVisibility(GONE);
            play.setOnClickListener(v -> {
                DeviceCard.OnClickListener clickListener = card.getOnClickListener();
                if (clickListener != null) {
                    clickListener.onPlay();
                }
            });
        }

        ImageView record = findViewById(R.id.record_image);
        if (card.shouldHideButtons()) {
            record.setVisibility(INVISIBLE);
        } else if (card.isRecordable()) {
            record.setVisibility(VISIBLE);
            record.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (card.getOnClickListener() != null) {
                        card.getOnClickListener().onRecord();
                    }
                }
            });
        } else {
            record.setVisibility(GONE);
        }

        previewImage.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (card.getOnClickListener() != null) {
                    if(card.isRecording()) {
                        card.getOnClickListener().onRecord();
                    } else {
                        card.getOnClickListener().onStream();
                    }
                }
            }
        });
    }

    protected void showCardOffline(@NonNull final DeviceCard card) {
        CardView cardView = findViewById(R.id.cardView);
        if (cardView == null) {
            return;
        }

        cardView.setCardBackgroundColor(getContext().getResources().getColor(R.color.black_with_20));
        if (card.isDividerShown()) {
            showDivider(true);
        }

        View offlineContainer = findViewById(R.id.camera_controls_offline_container);
        View onlineContainer = findViewById(R.id.camera_controls_container);
        View noPreviewImage = findViewById(R.id.preview_image_unavailable);

        offlineContainer.setVisibility(VISIBLE);
        onlineContainer.setVisibility(GONE);
        noPreviewImage.setVisibility(VISIBLE);

        TextView offlineName = findViewById(R.id.device_name_offline);
        offlineName.setText(card.getTitle());

        previewImage = findViewById(R.id.preview_image);
        if (previewImage != null) {
            previewImage.setImageBitmap(null);
            previewImage.setOnClickListener(null);
        }
    }

    protected void showCardFirmwareUpdating(@NonNull final DeviceCard card) {
        CardView cardView = findViewById(R.id.cardView);
        if (cardView == null) {
            return;
        }

        cardView.setCardBackgroundColor(getContext().getResources().getColor(R.color.black_with_20));
        if (card.isDividerShown()) {
            showDivider(true);
        }

        View firmwareUpdateContainer = findViewById(R.id.camera_controls_firmware_container);
        View onlineContainer = findViewById(R.id.camera_controls_container);
        View noPreviewImage = findViewById(R.id.preview_image_unavailable);

        firmwareUpdateContainer.setVisibility(VISIBLE);
        onlineContainer.setVisibility(GONE);
        noPreviewImage.setVisibility(VISIBLE);

        TextView offlineName = findViewById(R.id.device_name_firmware);
        offlineName.setText(card.getTitle());

        previewImage = findViewById(R.id.preview_image);
        if (previewImage != null) {
            previewImage.setImageBitmap(null);
            previewImage.setOnClickListener(null);
        }
    }

    protected void showDivider(boolean isOffline) {
        View divider = findViewById(R.id.divider);
        if (divider != null) {
            divider.setVisibility(View.VISIBLE);
            int color = isOffline ? R.color.black_with_10 : R.color.white_with_10;
            Context context = getContext();
            if (context != null) {
                divider.setBackgroundColor(context.getResources().getColor(color));
            }
        }
    }
}

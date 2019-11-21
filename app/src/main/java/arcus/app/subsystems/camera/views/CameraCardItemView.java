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
import androidx.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import arcus.app.R;
import arcus.app.common.view.DashboardFlipViewHolder;
import arcus.app.common.view.Version1TextView;
import arcus.app.subsystems.camera.cards.CameraCard;


public class CameraCardItemView extends DashboardFlipViewHolder {

    ImageView serviceImage;
    Version1TextView serviceName;
    Context context;
    Version1TextView lastRecording;
    View summaryContainer;

    public CameraCardItemView(View view) {
        super(view);
        serviceImage = (ImageView) view.findViewById(R.id.service_image);
        serviceName = (Version1TextView) view.findViewById(R.id.service_name);
        context = view.getContext();

        lastRecording = (Version1TextView) view.findViewById(R.id.last_recording);
        summaryContainer = view.findViewById(R.id.summary_container);
    }

    public void build(@NonNull CameraCard card) {
        serviceName.setText(context.getString(R.string.card_cameras_title));
        serviceImage.setImageResource(R.drawable.dashboard_camera);

        if (TextUtils.isEmpty(card.getLastRecordingString())) {
            summaryContainer.setVisibility(View.GONE);
        }
        else {
            summaryContainer.setVisibility(View.VISIBLE);
            lastRecording.setText(card.getLastRecordingString());
        }
    }
}

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
package arcus.app.device.pairing.post;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import arcus.app.R;
import arcus.app.common.image.ImageManager;
import arcus.app.common.image.Wallpaper;
import arcus.app.common.sequence.SequencedFragment;
import arcus.app.common.view.Version1Button;


public class SomfyFavoritePositionReminderFragment extends SequencedFragment {
    private static String DEVICE_NAME = "DEVICE_NAME";
    private Version1Button nextButton;

    public static SomfyFavoritePositionReminderFragment newInstance (String deviceName) {
        SomfyFavoritePositionReminderFragment fragment = new SomfyFavoritePositionReminderFragment();
        Bundle bundle = new Bundle();
        bundle.putString(DEVICE_NAME, deviceName);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup parentGroup = (ViewGroup) super.onCreateView(inflater, container, savedInstanceState);
        nextButton = (Version1Button) parentGroup.findViewById(R.id.next_btn);
        ((TextView) parentGroup.findViewById(R.id.somfy_title)).setText(getResources().getString(R.string.somfy_blinds_fav_reminder_title));
        ((TextView) parentGroup.findViewById(R.id.somfy_description)).setText(getResources().getString(R.string.somfy_blinds_fav_reminder_desc));
        return parentGroup;
    }

    @Override
    public void onResume () {
        super.onResume();
        ImageManager.with(getActivity()).setWallpaper(Wallpaper.ofCurrentPlace().lightend());
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goNext();
            }
        });
    }

    @Nullable
    @Override
    public String getTitle() {
        return getArguments().getString(DEVICE_NAME);
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_somfy_favorite_position_reminder;
    }

}

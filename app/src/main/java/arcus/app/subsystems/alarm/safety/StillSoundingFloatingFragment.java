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
package arcus.app.subsystems.alarm.safety;

import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import arcus.app.R;
import arcus.app.common.popups.ArcusFloatingFragment;
import arcus.app.common.view.Version1TextView;


public class StillSoundingFloatingFragment extends ArcusFloatingFragment {

    public static final String SOUND_DEVICE_KEY ="sound device key";

    @Nullable
    private SoundDevice mSoundDevice;

    public enum SoundDevice{
        SMOKE,
        CO
    }

    @NonNull
    public static StillSoundingFloatingFragment newInstance(SoundDevice soundDevice){
        StillSoundingFloatingFragment fragment = new StillSoundingFloatingFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(SOUND_DEVICE_KEY,soundDevice);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Bundle arguments = getArguments();
        if (arguments != null) {
            mSoundDevice = (SoundDevice) arguments.getSerializable(SOUND_DEVICE_KEY);
        }
    }

    @Override
    public Integer floatingBackgroundColor() {
        return getResources().getColor(R.color.pink_banner);
    }

    @Override
    public void setFloatingTitle() {
        title.setText(getString(R.string.safety_alarm_may_still_sound_title));
        title.setTextColor(Color.WHITE);
    }

    @Override
    public void doContentSection() {
        setCloseButtonIcon(R.drawable.button_close_box_white);

        Version1TextView sounding = (Version1TextView) contentView.findViewById(R.id.safety_alarm_sounding_text);
        switch (mSoundDevice){
            case CO:
                sounding.setText(getString(R.string.safety_alarm_co_still_sounding));
                break;
            case SMOKE:
                sounding.setText(getString(R.string.safety_alarm_smoke_still_sounding));
                break;
        }
    }

    @Override
    public Integer contentSectionLayout() {
        return R.layout.safety_alarm_still_sound_text;
    }

    @NonNull
    @Override
    public String getTitle() {
        return "";
    }
}

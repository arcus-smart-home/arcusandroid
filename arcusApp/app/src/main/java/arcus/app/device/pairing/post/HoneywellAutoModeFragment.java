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

import android.app.Activity;
import android.view.View;

import arcus.app.R;
import arcus.app.common.image.ImageManager;
import arcus.app.common.image.Wallpaper;
import arcus.app.common.sequence.SequencedFragment;


public class HoneywellAutoModeFragment extends SequencedFragment {

    public static HoneywellAutoModeFragment newInstance() {
        return new HoneywellAutoModeFragment();
    }

    @Override
    public void onResume () {
        super.onResume();
        ImageManager.with(getActivity()).setWallpaper(Wallpaper.ofCurrentPlace().lightend());

        View view = getView();
        if (view == null) {
            return;
        }

        View nextButton = view.findViewById(R.id.next_button);
        if (nextButton != null) {
            nextButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    goNext();
                }
            });
        }

        Activity activity = getActivity();
        if (activity != null) {
            activity.setTitle(getTitle());
            activity.invalidateOptionsMenu();
        }
    }

    @Override
    public String getTitle() {
        return getString(R.string.honeywell_auto_mode);
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_auto_mode;
    }
}

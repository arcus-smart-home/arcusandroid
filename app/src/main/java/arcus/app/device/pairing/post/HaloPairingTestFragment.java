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
import android.widget.ImageView;

import arcus.app.R;
import arcus.app.common.image.ImageManager;
import arcus.app.common.image.Wallpaper;
import arcus.app.common.sequence.SequencedFragment;
import arcus.app.common.view.Version1TextView;

public class HaloPairingTestFragment extends SequencedFragment {

    public static HaloPairingTestFragment newInstance() {
        return new HaloPairingTestFragment();
    }

    @Override
    public void onResume () {
        super.onResume();
        ImageManager.with(getActivity()).setWallpaper(Wallpaper.ofCurrentPlace().lightend());

        View view = getView();
        if (view == null) {
            return;
        }

        ImageView image = (ImageView) view.findViewById(R.id.halo_image);
        image.setImageDrawable(getResources().getDrawable(R.drawable.halo_pairing_test));

        Version1TextView title = (Version1TextView) view.findViewById(R.id.title);
        title.setText(getString(R.string.halo_post_pairing_test_title));

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
        }
    }

    @Override public String getTitle() {
        return getString(R.string.test_text);
    }

    @Override public Integer getLayoutId() {
        return R.layout.fragment_halo_postpairing_test;
    }
}

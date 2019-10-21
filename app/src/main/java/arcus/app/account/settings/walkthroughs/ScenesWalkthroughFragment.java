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
package arcus.app.account.settings.walkthroughs;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import arcus.app.R;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.fragments.BaseFragment;
import arcus.app.common.utils.PreferenceCache;
import arcus.app.common.utils.PreferenceUtils;
import arcus.app.common.view.Version1TextView;

public class ScenesWalkthroughFragment extends BaseFragment {


    private SetupWalkthroughs[] setupWalkthroughs = new SetupWalkthroughs[]{
            new SetupWalkthroughs(R.layout.fragment_scenes_walkthrough, R.string.walkthrough_scenes_header_1, R.string.walkthrough_scenes_desc_1, R.drawable.walkthrough_scenes_screen1),
            new SetupWalkthroughs(R.layout.fragment_scenes_walkthrough, R.string.walkthrough_scenes_header_2, R.string.walkthrough_scenes_desc_2, R.drawable.walkthrough_scenes_screen1),
            new SetupWalkthroughs(R.layout.fragment_scenes_walkthrough, R.string.walkthrough_scenes_header_3, R.string.walkthrough_scenes_desc_3, R.drawable.walkthrough_scenes_screen3),
            new SetupWalkthroughs(R.layout.fragment_scenes_walkthrough, R.string.walkthrough_scenes_header_4, R.string.walkthrough_scenes_desc_4, R.drawable.walkthrough_scenes_screen4),
            new SetupWalkthroughs(R.layout.fragment_scenes_walkthrough, R.string.walkthrough_tutorials, R.string.walkthrough_tutorials_disc, R.drawable.intro_arcus_screen7)
    };




    private View closeButton;
    private Version1TextView title;
    private Version1TextView description;
    private ImageView introArcusPhoto;
    private boolean isChecked = true;
    private ImageView checkBox;
    private View checkBoxView;
    private View middleView;
    private int position;

    @NonNull
    public static ScenesWalkthroughFragment newInstance(int i) {
        ScenesWalkthroughFragment scenesWalkthroughFragment = new ScenesWalkthroughFragment();
        Bundle args = new Bundle();
        args.putInt("POSITION", i);
        scenesWalkthroughFragment.setArguments(args);
        return scenesWalkthroughFragment;
    }

    @Override
    public boolean onBackPressed() {
        return false;
    }

    @Nullable
    @Override
    public String getTitle() {
        return null;
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_scenes_walkthrough;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = super.onCreateView(inflater, container, savedInstanceState);

        if (view != null) {

            title = (Version1TextView) view.findViewById(R.id.intro_arcus_title);
            description = (Version1TextView) view.findViewById(R.id.intro_arcus_des);
            introArcusPhoto = (ImageView) view.findViewById(R.id.intro_arcus_photo);
            closeButton = view.findViewById(R.id.exit_view);
            checkBox = (ImageView) view.findViewById(R.id.checkbox_climate_image);
            checkBoxView = view.findViewById(R.id.checkbox_view);
            middleView = view.findViewById(R.id.scene_custom_view);
            //TODO: this is not passing in the position
            setupView(getArguments().getInt("POSITION", 0));
        }

        return view;
    }

    private void setupView(int position){

        title.setText(setupWalkthroughs[position].getTitleResId());
        description.setText(setupWalkthroughs[position].getDescriptionId());

        //Special Cases

        description.setVisibility(View.VISIBLE);
        introArcusPhoto.setVisibility(View.VISIBLE);
        introArcusPhoto.setImageResource(setupWalkthroughs[position].getImageId());
        middleView.setVisibility((View.GONE));
        closeButton.setVisibility(View.INVISIBLE);
        checkBoxView.setVisibility(View.GONE);

        if (position == 0) {
            ((RelativeLayout.LayoutParams) introArcusPhoto.getLayoutParams()).removeRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        } else if (position == 1) {
            introArcusPhoto.setVisibility(View.GONE);
            middleView.setVisibility((View.VISIBLE));
            checkBoxView.setVisibility(View.GONE);
            description.setVisibility(View.GONE);
        } else if (position == 4) {
            //checkbox_view visible
            introArcusPhoto.setImageResource(setupWalkthroughs[position].getImageId());
            checkBox.setImageResource(isChecked ? R.drawable.circle_check_white_filled : R.drawable.circle_hollow_white);
            checkBox.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    isChecked = !isChecked;
                    checkBox.setImageResource(isChecked ? R.drawable.circle_check_white_filled : R.drawable.circle_hollow_white);
                }
            });
            checkBoxView.setVisibility(View.VISIBLE);
            closeButton.setVisibility(View.VISIBLE);
            closeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PreferenceCache.getInstance().putBoolean(
                            PreferenceUtils.SCENES_WALKTHROUGH_DONT_SHOW_AGAIN,
                            isChecked
                    );
                    BackstackManager.getInstance().navigateBack();
                }
            });
        }
    }

}
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
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import arcus.app.R;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.fragments.BaseFragment;
import arcus.app.common.view.Version1TextView;

public class IntroductionArcusFragment extends BaseFragment {


    private SetupWalkthroughs[] setupWalkthroughs = new SetupWalkthroughs[]{
            new SetupWalkthroughs(R.layout.fragment_walkthrough_image_bottom, R.string.welcome_to_arcus, R.string.welcome_to_arcus_disc, R.drawable.intro_arcus_screen1),
            new SetupWalkthroughs(R.layout.fragment_walkthrough_image_bottom, R.string.favorites, R.string.favorites_disc, R.drawable.intro_arcus_screen2),
            new SetupWalkthroughs(R.layout.fragment_walkthrough_image_bottom, R.string.walkthrough_cards, R.string.walkthrough_cards_disc, R.drawable.intro_arcus_screen3),
            new SetupWalkthroughs(R.layout.fragment_walkthrough_image_bottom, R.string.customize_the_dashboard, R.string.customize_the_dashboard_disc, R.drawable.intro_arcus_screen4),
            new SetupWalkthroughs(R.layout.fragment_walkthrough_image_bottom, R.string.expand_your_smart_home, R.string.expand_your_smart_home_disc, R.drawable.intro_arcus_screen5),
            new SetupWalkthroughs(R.layout.fragment_walkthrough_image_bottom, R.string.manage_your_smart_home, R.string.manage_your_smart_home_disc, R.drawable.intro_arcus_screen6),
            new SetupWalkthroughs(R.layout.fragment_walkthrough_image_bottom, R.string.walkthrough_tutorials, R.string.walkthrough_tutorials_disc, R.drawable.intro_arcus_screen7)
    };

    private View closeButton;
    private Version1TextView title;
    private Version1TextView description;
    private ImageView introArcusPhoto;


    @NonNull
    public static IntroductionArcusFragment newInstance(int i) {
        IntroductionArcusFragment introductionArcusFragment = new IntroductionArcusFragment();
        Bundle args = new Bundle();
        args.putInt("POSITION", i);
        introductionArcusFragment.setArguments(args);
        return introductionArcusFragment;
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
        return R.layout.fragment_walkthrough_image_bottom;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = super.onCreateView(inflater, container, savedInstanceState);

        if (view != null) {

            title = (Version1TextView) view.findViewById(R.id.intro_arcus_title);
            description = (Version1TextView) view.findViewById(R.id.intro_arcus_des);
            introArcusPhoto = (ImageView) view.findViewById(R.id.intro_arcus_photo);
            closeButton = view.findViewById(R.id.exit_view);

            int position = getArguments().getInt("POSITION", 0);
            setupView(position);



        }

        return view;
    }
        private void setupView(int position){
            int currentPage = position;

            title.setText(setupWalkthroughs[currentPage].getTitleResId());

            description.setText(setupWalkthroughs[currentPage].getDescriptionId());

            introArcusPhoto.setImageResource(setupWalkthroughs[currentPage].getImageId());

            if (currentPage == 6) {
                closeButton.setVisibility(View.VISIBLE);
                closeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        BackstackManager.getInstance().navigateBack();
                    }
                });
            } else {
                closeButton.setVisibility(View.INVISIBLE);
            }
        }

}

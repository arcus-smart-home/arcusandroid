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
import arcus.app.common.utils.PreferenceCache;
import arcus.app.common.utils.PreferenceUtils;
import arcus.app.common.view.Version1TextView;
import com.viewpagerindicator.CirclePageIndicator;

public class HistoryWalkthroughFragment extends BaseFragment {


    private SetupWalkthroughs[] setupWalkthroughs = new SetupWalkthroughs[]{
            new SetupWalkthroughs(R.layout.fragment_history_walkthrough, R.string.walkthrough_history_header_1, R.string.walkthrough_history_desc_1, R.drawable.history_walkthrough_screen1)
    };



    private View closeButton;
    private CirclePageIndicator indicator;
    private Version1TextView title;
    private Version1TextView description;
    private ImageView introArcusPhoto;
    private boolean isChecked = true;
    private ImageView checkBox;
    private View checkBoxView;


    @NonNull
    public static HistoryWalkthroughFragment newInstance(int i) {
        HistoryWalkthroughFragment historyWalkthroughFragment = new HistoryWalkthroughFragment();
        Bundle args = new Bundle();
        args.putInt("POSITION", i);
        historyWalkthroughFragment.setArguments(args);
        return historyWalkthroughFragment;
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
        return R.layout.fragment_history_walkthrough;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = super.onCreateView(inflater, container, savedInstanceState);

        if (view != null) {
            int position = getArguments().getInt("POSITION", 0);

            indicator = (CirclePageIndicator) view.findViewById(R.id.introduction_indicator);
            title = (Version1TextView) view.findViewById(R.id.intro_arcus_title);
            description = (Version1TextView) view.findViewById(R.id.intro_arcus_des);
            introArcusPhoto = (ImageView) view.findViewById(R.id.intro_arcus_photo);
            closeButton = view.findViewById(R.id.exit_view);
            checkBox = (ImageView) view.findViewById(R.id.checkbox_climate_image);
            checkBoxView = view.findViewById(R.id.checkbox_view);

            setupView(position);

        }

        return view;
    }

    private void setupView(int position){

        title.setText(setupWalkthroughs[position].getTitleResId());
        description.setText(setupWalkthroughs[position].getDescriptionId());
        introArcusPhoto.setImageResource(setupWalkthroughs[position].getImageId());

        if (position == 0) {
            //checkbox_view visible
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
                            PreferenceUtils.HISTORY_WALKTHROUGH_DONT_SHOW_AGAIN,
                            isChecked
                    );
                    BackstackManager.getInstance().navigateBack();
                }
            });
        } else {
            closeButton.setVisibility(View.INVISIBLE);
            checkBoxView.setVisibility(View.GONE);
        }
    }

}

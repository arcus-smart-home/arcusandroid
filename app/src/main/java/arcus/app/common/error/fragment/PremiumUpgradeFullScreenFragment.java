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
package arcus.app.common.error.fragment;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import arcus.app.R;



public abstract class PremiumUpgradeFullScreenFragment extends Fragment {

    public abstract String getErrorTitle();
    public abstract int getBodyLayoutId();
    public abstract int getPostItLayoutId();

    private View view;
    private TextView errorTitle;
    private RelativeLayout bodyLayout;
    private RelativeLayout postitLayout;
    private RelativeLayout closeClickableRegion;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(getLayoutId(), container, false);

        errorTitle = (TextView) view.findViewById(R.id.error_title);
        bodyLayout = (RelativeLayout) view.findViewById(R.id.body_layout);
        postitLayout = (RelativeLayout) view.findViewById(R.id.postit_layout);
        closeClickableRegion = (RelativeLayout) view.findViewById(R.id.close_clickable_region);

        return view;
    }

    @Override
    public void onResume() {

        errorTitle.setText(getErrorTitle());
        bodyLayout.removeAllViews();
        postitLayout.removeAllViews();

        getActivity().getLayoutInflater().inflate(getBodyLayoutId(), bodyLayout, true);
        getActivity().getLayoutInflater().inflate(getPostItLayoutId(), postitLayout, true);

        closeClickableRegion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });

        super.onResume();
    }

    public Integer getLayoutId() {
        return R.layout.fragment_premium_upgrade_full_screen;
    }

}

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
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import arcus.app.R;


public abstract class FullScreenErrorFragment extends Fragment {

    public abstract int getErrorIcon ();
    public abstract String getErrorTitle();
    public abstract int getBodyLayoutId();
    public abstract int getPostItLayoutId();

    private ImageView errorIcon;
    private TextView errorTitle;
    protected RelativeLayout bodyLayout;
    protected RelativeLayout postitLayout;
    protected RelativeLayout closeClickableRegion;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(getLayoutId(), container, false);

        errorIcon = (ImageView) view.findViewById(R.id.error_icon);
        errorTitle = (TextView) view.findViewById(R.id.error_title);
        bodyLayout = (RelativeLayout) view.findViewById(R.id.body_layout);
        postitLayout = (RelativeLayout) view.findViewById(R.id.postit_layout);
        closeClickableRegion = (RelativeLayout) view.findViewById(R.id.close_clickable_region);

        return view;
    }

    @Override
    public void onResume() {

        errorIcon.setImageResource(getErrorIcon());
        errorTitle.setText(getErrorTitle());
        bodyLayout.removeAllViews();
        postitLayout.removeAllViews();

        getActivity().getLayoutInflater().inflate(getBodyLayoutId(), bodyLayout, true);

        if (getPostItLayoutId() != 0) {
            getActivity().getLayoutInflater().inflate(getPostItLayoutId(), postitLayout, true);
        }

        closeClickableRegion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });

        super.onResume();
    }

    public Integer getLayoutId() {
        return R.layout.fragment_full_screen_error;
    }
}

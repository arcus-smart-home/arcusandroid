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
package arcus.app.subsystems.place;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import arcus.app.R;
import arcus.app.common.image.ImageManager;
import arcus.app.common.image.picasso.transformation.BlackWhiteInvertTransformation;
import arcus.app.common.image.picasso.transformation.Invert;


public class PlaceAccountPremiumPlanFragment extends PlaceCreationStepFragment {

    private ImageView iconCameraSmall;
    private ImageView iconSecuritySmall;
    private ImageView iconCareSmall;

    @NonNull
    public static PlaceAccountPremiumPlanFragment newInstance() {
        PlaceAccountPremiumPlanFragment fragment = new PlaceAccountPremiumPlanFragment();
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        iconCameraSmall = (ImageView) view.findViewById(R.id.icon_camera_small);
        iconSecuritySmall = (ImageView) view.findViewById(R.id.icon_security_small);
        iconCareSmall = (ImageView) view.findViewById(R.id.icon_care_small);

        return view;
    }

    public void onResume () {
        super.onResume();

        ImageManager
                .with(getActivity())
                .putDrawableResource(R.drawable.icon_service_camera_small)
                .withTransform(new BlackWhiteInvertTransformation(Invert.WHITE_TO_BLACK))
                .into(iconCameraSmall)
                .execute();

        ImageManager
                .with(getActivity())
                .putDrawableResource(R.drawable.icon_service_safetyalarm_small)
                .withTransform(new BlackWhiteInvertTransformation(Invert.WHITE_TO_BLACK))
                .into(iconSecuritySmall)
                .execute();

        ImageManager
                .with(getActivity())
                .putDrawableResource(R.drawable.icon_service_care_small)
                .withTransform(new BlackWhiteInvertTransformation(Invert.WHITE_TO_BLACK))
                .into(iconCareSmall)
                .execute();

    }

    @Override
    public boolean submit() {
        transitionToNextState();
        return true;
    }

    @Override
    public boolean validate() {
        // Nothing to validate
        return true;
    }

    @Override
    public String getTitle() {
        return getString(R.string.account_registration_premium_plan);
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_account_premium_plan;
    }
}

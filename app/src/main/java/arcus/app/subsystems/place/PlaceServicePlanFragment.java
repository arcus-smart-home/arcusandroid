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

import android.app.Activity;
import android.os.Bundle;
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import arcus.cornea.controller.SubscriptionController;
import arcus.app.R;
import arcus.app.common.sequence.SequencedFragment;
import arcus.app.common.view.Version1Button;
import arcus.app.subsystems.place.controller.NewPlaceSequenceController;


public class PlaceServicePlanFragment extends SequencedFragment<NewPlaceSequenceController> implements NewPlaceSequenceController.PrimaryPlaceServiceLevelCallback {

    private Version1Button nextButton;
    TextView title;

    @NonNull
    public static PlaceServicePlanFragment newInstance() {
        return new PlaceServicePlanFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        nextButton = (Version1Button) view.findViewById(R.id.fragment_account_parent_continue_btn);
        title = (TextView) view.findViewById(R.id.title);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        title.setVisibility(View.INVISIBLE);
        getController().getPrimaryPlaceServiceLevel(this);

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goNext();
            }
        });

        Activity activity = getActivity();
        if (activity != null) {
            activity.setTitle(getTitle());
        }
    }

    @NonNull
    @Override
    public String getTitle() {
        return getResources().getString(R.string.place_service_plan_text);
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_place_service_plan;
    }

    @Override
    public void onError() {

    }

    @Override
    public void onSuccess(String serviceLevel) {

        if(SubscriptionController.isPremiumOrPro()) {
            title.setText(getString(R.string.place_service_plan_title_promon));

        } else {
            title.setText(String.format(getString(R.string.place_service_plan_title_not_promon), getController().getExistingPlaceNickname()));
        }
        title.setVisibility(View.VISIBLE);

    }
}

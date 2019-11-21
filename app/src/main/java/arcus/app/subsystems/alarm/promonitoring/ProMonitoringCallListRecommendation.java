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
package arcus.app.subsystems.alarm.promonitoring;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import arcus.app.R;
import arcus.app.common.sequence.ReturnToSenderSequenceController;
import arcus.app.common.sequence.SequencedFragment;
import arcus.app.common.utils.GlobalSetting;
import arcus.app.common.view.Version1Button;



public class ProMonitoringCallListRecommendation extends SequencedFragment<ReturnToSenderSequenceController> {

    private Version1Button learnMoreButton;

    public static ProMonitoringCallListRecommendation newInstance() {
        return new ProMonitoringCallListRecommendation();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        this.learnMoreButton = (Version1Button) view.findViewById(R.id.learn_more_button);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        learnMoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().startActivity(new Intent(Intent.ACTION_VIEW, GlobalSetting.SECURITY_CALLTREE_SUPPORT));
            }
        });
    }

    @Nullable
    @Override
    public String getTitle() {
        return getString(R.string.alarm_calltree);
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_calllist_recommendation;
    }
}

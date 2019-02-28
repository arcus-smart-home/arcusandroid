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
package arcus.app.account.creation;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.jinatonic.confetti.CommonConfetti;
import arcus.app.R;
import arcus.app.activities.BaseActivity;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.view.ScleraButton;



public class CreationConfettiFragment extends Fragment {

    private ScleraButton nextButton;
    private ViewGroup confettiContainer;

    public static CreationConfettiFragment newInstance() {
        return new CreationConfettiFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_creation_confetti, container, false);
        nextButton = (ScleraButton) view.findViewById(R.id.next);
        confettiContainer = (ViewGroup) view.findViewById(R.id.confetti_container);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        ActionBar actionBar = ((BaseActivity)getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BackstackManager.getInstance().navigateToFragment(HaveAHubFragment.newInstance(), true);
            }
        });

        final int[] confettiColors = new int[] {
                getResources().getColor(R.color.pink_confetto),
                getResources().getColor(R.color.purple_confetto),
                getResources().getColor(R.color.yellow_confetto),
                getResources().getColor(R.color.blue_confetto),
                getResources().getColor(R.color.turqoise_confetto)
        };

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                CommonConfetti.rainingConfetti(confettiContainer, confettiColors).oneShot();
            }
        }, 2000);
    }
}

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

import android.app.Activity;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.ActionBar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import arcus.app.R;
import arcus.app.activities.BaseActivity;
import arcus.app.activities.DashboardActivity;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.view.ScleraLinkView;
import arcus.app.dashboard.HomeFragment;
import arcus.app.pairing.device.productcatalog.ProductCatalogActivity;
import arcus.app.pairing.hub.kickoff.HubKitFragment;

public class HaveAHubFragment extends Fragment {
    private Button yesButton;
    private Button noButton;
    private ScleraLinkView dashboardLink;

    public static HaveAHubFragment newInstance() {
        return new HaveAHubFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_have_a_hub, container, false);

        yesButton = view.findViewById(R.id.yes);
        noButton = view.findViewById(R.id.no);
        dashboardLink = view.findViewById(R.id.dashboard);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        noButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Activity activity = getActivity();
                if (activity != null) {
                    activity.startActivity(ProductCatalogActivity.createIntentForNoHubProducts(getActivity()));
                }
            }
        });

        yesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BackstackManager.getInstance().navigateToFragment(HubKitFragment.newInstanceHidingToolbar(true), true);
                if (getActivity() instanceof DashboardActivity) {
                    ((DashboardActivity) getActivity()).setIsHub(true);
                }
            }
        });

        dashboardLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BackstackManager.getInstance().navigateBackToFragment(HomeFragment.newInstance());

                ActionBar actionBar = ((BaseActivity) getActivity()).getSupportActionBar();

                if (actionBar != null) {
                    actionBar.show();
                }
            }
        });
    }
}

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
package arcus.app.account.registration;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import arcus.app.R;
import arcus.app.activities.DashboardActivity;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.fragments.BaseFragment;
import arcus.app.common.image.ImageManager;
import arcus.app.common.image.Wallpaper;
import arcus.app.common.popups.NoHubRequiredDevicesPopup;
import arcus.app.common.view.Version1TextView;
import arcus.app.device.pairing.catalog.controller.ProductCatalogSequenceController;
import arcus.app.device.pairing.hub.HubParentFragment;


public class AccountGettingStartedFragment extends BaseFragment {

    private LinearLayout hasHubButton;
    private LinearLayout doesntHaveHubButton;
    private Version1TextView infoText;

    public static AccountGettingStartedFragment newInstance() {
        return new AccountGettingStartedFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        hasHubButton = (LinearLayout) view.findViewById(R.id.hub_button);
        doesntHaveHubButton = (LinearLayout) view.findViewById(R.id.no_hub_button);
        infoText = (Version1TextView) view.findViewById(R.id.info_text);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        setTitle();

        ImageManager.with(getActivity()).setWallpaper(Wallpaper.ofCurrentPlace().lightend());

        hasHubButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BackstackManager.getInstance().navigateBack();  // Close this popup first
                BackstackManager.getInstance().navigateToFragment(HubParentFragment.newInstance(), true);
                ((DashboardActivity) getActivity()).setIsHub(true);
            }
        });

        doesntHaveHubButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BackstackManager.getInstance().navigateBack();  // Close this popup first
                new ProductCatalogSequenceController().startSequence(getActivity(), null, true);
            }
        });

        SpannableStringBuilder builder = new SpannableStringBuilder();
        builder.append(getString(R.string.account_registration_no_hub_info));
        builder.append("   ");      // Some margin between the text and info icon
        builder.setSpan(new ImageSpan(getActivity(), R.drawable.button_info), builder.length() - 1, builder.length(), 0);
        infoText.setText(builder);

        infoText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BackstackManager.getInstance().navigateToFloatingFragment(NoHubRequiredDevicesPopup.newInstance(), NoHubRequiredDevicesPopup.class.getSimpleName(), true);
            }
        });
    }

    @Nullable
    @Override
    public String getTitle() {
        return getString(R.string.account_registration_getting_started);
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_account_getting_started;
    }
}

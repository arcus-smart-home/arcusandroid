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


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import arcus.cornea.common.PresentedView;
import arcus.app.R;
import arcus.app.common.fragments.BaseFragment;
import arcus.app.common.utils.ActivityUtils;
import arcus.app.common.view.Version1TextView;
import arcus.app.subsystems.alarm.promonitoring.presenters.AlarmProviderOfflinePresenter;

public class ProMonitoringMoreDevicesNeededFragment extends BaseFragment implements PresentedView {

    private final static String IMAGE_RES_ID = "IMAGE";
    private final static String TITLE = "TITLE";
    private final static String SCREEN_TITLE = "SCREEN_TITLE";
    private final static String SCREEN_SUBTITLE = "SCREEN_SUBTITLE";
    private final static String SCREEN_DESCRIPTION = "SCREEN_DESCRIPTION";
    private final static String BUTTON_TEXT = "BUTTON_TEXT";

    private AlarmProviderOfflinePresenter presenter = new AlarmProviderOfflinePresenter();
    ImageView image;
    Version1TextView screenTitle;
    Version1TextView screenSubTitle;
    Version1TextView screenDescription;
    TextView shopButton;

    @NonNull
    public static ProMonitoringMoreDevicesNeededFragment newInstance(int imageResId, String title, String screenTitle, String screenSubtitle, String screenDescription, String buttonText){
        ProMonitoringMoreDevicesNeededFragment instance = new ProMonitoringMoreDevicesNeededFragment();
        Bundle arguments = new Bundle();

        arguments.putInt(IMAGE_RES_ID, imageResId);
        arguments.putString(TITLE, title);
        arguments.putString(SCREEN_TITLE, screenTitle);
        arguments.putString(SCREEN_SUBTITLE, screenSubtitle);
        arguments.putString(SCREEN_DESCRIPTION, screenDescription);
        arguments.putString(BUTTON_TEXT, buttonText);
        instance.setArguments(arguments);

        return instance;
    }

    public ProMonitoringMoreDevicesNeededFragment() {
        // Required empty public constructor
    }


    @Nullable
    @Override
    public String getTitle() {
        return getArguments().getString(TITLE, null);
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_alarm_type_more_devices;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        image = (ImageView) view.findViewById(R.id.image);
        screenTitle = (Version1TextView) view.findViewById(R.id.screen_title);
        screenSubTitle = (Version1TextView) view.findViewById(R.id.screen_subtitle);
        screenDescription = (Version1TextView) view.findViewById(R.id.screen_description);
        shopButton = (TextView) view.findViewById(R.id.button);

        image.setBackground(ContextCompat.getDrawable(getContext(), getArguments().getInt(IMAGE_RES_ID, -1)));
        screenTitle.setText(getArguments().getString(SCREEN_TITLE));
        screenSubTitle.setText(getArguments().getString(SCREEN_SUBTITLE));
        screenDescription.setText(getArguments().getString(SCREEN_DESCRIPTION));
        shopButton.setText(getArguments().getString(BUTTON_TEXT));
        shopButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                launchShop();
            }
        });
        setTitle();
        return view;
    }

    private void launchShop(){
        String buttonText = getArguments().getString(BUTTON_TEXT);

        if(buttonText == getResources().getString(R.string.smoke_alarm_devices_needed_button)) {
            ActivityUtils.launchShopSmokeNow();
        } else if(buttonText == getResources().getString(R.string.co_alarm_devices_needed_button)) {
            ActivityUtils.launchShopSmokeNow();
        } else if(buttonText == getResources().getString(R.string.waterleak_alarm_devices_needed_button)) {
            ActivityUtils.launchShopWaterNow();
        } else if(buttonText == getResources().getString(R.string.security_alarm_devices_needed_button).toUpperCase()) {
            ActivityUtils.launchShopSecurityNow();
        } else {
            ActivityUtils.launchShopNow();
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.startPresenting(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        presenter.stopPresenting();
    }

    @Override
    public void onPending(@Nullable Integer progressPercent) {
        // Nothing to do.
    }

    @Override
    public void onError(@NonNull Throwable throwable) {
        // Nothing to do.
    }

    @Override
    public void updateView(@NonNull Object model) {
        // Nothing to do.
    }
}

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
package arcus.app.subsystems.alarm.promonitoring.settings;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import arcus.cornea.common.PresentedView;
import arcus.app.R;
import arcus.app.common.sequence.ReturnToSenderSequenceController;
import arcus.app.common.sequence.SequencedFragment;
import arcus.app.common.utils.ActivityUtils;
import arcus.app.common.view.Version1Button;
import arcus.app.subsystems.alarm.promonitoring.presenters.AlarmProviderOfflinePresenter;



public class ProMonitoringNoMotionSensors extends SequencedFragment<ReturnToSenderSequenceController> implements PresentedView {

    private AlarmProviderOfflinePresenter presenter = new AlarmProviderOfflinePresenter();
    private Version1Button shopButton;

    public static ProMonitoringNoMotionSensors newInstance() {
        return new ProMonitoringNoMotionSensors();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        shopButton = (Version1Button) view.findViewById(R.id.shop_button);

        shopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityUtils.launchShopSecurityNow();
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.startPresenting(this);
        getActivity().setTitle(getTitle());
    }

    @Override
    public void onPause() {
        super.onPause();
        presenter.stopPresenting();
    }

    @Nullable
    @Override
    public String getTitle() {
        return getString(R.string.alarm_requirements);
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_pro_monitoring_no_sensors;
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

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
package arcus.app.subsystems.camera;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import arcus.app.R;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.fragments.BaseFragment;
import arcus.app.common.view.Version1Toggle;
import arcus.app.subsystems.alarm.promonitoring.presenters.AlarmMoreContract;
import arcus.app.subsystems.alarm.promonitoring.presenters.AlarmMorePresenter;

public class CameraMoreFragment extends BaseFragment implements View.OnClickListener, AlarmMoreContract.AlarmMoreView {
    private AlarmMorePresenter presenter = new AlarmMorePresenter();
    private Version1Toggle toggle;

    public static CameraMoreFragment newInstance() {
        return new CameraMoreFragment();
    }

    @Nullable @Override public View onCreateView(
          LayoutInflater inflater,
          @Nullable ViewGroup container,
          @Nullable Bundle savedInstanceState
    ) {
        View rootView = super.onCreateView(inflater, container, savedInstanceState);
        if (rootView == null) {
            return null;
        }

        View storageContainer = rootView.findViewById(R.id.storage_more_container);
        if (storageContainer != null) {
            storageContainer.setOnClickListener(this);
        }

        toggle = (Version1Toggle) rootView.findViewById(R.id.alarm_toggle);
        toggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                presenter.setRecordOnAlarmValue(toggle.isChecked());
            }
        });

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        presenter.startPresenting(this);
        presenter.requestUpdate();
    }

    @Override public void onClick(View v) {
        if (v == null) {
            return;
        }

        switch (v.getId()) {
            case R.id.storage_more_container:
                BackstackManager.getInstance().navigateToFragment(VideoStorageFragment.newInstance(), true);
            default:
                break; /* no-op */
        }
    }

    @Nullable @Override public String getTitle() {
        return null;
    }

    @Override public Integer getLayoutId() {
        return R.layout.camera_more_fragment;
    }

    @Override
    public void onPending(@Nullable Integer progressPercent) {

    }

    @Override
    public void onError(@NonNull Throwable throwable) {

    }

    @Override
    public void updateView(@NonNull AlarmMoreContract.AlarmMoreModel model) {
        toggle.setChecked(model.recordOnSecurity);
    }

    @Override
    public void presentNoDevicesAvailable() {

    }
}

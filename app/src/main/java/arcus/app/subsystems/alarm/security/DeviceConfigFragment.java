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
package arcus.app.subsystems.alarm.security;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ToggleButton;

import arcus.cornea.common.PresentedView;
import arcus.cornea.subsystem.security.SecurityDeviceConfigController;
import com.iris.client.event.ListenerRegistration;
import arcus.app.R;
import arcus.app.common.fragments.BaseFragment;
import arcus.app.common.image.ImageManager;
import arcus.app.common.image.Wallpaper;

public class DeviceConfigFragment extends BaseFragment implements SecurityDeviceConfigController.SelectedDeviceCallback,
        View.OnClickListener, PresentedView {

    private SecurityDeviceConfigController mConfigController;
    private ListenerRegistration mSelectedDeviceListener;

    private String mSelectedDeviceId;

    private ToggleButton mOnCheck;
    private ToggleButton mOnPartialCheck;
    private ToggleButton mPartialCheck;
    private ToggleButton mNotParticipatingCheck;

    private String mTitle;

    @NonNull
    public static DeviceConfigFragment newInstance(String deviceName, String deviceId) {
        DeviceConfigFragment fragment = new DeviceConfigFragment();
        fragment.setSelectedDeviceId(deviceId);
        fragment.setTitle(deviceName);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        View onView = view.findViewById(R.id.on_cell);
        View onPartialView = view.findViewById(R.id.on_partial_cell);
        View partialView = view.findViewById(R.id.partial_cell);
        View notParticipatingView = view.findViewById(R.id.not_participating_cell);

        mOnCheck = (ToggleButton) view.findViewById(R.id.on_checkbox);
        mOnPartialCheck = (ToggleButton) view.findViewById(R.id.on_partial_checkbox);
        mPartialCheck = (ToggleButton) view.findViewById(R.id.partial_checkbox);
        mNotParticipatingCheck = (ToggleButton) view.findViewById(R.id.not_participating_checkbox);

        onView.setOnClickListener(this);
        onPartialView.setOnClickListener(this);
        partialView.setOnClickListener(this);
        notParticipatingView.setOnClickListener(this);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mConfigController == null) {
            mConfigController = SecurityDeviceConfigController.instance();
        }

        if (mSelectedDeviceId != null && !mSelectedDeviceId.equals(""))
            mSelectedDeviceListener = mConfigController.setSelectedDeviceCallback(mSelectedDeviceId, this);

        ImageManager.with(getContext()).setWallpaper(Wallpaper.ofCurrentPlace().darkened());
        getActivity().setTitle(getTitle());
    }

    @Override
    public void onPause() {
        super.onPause();
        mSelectedDeviceListener.remove();
    }

    @Override
    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        this.mTitle = title;
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_security_alarm_device_config;
    }

    @Override
    public void updateSelected(String name, SecurityDeviceConfigController.Mode mode) {
        setSelection(mode);
    }

    private void setSelection(SecurityDeviceConfigController.Mode mode) {
        if (mode == SecurityDeviceConfigController.Mode.ON_ONLY) {
            mOnCheck.setChecked(true);
            mOnPartialCheck.setChecked(false);
            mPartialCheck.setChecked(false);
            mNotParticipatingCheck.setChecked(false);
        } else if (mode == SecurityDeviceConfigController.Mode.ON_AND_PARTIAL) {
            mOnCheck.setChecked(false);
            mOnPartialCheck.setChecked(true);
            mPartialCheck.setChecked(false);
            mNotParticipatingCheck.setChecked(false);
        } else if (mode == SecurityDeviceConfigController.Mode.PARTIAL_ONLY) {
            mOnCheck.setChecked(false);
            mOnPartialCheck.setChecked(false);
            mPartialCheck.setChecked(true);
            mNotParticipatingCheck.setChecked(false);
        } else if (mode == SecurityDeviceConfigController.Mode.NOT_PARTICIPATING) {
            mOnCheck.setChecked(false);
            mOnPartialCheck.setChecked(false);
            mPartialCheck.setChecked(false);
            mNotParticipatingCheck.setChecked(true);
        }
    }

    @Override
    public void onClick(@NonNull View v) {
        final int id = v.getId();
        switch (id) {
            case R.id.on_cell:
                setSelection(SecurityDeviceConfigController.Mode.ON_ONLY);
                if (mConfigController != null) mConfigController.setMode(mSelectedDeviceId, SecurityDeviceConfigController.Mode.ON_ONLY);
                break;
            case R.id.on_partial_cell:
                setSelection(SecurityDeviceConfigController.Mode.ON_AND_PARTIAL);
                if (mConfigController != null) mConfigController.setMode(mSelectedDeviceId, SecurityDeviceConfigController.Mode.ON_AND_PARTIAL);
                break;
            case R.id.partial_cell:
                setSelection(SecurityDeviceConfigController.Mode.PARTIAL_ONLY);
                if (mConfigController != null) mConfigController.setMode(mSelectedDeviceId, SecurityDeviceConfigController.Mode.PARTIAL_ONLY);
                break;
            case R.id.not_participating_cell:
                setSelection(SecurityDeviceConfigController.Mode.NOT_PARTICIPATING);
                if (mConfigController != null) mConfigController.setMode(mSelectedDeviceId, SecurityDeviceConfigController.Mode.NOT_PARTICIPATING);
                break;
        }
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

    public void setSelectedDeviceId(String selectedDeviceId) {
        this.mSelectedDeviceId = selectedDeviceId;
    }
}

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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import arcus.cornea.error.ErrorModel;
import arcus.cornea.subsystem.security.SecuritySettingsController;
import arcus.cornea.subsystem.security.model.SettingsModel;
import com.iris.client.event.ListenerRegistration;
import arcus.app.R;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.fragments.BaseFragment;
import arcus.app.common.popups.TimePickerPopup;
import arcus.app.common.view.Version1TextView;

import java.util.concurrent.TimeUnit;


public class GracePeriodFragment extends BaseFragment implements SecuritySettingsController.Callback {

    private SecuritySettingsController mSettingsController;
    private ListenerRegistration mListener;

    private Version1TextView mOnExitTime;
    private Version1TextView mOnEntranceTime;
    private Version1TextView mPartialExitTime;
    private Version1TextView mPartialEntranceTime;

    private int mOnExitSeconds;
    private int mOnEntranceSeconds;
    private int mPartialExitSeconds;
    private int mPartialEntranceSeconds;

    @NonNull
    public static GracePeriodFragment newInstance(){
        GracePeriodFragment fragment = new GracePeriodFragment();

        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        View onExitCell = view.findViewById(R.id.on_exit_cell);
        View onEntranceCell = view.findViewById(R.id.on_entrance_cell);
        View partialExitCell = view.findViewById(R.id.partial_exit_cell);
        View partialEntranceCell = view.findViewById(R.id.partial_entrance_cell);

        mOnExitTime = (Version1TextView) view.findViewById(R.id.on_exit_time);
        mOnEntranceTime = (Version1TextView) view.findViewById(R.id.on_entrance_time);
        mPartialExitTime = (Version1TextView) view.findViewById(R.id.partial_exit_time);
        mPartialEntranceTime = (Version1TextView) view.findViewById(R.id.partial_entrance_time);

        onExitCell.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerPopup fragment = TimePickerPopup.newInstance("DELAY", // Title
                        "MIN", // Left Title
                        "SEC", // Right Title
                        getMinutes(mOnExitSeconds), // Left Value
                        0, // Left Min
                        60, // Left Max
                        getSeconds(mOnExitSeconds), // Right Value
                        0, // Right Min
                        60); // Right Max
                fragment.setOnTimeChangedListener(new TimePickerPopup.OnTimeChangedListener() {
                    @Override
                    public void onTimeChanged(int leftValue, int rightValue) {
                        mSettingsController.setExitDelayOnSec(getSeconds(leftValue, rightValue));
                    }

                    @Override
                    public void onAccept(int leftValue, int rightValue) {

                    }

                    @Override
                    public void onExit(int leftValue, int rightValue) {

                    }
                });
                BackstackManager.getInstance().navigateToFloatingFragment(fragment, fragment.getClass().getCanonicalName(), true);
            }
        });

        onEntranceCell.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerPopup fragment = TimePickerPopup.newInstance("DELAY", // Title
                        "MIN", // Left Title
                        "SEC", // Right Title
                        getMinutes(mOnEntranceSeconds), // Left Value
                        0, // Left Min
                        60, // Left Max
                        getSeconds(mOnEntranceSeconds), // Right Value
                        0, // Right Min
                        60); // Right Max
                fragment.setOnTimeChangedListener(new TimePickerPopup.OnTimeChangedListener() {
                    @Override
                    public void onTimeChanged(int leftValue, int rightValue) {
                        mSettingsController.setEntranceDelayOnSec(getSeconds(leftValue, rightValue));
                    }

                    @Override
                    public void onAccept(int leftValue, int rightValue) {

                    }

                    @Override
                    public void onExit(int leftValue, int rightValue) {

                    }
                });
                BackstackManager.getInstance().navigateToFloatingFragment(fragment, fragment.getClass().getCanonicalName(), true);
            }
        });

        partialExitCell.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerPopup fragment = TimePickerPopup.newInstance("DELAY", // Title
                        "MIN", // Left Title
                        "SEC", // Right Title
                        getMinutes(mPartialExitSeconds), // Left Value
                        0, // Left Min
                        60, // Left Max
                        getSeconds(mPartialExitSeconds), // Right Value
                        0, // Right Min
                        60); // Right Max
                fragment.setOnTimeChangedListener(new TimePickerPopup.OnTimeChangedListener() {
                    @Override
                    public void onTimeChanged(int leftValue, int rightValue) {
                        mSettingsController.setExitDelayPartialSec(getSeconds(leftValue, rightValue));
                    }

                    @Override
                    public void onAccept(int leftValue, int rightValue) {

                    }

                    @Override
                    public void onExit(int leftValue, int rightValue) {

                    }
                });
                BackstackManager.getInstance().navigateToFloatingFragment(fragment, fragment.getClass().getCanonicalName(), true);
            }
        });

        partialEntranceCell.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerPopup fragment = TimePickerPopup.newInstance("DELAY", // Title
                        "MIN", // Left Title
                        "SEC", // Right Title
                        getMinutes(mPartialEntranceSeconds), // Left Value
                        0, // Left Min
                        60, // Left Max
                        getSeconds(mPartialEntranceSeconds), // Right Value
                        0, // Right Min
                        60); // Right Max
                fragment.setOnTimeChangedListener(new TimePickerPopup.OnTimeChangedListener() {
                    @Override
                    public void onTimeChanged(int leftValue, int rightValue) {
                        mSettingsController.setEntranceDelayPartialSec(getSeconds(leftValue, rightValue));
                    }

                    @Override
                    public void onAccept(int leftValue, int rightValue) {

                    }

                    @Override
                    public void onExit(int leftValue, int rightValue) {

                    }
                });
                BackstackManager.getInstance().navigateToFloatingFragment(fragment, fragment.getClass().getCanonicalName(), true);
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mSettingsController == null) {
            mSettingsController = SecuritySettingsController.instance();
        }

        mListener = mSettingsController.setCallback(this);

        getActivity().setTitle(getTitle());
    }

    @Override
    public void onPause() {
        super.onPause();
        mListener.remove();
    }

    @NonNull
    @Override
    public String getTitle() {
        return "GRACE PERIODS";
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_security_alarm_grace_periods;
    }

    @Override
    public void updateSettings(@NonNull SettingsModel model) {
        mOnExitSeconds = model.getExitDelayOnSec();
        mOnExitTime.setText(timeFormat(getMinutes(mOnExitSeconds), getSeconds(mOnExitSeconds)));

        mOnEntranceSeconds = model.getEntranceDelayOnSec();
        mOnEntranceTime.setText(timeFormat(getMinutes(mOnEntranceSeconds), getSeconds(mOnEntranceSeconds)));

        mPartialExitSeconds = model.getExitDelayPartialSec();
        mPartialExitTime.setText(timeFormat(getMinutes(mPartialExitSeconds), getSeconds(mPartialExitSeconds)));

        mPartialEntranceSeconds = model.getEntranceDelayPartialSec();
        mPartialEntranceTime.setText(timeFormat(getMinutes(mPartialEntranceSeconds), getSeconds(mPartialEntranceSeconds)));
    }

    @Override
    public void showError(ErrorModel error) {

    }

    private int getMinutes(int seconds) {
        int minutes = (int) TimeUnit.SECONDS.toMinutes(seconds);
        return minutes;
    }

    private int getSeconds(int seconds) {
        int minutes = (int) TimeUnit.SECONDS.toMinutes(seconds);
        seconds -= TimeUnit.MINUTES.toSeconds(minutes);
        return seconds;
    }

    private int getSeconds(int minute, int seconds) {
        return minute * 60 + seconds;
    }

    private String timeFormat(int minutes, int seconds) {
        if (minutes == 0 && seconds == 0)
            return "0s";

        StringBuilder sb = new StringBuilder();

        if (minutes > 0)
            sb.append(minutes + "m");
        if (seconds > 0)
            sb.append(seconds + "s");

        return sb.toString();
    }
}

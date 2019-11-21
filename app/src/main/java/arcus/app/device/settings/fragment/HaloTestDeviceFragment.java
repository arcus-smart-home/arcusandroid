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
package arcus.app.device.settings.fragment;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;

import arcus.cornea.device.smokeandco.HaloContract;
import arcus.cornea.device.smokeandco.HaloDeviceTestPresenter;
import arcus.cornea.device.smokeandco.HaloModel;
import com.iris.client.capability.Halo;
import arcus.app.R;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.error.ErrorManager;
import arcus.app.common.fragments.BaseFragment;
import arcus.app.common.fragments.IClosedFragment;
import arcus.app.common.popups.InfoTextPopup;
import arcus.app.common.utils.GlobalSetting;
import arcus.app.common.view.Version1Button;
import arcus.app.common.view.Version1TextView;

public class HaloTestDeviceFragment extends BaseFragment implements HaloContract.View, IClosedFragment {
    public static String DEVICE_ADDRESS = "DEVICE_ADDRESS";

    private String deviceAddress;
    private HaloContract.DeviceTestPresenter presenter;
    private Version1TextView topTextView, resultOverallStatus, resultDetails;
    private View testResultContainer, supportLinkContainer;

    public static HaloTestDeviceFragment newInstance(String deviceAddress) {
        HaloTestDeviceFragment fragment = new HaloTestDeviceFragment();
        Bundle args = new Bundle();
        args.putString(DEVICE_ADDRESS, deviceAddress);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onResume () {
        super.onResume();
        Activity activity = getActivity();
        if (activity != null) {
            activity.setTitle(getTitle());
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        hideProgressBar();
    }

    @Override public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if (args == null) {
            return;
        }

        deviceAddress = args.getString(DEVICE_ADDRESS, null);
    }

    @Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (view == null) {
            return;
        }

        testResultContainer = view.findViewById(R.id.test_result_container);
        resultOverallStatus = (Version1TextView) view.findViewById(R.id.result_status_overview);
        resultDetails = (Version1TextView) view.findViewById(R.id.result_status_description);

        supportLinkContainer = view.findViewById(R.id.test_failed_support_container);
        View clickableSupportLink = view.findViewById(R.id.clickable_support_link);
        clickableSupportLink.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                Activity activity = getActivity();
                if (activity != null) {
                    activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(GlobalSetting.SUPPORT_URL)));
                }
            }
        });

        topTextView = (Version1TextView) view.findViewById(R.id.first_text_view);
        Version1Button arcusButton = (Version1Button) view.findViewById(R.id.page_bottom_button);
        if (arcusButton != null) {
            arcusButton.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    testDevice();
                }
            });
        }
    }

    @Override public void onStart() {
        super.onStart();
        if (presenter == null) {
            presenter = new HaloDeviceTestPresenter(deviceAddress);
        }

        // TODO: HALO 8/30/16 Disable button until updateView is called?
        presenter.startPresenting(this);
    }

    void testDevice() {
        if (presenter == null) {
            logger.error("Presenter was null - how...?");
            return;
        }

        // TODO: Progress bar control should be moved into presenter
        showProgressBar();

        InfoTextPopup popup = InfoTextPopup.newInstance(R.string.halo_test_sent_description, R.string.test_text);
        BackstackManager.getInstance().navigateToFloatingFragment(popup, popup.getClass().getSimpleName(), true);

        // TODO: HALO 8/30/16 Does this need a callback so we can disable/enable the button while the message is in flight?
        presenter.testDevice();
    }

    @Override public void onError(Throwable throwable) {
        hideProgressBar();
        ErrorManager.in(getActivity()).showGenericBecauseOf(throwable);
    }

    @Override
    public void onPending(Integer progressPercentage) {
        showProgressBar();
    }

    @Override public void updateView(HaloModel model) {
        hideProgressBar();
        if (topTextView == null) {
            return;
        }

        String dateText = model.getLastTested();
        if (dateText == null) {
            dateText = getString(R.string.never_tested_text);
        }
        topTextView.setText(getString(R.string.last_tested_with_ph, dateText));

        if (model.isLastTestPassed()) {
            supportLinkContainer.setVisibility(View.GONE);
            resultDetails.setVisibility(View.GONE);

            resultOverallStatus.setText(R.string.success_text);
            testResultContainer.setVisibility(View.VISIBLE);
        }
        else {
            supportLinkContainer.setVisibility(View.VISIBLE);

            resultOverallStatus.setText(R.string.failed_text);
            testResultContainer.setVisibility(View.VISIBLE);

            if (TextUtils.isEmpty(model.getLastTestResult())) {
                resultDetails.setVisibility(View.GONE);
            } else {
                resultDetails.setText(errorStringValuesFrom(model.getLastTestResult()));
                resultDetails.setVisibility(View.VISIBLE);
            }
        }
    }

    @NonNull
    private String errorStringValuesFrom(String result) {
        if (TextUtils.isEmpty(result)) {
            return "";
        }

        if(Halo.REMOTETESTRESULT_FAIL_CO_SENSOR.equals(result)) {
            return getString(R.string.halo_fail_co_sensor);
        } else if(Halo.REMOTETESTRESULT_FAIL_ION_SENSOR.equals(result) || Halo.REMOTETESTRESULT_FAIL_PHOTO_SENSOR.equals(result)) {
            return getString(R.string.halo_fail_smoke_sensor);
        } else if(Halo.REMOTETESTRESULT_FAIL_TEMP_SENSOR.equals(result)) {
            return getString(R.string.halo_fail_temp_sensor);
        } else if(Halo.REMOTETESTRESULT_FAIL_WEATHER_RADIO.equals(result)) {
            return getString(R.string.halo_fail_weather_radio);
        } else {
            return getString(R.string.halo_fail_other);
        }
    }

    @Nullable @Override public String getTitle() {
        return getString(R.string.testing_text);
    }

    @Override public Integer getLayoutId() {
        return R.layout.fragment_test_device;
    }

    @Override
    public void onClosedFragment() {
        hideProgressBar();
    }
}

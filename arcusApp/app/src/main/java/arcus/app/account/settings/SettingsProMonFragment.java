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
package arcus.app.account.settings;

import android.Manifest;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import arcus.cornea.SessionController;
import arcus.app.R;

import arcus.app.account.settings.walkthroughs.ProMonSettingsCertificateFragment;
import arcus.app.activities.BaseActivity;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.error.ErrorManager;
import arcus.app.common.fragments.BaseFragment;
import arcus.app.common.utils.GlobalSetting;
import arcus.app.common.view.Version1Button;
import arcus.cornea.utils.Listeners;
import com.iris.client.event.Listener;
import arcus.cornea.provider.ProMonitoringSettingsProvider;
import com.iris.client.model.ProMonitoringSettingsModel;
import arcus.app.account.settings.contract.CertificateDownloadContract;
import arcus.app.account.settings.presenter.CertificateDownloadPresenter;
import arcus.app.subsystems.alarm.AlertFloatingFragment;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;




public class SettingsProMonFragment extends BaseFragment implements CertificateDownloadContract.CertificateDownloadView, BaseActivity.PermissionCallback {

    private static final String PLACE_ID = "PLACE_ID";

    private String viewingPlaceID, certificateUrl;
    private View promonPermitLayout, promonDirectionsLayout, promonGateAccessLayout,
            promonFirstRespondersLayout, proMonWhoLivesHereLayout;
    private Version1Button downloadCertificateButton;
    private ProMonitoringSettingsModel proMonSettingsModel;
    private CertificateDownloadPresenter presenter = new CertificateDownloadPresenter();
    private SettingsProMonFragment fragment;

    public static SettingsProMonFragment newInstance(@NonNull String placeID) {
        SettingsProMonFragment fragment = new SettingsProMonFragment();
        Bundle args = new Bundle(1);
        args.putString(PLACE_ID, placeID);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = super.onCreateView(inflater, container, savedInstanceState);
        viewingPlaceID = getArguments().getString(PLACE_ID);
        fragment = this;

        promonPermitLayout = rootView.findViewById(R.id.settings_promon_permit_layout);
        promonPermitLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BackstackManager.getInstance().navigateToFragment(ProMonSettingsPermitFragment.newInstance(viewingPlaceID), true);
            }
        });
        promonDirectionsLayout = rootView.findViewById(R.id.settings_promon_additional_directions_layout);
        promonDirectionsLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BackstackManager.getInstance().navigateToFragment(ProMonSettingsDirectionsFragment.newInstance(viewingPlaceID), true);
            }
        });
        promonGateAccessLayout = rootView.findViewById(R.id.settings_promon_gate_access_layout);
        promonGateAccessLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BackstackManager.getInstance().navigateToFragment(ProMonSettingsGateAccessFragment.newInstance(viewingPlaceID), true);
            }
        });
        promonFirstRespondersLayout = rootView.findViewById(R.id.settings_promon_first_responder_info_layout);
        promonFirstRespondersLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BackstackManager.getInstance().navigateToFragment(ProMonSettingsFirstResponderFragment.newInstance(viewingPlaceID), true);
            }
        });
        proMonWhoLivesHereLayout = rootView.findViewById(R.id.settings_promon_who_lives_here_layout);
        proMonWhoLivesHereLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BackstackManager.getInstance().navigateToFragment(ProMonSettingsWhoLivesHereFragment.newInstance(viewingPlaceID), true);
            }
        });

        downloadCertificateButton = (Version1Button) rootView.findViewById(R.id.settings_promon_download_certificate_button);
        downloadCertificateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((BaseActivity)getActivity()).setPermissionCallback(fragment);
                ArrayList<String> permissions = new ArrayList<String>();
                permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                ((BaseActivity)getActivity()).checkPermission(permissions, GlobalSetting.PERMISSION_WRITE_EXTERNAL_STORAGE, R.string.permission_rationale_storage_promon_cert);
            }
        });

        SessionController.instance().isAccountOwner(viewingPlaceID).onSuccess(Listeners.runOnUiThread(new Listener<Boolean>() {
            @Override
            public void onEvent(Boolean isAccountOwner) {
                downloadCertificateButton.setVisibility(isAccountOwner ? View.VISIBLE : View.GONE);
            }
        }));

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        setTitle();
        presenter.startPresenting(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        presenter.stopPresenting();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        hideProgressBar();
    }


    @Override
    public void onDownloadError() {
        hideProgressBar();

        AlertFloatingFragment popup = AlertFloatingFragment.newInstance(
                getString(R.string.settings_promon_certificate_download_error_title).toUpperCase(),
                getString(R.string.settings_promon_certificate_download_error_text),
                getString(R.string.settings_promon_certificate_download_error_button_text),
                null,
                null,
                new AlertFloatingFragment.AlertButtonCallback() {
                    @Override public boolean topAlertButtonClicked() {
                        downloadCertificate();
                        return true;
                    }

                    @Override public boolean bottomAlertButtonClicked() { return false; }
                }
        );
        BackstackManager.getInstance().navigateToFloatingFragment(popup, popup.getClass().getSimpleName(), true);
    }

    @Override
    public void onDownloadComplete(@NonNull String filename) {
        hideProgressBar();
        BackstackManager.getInstance().navigateToFragment(ProMonSettingsCertificateFragment.newInstance(filename), true);
    }

    @Nullable
    @Override
    public String getTitle() {
        return getString(R.string.settings_promon_info_title);
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.settings_pro_monitoring;
    }


    @Override
    public void onPending(@Nullable Integer progressPercent) {

    }

    @Override
    public void onError(@NonNull Throwable throwable) {

    }

    @Override
    public void updateView(@NonNull ProMonitoringSettingsModel model) {

    }

    private void downloadCertificate() {
        showProgressBar();
        presenter.downloadCertificate(certificateUrl);
    }

    @Override
    public void permissionsUpdate(int permissionType, ArrayList<String> permissionsDenied, ArrayList<String> permissionsDeniedNeverAskAgain) {
        if(permissionsDenied.contains(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            ((BaseActivity)getActivity()).showSnackBarForPermissions(getString(R.string.permission_storage_denied_message));
            return;
        }
        ProMonitoringSettingsProvider.getInstance().getProMonSettings(viewingPlaceID).onSuccess(Listeners.runOnUiThread(new Listener<ProMonitoringSettingsModel>() {
            @Override
            public void onEvent(ProMonitoringSettingsModel model) {
                proMonSettingsModel = model;
                certificateUrl = proMonSettingsModel.getCertUrl();
                if (!StringUtils.isEmpty(certificateUrl)) {
                    downloadCertificate();
                }
            }
        })).onFailure(new Listener<Throwable>() {
            @Override
            public void onEvent(Throwable event) {
                ErrorManager.in(getActivity()).showGenericBecauseOf(event);
            }
        });

    }
}

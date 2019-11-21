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
package arcus.app.device.zwtools;

import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.iris.client.capability.HubZwave;
import arcus.app.R;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.error.ErrorManager;
import arcus.app.common.popups.AlertPopup;
import arcus.app.common.popups.InfoTextPopup;
import arcus.app.common.popups.ArcusFloatingFragment;
import arcus.app.common.sequence.SequencedFragment;
import arcus.app.common.view.Version1Button;
import arcus.app.common.view.Version1TextView;
import arcus.app.device.zwtools.controller.ZWaveNetworkRepairSequence;
import arcus.app.device.zwtools.presenter.ZWaveToolsContract;
import arcus.app.device.zwtools.presenter.ZWaveToolsPresenter;


public class ZWaveNetworkRepairProgressFragment extends SequencedFragment<ZWaveNetworkRepairSequence> implements ZWaveToolsContract.ZWaveToolsView {

    private ZWaveToolsPresenter presenter;

    private ProgressBar progressBar;
    private Version1TextView progressPercent;
    private Version1Button cancelButton;
    private Version1Button continueToDashboardButton;
    private Version1TextView title;
    private Version1TextView description;

    public static ZWaveNetworkRepairProgressFragment newInstance () {
        return new ZWaveNetworkRepairProgressFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        progressBar = (ProgressBar) view.findViewById(R.id.progress_bar);
        progressPercent = (Version1TextView) view.findViewById(R.id.progress_percent);
        cancelButton = (Version1Button) view.findViewById(R.id.cancel_button);
        continueToDashboardButton = (Version1Button) view.findViewById(R.id.continue_button);
        title = (Version1TextView) view.findViewById(R.id.title);
        description = (Version1TextView) view.findViewById(R.id.description);

        progressBar.getProgressDrawable().setColorFilter(Color.WHITE, android.graphics.PorterDuff.Mode.SRC_IN);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle(getTitle());

        continueToDashboardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showContinueToDashboardPopup();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showOnCancelPopup();
            }
        });

        if (presenter == null) {
            presenter = new ZWaveToolsPresenter();
        }

        presenter.startPresenting(this);

        if (getController().isRepairInProgress()) {
            presenter.requestUpdate();
        } else {
            presenter.startRebuilding();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        presenter.stopPresenting();
    }

    @Nullable
    @Override
    public String getTitle() {
        return getString(R.string.zwtools_repair_network);
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_zwave_network_repair_progress;
    }

    @Override
    public void onError(Throwable throwable) {
        ErrorManager.in(getActivity()).showGenericBecauseOf(throwable);
    }

    @Override
    public void onPending(Integer progressPercentage) {
        // Nothing to do
    }

    @Override
    public void updateView(HubZwave model) {
        if (model.getHealInProgress()) {
            title.setText(getString(R.string.zwtools_rebuilding_title));
            description.setText(getString(R.string.zwtools_rebuilding_desc));
            progressPercent.setText(getString(R.string.zwtools_percent_complete, (int)(model.getHealPercent() * 100)));
            progressBar.setProgress((int)(model.getHealPercent() * 100));

            cancelButton.setVisibility(View.VISIBLE);
            continueToDashboardButton.setText(getString(R.string.zwtools_continue_to_dashboard));
        }

        else {
            progressPercent.setText(getString(R.string.zwtools_percent_complete, 100));
            progressBar.setProgress(100);
            title.setText(getString(R.string.zwtools_rebuilding_success_title));
            description.setText(getString(R.string.zwtools_rebuilding_success_desc));

            cancelButton.setVisibility(View.GONE);
            continueToDashboardButton.setText(getString(R.string.zwtools_done));
        }
    }

    private void showContinueToDashboardPopup() {
        // Rebuild in progress; notify user that it will continue in background
        if (getController().isRepairInProgress()) {

            InfoTextPopup popup = InfoTextPopup.newInstance(R.string.zwtools_note_desc, R.string.zwtools_note_title);
            popup.setOnCloseHandler(new ArcusFloatingFragment.OnCloseHandler() {
                @Override
                public void onClose() {
                    endSequence(true);
                }
            });

            BackstackManager.getInstance().navigateToFloatingFragment(popup, popup.getClass().getSimpleName(), true);
        }

        // Rebuild is complete; terminate sequence
        else {
            endSequence(true);
        }
    }

    private void showOnCancelPopup () {
        AlertPopup popup = AlertPopup.newInstance(getString(R.string.zwtools_cancel_title),
                getString(R.string.zwtools_cancel_desc),
                getString(R.string.zwtools_yes),
                getString(R.string.zwtools_no),
                new AlertPopup.AlertButtonCallback() {
                    @Override
                    public boolean topAlertButtonClicked() {
                        // User clicked yes; cancel and transition to dashboard
                        presenter.cancelRebuilding();
                        BackstackManager.getInstance().navigateBack();
                        endSequence(false);
                        return false;
                    }

                    @Override
                    public boolean bottomAlertButtonClicked() {
                        // User clicked no; close dialog
                        return true;
                    }

                    @Override
                    public boolean errorButtonClicked() {
                        return false;
                    }

                    @Override
                    public void close() {}

                });

        popup.setCloseButtonVisible(false);
        BackstackManager.getInstance().navigateToFloatingFragment(popup, popup.getClass().getSimpleName(), true);
    }
}

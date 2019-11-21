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

import android.app.Activity;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import android.view.View;

import arcus.presentation.cameras.storage.VideoStoragePresenter;
import arcus.presentation.cameras.storage.VideoStoragePresenterImpl;
import arcus.presentation.cameras.storage.VideoStorageView;
import arcus.app.R;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.error.ErrorManager;
import arcus.app.common.fragments.BaseFragment;
import arcus.app.common.popups.AlertPopup;
import arcus.app.common.view.Version1Button;
import arcus.app.common.view.Version1TextView;

public class VideoStorageFragment extends BaseFragment implements VideoStorageView {
    private Version1Button cleanUpButton;
    private Version1Button deleteButton;
    private Version1TextView videoStorageText;
    private Version1TextView pinnedClipStorageText;
    private Version1TextView upgradePlanText;
    private Version1TextView serviceLevelText;
    private final VideoStoragePresenter videoStoragePresenter = new VideoStoragePresenterImpl();

    public static VideoStorageFragment newInstance() {
        return new VideoStorageFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        deleteButton = view.findViewById(R.id.delete_button);
        cleanUpButton = view.findViewById(R.id.cleanup_button);
        videoStorageText = view.findViewById(R.id.video_storage_text);
        pinnedClipStorageText = view.findViewById(R.id.pinned_clip_storage_text);
        upgradePlanText = view.findViewById(R.id.upgrade_plan_text);
        serviceLevelText = view.findViewById(R.id.service_level_header);
    }

    @NonNull
    @Override
    public String getTitle() {
        return getString(R.string.video_storage);
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_camera_video_storage;
    }

    @Override
    public void onResume() {
        super.onResume();

        cleanUpButton.setOnClickListener(v -> {
            AlertPopup popup = createPopup(
                    R.string.cleanup_cannot_be_undone,
                    R.string.yes_clean_up,
                    videoStoragePresenter::cleanUp);
            BackstackManager.getInstance().navigateToFloatingFragment(popup, popup.getClass().getSimpleName(),
                    true);
        });

        deleteButton.setOnClickListener(v -> {
            AlertPopup popup = createPopup(
                    R.string.delete_all_cannot_be_undone,
                    R.string.yes_delete_clips,
                    videoStoragePresenter::deleteAll);
            BackstackManager.getInstance().navigateToFloatingFragment(popup, popup.getClass().getSimpleName(),
                    true);
        });
        videoStoragePresenter.setView(this);

        Activity activity = getActivity();
        if (activity != null) {
            activity.setTitle(getTitle());
        }
    }

    protected AlertPopup createPopup(
            @StringRes int description,
            @StringRes int topButtonText,
            Runnable topClickAction
    ) {
        AlertPopup popup = AlertPopup.newInstance(
                getString(R.string.are_you_sure),
                getString(description),
                getString(topButtonText),
                getString(R.string.cancel),
                new AlertPopup.AlertButtonCallback() {
                    @Override public boolean topAlertButtonClicked() {
                        topClickAction.run();
                        return false;
                    }

                    @Override public boolean bottomAlertButtonClicked() { return true; }

                    @Override
                    public boolean errorButtonClicked() {
                        return false;
                    }

                    @Override
                    public void close() {}
                });

        popup.setCloseButtonVisible(false);
        return popup;
    }

    @Override
    public void onPause() {
        super.onPause();
        hideProgressBar();
        videoStoragePresenter.clearView();
    }

    @Override
    public void onLoading() {
        showProgressBar();
    }

    @Override
    public void showBasicConfiguration(int daysStored) {
        serviceLevelText.setText(getString(R.string.service_level_basic).toUpperCase());
        videoStorageText.setText(getString(R.string.video_storage_text, daysStored));
        pinnedClipStorageText.setVisibility(View.GONE);
        upgradePlanText.setVisibility(View.VISIBLE);
        cleanUpButton.setVisibility(View.GONE);

        deleteButton.setOnClickListener(v -> {
            AlertPopup popup = createPopup(
                    R.string.delete_all_basic_description,
                    R.string.yes_delete_clips,
                    videoStoragePresenter::deleteAll
            );
            BackstackManager.getInstance().navigateToFloatingFragment(popup, popup.getClass().getSimpleName(),
                    true);
        });
    }

    @Override
    public void showPremiumConfiguration(int daysStored, int pinnedCap) {
        serviceLevelText.setText(getString(R.string.service_level_premium).toUpperCase());
        videoStorageText.setText(getString(R.string.video_storage_text, daysStored));
        pinnedClipStorageText.setText(getString(R.string.pinned_clip_storage_text, pinnedCap));
        pinnedClipStorageText.setVisibility(View.VISIBLE);
        upgradePlanText.setVisibility(View.GONE);
        cleanUpButton.setVisibility(View.VISIBLE);
    }

    @Override
    public void showPremiumPromonConfiguration(int daysStored, int pinnedCap) {
        serviceLevelText.setText(getString(R.string.service_level_promon).toUpperCase());
        videoStorageText.setText(getString(R.string.video_storage_text, daysStored));
        pinnedClipStorageText.setText(getString(R.string.pinned_clip_storage_text, pinnedCap));
        pinnedClipStorageText.setVisibility(View.VISIBLE);
        upgradePlanText.setVisibility(View.GONE);
        cleanUpButton.setVisibility(View.VISIBLE);
    }

    @Override
    public void deleteAllSuccess() {
        hideProgressBar();
        BackstackManager.getInstance().navigateBack();
    }

    @Override
    public void showError(@NonNull Throwable throwable) {
        ErrorManager.in(getActivity()).showGenericBecauseOf(throwable);
        hideProgressBar();
    }
}

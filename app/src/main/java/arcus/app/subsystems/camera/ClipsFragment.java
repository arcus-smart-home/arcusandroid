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
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import arcus.cornea.controller.SubscriptionController;
import arcus.cornea.subsystem.cameras.ClipInteractionController;
import arcus.cornea.subsystem.cameras.ClipListingController;
import arcus.cornea.subsystem.cameras.ClipPreviewImageGetter;
import arcus.cornea.subsystem.cameras.model.ClipModel;
import arcus.cornea.subsystem.cameras.model.PlaybackModel;
import arcus.cornea.utils.Listeners;
import arcus.cornea.utils.LooperExecutor;
import com.iris.client.event.ListenerRegistration;
import arcus.app.R;
import arcus.app.activities.BaseActivity;
import arcus.app.activities.VideoActivity;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.error.ErrorManager;
import arcus.app.common.fragments.BaseFragment;
import arcus.app.common.fragments.ModalBottomSheet;
import arcus.app.common.image.ImageManager;
import arcus.app.common.image.ViewBackgroundTarget;
import arcus.app.common.image.Wallpaper;
import arcus.app.common.image.picasso.transformation.FilterBarTransformation;
import arcus.app.common.popups.CameraFiltersPopup;
import arcus.app.common.popups.InfoButtonPopup;
import arcus.app.common.popups.InfoTextPopup;
import arcus.app.common.utils.StringUtils;
import arcus.app.common.view.Version1TextView;
import arcus.app.common.view.Version1Toggle;
import arcus.app.subsystems.alarm.AlertFloatingFragment;
import arcus.app.subsystems.camera.adapter.ClipRecyclerViewAdapter;
import arcus.app.subsystems.camera.adapter.ClipRecyclerViewAdapter.ClickListener;
import arcus.app.subsystems.camera.adapter.EndlessScrollRecycleListener;
import arcus.app.subsystems.camera.controllers.ClipDownloadController;
import arcus.app.subsystems.camera.controllers.ClipDownloadControllerImpl;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

import kotlin.Unit;

public class ClipsFragment extends BaseFragment implements
      ClipListingController.Callback,
      ClipInteractionController.Callback,
      ClipPreviewImageGetter.Callback,
      ClickListener,
      ClipDownloadController.Callback {
    private ListenerRegistration mCallbackListener, clipListenerRegistration, clipImageUpdater, clipDownloadReg;

    private View noClipsContent, progressContainer;
    private ProgressBar progressBar;
    private ClipRecyclerViewAdapter adapter;
    private SwipeRefreshLayout swipeContainer;
    private Version1Toggle showPinnedClipsToggle;
    private Version1TextView noClipsText;
    private Version1TextView filter;
    private BaseTransientBottomBar snackbar = null;
    private ModalBottomSheet popupShowing = null;

    private AtomicBoolean loadingMore = new AtomicBoolean(false);

    private ClipInteractionController clipInteractionController;

    @NonNull public static ClipsFragment newInstance() {
        return new ClipsFragment();
    }

    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        noClipsContent = view.findViewById(R.id.no_clips);
        noClipsText = view.findViewById(R.id.no_clips_text);
        progressContainer = view.findViewById(R.id.download_container);
        progressBar = view.findViewById(R.id.download_progress);
        RelativeLayout filterContainer = view.findViewById(R.id.filter_container);
        showPinnedClipsToggle = view.findViewById(R.id.toggle);
        LinearLayout toolbarContainer = view.findViewById(R.id.filter_bar);
        filter = view.findViewById(R.id.filter);

        adapter = new ClipRecyclerViewAdapter(this, null, (BaseActivity) getActivity());
        adapter.setHasStableIds(true);
        adapter.addHeaders("All Devices", "All of Time?");

        swipeContainer = view.findViewById(R.id.swipeContainer);
        swipeContainer.setOnRefreshListener(() -> {
            adapter.removeAll();
            adapter.addHeaders("All Devices", "All of Time?");
            ClipListingController.instance().refresh();
        });
        swipeContainer.setColorSchemeResources(R.color.arcus_teal, R.color.arcus_light_blue);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerview);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        recyclerView.addOnScrollListener(new EndlessScrollRecycleListener(3) {
            @Override public void onLoadMore() {
                if (!loadingMore.getAndSet(true)) {
                    ClipListingController.instance().loadMoreClips();
                }
            }
        });

        filterContainer.setOnClickListener(view1 -> filterByDevice());
        updateFilterText();

        view.findViewById(R.id.cancel_active_download).setOnClickListener(v -> ClipDownloadControllerImpl.instance().cancelCurrentDownload());

        ViewBackgroundTarget vbt = new ViewBackgroundTarget(toolbarContainer);
        ImageManager.with(getContext())
            .putWallpaper(Wallpaper.ofCurrentPlace().darkened())
            .withFirstTransform(new FilterBarTransformation())
            .into(vbt)
            .execute();

        Version1TextView pinnedClipsText = view.findViewById(R.id.pinned_clips_text);
        if(SubscriptionController.isPremiumOrPro()){
            pinnedClipsText.setText(R.string.camera_pinned_clips_title);
            showPinnedClipsToggle.setOnClickListener(view12 -> filterByPinned(showPinnedClipsToggle.isChecked()));
        } else {
            pinnedClipsText.setText(R.string.camera_clips_clips_title);
            showPinnedClipsToggle.setVisibility(View.GONE);
        }
    }

    @NonNull @Override public String getTitle() {
        return "";
    }

    @Override public Integer getLayoutId() {
        return R.layout.fragment_camera_clips;
    }

    @Override public void onResume() {
        super.onResume();
        if (clipInteractionController == null) {
            clipInteractionController = new ClipInteractionController();
        }

        clipListenerRegistration = clipInteractionController.setCallback(this);
        clipImageUpdater = ClipPreviewImageGetter.instance().setCallback(this);
        if (!Listeners.isRegistered(mCallbackListener)) {
            mCallbackListener = ClipListingController.instance().setCallback(this);
        }

        if (!Listeners.isRegistered(clipDownloadReg)) {
            clipDownloadReg = ClipDownloadControllerImpl.instance().setCallback(this);
            ClipDownloadControllerImpl.instance().getCurrentDownloadStatus();
        }
    }

    @Override public void onDestroy() {
        super.onDestroy();
        Listeners.clear(clipDownloadReg);
        Listeners.clear(clipImageUpdater);
        Listeners.clear(clipListenerRegistration);
        Listeners.clear(mCallbackListener);
        ClipListingController.instance().removeTag("FAVORITE");
    }

    @Override public void onDestroyView() {
        super.onDestroyView();
        Listeners.clear(clipDownloadReg);
        Listeners.clear(clipImageUpdater);
        Listeners.clear(clipListenerRegistration);
        Listeners.clear(mCallbackListener);
        ClipListingController.instance().removeTag("FAVORITE");
    }

    @Override public void addClips(List<ClipModel> clips) {
        adapter.add(clips);
        updateVisibility();
        loadingMore.set(false);
        swipeContainer.setRefreshing(false);
    }

    protected void updateVisibility() {
        boolean noClips = hasNoClips();
        noClipsContent.setVisibility(noClips ? View.VISIBLE : View.GONE);
        if(ClipListingController.instance().getFilterEndTime() == null &&
                ClipListingController.instance().getFilterStartTime() == null &&
                ClipListingController.instance().getFilterDeviceAddress() == null) {
            noClipsText.setText(getString(R.string.no_video_clips));
        } else {
            noClipsText.setText(getString(R.string.no_video_clips_filter));
        }
    }

    @Override public void playbackURL(String recordingID, String url) {
        PlaybackModel model = new PlaybackModel();
        model.setRecordingID(recordingID);
        model.setIsNewStream(false);
        model.setIsStreaming(false);
        model.setUrl(url);
        model.setIsClip(true);

        Activity activity = getActivity();
        if (activity != null) {
            ClipModel clipModel = adapter.getClipByRecordingId(recordingID);

            String clipDate;
            if(clipModel != null && clipModel.getTime() != null) {
                clipDate = getDateString(clipModel.getTime());
            } else {
                clipDate = "";
            }

            startActivity(VideoActivity.getLaunchIntent(activity, model, clipDate));
        }
    }

    @Override public void downloadURL(String recordingID, String fileName, long estimatedSize, String url) {
        boolean success = ClipDownloadControllerImpl.instance().downloadClip(recordingID, fileName, estimatedSize, url, false);

        if (success) {
            InfoTextPopup popup = InfoTextPopup.newInstance(R.string.download_started_body, R.string.download_started);
            BackstackManager.getInstance().navigateToFloatingFragment(popup, popup.getClass().getName(), true);
        }
    }

    @Override public void clipDeleted(String recordingID) {
        adapter.remove(new ClipModel(recordingID));
        updateVisibility();
    }

    @Override public void onError(Throwable throwable) {
        ErrorManager.in(getActivity()).showGenericBecauseOf(throwable);
    }

    @Override public void addedImageToCache(final String recordingID) {
        LooperExecutor.getMainExecutor().execute(() -> {
            try {
                File file = ClipPreviewImageGetter.instance().getClipForID(recordingID);
                if (file != null) {
                    adapter.update(new ClipModel(recordingID, file));
                }
            }
            catch (Exception ex) {
                logger.error("Could not update clip preview image.", ex);
            }
        });
    }

    @Override public void playClip(String recordingID) {
        clipInteractionController.getPlaybackURL(recordingID);
    }

    @Override public void deleteClip(String recordingID) {
        clipInteractionController.delete(recordingID);
    }

    @Override public void downloadClip(final String recordingID) {
        //prompt for cellular users
        if(getActivity() != null) {
            ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            if(cm !=  null) {
                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                    InfoButtonPopup popup = InfoButtonPopup.newInstance(
                            getString(R.string.cellular_connection).toUpperCase(),
                            getString(R.string.cellular_connection_plan_message),
                            getString(R.string.cancel),
                            getString(R.string.yes),
                            false
                    );
                    popup.setCallback(correct -> {
                        if (!correct) {
                            clipInteractionController.getDownloadURL(recordingID);
                        }
                    });
                    BackstackManager.getInstance().navigateToFloatingFragment(popup, popup.getClass().getSimpleName(), true);
                } else {
                    clipInteractionController.getDownloadURL(recordingID);
                }
            }
        }
    }

    private void filterByPinned(boolean bPinned) {
        //Toast.makeText(getActivity(), "filter by pinned", Toast.LENGTH_LONG).show();
        if(bPinned) {
            ClipListingController.instance().addTag("FAVORITE");
        } else {
            ClipListingController.instance().removeTag("FAVORITE");
        }
        adapter.removeAll();
        adapter.addHeaders("All Devices", "All of Time?");
        ClipListingController.instance().refresh();
    }

    private void filterByDevice() {
        if(BackstackManager.getInstance().isFragmentOnStack(CameraFiltersPopup.class)) {
            return;
        }
        CameraFiltersPopup popup = CameraFiltersPopup.newInstance();
        popup.setCallback(() -> {
            adapter.removeAll();
            adapter.addHeaders("All Devices", "All of Time?");
            updateFilterText();
            ClipListingController.instance().refresh();
        });
        BackstackManager.getInstance().navigateToFragmentSlideAnimation(popup, popup.getClass().getCanonicalName(), true);
    }

    @Override
    public void pinUpdate(boolean bMakePinned, String recordingId) {
        clipInteractionController.updatePinState(recordingId, bMakePinned);
    }

    @Override
    public void exceededPinnedLimit(String recordingId) {
        LooperExecutor.getMainExecutor().execute(() -> {
            ClipModel clip = new ClipModel(recordingId);
            showMaxPinnedSnackbar();
            adapter.unsetPinned(clip);
        });
    }

    @Override
    public void showExpiredClipPopup(String recordingId) {
        if(popupShowing != null) {
            popupShowing.dismiss();
        }

        Generic2ButtonErrorPopup popup = Generic2ButtonErrorPopup.newInstance(
            getString(R.string.unpin_question),
            getString(R.string.unpinning_will_permanently_delete_the_video),
            getString(R.string.no),
            getString(R.string.yes)
        );
        popup.setBottomButtonListener(() -> {
            pinUpdate(false, recordingId);
            adapter.unsetPinned(new ClipModel(recordingId));
            return Unit.INSTANCE;
        });
        popup.show(getFragmentManager());
        popupShowing = popup;
    }

    private void showMaxPinnedSnackbar() {
        if (snackbar != null) {
            snackbar.dismiss();
            snackbar = null;
        }
        Activity activity = getActivity();
        if (activity != null) {
            ViewGroup rootContainer = getActivity().findViewById(R.id.coordinator_layout);
            AlertSnackBar newSnackBar = AlertSnackBar
                    .make(rootContainer, 7_000)
                    .setText(R.string.max_pinned_clips_reached);

            snackbar = newSnackBar;
            newSnackBar.show();
        }
    }

    @Override public void onDownloadFatalError(Throwable throwable) {
        ErrorManager.in(getActivity()).showGenericBecauseOf(throwable);
    }

    @Override public void onDownloadError(@ClipDownloadController.DownloadErrorStatus int errorType) {
        switch (errorType) {
            case ClipDownloadController.DOWNLOAD_IN_PROGRESS:
                if(BackstackManager.getInstance().isFragmentOnStack(InfoTextPopup.class)) {
                    BackstackManager.getInstance().navigateBack();
                }
                InfoTextPopup infoTextPopup = InfoTextPopup.newInstance(
                      R.string.camera_download_in_progress_desc, R.string.camera_download_in_progress_title
                );
                BackstackManager.getInstance().navigateToFloatingFragment(infoTextPopup, infoTextPopup.getClass().getCanonicalName(), true);
                break;

            case ClipDownloadController.INSUFFICIENT_STORAGE_SPACE:
                if(BackstackManager.getInstance().isFragmentOnStack(InfoTextPopup.class)) {
                    BackstackManager.getInstance().navigateBack();
                }
                AlertFloatingFragment spacePopup = AlertFloatingFragment.newInstance(
                      getString(R.string.insufficient_storage_space_title),
                      getString(R.string.insufficient_storage_space_desc), null, null, null
                );
                BackstackManager.getInstance().navigateToFloatingFragment(spacePopup, spacePopup.getClass().getCanonicalName(), true);
                break;

            case ClipDownloadController.STORAGE_NOT_AVAILABLE:
                if(BackstackManager.getInstance().isFragmentOnStack(InfoTextPopup.class)) {
                    BackstackManager.getInstance().navigateBack();
                }
                AlertFloatingFragment storagePopup = AlertFloatingFragment.newInstance(
                      getString(R.string.cannot_access_storage_title),
                      getString(R.string.cannot_access_storage_desc), null, null, null
                );
                BackstackManager.getInstance().navigateToFloatingFragment(storagePopup, storagePopup.getClass().getCanonicalName(), true);
                break;

            default:
                break;
        }
    }

    @Override public void onDownloadProgressChanged(
          @Nullable String recordingID,
          int newProgress,
          @ClipDownloadController.DownloadProgressStatus int status
    ) {
        switch (status) {
            case ClipDownloadController.STATUS_DOWNLOAD_CANCELED:
            case ClipDownloadController.STATUS_DOWNLOAD_COMPLETE:
                enableDownloadDelete(recordingID, true);
                progressContainer.setVisibility(View.GONE);
                progressBar.setProgress(0);
                break;

            case ClipDownloadController.STATUS_DOWNLOAD_RUNNING:
                if (newProgress >= 0) {
                    if (newProgress < 2) {
                        newProgress = 2; // So we're showing some progress in the progress bar initially; 2 is the new 0
                    }
                    enableDownloadDelete(recordingID, false);
                    progressContainer.setVisibility(View.VISIBLE);
                    progressBar.setProgress(newProgress);
                }
                break;

            default:
            case ClipDownloadController.STATUS_SIZE_UNKNOWN:
                break;
        }
    }

    @Override public void onDownloadComplete(@Nullable String recordingID, @ClipDownloadController.DownloadCompleteStatus int status) {
        enableDownloadDelete(recordingID, true);
        progressContainer.setVisibility(View.GONE);
        progressBar.setProgress(0);
    }

    private void updateFilterText() {
        int filterCount = 0;
        String filterText = getString(R.string.care_filter);
        if(ClipListingController.instance().getFilterDeviceAddress() != null && !ClipListingController.instance().getFilterDeviceAddress().equals("")) {
            filterCount++;
        }
        if(ClipListingController.instance().getFilterStartTime() != null || ClipListingController.instance().getFilterEndTime() != null) {
            filterCount++;
        }
        if(filterCount > 0) {
            filterText = filterText + " (" + filterCount + ")";
        }
        filter.setText(filterText);
    }

    protected void enableDownloadDelete(@Nullable String recordingID, boolean enable) {
        if (recordingID != null) {
            ClipModel update = new ClipModel(recordingID);
            update.setDownloadDeleteAvailable(enable);
            adapter.updatePlayDelete(update);
        }
    }

    protected boolean hasNoClips() {
        //for now the headers for filtering are adapter items
        return adapter.getItemCount() < 2;
    }

    public String getDateString (Date dateValue) {
        DateFormat timeFormat = new SimpleDateFormat(" h:mm a", Locale.US);
        DateFormat dateFormat = new SimpleDateFormat("EEE MMM d,", Locale.US);

        if (dateValue == null) {
            return "";
        }
        else if (StringUtils.isDateToday(dateValue)) {
            return getString(R.string.today) + "," + timeFormat.format(dateValue);
        }
        else if (StringUtils.isDateYesterday(dateValue)) {
            return getString(R.string.yesterday) + "," + timeFormat.format(dateValue);
        }
        else {
            return dateFormat.format(dateValue) + timeFormat.format(dateValue);
        }
    }
}

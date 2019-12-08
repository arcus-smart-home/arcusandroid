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
package arcus.app.pairing.hub.original;

import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.Editable;
import android.text.Html;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputLayout;
import com.iris.client.capability.Place;
import arcus.app.R;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.fragments.BaseFragment;
import arcus.app.common.fragments.IShowedFragment;
import arcus.app.common.models.RegistrationContext;
import arcus.app.common.popups.ScleraInfoButtonPopup;
import arcus.app.common.utils.ActivityUtils;
import arcus.app.common.utils.Errors;
import arcus.app.common.utils.GlobalSetting;
import arcus.app.common.view.ProgressBarFromToAnimation;
import arcus.app.common.view.ButtonColor;
import arcus.app.dashboard.HomeFragment;

import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HubPairFragment extends BaseFragment implements IShowedFragment {

    ProgressBar progressBar;
    View progressLayout;
    View hubSearchingLayout;
    View hubLongSearchLayout;
    View hubUpdatesLayout;
    View buttonLayout;

    TextView hubSearchingTitle;
    TextView hubSearchDescription;
    TextView hubActionTitle;
    TextView hubActionDescription;
    TextView progressPercent;
    TextView needHelp;
    EditText hubId;
    TextInputLayout hubIdContainer;
    TextView exitPairing;

    Button pairingButton;
    Button supportButton;
    ProgressBarTimerTask task;

    String title = "";
    String currentState = "";
    long pairingStartTime;
    long downloadStartTime;
    long installStartTime;

    int pairingTimeout = 2000*60;
    int installTimeout = 5000*60;
    int downloadTimeout = 11000*60;

    boolean longSearch = false;

    String promptTitle;
    String promptBody = null;

    Callback callback;
    Timer hubProgressBarTimer;

    public interface Callback {
        void restartPairing(String hubId);
        void cancelPairing();
        void navigateToNextStep();
        void updateCloseButton(boolean show);
        void errorBannerVisible(boolean visible);
        void setHubErrorBanner(int bannerColor, int bannerText, @Nullable String hubID, @Nullable String errorCode);
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    @NonNull
    public static HubPairFragment newInstance() {
        return new HubPairFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        progressBar = view.findViewById(R.id.progress);
        progressLayout = view.findViewById(R.id.progress_layout);
        hubSearchingLayout = view.findViewById(R.id.hub_searching_layout);
        hubLongSearchLayout = view.findViewById(R.id.hub_long_search);
        hubUpdatesLayout = view.findViewById(R.id.hub_updates_layout);
        buttonLayout = view.findViewById(R.id.button_layout);

        hubSearchingTitle = view.findViewById(R.id.hub_searching_title);
        hubSearchDescription = view.findViewById(R.id.hub_search_description);
        hubActionTitle = view.findViewById(R.id.hub_action_title);
        hubActionDescription = view.findViewById(R.id.hub_action_description);
        progressPercent = view.findViewById(R.id.progress_percent);
        needHelp = view.findViewById(R.id.need_help);

        pairingButton = view.findViewById(R.id.hub_pairing_button);
        supportButton = view.findViewById(R.id.call_support_button);

        hubId = view.findViewById(R.id.hub_edittext_id);
        hubIdContainer = view.findViewById(R.id.hub_edittext_id_container);
        exitPairing = view.findViewById(R.id.exit_pairing);

        int color = 0xFF00BFB3; //0xFFFFFFEE;
        int colorBackground =  0xFFffffff; //0xFFC3C3C3;
        progressBar.getIndeterminateDrawable().setColorFilter(colorBackground, PorterDuff.Mode.SRC_IN);
        progressBar.getProgressDrawable().setColorFilter(color, PorterDuff.Mode.SRC_IN);


        pairingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(callback != null) {
                    hubSearchDescription.setVisibility(View.VISIBLE);
                    hubIdContainer.setVisibility(View.GONE);
                    callback.errorBannerVisible(false);
                    callback.restartPairing(hubId.getText().toString());
                    hubId.setText("");
                    hubSearchingTitle.setText(String.format(getString(R.string.hub_searching_title), registrationContext.getHubID()));
                }
            }
        });

        supportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityUtils.callSupport();
            }
        });

        needHelp.setText(Html.fromHtml(getString(R.string.hub_longsearching_description)));
        needHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityUtils.launchUrl(GlobalSetting.HUB_TIMEOUT_HELP);
                if(callback != null) {
                    callback.cancelPairing();
                }
            }
        });

        exitPairing.setText(Html.fromHtml(getString(R.string.exit_pairing)));
        exitPairing.setOnClickListener(v -> showGoToDashboardPopup());

        hubId.setFilters(new InputFilter[] {new InputFilter.AllCaps()});
        hubIdContainer.setHint(getString(R.string.hub_id_hint_title));
        pairingButton.setEnabled(false);
        hubId.addTextChangedListener(new TextWatcher() {
            int len =0;
            @Override
            public void beforeTextChanged(@NonNull CharSequence s, int start, int count, int after) {
                len = s.length();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(@NonNull Editable s) {
                if(s.length() == 0) {
                    hubIdContainer.setError(getString(R.string.hub_id_missing));
                }
                if(s.length() == 3 && len < s.length()){
                    s.append("-");
                }

                Pattern mPattern = Pattern.compile("^[a-zA-Z]{3,}\\-\\d{4,}");

                Matcher matcher = mPattern.matcher(s.toString());
                if(!matcher.find() || s.length()>8) {
                    hubIdContainer.setError(getString(R.string.hub_id_wrong_format));
                    pairingButton.setEnabled(false);
                } else {
                    hubIdContainer.setError(null);
                    pairingButton.setEnabled(true);
                }
            }
        });
        return view;
    }


    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_pair_hub;
    }

    @Override
    public void onResume() {
        super.onResume();
        (getActivity()).setTitle(R.string.pairing_the_hub_title);
        if(callback != null) {
            // longSearch set to true when we show the "longSearch' layout in showLongSearch().
            // It is set to false whenever we re-start pairing in showSearching().
            if(!longSearch) {
                if(RegistrationContext.getInstance().getHubID() != null) {
                    callback.restartPairing(RegistrationContext.getInstance().getHubID());
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        RegistrationContext.getInstance().setHubID(null); // This RegistrationContext SO needs to go.
    }

    public void onShowedFragment() {
        hubSearchingTitle.setText(String.format(getString(R.string.hub_searching_title), registrationContext.getHubID()));

        if(callback != null) {
            callback.errorBannerVisible(false);
        }
    }

    public void startPairing() {
        pairingStartTime = System.currentTimeMillis();
        currentState = "";
        progressBar.setProgress(0);
        updateState(Place.RegisterHubV2Response.STATE_OFFLINE, 0);
    }

    public boolean hasInstallTimedOut() {
        if((System.currentTimeMillis()-installStartTime > installTimeout) && Place.RegisterHubV2Response.STATE_APPLYING.equals(currentState)) {
            return true;
        }
        return false;
    }

    public boolean hasDownloadTimedOut() {
        if((System.currentTimeMillis()-downloadStartTime > downloadTimeout) && Place.RegisterHubV2Response.STATE_DOWNLOADING.equals(currentState)) {
            return true;
        }
        return false;
    }

    public void updateState(String state, int progress) {
        hideLayouts();
        switch(state) {
            case Place.RegisterHubV2Response.STATE_OFFLINE:
                if(currentState.equals("")) {
                    //animate every 90th of the timeout (to sync progress bar at 90% matching transition to long search)
                    // before transitioning for long search
                    animateProgress(90, 1, pairingTimeout /90, pairingStartTime+ pairingTimeout);
                }
                showSearching();
                break;
            case Place.RegisterHubV2Response.STATE_DOWNLOADING:
                promptTitle = getString(R.string.download_in_progess);
                promptBody = getString(R.string.encourage_latest_firmware);
                if(currentState.equals(state)) {
                    showDownload(progress);
                    showApplying(getString(R.string.hub_update_available_title), getString(R.string.hub_update_available_description), true);
                } else {
                    downloadStartTime = System.currentTimeMillis();
                    showSearching();
                    animateStateComplete(state, progress);
                }
                break;
            case Place.RegisterHubV2Response.STATE_APPLYING:
                promptTitle = getString(R.string.applying_update);
                promptBody = getString(R.string.encourage_apply_latest_firmware);
                if(currentState.equals(state)) {
                    showApplying(getString(R.string.hub_applying_update_title), getString(R.string.hub_applying_update_description), true);;
                } else {
                    installStartTime = System.currentTimeMillis();
                    showApplying(getString(R.string.hub_applying_update_title), getString(R.string.hub_applying_update_description), true);
                    animateStateComplete(state, progress);
                }
                break;
            default:
                showSearching();
                break;
        }
        currentState = state;
        setTitle();
    }

    private void showSearching() {
        longSearch = false;
        if(System.currentTimeMillis()-pairingStartTime > pairingTimeout) {
            showLongSearch();
            progressLayout.setVisibility(View.VISIBLE);
            title = getString(R.string.searching_for_hub).toUpperCase();
            progressBar.setProgress(90);
            progressPercent.setText(getString(R.string.lightsnswitches_percentage, 90));
            buttonLayout.setVisibility(View.VISIBLE);
            pairingButton.setVisibility(View.VISIBLE);
            // Error banner -- timeout
            if(callback != null) {
                callback.setHubErrorBanner(R.color.sclera_warning, R.string.hub_longsearching_error, registrationContext.getHubID(), null);
                callback.errorBannerVisible(true);
            }
        } else {
            hubSearchingLayout.setVisibility(View.VISIBLE);
            progressLayout.setVisibility(View.VISIBLE);
            title = getString(R.string.pairing_hub).toUpperCase();
            hubIdContainer.setVisibility(View.GONE);
        }
    }

    private void showLongSearch() {
        hubSearchingLayout.setVisibility(View.VISIBLE);
        hubSearchDescription.setVisibility(View.GONE);
        hubLongSearchLayout.setVisibility(View.VISIBLE);
        hubIdContainer.setHint(getString(R.string.hub_id_hint_title));
        hubIdContainer.setVisibility(View.VISIBLE);
        longSearch = true;
    }

    private void showDownload(int responseProgress) {
        int progress = (responseProgress/10)*10;
        progressLayout.setVisibility(View.VISIBLE);
        title = getString(R.string.update_available).toUpperCase();
        final ProgressBarFromToAnimation anim = new ProgressBarFromToAnimation(progressBar, progressBar.getProgress(), progress);
        anim.setDuration(100);
        progressBar.setProgress(progress);
        progressBar.startAnimation(anim);
        progressPercent.setText(getString(R.string.lightsnswitches_percentage, progress));
    }

    private void showApplying(String line1, String line2, boolean canExitPairing) {
        hubUpdatesLayout.setVisibility(View.VISIBLE);
        hubActionDescription.setVisibility(View.VISIBLE);
        exitPairing.setVisibility(canExitPairing ? View.VISIBLE : View.GONE);
        hubActionTitle.setText(line1);
        hubActionDescription.setText(line2);
        progressLayout.setVisibility(View.VISIBLE);
        title = getString(R.string.applying_update).toUpperCase();
    }

    private void showFailed(String description, boolean canExitPairing) {
        hubUpdatesLayout.setVisibility(View.VISIBLE);
        exitPairing.setVisibility(canExitPairing ? View.VISIBLE : View.GONE);
        hubActionTitle.setText(description);
        hubActionDescription.setVisibility(View.GONE);
    }

    public void updateErrorState(String error) {
        hideLayouts();
        hubProgressBarTimer.cancel();
        buttonLayout.setVisibility(View.VISIBLE);
        supportButton.setVisibility(View.VISIBLE);
        boolean showCloseButton = true;
        if(callback != null) {
            callback.errorBannerVisible(true);
            promptTitle = getString(R.string.exit_pairing_title); // title is exit pairing
            switch (error) {
                case Errors.Hub.ALREADY_REGISTERED:
                    showFailed(getString(R.string.hub_error_description, registrationContext.getHubID()), false);
                    title = getString(R.string.hub_error).toUpperCase();
                    // Error banner - E01
                    callback.setHubErrorBanner(R.color.sclera_alert, R.string.hub_reporting_error_code, registrationContext.getHubID(), "E01");
                    showCloseButton = false;
                    break;
                case Errors.Hub.FWUPGRADE_FAILED:
                    showFailed(getString(R.string.hub_tap_support_description), true);
                    title = getString(R.string.download_failed).toUpperCase();
                    // Error banner - download failed
                    callback.setHubErrorBanner(R.color.sclera_alert, R.string.hub_download_failed_error,null, null);
                    // If the user clicks 'exit pairing', show this text in the popup
                    promptBody = getString(R.string.encourage_latest_firmware);
                    break;
                case Errors.Hub.INSTALL_TIMEDOUT:
                    showFailed(getString(R.string.hub_tap_support_description), true);
                    title = getString(R.string.install_failed).toUpperCase();
                    // Error banner - install failed
                    callback.setHubErrorBanner(R.color.sclera_alert, R.string.hub_applying_update_failed_error, null, null);
                    // If the user clicks 'exit pairing', show this text in the popup
                    promptBody = getString(R.string.encourage_apply_latest_firmware);
                    break;
                case Errors.Hub.ORPHANED_HUB:
                    showFailed(getString(R.string.hub_error_description_orphaned, registrationContext.getHubID()), false);
                    title = getString(R.string.hub_error).toUpperCase();
                    showCloseButton = false;
                    // Error banner - E02
                    callback.setHubErrorBanner(R.color.sclera_alert, R.string.hub_reporting_error_code, registrationContext.getHubID(), "E02");
                    break;
                default:
                    showFailed(getString(R.string.hub_error_description_orphaned, registrationContext.getHubID()), false);
                    title = getString(R.string.hub_error).toUpperCase();
                    // Hide the error banner
                    callback.errorBannerVisible(false);
                    break;
            }
            callback.updateCloseButton(showCloseButton);
        }
        setTitle();
    }

    public void animateProgress(int maxProgress, int increment, long interval, long whenToTimeout) {
        hubProgressBarTimer = new Timer("HUB_PROGRESSBAR_TIMER");
        if(task != null) {
            task.cancel();
        }
        task = new ProgressBarTimerTask(maxProgress, increment, whenToTimeout);
        hubProgressBarTimer.schedule(task, 0, interval);
    }

    public void animateRegistered() {
        if(task != null) {
            task.cancel();
        }
        if(hubProgressBarTimer != null) {
            hubProgressBarTimer.cancel();
        }
        ProgressBarFromToAnimation anim = new ProgressBarFromToAnimation(progressBar, progressBar.getProgress(), 100);
        anim.setDuration(1000);
        progressBar.setProgress(100);
        progressPercent.setText(getString(R.string.lightsnswitches_percentage, 100));
        anim.setAnimationListener(new Animation.AnimationListener() {
            public void onAnimationStart(Animation anim) {}
            public void onAnimationRepeat(Animation anim) {}
            public void onAnimationEnd(Animation anim) {
                anim.setAnimationListener(null);
                if(callback != null) {
                    callback.navigateToNextStep();
                }
            }
        });
        progressBar.startAnimation(anim);
    }

    //progressForNextState is necessary in the event that we transition to download and the download progress is not 0
    public void animateStateComplete(final String state, final int progressForNextState) {
        if(task != null) {
            task.cancel();
        }
        if(hubProgressBarTimer != null) {
            hubProgressBarTimer.cancel();
        }
        final ProgressBarFromToAnimation anim = new ProgressBarFromToAnimation(progressBar, progressBar.getProgress(), 100);
        anim.setDuration(1000);
        progressBar.setProgress(100);
        progressPercent.setText(getString(R.string.lightsnswitches_percentage, 100));
        anim.setAnimationListener(new Animation.AnimationListener() {
            public void onAnimationStart(Animation anim) {}
            public void onAnimationRepeat(Animation anim) {}
            public void onAnimationEnd(Animation anim) {
                anim.setAnimationListener(null);
                switch(state) {
                    case Place.RegisterHubV2Response.STATE_DOWNLOADING:
                        hideLayouts();
                        showDownload(progressForNextState);
                        break;
                    case Place.RegisterHubV2Response.STATE_APPLYING:
                        if(hubProgressBarTimer != null) {
                            hubProgressBarTimer.cancel();
                        }
                        hideLayouts();
                        progressBar.setProgress(0);
                        progressPercent.setText(getString(R.string.lightsnswitches_percentage, 0));
                        showApplying(getString(R.string.hub_update_available_title), getString(R.string.hub_update_available_description), true);
                        animateProgress(100, 1, installTimeout/100, installStartTime+installTimeout);
                        break;
                    default:
                        break;
                }
            }
        });
        progressBar.startAnimation(anim);
    }

    private void hideLayouts() {
        progressLayout.setVisibility(View.GONE);
        hubSearchingLayout.setVisibility(View.GONE);
        hubLongSearchLayout.setVisibility(View.GONE);
        hubUpdatesLayout.setVisibility(View.GONE);
        buttonLayout.setVisibility(View.GONE);
        pairingButton.setVisibility(View.GONE);
        supportButton.setVisibility(View.GONE);
    }

    public void cancelTimers() {
        if(task != null) {
            task.cancel();
        }
        if(hubProgressBarTimer != null) {
            hubProgressBarTimer.cancel();
        }
    }

    private void showGoToDashboardPopup() {
        // Defaults
        if(promptTitle == null) {
            promptTitle = getString(R.string.exit_pairing_title);
        }
        if(promptBody == null){
            promptBody = getString(R.string.encourage_updates);
        }

        ScleraInfoButtonPopup popup = ScleraInfoButtonPopup.newInstance(
                promptTitle,
                promptBody,
                getString(R.string.yes_dashboard).toUpperCase(),
                getString(R.string.cancel).toUpperCase(),
                ButtonColor.SOLID_PURPLE,
                ButtonColor.OUTLINE_PURPLE,
                false
        );

        popup.setCallback(topButton -> {
            if(topButton) {
                // go to dashboard
                BackstackManager.getInstance().navigateBackToFragment(HomeFragment.newInstance());
            }
        });
        BackstackManager.getInstance().navigateToFloatingFragment(popup, popup.getClass().getSimpleName(), true);
    }

    private class ProgressBarTimerTask extends TimerTask {
        int maxProgress = 100;
        int increment = 0;
        long whenToTimeout = 0;
        ProgressBarTimerTask(int maxProgress, int increment, long whenToTimeout) {
            this.maxProgress = maxProgress;
            this.increment = increment;
            this.whenToTimeout = whenToTimeout;
        }

        @Override
        public boolean cancel() {
            return super.cancel();
        }

        @Override
        public void run() {
            if(System.currentTimeMillis() >= whenToTimeout) {
                cancel();
                return;
            }
            int newValue = progressBar.getProgress()+increment;
            if(newValue > maxProgress) {
                newValue = maxProgress;
                cancel();
            }

            final ProgressBarFromToAnimation anim = new ProgressBarFromToAnimation(progressBar, progressBar.getProgress(), newValue);
            anim.setDuration(100);
            final int progressText = newValue;
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    try {
                        progressBar.setProgress(progressText);
                        progressBar.startAnimation(anim);
                        progressPercent.setText(getResourceString(R.string.lightsnswitches_percentage, progressText));
                    } catch (IllegalStateException exception) {
                        cancel();
                    }

                }
            });
        }
    }
}

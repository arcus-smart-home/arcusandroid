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
package arcus.app.subsystems.alarm.promonitoring;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import arcus.cornea.SessionController;
import arcus.cornea.controller.SubscriptionController;
import com.iris.client.model.PlaceModel;
import arcus.app.R;
import arcus.app.activities.BaseActivity;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.error.ErrorManager;
import arcus.app.common.fragments.BaseFragment;
import arcus.app.common.popups.AlertPopup;
import arcus.app.common.utils.StringUtils;
import arcus.app.common.view.IncidentCircleView;
import arcus.app.common.view.Version1ButtonColor;
import arcus.app.common.view.Version1ImageButton;
import arcus.app.common.view.Version1TextView;
import arcus.app.subsystems.alarm.promonitoring.adapters.ProMonitoringHistoryAdapter;
import arcus.app.subsystems.alarm.promonitoring.models.AlarmTrackerModel;
import arcus.app.subsystems.alarm.promonitoring.models.AlarmTrackerStateModel;
import arcus.app.subsystems.alarm.promonitoring.models.HistoryListItemModel;
import arcus.app.subsystems.alarm.promonitoring.presenters.AlarmIncidentContract;
import arcus.app.subsystems.alarm.promonitoring.presenters.AlarmIncidentPresenter;

import java.util.ArrayList;
import java.util.List;

public class ProMonitoringIncidentFragment extends BaseFragment implements Animation.AnimationListener, AlarmIncidentContract.AlarmIncidentView {

    private static String INCIDENT_ADDRESS = "INCIDENT_ADDRESS";
    private static String HISTORICAL = "HISTORICAL";

    private AlarmIncidentPresenter presenter = new AlarmIncidentPresenter();
    private RecyclerView historyList;

    private IncidentCircleView dashedCircleLeft;
    private ImageView alarmImageLeft;
    private IncidentCircleView dashedCircleMiddle;
    private ImageView alarmImageMiddle;
    private Version1TextView alarmCountdownMiddle;
    private Version1TextView placeName;
    private Version1TextView incidentStepDescription;
    private Version1TextView hubOfflineBannerText;
    private View incidentLayout;
    private Version1ImageButton cancelButton;
    private Version1ImageButton confirmButton;
    private ImageView promonImage;
    private LinearLayout buttonRegion;
    private View buttonLayout;
    private View hubOfflineBanner;

    private Animation animFadeIn;
    private Animation animFadeOut;
    private boolean proMonitored;
    private boolean completed;
    private String title = "";

    private List<AlarmTrackerStateModel> states = new ArrayList<>();

    public static ProMonitoringIncidentFragment newInstance(String incidentAddress) {
        ProMonitoringIncidentFragment fragment = new ProMonitoringIncidentFragment();

        Bundle bundle = new Bundle();
        bundle.putString(INCIDENT_ADDRESS, incidentAddress);
        fragment.setArguments(bundle);
        return fragment;
    }

    public ProMonitoringIncidentFragment() {
        // Required empty public constructor
    }

    @Override
    public void onResume() {
        super.onResume();

        presenter.startPresenting(this);
        presenter.requestUpdate(getArguments().getString(INCIDENT_ADDRESS));
        if (!TextUtils.isEmpty(title)) {
            ((BaseActivity) getActivity()).setToolbarTitle(title);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        presenter.stopPresenting();
    }


    @Override
    public String getTitle() {
        if (!TextUtils.isEmpty(title)) {
            return title;
        }
        return null;
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_pro_monitoring_incident;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        historyList = (RecyclerView) view.findViewById(R.id.historyRecyclerView);

        animFadeIn = AnimationUtils.loadAnimation(getContext(), R.anim.fade_in);
        animFadeOut = AnimationUtils.loadAnimation(getContext(), R.anim.fade_out);
        animFadeOut.setAnimationListener(this);

        buttonLayout = view.findViewById(R.id.button_layout);
        confirmButton = (Version1ImageButton) view.findViewById(R.id.confirm_button);
        confirmButton.setColorScheme(Version1ButtonColor.ALARM_CONFIRM_BUTTON_ENABLED);
        cancelButton = (Version1ImageButton) view.findViewById(R.id.cancel_button);
        cancelButton.setColorScheme(Version1ButtonColor.ALARM_CANCEL_BUTTON_ENABLED);

        hubOfflineBanner = view.findViewById(R.id.hub_offline_banner);
        hubOfflineBanner.setVisibility(View.GONE);

        hubOfflineBannerText = (Version1TextView) view.findViewById(R.id.hub_offline_banner_text);

        RecyclerView.LayoutManager layoutManagerHistory = new LinearLayoutManager(getActivity());
        historyList.setLayoutManager(layoutManagerHistory);
        historyList.setAdapter(new ProMonitoringHistoryAdapter(R.layout.cell_incident_history, new ArrayList<HistoryListItemModel>()));

        incidentLayout = view.findViewById(R.id.incident_layout);
        dashedCircleLeft = (IncidentCircleView) view.findViewById(R.id.dashed_circle_left);
        dashedCircleMiddle = (IncidentCircleView) view.findViewById(R.id.dashed_circle_middle);
        alarmImageLeft = (ImageView) view.findViewById(R.id.alarm_image_left);
        alarmImageMiddle = (ImageView) view.findViewById(R.id.alarm_image_middle);
        alarmCountdownMiddle = (Version1TextView) view.findViewById(R.id.alarm_countdown);
        placeName = (Version1TextView) view.findViewById(R.id.place_name);
        incidentStepDescription = (Version1TextView) view.findViewById(R.id.incident_step_description);
        promonImage = (ImageView) view.findViewById(R.id.promon_image);
        buttonRegion = (LinearLayout) view.findViewById(R.id.button_region);

        // Premium or pro user layout
        if (SubscriptionController.isPremiumOrPro()) {
            dashedCircleMiddle.showLeftGradient(false);
            dashedCircleMiddle.showRightGradient(false);
            dashedCircleMiddle.setSelected(true);
        }

        // Basic user layout
        else {
            dashedCircleLeft.setVisibility(View.INVISIBLE);
            alarmImageLeft.setVisibility(View.INVISIBLE);

            dashedCircleMiddle.showLeftGradient(false);
            dashedCircleMiddle.showRightGradient(false);
            dashedCircleMiddle.setSelected(true);

            final PlaceModel placeModel = SessionController.instance().getPlace();
            if (placeModel == null) {
                return view;
            }
            ((TextView) view.findViewById(R.id.place_name)).setText(placeModel.getName());
        }

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.requestCancel();
            }
        });

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.requestConfirm();
            }
        });

        return view;
    }

    private void updateIncidentLayout(List<AlarmTrackerStateModel> states) {
        if (states.size() == 0) {
            incidentLayout.setVisibility(View.INVISIBLE);
            return;
        }

        if (SubscriptionController.isPremiumOrPro()) {
            if (states.size() == 1) {
                dashedCircleLeft.setVisibility(View.INVISIBLE);
                alarmImageLeft.setVisibility(View.INVISIBLE);
                dashedCircleMiddle.showLeftGradient(false);
                dashedCircleLeft.invalidate();
            } else {
                dashedCircleLeft.setVisibility(View.VISIBLE);
                alarmImageLeft.setVisibility(View.VISIBLE);
                alarmImageLeft.setImageResource(states.get(states.size() - 2).getUnselectedPizzaIconResId());
                dashedCircleMiddle.showLeftGradient(true);
                dashedCircleLeft.invalidate();
            }
            if (states.size() > 2) {
                dashedCircleLeft.showLeftGradient(true);
            } else {
                dashedCircleLeft.showLeftGradient(false);
            }
        }

        // Change color of cancel button to tint color when not professionally monitored
        if (!proMonitored) {
            cancelButton.setColorScheme(getCurrentState().getButtonColor());
        }

        confirmButton.setVisibility(proMonitored ? View.VISIBLE : View.GONE);

        if (completed) {
            dashedCircleMiddle.setAlarmColor(getResources().getColor(R.color.unselected_circle_color));
        } else {
            dashedCircleMiddle.setAlarmColor(getCurrentState().getTintColor());
        }
        incidentStepDescription.setText(getCurrentState().getIncidentStateName());
        placeName.setText(getCurrentState().getPlaceName().toUpperCase());
        alarmImageLeft.setColorFilter(getResources().getColor(R.color.unselected_circle_color));
        incidentLayout.setVisibility(View.VISIBLE);
        dashedCircleMiddle.setVisibility(View.VISIBLE);

        renderCenterCircle();
    }

    @Override
    public void onAnimationStart(Animation animation) {
    }

    @Override
    public void onAnimationEnd(Animation animation) {
        if (animation == animFadeOut) {
            updateIncidentLayout(states);
            incidentLayout.startAnimation(animFadeIn);
        }
    }

    @Override
    public void onAnimationRepeat(Animation animation) {

    }

    @Override
    public void showCancel(String title, CharSequence reasonCopy) {
        AlertPopup popup = AlertPopup.newInstance(title, reasonCopy, getString(R.string.incident_cancel_close), null, AlertPopup.ColorStyle.WHITE, new AlertPopup.AlertButtonCallback() {
            @Override
            public boolean topAlertButtonClicked() {
                return true;
            }

            @Override
            public boolean bottomAlertButtonClicked() {
                return true;
            }

            @Override
            public boolean errorButtonClicked() {
                return true;
            }

            @Override
            public void close() {
                BackstackManager.getInstance().navigateBack();
            }
        });

        BackstackManager.getInstance().navigateToFloatingFragment(popup, popup.getClass().getSimpleName(), true);
    }

    @Override
    public void showError(Throwable t) {
        ErrorManager.in(getActivity()).showGenericBecauseOf(t);
    }

    @Override
    public void showHubDisconnectedBanner(final boolean isHubOffline, @Nullable String hubOfflineTime) {
        final String offlineText = getString(R.string.hub_local_offline_incident_banner_text, hubOfflineTime);

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isHubOffline) {
                    hubOfflineBanner.setVisibility(View.VISIBLE);
                    if (!offlineText.isEmpty()) {
                        hubOfflineBannerText.setText(offlineText);
                    }
                } else {
                    hubOfflineBanner.setVisibility(View.GONE);
                }
            }
        });
    }

    @Override
    public void updateHubDisconnectedBanner(String hubOfflineTime) {
        final String offlineText = getString(R.string.hub_local_offline_incident_banner_text, hubOfflineTime);

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (hubOfflineBannerText.isShown()) {
                    hubOfflineBannerText.setText(offlineText);
                }
            }
        });
    }

    @Override
    public void onPending(@Nullable Integer progressPercent) {
        // Nothing to do
    }

    @Override
    public void onError(@NonNull Throwable throwable) {
        showError(throwable);
    }

    @Override
    public void updateView(@NonNull AlarmTrackerModel model) {
        AlarmTrackerStateModel lastState = getCurrentState();
        states = model.getTrackerStates();
        proMonitored = model.isProMonitored();
        title = model.getAlarmTypeTitle();

        ProMonitoringHistoryAdapter adapter = ((ProMonitoringHistoryAdapter) historyList.getAdapter());
        int currentItems = adapter.getItemCount();
        int newItems = model.getHistoryListItems().size();
        for (int index = currentItems; index < newItems; index++) {
            adapter.add(model.getHistoryListItems().get(index));
            historyList.scrollToPosition(0);
            adapter.notifyItemInserted(0);
        }

        // Basic users get shaded incident
        if (!SubscriptionController.isPremiumOrPro()) {
            incidentLayout.setBackgroundColor(model.getIncidentLayoutTint());
        }

        promonImage.setVisibility(model.isProMonitored() ? View.VISIBLE : View.GONE);

        buttonRegion.setVisibility((model.isCancelable() || model.isConfirmable()) ? View.VISIBLE : View.GONE);

        if (model.isComplete()) {
            completed = true;
            buttonLayout.setVisibility(View.GONE);
        } else {
            completed = false;
            buttonLayout.setVisibility(View.VISIBLE);
            cancelButton.setEnabled(model.isCancelable(),
                    Version1ButtonColor.ALARM_BUTTON_DISABLED,
                    getResources().getIdentifier("promon_security_cancel_alarm", "drawable", getActivity().getPackageName()),
                    getResources().getIdentifier("promon_security_cancel_alarm_grey", "drawable", getActivity().getPackageName()));
            confirmButton.setEnabled(model.isConfirmable(),
                    Version1ButtonColor.ALARM_BUTTON_DISABLED,
                    getResources().getIdentifier("promon_security_confirm_alarm", "drawable", getActivity().getPackageName()),
                    getResources().getIdentifier("promon_security_confirm_alarm_grey", "drawable", getActivity().getPackageName()));
        }

        ((BaseActivity) getActivity()).setToolbarTitle(model.getAlarmTypeTitle());

        renderCenterCircle();
        // Only animate pizza when we're actually changing incident state
        if (lastState == null || lastState.getIncidentStateName() == null) {
            updateIncidentLayout(states);
            incidentLayout.startAnimation(animFadeIn);
        } else if (!lastState.getIncidentStateName().equals(getCurrentState().getIncidentStateName())) {
            incidentLayout.startAnimation(animFadeOut);
        }

    }

    @Nullable
    private AlarmTrackerStateModel getCurrentState() {
        return states == null || states.size() == 0 ? null : states.get(states.size() - 1);
    }

    private void renderCenterCircle() {
        AlarmTrackerStateModel currentState = getCurrentState();

        // Current state displays an icon
        if (currentState.getCountdown() == null) {
            alarmCountdownMiddle.setVisibility(View.GONE);
            if (completed) {
                alarmImageMiddle.setColorFilter(getResources().getColor(R.color.unselected_circle_color));
                dashedCircleMiddle.setAlarmColor(getResources().getColor(R.color.unselected_circle_color));
            } else {
                alarmImageMiddle.setColorFilter(currentState.getTintColor());
                dashedCircleMiddle.setAlarmColor(currentState.getTintColor());
            }

            alarmImageMiddle.setImageResource(currentState.getSelectedPizzaIconResId());
            alarmImageMiddle.setVisibility(View.VISIBLE);
        }

        // Current state displays a countdown
        else {
            alarmImageMiddle.setVisibility(View.GONE);
            dashedCircleMiddle.setAlarmColor(getResources().getColor(R.color.black_with_35));
            alarmCountdownMiddle.setText(StringUtils.getSuperscriptSpan(String.valueOf(currentState.getCountdown()), getString(R.string.security_seconds_suffix)));
            alarmCountdownMiddle.setVisibility(View.VISIBLE);
        }
    }
}


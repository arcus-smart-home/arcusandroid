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
package arcus.app.dashboard;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.dexafree.materialList.model.Card;

import arcus.app.subsystems.alarm.promonitoring.views.ProMonitoringDashboardCardItemView;
import arcus.cornea.SessionController;
import arcus.cornea.controller.InvitationController;
import arcus.cornea.model.PlaceAndRoleModel;
import arcus.cornea.provider.AvailablePlacesProvider;
import arcus.cornea.subsystem.cameras.ClipListingController;
import arcus.cornea.subsystem.connection.CellularBackup;
import arcus.cornea.subsystem.connection.model.CellBackupModel;
import arcus.cornea.utils.Listeners;
import com.iris.client.capability.Hub;
import com.iris.client.capability.HubPower;
import com.iris.client.capability.Person;
import com.iris.client.event.Listener;
import com.iris.client.event.ListenerRegistration;
import com.iris.client.model.DeviceModel;
import com.iris.client.model.HubModel;
import com.iris.client.model.PersonModel;
import com.iris.client.session.SessionActivePlaceSetEvent;
import arcus.app.R;
import arcus.app.activities.BaseActivity;
import arcus.app.common.adapters.HomeFragmentRecyclerAdapter;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.backstack.TransitionEffect;
import arcus.app.common.banners.ConfigureDeviceBanner;
import arcus.app.common.banners.FirmwareUpdatingBanner;
import arcus.app.common.banners.InvitationBanner;
import arcus.app.common.banners.NoHubConnectionBanner;
import arcus.app.common.banners.ServiceSuspendedBanner;
import arcus.app.common.banners.UpdateServicePlanBanner;
import arcus.app.common.banners.core.BannerManager;
import arcus.app.common.cards.AlertCard;
import arcus.app.common.cards.CenteredTextCard;
import arcus.app.common.cards.SimpleDividerCard;
import arcus.app.common.cards.TopImageCard;
import arcus.app.common.cards.view.AlertCardItemView;
import arcus.app.common.controller.AbstractCardController;
import arcus.app.common.controller.BackstackPopListener;
import arcus.app.common.events.PlaceChangeRequestedEvent;
import arcus.app.common.fragments.BaseFragment;
import arcus.app.common.image.ImageManager;
import arcus.app.common.image.Wallpaper;
import arcus.app.common.models.SessionModelManager;
import arcus.app.common.utils.ActivityUtils;
import arcus.app.common.utils.PreferenceUtils;
import arcus.app.common.view.DashboardRecyclerItemClickListener;
import arcus.app.dashboard.popups.responsibilities.dashboard.DashboardPopupManager;
import arcus.app.dashboard.popups.responsibilities.history.HistoryServicePopupManager;
import arcus.app.dashboard.settings.favorites.FavoritesOrderChangedEvent;
import arcus.app.dashboard.settings.services.CardListChangedEvent;
import arcus.app.dashboard.settings.services.ServiceCard;
import arcus.app.device.ota.controller.FirmwareUpdateController;
import arcus.app.subsystems.alarm.promonitoring.presenters.AlarmCardPresenter;
import arcus.app.subsystems.alarm.safety.SafetyAlarmParentFragment;
import arcus.app.subsystems.alarm.safety.controllers.SafetyCardController;
import arcus.app.subsystems.alarm.security.SecurityParentFragment;
import arcus.app.subsystems.alarm.security.controllers.SecurityCardController;
import arcus.app.subsystems.camera.CameraParentFragment;
import arcus.app.subsystems.camera.controllers.CameraCardController;
import arcus.app.subsystems.camera.views.CameraCardItemView;
import arcus.app.subsystems.care.CareParentFragment;
import arcus.app.subsystems.care.controller.CareDashboardViewController;
import arcus.app.subsystems.care.view.CareCardItemView;
import arcus.app.subsystems.climate.ClimateParentFragment;
import arcus.app.subsystems.climate.controllers.ClimateCardController;
import arcus.app.subsystems.climate.views.ClimateCardItemView;
import arcus.app.subsystems.comingsoon.cards.ComingSoonCard;
import arcus.app.subsystems.doorsnlocks.DoorsNLocksParentFragment;
import arcus.app.subsystems.doorsnlocks.controllers.DoorsnlocksCardController;
import arcus.app.subsystems.doorsnlocks.view.DoorsNLocksCardItemView;
import arcus.app.subsystems.favorites.controllers.FavoritesCardController;
import arcus.app.subsystems.feature.cards.FeatureCard;
import arcus.app.subsystems.feature.views.FeatureCardItemView;
import arcus.app.subsystems.history.HistoryFragment;
import arcus.app.subsystems.history.controllers.HistoryCardController;
import arcus.app.subsystems.history.view.HistoryCardItemView;
import arcus.app.subsystems.homenfamily.HomeNFamilyParentFragment;
import arcus.app.subsystems.homenfamily.controllers.HomeNFamilyCardController;
import arcus.app.subsystems.homenfamily.view.HomeNFamilyCardItemView;
import arcus.app.subsystems.lawnandgarden.LawnAndGardenParentFragment;
import arcus.app.subsystems.lawnandgarden.controllers.LawnAndGardenCardController;
import arcus.app.subsystems.lawnandgarden.views.LawnAndGardenCardItemView;
import arcus.app.subsystems.learnmore.cards.LearnMoreCard;
import arcus.app.subsystems.lightsnswitches.LightsNSwitchesParentFragment;
import arcus.app.subsystems.lightsnswitches.controllers.LightsNSwitchesCardController;
import arcus.app.subsystems.lightsnswitches.view.LightsNSwitchesCardItemView;
import arcus.app.subsystems.water.WaterParentFragment;
import arcus.app.subsystems.water.controllers.WaterCardController;
import arcus.app.subsystems.water.views.WaterCardItemView;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import de.greenrobot.event.EventBus;

public class HomeFragment extends BaseFragment implements BackstackPopListener, AbstractCardController.Callback, FirmwareUpdateController.UpdateCallback,
        InvitationController.Callback, CellularBackup.Callback, HistoryCardController.Callback {

    private final static Logger logger = LoggerFactory.getLogger(HomeFragment.class);

    private RecyclerView cardsListView;
    private HomeFragmentRecyclerAdapter adapter;
    private ListenerRegistration propertyChangeRegistry, cellBackupListener;
    private Set<String> hubStateFilters = new HashSet<>(Arrays.asList(Hub.ATTR_STATE, HubPower.ATTR_SOURCE));
    private final PropertyChangeListener hubStateListener = Listeners.filter(hubStateFilters, new PropertyChangeListener() {
        @Override
        public void propertyChange(final PropertyChangeEvent event) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateHubConnection(String.valueOf(event.getNewValue()));
                    // If we're no longer showing the banner....
                    if(getActivity() != null) {
                        if (!BannerManager.in(getActivity()).containsBanner(NoHubConnectionBanner.class)) {
                            showCellularBanners(null);
                        }
                    }
                }
            });
        }
    });

    /**
     * Card Controllers
     */
    private SafetyCardController mSafetyCardController;
    private SecurityCardController mSecurityCardController;
    private AlarmCardPresenter mAlarmCardPresenter;
    private DoorsnlocksCardController mDoorsnlocksCardController;
    private ClimateCardController mClimateCardController;
    private CameraCardController mCameraCardController;
    private FavoritesCardController mFavoritesCardController;
    private HistoryCardController mHistoryCardController;
    private HomeNFamilyCardController mHomeNFamilyCardController;
    private LightsNSwitchesCardController mLightsNSwitchesCardController;
    private WaterCardController mWaterCardController;
    private LawnAndGardenCardController mLawnAndGardenCardController;
    private InvitationController mInvitationController;
    private ComingSoonCard windowsAndBlindsCard;
    private CareDashboardViewController careCard;
    private ComingSoonCard energyCard;
    private FeatureCard featureCard;

    private AtomicBoolean isDashboardInQuietPeriod = new AtomicBoolean(false);
    private AtomicBoolean isDashboardRefreshRequested = new AtomicBoolean(false);
    private AtomicBoolean shouldRedraw = new AtomicBoolean(true);
    private Handler popupDelayHandler = new Handler();

    TopImageCard topImageCard;
    String placeName;
    ScheduledFuture<?> scheduledFuture;

    @NonNull
    public static HomeFragment newInstance() {
        return new HomeFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Warning: Controllers need to all be created before setting the callbacks
        // TODO: Aren't these always going to be null during onCreate?..

	if (mAlarmCardPresenter == null) {
	    mAlarmCardPresenter = new AlarmCardPresenter(getActivity());
	}

        if (mSecurityCardController == null) {
            mSecurityCardController = new SecurityCardController(getActivity());
        }

        if (mSafetyCardController == null) {
            mSafetyCardController = new SafetyCardController(getActivity());
        }

        if(mDoorsnlocksCardController == null){
            mDoorsnlocksCardController = new DoorsnlocksCardController(getActivity());
        }

        if (mClimateCardController == null) {
            mClimateCardController = new ClimateCardController(getActivity());
        }

        if(mCameraCardController == null){
            mCameraCardController = new CameraCardController(getActivity());
        }

        if (mFavoritesCardController == null) {
            mFavoritesCardController = new FavoritesCardController(getActivity());
        }

        if (mHistoryCardController == null) {
            mHistoryCardController = new HistoryCardController(getActivity(), this);
        }

        if (mHomeNFamilyCardController == null) {
            mHomeNFamilyCardController = new HomeNFamilyCardController(getActivity());
        }

        if (mLightsNSwitchesCardController == null) {
            mLightsNSwitchesCardController = new LightsNSwitchesCardController(getActivity());
        }

        careCard = new CareDashboardViewController(getActivity());
        if (mWaterCardController == null) {
            mWaterCardController = new WaterCardController(getActivity());
        }

        if (mLawnAndGardenCardController == null) {
            mLawnAndGardenCardController = new LawnAndGardenCardController(getActivity());
        }

        if (mInvitationController == null) {
            mInvitationController = InvitationController.instance();
            mInvitationController.setCallback(this);
        }

        Activity activity = getActivity();
        if (activity != null) {
            windowsAndBlindsCard = new ComingSoonCard(activity, ServiceCard.WINDOWS_AND_BLINDS);
            energyCard = new ComingSoonCard(activity, ServiceCard.ENERGY);
            featureCard = new FeatureCard(activity);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        cardsListView = (RecyclerView) view.findViewById(R.id.homefragment_recyclerview);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        cardsListView.setLayoutManager(layoutManager);
        adapter = new HomeFragmentRecyclerAdapter(new ArrayList<SimpleDividerCard>());
        cardsListView.setAdapter(adapter);
        cardsListView.addOnItemTouchListener(new DashboardRecyclerItemClickListener(getActivity(), new DashboardRecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onDragRight(RecyclerView.ViewHolder view, int position) {
                ((HomeFragmentRecyclerAdapter)cardsListView.getAdapter()).flipCardRight(view, view.getAdapterPosition());
            }

            @Override
            public void onDragLeft(RecyclerView.ViewHolder view, int position) {
                ((HomeFragmentRecyclerAdapter)cardsListView.getAdapter()).flipCardLeft(view, view.getAdapterPosition());
            }
            @Override public void onItemClick(RecyclerView.ViewHolder view, int position) {
                //TODO: based on list of cards, figure out the type and where to go?
                if (view instanceof HistoryCardItemView) {
                    BackstackManager.withAnimation(TransitionEffect.FADE).navigateToFragment(new HistoryFragment(), true);
                    HistoryServicePopupManager.getInstance().triggerPopups();
                }
                else if (view instanceof ProMonitoringDashboardCardItemView) {
                    if(((ProMonitoringDashboardCardItemView)view).isAlarmSubsystemEnabled()) {
                        BackstackManager.withAnimation(TransitionEffect.FADE).navigateToFragment(new SecurityParentFragment(), true);
                    } else {
                        AlarmSubsystemActivationFragment activationFragment = AlarmSubsystemActivationFragment.newInstance();
                        BackstackManager.getInstance().navigateToFloatingFragment(activationFragment, activationFragment.getClass().getName(), true);
                    }
                }
                else if(view instanceof AlertCardItemView) {
                    if (((AlertCardItemView) view).getAlarmSystem() == AlertCard.ALARM_SYSTEM.SAFETY){
                        BackstackManager.withAnimation(TransitionEffect.FADE).navigateToFragment(new SafetyAlarmParentFragment(), true);
                    }
                    else {
                        BackstackManager.withAnimation(TransitionEffect.FADE).navigateToFragment(new SecurityParentFragment(), true);
                    }
                }
                else if(view instanceof DoorsNLocksCardItemView){
                    BackstackManager.withAnimation(TransitionEffect.FADE).navigateToFragment(new DoorsNLocksParentFragment(), true);
                }
                else if (view instanceof ClimateCardItemView) {
                    BackstackManager.withAnimation(TransitionEffect.FADE).navigateToFragment(new ClimateParentFragment(), true);
                }
                else if (view instanceof CameraCardItemView){
                    ClipListingController.instance().setFilterByDevice(null);
                    ClipListingController.instance().setFilterByTime(null, null);
                    ClipListingController.instance().setFilterByTimeValue(0);
                    BackstackManager.withAnimation(TransitionEffect.FADE).navigateToFragment(new CameraParentFragment(), true);
                }
                else if (view instanceof HomeNFamilyCardItemView){
                    BackstackManager.withAnimation(TransitionEffect.FADE).navigateToFragment(new HomeNFamilyParentFragment(), true);
                }
                else if (view instanceof LightsNSwitchesCardItemView) {
                    BackstackManager.withAnimation(TransitionEffect.FADE).navigateToFragment(new LightsNSwitchesParentFragment(), true);
                }
                else if (view instanceof CareCardItemView) {
                    BackstackManager.getInstance().navigateToFragment(CareParentFragment.newInstance(), true);
                }
                else if (view instanceof WaterCardItemView) {
                    BackstackManager.withAnimation(TransitionEffect.FADE).navigateToFragment(new WaterParentFragment(), true);
                }
                else if (view instanceof LawnAndGardenCardItemView) {
                    BackstackManager.withAnimation(TransitionEffect.FADE).navigateToFragment(new LawnAndGardenParentFragment(), true);
                } else if (view instanceof FeatureCardItemView) {
                    ActivityUtils.launchShopNow();
                }
            }
        }));

        return view;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.menu_add_device) {

            BaseFragment newFragment = AddMenuFragment.newInstance();
            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            transaction
                .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_right, R.anim.enter_from_left, R.anim.exit_to_right)
                .addToBackStack(newFragment.getClass().getName())
                .replace(R.id.container, newFragment, newFragment.getClass().getName())
                .commitAllowingStateLoss();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPause() {
        super.onPause();

        // Cancel display of any popups
        popupDelayHandler.removeCallbacksAndMessages(null);

        shouldRedraw.set(true);
        removeCardListeners();
        if(scheduledFuture != null) {
            scheduledFuture.cancel(true);
        }

        FirmwareUpdateController.getInstance().stopFirmwareUpdateStatusMonitor();
        Listeners.clear(propertyChangeRegistry);
        Listeners.clear(cellBackupListener);

        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        BannerManager.in(getActivity()).clearBanners();
        ((BaseActivity)getActivity()).showTitle(false);

        hideProgressBar();
    }

    public void onPopped() {
        if (shouldRedraw.get()) {
            ImageManager.with(getActivity()).setWallpaper(Wallpaper.ofCurrentPlace().darkened());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().registerSticky(this);

        ((BaseActivity)getActivity()).showTitle(false);
        getActivity().setTitle(getTitle());

        mAlarmCardPresenter.setCallback(this);
        mSecurityCardController.setCallback(this);
        mSafetyCardController.setCallback(this);
        mDoorsnlocksCardController.setCallback(this);
        mClimateCardController.setCallback(this);
        mCameraCardController.setCallback(this);
        mFavoritesCardController.setCallback(this);
        mHomeNFamilyCardController.setCallback(this);
        mLightsNSwitchesCardController.setCallback(this);
        careCard.setCallback(this);
        mWaterCardController.setCallback(this);
        mLawnAndGardenCardController.setCallback(this);

        // No callback because no means to asynchronously notify us when history changes
        mHistoryCardController.updateHistoryLogEntries();

        onPopped(); // sets the bg wallpaper.
        addHubPropertyChangeListener();
        FirmwareUpdateController.getInstance().startFirmwareUpdateStatusMonitor(getActivity(), HomeFragment.this);
        redrawDashboardCards();
        loadPlaceName();
        
        cellBackupListener = CellularBackup.instance().setCallback(this);

        // Display any popups that may be required given user context
        popupDelayHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                DashboardPopupManager.getInstance().triggerPopups();
            }
        }, 500);

        ScheduledExecutorService scheduler =
                Executors.newSingleThreadScheduledExecutor();

        scheduledFuture = scheduler.scheduleAtFixedRate
                (new Runnable() {
                    public void run() {
                        mHistoryCardController.updateHistoryLogEntries();
                    }
                }, 15, 15, TimeUnit.SECONDS);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(getActivity() != null) {
                    getActivity().invalidateOptionsMenu();
                }
            }
        }, 100);

        if (FirmwareUpdateController.getInstance().areDevicesUpdating()) {
            BannerManager.in(getActivity()).showBanner(new FirmwareUpdatingBanner());
        }
    }

    private void addHubPropertyChangeListener() {
        Listeners.clear(propertyChangeRegistry);
        HubModel model = SessionModelManager.instance().getHubModel();
        if (model == null) {
            return;
        }

        checkHubConnection();
        propertyChangeRegistry = model.addPropertyChangeListener(hubStateListener);
    }

    private void redrawDashboardCards() {
        if (!shouldRedraw.get()) {
            logger.debug("Request to redraw service cards denied, should redraw was false.");
            return;
        }

        logger.debug("Request to redraw service cards.");

        // Populating the dashboard with cards will cause "notify" events to re-fire this method
        // causing unneeded work and screen flickering. Put the dashboard in a "quiet period" while
        // cards build themselves. If any card requests a refresh during this period, handle it once
        // after the quiet period elapses.

        if (!isDashboardInQuietPeriod.get()) {

            logger.debug("Redrawing service cards; entering quiet period.");

            // Put the dashboard in a quiet period ...
            isDashboardInQuietPeriod.set(true);
            isDashboardRefreshRequested.set(false);

            // ... and repopulate all the cards
            populateCardListView();
            //loadPlaceName();

            // ... and expire the quiet period, and if need be, re-fire this method.
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    logger.debug("Dashboard quiet period has expired.");

                    // Take dashboard out of quiet period
                    isDashboardInQuietPeriod.set(false);

                    // And if a card requested a refresh during population, redraw cards again
                    if (isDashboardRefreshRequested.get()) {
                        logger.debug("Redraw request was made during quiet period; handling request now.");
                        redrawDashboardCards();
                    }
                }
            }, 1500);
        }

        // Dashboard is in a quiet period; won't redraw now but flag a request to redraw when the
        // quiet period ends...
        else {
            logger.debug("... but dashboard is in quiet period; queueing request for redraw at end of quiet period.");
            isDashboardRefreshRequested.set(true);
        }
    }

    private void populateCardListView () {
        HomeFragmentRecyclerAdapter adapter = (HomeFragmentRecyclerAdapter)cardsListView.getAdapter();
        adapter.removeAll();

        // Walk through the list of preference-ordered service cards
        for (ServiceCard thisService : getServiceCardList()) {

            // ... and get a card for it
            SimpleDividerCard card = getCardForService(thisService);

            // Add cards that should be shown to adapter
            boolean showCard =
                card != null
                && !(card instanceof ComingSoonCard)
                && !(card instanceof LearnMoreCard);

            if (showCard) {
                adapter.add(card);
            }
        }

        // add default top image card (header)
        topImageCard = new TopImageCard(getActivity());
        topImageCard.setPlaceID(SessionController.instance().getPlaceIdOrEmpty());
        topImageCard.setHideSettingsGear(false);
        topImageCard.setPlaceName(placeName);
        adapter.addAtStart(topImageCard);
    }

    /**
     * Gets an ordered list of service cards. This list represents those cards that should be displayed
     * on the dashboard in the order that they should be displayed.
     *
     * Delegates to previously saved preferences and user settings to determine the order.
     * @return
     */
    @NonNull
    private List<ServiceCard> getServiceCardList () {

        // If the user recently changed the service card list (via dashboard settings), then use that list...
        CardListChangedEvent changedEvent = EventBus.getDefault().getStickyEvent(CardListChangedEvent.class);
        if (changedEvent != null) {
            return changedEvent.getOrderedVisibleCards();
        }

        // Otherwise, fall back to a previously saved list...
        Set<ServiceCard> visibleCards = PreferenceUtils.getVisibleServiceSet();
        List<ServiceCard> orderedCards = PreferenceUtils.getOrderedServiceCardList();

        // Check whether Feature card has ever been shown
        boolean featurePremiere = !visibleCards.contains(ServiceCard.FEATURE) && !orderedCards.contains(ServiceCard.FEATURE);

        if (featurePremiere) {
            visibleCards.add(ServiceCard.FEATURE);
            PreferenceUtils.putVisibleServiceSet(visibleCards);
            orderedCards.add(ServiceCard.FEATURE);
            PreferenceUtils.putOrderedServiceList(orderedCards);
        }

        List<ServiceCard> orderedVisibleCards = new ArrayList<>();

        for (ServiceCard thisCard : orderedCards) {
            if (visibleCards.contains(thisCard)) {
                orderedVisibleCards.add(thisCard);
            }
        }

        return orderedVisibleCards;
    }

    @Nullable
    private SimpleDividerCard getCardForService (@NonNull ServiceCard service) {
        switch (service) {
            case HISTORY:
                return mHistoryCardController.getCard();
            case FAVORITES:
                return mFavoritesCardController.getCard();
            case SECURITY_ALARM:
                return mAlarmCardPresenter.getCard();
            case DOORS_AND_LOCKS:
                return mDoorsnlocksCardController.getCard();
            case CLIMATE:
                return mClimateCardController.getCard();
            case CAMERAS:
                return mCameraCardController.getCard();
            case LIGHTS_AND_SWITCHES:
                return mLightsNSwitchesCardController.getCard();
            case HOME_AND_FAMILY:
                return mHomeNFamilyCardController.getCard();
            case WINDOWS_AND_BLINDS:
                return windowsAndBlindsCard;
            case CARE:
                return careCard.getCard();
            case WATER:
                return mWaterCardController.getCard();
            case ENERGY:
                return energyCard;
            case LAWN_AND_GARDEN:
                return mLawnAndGardenCardController.getCard();
            case FEATURE:
                return featureCard;
            default:
                logger.debug("HomeFragment ---> Returning null on [{}], no card for this?", service.name());
                return null;
        }
    }

    private void removeCardListeners(){

        if (mAlarmCardPresenter !=null){
	    mAlarmCardPresenter.removeCallback();
	}

        if(mDoorsnlocksCardController !=null){
            mDoorsnlocksCardController.removeCallback();
        }

        if (mClimateCardController != null) {
            mClimateCardController.removeCallback();
        }

        if(mCameraCardController !=null){
            mCameraCardController.removeCallback();
        }

        if (mFavoritesCardController != null) {
            mFavoritesCardController.removeCallback();
        }

        if (mHomeNFamilyCardController != null) {
            mHomeNFamilyCardController.removeCallback();
        }

        if (mLightsNSwitchesCardController != null) {
            mLightsNSwitchesCardController.removeCallback();
        }

        if (careCard != null) {
            careCard.removeCallback();
        }

        if(mWaterCardController != null) {
            mWaterCardController.removeCallback();
        }

        if(mLawnAndGardenCardController != null) {
            mLawnAndGardenCardController.removeCallback();
        }
    }

    @Override
    public String getTitle() {
        return getString(R.string.dashboard_arcus);
    }

    @Override
    public Integer getMenuId() {
        return R.menu.menu_dashboard;
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_home;
    }

    public void onEvent(@NonNull FavoritesOrderChangedEvent event) {
        PreferenceUtils.putOrderedFavoritesList(event.getOrderedFavoriteDeviceIds());
        if (mFavoritesCardController != null) {
            mFavoritesCardController.favoriteOrderChanged();
        }
    }

    public void onEvent(@NonNull CardListChangedEvent event) {
        PreferenceUtils.putOrderedServiceList(event.getServiceOrder());
        PreferenceUtils.putVisibleServiceSet(event.getVisibleCards());
    }

    /**
     * AbstractCardController Callback Methods
     */

    @Override
    public void updateCard(Card c) {

        // Attempt to update only the card indicated...
        if (!adapter.notifyCardChanged((SimpleDividerCard) c)) {
            // ... if that fails, redraw all cards ...
            redrawDashboardCards();
        }
    }

    @Override
    public void onDevicesUpdating(List<DeviceModel> deviceList) {
        if (FirmwareUpdateController.getInstance().areDevicesUpdating()) {
            BannerManager.in(getActivity()).showBanner(new FirmwareUpdatingBanner());
        }
    }

    @Override
    public void onDeviceFirmwareUpdateStatusChange(DeviceModel device, boolean isUpdating, boolean otherDevicesUpdating) {
        if (!isUpdating && !otherDevicesUpdating) {
            BannerManager.in(getActivity()).removeBanner(FirmwareUpdatingBanner.class);
            showCellularBanners(null);
        }
    }

    @Override
    public void onDeviceFirmwareUpdateProgressChange(DeviceModel device, Double progress) {
    }

    /**
     * Event indicates that a place change has been requested (either by user action or because
     * we've lost access to the current place).
     *
     * This method puts the dashboard in a "Loading..." state pending completion of the active place
     * change. Once completed the {@link SessionActivePlaceSetEvent} event will be delivered.
     *
     * @param event
     */
    public void onEvent(PlaceChangeRequestedEvent event) {
        showProgressBar();
        ((BaseActivity)getActivity()).showTitle(false);
        shouldRedraw.set(false);
        HomeFragmentRecyclerAdapter adapter = (HomeFragmentRecyclerAdapter) cardsListView.getAdapter();
        adapter.removeAll();
        String placeRequested = event.getPlaceIDRequested();

        placeName = null;
        topImageCard = new TopImageCard(getActivity());
        topImageCard.setPlaceID(placeRequested);
        topImageCard.setHideSettingsGear(true);
        topImageCard.setPlaceName(null);

        BannerManager.in(getActivity()).clearBanners();

        adapter.addAtStart(topImageCard);

        // TODO: 5/5/16 APAP: Update to use correct card style/text
        CenteredTextCard centeredTextCard = new CenteredTextCard(getActivity());
        centeredTextCard.setTitle(getString(R.string.loading));
        centeredTextCard.useTransparentBackground(true);

        adapter.add(centeredTextCard);
    }

    /**
     * Event indicates the active place has successfully been changed and that dashboard should
     * redraw all cards accordingly.
     *
     * This event is generated following a {@link PlaceChangeRequestedEvent}.
     *
     * @param event
     */
    public void onEvent(SessionActivePlaceSetEvent event) {

        mHistoryCardController = new HistoryCardController(getActivity(), this);
        mHistoryCardController.updateHistoryLogEntries();
        cardsListView.post(new Runnable() {
            @Override public void run() {
                shouldRedraw.set(true);
                redrawDashboardCards();
                loadPlaceName();
                addHubPropertyChangeListener();
                showCellularBanners(null);
                ImageManager.with(getActivity()).setWallpaper(Wallpaper.ofCurrentPlace().darkened());

                hideProgressBar();
            }
        });
    }

    //InvitationController callbacks
    @Override
    public void updateView() {
        final PersonModel personModel = SessionController.instance().getPerson();
        if(personModel != null) {
            personModel.pendingInvitations()
                    .onSuccess(Listeners.runOnUiThread(new Listener<Person.PendingInvitationsResponse>() {
                        @Override public void onEvent(Person.PendingInvitationsResponse pendingInvitationResponse) {
                            List<Map<String, Object>> pending = pendingInvitationResponse.getInvitations();
                            if(pending.size() > 0) {
                                BannerManager.in(getActivity()).showBanner(new InvitationBanner());
                            }
                        }
                    }));
        }
    }

    /**
     * Fetches the list of places accessible to this user and when more than one exists, sets the
     * dashboard place name to the current place name. Hides the place name when only one place is
     * accessible to the user.
     */
    private void loadPlaceName() {
        AvailablePlacesProvider.instance()
              .load()
              .onSuccess(Listeners.runOnUiThread(new Listener<List<PlaceAndRoleModel>>() {
                  @Override public void onEvent(List<PlaceAndRoleModel> places) {
                      if (places.size() > 1) {
                          for (PlaceAndRoleModel place : places) {
                              if (String.valueOf(place.getPlaceId()).equals(SessionController.instance().getActivePlace())) {
                                  setPlaceName(place.getName());
                                  break;
                              }
                          }
                      } else {
                          // Hide place name if only one place is available to user
                          setPlaceName(null);
                      }
                  }
              }));
    }

    /**
     * Sets the text in the place name banner (just below the place image) to the given name. When
     * null, the place name banner will be hidden.
     *
     * @param name Name of the current place or null to hide the place name
     */
    private void setPlaceName(String name) {
        if (cardsListView == null || topImageCard == null) {
            return;
        }

        RecyclerView.Adapter adapter = cardsListView.getAdapter();
        if (adapter == null || adapter.getItemCount() < 1) {
            return;
        }

        placeName = name;
        topImageCard.setPlaceName(placeName);
        adapter.notifyItemChanged(0);
    }

    @Override public void showLoading() {}
    @Override public void onModelLoaded(PersonModel personModel) {}
    @Override public void onError(Throwable throwable) {}

    @Override public void show(CellBackupModel backupModel) {
        showCellularBanners(backupModel);
    }

    public void showCellularBanners(@Nullable CellBackupModel backupModel) {
        if (backupModel == null) {
            backupModel = CellularBackup.instance().getStatus();
        }

        if (backupModel.serviceSuspended()) {
            BannerManager.in(getActivity()).showBanner(new ServiceSuspendedBanner());
        }
        else if (backupModel.requiresConfiguration()) {
            BannerManager.in(getActivity()).showBanner(new ConfigureDeviceBanner());
        }
        else if (backupModel.needsServicePlan()) {
            BannerManager.in(getActivity()).showBanner(new UpdateServicePlanBanner());
        }
        else {
            BannerManager.in(getActivity()).removeBanner(ServiceSuspendedBanner.class);
            BannerManager.in(getActivity()).removeBanner(ConfigureDeviceBanner.class);
            BannerManager.in(getActivity()).removeBanner(UpdateServicePlanBanner.class);
            updateView();
        }
    }

    @Override
    public void updateHistory() {
        if(cardsListView.getAdapter() != null) {
            ((HomeFragmentRecyclerAdapter)cardsListView.getAdapter()).notifyCardChanged(mHistoryCardController.getCard());
        }
    }
}

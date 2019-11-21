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
package arcus.app.common.utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import arcus.cornea.SessionController;
import arcus.cornea.subsystem.SubsystemController;
import com.iris.client.capability.Device;
import com.iris.client.capability.Scene;
import com.iris.client.model.DeviceModel;
import com.iris.client.model.Model;
import com.iris.client.model.SubsystemModel;
import arcus.app.BuildConfig;
import arcus.app.dashboard.settings.services.ServiceCard;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Common shared preference routines.
 */
public class PreferenceUtils {

    private final static Logger logger = LoggerFactory.getLogger(PreferenceUtils.class);

    // Shared preference keys
    public static final String CARE_BEHAVIORS_DONT_SHOW_AGAIN = "CARE_BEHAVIORS_DONT_SHOW_AGAIN";
    public static final String CLIMATE_WALKTHROUGH_DONT_SHOW_AGAIN = "CLIMATE_WALKTHROUGH_DONT_SHOW_AGAIN";
    public static final String RULES_WALKTHROUGH_DONT_SHOW_AGAIN = "RULES_WALKTHROUGH_DONT_SHOW_AGAIN";
    public static final String SCENES_WALKTHROUGH_DONT_SHOW_AGAIN = "SCENES_WALKTHROUGH_DONT_SHOW_AGAIN";
    public static final String SECURITY_WALKTHROUGH_DONT_SHOW_AGAIN = "SECURITY_WALKTHROUGH_DONT_SHOW_AGAIN";
    public static final String ALARMS_WALKTHROUGH_DONT_SHOW_AGAIN = "ALARMS_WALKTHROUGH_DONT_SHOW_AGAIN";
    public static final String HISTORY_WALKTHROUGH_DONT_SHOW_AGAIN = "HISTORY_WALKTHROUGH_DONT_SHOW_AGAIN";

    private static final String SERVICE_CARD_ORDER = "service-card-order";
    private static final String SERVICE_CARD_VISIBLE = "service-card-visible";
    private static final String FAVORITES_ORDER = "favorites-order";
    private static final String TOKEN_KEY = "token key";
    private static final String LPID = "LPID";
    private static final String PLATFORM_URL = "platform-url";
    private static final String CONNECTION_MONITOR = "connection-monitor";
    private static final String GCM_REG_KEY = "gcm reg key";
    private static final String TRANSITION_ANIMATIONS_ENABLED = "transition-animations-enabled";
    private static final String LIGHTS_AND_SWITCHES_ORDER = "lights-and-switches-order";
    private static final String USER_REORDERED_SERVICE_CARDS = "user-ordered-cards";
    private static final String PICASSO_DEBUG = "IMAGE_DEBUG";
    private static final String PICASSO_INDICATORS = "IMAGE_INDICATORS";
    private static final String PICASSO_CACHE_DISABLED = "PICASSO_CACHE_DISABLED";
    private static final String PICASSO_MEMORY_CACHE_SIZE = "PICASSO_MEMORY_CACHE_SIZE";
    private static final String SHOW_WHATSNEW = "SHOW_WHATSNEW";
    private static final String PLACE_BACKGROUND_MAP = "place-background-map";
    private static final String TUTORIAL_FLAG = "tutorialFlagKey";
    private static final String WEATHER_RADIO_SHOW_SNOOZE = "weatherRadioShowSnooze";
    private static final String FORCE_ALARMS_VISIBLE = "FORCE_ALARMS_VISIBLE";
    private static final String FORCE_ALARMS_ORDER = "FORCE_ALARMS_ORDER";
    private static final String ALARM_ACTIVITY_FILTER = "ALARM_ACTIVITY_FILTER";
    private static final String UCC_CONTACT_PROMPT = "UCC_CONTACT_PROMPTED";
    private static final String UCC_CONTACT_ADDED = "UCC_CONTACT_ADDED";
    private static final String LAST_VERSION = "LAST_VERSION";
    private static final String USE_FINGERPRINT = "USE_FINGERPRINT";
    private static final String SEEN_FINGERPRINT = "SEEN_FINGERPRINT";

    public static void setHasSeenUccContactPrompt(boolean hasSeenPrompt) {
        PreferenceCache.getInstance().putBoolean(UCC_CONTACT_PROMPT, hasSeenPrompt);
    }

    public static boolean hasSeenUccContactPrompt() {
        return PreferenceCache.getInstance().getBoolean(UCC_CONTACT_PROMPT, false);
    }

    public static void setHasAddedUccContact(boolean hasAddedUccContact) {
        PreferenceCache.getInstance().putBoolean(UCC_CONTACT_ADDED, hasAddedUccContact);
    }

    public static boolean hasAddedUccContact() {
        return PreferenceCache.getInstance().getBoolean(UCC_CONTACT_ADDED, false);
    }

    public static void putPlaceBackgroundMap(Map<String,String> placeToBackgroundMap) {
        PreferenceCache.getInstance().putStringMap(PLACE_BACKGROUND_MAP, placeToBackgroundMap);
    }

    public static Map<String,String> getPlaceBackgroundMap() {
        return PreferenceCache.getInstance().getStringMap(PLACE_BACKGROUND_MAP);
    }

    public static void putOrderedLightsAndSwitchesList(List<String> lightsAndSwitchesList) {
        PreferenceCache.getInstance().putStringList(LIGHTS_AND_SWITCHES_ORDER, lightsAndSwitchesList);
    }

    public static List<String> getOrderedLightsAndSwitchesList() {
        return Arrays.asList(PreferenceCache.getInstance().getStringList(LIGHTS_AND_SWITCHES_ORDER));
    }

    public static void putOrderedFavoritesList(List<String> favoritesList) {
        PreferenceCache.getInstance().putStringList(FAVORITES_ORDER, favoritesList);
    }

    public static void putOrderedServiceList(List<ServiceCard> cardList) {
        putOrderedServiceList(cardList, true);
    }

    private static void putOrderedServiceList(List<ServiceCard> cardList, boolean fromUser) {
        PreferenceCache.getInstance().putStringList(SERVICE_CARD_ORDER, cardList);

        if (fromUser) {
            PreferenceCache.getInstance().putBoolean(USER_REORDERED_SERVICE_CARDS, true);
        }
    }

    public static List<String> getOrderedFavoritesList() {
        return Arrays.asList(PreferenceCache.getInstance().getStringList(FAVORITES_ORDER));
    }

    public static void setAnimationEnabled(boolean enabled) {
        PreferenceCache.getInstance().putBoolean(TRANSITION_ANIMATIONS_ENABLED, enabled);
    }

    public static boolean isAnimationEnabled() {
        return PreferenceCache.getInstance().getBoolean(TRANSITION_ANIMATIONS_ENABLED, false);
    }

    public static void setPicassoIndicatorsEnabled(boolean enabled) {
        PreferenceCache.getInstance().putBoolean(PICASSO_INDICATORS, enabled);
    }

    public static boolean arePicassoIndicatorsEnabled() {
        return PreferenceCache.getInstance().getBoolean(PICASSO_INDICATORS, false);
    }

    public static void setPicassoInDebugMode(boolean enabled) {
        PreferenceCache.getInstance().putBoolean(PICASSO_DEBUG, enabled);
    }

    public static boolean isPicassoInDebugMode() {
        return PreferenceCache.getInstance().getBoolean(PICASSO_DEBUG, false);
    }

    public static boolean isPicassoCacheDisabled() {
        return PreferenceCache.getInstance().getBoolean(PICASSO_CACHE_DISABLED, false);
    }

    public static void setPicassoCacheDisabled(boolean isDisabled) {
        PreferenceCache.getInstance().putBoolean(PICASSO_CACHE_DISABLED, isDisabled);
    }

    public static void setPicassoMemoryCacheSize(int percentOfHeap) {
        PreferenceCache.getInstance().putInteger(PICASSO_MEMORY_CACHE_SIZE, percentOfHeap);
    }

    public static int getPicassoMemoryCacheSize() {
        return PreferenceCache.getInstance().getInteger(PICASSO_MEMORY_CACHE_SIZE, 15);
    }

    public static void putVisibleServiceSet(@NonNull Set<ServiceCard> cardSet) {
        Set<String> cardStringSet = new HashSet<>();
        for (ServiceCard thisCard : cardSet) {
            cardStringSet.add(thisCard.toString());
        }

        PreferenceCache.getInstance().putStringSet(SERVICE_CARD_VISIBLE, cardStringSet);
    }

    @NonNull
    public static List<Model> getOrderedFavorites(@NonNull List<Model> favoriteModels) {

        // Previous local ordering (if it exists)
        List<String> orderedFavoriteIds = getOrderedFavoritesList();

        // Ordered items (items which do have a previously saved ordering)
        List<Model> orderedFavorites = new ArrayList<>();

        for (String thisFavoriteId : orderedFavoriteIds) {
            for (Model thisDevice : favoriteModels) {
                if (thisDevice.getId().equals(thisFavoriteId)) {
                    orderedFavorites.add(thisDevice);
                }
            }
        }

        // Unordered items (items which do not have a previously saved ordering)
        List<Model> unorderedFavorites = new ArrayList<>();

        for (Model thisFavorite : favoriteModels) {
            if (!orderedFavoriteIds.contains(thisFavorite.getId())) {
                unorderedFavorites.add(thisFavorite);
            }
        }

        // Apply default alpha sort to previously unordered items
        defaultModelSort(unorderedFavorites);

        // Append unordered items to end of previously ordered list
        orderedFavorites.addAll(unorderedFavorites);

        return orderedFavorites;
    }

    private static void defaultModelSort(List<Model> unsortedModels) {
        Collections.sort(unsortedModels, (firstModel, secondModel) -> {
            String firstModelName = (firstModel instanceof DeviceModel) ? String.valueOf(firstModel.get(Device.ATTR_NAME)) : String.valueOf(firstModel.get(Scene.ATTR_NAME));
            String secondModelName = (secondModel instanceof DeviceModel) ? String.valueOf(secondModel.get(Device.ATTR_NAME)) : String.valueOf(secondModel.get(Scene.ATTR_NAME));

            return firstModelName.compareToIgnoreCase(secondModelName);
        });
    }
    
    @NonNull
    public static Set<ServiceCard> getVisibleServiceSet() {

        Set<String> visibleCardStrings = PreferenceCache.getInstance()
              .getStringSet(SERVICE_CARD_VISIBLE, getDefaultVisibleServiceCards());

        Set<ServiceCard> visibleCards = new HashSet<>();
        for (String thisCardString : visibleCardStrings) {
            try {
                visibleCards.add(ServiceCard.valueOf(thisCardString));
            }
            catch (IllegalArgumentException e) {
                logger.error("Saved service card ({}) is invalid; can't determine saved card order");
            }
        }
        
        return forceAlarmsVisible(visibleCards);

    }

    @NonNull
    public static Set<String> getDefaultVisibleServiceCards() {
        Set<String> serviceCards = new HashSet<>();

        // By default, all service cards should be visible.
        for (ServiceCard thisCard : ServiceCard.values()) {
            serviceCards.add(thisCard.toString());
        }

        return serviceCards;
    }

    /**
     * Determines if the user has ordered their service card list (under dashboard settings) or if
     * we're currently using the default order.
     *
     * @return True if user reordered the list; false otherwise.
     */
    public static boolean isServiceCardListUserOrdered() {
        return PreferenceCache.getInstance().getBoolean(USER_REORDERED_SERVICE_CARDS, false);
    }

    @NonNull
    public static List<ServiceCard> getOrderedServiceCardList() {

        String[] savedList = PreferenceCache.getInstance().getStringList(SERVICE_CARD_ORDER);
        List<ServiceCard> serviceCardList = new ArrayList<>();

        if (savedList.length > 0) {
            for (String thisService : savedList) {
                try {
                    if(thisService.equals("SAFETY_ALARM")) {
                        //TODO: PROMON - we're removing this card, but there has to be a better way to do this
                    }
                    else {
                        serviceCardList.add(ServiceCard.valueOf(thisService));
                    }
                }
                catch (IllegalArgumentException e) {
                    logger.error("Saved service card ({}) is invalid; can't determine saved card order");
                    serviceCardList.clear();
                    Collections.addAll(serviceCardList, ServiceCard.values());
                }
            }
        }
        else {
            Collections.addAll(serviceCardList, ServiceCard.values());
        }

        return forceActiveSubsystemRefresh(forceAlarmsOrder(serviceCardList));
    }

    private static List<ServiceCard> forceActiveSubsystemRefresh(List<ServiceCard> serviceCardList){
        List<SubsystemModel> activeSubsystems = new ArrayList<>();
        Iterable<SubsystemModel> allSubsystems = SubsystemController.instance().getSubsystems().values();
        ServiceCard[] allCards = ServiceCard.values();

        for (SubsystemModel subsystemModel : allSubsystems) {
            if (subsystemModel.getAvailable()) {
                activeSubsystems.add(subsystemModel);
            }
        }

        for (ServiceCard card : allCards) {
            String cardShortName = card.name().split("_")[card.name().split("_").length - 1].toUpperCase();
            for (SubsystemModel activeSubsystem : activeSubsystems) {
                String subsystemShortName = activeSubsystem.getName().replace("Subsystem", "").toUpperCase();

                if (subsystemShortName.contains(cardShortName)) {
                    if (!serviceCardList.contains(card)) {
                        serviceCardList.add(card);
                        PreferenceUtils.putVisibleServiceSet(new HashSet<>(serviceCardList));
                    }
                }
            }
        }

        return serviceCardList;
    }

    @NonNull
    private static List<ServiceCard> forceAlarmsOrder(@NonNull List<ServiceCard> cardOrder) {

        if (PreferenceCache.getInstance().getBoolean(FORCE_ALARMS_ORDER, true)) {
            PreferenceCache.getInstance().putBoolean(FORCE_ALARMS_ORDER, false);
            addAlarmsCardToCardList(cardOrder);
            putOrderedServiceList(cardOrder);
        }
        return cardOrder;
    }

    private static void addAlarmsCardToCardList (@NonNull List<ServiceCard> cardOrder) {

        Set<ServiceCard> visible = getVisibleServiceSet();
        if(visible.contains(ServiceCard.SECURITY_ALARM)) {
            return;
        }

        ServiceCard beforeSecurity = null;
        for(ServiceCard card : cardOrder) {
            if(visible.contains(cardOrder)) {
                if(card == ServiceCard.SECURITY_ALARM) {
                    break;
                }
                beforeSecurity = card;
            }
        }

        ArrayList<ServiceCard> newVisible = new ArrayList<>();
        if(beforeSecurity != null) {
            for(ServiceCard card : visible) {
                newVisible.add(card);
                if(beforeSecurity == card) {
                    newVisible.add(ServiceCard.SECURITY_ALARM);
                }
            }
        }
        visible.clear();
        visible.addAll(newVisible);
        putVisibleServiceSet(visible);

        // Alarms card not shown.  Need to find the security_alarm card and force it to be in the list
        if (beforeSecurity != null && cardOrder.contains(beforeSecurity)) {
            cardOrder.add(cardOrder.indexOf(beforeSecurity) + 1, ServiceCard.SECURITY_ALARM);
        } else {
            cardOrder.add(0, ServiceCard.SECURITY_ALARM);
        }
    }

    @NonNull
    private static Set<ServiceCard> forceAlarmsVisible(@NonNull Set<ServiceCard> visibleCards) {
        if (PreferenceCache.getInstance().getBoolean(FORCE_ALARMS_VISIBLE, true)) {
            PreferenceCache.getInstance().putBoolean(FORCE_ALARMS_VISIBLE, false); // One-shot

            if (!visibleCards.contains(ServiceCard.SECURITY_ALARM)) {
                visibleCards.add(ServiceCard.SECURITY_ALARM);
                putVisibleServiceSet(visibleCards);
            }
        }
        return visibleCards;
    }

    public static void putConnectionMonitorEnabled(boolean enabled) {
        PreferenceCache.getInstance().putBoolean(CONNECTION_MONITOR, enabled);
    }

    public static boolean getConnectionMonitorEnabled() {
        return PreferenceCache.getInstance().getBoolean(CONNECTION_MONITOR, false);
    }

    public static String getPlatformUrl() {
        return PreferenceCache.getInstance().getString(PLATFORM_URL, GlobalSetting.ARCUS_PLATFORM_WS);
    }

    public static boolean isDevEnvironment() {
        return getPlatformUrl().toLowerCase().contains("dev-");
    }

    public static void putPlatformUrl(String url) {
        PreferenceCache.getInstance().putString(PLATFORM_URL, url);
    }

    public static void putGcmNotificationToken(String token) {
        PreferenceCache.getInstance().putString(GCM_REG_KEY, token);
    }

    @Nullable
    public static String getGcmNotificationToken() {
        return PreferenceCache.getInstance().getString(GCM_REG_KEY, null);
    }

    public static void removeLoginToken() {
        PreferenceCache.getInstance().removeKey(TOKEN_KEY);
    }

    public static void putLoginToken(String token) {
        PreferenceCache.getInstance().putString(TOKEN_KEY, token);
    }

    @Nullable
    public static String getLoginToken() {
        return PreferenceCache.getInstance().getString(TOKEN_KEY, null);
    }

    public static void removeLastPlaceID() {
        PreferenceCache.getInstance().removeKey(LPID);
    }

    public static void putLastPlaceID(String value) {
        PreferenceCache.getInstance().putString(LPID, value);
    }

    @Nullable
    public static String getLastPlaceID() {
        return PreferenceCache.getInstance().getString(LPID, null);
    }

    public static boolean hasSeenWhatsNew() {
        return !PreferenceCache.getInstance().getBoolean(SHOW_WHATSNEW, true);
    }

    public static void setSeenWhatsNew(boolean seenWhatsNew) {
        PreferenceCache.getInstance().putBoolean(SHOW_WHATSNEW, !seenWhatsNew);
    }

    public static boolean hasCompletedTutorial() {
        return PreferenceCache.getInstance().getBoolean(TUTORIAL_FLAG, false);
    }

    public static void setCompletedTutorial(boolean completedTutorial) {
        PreferenceCache.getInstance().putBoolean(TUTORIAL_FLAG, completedTutorial);
    }

    public static boolean getShowWeatherRadioSnooze() {
        return PreferenceCache.getInstance().getBoolean(WEATHER_RADIO_SHOW_SNOOZE, true);
    }

    public static void setShowWeatherRadioSnooze(boolean showWeatherRadioSnooze) {
        PreferenceCache.getInstance().putBoolean(WEATHER_RADIO_SHOW_SNOOZE, showWeatherRadioSnooze);
    }

    private static String getKeyWithPlace(String key) {
        String activePlace = SessionController.instance().getActivePlace();
        if (activePlace == null) {
            activePlace = "";
        }

        return String.format("%s%s", key, activePlace);
    }

    public static void putAlarmActivityFilter(String filter) {
        PreferenceCache.getInstance().putString(ALARM_ACTIVITY_FILTER, filter);
    }

    public static String getAlarmActivityFilter() {
        return PreferenceCache.getInstance().getString(ALARM_ACTIVITY_FILTER, "");
    }

    public static boolean hasUserUpgraded() {
        int last = PreferenceCache.getInstance().getInteger("LAST_VERSION", 0);
        if (BuildConfig.VERSION_CODE > last) {
            PreferenceCache.getInstance().putInteger("LAST_VERSION", BuildConfig.VERSION_CODE);
            setSeenWhatsNew(false);
            return true;
        }

        return false;
    }


    public static void setHasSeenFingerPrint(boolean hasSeen) {
        PreferenceCache.getInstance().putBoolean(SEEN_FINGERPRINT, hasSeen);
    }

    public static boolean getHasSeenFingerPrint() {
        return PreferenceCache.getInstance().getBoolean(SEEN_FINGERPRINT, false);
    }

    public static void setUseFingerPrint(boolean useFingerPrint) {
        PreferenceCache.getInstance().putBoolean(USE_FINGERPRINT, useFingerPrint);
    }

    public static boolean getUsesFingerPrint(){
        return PreferenceCache.getInstance().getBoolean(USE_FINGERPRINT, false);
    }
}

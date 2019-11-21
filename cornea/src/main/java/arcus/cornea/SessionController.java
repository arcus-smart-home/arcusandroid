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
package arcus.cornea;

import androidx.annotation.CheckResult;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import android.text.TextUtils;
import android.util.Pair;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import arcus.cornea.events.LogoutEvent;
import arcus.cornea.model.PlaceAndRoleModel;
import arcus.cornea.model.PlaceLostEvent;
import arcus.cornea.provider.AvailablePlacesProvider;
import arcus.cornea.provider.DeviceModelProvider;
import arcus.cornea.provider.HubModelProvider;
import arcus.cornea.provider.PersonModelProvider;
import arcus.cornea.provider.ProductModelProvider;
import arcus.cornea.utils.AddressableModelSource;
import arcus.cornea.utils.CachedModelSource;
import arcus.cornea.utils.Listeners;
import arcus.cornea.utils.LooperExecutor;
import arcus.cornea.utils.ModelSource;
import com.iris.capability.util.Addresses;
import com.iris.client.ClientEvent;
import com.iris.client.IrisClient;
import com.iris.client.IrisClientFactory;
import com.iris.client.bean.PersonAccessDescriptor;
import com.iris.client.capability.Account;
import com.iris.client.capability.Person;
import com.iris.client.capability.Place;
import com.iris.client.event.ClientFuture;
import com.iris.client.event.Futures;
import com.iris.client.event.Listener;
import com.iris.client.event.ListenerRegistration;
import com.iris.client.exception.UnauthorizedException;
import com.iris.client.model.AccountModel;
import com.iris.client.model.PersonModel;
import com.iris.client.model.PlaceModel;
import com.iris.client.session.Credentials;
import com.iris.client.session.SessionActivePlaceSetEvent;
import com.iris.client.session.SessionEvent;
import com.iris.client.session.SessionExpiredEvent;
import com.iris.client.session.SessionInfo;
import com.iris.client.session.SessionPlaceClearedEvent;
import com.iris.client.session.SessionTokenCredentials;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import de.greenrobot.event.EventBus;

final public class SessionController {

    private final int CACHE_LOAD_TIMEOUT_SEC = 30;
    private final int CACHE_COUNT = 7;                  // Number of caches we load on login

    public interface LoginCallback extends ErrorCallback {
        void loginSuccess(
              @Nullable PlaceModel placeModel,
              @Nullable PersonModel personModel,
              @Nullable AccountModel accountModel
        );
    }

    public interface LogoutCallback extends ErrorCallback {
        void logoutSuccess();
    }

    public interface ActivePlaceCallback extends ErrorCallback {
        void activePlaceChanged();
    }

    interface ErrorCallback {
        void onError(Throwable throwable);
    }

    private static final Logger logger = LoggerFactory.getLogger(SessionController.class);

    private static final SessionController INSTANCE;
    static {
        INSTANCE = new SessionController(
              CorneaClientFactory.getClient()
        );
    }

    private CountDownLatch loginEventsPending;
    private final AtomicReference<ClientFuture<SessionInfo>> loginFutureRef = new AtomicReference<>(null);
    private final AtomicReference<String> desiredPlace = new AtomicReference<>(null);
    private final IrisClient irisClient;
    private final AddressableModelSource<PlaceModel> placeRef = CachedModelSource.newSource();
    private final AddressableModelSource<PersonModel> personRef = CachedModelSource.newSource();
    private final AddressableModelSource<AccountModel> accountRef = CachedModelSource.newSource();
    private final Listener<SessionInfo> loggedInListener = new Listener<SessionInfo>() {
        @Override public void onEvent(SessionInfo sessionInfo) {
            updateSessionInfo(sessionInfo);
        }
    };
    private final Listener logoutListener = Listeners.runOnUiThread(new Listener<Object>() {
        @Override public void onEvent(Object o) {
            try {
                irisClient.close();
                CorneaClientFactory.getModelCache().clearCache();
                logoutSuccess();
            }
            catch (Exception ex) {
                logoutErrorListener.onEvent(ex);
            }
        }
    });
    private final Listener<UUID> loginToActivePlaceListener = Listeners.runOnUiThread(new Listener<UUID>() {
        @Override public void onEvent(UUID uuid) {
            finishLogin(uuid);
        }
    });
    private final Listener loaded = new Listener<Object>() {
        @Override
        public void onEvent(Object model) {
            loginEventsPending.countDown();
        }
    };
    private final Listener<AccountModel> placeSwitchedAndReloaded = Listeners.runOnUiThread(
          new Listener<AccountModel>() {
              @Override public void onEvent(AccountModel accountModel) {
                  onActivePlaceSwitchedSuccessfully();
              }
          }
    );
    protected Listener<UUID> onPlaceSwitchedListener = Listeners.runOnUiThread(new Listener<UUID>() {
        @Override public void onEvent(UUID uuid) {
            onActivePlaceSwitched();
        }
    });

    private final Listener<Throwable> loginErrorListener = Listeners.runOnUiThread(new Listener<Throwable>() {
        @Override public void onEvent(Throwable throwable) {
            onLoginError(throwable);
        }
    });
    private final Listener<Throwable> logoutErrorListener = Listeners.runOnUiThread(new Listener<Throwable>() {
        @Override public void onEvent(Throwable throwable) {
            onLogoutError(throwable);
        }
    });
    private final Listener<Throwable> activePlaceErrorListener = Listeners.runOnUiThread(new Listener<Throwable>() {
        @Override public void onEvent(Throwable throwable) {
            onActivePlaceError(throwable);
        }
    });

    private Reference<LoginCallback> loginCallbackRef = new SoftReference<>(null);
    private Reference<LogoutCallback> logoutCallbackRef = new SoftReference<>(null);
    private Reference<ActivePlaceCallback> activePlaceCallbackRef = new SoftReference<>(null);
    private final AtomicBoolean needPrivacyUpdate = new AtomicBoolean(false);
    private final AtomicBoolean needTermsUpdate = new AtomicBoolean(false);

    @VisibleForTesting
    protected SessionController(IrisClient client) {
        Preconditions.checkNotNull(client);

        irisClient = client;
        irisClient.addSessionListener(new Listener<SessionEvent>() {
            @Override
            public void onEvent(SessionEvent sessionEvent) {
                if (sessionEvent instanceof SessionPlaceClearedEvent) {
                    SessionController.this.onActivePlaceCleared();
                }
                else if (sessionEvent instanceof SessionExpiredEvent) {
                    LogoutCallback callback = logoutCallbackRef.get();
                    if (callback == null) { // Only post this if there isn't a callback, otherwise let the callback handle
                        postOnUiThread(new LogoutEvent());
                    }
                }

                logger.debug("SessionEvent: {} -> {}", sessionEvent.getClass().getSimpleName(), sessionEvent);
            }
        });
    }

    public static SessionController instance() {
        return INSTANCE;
    }

    public ListenerRegistration setCallback(LoginCallback callback) {
        loginCallbackRef = new SoftReference<>(callback);
        return Listeners.wrap(loginCallbackRef);
    }

    public ListenerRegistration setCallback(LogoutCallback callback) {
        logoutCallbackRef = new SoftReference<>(callback);
        return Listeners.wrap(logoutCallbackRef);
    }

    public ListenerRegistration setCallback(ActivePlaceCallback callback) {
        activePlaceCallbackRef = new SoftReference<>(callback);
        return Listeners.wrap(activePlaceCallbackRef);
    }

    public void login(@NonNull Credentials credentials, String activePlace) {
        Preconditions.checkNotNull(credentials, "Credentials cannot be null");

        ClientFuture<SessionInfo> loginStatus = loginFutureRef.get();
        if (loginStatus != null && !loginStatus.isDone()) {
            return;
        }

        desiredPlace.set(activePlace);
        runOnThread(new LoginRunnable(credentials));
    }

    public void reconnect() {
        SessionInfo sessionInfo = getSessionInfo();
        if (sessionInfo == null) {
            onLoginError(new UnauthorizedException("Invalid Session ID"));
            return;
        }

        SessionTokenCredentials credentials = new SessionTokenCredentials();
        credentials.setToken(sessionInfo.getSessionToken());
        credentials.setConnectionURL(irisClient.getConnectionURL());

        login(credentials, desiredPlace.get());
    }

    public void close() {
        try {
            CorneaClientFactory.getClient().close();
        } catch (Exception ex) {
            logger.error("Error closing connection.", ex);
        }
    }

    public void logout() {
        runOnThread(new LogoutRunnable());
    }

    @Nullable public SessionInfo getSessionInfo() {
        return irisClient.getSessionInfo();
    }

    @Nullable public SessionInfo.PlaceDescriptor getPlaceDescriptorForActivePlace() {
        for (SessionInfo.PlaceDescriptor thisPlaceDescriptor : irisClient.getSessionInfo().getPlaces()) {
            if (thisPlaceDescriptor.getPlaceId().equalsIgnoreCase(getActivePlace())) {
                return thisPlaceDescriptor;
            }
        }

        return null;
    }

    @Nullable public String getPersonId() {
        PersonModel personModel = personRef.get();
        return personModel == null ? null : personModel.getId();
    }

    @NonNull public String getPlaceIdOrEmpty() {
        PlaceModel placeModel = placeRef.get();
        return placeModel == null ? "" : placeModel.getId();
    }

    @Nullable public String getActivePlace() {
        UUID placeId = irisClient.getActivePlace();
        if (placeId != null)
            return placeId.toString();
        else
            return null;
    }

    public void changeActivePlace(@NonNull String uuidString) {
        if (TextUtils.isEmpty(uuidString)) {
            onActivePlaceError(new RuntimeException("Cannot switch places. Place ID was null/empty"));
            return;
        }

        if (uuidString.equals(getActivePlace())) {
            onActivePlaceSwitched();
            return;
        }

        desiredPlace.set(uuidString);
        irisClient
              .setActivePlace(uuidString)
              .onSuccess(onPlaceSwitchedListener)
              .onFailure(activePlaceErrorListener);
    }

    protected void updateSessionInfo(SessionInfo sessionInfo) {
        List<SessionInfo.PlaceDescriptor> places = sessionInfo.getPlaces();
        if (places == null || places.isEmpty()) {
            onLoginError(new RuntimeException("Unable to get the active place from provided descriptors."));
        }
        else {
            String desiredActivePlace = desiredPlace.get();
            if (TextUtils.isEmpty(desiredActivePlace)) {
                desiredActivePlace = getFirstOwnedOr0(places);
            }
            else {
                // Make sure the place set is in the list of places returned from the platform, default to the first place
                // where this user has a role of OWNER.  If there are no places that this person is an owner, default to place(0)
                boolean found = false;
                String placeOwnerAt = null;
                for (SessionInfo.PlaceDescriptor place : places) {
                    if (TextUtils.isEmpty(placeOwnerAt) && SessionInfo.PlaceDescriptor.ROLE_OWNER.equals(place.getRole())) {
                        placeOwnerAt = place.getPlaceId();
                    }

                    if (desiredActivePlace.equals(place.getPlaceId())) {
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    if (TextUtils.isEmpty(placeOwnerAt)) {
                        placeOwnerAt = places.get(0).getPlaceId();
                    }

                    desiredActivePlace = placeOwnerAt;
                    desiredPlace.set(desiredActivePlace);
                }
            }

            needPrivacyUpdate.set(Boolean.TRUE.equals(sessionInfo.getRequiresPrivacyPolicyConsent()));
            needTermsUpdate.set(Boolean.TRUE.equals(sessionInfo.getRequiresTermsAndConditionsConsent()));
            irisClient
                  .setActivePlace(desiredActivePlace)
                  .onFailure(loginErrorListener)
                  .onSuccess(loginToActivePlaceListener);
        }
    }

    protected String getFirstOwnedOrNull(List<PlaceAndRoleModel> places) {
        for (PlaceAndRoleModel place : places) {
            if (SessionInfo.PlaceDescriptor.ROLE_OWNER.equals(place.getRole())) {
                return place.getPlaceId();
            }
        }

        return null;
    }

    protected String getFirstOwnedOr0(List<SessionInfo.PlaceDescriptor> places) {
        for (SessionInfo.PlaceDescriptor place : places) {
            if (SessionInfo.PlaceDescriptor.ROLE_OWNER.equals(place.getRole())) {
                return place.getPlaceId();
            }
        }

        return places.get(0).getPlaceId();
    }

    @SuppressWarnings({"unchecked"}) protected void finishLogin(UUID uuid) {
        SessionInfo sessionInfo = getSessionInfo();
        if (sessionInfo == null) {
            onLoginError(new RuntimeException("Could not determine active session."));
            return;
        }

        List<SessionInfo.PlaceDescriptor> places = sessionInfo.getPlaces();
        if (places == null || places.isEmpty()) {
            onLoginError(new RuntimeException("Could not determine active place."));
            return;
        }

        // Write
        String setPlace = uuid.toString();
        SessionInfo.PlaceDescriptor place = getPlaceOr0(sessionInfo, setPlace);
        String personAddress    = Addresses.toObjectAddress(Person.NAMESPACE, sessionInfo.getPersonId());
        String placeAddress     = Addresses.toObjectAddress(Place.NAMESPACE, place.getPlaceId());
        String accountAddress   = Addresses.toObjectAddress(Account.NAMESPACE, place.getAccountId());

        placeRef.setAddress(placeAddress);
        personRef.setAddress(personAddress);
        accountRef.setAddress(accountAddress);

        loginEventsPending = new CountDownLatch(CACHE_COUNT);

        // Start loading caches
        placeRef.reload().onSuccess(loaded).onFailure(loginErrorListener);
        personRef.reload().onSuccess(loaded).onFailure(loginErrorListener);
        accountRef.reload().onSuccess(loaded).onFailure(loginErrorListener);
        DeviceModelProvider.instance().reload().onSuccess(loaded).onFailure(loginErrorListener);
        HubModelProvider.instance().reload().onSuccess(loaded).onFailure(loginErrorListener);
        PersonModelProvider.instance().reload().onSuccess(loaded).onFailure(loginErrorListener);
        ProductModelProvider.instance().reload().onSuccess(loaded).onFailure(loginErrorListener);

        // Wait for caches to finish loading before proceeding
        runOnThread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Give all caches 30 seconds to load; then bail
                    if (loginEventsPending.await(CACHE_LOAD_TIMEOUT_SEC, TimeUnit.SECONDS)) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                loginSuccess();
                            }
                        });
                    } else {
                        onLoginError(new RuntimeException("Cache load timeout"));
                    }
                }

                // This thread was interrupted; abort
                catch (InterruptedException e) {
                    onLoginError(e);
                }
            }
        });
    }

    public final boolean isAccountOwner() {
        PersonModel personModel = getPerson();
        if (personModel == null) {
            return false; // Default to false saying we're not the account owner
        }

        String personRole = PersonModelProvider.instance().getRole(personModel.getAddress());
        return PersonAccessDescriptor.ROLE_OWNER.equals(personRole);
    }

    public final ClientFuture<Boolean> isAccountOwner(String placeId) {
        Place.ListPersonsWithAccessRequest request = new Place.ListPersonsWithAccessRequest();
        request.setAddress("SERV:" + Place.NAMESPACE + ":" + placeId);
        return Futures.transform(IrisClientFactory.getClient().request(request), isCurrentPersonAccountOwner);
    }

    private static final Function<ClientEvent, Boolean> isCurrentPersonAccountOwner =
            new Function<ClientEvent, Boolean>() {
                @Override
                public Boolean apply(ClientEvent clientEvent) {
                    Place.ListPersonsWithAccessResponse response = new Place.ListPersonsWithAccessResponse(clientEvent);
                    List<Map<String, Object>> personsWithAccessDescriptors = response.getPersons();
                    if (personsWithAccessDescriptors == null) {
                        return false;
                    }
                    SessionInfo sessionInfo = IrisClientFactory.getClient().getSessionInfo();
                    for (Map<String, Object> personsWithAccessDescriptor : personsWithAccessDescriptors) {
                        PersonAccessDescriptor descriptor = new PersonAccessDescriptor(personsWithAccessDescriptor);
                        String address = (String) descriptor.getPerson().get(Person.ATTR_ADDRESS);
                        String currentPersonAddress = "SERV:person:" + sessionInfo.getPersonId();
                        if (address.equalsIgnoreCase(currentPersonAddress)) {
                            return PersonAccessDescriptor.ROLE_OWNER.equals(descriptor.getRole());
                        }
                    }
                    return false;
                }
            };

    public final boolean isClone() {
        PersonModel personModel = getPerson();
        if (personModel == null) {
            return false; // Default to false saying we're not a full access user
        }

        String personRole = PersonModelProvider.instance().getRole(personModel.getAddress());
        return PersonAccessDescriptor.ROLE_FULL_ACCESS.equals(personRole);
    }

    public final boolean isCloneWithPlace() {
        PersonModel personModel = getPerson();
        if (personModel == null) {
            return false;
        }

        String personRole = PersonModelProvider.instance().getRole(personModel.getAddress());
        return PersonAccessDescriptor.ROLE_FULL_ACCESS.equals(personRole) && !TextUtils.isEmpty(personModel.getCurrPlace());
    }

    public final boolean isHobbit() {
        PersonModel personModel = getPerson();
        if (personModel == null) {
            return true; // Default to saying we ARE a hobbit
        }

        String personRole = PersonModelProvider.instance().getRole(personModel.getAddress());
        return PersonAccessDescriptor.ROLE_HOBBIT.equals(personRole);
    }

    /**
     * Gets the Static Resource Base URL or an empty string.
     */
    public final String getStaticResourceBaseUrl() {
        SessionInfo sessionInfo = getSessionInfo();
        if (sessionInfo != null) {
            return sessionInfo.getStaticResourceBaseUrl();
        }

        return "";
    }

    /**
     * Gets the Honeywell Redirect URI or an empty string
     */
    public final String getHoneywellRedirectURI() {
        SessionInfo sessionInfo = getSessionInfo();
        if (sessionInfo != null) {
            return sessionInfo.getHoneywellRedirectUri();
        }

        return "";
    }

    /**
     * Gets the Lutron URI or an empty string
     */
    public final Pair<String, String> geLutronCookieValue() {
        SessionInfo sessionInfo = getSessionInfo();
        if (sessionInfo != null) {
            return Pair.create(sessionInfo.getLutronLoginBaseUrl(), "irisAuthToken=" + sessionInfo.getSessionToken());
        }

        return Pair.create("", "");
    }

    /**
     * Checks to see if there is evidence we should show (force) a screen to the user informing them we've
     * updated our T&C/Privacy statements.
     *
     * @return true if is the account owner and needs either terms or privacy policy update.
     */
    public final @CheckResult boolean needsTermsOrPrivacyUpdate() {
        return isAccountOwner() && (needTermsUpdate.get() || needPrivacyUpdate.get());
    }

    /**
     * Indicates if we need terms update or not. Used to know which to send - so we don't just blindly send both accepts.
     * *****Should NOT be used to decide if we should render a screen asking the user to accept new terms/privacy conditions.
     *
     * @return true if the session object said we needed to update and the user has not done so yet.
     */
    public final @CheckResult boolean needsTermsAndConditionsUpdate() {
        return needTermsUpdate.get();
    }

    /**
     * Indicates if we need privacy update or not. Used to know which to send - so we don't just blindly send both accepts.
     * *****Should NOT be used to decide if we should render a screen asking the user to accept new terms/privacy conditions.
     *
     * @return true if the session object said we needed to update and the user has not done so yet.
     */
    public final @CheckResult boolean needsPrivacyUpdate() {
        return needPrivacyUpdate.get();
    }

    /**
     * Toggle the current sessions value - Since we can't modify the Session Created object - we keep a local state.
     */
    public final void hasUpdatedTerms() {
        needTermsUpdate.set(false);
    }

    /**
     * Toggle the current sessions value - Since we can't modify the Session Created object - we keep a local state.
     */
    public final void hasUpdatedPrivacy() {
        needPrivacyUpdate.set(false);
    }

    @Nullable public PlaceModel getPlace() {
        placeRef.load();
        return placeRef.get();
    }

    @Nullable public PersonModel getPerson() {
        personRef.load();
        return personRef.get();
    }

    @Nullable public AccountModel getAccount() {
        accountRef.load();
        return accountRef.get();
    }

    @NonNull public ModelSource<PlaceModel> getPlaceRef() {
        return placeRef;
    }

    @NonNull public ModelSource<PersonModel> getPersonRef() {
        return personRef;
    }

    @NonNull public ModelSource<AccountModel> getAccountRef() {
        return accountRef;
    }

    protected SessionInfo.PlaceDescriptor getPlaceOr0(@NonNull SessionInfo sessionInfo, @NonNull String setPlace) {
        for (SessionInfo.PlaceDescriptor placeDescriptor : sessionInfo.getPlaces()) {
            if (placeDescriptor.getPlaceId().equals(setPlace)) {
                return placeDescriptor;
            }
        }

        return sessionInfo.getPlaces().get(0);
    }

    protected void onActivePlaceSwitched() {
        String activePlace = getActivePlace();
        if (TextUtils.isEmpty(activePlace)) {
            onActivePlaceError(new RuntimeException("Unable to fetch new active place."));
            return;
        }

        SessionInfo sessionInfo = getSessionInfo();
        if (sessionInfo == null) {
            onActivePlaceError(new RuntimeException("Unable to fetch new session information."));
            return;
        }

        List<SessionInfo.PlaceDescriptor> places = sessionInfo.getPlaces();
        if (places == null || places.isEmpty()) {
            onActivePlaceError(new RuntimeException("Unable to fetch new places from session."));
            return;
        }

        String personAddress    = Addresses.toObjectAddress(Person.NAMESPACE, sessionInfo.getPersonId());
        String placeAddress     = Addresses.toObjectAddress(Place.NAMESPACE,  activePlace);

         personRef.setAddress(personAddress);
          placeRef.setAddress(placeAddress);

         personRef.load()
               .onSuccess(new Listener<PersonModel>() {
                   @Override public void onEvent(PersonModel personModel) {
                       loadPlaceAfterSwitchPlaces();
                   }
               }).onFailure(activePlaceErrorListener);
    }

    protected void loadPlaceAfterSwitchPlaces() {
        placeRef.reload().onSuccess(new Listener<PlaceModel>() {
            @Override public void onEvent(PlaceModel placeModel) {
                loadAccountAfterSwitchPlaces(Addresses.toObjectAddress(Account.NAMESPACE, placeModel.getAccount()));
            }
        }).onFailure(activePlaceErrorListener);
    }

    protected void loadAccountAfterSwitchPlaces(String accountAddress) {
        // Load the account based off of the place.  This is to catch when we add a new place as the account
        // holder then go back to the dashboard and try to switch to it, it won't be in the Session Info's
        // list of PlaceDescriptors so we load it from the place
        accountRef.setAddress(accountAddress);
        accountRef.reload().onSuccess(placeSwitchedAndReloaded).onFailure(activePlaceErrorListener);
    }

    protected void onActivePlaceCleared() {
        AvailablePlacesProvider.instance().load().onSuccess(new Listener<List<PlaceAndRoleModel>>() {
            @Override
            public void onEvent(List<PlaceAndRoleModel> placeAndRoleModels) {
                String firstOwnedPlace = getFirstOwnedOrNull(placeAndRoleModels);

                // Current place went buh-bye and user doesn't own another place
                if (firstOwnedPlace == null) {
                    logger.debug("Existing place was cleared and user own no other places; logging out.");
                    logout();
                }

                // Current place is gone, but user has a place they own; switch to it
                else {
                    logger.debug("Place was cleared; switching to first owned place {}.", firstOwnedPlace);
                    postOnUiThread(new PlaceLostEvent(firstOwnedPlace));
                    changeActivePlace(firstOwnedPlace);
                }
            }
        });
    }

    protected void onActivePlaceSwitchedSuccessfully() {
        ActivePlaceCallback callback = activePlaceCallbackRef.get();
        if (callback != null) {
            callback.activePlaceChanged();
        }

        logger.debug("Active place successfully changed; notifying EventBus listeners.");

        EventBus.getDefault().removeAllStickyEvents();
        postOnUiThread(new SessionActivePlaceSetEvent(irisClient.getActivePlace()));
    }

    protected void loginSuccess() {
        LoginCallback callback = loginCallbackRef.get();
        if (callback != null) {
            callback.loginSuccess(placeRef.get(), personRef.get(), accountRef.get());
        }
    }

    protected void logoutSuccess() {
        LogoutCallback callback = logoutCallbackRef.get();
        if (callback != null) {
            callback.logoutSuccess();
        }
        else { // Only call this if there is not a callback that handles the logout event.
            EventBus.getDefault().removeAllStickyEvents();
            postOnUiThread(new LogoutEvent());
        }
    }

    protected void onLoginError(Throwable throwable) {
        onError(throwable, loginCallbackRef);
    }

    protected void onLogoutError(Throwable throwable) {
        onError(throwable, logoutCallbackRef);
    }

    protected void onActivePlaceError(Throwable throwable) {
        onError(throwable, activePlaceCallbackRef);
    }

    protected void onError(final Throwable throwable, Reference<? extends ErrorCallback> callbackReference) {
        final ErrorCallback callback = callbackReference.get();
        if (callback != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    callback.onError(throwable);
                }
            });
        }
    }

    protected void postOnUiThread(final Object event) {
        LooperExecutor.getMainExecutor().execute(new Runnable() {
            @Override
            public void run() {
                EventBus.getDefault().post(event);
            }
        });
    }

    protected void runOnThread(Runnable runnable) {
        new Thread(runnable).start();
    }

    protected void runOnUiThread(Runnable runnable) {
        LooperExecutor.getMainExecutor().execute(runnable);
    }

    class LoginRunnable implements Runnable {
        private final Credentials credentials;

        public LoginRunnable(Credentials loginCredentials) {
            this.credentials = loginCredentials;
        }

        @Override public void run() {
            if (CorneaClientFactory.isConnected()) {
                closeConnection();
            }

            ClientFuture<SessionInfo> loginStatus = irisClient.login(credentials);
            loginFutureRef.set(loginStatus);
            loginStatus.onSuccess(loggedInListener).onFailure(loginErrorListener);
        }

        protected void closeConnection() {
            try {
                irisClient.close();
            }
            catch (Exception ex) {
                logger.debug("Error closing socket.", ex);
            }
        }
    }

    class LogoutRunnable implements Runnable {
        @SuppressWarnings({"unchecked"}) @Override public void run() {
            irisClient.logout().onFailure(logoutErrorListener).onSuccess(logoutListener);
        }
    }

    public boolean isLoginPending() {
        return loginFutureRef != null && loginFutureRef.get() != null && !loginFutureRef.get().isDone();
    }
}

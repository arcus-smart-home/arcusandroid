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
package arcus.cornea.controller;

import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import arcus.cornea.CorneaClientFactory;
import arcus.cornea.SessionController;
import arcus.cornea.model.InviteModel;
import arcus.cornea.provider.PersonModelProvider;
import arcus.cornea.utils.AddressableModelSource;
import arcus.cornea.utils.CachedModelSource;
import arcus.cornea.utils.Listeners;
import com.iris.capability.definition.AttributeDefinition;
import com.iris.capability.definition.StaticDefinitionRegistry;
import com.iris.capability.util.Addresses;
import com.iris.client.ClientEvent;
import com.iris.client.IrisClient;
import com.iris.client.capability.Person;
import com.iris.client.capability.Place;
import com.iris.client.event.Listener;
import com.iris.client.event.ListenerRegistration;
import com.iris.client.exception.ErrorResponseException;
import com.iris.client.model.ModelAddedEvent;
import com.iris.client.model.PersonModel;
import com.iris.client.model.PlaceModel;
import com.iris.client.service.PersonService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class PersonController {
    private static final Logger logger = LoggerFactory.getLogger(PersonController.class);
    private final static int MAX_TIMES_CHECK_FOR_MODEL = 10;
    private final static int INTERVAL_BETWEN_CHECK_FOR_MODEL_MS = 500;
    private final static String PIN_SUCCESS_ATTRIBUTE = "success";
    private final static int DEFAULT_TIMEOUT = 30_000;
    private final static String PERSON_PREFIX = "SERV:" + Person.NAMESPACE + ":";
    private final static String PLACE_PREFIX = "SERV:" + Place.NAMESPACE + ":";
    private final static PersonController INSTANCE;
    private final List<String> writeableAttribues;
    private final List<String> readableAttribues;
    private final IrisClient client;
    private Mode mode;

    private AtomicBoolean storeLoaded = new AtomicBoolean(false);
    private WeakReference<Callback> callbackRef;
    private AddressableModelSource<PersonModel> personModel;
    private Map<String, Object> newPersonValues;

    private WeakReference<Predicate<PersonModel>> selectedFilter = new WeakReference<>(null);
    private Predicate<PersonModel> fullPersons = new Predicate<PersonModel>() {
        @Override
        public boolean apply(PersonModel input) {
            return input != null && Boolean.TRUE.equals(input.getHasLogin());
        }
    };

    private ListenerRegistration storeLoadedListener;
    private ListenerRegistration modelAddedListener;
    private Listener<Throwable> failureListener = Listeners.runOnUiThread(new Listener<Throwable>() {
        @Override
        public void onEvent(Throwable throwable) {
            if (isNotUniqueError(throwable)) {
                // Calling updateView on the pin page will cause goNext() to be called so we
                // only call on error in this case.
                Callback callback = callbackRef.get();
                if (callback != null) {
                    callback.onError(throwable);
                }
            } else {
                reloadPersonAndEmitError(throwable);
            }
        }

        private boolean isNotUniqueError(Throwable throwable) {
            return throwable instanceof ErrorResponseException &&
                    ((ErrorResponseException) throwable).getCode().equals("pin.notUniqueAtPlace");
        }

        private void reloadPersonAndEmitError(final Throwable throwable) {
            personModel
                    .reload()
                    .onCompletion(
                            Listeners.runOnUiThread(result -> {
                                Callback callback = callbackRef.get();
                                if (callback != null) {
                                    if (result.isValue()) {
                                        callback.updateView(result.getValue());
                                    }
                                    callback.onError(throwable);
                                }
                            })
                    );
        }
    });

    private Listener<ClientEvent> successListener = Listeners.runOnUiThread(new Listener<ClientEvent>() {
        @Override
        public void onEvent(ClientEvent clientEvent) {
            Callback callback = callbackRef.get();
            if (callback != null) {
                callback.updateView(personModel.get());
            }
        }
    });

    static {
        INSTANCE = new PersonController(CorneaClientFactory.getClient());
    }

    enum Mode {
        CREATE,
        EDIT,
        VIEW
    }

    public interface Callback {
        /**
         * Called when Network IO is taking place.
         */
        void showLoading();

        /**
         * Called after an operation has completed successfully.
         *
         * @param personModel Person currently updating.
         */
        void updateView(PersonModel personModel);

        /**
         *
         * Called when the selected rule for editing is available and loaded
         *
         * @param personModel person being edited
         */
        void onModelLoaded(PersonModel personModel);

        /**
         * Called when the requested models are loaded.
         *
         * @param personList List of persons requested
         */
        void onModelsLoaded(@NonNull List<PersonModel> personList);

        /**
         * Called when a Network IO operation returned an ErrorEvent
         *
         * @param throwable error encountered
         */
        void onError(Throwable throwable);

        /**
         * Called when a person is created, but we were unable to find a model that matches the person we created.
         */
        void createdModelNotFound();
    }

    public interface CreateInviteCallback {
        void inviteError(Throwable throwable);
        void inviteCreated(InviteModel inviteModel);
    }

    public interface SendInviteCallback {
        void inviteError(Throwable throwable);
        void personInvited();
    }

    PersonController(IrisClient client) {
        this.client = client;
        init();
        List<String> writeAttribs = new LinkedList<>();
        List<String> readAttribs = new LinkedList<>();
        for (AttributeDefinition definition : StaticDefinitionRegistry.getInstance().getCapability(Person.NAMESPACE).getAttributes()) {
            if (definition.isWritable()) {
                writeAttribs.add(definition.getName());
            }
            if (definition.isReadable()) {
                readAttribs.add(definition.getName());
            }
        }
        writeableAttribues = Collections.unmodifiableList(writeAttribs);
        readableAttribues = Collections.unmodifiableList(readAttribs);
    }

    protected void init() {
        reset();

        if (!PersonModelProvider.instance().isLoaded()) {
            callShowLoading();
            PersonModelProvider.instance().load();
        }

        storeLoadedListener = PersonModelProvider.instance().addStoreLoadListener(Listeners.runOnUiThread(new Listener<List<PersonModel>>() {
            @Override
            public void onEvent(List<PersonModel> personModels) {
                storeLoaded.set(true);

                Callback callback = callbackRef.get();
                if (callback != null) {
                    callback.onModelsLoaded(getModels(selectedFilter.get()));
                }
                Listeners.clear(storeLoadedListener);
            }
        }));
    }

    protected void setCallback(Callback callback) {
        if (this.callbackRef.get() != null) {
            logger.warn("Updating callbacks with [{}].", callback);
        }

        this.callbackRef = new WeakReference<>(callback);
    }

    protected void getFilteredPersons(Callback callback, Predicate<PersonModel> predicate) {
        selectedFilter = new WeakReference<>(predicate);
        setCallback(callback);
        setMode(Mode.VIEW);

        if (!storeLoaded.get()) {
            callShowLoading();
        }
        else {
            if (callback != null) {
                callback.onModelsLoaded(getModels(predicate));
            }
        }
    }

    protected List<PersonModel> getModels(Predicate<PersonModel> predicate) {
        return Lists.newArrayList(Iterables.filter(PersonModelProvider.instance().getStore().values(), predicate));
    }


    public boolean hasPeople(){
        //the default users will be there by default (so must be 1)
        return getModels(Predicates.<PersonModel>alwaysTrue()).size() > 1;
    }

    protected void updateView() {
        Callback callbacks = callbackRef.get();
        if (callbacks != null) {
            callbacks.updateView(personModel.get());
        }
    }

    protected void reset() {
        mode = Mode.VIEW;
        callbackRef = new WeakReference<>(null);
        personModel = CachedModelSource.newSource();
        selectedFilter = new WeakReference<>(Predicates.<PersonModel>alwaysTrue());
        newPersonValues = new HashMap<>();
        removeModelAddedListener();
    }

    protected void setMode(Mode newMode) {
        logger.info("Changing to [{}] mode", newMode.name());
        this.mode = newMode;
    }

    protected boolean inEditMode() {
        return mode.equals(Mode.EDIT);
    }

    protected boolean inCreateMode() {
        return mode.equals(Mode.CREATE);
    }

    protected String getPersonAddress(String address) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(address));

        if (address.startsWith(PERSON_PREFIX)) {
            return address;
        }
        else {
            return PERSON_PREFIX + address;
        }
    }

    protected void callShowLoading() {
        Callback callback = callbackRef.get();
        if (callback != null) {
            callback.showLoading();
        }
    }

    protected void removeModelAddedListener() {
        if (modelAddedListener != null && modelAddedListener.isRegistered()) {
            modelAddedListener.remove();
        }
    }

    /**
     *
     * Currently, we do not know what model ID the new person has, however, we can tell if that operation succeeded or
     * failed.  This checks the store for models matching the first/last name of the person we just tried to create
     * and then uses that as the person ....
     *
     * @param callback Callback to post event to
     * @param timesCalled Number of times we've checked the store.
     * @param pinNumber pin number, optional, to create for the person.
     */
    protected void completeCreateUser(final Callback callback, final int timesCalled, final String pinNumber, final String placeID) {
        if (personModel.get() != null) {
            logger.debug("Found model after [{}] attempts.", timesCalled);

            if (!Strings.isNullOrEmpty(pinNumber)) {
                doUpdatePin(pinNumber, placeID);
            }
            else {
                updateView();
            }

            removeModelAddedListener();
        }
        else if (timesCalled <= MAX_TIMES_CHECK_FOR_MODEL) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    completeCreateUser(callback, timesCalled + 1, pinNumber, placeID);
                }
            }, INTERVAL_BETWEN_CHECK_FOR_MODEL_MS);
        }
        else {
            callback.createdModelNotFound();
            removeModelAddedListener();
        }
    }

    public static PersonController instance() {
        return INSTANCE;
    }

    public @Nullable PersonModel getPerson() {
        return personModel.get();
    }

    /**
     * Fetches all persons from the store; if the store is not loaded this will load the store as well.
     * Sets Mode -> Mode.VIEW
     *
     * @param callback Callback channel
     */
    public void getPersons(@NonNull Callback callback) {
        getFilteredPersons(callback, Predicates.<PersonModel>alwaysTrue());
    }

    /**
     * Fetches all persons from the store; if the store is not loaded this will load the store as well.
     * The list is then filtered to include only those matching the {@code personModelPredicate}
     *
     * Sets Mode -> Mode.VIEW
     *
     * @param callback Callback channel
     */
    public void getPersons(@NonNull Callback callback, @NonNull Predicate<PersonModel> personModelPredicate) {
        getFilteredPersons(callback, personModelPredicate);
    }

    /**
     * Fetches only persons with logins from the store; if the store is not loaded this will load the store as well.
     * Sets Mode -> Mode.VIEW
     *
     * @param callback Callback channel
     */
    public void getPersonsWithLogins(@NonNull Callback callback) {
        getFilteredPersons(callback, fullPersons);
    }

    /**
     * Fetches only hobbits from the store; if the store is not loaded this will load the store as well.
     * Sets Mode -> Mode.VIEW
     *
     * @param callback Callback channel
     */
    public void getPersonsWithoutLogins(@NonNull Callback callback) {
        getFilteredPersons(callback, Predicates.not(fullPersons));
    }

    /**
     * Start creating a new person, this resets the controller state.
     */
    public void startNewPerson() {
        reset();
        setMode(Mode.CREATE);
    }

    /**
     * Start editing a person, and funnel all callbacks through {@code callback}
     *
     * @param personAddress Address of person to be edited.
     * @param callback Callback channel
     */
    public void edit(@NonNull String personAddress, @NonNull Callback callback) {
        if (Strings.isNullOrEmpty(personAddress)) {
            logger.debug("Cannot edit a null/empty model, received [{}]", personAddress);
            return;
        }
        else if (getPersonAddress(personAddress).equals(personModel.getAddress()) && personModel.get() != null) {
            setCallback(callback);
            setMode(Mode.EDIT);
            callback.onModelLoaded(personModel.get());
            return;
        }

        reset();
        setCallback(callback);
        setMode(Mode.EDIT);

        String newPersonAddress = getPersonAddress(personAddress);
        personModel.setAddress(newPersonAddress);
        if (personModel.get() != null) {
            callback.onModelLoaded(personModel.get());
        }
        else {
            callShowLoading();
            personModel
                  .reload()
                  .onFailure(failureListener)
                  .onSuccess(Listeners.runOnUiThread(new Listener<PersonModel>() {
                      @Override
                      public void onEvent(PersonModel model) {
                          Callback callback = callbackRef.get();
                          if (callback != null) {
                              callback.onModelLoaded(model);
                          }
                      }
                  }));
        }
    }

    /**
     * Set a value to be used in update/creation.  This checks to see if the attribute you are trying to set is
     * contained within the Person definition AND is writeable.  If not, will fail with a debug message.
     *
     * To avoid issues setting values, use attributes from Person.ATTR_*
     *
     * @param key Person.ATTR_* key
     * @param value value
     */
    public void set(@NonNull String key, Object value) {
        if (Strings.isNullOrEmpty(key)) {
            logger.error("Cannot set a null key name.  Key [{}], Value [{}]", key, value);
            return;
        }
        else if (!writeableAttribues.contains(key)) {
            if (!(inCreateMode() && key.equals(Person.ATTR_TAGS))) {
                logger.error("Cannot set a non-writeable person key. Available options: [{}]", writeableAttribues);
                return;
            }
        }

        if (!inEditMode()) {
            newPersonValues.put(key, value);
        }
        else {
            personModel.get().set(key, value);
        }
    }

    public @Nullable Object get(@NonNull String key) {
        if (!readableAttribues.contains(key)) {
            logger.error("Cannot get a non-readable person key. Available options: [{}]", readableAttribues);
            return null;
        }

        if (!inEditMode()) {
            return newPersonValues.get(key);
        }
        else {
            return personModel.get().get(key);
        }
    }

    public void setNewCallback(@NonNull Callback callback) {
        setCallback(callback);
    }

    /**
     * Create a new person.
     *
     * @param password optional password to use
     * @param pinNumber optional pin number
     */
    public void createPerson(@Nullable char[] password, @Nullable final String pinNumber, final String placeID) {
        if (!inCreateMode()) {
            logger.error("Cannot create person while not in create mode.");
            return;
        }

        callShowLoading();
        Place.AddPersonRequest request = new Place.AddPersonRequest();
        request.setAddress(PLACE_PREFIX + client.getActivePlace());
        request.setTimeoutMs(DEFAULT_TIMEOUT);
        if (password != null) {
            request.setPassword(new String(password));
        }
        request.setPerson(newPersonValues);

        removeModelAddedListener();
        modelAddedListener = PersonModelProvider.instance().getStore().addListener(ModelAddedEvent.class, new Listener<ModelAddedEvent>() {
            @Override
            public void onEvent(ModelAddedEvent modelAddedEvent) {
                PersonModel input = (PersonModel) modelAddedEvent.getModel();

                String modelFirstName = String.valueOf(input.getFirstName());
                String modelLastName = String.valueOf(input.getLastName());
                String createdFirstName = String.valueOf(newPersonValues.get(Person.ATTR_FIRSTNAME));
                String createdLastName = String.valueOf(newPersonValues.get(Person.ATTR_LASTNAME));

                if (modelFirstName.equals(createdFirstName) && modelLastName.equals(createdLastName)) {
                    personModel.setAddress(input.getAddress());
                }
            }
        });

        client
              .request(request)
              .onFailure(failureListener)
              .onSuccess(Listeners.runOnUiThread(new Listener<ClientEvent>() {
                  @Override
                  public void onEvent(ClientEvent clientEvent) {
                      Callback callback = callbackRef.get();
                      if (callback != null) {
                          completeCreateUser(callback, 0, pinNumber, placeID);
                      }
                  }
              }));
    }

    @SuppressWarnings({"unchecked", "ConstantConditions"}) public void createInvite(@NonNull final CreateInviteCallback inviteCallback) {
        // Can only invite people to the currently logged in place?
        PlaceModel placeModel = SessionController.instance().getPlace();
        if (placeModel == null || !inCreateMode() || inviteCallback == null) {
            throw new RuntimeException("Cannot create invite for person not creating, place model was null or cb was null.");
        }

        String firstName = (String) newPersonValues.get(Person.ATTR_FIRSTNAME);
        String lastName = (String) newPersonValues.get(Person.ATTR_LASTNAME);
        String email = (String) newPersonValues.get(Person.ATTR_EMAIL);
        Set<String> tags = (Set<String>) newPersonValues.get(Person.ATTR_TAGS);
        String relationship = "";
        if (tags != null && !tags.isEmpty()) {
            relationship = tags.iterator().next();
        }

        placeModel.createInvitation(firstName, lastName, email, relationship)
              .onSuccess(Listeners.runOnUiThread(new Listener<Place.CreateInvitationResponse>() {
                  @Override public void onEvent(Place.CreateInvitationResponse createInvitationResponse) {
                      Map<String, Object> invite = createInvitationResponse.getInvitation();
                      if (invite == null) {
                          invite = Collections.emptyMap();
                      }

                      InviteModel model = new InviteModel(invite);
                      inviteCallback.inviteCreated(model);
                  }
              }))
              .onFailure(Listeners.runOnUiThread(new Listener<Throwable>() {
                  @Override public void onEvent(Throwable throwable) {
                      inviteCallback.inviteError(throwable);
                  }
              }));
    }

    @SuppressWarnings({"ConstantConditions"}) public void sendInvite(@NonNull InviteModel inviteModel, @NonNull final SendInviteCallback inviteCallback) {
        PlaceModel placeModel = SessionController.instance().getPlace();
        if (placeModel == null || !inCreateMode() || inviteCallback == null || inviteModel == null) {
            throw new RuntimeException("Cannot create invite for person not creating, place model was null or cb was null.");
        }

        placeModel.sendInvitation(inviteModel.toMap())
              .onSuccess(Listeners.runOnUiThread(new Listener<Place.SendInvitationResponse>() {
                  @Override public void onEvent(Place.SendInvitationResponse sendInvitationResponse) {
                      inviteCallback.personInvited();
                  }
              }))
              .onFailure(Listeners.runOnUiThread(new Listener<Throwable>() {
                  @Override public void onEvent(Throwable throwable) {
                      inviteCallback.inviteError(throwable);
                  }
              }));
    }

    /**
     *
     * Change the password for the current person being edited.
     *
     * @param currentPassword existing pwd
     * @param newPassword new pwd
     */
    public void changePassword(@NonNull char[] currentPassword, @NonNull char[] newPassword) {
        if (!inEditMode()) {
            logger.error("Not in edit mode, cannot change password.");
            return;
        }

        callShowLoading();
        PersonService.ChangePasswordRequest request = new PersonService.ChangePasswordRequest();
        request.setRestfulRequest(true);
        request.setTimeoutMs(DEFAULT_TIMEOUT);
        request.setAddress(PersonService.CMD_CHANGEPASSWORD);
        request.setEmailAddress(personModel.get().getEmail());
        request.setCurrentPassword(new String(currentPassword));
        request.setNewPassword(new String(newPassword));

        client
              .request(request)
              .onFailure(failureListener)
              .onSuccess(successListener);
    }

    /**
     * Delete the current person being edited.
     */
    public void removePerson() {
        if (!inEditMode()) {
            logger.error("Cannot remove person while not in edit mode.");
            return;
        }

        callShowLoading();
        Person.DeleteRequest request = new Person.DeleteRequest();
        request.setAddress(personModel.getAddress());
        request.setTimeoutMs(DEFAULT_TIMEOUT);
        client
              .request(request)
              .onFailure(failureListener)
              .onSuccess(successListener);
    }

    /**
     * Save changes to the current person being edited.
     */
    public void updatePerson() {
        if (!inEditMode()) {
            logger.error("Cannot update person while not in edit mode.");
            return;
        }

        callShowLoading();
        PersonModel model = personModel.get();
        model
              .commit()
              .onFailure(failureListener)
              .onSuccess(successListener);
    }

    public void updatePin(String pinNumber, String placeID) {
        if (!inEditMode()) {
            logger.error("Cannot update pin while not in edit mode.");
            return;
        }

        callShowLoading();
        doUpdatePin(pinNumber, placeID);
    }

    private void doUpdatePin(String pinNumber, String placeID) {
        Person.ChangePinV2Request request = new Person.ChangePinV2Request();
        request.setAddress(personModel.getAddress());
        request.setRestfulRequest(true);
        request.setTimeoutMs(DEFAULT_TIMEOUT);
        request.setPlace(Addresses.getId(placeID));
        request.setPin(pinNumber);

        client
              .request(request)
              .onFailure(failureListener)
              .onSuccess(Listeners.runOnUiThread(new Listener<ClientEvent>() {
                  @Override
                  public void onEvent(ClientEvent clientEvent) {
                      Callback callback = callbackRef.get();
                      if (callback != null) {
                          // Not sure why this call responds this way, but a "success" doesn't always mean we
                          // changed the pin #.
                          if (Boolean.TRUE.equals(clientEvent.getAttribute(PIN_SUCCESS_ATTRIBUTE))) {
                              updateView();
                          }
                          else {
                              callback.onError(new RuntimeException("Pin was not updated."));
                          }
                      }
                  }
              }));
    }
}

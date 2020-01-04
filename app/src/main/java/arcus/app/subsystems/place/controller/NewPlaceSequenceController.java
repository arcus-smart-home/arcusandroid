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
package arcus.app.subsystems.place.controller;

import android.app.Activity;
import android.graphics.Bitmap;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import arcus.cornea.SessionController;
import arcus.cornea.provider.PlaceModelProvider;
import arcus.cornea.utils.Listeners;
import com.iris.capability.util.Addresses;
import com.iris.client.capability.Place;
import com.iris.client.event.Listener;
import com.iris.client.model.PlaceModel;
import arcus.app.ArcusApplication;
import arcus.app.account.registration.controller.task.SaveHomeTask;
import arcus.app.account.settings.pin.SettingsUpdatePin;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.image.ImageCategory;
import arcus.app.common.image.ImageRepository;
import arcus.app.common.sequence.AbstractStaticSequenceController;
import arcus.app.common.sequence.Sequenceable;
import arcus.app.common.sequence.SequencedFragment;
import arcus.app.dashboard.AddMenuFragment;
import arcus.app.dashboard.HomeFragment;
import arcus.app.integrations.Address;
import arcus.app.subsystems.place.PlaceCongratsFragment;
import arcus.app.subsystems.place.PlaceDoneHelpFragment;
import arcus.app.subsystems.place.model.PlaceTypeSequence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class NewPlaceSequenceController extends AbstractStaticSequenceController {
    private static final Logger logger = LoggerFactory.getLogger(NewPlaceSequenceController.class);

    private PlaceTypeSequence placeType;
    private String placeAddress;
    private String existingPlaceNickname;
    private String existingServiceLevel;

    private Address newPlaceAddressEntered;
    private String newPlaceNickname;
    private Bitmap newPlaceBitmap;

    public interface PrimaryPlaceServiceLevelCallback {
        void onError();
        void onSuccess(String serviceLevel);
    }

    public interface CreatePlaceCallback {
        void onError(Throwable throwable);
        void onSuccess();
    }

    public NewPlaceSequenceController(PlaceTypeSequence placeType) {
        this.placeType = placeType;
    }

    @Override
    public void startSequence(Activity activity, Sequenceable from, Object... data) {
        Class<? extends SequencedFragment> firstFragment = (Class<? extends SequencedFragment>) data[0];
        navigateForward(activity, newInstanceOf(firstFragment));
    }

    @Override
    public void goNext(Activity activity, @NonNull Sequenceable from, Object... data) {
        if(from instanceof PlaceCongratsFragment) {
            BackstackManager.getInstance().navigateBackToFragment(HomeFragment.newInstance());
            BackstackManager.getInstance().navigateToFloatingFragment(PlaceDoneHelpFragment.newInstance(true),PlaceDoneHelpFragment.class.getName(),true);
        }
        else {
            navigateToNextSequenceable(activity, placeType.getSequence(), from.getClass(), data);
        }
    }

    @Override
    public void goBack(Activity activity, @NonNull Sequenceable from, Object... data) {
        if (from instanceof SettingsUpdatePin) {
            return; // Block back button on back-button presses so we don't double add the place.
        }

        //this will terminate the new place Sequence if it has been successful rather than backing out.
        if (from instanceof PlaceCongratsFragment || from instanceof PlaceDoneHelpFragment){
            BackstackManager.getInstance().navigateBackToFragment(HomeFragment.newInstance());
            return;
        }

        boolean success = navigateToPreviousSequenceable(activity, placeType.getSequence(), from.getClass(), data);
        if (!success) {
            endSequence(activity, success);
        }
    }

    @Override
    public void endSequence(Activity activity, boolean isSuccess, Object... data) {

        BackstackManager.getInstance().navigateBackToFragment(AddMenuFragment.newInstance());
    }

    @Override
    protected Sequenceable newInstanceOf(@NonNull Class<? extends Sequenceable> clazz, Object... data) {

        if (clazz == SettingsUpdatePin.class) {
            String personID = SessionController.instance().getPersonId();
            String placeID  = Addresses.getId(placeAddress);
            return SettingsUpdatePin.newInstance(SettingsUpdatePin.ScreenVariant.ADD_A_PLACE, personID, placeID);
        }

        return super.newInstanceOf(clazz);
    }

    public void setPlaceAddress (String placeAddress) {
        this.placeAddress = placeAddress;
    }

    public String getPlaceAddress () {
        return this.placeAddress;
    }

    public String getExistingPlaceNickname() {
        return existingPlaceNickname;
    }

    public String getNewPlaceNickname() {
        return newPlaceNickname;
    }

    public void setNewPlaceNickname(String newPlaceNickname) {
        this.newPlaceNickname = newPlaceNickname;
    }

    public @Nullable Address getNewPlaceAddressEntered() {
        return newPlaceAddressEntered;
    }

    public void setNewPlaceAddressEntered(Address newPlaceAddressEntered) {
        this.newPlaceAddressEntered = newPlaceAddressEntered;
    }

    public Bitmap getNewPlaceBitmap() {
        return newPlaceBitmap;
    }

    public void setNewPlaceBitmap(Bitmap newPlaceBitmap) {
        this.newPlaceBitmap = newPlaceBitmap;
    }

    public void addNewPlace(@NonNull final CreatePlaceCallback createPlaceCallback) {
        if (PlaceTypeSequence.ADD_PLACE_OWNER.equals(placeType)) {
            addAnotherPlace(createPlaceCallback);
        }
        else {
            addInitialPlace(createPlaceCallback);
        }
    }

    protected void addInitialPlace(@NonNull final CreatePlaceCallback createPlaceCallback) {
        new SaveHomeTask(newPlaceNickname, newPlaceAddressEntered, new SaveHomeTask.AddAnotherPlaceCallback() {
            @Override public void onError(Throwable throwable) {
                NewPlaceSequenceController.this.onError(throwable, createPlaceCallback);
            }

            @Override public void onSuccess(String newPlaceAddress) {
                placeAddress = newPlaceAddress;
                String newPlaceID = Addresses.getId(newPlaceAddress);
                if (newPlaceBitmap != null) {
                    ImageRepository.saveImage(ArcusApplication.getContext(), newPlaceBitmap, ImageCategory.PLACE, newPlaceID, null);
                }
                SessionController.instance().changeActivePlace(Addresses.getId(newPlaceAddress));
                NewPlaceSequenceController.this.onSuccess(createPlaceCallback);
            }
        }).promoteToAccount(SessionController.instance().getPerson());
    }

    protected void createInitialPlaceWithAccount() {}

    // Account holder flow
    // Get the service level of the 'main' place
    // Then call SaveHomeTask to add the new place to the extisting account
    protected void addAnotherPlace(@NonNull final CreatePlaceCallback createPlaceCallback) {
        PlaceModelProvider.getPrimaryPlace()
              .onFailure(Listeners.runOnUiThread(new Listener<Throwable>() {
                  @Override public void onEvent(Throwable throwable) {
                      NewPlaceSequenceController.this.onError(throwable, createPlaceCallback);
                  }
              }))
              .onSuccess(new Listener<PlaceModel>() {
                  @Override public void onEvent(PlaceModel placeModel) {
                      existingPlaceNickname = placeModel.getName();
                      existingServiceLevel = Place.SERVICELEVEL_PREMIUM;
                      createAnotherPlace(createPlaceCallback);
                  }
              });
    }

    protected void createAnotherPlace(final CreatePlaceCallback createPlaceCallback) {
        new SaveHomeTask(newPlaceNickname, newPlaceAddressEntered, new SaveHomeTask.AddAnotherPlaceCallback() {
            @Override public void onError(Throwable throwable) {
                NewPlaceSequenceController.this.onError(throwable, createPlaceCallback);
            }

            @Override public void onSuccess(String newPlaceAddress) {
                placeAddress = newPlaceAddress;
                String newPlaceID = Addresses.getId(newPlaceAddress);
                if (newPlaceBitmap != null) {
                    ImageRepository.saveImage(ArcusApplication.getContext(), newPlaceBitmap, ImageCategory.PLACE, newPlaceID, null);
                }
                NewPlaceSequenceController.this.onSuccess(createPlaceCallback);
                SessionController.instance().changeActivePlace(newPlaceID);
            }
        }).addNewPlace(existingServiceLevel);
    }

    public void getPrimaryPlaceServiceLevel(@NonNull final PrimaryPlaceServiceLevelCallback callback) {

        PlaceModelProvider.getPrimaryPlace()
                .onFailure(Listeners.runOnUiThread(new Listener<Throwable>() {
                    @Override public void onEvent(Throwable throwable) {
                        callback.onError();
                    }
                }))
                .onSuccess(Listeners.runOnUiThread(new Listener<PlaceModel>() {
                    @Override public void onEvent(PlaceModel placeModel) {
                        callback.onSuccess(placeModel.getServiceLevel());
                    }
                }));
    }

    protected void onError(Throwable throwable, CreatePlaceCallback callback) {
        if (callback == null) {
            return;
        }

        try {
            callback.onError(throwable);
        }
        catch (Exception ex) {
            logger.error(
                  "Error calling onError callback Error trying to transmit: [{}]",
                  throwable.getClass().getSimpleName(),
                  ex
            );
        }
    }

    protected void onSuccess(CreatePlaceCallback callback) {
        if (callback == null) {
            return;
        }

        try {
            callback.onSuccess();
        }
        catch (Exception ex) {
            logger.error("Error calling onSuccess callback", ex);
        }
    }
}

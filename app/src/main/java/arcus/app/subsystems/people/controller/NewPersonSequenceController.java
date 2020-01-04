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
package arcus.app.subsystems.people.controller;

import android.app.Activity;
import androidx.annotation.NonNull;

import arcus.cornea.model.InviteModel;
import arcus.app.account.settings.pin.SettingsUpdatePin;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.sequence.AbstractStaticSequenceController;
import arcus.app.common.sequence.Sequenceable;
import arcus.app.dashboard.HomeFragment;
import arcus.app.subsystems.people.PersonCongratsFragment;
import arcus.app.subsystems.people.model.DeviceContact;
import arcus.app.subsystems.people.model.PersonTypeSequence;
import arcus.app.subsystems.place.PlaceDoneGuestHelpFragment;


public class NewPersonSequenceController extends AbstractStaticSequenceController {

    private PersonTypeSequence sequenceType;
    private String personAddress;
    private String placeID;
    private DeviceContact contact;
    private InviteModel inviteModel;

    public NewPersonSequenceController(PersonTypeSequence sequenceType) {
        this.sequenceType = sequenceType;
    }

    public int getSequenceType() {
        return sequenceType.ordinal();
    }

    public void setSequenceType(PersonTypeSequence sequenceType) {
        this.sequenceType = sequenceType;
    }

    public void setDeviceContact(DeviceContact contact) {
        this.contact = contact;
    }

    public DeviceContact getDeviceContact() {
        return this.contact;
    }

    @Override
    public void startSequence(Activity activity, Sequenceable from, Object... data) {
        throw new IllegalStateException("Bug! Not implemented.");
    }

    @Override
    public void goNext(Activity activity, @NonNull Sequenceable from, Object... data) {
        if (from instanceof PersonCongratsFragment){
            BackstackManager.getInstance().navigateToFloatingFragment(PlaceDoneGuestHelpFragment.newInstance(),PlaceDoneGuestHelpFragment.class.getName(),true);
            return;
        }

        navigateToNextSequenceable(activity, sequenceType.getSequence(), from.getClass(), data);
    }

    @Override
    public void goBack(Activity activity, @NonNull Sequenceable from, Object... data) {

        //this will terminate the new person Sequence if it has been successful rather than backing out.
        if (from instanceof PersonCongratsFragment){
            BackstackManager.getInstance().navigateBackToFragment(HomeFragment.newInstance());
            return;
        }

        if (from instanceof SettingsUpdatePin) {
            return;
        }
        boolean success = navigateToPreviousSequenceable(activity, sequenceType.getSequence(), from.getClass(), data);
        if (!success) {
            endSequence(activity, success);
        }
    }

    @Override
    public void endSequence(Activity activity, boolean isSuccess, Object... data) {
        BackstackManager.getInstance().navigateBackToFragment(HomeFragment.newInstance());
    }

    @Override
    protected Sequenceable newInstanceOf(@NonNull Class<? extends Sequenceable> clazz, Object... data) {

        if (clazz == SettingsUpdatePin.class) {
            return SettingsUpdatePin.newInstance(SettingsUpdatePin.ScreenVariant.ADD_A_PERSON, personAddress, placeID);
        }

        return super.newInstanceOf(clazz);
    }

    public void setPlaceID(String placeID) { this.placeID = placeID; }

    public String getPlaceID() {
        return placeID;
    }

    public void setPersonAddress (String personAddress) {
        this.personAddress = personAddress;
    }

    public String getPersonAddress () {
        return this.personAddress;
    }

    public InviteModel getInviteModel() {
        return inviteModel;
    }

    public void setInviteModel(InviteModel inviteModel) {
        this.inviteModel = inviteModel;
    }
}

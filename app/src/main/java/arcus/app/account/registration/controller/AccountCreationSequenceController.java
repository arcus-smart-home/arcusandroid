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
package arcus.app.account.registration.controller;

import android.app.Activity;
import androidx.annotation.NonNull;

import arcus.cornea.SessionController;
import arcus.cornea.utils.Listeners;
import com.iris.client.capability.Account;
import com.iris.client.event.Listener;
import com.iris.client.model.AccountModel;
import arcus.app.account.registration.AccountAboutYouFragment;
import arcus.app.account.registration.AccountBillingInfoFragment;
import arcus.app.account.registration.AccountEmailPasswordFragment;
import arcus.app.account.registration.AccountSecurityQuestionsFragment;
import arcus.app.account.registration.model.AccountTypeSequence;
import arcus.app.account.settings.pin.SettingsUpdatePin;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.error.ErrorManager;
import arcus.app.common.sequence.CachedSequenceController;
import arcus.app.common.sequence.Sequenceable;
import arcus.app.common.sequence.SequencedFragment;
import arcus.app.dashboard.HomeFragment;
import arcus.app.subsystems.people.model.DeviceContact;
import arcus.app.subsystems.place.PlaceDoneHelpFragment;


public class AccountCreationSequenceController extends CachedSequenceController {

    private DeviceContact contact;
    private AccountTypeSequence sequence;

    public AccountCreationSequenceController(AccountTypeSequence sequence, DeviceContact contact) {
        // Don't cache pin screen; user has to renter when coming back to this screen because the UI
        // has no means of letting the user keep a previously entered pin.
        this.sequence = sequence;
        this.contact = contact;
        super.addCacheExclusion(SettingsUpdatePin.class);
    }

    public AccountTypeSequence getSequence() {
        return sequence;
    }

    @Override
    public void startSequence(Activity activity, Sequenceable from, Object... data) {
        Class<? extends SequencedFragment> firstFragment = (Class<? extends SequencedFragment>) data[0];
        navigateForward(activity, newInstanceOf(firstFragment));
    }

    @Override
    public void endSequence(Activity activity, boolean isSuccess, Object... data) {
        BackstackManager.getInstance().navigateBackToFragment(HomeFragment.newInstance());
    }

    @Override
    public void goBack(@NonNull Activity activity, Sequenceable from, Object... data) {


        // If on the email/password screen, then return back to the welcome screen
        if (from instanceof AccountEmailPasswordFragment) {
            activity.finish();
        }

        // User is on the first page after email/password--can't back into password page
        else if (from instanceof AccountAboutYouFragment) {
            return;
        }

        // Don't let user back into the credit card screen
        else if (sequence.getSequence().getPrevious(from.getClass()) == AccountBillingInfoFragment.class) {
            return;
        } else if (sequence.getSequence().getPrevious(from.getClass()) == SettingsUpdatePin.class) {
            navigateToPreviousSequenceable(activity, sequence.getSequence(), SettingsUpdatePin.class);
        } else {
            navigateToPreviousSequenceable(activity, sequence.getSequence(), from.getClass());
        }
    }

    @Override
    public void goNext(Activity activity, @NonNull Sequenceable from, Object... data) {
        if (from instanceof SettingsUpdatePin && AccountTypeSequence.CURRENT_USER_INVITE_ACCEPT.equals(sequence)) {
            endSequence(activity,true,data);
            BackstackManager.getInstance().navigateToFloatingFragment(PlaceDoneHelpFragment.newInstance(false), PlaceDoneHelpFragment.class.getName(), true);
        } else if (from instanceof SequencedFragment && ((SequencedFragment) from).validate()) {
            navigateToNextSequenceable(activity, sequence.getSequence(), from.getClass());
        }
    }


    public void skipForward(@NonNull final SequencedFragment next) {

        AccountModel accountModel = SessionController.instance().getAccount();
        if (accountModel == null) { // All nullable?
            getActiveFragment().hideProgressBar();
            ErrorManager.in(getActiveFragment().getActivity()).showGenericBecauseOf(new RuntimeException("Account model not loaded. Cannot skip."));
            return;
        }

        getActiveFragment().showProgressBar();

          accountModel.skipPremiumTrial().onSuccess(Listeners.runOnUiThread(new Listener<Account.SkipPremiumTrialResponse>() {
              @Override
              public void onEvent(Account.SkipPremiumTrialResponse skipPremiumTrialResponse) {
                  getActiveFragment().hideProgressBar();

                  //move forward.
                  BackstackManager.getInstance().navigateToFragment(next, true);
              }
          })).onFailure(Listeners.runOnUiThread(new Listener<Throwable>() {
              @Override
              public void onEvent(Throwable throwable) {
                  getActiveFragment().hideProgressBar();
                  ErrorManager.in(getActiveFragment().getActivity()).showGenericBecauseOf(throwable);
              }
          }));
    }

    @Override
    public Sequenceable newInstanceOf(@NonNull Class<? extends Sequenceable> clazz, Object... data) {

        // These classes require non-default constructors
        if (clazz == AccountSecurityQuestionsFragment.class) {
            return AccountSecurityQuestionsFragment.newInstance(AccountSecurityQuestionsFragment.ScreenVariant.ACCOUNT_CREATION);
        } else if (clazz == AccountBillingInfoFragment.class) {
            return AccountBillingInfoFragment.newInstance(AccountBillingInfoFragment.ScreenVariant.ACCOUNT_CREATION);
        } else if (clazz == SettingsUpdatePin.class) {

            if(sequence.ordinal()== AccountTypeSequence.CURRENT_USER_INVITE_ACCEPT.ordinal()) {
                return SettingsUpdatePin.newInstance(
                        SettingsUpdatePin.ScreenVariant.SETTINGS,
                        SessionController.instance().getPersonId(),
                        contact != null && contact.hasPlaceIDSet() ? contact.getPlaceID() : SessionController.instance().getPlaceIdOrEmpty()
                );
            }
            else if(sequence.ordinal()== AccountTypeSequence.INVITATION_ACCOUNT_CREATION.ordinal()) {
                return SettingsUpdatePin.newInstance(
                        SettingsUpdatePin.ScreenVariant.ACCOUNT_CREATION,
                        SessionController.instance().getPersonId(),
                        contact != null && contact.hasPlaceIDSet() ? contact.getPlaceID() : SessionController.instance().getPlaceIdOrEmpty()
                );
            }
            return SettingsUpdatePin.newInstance(
                  SettingsUpdatePin.ScreenVariant.ACCOUNT_CREATION,
                  SessionController.instance().getPersonId(),
                  contact != null && contact.hasPlaceIDSet() ? contact.getPlaceID() : SessionController.instance().getPlaceIdOrEmpty()
            );
        }

        return super.newInstanceOf(clazz, data);
    }

    public DeviceContact getDeviceContact() {
        return this.contact;
    }


}

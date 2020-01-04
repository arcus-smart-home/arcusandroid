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
package arcus.app.account.registration.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import arcus.app.account.registration.AccountAboutYourHomeFragment;
import arcus.app.account.registration.AccountBillingInfoFragment;
import arcus.app.account.registration.AccountCongratsFragment;
import arcus.app.account.registration.AccountEmailPasswordFragment;
import arcus.app.account.registration.AccountAboutYouFragment;
import arcus.app.account.registration.AccountPremiumPlanFragment;
import arcus.app.account.registration.AccountSecurityQuestionsFragment;
import arcus.app.account.settings.pin.SettingsUpdatePin;
import arcus.app.common.sequence.SequencedFragment;
import arcus.app.common.utils.StringUtils;
import arcus.app.subsystems.people.PersonInvitationCongratsFragment;
import arcus.app.subsystems.place.PlaceAccountBillingInfoFragment;
import arcus.app.subsystems.place.PlaceCongratsFragment;
import arcus.app.subsystems.place.PlaceDoneGuestHelpFragment;
import arcus.app.subsystems.place.PlaceDoneHelpFragment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;


public enum TransitionState {

    SIGN_UP("signUp1"),
    ABOUT_YOU("ABOUT_YOU","About you"),
    ABOUT_YOUR_HOME("ABOUT_YOUR_HOME","About Your Home"),
    PIN_CODE("PIN_CODE", "pin_code"),
    SECURITY_QUESTIONS("SECURITY_QUESTIONS","Security Questions"),
    NOTIFICATIONS("NOTIFICATIONS","notifications"),
    PREMIUM_PLAN("PREMIUM_PLAN","Premium Plan"),
    BILLING_INFORMATION("BILLING_INFO","Billing Information","Credit Card Information"),
    COMPLETE("COMPLETE", "Congrats");

    private final List<String> stateNames = new ArrayList<>();
    private final static Logger logger = LoggerFactory.getLogger(TransitionState.class);

    TransitionState(@NonNull String... stateNames){
        for (String thisStateName : stateNames) {
            this.stateNames.add(thisStateName.toUpperCase());
        }
    }

    public String getStateName() {
        return stateNames.get(0);
    }

    @NonNull
    public static TransitionState fromState(@NonNull String state){

        if (StringUtils.isEmpty(state)) {
            logger.error("Bug! Got empty account state; assuming state is COMPLETE.");
            return COMPLETE;
        }

        for (TransitionState thisState : TransitionState.values()) {
            if (thisState.stateNames.contains(state.toUpperCase())) {
                return thisState;
            }
        }

        logger.error("Bug! No transition state identifiable as " + state + "; assuming state is COMPLETE.");
        return TransitionState.COMPLETE;
    }

    @Nullable
    public Class<? extends SequencedFragment> getNextFragmentClass() {
        switch (this) {
            case SIGN_UP: return AccountAboutYouFragment.class;
            case ABOUT_YOU: return AccountAboutYourHomeFragment.class;
            case ABOUT_YOUR_HOME: return SettingsUpdatePin.class;
            case PIN_CODE: return AccountSecurityQuestionsFragment.class;
            case SECURITY_QUESTIONS: return AccountPremiumPlanFragment.class;
            case NOTIFICATIONS:
            case PREMIUM_PLAN: return AccountBillingInfoFragment.class;
            case BILLING_INFORMATION: return AccountCongratsFragment.class;
            case COMPLETE: return null;
            default:
                throw new IllegalArgumentException("Bug: Unhandled case for " + this);
        }
    }

    public static TransitionState fromFragment (final Fragment fragment){

        if(fragment instanceof AccountEmailPasswordFragment){
            return TransitionState.SIGN_UP;
        }else if(fragment instanceof AccountAboutYouFragment) {
            return TransitionState.ABOUT_YOU;
        }else if(fragment instanceof SettingsUpdatePin){
            return TransitionState.PIN_CODE;
        }else if(fragment instanceof AccountAboutYourHomeFragment){
            return TransitionState.ABOUT_YOUR_HOME;
        }else if(fragment instanceof AccountSecurityQuestionsFragment){
            return TransitionState.SECURITY_QUESTIONS;
        }else if(fragment instanceof AccountPremiumPlanFragment){
            return TransitionState.PREMIUM_PLAN;
        }else if(fragment instanceof AccountBillingInfoFragment){
            return TransitionState.BILLING_INFORMATION;
        }else if(fragment instanceof AccountCongratsFragment){
            return TransitionState.COMPLETE;
        }else if(fragment instanceof PersonInvitationCongratsFragment){
            return TransitionState.COMPLETE;
        }else if(fragment instanceof PlaceDoneHelpFragment){
            return TransitionState.COMPLETE;
        }else if(fragment instanceof PlaceDoneGuestHelpFragment){
            return TransitionState.COMPLETE;
        } else if(fragment instanceof PlaceCongratsFragment){
            return TransitionState.COMPLETE;
        } else if(fragment instanceof PlaceAccountBillingInfoFragment) {
            return TransitionState.COMPLETE;
        } else {
            return TransitionState.SIGN_UP;
        }
    }

    public boolean isRegistrationComplete() {
        return this == COMPLETE;
    }
}

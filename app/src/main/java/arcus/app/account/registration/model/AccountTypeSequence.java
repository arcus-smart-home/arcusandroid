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

import arcus.app.account.registration.AccountAboutYouFragment;
import arcus.app.account.registration.AccountAboutYourHomeFragment;
import arcus.app.account.registration.AccountBillingInfoFragment;
import arcus.app.account.registration.AccountCongratsFragment;
import arcus.app.account.registration.AccountEmailPasswordFragment;
import arcus.app.account.registration.AccountPremiumPlanFragment;
import arcus.app.account.registration.AccountSecurityQuestionsFragment;
import arcus.app.account.settings.pin.SettingsUpdatePin;
import arcus.app.common.sequence.SequencedFragment;
import arcus.app.common.sequence.StaticSequence;
import arcus.app.subsystems.people.PersonInvitationCongratsFragment;
import arcus.app.subsystems.place.PlaceDoneHelpFragment;

public enum AccountTypeSequence {

    ACCOUNT_CREATION(AccountEmailPasswordFragment.class,
            AccountAboutYouFragment.class,
            AccountAboutYourHomeFragment.class,
            SettingsUpdatePin.class,
            AccountSecurityQuestionsFragment.class,
            AccountPremiumPlanFragment.class,
            AccountBillingInfoFragment.class,
            AccountCongratsFragment.class),

    INVITATION_ACCOUNT_CREATION(AccountEmailPasswordFragment.class,
            AccountAboutYouFragment.class,
            AccountSecurityQuestionsFragment.class,
            SettingsUpdatePin.class,
            PersonInvitationCongratsFragment.class),

    CURRENT_USER_INVITE_ACCEPT(
          SettingsUpdatePin.class,
          PlaceDoneHelpFragment.class
    );

    @NonNull
    private final StaticSequence sequence;

    AccountTypeSequence(Class<? extends SequencedFragment>... fragments) {
        this.sequence = StaticSequence.from(fragments);
    }

    @NonNull
    public StaticSequence getSequence () { return sequence; }
}

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
package arcus.app.subsystems.people.model;

import androidx.annotation.NonNull;

import arcus.app.account.settings.pin.SettingsUpdatePin;
import arcus.app.common.sequence.SequencedFragment;
import arcus.app.common.sequence.StaticSequence;
import arcus.app.subsystems.people.PersonAddFragment;
import arcus.app.subsystems.people.PersonCongratsFragment;
import arcus.app.subsystems.people.PersonIdentityFragment;
import arcus.app.subsystems.people.PersonIdentityPartialFragment;
import arcus.app.subsystems.people.PersonInvitationPreviewFragment;
import arcus.app.subsystems.people.PersonInvitationReminderFragment;
import arcus.app.subsystems.people.PersonModeSelectionFragment;
import arcus.app.subsystems.people.PersonPINCodeExplanationFragment;
import arcus.app.subsystems.people.PersonPhotoFragment;
import arcus.app.subsystems.people.PersonTagFragment;

public enum PersonTypeSequence {

    FULL_ACCESS(PersonAddFragment.class, PersonModeSelectionFragment.class, PersonIdentityFragment.class, PersonTagFragment.class, PersonInvitationReminderFragment.class, PersonInvitationPreviewFragment.class, PersonCongratsFragment.class),
    //PARTIAL_ACCESS(PersonAddFragment.class, PersonModeSelectionFragment.class, PersonIdentityPartialFragment.class, PersonTagFragment.class, SettingsUpdatePin.class, PersonPhotoFragment.class, PersonPINCodeExplanationFragment.class, PersonCongratsFragment.class);
    PARTIAL_ACCESS(PersonAddFragment.class, PersonModeSelectionFragment.class, PersonIdentityPartialFragment.class, PersonTagFragment.class, SettingsUpdatePin.class, PersonPhotoFragment.class, PersonPINCodeExplanationFragment.class, PersonCongratsFragment.class);

    @NonNull
    private final StaticSequence sequence;

    PersonTypeSequence(Class<? extends SequencedFragment>... fragments) {
        this.sequence = StaticSequence.from(fragments);
    }

    @NonNull
    public StaticSequence getSequence () { return sequence; }
}

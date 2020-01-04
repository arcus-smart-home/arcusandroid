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
package arcus.app.subsystems.place.model;

import androidx.annotation.NonNull;

import arcus.app.account.settings.pin.SettingsUpdatePin;
import arcus.app.common.sequence.SequencedFragment;
import arcus.app.common.sequence.StaticSequence;
import arcus.app.subsystems.place.PlaceAccountBillingInfoFragment;
import arcus.app.subsystems.place.PlaceAccountPremiumPlanFragment;
import arcus.app.subsystems.place.PlaceAddFragment;
import arcus.app.subsystems.place.PlaceCongratsFragment;
import arcus.app.subsystems.place.PlaceDoneGuestHelpFragment;
import arcus.app.subsystems.place.PlaceDoneHelpFragment;
import arcus.app.subsystems.place.PlaceServicePlanFragment;


public enum PlaceTypeSequence {

    ADD_PLACE_OWNER(PlaceAddFragment.class, SettingsUpdatePin.class, PlaceServicePlanFragment.class, PlaceCongratsFragment.class, PlaceDoneHelpFragment.class),
    ADD_PLACE_GUEST(PlaceAddFragment.class, SettingsUpdatePin.class, PlaceAccountPremiumPlanFragment.class, PlaceAccountBillingInfoFragment.class, PlaceCongratsFragment.class, PlaceDoneGuestHelpFragment.class);
    @NonNull
    private final StaticSequence sequence;

    PlaceTypeSequence(Class<? extends SequencedFragment>... fragments) {
        this.sequence = StaticSequence.from(fragments);
    }

    @NonNull
    public StaticSequence getSequence () { return sequence; }
}

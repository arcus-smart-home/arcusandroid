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
package arcus.cornea.subsystem.care.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Collection;

public interface BehaviorTemplate extends Comparable<BehaviorTemplate> {
    @NonNull String getName();
    @NonNull String getDescription();
    @NonNull String getID();

    @Nullable String getParticipatingDevicesTitle();
    @Nullable String getParticipatingDevicesDescription();

    @Nullable String getInactivityTitle();
    @Nullable String getInactivityDescription();

    @Nullable String getTimeWindowTitle();
    @Nullable String getTimeWindowDescription();

    @Nullable String getLowTempTitle();
    @Nullable String getHighTempTitle();

    @Nullable String getEditNameLabel();

    @Nullable String getLowTempUnit();
    @Nullable String getHighTempUnit();
    @Nullable TimeWindowSupport getTimeWindowsUnit();
    @Nullable String getDevicesUnit();
    @NonNull  DurationType getDurationType();
    @Nullable String getDurationTypeAbstract();
    @NonNull  String[] getDurationUnitValuesArray();
    @NonNull  int[] getHighTemperatureUnitValuesArray();
    @NonNull  int[] getLowTemperatureUnitValuesArray();

    @Nullable String getLowTempValues();
    @Nullable String getHighTempValues();
    @Nullable String getTimeWindowsValues();
    @Nullable String getDevicesValues();
    @Nullable String getDurationValues();
    @Nullable String getDurationTypeValues();

    @NonNull Collection<String> getAvailableDevices();

    boolean isSatisfiable();
    boolean supportsTimeWindows();
    boolean requiresTimeWindows();
    boolean isNoDurationType();
}

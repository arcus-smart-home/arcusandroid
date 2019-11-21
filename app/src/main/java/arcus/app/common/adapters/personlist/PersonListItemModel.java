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
package arcus.app.common.adapters.personlist;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import arcus.app.dashboard.settings.model.AbstractDraggableListModel;

import java.lang.ref.WeakReference;


public class PersonListItemModel implements AbstractDraggableListModel {

    @NonNull private final String firstName;
    @Nullable private final String lastName;
    @Nullable private final String displayRelationship;
    @Nullable private final String subtext;
    @Nullable private final String personId;
    @Nullable private final Integer personIconResId;
    private boolean isChecked;
    private final boolean hasDisclosure;
    private final boolean isReorderable;
    private final boolean isEnabled;

    private WeakReference<OnPersonCheckedChangeListener> listenerRef = new WeakReference<>(null);

    public interface OnPersonCheckedChangeListener {
        void onPersonCheckedChange(boolean isChecked);
    }

    public static PersonListItemModel forEnabledPerson(String firstName, String lastName, String displayRelationship, String personId, boolean isChecked, boolean isEnabled) {
        return new PersonListItemModel(firstName, lastName, displayRelationship, null, personId, null, isChecked, false, true, isEnabled);
    }

    public static PersonListItemModel forDisabledPerson(String firstName, String lastName, String displayRelationship, String personId, boolean isChecked) {
        return new PersonListItemModel(firstName, lastName, displayRelationship, null, personId, null, isChecked, false, false, false);
    }

    public static PersonListItemModel forDummyItem(String title, String subtext, int iconResId, boolean hasDisclosure, boolean isReorderable) {
        return new PersonListItemModel(title, null, null, subtext, null, iconResId, false, hasDisclosure, isReorderable, true);
    }

    private PersonListItemModel (String firstName, String lastName, String displayRelationship, String subtext, String personId, Integer personIconResId, boolean isChecked, boolean hasDisclosure, boolean isReorderable, boolean isEnabled) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.displayRelationship = displayRelationship;
        this.personId = personId;
        this.isChecked = isChecked;
        this.subtext = subtext;
        this.personIconResId = personIconResId;
        this.hasDisclosure = hasDisclosure;
        this.isReorderable = isReorderable;
        this.isEnabled = isEnabled;
    }

    public void setOnPersonCheckedChangeListener(OnPersonCheckedChangeListener listener) {
        this.listenerRef = new WeakReference<>(listener);
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setIsChecked(boolean isChecked) {
        this.isChecked = isChecked;

        if (listenerRef.get() != null) {
            listenerRef.get().onPersonCheckedChange(isChecked);
        }
    }

    public String getDisplayName() {
        String displayName = firstName;

        if (lastName != null) {
            displayName += " " + lastName;
        }

        return displayName;
    }

    public String getDisplayRelationship() {
        return displayRelationship;
    }

    public String getPersonId() {
        return personId;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getSubtext() {
        return subtext;
    }

    public Integer getPersonIconResId() {
        return personIconResId;
    }

    public boolean hasDisclosure() {
        return hasDisclosure;
    }

    public boolean isReorderable() {
        return isReorderable;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    @Override
    public long getId() {
        return personId != null ? personId.hashCode() : personIconResId;
    }
}

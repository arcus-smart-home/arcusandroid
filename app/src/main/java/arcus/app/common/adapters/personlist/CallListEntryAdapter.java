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

import arcus.cornea.subsystem.calllist.CallListEntry;

import java.util.ArrayList;
import java.util.List;


public class CallListEntryAdapter extends AbstractPersonSelectionListAdapter {

    public void setCallListEntries(List<CallListEntry> entries){
        List<PersonListItemModel> items = new ArrayList<>();

        for (CallListEntry entry : entries) {
            items.add(toPersonListItemModel(entry));
        }

        setItems(items);
    }

    public void setPersonListEntries(List<PersonListItemModel> entries) {
        setItems(entries);
    }

    public List<CallListEntry> getCallListEntries() {
        List<CallListEntry> entries = new ArrayList<>();

        for (PersonListItemModel item : getItems()) {
            entries.add(toCallListEntry(item));
        }

        return entries;
    }

    public List<CallListEntry> getEnabledCallListEntries() {
        List<CallListEntry> entries = new ArrayList<>();

        for (CallListEntry thisEntry : getCallListEntries()) {
            if (thisEntry.isEnabled()) {
                entries.add(thisEntry);
            }
        }

        return entries;
    }

    public static CallListEntry toCallListEntry (PersonListItemModel model) {
        return new CallListEntry(model.getPersonId(), model.getFirstName(), model.getLastName(), model.getDisplayRelationship(), model.isChecked());
    }

    public static PersonListItemModel toPersonListItemModel (CallListEntry entry) {
        return PersonListItemModel.forEnabledPerson(entry.getFirstName(), entry.getLastName(), entry.getRelationship(), entry.getId(), entry.isEnabled(), true);
    }
}

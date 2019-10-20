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

import arcus.cornea.subsystem.doorsnlocks.model.AccessState;
import com.iris.client.bean.LockAuthorizationState;

import java.util.ArrayList;
import java.util.List;


public class AccessStateAdapter extends AbstractPersonSelectionListAdapter {

    public List<AccessState> getAccessStateEntries () {
        List<AccessState> accessStateEntries = new ArrayList<>();

        for (PersonListItemModel item : getItems()) {
            accessStateEntries.add(toAccessState(item));
        }

        return accessStateEntries;
    }

    public void setAccessStateEntries(List<AccessState> entries) {
        List<PersonListItemModel> items = new ArrayList<>();

        for (AccessState entry : entries) {
            items.add(toPersonListItemModel(entry));
        }

        setItems(items);
    }

    private AccessState toAccessState (PersonListItemModel thisEntry) {
        AccessState accessState = new AccessState();
        accessState.setAccessState(thisEntry.isChecked() ? LockAuthorizationState.STATE_AUTHORIZED : LockAuthorizationState.STATE_UNAUTHORIZED);
        accessState.setFirstName(thisEntry.getFirstName());
        accessState.setLastName(thisEntry.getLastName());
        accessState.setRelationship(thisEntry.getDisplayRelationship());
        accessState.setPersonId(thisEntry.getPersonId());

        return accessState;
    }

    private PersonListItemModel toPersonListItemModel (AccessState accessState) {
        boolean isChecked = LockAuthorizationState.STATE_AUTHORIZED.equals(accessState.getAccessState());
        return PersonListItemModel.forEnabledPerson(accessState.getFirstName(), accessState.getLastName(), accessState.getRelationship(), accessState.getPersonId(), isChecked, true);
    }
}

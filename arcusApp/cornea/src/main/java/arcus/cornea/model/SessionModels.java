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
package arcus.cornea.model;

import arcus.cornea.utils.ModelSource;
import com.iris.client.model.AccountModel;
import com.iris.client.model.PersonModel;
import com.iris.client.model.PlaceModel;
import com.iris.client.session.SessionInfo;

public class SessionModels {
    SessionInfo info;
    ModelSource<AccountModel> accountRef;
    ModelSource<PersonModel> personRef;
    ModelSource<PlaceModel> placeRef;

    public SessionModels(
          SessionInfo info,
          ModelSource<AccountModel> accountRef,
          ModelSource<PersonModel> personRef,
          ModelSource<PlaceModel> placeRef
    ) {
        this.info = info;
        this.accountRef = accountRef;
        this.personRef = personRef;
        this.placeRef = placeRef;
    }

    public SessionInfo getInfo() {
        return info;
    }

    public ModelSource<AccountModel> getAccountRef() {
        return accountRef;
    }

    public ModelSource<PersonModel> getPersonRef() {
        return personRef;
    }

    public ModelSource<PlaceModel> getPlaceRef() {
        return placeRef;
    }
}

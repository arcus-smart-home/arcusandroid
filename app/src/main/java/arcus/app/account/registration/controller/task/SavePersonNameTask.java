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
package arcus.app.account.registration.controller.task;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import arcus.cornea.CorneaService;
import com.iris.client.ClientEvent;
import com.iris.client.capability.Person;
import com.iris.client.event.Listener;
import com.iris.client.model.PersonModel;
import com.iris.client.util.Result;
import arcus.app.common.models.RegistrationContext;


public class SavePersonNameTask extends ArcusTask {

    public SavePersonNameTask(Context context, Fragment fragment, ArcusTaskListener listener, CorneaService corneaService, RegistrationContext registrationContext) {
        super(context, fragment, listener, corneaService, registrationContext);
    }

    @Nullable
    @Override
    protected Void doInBackground(Void... params) {

        try {
            PersonModel personModel = registrationContext.getPersonModel();
            personModel.set(Person.ATTR_FIRSTNAME, registrationContext.getFirstName());
            personModel.set(Person.ATTR_LASTNAME, registrationContext.getLastName());
            personModel.set(Person.ATTR_MOBILENUMBER, registrationContext.getMobileNumber());

            personModel.commit().onCompletion(new Listener<Result<ClientEvent>>() {
                @Override
                public void onEvent(@NonNull Result<ClientEvent> clientEventResult) {
                    logger.trace("receive set name response: {}", clientEventResult);
                    if(clientEventResult.isError()){
                        futureState.setValue(false);
                        exception = (Exception) clientEventResult.getError();
                    }else{
                        futureState.setValue(true);
                    }
                }
            });

            isResultOk = futureState.get();

        }catch (Exception e){
            exception = e;
            isResultOk = false;
        }

        return null;
    }
}

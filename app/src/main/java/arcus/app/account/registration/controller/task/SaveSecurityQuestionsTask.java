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
import android.util.Log;

import arcus.cornea.CorneaService;
import com.iris.client.capability.Person;
import com.iris.client.event.Listener;
import com.iris.client.model.PersonModel;
import com.iris.client.util.Result;
import arcus.app.common.models.RegistrationContext;


public class SaveSecurityQuestionsTask extends ArcusTask {

    public SaveSecurityQuestionsTask(Context context, Fragment fragment, ArcusTaskListener listener, CorneaService corneaService, RegistrationContext registrationContext) {
        super(context, fragment, listener, corneaService, registrationContext);
    }

    @Nullable
    @Override
    protected Void doInBackground(Void... params) {

        try {

            final PersonModel personModel = registrationContext.getPersonModel();

            Log.e("SECURITY", "Q1: " + registrationContext.getSecurityQuestionOne() + " A1:" + registrationContext.getSecurityAnswerOne());
            Log.e("SECURITY", "Q2: " + registrationContext.getSecurityQuestionTwo() + " A2:" + registrationContext.getSecurityAnswerTwo());
            Log.e("SECURITY", "Q2: " + registrationContext.getSecurityQuestionThree() + " A3:" + registrationContext.getSecurityAnswerThree());

            personModel.setSecurityAnswers(
                    registrationContext.getSecurityQuestionOne(), registrationContext.getSecurityAnswerOne(),
                    registrationContext.getSecurityQuestionTwo(), registrationContext.getSecurityAnswerTwo(),
                    registrationContext.getSecurityQuestionThree(), registrationContext.getSecurityAnswerThree())
                    .onCompletion(new Listener<Result<Person.SetSecurityAnswersResponse>>() {
                        @Override
                        public void onEvent(@NonNull Result<Person.SetSecurityAnswersResponse> setSecurityAnswersResponseResult) {
                            logger.trace("Received set security answers response: {}", setSecurityAnswersResponseResult);
                            if (setSecurityAnswersResponseResult.isError()) {
                                futureState.setValue(false);
                                exception = (Exception) setSecurityAnswersResponseResult.getError();
                            } else {
                                futureState.setValue(true);
                            }
                    }
                });

            isResultOk = futureState.get();

        }catch (Exception e){
            exception =e;
            isResultOk =false;
        }

        return null;
    }
}

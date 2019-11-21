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
import arcus.cornea.billing.BillingTokenRequest;
import com.iris.client.capability.Account;
import com.iris.client.event.Listener;
import com.iris.client.model.AccountModel;
import com.iris.client.model.PlaceModel;
import com.iris.client.util.Result;
import arcus.app.common.models.RegistrationContext;
import arcus.app.common.utils.GlobalSetting;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;


public class SaveCreditCardTask extends ArcusTask {

    private AccountModel mAccountModel;

    public SaveCreditCardTask(Context context, Fragment fragment, ArcusTaskListener listener, CorneaService corneaService, RegistrationContext registrationContext) {
        super(context, fragment, listener, corneaService, registrationContext);
    }


    @Nullable
    @Override
    protected Void doInBackground(Void... params) {

        try {

            HashMap<String, String> creditInfo = registrationContext.getCreditInfo();

            BillingTokenRequest info = new BillingTokenRequest();

            // The address requirements here are for the test gateway, not sure what will be enforced
            // on the production gateway.
            info.setFirstName(creditInfo.get(GlobalSetting.CREDIT_INFO_FIRST_NAME_KEY)); // Required!
            info.setLastName(creditInfo.get(GlobalSetting.CREDIT_INFO_LAST_NAME_KEY)); // Required!
            info.setYear(Integer.parseInt(creditInfo.get(GlobalSetting.CREDIT_INFO_YEAR_KEY))); // Required!
            info.setMonth(Integer.parseInt(creditInfo.get(GlobalSetting.CREDIT_INFO_MONTH_KEY))); // Required!
            info.setAddress1(creditInfo.get(GlobalSetting.CREDIT_INFO_ADDRESS1_KEY)); // Required!
            info.setAddress2(creditInfo.get(GlobalSetting.CREDIT_INFO_ADDRESS2_KEY));
            info.setCity(creditInfo.get(GlobalSetting.CREDIT_INFO_CITY_KEY)); // Required!
            info.setState(creditInfo.get(GlobalSetting.CREDIT_INFO_STATE_KEY)); // Required!
            info.setPostalCode(creditInfo.get(GlobalSetting.CREDIT_INFO_ZIPCODE_KEY)); // Required!
            info.setCountry("US"); // Required!
            info.setCardNumber(creditInfo.get(GlobalSetting.CREDIT_INFO_CARD_NUMBER_KEY)); // Required!
            info.setVerificationValue(creditInfo.get(GlobalSetting.CREDIT_INFO_VERIFICATION_CODE_KEY)); // Required!

            corneaService.setup().getBillingToken(info).onCompletion(new Listener<Result<String>>() {
                @Override
                public void onEvent(@NonNull Result<String> stringResult) {
                    logger.trace("Received get billing token response: {}", stringResult);
                    if (stringResult.isError()) {
                        futureState.setValue(false);
                    } else {
                        final AccountModel accountModel = registrationContext.getAccountModel();
                        final PlaceModel placeModel = registrationContext.getPlaceModel();
                        accountModel.createBillingAccount(stringResult.getValue(), placeModel.getId()).onCompletion(new Listener<Result<Account.CreateBillingAccountResponse>>() {
                            @Override
                            public void onEvent(@NonNull Result<Account.CreateBillingAccountResponse> createBillingAccountResponseResult) {
                                logger.trace("Received create billing account response: {}", createBillingAccountResponseResult);
                                if (createBillingAccountResponseResult.isError()) {
                                    futureState.setValue(false);
                                    exception = (Exception) createBillingAccountResponseResult.getError();
                                } else {
                                    futureState.setValue(true);
                                    mAccountModel = accountModel;

                                }
                            }
                        });
                    }
                }
            }).get(GlobalSetting.TIMEOUT_PER_GET_SECONDS, TimeUnit.SECONDS);

            isResultOk = futureState.get();

        }catch (Exception e){
            exception = e;
            isResultOk = false;
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);
        if (mAccountModel != null) {
            mAccountModel.refresh();
        }

    }
}

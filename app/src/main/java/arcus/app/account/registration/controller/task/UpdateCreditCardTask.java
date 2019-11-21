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
import arcus.cornea.billing.TokenClient;
import com.iris.client.capability.Account;
import com.iris.client.event.Listener;
import com.iris.client.model.AccountModel;
import com.iris.client.util.Result;
import arcus.app.common.models.RegistrationContext;
import arcus.app.common.utils.GlobalSetting;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;


public class UpdateCreditCardTask extends ArcusTask {

    private AccountModel mAccountModel;

    public UpdateCreditCardTask(Context context, Fragment fragment, ArcusTaskListener listener, CorneaService corneaService, RegistrationContext registrationContext) {
        super(context, fragment, listener, corneaService, registrationContext);
    }

    @Nullable
    @Override
    protected Void doInBackground(Void... params) {

        try {

            final HashMap<String, String> creditInfo = registrationContext.getCreditInfo();

            final BillingTokenRequest billingTokenRequest = new BillingTokenRequest();

            // The address requirements here are for the test gateway, not sure what will be enforced
            // on the production gateway.
            billingTokenRequest.setFirstName(creditInfo.get(GlobalSetting.CREDIT_INFO_FIRST_NAME_KEY)); // Required!
            billingTokenRequest.setLastName(creditInfo.get(GlobalSetting.CREDIT_INFO_LAST_NAME_KEY)); // Required!
            billingTokenRequest.setYear(Integer.parseInt(creditInfo.get(GlobalSetting.CREDIT_INFO_YEAR_KEY))); // Required!
            billingTokenRequest.setMonth(Integer.parseInt(creditInfo.get(GlobalSetting.CREDIT_INFO_MONTH_KEY))); // Required!
            billingTokenRequest.setAddress1(creditInfo.get(GlobalSetting.CREDIT_INFO_ADDRESS1_KEY)); // Required!
            billingTokenRequest.setAddress2(creditInfo.get(GlobalSetting.CREDIT_INFO_ADDRESS2_KEY)); // Required!
            billingTokenRequest.setCity(creditInfo.get(GlobalSetting.CREDIT_INFO_CITY_KEY)); // Required!
            billingTokenRequest.setState(creditInfo.get(GlobalSetting.CREDIT_INFO_STATE_KEY)); // Required!
            billingTokenRequest.setPostalCode(creditInfo.get(GlobalSetting.CREDIT_INFO_ZIPCODE_KEY)); // Required!
            billingTokenRequest.setCountry("US"); // Required!
            billingTokenRequest.setCardNumber(creditInfo.get(GlobalSetting.CREDIT_INFO_CARD_NUMBER_KEY)); // Required!
            billingTokenRequest.setVerificationValue(creditInfo.get(GlobalSetting.CREDIT_INFO_VERIFICATION_CODE_KEY)); // Required!

            corneaService.setup().getBillingToken(billingTokenRequest).onCompletion(new Listener<Result<String>>() {
                @Override
                public void onEvent(@NonNull final Result<String> stringResult) {
                    logger.trace("Received get billing token response: {}", stringResult);
                    if (stringResult.isError()) {
                        futureState.setValue(false);
                    } else {

                        final AccountModel accountModel = registrationContext.getAccountModel();
                        TokenClient tokenClient = new TokenClient();
                        tokenClient.getBillingToken(billingTokenRequest).onCompletion(new Listener<Result<String>>() {
                            @Override
                            public void onEvent(Result<String> stringResult) {

                                if (stringResult.isError()) {
                                    futureState.setValue(false);
                                } else {
                                    accountModel.updateBillingInfoCC(stringResult.getValue()).onCompletion(new Listener<Result<Account.UpdateBillingInfoCCResponse>>() {
                                        @Override
                                        public void onEvent(@NonNull Result<Account.UpdateBillingInfoCCResponse> updateBillingInfoCCResponseResult) {
                                            logger.trace("Received update billing info CC response: {}", updateBillingInfoCCResponseResult);
                                            if (updateBillingInfoCCResponseResult.isError()) {
                                                futureState.setValue(false);
                                                exception = (Exception) updateBillingInfoCCResponseResult.getError();
                                            } else {
                                                futureState.setValue(true);
                                                mAccountModel = accountModel;
                                            }
                                        }
                                    });
                                }

                            }
                        });


                    }
                }
            }).get(GlobalSetting.TIMEOUT_PER_GET_SECONDS, TimeUnit.SECONDS);

            isResultOk = futureState.get();

        } catch (Exception e) {
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

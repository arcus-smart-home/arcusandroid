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
package arcus.app.common.error.type;

import androidx.annotation.NonNull;

import com.iris.client.exception.ErrorResponseException;
import arcus.app.R;
import arcus.app.common.error.base.Error;
import arcus.app.common.error.definition.CallSupportError;
import arcus.app.common.error.definition.DisplayedError;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum BillingErrorType implements ErrorType {
    DECLINED(new DisplayedError(R.string.billing_error_title_fraud_stolen_card, R.string.billing_error_declined_card_number)),
    DECLINED_CARD_NUMBER(new DisplayedError(R.string.billing_error_title_default, R.string.billing_error_declined_card_number)),
    INVALID_CARD_NUMBER(new DisplayedError(R.string.billing_error_title_declined_card_number, R.string.billing_error_declined_card_number)),
    TAKEN_SIMULTANEOUS(new DisplayedError(R.string.billing_error_title_transaction_unsuccessful, R.string.billing_error_taken_simulataneous)),
    EXPIRED_CARD(new DisplayedError(R.string.billing_error_title_expired_card, R.string.billing_error_expired_card)),
    DECLINED_EXPIRATION_DATE(new DisplayedError(R.string.billing_error_title_declined_expiration_date, R.string.billing_error_declined_expiration_date)),
    CARD_TYPE_NOT_ACCEPTED(new DisplayedError(R.string.billing_error_title_card_type_not_accepted, R.string.billing_error_card_type_not_accepted)),
    CARD_NOT_ACTIVATED(new DisplayedError(R.string.billing_error_title_card_not_activated, R.string.billing_error_card_not_activated)),
    FRAUD_ADDRESS(new DisplayedError(R.string.billing_error_title_fraud_address, R.string.billing_error_fraud_address)),
    SECURITY_CODE(new DisplayedError(R.string.billing_error_title_declined_security_code, R.string.billing_error_declined_security_code)),
    INVALID_MERCHANT_OR_ISSUER(new DisplayedError(R.string.billing_error_title_invalid_merchant_type, R.string.billing_error_invalid_merchant_type)),
    RESTRICTED_CARD(new DisplayedError(R.string.billing_error_title_restricted_card, R.string.billing_error_restricted_card)),
    FRAUD_OTHER(new DisplayedError(R.string.billing_error_title_fraud_stolen_card, R.string.billing_error_fraud_stolen_card)),
    INVALID(new CallSupportError(R.string.billing_error_title_invalid_login, R.string.billing_error_invalid_login)),
    UNKNOWN(new CallSupportError(R.string.billing_error_title_default, R.string.billing_error_default));

    private static final Logger logger = LoggerFactory.getLogger(BillingErrorType.class);
    private Error error;

    BillingErrorType(Error error) {
        this.error = error;
    }

    @Override
    public Error getError() {
        return this.error;
    }

    public static Error fromThrowable(Throwable throwable) {
        if (throwable instanceof ErrorResponseException) {
            return fromErrorResponseException((ErrorResponseException) throwable);
        }
        else if (throwable.getCause() instanceof ErrorResponseException) {
            return fromErrorResponseException((ErrorResponseException) throwable.getCause());
        }
        else {
            logger.debug("Unhandled Exception. Returning Generic Error Message. Throwable: [{}]", throwable.getClass().getSimpleName());
            return UNKNOWN.getError();
        }
    }

    private static Error fromErrorResponseException(@NonNull ErrorResponseException response) {
        logger.error("Resolving error from ErrorResponseException code {}. Message is: {}", response.getCode(), response.getErrorMessage());

        switch (response.getCode()) {
            case Billing.DECLINED:
                return DECLINED.getError();
            case Billing.DECLINED_CARD_NUMBER:
                return DECLINED_CARD_NUMBER.getError();
            case Billing.INVALID_CARD_NUMBER:
            case Billing.INVALID_PARAMETER:
                return INVALID_CARD_NUMBER.getError();
            case Billing.TAKEN:
            case Billing.SIMULTANEOUS:
                return TAKEN_SIMULTANEOUS.getError();
            case Billing.EXPIRED_CARD:
                return EXPIRED_CARD.getError(); // 4000-0000-0000-0069
            case Billing.DECLINED_EXPIRATION_DATE:
                return DECLINED_EXPIRATION_DATE.getError();
            case Billing.CARD_TYPE_NOT_ACCEPTED:
                return CARD_TYPE_NOT_ACCEPTED.getError();
            case Billing.CARD_NOT_ACTIVATED:
                return CARD_NOT_ACTIVATED.getError();
            case Billing.FRAUD_ADDRESS:
                return FRAUD_ADDRESS.getError();
            case Billing.DECLINED_SECURITY_CODE:
            case Billing.FRAUD_SECURITY_CODE:
                return SECURITY_CODE.getError(); // 4000-0000-0000-0101

            case Billing.INVALID_MERCHANT_TYPE:
            case Billing.INVALID_ISSUER:
                return INVALID_MERCHANT_OR_ISSUER.getError();

            case Billing.RESTRICTED_CARD_CHARGEBACK:
            case Billing.RESTRICTED_CARD:
                return RESTRICTED_CARD.getError();

            case Billing.FRAUD_STOLEN_CARD:
            case Billing.FRAUD_IP_ADDRESS:
            case Billing.FRAUD_ADVANCED_VERIFICATION:
            case Billing.FRAUD_GATEWAY:
                return FRAUD_OTHER.getError();

            case Billing.INVALID_GATEWAY_CONFIGURATION:
            case Billing.INVALID_LOGIN:
            case Billing.GATEWAY_UNAVAILABLE:
            case Billing.PROCESSOR_UNAVAILABLE:
            case Billing.ISSUER_UNAVAILABLE:
            case Billing.GATEWAY_TIMEOUT:
            case Billing.GATEWAY_ERROR:
            case Billing.CONTACT_GATEWAY:
            case Billing.TRY_AGAIN:
            case Billing.SSL_ERROR:
            case Billing.NO_GATEWAY:
            case Billing.API_ERRROR:
            case Billing.UNKNOWN_ERR:
            case Billing.RECURLY_ERROR:
            case Billing.ZERO_DOLLAR_AUTH_NOT_SUPPORTED:
                return INVALID.getError();

            default:
                return UNKNOWN.getError();
        }
    }

    interface Billing {
        String DECLINED_CARD_NUMBER = "declined_card_number";
        String DECLINED = "declined";
        String INVALID_CARD_NUMBER = "invalid_card_number";
        String INVALID_PARAMETER = "invalid-parameter";
        String DECLINED_SECURITY_CODE = "declined_security_code";
        String FRAUD_SECURITY_CODE = "fraud_security_code";
        String EXPIRED_CARD = "expired_card";
        String DECLINED_EXPIRATION_DATE = "declined_expiration_date";
        String INVALID_MERCHANT_TYPE = "invalid_merchant_type";
        String INVALID_ISSUER = "invalid_issuer";
        String CARD_TYPE_NOT_ACCEPTED = "card_type_not_accepted";
        String RESTRICTED_CARD = "restricted_card";
        String RESTRICTED_CARD_CHARGEBACK = "restricted_card_chargeback";
        String CARD_NOT_ACTIVATED = "card_not_activated";
        String FRAUD_ADDRESS = "fraud_address";
        String FRAUD_STOLEN_CARD = "fraud_stolen_card";
        String FRAUD_IP_ADDRESS = "fraud_ip_address";
        String FRAUD_GATEWAY = "fraud_gateway";
        String FRAUD_ADVANCED_VERIFICATION = "fraud_advanced_verification";
        String INVALID_GATEWAY_CONFIGURATION = "invalid_gateway_configuration";
        String INVALID_LOGIN = "invalid_login";
        String GATEWAY_UNAVAILABLE = "gateway_unavailable";
        String PROCESSOR_UNAVAILABLE = "processor_unavailable";
        String ISSUER_UNAVAILABLE = "issuer_unavailable";
        String GATEWAY_TIMEOUT = "gateway_timeout";
        String GATEWAY_ERROR = "gateway_error";
        String CONTACT_GATEWAY = "contact_gateway";
        String TRY_AGAIN = "try_again";
        String SSL_ERROR = "ssl_error";
        String ZERO_DOLLAR_AUTH_NOT_SUPPORTED = "zero_dollar_auth_not_supported";
        String NO_GATEWAY = "no_gateway";
        String RECURLY_ERROR = "recurly_error";
        String UNKNOWN_ERR = "unknown";
        String API_ERRROR = "api_error";
        String TAKEN = "taken";
        String SIMULTANEOUS = "simultaneous";
    }
}

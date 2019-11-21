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
package arcus.app.common.utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.TextUtils;

import arcus.cornea.CorneaClientFactory;
import arcus.cornea.SessionController;
import com.iris.client.exception.ErrorResponseException;
import com.iris.client.service.SessionService;
import com.iris.client.session.HandoffTokenCredentials;
import com.iris.client.session.SessionInfo;
import com.iris.client.session.SessionTokenCredentials;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;

public class LoginUtils {
    private final static Logger logger = LoggerFactory.getLogger(LoginUtils.class);

    private LoginUtils() {
        throw new RuntimeException("No Instances!");
    }

    /**
     * If the given place ID is null or empty, then this method returns the last saved place ID that
     * was used to log into the app. Otherwise, it returns the provided place id.
     *
     * @param contextualPlaceId The ID of the place in context to log into. Typically provided through
     *                         a push notification or email link. When provided, this place id will be
     *                         used as the active place.
     * @return When not empty the provided contextualPlaceId, otherwise, the last saved placed id
     * used to log in with
     */
    @Nullable public static String getContextualPlaceIdOrLastUsed(String contextualPlaceId) {
        return StringUtils.isEmpty(contextualPlaceId) ? PreferenceUtils.getLastPlaceID() : contextualPlaceId;
    }

    private static void writeSessionInfo() {
        SessionInfo sessionInfo = SessionController.instance().getSessionInfo();
        String activePlace = SessionController.instance().getActivePlace();
        if (sessionInfo != null) {
            PreferenceUtils.putLoginToken(sessionInfo.getSessionToken());
        }

        if (!TextUtils.isEmpty(activePlace)) {
            PreferenceUtils.putLastPlaceID(activePlace);
        }
    }

    /**
     * Gets a Credentials object given a platform login token string; uses the platform endpoint
     * stored in {@link PreferenceUtils#getPlatformUrl()}.
     *
     * @param token The platform token string.
     * @return The Credentials object.
     */
    @NonNull
    public static SessionTokenCredentials getSessionTokenCredentials(String token) {
        SessionTokenCredentials stc = new SessionTokenCredentials();
        stc.setConnectionURL(PreferenceUtils.getPlatformUrl());
        stc.setToken(token);
        return stc;
    }

    /**
     * Gets a Credentials object given a single-use, web-to-mobile hand-off token string; uses the
     * platform endpoint stored in {@link PreferenceUtils#getPlatformUrl()}.
     *
     * @param token The handoff token string.
     * @return The Credentials object.
     */
    @NonNull
    public static HandoffTokenCredentials getHandoffTokenCredentials(String token) {
        HandoffTokenCredentials htc = new HandoffTokenCredentials();
        htc.setConnectionURL(PreferenceUtils.getPlatformUrl());
        htc.setToken(token);
        return htc;
    }


    public static void completeLogin() {
        LoginUtils.writeSessionInfo();
    }

    public static void completeLogout() {
        PreferenceUtils.removeLoginToken();
        PreferenceUtils.removeLastPlaceID();
    }

    public static void logLoginFailure(Throwable throwable, String token) {
        try {
            logger.error("An error occurred when trying to login.", throwable);

            String message;
            if (throwable instanceof ErrorResponseException) {
                ErrorResponseException ere = (ErrorResponseException) throwable;
                message = String.format("Token[%s], %s -> %s", token, ere.getCode(), ere.getErrorMessage());
            }
            else {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                throwable.printStackTrace(pw);
                message = "Token[" + token + "]," + sw.toString();
            }
            CorneaClientFactory.getService(SessionService.class).log("login", "login.token.failure", message);
            // Log
        }
        catch (Exception ex) {
            // Log
        }
    }

    /**
     * Gets the URL of the platform that we should log into. Either the value provided by
     * {@link PreferenceUtils#getPlatformUrl()} or, when a magic email address is used, the
     * URL-portion of the email address.
     *
     * @return The intended URL of the platform
     */
    public static String getPlatformUrl(String platformUrl) {
        if (isMagicEmail(platformUrl)) {
            return platformUrl.trim();
        } else {
            return PreferenceUtils.getPlatformUrl();
        }
    }

    /**
     * @return The intended login username
     */
    public static String getUsername(String email) {
        if (isMagicEmail(email)) {
            return email.trim();
        }
        else return email;
    }

    /**
     * @return True if email entry is magic; false otherwise
     */
    public static boolean isMagicEmail (String email) {
        return false;
    }
}

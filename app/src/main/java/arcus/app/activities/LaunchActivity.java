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
package arcus.app.activities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import arcus.cornea.SessionController;
import arcus.cornea.utils.Listeners;
import com.iris.client.event.ListenerRegistration;
import com.iris.client.model.AccountModel;
import com.iris.client.model.PersonModel;
import com.iris.client.model.PlaceModel;
import com.iris.client.session.Credentials;
import com.iris.client.session.HandoffTokenCredentials;
import com.iris.client.session.SessionTokenCredentials;
import arcus.app.R;
import arcus.app.account.fingerprint.FingerprintPopup;
import arcus.app.account.login.LoginFragment;
import arcus.app.common.image.ImageManager;
import arcus.app.common.utils.BiometricLoginUtils;
import arcus.app.common.utils.LoginUtils;
import arcus.app.common.utils.PreferenceUtils;
import arcus.app.common.utils.StringUtils;
import arcus.app.createaccount.AccountCreationConstantsKt;
import arcus.app.createaccount.CreateAccountActivity;

/**
 * This activity provides the splash login screen functionality. It this entrypoint to this
 * application (it starts when the user clicks the Arcus icon from their app launcher).
 *
 * Except when started using {@link #startLoginScreen(Activity)} method, this activity attempts
 * to auto-login the user with a previously-saved session token (or, upon new account creation,
 * a hand-off token). Upon successful login, the user is directed to the
 * {@link DashboardActivity}; upon failed login, the login screen is displayed.
 */
public class LaunchActivity extends BaseActivity {

    private final static int SPLASH_TIME_OUT = 500;

    public final static String TAG_FORCE_WELCOME_SCREEN = "force-welcome";
    public final static String TAG_PLACE_ID = "place-id";
    public final static String TAG_PLACE_NAME = "place-name";
    public final static String TAG_ADDRESS = "address";
    public final static String TAG_TARGET = "target";

    private AutoLoginRequestObserver loginObserver;
    private ListenerRegistration listenerReg;
    boolean useFingerprint;
    boolean loginPending;

    /**
     * Start the activity and force display of the login screen (irrespective of whether or the not
     * the user has a saved session token.)
     *
     * @param fromActivity The current activity
     */
    public static void startLoginScreen(Activity fromActivity) {
        Intent intent = new Intent(fromActivity, LaunchActivity.class);
        intent.putExtra(TAG_FORCE_WELCOME_SCREEN, true);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        fromActivity.startActivity(intent);
    }

    /**
     * Start the launch activity as though the user just launched the app (first attempting to auto-
     * login using a stored session token and display the username/password screen if that fails).
     *
     * @param fromActivity The current activity
     */
    public static void startAutoLogin(Activity fromActivity) {
        Intent intent = new Intent(fromActivity, LaunchActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        fromActivity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display the splash screen and login spinner
        setContentView(R.layout.launch_progress);

        // Configure image manager
        ImageManager.setDefaultWallpaperResId(R.drawable.background_1);
        ImageManager.setConfiguration(this, PreferenceUtils.isPicassoCacheDisabled(), PreferenceUtils.getPicassoMemoryCacheSize());
    }

    public void onResume() {
        super.onResume();

        String cachedLoginToken = PreferenceUtils.getLoginToken();
        useFingerprint = PreferenceUtils.getUsesFingerPrint();
        loginPending = SessionController.instance().isLoginPending();

        // Caller requested we go straight to login, irrespective of whether a token exists
        if (getIntent().getBooleanExtra(TAG_FORCE_WELCOME_SCREEN, false)) {
            displayLoginScreen();
        }

        // Deep link URI provided; handle it
        else if (getIntent().getData() != null) {
            Uri deepLink = getIntent().getData();

            if (isAccountCreatedDeepLink(deepLink)) {
                String handoffToken = getWebHandoffToken(deepLink);
                consumeDeepLinkFlag();

                if (!StringUtils.isEmpty(handoffToken)) {
                    logger.debug("Received an account-create single-use token; logging in with it.");
                    loginWithCredentials(LoginUtils.getHandoffTokenCredentials(handoffToken));
                } else {
                    logger.debug("Received an account-create deep link, but got a bogus token: " + handoffToken);
                }

            } else {
                logger.debug("Received a deep-link, but don't know how to dispatch it: " + deepLink);
            }
        }

        // User has cached session token; login using it
        else if (!StringUtils.isEmpty(cachedLoginToken)) {

            // Use fingerprint, but don't display the prompt if login is already in progress
            if(useFingerprint && !loginPending) {
                initializeFingerprintPrompt(cachedLoginToken
                        , R.string.fingerprint_sign_in
                        , R.string.fingerprint_instructions);
            }
            else { // If not using fingerprint, log in normally
                loginWithCredentials(LoginUtils.getSessionTokenCredentials(cachedLoginToken));
            }
        }

        // Nothing to do but display login screen
        else {
            displayLoginScreen();
        }
    }

    @Override
    protected boolean isDeepLinkIntent() {
        return isAccountCreatedDeepLink(getIntent().getData());
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    /**
     * Create the fingerprint dialog
     * @param loginToken if fingerprint is good, we pass it to loginWithCredentials
     */
    private void initializeFingerprintPrompt(final String loginToken,
                                             @Nullable int dialogTitle,
                                             @Nullable int dialogMessage) {


        // If for some reason fingerprint deleted or screen lock disabled while the app was backgrounded
        String fingerprintSettingsMissing = BiometricLoginUtils.fingerprintUnavailable(this);
        if (fingerprintSettingsMissing.length() > 0) {
            PreferenceUtils.setUseFingerPrint(false);
            logOut();
        } else {
            FingerprintPopup popup = FingerprintPopup.newInstance(getString(dialogTitle), getString(dialogMessage),
                    new FingerprintPopup.FingerprintPopupCallback() {
                        @Override
                        public void successfullyAuthenticated() {
                            // Log in normally 
                            loginWithCredentials(LoginUtils.getSessionTokenCredentials(loginToken));
                        }

                        @Override
                        public void failedAuthentication() {
                            // Logout
                            logOut();
                        }

                        @Override
                        public void onCanceled() {
                            // Logout
                            logOut();
                        }
                    });
            popup.show(getFragmentManager(), "Fingerprint Login Popup");
        }
    }

    private void displayLoginScreen() {
        setContentView(R.layout.activity_login);
        navigateToFragment(LoginFragment.Companion.newInstance(null));
    }

    private void navigateToFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction
            .replace(R.id.container, fragment, fragment.getClass().getName())
            .addToBackStack(null)
            .commitAllowingStateLoss();
    }

    /**
     * Login to the platform using the given credentials. Navigate to the dashboard upon success, or
     * display the login screen on failure.
     *
     * @param credentials Credentials to login with.
     */
    private void loginWithCredentials(final Credentials credentials) {
        // Otherwise, attempt to login
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                try { // Try to login using an existing token
                    logger.debug("Logging in with requested place {}", getLoginPlaceId());

                    loginObserver = new AutoLoginRequestObserver(credentials);
                    listenerReg = SessionController.instance().setCallback(loginObserver);
                    SessionController.instance().login(credentials, getLoginPlaceId());
                } catch (Exception e) {
                    logger.error("Can't get client from ArcusClientFactory: {}", e);
                    displayLoginScreen();
                }
            }
        }, SPLASH_TIME_OUT);
    }

    private String getLoginPlaceId() {
        return LoginUtils.getContextualPlaceIdOrLastUsed(getIntent().getStringExtra(TAG_PLACE_ID));
    }

    @Override
    public boolean isNoNetworkErrorSupressed() {
        // Do not display the no network full-screen error popup at any point in this activity
        return true;
    }


    private boolean isAccountCreatedDeepLink(Uri launchUrl) {
        return launchUrl != null &&
                !StringUtils.isEmpty(launchUrl.getQueryParameter("token")) &&
                launchUrl.getPath().contains("new-account-created");
    }

    private String getWebHandoffToken(Uri launchUrl) {
        String oneTimeToken = launchUrl == null ? null : launchUrl.getQueryParameter("token");
        if (oneTimeToken != null) {
            logger.debug("Received one-time use login token: " + oneTimeToken);
        }
        return oneTimeToken;
    }

    /**
     * An observer of auto-login results.
     */
    private class AutoLoginRequestObserver implements SessionController.LoginCallback {

        private final Credentials credentials;

        public AutoLoginRequestObserver(Credentials credentials) {
            this.credentials = credentials;
        }

        @Override
        public void loginSuccess(@Nullable PlaceModel placeModel, @Nullable PersonModel personModel, @Nullable AccountModel accountModel) {
            logger.debug("Login succeeded; transitioning to MainActivity.");

            LoginUtils.completeLogin();

            if (accountModel != null) {
                if (credentials instanceof HandoffTokenCredentials) {
                    // Came from the web - show Confetti
                    DashboardActivity.startActivityForAccountConfetti(LaunchActivity.this);
                } else if (AccountCreationConstantsKt.COMPLETE.equals(accountModel.getState())) {
                    // Normal Login since the account state == COMPLETE
                    DashboardActivity.startActivity(LaunchActivity.this);
                } else if (AccountCreationConstantsKt.SIGNUP_1.equals(accountModel.getState())) {
                    // Show the "Check your Email" screen so they can resend email if needed
                    // since they haven't started any of the web setup
                    String personAddress;
                    if (personModel != null) {
                        personAddress = personModel.getAddress();
                    } else {
                        personAddress = "";
                    }

                    startActivity(CreateAccountActivity.forEmailSentLandingPage(
                            LaunchActivity.this,
                            personAddress
                    ));
                } else {
                    // Show the "Almost Finished" Screen
                    // Completed some, but not all of the web setup
                    String personName;
                    String personEmail;
                    if (personModel != null) {
                        personName = personModel.getFirstName();
                        personEmail = personModel.getEmail();
                    } else {
                        personName = "";
                        personEmail = "";
                    }

                    startActivity(CreateAccountActivity.forAlmostFinishedLandingPage(
                            LaunchActivity.this,
                            personName,
                            personEmail
                    ));
                }
            }

            LaunchActivity.this.finish();
            Listeners.clear(listenerReg);
        }

        @Override
        public void onError(Throwable throwable) {
            logger.debug("Login failed; displaying login screen.");

            LoginUtils.logLoginFailure(throwable, getToken());
            displayLoginScreen();
            Listeners.clear(listenerReg);
        }

        /**
         * Retrieves the login token from the credentials object used to login.
         * @return The login token or null, if one does not exist.
         */
        private String getToken() {
            if (credentials instanceof SessionTokenCredentials) {
                return ((SessionTokenCredentials) credentials).getToken();
            } else if (credentials instanceof HandoffTokenCredentials) {
                return ((HandoffTokenCredentials) credentials).getToken();
            } else {
                return null;
            }
        }
    }

    private void logOut() {
        setContentView(R.layout.activity_login);
        SessionController.instance().logout();
        LoginUtils.completeLogout();

        LaunchActivity.startLoginScreen(this);
        finishAffinity();
    }
}

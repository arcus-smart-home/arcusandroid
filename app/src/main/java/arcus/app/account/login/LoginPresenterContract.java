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
package arcus.app.account.login;

import android.support.annotation.Nullable;

import arcus.cornea.common.PresentedView;
import arcus.cornea.common.Presenter;



public class LoginPresenterContract {

    public interface LoginPresenter extends Presenter<LoginView> {
        void promptForSavedCredentials();
        void login(String placeId, String username, char[] password, CharSequence platformUrl);
        void useInvitationCode();
        void forgotPassword();
    }

    public interface LoginView extends PresentedView {
        void onLoginSucceeded();
        void showPlatformUrlEntry(@Nullable String value);
        void onLoginFailed(boolean badCredentialsCause);
        void onRetrievedSharedCredential(String shareUsername, String sharedPassword);

        void onAccountAlmostFinished(String personName, String personEmail);

        void onAccountCheckEmail(String personAddress);
    }
}

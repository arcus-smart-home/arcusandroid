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
package arcus.app.launch;

import com.google.android.gms.auth.api.credentials.Credential;

import java.lang.ref.WeakReference;



public class CredentialResolutionResultHandler {

    private final static CredentialResolutionResultHandler instance = new CredentialResolutionResultHandler();

    private Credential credential;
    private WeakReference<Callback> callbackRef = new WeakReference<>(null);

    private CredentialResolutionResultHandler() {}

    public static CredentialResolutionResultHandler getInstance() {
        return instance;
    }

    /**
     * Sets the Credential resolved as part of a request to access Google Smart Lock shared
     * credentials. A "resolved credential" is the credential selected by the user when multiple
     * credentials are available for use.
     *
     * @param credential
     */
    public void setResolvedCredential(Credential credential) {
        this.credential = credential;
        if (callbackRef.get() != null) {
            callbackRef.get().onResolvedCredential(credential);
            callbackRef.clear();
        }
    }

    /**
     * Gets the last Credential object resolved by the OS. A "resolved credential" is the credential
     * selected by the user when multiple credentials are available for use.
     *
     * @return The last resolved credential.
     */
    public Credential getResolvedCredential() {
        return this.credential;
    }

    /**
     * Register an observer of changes to resolved credentials. Note that this is a one-shot
     * callback; the registered object is automatically unregistered as soon as the callback
     * fires.
     *
     * @param callback The observer of changes to resolved credentials.
     */
    public void setResolvedCredentialCallback(Callback callback) {
        this.callbackRef = new WeakReference<>(callback);
    }

    public interface Callback {
        /**
         * Fired at the completion of a request to resolve a saved credential.
         *
         * This occurs when attempting to retrieved a Google Smart Lock saved credential and the
         * user has more than one applicable saved credential. In this case, the OS prompts the
         * user to choose a credential. This method fires when the user has finished making a
         * choice.
         *
         * @param credential The credential selected by the user, or null if no credential was
         *                   selected (user denied access to their saved credentials)
         */
        void onResolvedCredential(Credential credential);
    }
}

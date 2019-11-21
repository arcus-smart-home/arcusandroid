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
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import arcus.cornea.CorneaClientFactory;
import arcus.cornea.CorneaService;
import arcus.cornea.SessionController;
import arcus.cornea.utils.Listeners;
import com.iris.client.capability.Person;
import com.iris.client.event.ListenerRegistration;
import com.iris.client.model.AccountModel;
import com.iris.client.model.PersonModel;
import com.iris.client.model.PlaceModel;
import com.iris.client.service.InvitationService;
import com.iris.client.session.UsernameAndPasswordCredentials;
import arcus.app.ArcusApplication;
import arcus.app.common.utils.LoginUtils;
import arcus.app.common.utils.PreferenceUtils;
import arcus.app.subsystems.people.model.DeviceContact;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class SaveEmailTask extends ArcusTask {
    private boolean acceptNewsAndOffer = false;
    private char[] password;
    private String email;
    private DeviceContact contact;
    private ListenerRegistration listenerRegistration;
    private final SessionController.LoginCallback loginCallback = new SessionController.LoginCallback() {
        @Override public void loginSuccess(
                @Nullable PlaceModel placeModel,
                @Nullable PersonModel personModel,
                @Nullable AccountModel accountModel
        ) {
            LoginUtils.completeLogin();
            futureState.setValue(true);
            if (acceptNewsAndOffer && personModel != null) {
                personModel.setConsentOffersPromotions(new Date());
                personModel.commit();
            }
            Listeners.clear(listenerRegistration);
        }

        @Override public void onError(Throwable throwable) {
            futureState.setValue(false);
            exception = (Exception) throwable;

            Listeners.clear(listenerRegistration);
        }
    };

    public SaveEmailTask(Context context, Fragment fragment, ArcusTaskListener listener, CorneaService corneaService, String email, char[] password, boolean isNewsAndOffer,
                         DeviceContact contact) {
        super(context, fragment, listener, corneaService, ArcusApplication.getRegistrationContext());
        this.acceptNewsAndOffer = isNewsAndOffer;
        this.password = password;
        this.email = email;
        this.contact = contact;
    }

    @Nullable
    @Override
    protected Void doInBackground(Void... params) {

        String platformURL = PreferenceUtils.getPlatformUrl();
        try {

            if (contact != null && contact.getValidationCode() != null) {
                Map<String, Object> attributes = new HashMap<>();
                attributes.put(Person.ATTR_EMAIL, email);
                CorneaClientFactory.getService(InvitationService.class).acceptInvitationCreateLogin(attributes,
                        new String(password), contact.getValidationCode(),
                        contact.getInvitationEmail()).get();
            } else {
                corneaService.setup().
                        createAccount(platformURL, email, new String(password), String.valueOf(this.acceptNewsAndOffer)).get();
            }

            UsernameAndPasswordCredentials uap = new UsernameAndPasswordCredentials();
            uap.setConnectionURL(platformURL);
            uap.setPassword(password);
            uap.setUsername(email);

            listenerRegistration = SessionController.instance().setCallback(loginCallback);
            SessionController.instance().login(uap, LoginUtils.getContextualPlaceIdOrLastUsed(null));
            isResultOk = futureState.get();
        } catch (Exception e) {
            exception = e;
            isResultOk =false;
        }
        return null;
    }

    protected Map<String, Object> getAttributesForRequest(DeviceContact contact) {
        Map<String, Object> attributes = new HashMap<>();

        attributes.put(Person.ATTR_EMAIL, contact.getInvitationEmail());
/*        attributes.put(Person.ATTR_FIRSTNAME, contact.getFirstName());
        attributes.put(Person.ATTR_LASTNAME, contact.getLastName());*/
        return attributes;
    }
}

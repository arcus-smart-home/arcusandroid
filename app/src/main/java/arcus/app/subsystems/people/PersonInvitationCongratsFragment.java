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
package arcus.app.subsystems.people;

import android.os.Bundle;
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import arcus.app.R;
import arcus.app.account.registration.AccountCreationStepFragment;
import arcus.app.common.view.Version1TextView;
import arcus.app.subsystems.people.model.DeviceContact;


public class PersonInvitationCongratsFragment extends AccountCreationStepFragment {

    @NonNull
    public static PersonInvitationCongratsFragment newInstance(){
        PersonInvitationCongratsFragment fragment = new PersonInvitationCongratsFragment();
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        Version1TextView placeTitle = (Version1TextView) view.findViewById(R.id.place_title);
        DeviceContact contact = getController().getDeviceContact();
        if(contact != null) {
            String message = String.format(getResources().getString(R.string.invitation_congrats_title),
                    contact.getInvitorFirstName(), contact.getInvitorLastName());
            placeTitle.setText(message);
        }
        return view;
    }

    @Override
    public boolean submit() {
        transitionToNextState();
        return true;
    }

    @Override
    public boolean validate() {
        // Nothing to validate
        return true;
    }

    @Override
    public String getTitle() {
        return getString(R.string.account_registration_done);
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_invitation_account_done;
    }


}

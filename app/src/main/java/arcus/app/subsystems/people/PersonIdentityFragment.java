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
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import arcus.cornea.controller.PersonController;
import com.iris.client.capability.Person;
import arcus.app.R;
import arcus.app.common.error.ErrorManager;
import arcus.app.common.error.type.OtherErrorTypes;
import arcus.app.common.sequence.SequencedFragment;
import arcus.app.common.validation.EmailValidator;
import arcus.app.common.validation.NotEmptyValidator;
import arcus.app.common.view.Version1Button;
import arcus.app.common.view.Version1EditText;
import arcus.app.subsystems.people.controller.NewPersonSequenceController;
import arcus.app.subsystems.people.model.DeviceContact;
import arcus.app.subsystems.people.model.DeviceContactData;
import arcus.app.subsystems.people.model.PersonTypeSequence;

import java.util.ArrayList;


public class PersonIdentityFragment extends SequencedFragment<NewPersonSequenceController> {

    public static String DEVICE_CONTACT = "DEVICE_CONTACT";
    protected Version1EditText firstName;
    protected Version1EditText lastName;
    protected Version1EditText emailAddr;
    protected Version1EditText confirmEmailAddr;
    protected Version1Button nextButton;
    protected DeviceContact person;

    @NonNull
    public static PersonIdentityFragment newInstance() {
        return new PersonIdentityFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        firstName = (Version1EditText) view.findViewById(R.id.first_name);
        lastName = (Version1EditText) view.findViewById(R.id.last_name);
        emailAddr = (Version1EditText) view.findViewById(R.id.email);
        confirmEmailAddr = (Version1EditText) view.findViewById(R.id.confirm_email);
        nextButton = (Version1Button) view.findViewById(R.id.next_button);

        person = getController().getDeviceContact();
        if(person != null) {
            firstName.setText(person.getFirstName());
            lastName.setText(person.getLastName());
            boolean bHomeEmail = false;
            String typeHome = getResources().getString(R.string.type_home);
            for(DeviceContactData obj : person.getEmailAddresses()) {
                if(typeHome.equals(obj.getType())){
                    emailAddr.setText(obj.getDisplay());
                    confirmEmailAddr.setText(obj.getDisplay());
                    bHomeEmail = true;
                    break;
                }
            }
            if(!bHomeEmail && person.getEmailAddresses().size() > 0) {
                emailAddr.setText(person.getEmailAddresses().get(0).getDisplay());
                confirmEmailAddr.setText(person.getEmailAddresses().get(0).getDisplay());
            }
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle(getTitle());

        PersonController.instance().startNewPerson();

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isValidInput()) {
                    getController().setDeviceContact(getContactInformation());
                    goNext();
                }
            }
        });
    }

    protected DeviceContact getContactInformation() {
        DeviceContact contact = new DeviceContact();
        contact.setFirstName(firstName.getText().toString());
        contact.setLastName(lastName.getText().toString());
        ArrayList<DeviceContactData> email = new ArrayList<DeviceContactData>();
        DeviceContactData homeEmail = new DeviceContactData(emailAddr.getText().toString(), getResources().getString(R.string.type_home));
        email.add(homeEmail);
        contact.setEmailAddresses(email);
        return contact;
    }

    @Override
    public String getTitle() {
        return getString(R.string.people_add_a_person);
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_person_identity;
    }

    protected boolean isValidInput () {
        boolean valid = true;
        if (! new NotEmptyValidator(getActivity(), firstName).isValid()) {
            valid = false;
        }
        else {
            PersonController.instance().set(Person.ATTR_FIRSTNAME, firstName.getText().toString());
        }

        if (! new NotEmptyValidator(getActivity(), lastName).isValid()) {
            valid = false;
        }
        else {
            PersonController.instance().set(Person.ATTR_LASTNAME, lastName.getText().toString());
        }

        if (!valid) {
            return false;
        }

        PersonTypeSequence creationType = PersonTypeSequence.values()[getController().getSequenceType()];
        return validateEmailFields(PersonTypeSequence.FULL_ACCESS.equals(creationType));
    }

    protected boolean validateEmailFields(boolean required) {
        PersonController.instance().set(Person.ATTR_EMAIL, null); // Reset this field
        if (emailEmpty() && confirmEmailEmpty()) { // They didn't enter any email addresses
            if (required) {
                ErrorManager.in(getActivity()).show(OtherErrorTypes.EMAIL_REQUIRED);
            }

            return !required;
        }

        if (emailsMatchAndAreValid()) {
            PersonController.instance().set(Person.ATTR_EMAIL, emailAddr.getText().toString());
            return true;
        }

        return false;
    }

    protected boolean emailsMatchAndAreValid() {
        EmailValidator emailValidator = new EmailValidator(emailAddr);
        if (!emailValidator.isValid()) {
            return false; // The validator puts text on the email field if not valid email address
        }

        if (!emailAddr.getText().toString().equals(confirmEmailAddr.getText().toString())) {
            ErrorManager.in(getActivity()).show(OtherErrorTypes.EMAIL_ADDRESSES_DO_NOT_MATCH);
            return false;
        }

        return true;
    }

    protected boolean emailEmpty() {
        return TextUtils.isEmpty(emailAddr.getText());
    }

    protected boolean confirmEmailEmpty() {
        return TextUtils.isEmpty(confirmEmailAddr.getText());
    }
}

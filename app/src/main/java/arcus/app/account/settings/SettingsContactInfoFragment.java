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
package arcus.app.account.settings;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import arcus.cornea.controller.PersonController;
import com.iris.client.capability.Person;
import com.iris.client.exception.ErrorResponseException;
import com.iris.client.model.PersonModel;
import arcus.app.R;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.error.ErrorManager;
import arcus.app.common.error.type.PersonErrorType;
import arcus.app.common.sequence.SequencedFragment;
import arcus.app.common.utils.CorneaUtils;
import arcus.app.common.validation.EmailValidator;
import arcus.app.common.validation.NotEmptyValidator;
import arcus.app.common.validation.PhoneNumberValidator;
import arcus.app.common.view.Version1Button;
import arcus.app.common.view.Version1EditText;

import java.util.List;

import static arcus.app.createaccount.AccountCreationConstantsKt.EMAIL_IN_USE_UPDATE;


public class SettingsContactInfoFragment extends SequencedFragment implements PersonController.Callback {

    private final static String PERSON_ADDRESS = "PERSON_ADDRESS";
    private final static String SCREEN_VARIANT = "SCREEN_VARIANT";

    private MenuItem mMenuItem;
    private Version1EditText firstName;
    private Version1EditText lastName;
    private Version1EditText phone;
    private Version1EditText email;
    private Version1EditText confirmEmail;
    private Version1EditText password;
    private Version1Button doneBtn;
    private boolean emailAndPhoneOptional;

    public enum ScreenVariant {
        SHOW_PASSWORD_EDIT, HIDE_PASSWORD_EDIT
    }

    @NonNull
    public static SettingsContactInfoFragment newInstance(String personAddress, ScreenVariant variant) {
        SettingsContactInfoFragment instance = new SettingsContactInfoFragment();
        Bundle arguments = new Bundle();

        arguments.putString(PERSON_ADDRESS, personAddress);
        arguments.putSerializable(SCREEN_VARIANT, variant);
        instance.setArguments(arguments);

        return instance;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        setHasOptionsMenu(true);

        firstName = view.findViewById(R.id.fragment_contact_firstName);
        lastName = view.findViewById(R.id.fragment_contact_lastName);
        phone = view.findViewById(R.id.fragment_contact_phone_number);
        email  = view.findViewById(R.id.fragment_contact_email);
        confirmEmail = view.findViewById(R.id.fragment_contact_confirm_email);
        doneBtn = view.findViewById(R.id.fragment_contact_done_btn);
        doneBtn.setVisibility(View.GONE);
        password = view.findViewById(R.id.fragment_contact_password_star);
        RelativeLayout passwordRegion = view.findViewById(R.id.change_password_region);

        if (getArguments() != null){
            String personAddress = getArguments().getString(PERSON_ADDRESS);
            if (getArguments().getSerializable(SCREEN_VARIANT) == ScreenVariant.HIDE_PASSWORD_EDIT) {
                passwordRegion.setVisibility(View.GONE);
            }
            if(personAddress != null) {
                PersonController.instance().edit(personAddress, this);
            }
        }
        enableInput(false);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle(getTitle());

        password.setOnClickListener(v ->
                BackstackManager.getInstance().navigateToFragment(SettingsUpdatePassword.newInstance(), true)
        );
    }

    @Override
    public Integer getMenuId() {
        return R.menu.menu_edit;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        mMenuItem = item;
        if (item.getItemId() == R.id.menu_edit_contact) {
            boolean isEditing = String.valueOf(item.getTitle()).equals("EDIT");
            boolean legit = validate();
            item.setTitle(isEditing && legit ? "DONE" : "EDIT");
            if (!isEditing) {
                if(saveData()) {
                    item.setTitle("EDIT");
                    enableInput(false);
                }
                else {
                    item.setTitle("DONE");
                    enableInput(true);
                }
            }
            else {
                item.setTitle("DONE");
                enableInput(true);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void enableInput(boolean isEditing) {
        firstName.setEnabled(isEditing);
        lastName.setEnabled(isEditing);
        phone.setEnabled(isEditing);
        email.setEnabled(isEditing);
        confirmEmail.setVisibility(!isEditing ? View.GONE : View.VISIBLE);
    }

    protected boolean saveData() {
        boolean legit = validate();
        if (legit) {
            if (emailAndPhoneOptional && phone.getText().length() == 0) {
                ErrorManager.in(getActivity()).show(PersonErrorType.CANT_NOTIFY_HOBBIT_WITHOUT_PHONE);
            }

            submit();
        }
        return legit;
    }

    @Override
    public String getTitle() {
        return getString(R.string.contact_info_title);
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_contact_info;
    }

    @Override
    public boolean validate() {

        boolean dataIsValid = true;

        if (!new NotEmptyValidator(getActivity(), firstName, R.string.account_registration_first_name_blank_error).isValid()) {
            dataIsValid = false;
        }

        if (!new NotEmptyValidator(getActivity(), lastName, R.string.account_registration_last_name_blank_error).isValid()) {
            dataIsValid = false;
        }

        if (!emailAndPhoneOptional && !new PhoneNumberValidator(getActivity(), phone).isValid() || (emailAndPhoneOptional && phone.getText().length() > 0 && !new PhoneNumberValidator(getActivity(), phone).isValid())) {
            dataIsValid = false;
        }

        if((!emailAndPhoneOptional || (emailAndPhoneOptional && !email.getText().toString().equals("")))  && !new EmailValidator(email).isValid()) {
            dataIsValid = false;
        } else if (!email.getText().toString().equals(confirmEmail.getText().toString())) {
            confirmEmail.setError(getResources().getString(R.string.email_err_not_equal));
            dataIsValid = false;
        }

        return dataIsValid;
    }

    @Override
    public boolean submit() {
        PersonController.instance().set(Person.ATTR_FIRSTNAME, firstName.getText().toString());
        PersonController.instance().set(Person.ATTR_LASTNAME, lastName.getText().toString());
        PersonController.instance().set(Person.ATTR_MOBILENUMBER, phone.getText().toString());
        PersonController.instance().set(Person.ATTR_EMAIL, email.getText().toString());

        PersonController.instance().updatePerson();
        return true;
    }

    @Override
    public void showLoading() {
        showProgressBar();
    }

    @Override
    public void updateView(PersonModel personModel) {
        hideProgressBar();
        updateViews(personModel);
    }

    @Override
    public void onModelLoaded(@NonNull PersonModel personModel) {
        hideProgressBar();
        updateViews(personModel);

        password.useLightColorScheme(true).useUppercaseLabels();

        doneBtn.setTextSize(14);
        doneBtn.setTextColor(Color.BLACK);
        doneBtn.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.button_rounded_white));

        doneBtn.setOnClickListener(v ->
                saveData()
        );
    }

    @Override
    public void onModelsLoaded(@NonNull List<PersonModel> personList) {
        // Nothing to do
    }

    @Override
    public void onError(Throwable throwable) {
        hideProgressBar();
        if(throwable instanceof ErrorResponseException) {
            switch (((ErrorResponseException) throwable).getCode()){
                case EMAIL_IN_USE_UPDATE:
                    showEmailInUseDialog();

                    // Go to Edit mode and update the Menu label
                    enableInput(true);
                    onOptionsItemSelected(mMenuItem);
            }
        } else {
            ErrorManager.in(getActivity()).showGenericBecauseOf(throwable);
        }
    }

    @Override
    public void createdModelNotFound() {
        // Nothing to do
    }

    private void showEmailInUseDialog() {
        if(getActivity() != null) {
            final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
            dialogBuilder.setCancelable(false);

            dialogBuilder.setMessage(getString(R.string.email_not_available_text));
            dialogBuilder.setTitle(getString(R.string.email_already_registered_title));

            dialogBuilder.setNegativeButton(
                getString(R.string.ok),
                (dialog, which) ->
                    dialog.dismiss()
            );
            dialogBuilder.create().show();
        }
    }


    private void updateViews(PersonModel personModel) {
        firstName.useLightColorScheme(true).useUppercaseLabels();
        firstName.setText(personModel.getFirstName());

        lastName.useLightColorScheme(true).useUppercaseLabels();
        lastName.setText(personModel.getLastName());

        phone.useLightColorScheme(true).useUppercaseLabels();
        phone.setText(personModel.getMobileNumber());
        phone.setFilters(new InputFilter[]{new InputFilter.LengthFilter(14)});
        phone.addTextChangedListener(new PhoneNumberFormattingTextWatcher());

        email.useLightColorScheme(true).useUppercaseLabels();
        email.setText(personModel.getEmail());
        confirmEmail.useLightColorScheme(true).useUppercaseLabels();
        confirmEmail.setText(personModel.getEmail());

        // Hobbits don't require email or phone
        emailAndPhoneOptional = CorneaUtils.isHobbit(personModel);
    }



}

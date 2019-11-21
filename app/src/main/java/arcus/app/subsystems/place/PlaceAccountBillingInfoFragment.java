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
package arcus.app.subsystems.place;

import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.iris.client.model.AccountModel;
import arcus.app.R;
import arcus.app.account.registration.controller.task.ArcusTask;
import arcus.app.account.registration.controller.task.SaveCreditCardTask;
import arcus.app.common.adapters.SpinnerAdapter;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.error.ErrorDuring;
import arcus.app.common.error.ErrorManager;
import arcus.app.common.utils.GlobalSetting;
import arcus.app.common.validation.CreditCardTextFormatter;
import arcus.app.common.view.Version1Button;
import arcus.app.common.view.Version1EditText;
import arcus.app.common.view.Version1TextView;
import arcus.app.subsystems.alarm.SkipFloatingFragment;
import arcus.app.subsystems.scenes.editor.controller.AccountBillingInfoFragmentController;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;


public class PlaceAccountBillingInfoFragment extends PlaceCreationStepFragment
        implements AccountBillingInfoFragmentController.Callbacks, ArcusTask.ArcusTaskListener {

    private Spinner state;

    private Version1EditText zipcode;
    private Version1EditText street1;
    private Version1EditText street2;
    private Version1EditText city;
    private Version1EditText firstName;
    private Version1EditText lastName;
    private Version1EditText cardNumber;
    private Version1EditText cvc;
    private Spinner expMonth;
    private Spinner expYear;
    private SpinnerAdapter adapter;
    private boolean skipEnabled = true;
    private Version1Button btnNext;

    @Override
    public void onComplete(boolean result) {
        logger.debug("Successfully submit credit card info:{}", result);
        goNext();
    }

    @Override
    public void onError(Exception e) {
        logger.error("Got exception when sending credit card info: {}", e);

        btnNext.setEnabled(true);
        ErrorManager.in(getActivity()).got(e).during(ErrorDuring.ENTER_BILLING_INFO);
    }

    @NonNull
    public static PlaceAccountBillingInfoFragment newInstance() {
        return new PlaceAccountBillingInfoFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = super.onCreateView(inflater, container, savedInstanceState);

        Version1TextView expiration = (Version1TextView) view.findViewById(R.id.expiration_text);

        firstName = (Version1EditText) view.findViewById(R.id.etCardFirstName);
        lastName = (Version1EditText) view.findViewById(R.id.etCardLastName);
        cardNumber = (Version1EditText) view.findViewById(R.id.etCardNumber);
        cvc = (Version1EditText) view.findViewById(R.id.etCvc);

        expMonth = (Spinner) view.findViewById(R.id.spMonthExp);
        expYear = (Spinner) view.findViewById(R.id.spExpYear);
        setupMonthSpinner();
        setUpYearSpinner();

        street1 = (Version1EditText) view.findViewById(R.id.fragment_account_billing_street1);
        street2 = (Version1EditText) view.findViewById(R.id.fragment_account_billing_street2);
        city = (Version1EditText) view.findViewById(R.id.fragment_account_billing_city);
        zipcode = (Version1EditText) view.findViewById(R.id.fragment_account_billing_zipcode);
        state = (Spinner) view.findViewById(R.id.fragment_account_billing_state);
        setUpStateSpinner();

        cardNumber.addTextChangedListener(new CreditCardTextFormatter());

        firstName.useLightColorScheme(false).useUppercaseLabels();
        lastName.useLightColorScheme(false).useUppercaseLabels();
        cardNumber.useLightColorScheme(false).useUppercaseLabels();
        street1.useLightColorScheme(false).useUppercaseLabels();
        street2.useLightColorScheme(false).useUppercaseLabels();
        city.useLightColorScheme(false).useUppercaseLabels();
        zipcode.useLightColorScheme(false).useUppercaseLabels();
        cvc.useLightColorScheme(false).useUppercaseLabels();

        expiration.setTextColor(Color.BLACK);

        AccountBillingInfoFragmentController controller = new AccountBillingInfoFragmentController();
        controller.loadAccountModel(this);

        btnNext = (Version1Button) view.findViewById(R.id.fragment_account_parent_continue_btn);
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validate()) {
                    submit();
                }
            }
        });
        return view;
    }

    @Override
    public String getTitle() {
        return getString(R.string.account_registration_credit_card);
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_account_credit;
    }

    private void setupMonthSpinner() {

        adapter = new SpinnerAdapter(
                getActivity(),
                R.layout.spinner_item_state_closed,
                getResources().getStringArray(R.array.account_registration_exp_month_list),
                false);

        expMonth.setAdapter(adapter);
    }

    private void setUpYearSpinner() {
        adapter = new SpinnerAdapter(
                getActivity(),
                R.layout.spinner_item_state_closed,
                getResources().getStringArray(R.array.account_registration_exp_year_list),
                false);


        expYear.setAdapter(adapter);
    }

    private void setUpStateSpinner() {
        adapter = new SpinnerAdapter(
                getActivity(),
                R.layout.spinner_item_state_closed,
                getResources().getStringArray(R.array.states),
                false);

        state.setAdapter(adapter);
    }

    @Override
    public boolean validate() {

        boolean dataIsValid = true;

        TextView errorExpMonth;
        TextView errorExpYear;
        TextView errorState;

        if (firstName.getText() != null && StringUtils.isBlank(firstName.getText().toString())) {

            firstName.setError(getString(R.string.account_registration_first_name_blank_error));
            dataIsValid = false;
        }

        if (lastName.getText() != null && StringUtils.isBlank(lastName.getText().toString())) {

            lastName.setError(getString(R.string.account_registration_last_name_blank_error));
            dataIsValid = false;
        }

        if (cardNumber.getText() != null && StringUtils.isBlank(cardNumber.getText().toString())) {

            cardNumber.setError(getString(R.string.account_registration_card_number_blank_error));
            dataIsValid = false;
        }

        if (cvc.getText() != null && StringUtils.isBlank(cvc.getText().toString())) {

            cvc.setError(getString(R.string.account_registration_cvv_blank_error));
            dataIsValid = false;
        }

        if (expMonth.getSelectedItemPosition() == 0) {

            errorExpMonth = (TextView) expMonth.getSelectedView();
            errorExpMonth.setError("");
            errorExpMonth.setTextColor(Color.RED);
            dataIsValid = false;
        }

        if (expYear.getSelectedItemPosition() == 0) {

            errorExpYear = (TextView) expYear.getSelectedView();
            errorExpYear.setError("");
            errorExpYear.setTextColor(Color.RED);
            dataIsValid = false;
        }

        if (state.getSelectedItemPosition() == 0) {

            errorState = (TextView) state.getSelectedView();
            errorState.setError(getString(R.string.account_registration_state_blank_error));
            errorState.setTextColor(Color.RED);
            dataIsValid = false;
        }

        if (street1.getText() != null && StringUtils.isBlank(street1.getText().toString())) {

            street1.setError(getString(R.string.account_registration_street_blank_error));
            dataIsValid = false;
        }

        if (city.getText() != null && StringUtils.isBlank(city.getText().toString())) {

            city.setError(getString(R.string.account_registration_city_blank_error));
            dataIsValid = false;
        }

        if (zipcode.getText() != null && StringUtils.isBlank(zipcode.getText().toString())) {

            zipcode.setError(getString(R.string.account_registration_zipcode_blank_error));
            dataIsValid = false;
        }

        return dataIsValid;
    }

    @Override
    public boolean submit() {
        HashMap<String, String> creditInfo = new HashMap<>();
        creditInfo.put(GlobalSetting.CREDIT_INFO_FIRST_NAME_KEY, firstName.getText().toString());
        creditInfo.put(GlobalSetting.CREDIT_INFO_LAST_NAME_KEY, lastName.getText().toString());
        creditInfo.put(GlobalSetting.CREDIT_INFO_YEAR_KEY, expYear.getSelectedItem().toString());
        creditInfo.put(GlobalSetting.CREDIT_INFO_MONTH_KEY, expMonth.getSelectedItem().toString());
        creditInfo.put(GlobalSetting.CREDIT_INFO_ADDRESS1_KEY, street1.getText().toString());
        creditInfo.put(GlobalSetting.CREDIT_INFO_ADDRESS2_KEY, street2.getText().toString());
        creditInfo.put(GlobalSetting.CREDIT_INFO_CITY_KEY, city.getText().toString());
        creditInfo.put(GlobalSetting.CREDIT_INFO_STATE_KEY, state.getSelectedItem().toString());
        creditInfo.put(GlobalSetting.CREDIT_INFO_ZIPCODE_KEY, zipcode.getText().toString());
        creditInfo.put(GlobalSetting.CREDIT_INFO_COUNTRY_KEY, "USA");
        creditInfo.put(GlobalSetting.CREDIT_INFO_CARD_NUMBER_KEY, cardNumber.getText().toString());
        creditInfo.put(GlobalSetting.CREDIT_INFO_VERIFICATION_CODE_KEY, cvc.getText().toString());
        registrationContext.setCreditInfo(creditInfo);

        new SaveCreditCardTask(getActivity(), this, this, getCorneaService(), registrationContext).execute();

        return true;
    }

    private void setSpinnerByValue(Spinner spinner, String value){
        ArrayAdapter<CharSequence> arrayAdapter = (ArrayAdapter<CharSequence>) spinner.getAdapter();
        int spinnerPosition = arrayAdapter.getPosition(value);
        spinner.setSelection(spinnerPosition);

    }


    //callback methods for AccountBillingInfoFragmentController
    @Override
    public void onAccountModelLoaded(AccountModel model) {

        firstName.setText(model.getBillingFirstName());
        lastName.setText(model.getBillingLastName());
        street1.setText(model.getBillingStreet1());
        street2.setText(model.getBillingStreet2());
        city.setText(model.getBillingCity());
        zipcode.setText(model.getBillingZip());
        setSpinnerByValue(state, model.getBillingState());

    }

    @Override
    public void onCorneaError(Throwable cause) {
        ErrorManager.in(getActivity()).showGenericBecauseOf(cause);
    }

    public void setSkipEnabled(boolean skip) {
        this.skipEnabled = skip;
        getActivity().invalidateOptionsMenu();
    }

    private void showSkipDialog() {
        setSkipEnabled(false);
        final SkipFloatingFragment skipCreditCard = SkipFloatingFragment.newInstance(
              getActivity().getString(R.string.skip_cc_no),
              getActivity().getString(R.string.skip_cc_yes),
                new SkipFloatingFragment.AlertButtonCallback() {
                    @Override public boolean topAlertButtonClicked() {
                        setSkipEnabled(true);
                        return true;
                    }

                    @Override public boolean bottomAlertButtonClicked() {
                        BackstackManager.getInstance().navigateBack();
                        transitionToNextState();
                        return false;
                    }

                    @Override public void dialogClosed() {
                        setSkipEnabled(true);
                    }
                }
        );

        BackstackManager.getInstance().navigateToFloatingFragment(skipCreditCard, skipCreditCard.getClass().getSimpleName(), true);
    }

    public void handleBackPress() {
        if (!skipEnabled) {
            // Hide the skip dialog.
            // Is called from the MainActivity class overriding the default sequenced fragment behavior of normal goBack() call.
            setSkipEnabled(true);
            BackstackManager.getInstance().navigateBack();
        }
        else {
            goBack();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle(getTitle());
        getActivity().invalidateOptionsMenu();

    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (skipEnabled)
           menu.getItem(0).setEnabled(true);
        else
            menu.getItem(0).setEnabled(false);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_credit_card, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.skip_menu_item:

                showSkipDialog();
                return true;
            default:
                return true;
        }


    }
}

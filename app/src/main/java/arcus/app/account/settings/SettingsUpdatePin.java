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

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.common.base.Strings;
import arcus.cornea.controller.PersonController;
import com.iris.client.exception.ErrorResponseException;
import com.iris.client.model.PersonModel;
import arcus.app.R;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.error.ErrorManager;
import arcus.app.common.error.type.OtherErrorTypes;
import arcus.app.common.popups.YesNoPopupColored;
import arcus.app.common.sequence.SequencedFragment;
import arcus.app.subsystems.alarm.AlertFloatingFragment;
import arcus.app.subsystems.people.controller.NewPersonSequenceController;
import arcus.app.subsystems.people.model.PersonTypeSequence;

import java.util.ArrayList;
import java.util.List;

public class SettingsUpdatePin extends SequencedFragment implements View.OnClickListener, PersonController.Callback, YesNoPopupColored.Callback {

    private final static String SCREEN_VARIANT = "SCREEN_VARIANT";
    private final static String PERSON_ID = "PERSON_ID";
    private final static String PLACE_ID = "PLACE_ID";

    private int workingOn = 0;
    private boolean makingRequest = false;
    @NonNull
    private String[] pinNumbers = new String[2];

    private final int[] circles = {
        R.id.pin_number_1_circle,
        R.id.pin_number_2_circle,
        R.id.pin_number_3_circle,
        R.id.pin_number_4_circle
    };

    private final int[] numbers = {
        R.id.pin_number_pad_0,
        R.id.pin_number_pad_1,
        R.id.pin_number_pad_2,
        R.id.pin_number_pad_3,
        R.id.pin_number_pad_4,
        R.id.pin_number_pad_5,
        R.id.pin_number_pad_6,
        R.id.pin_number_pad_7,
        R.id.pin_number_pad_8,
        R.id.pin_number_pad_9
    };

    @NonNull
    private List<ImageView> circleViews = new ArrayList<>();
    private TextView stepInstructions;
    private Drawable filledCircle;
    private Drawable hollowCircle;

    @NonNull
    private ScreenVariant variant = ScreenVariant.SETTINGS;
    @Nullable
    private String personId;
    private String placeID;

    SettingsUpdatePin fragment;

    public enum ScreenVariant {
        SETTINGS,
        ADD_A_PERSON,
        ACCOUNT_CREATION,
        ADD_A_PLACE
    }

    @NonNull public static SettingsUpdatePin newInstance(
        ScreenVariant variant,
        String personId,
        @Nullable String placeID
    ) {
        SettingsUpdatePin settingsUpdatePin = new SettingsUpdatePin();

        Bundle arguments = new Bundle(3);
        arguments.putSerializable(SCREEN_VARIANT, variant);
        arguments.putString(PERSON_ID, personId);
        arguments.putString(PLACE_ID, placeID);
        settingsUpdatePin.setArguments(arguments);

        return settingsUpdatePin;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if(bundle!=null){
            variant = (ScreenVariant) bundle.getSerializable(SCREEN_VARIANT);
            personId = bundle.getString(PERSON_ID);
            placeID  = bundle.getString(PLACE_ID);
        }

        if (variant != ScreenVariant.ADD_A_PLACE && personId == null) {
            throw new IllegalStateException("Fragment cannot be created without a person id.");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        //hide the softkeyboard. In the rare event of exception, simply log the exception and continue.
        try {
            getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        } catch (Exception e) {
            // Log
        }

        int openCircleResId = isLightColorScheme() ? R.drawable.sidemenu_settings_whitecircle : R.drawable.sidemenu_settings_blackcircle;
        int closedCircleResId = isLightColorScheme() ? R.drawable.sidemenu_settings_whitecircle_filled : R.drawable.sidemenu_settings_blackcircle_filled;
        int deleteResId = isLightColorScheme() ? R.drawable.sidemenu_settings_delete : R.drawable.sidemenu_settings_delete_black;

        for (int i = 0; i < numbers.length; i++) {
            TextView current = (TextView) view.findViewById(numbers[i]);
            current.setText(String.format("%s", i));
            current.setOnClickListener(this);
        }

        stepInstructions = (TextView) view.findViewById(R.id.pin_pad_header_text);
        stepInstructions.setText(getTitleString());
        stepInstructions.setTextColor(isLightColorScheme() ? Color.WHITE : Color.BLACK);

        ImageView deleteButton = (ImageView) view.findViewById(R.id.pin_number_pad_backspace);
        deleteButton.setOnClickListener(this);
        deleteButton.setImageResource(deleteResId);

        for (int thisNumberResId : numbers) {
            TextView thisNumber = (TextView) view.findViewById(thisNumberResId);
            thisNumber.setBackgroundResource(openCircleResId);
            thisNumber.setTextColor(isLightColorScheme() ? Color.WHITE : Color.BLACK);
        }

        for (int i = 0; i < circles.length; i++) {
            ImageView thisCircle = (ImageView) view.findViewById(circles[i]);
            thisCircle.setImageResource(openCircleResId);
            circleViews.add(thisCircle);
        }

        filledCircle = ContextCompat.getDrawable(getActivity(), closedCircleResId);
        hollowCircle = ContextCompat.getDrawable(getActivity(), openCircleResId);

        PersonController.instance().edit(personId, this);

        return view;
    }

    public void onClick(@NonNull View view) {
        switch (view.getId()) {
            case R.id.pin_number_pad_0:
            case R.id.pin_number_pad_1:
            case R.id.pin_number_pad_2:
            case R.id.pin_number_pad_3:
            case R.id.pin_number_pad_4:
            case R.id.pin_number_pad_5:
            case R.id.pin_number_pad_6:
            case R.id.pin_number_pad_7:
            case R.id.pin_number_pad_8:
            case R.id.pin_number_pad_9:
                String pinNumber = ((TextView)view).getText().toString();
                appendChar(pinNumber);
                break;
            case R.id.pin_number_pad_backspace:
                removeLastChar();
                break;
        }
    }

    private void appendChar(String character) {
        if (makingRequest) {
            return;
        }

        if (Strings.isNullOrEmpty(pinNumbers[workingOn])) {
            pinNumbers[workingOn] = character;
            circleViews.get(0).setImageDrawable(filledCircle);
        }
        else if (pinNumbers[workingOn].length() != 4) {
            pinNumbers[workingOn] += character;
            circleViews.get(pinNumbers[workingOn].length() - 1).setImageDrawable(filledCircle);
        }

        if (pinNumbers[workingOn].length() == 4) {
            if (workingOn == 1) {
                processUpdate();
            }
            else {
                pinNumbers[++workingOn] = "";
                stepInstructions.setText(getTitleString());
                hollowDots();
            }
        }
    }

    private void removeLastChar() {
        if (!Strings.isNullOrEmpty(pinNumbers[workingOn])) {
            int length = pinNumbers[workingOn].length();

            pinNumbers[workingOn] = pinNumbers[workingOn].substring(0, length - 1);
            circleViews.get(length - 1).setImageDrawable(hollowCircle);
        }
    }

    private void hollowDots() {
        for (int i = 0; i < circleViews.size(); i++) {
            circleViews.get(i).setImageDrawable(hollowCircle);
        }
    }

    public void processUpdate() {
        if (!pinNumbers[0].equals(pinNumbers[1])) {
            ErrorManager.in(getActivity()).show(OtherErrorTypes.PIN_NUMBER_MISMATCH);
            resetForm();
            return;
        }

        makingRequest = true;
        PersonController.instance().updatePin(pinNumbers[workingOn], placeID);
    }

    private void resetForm() {
        hollowDots();
        workingOn = 0;
        pinNumbers = new String[2];
        stepInstructions.setText(getTitleString());
    }

    private boolean isLightColorScheme () {
        return variant == ScreenVariant.SETTINGS;
    }

    private String getTitleString () {

        int settingsStrings[] = new int[] {
                R.string.place_creation_pin_code_title,
                R.string.people_confirm_pin
        };

        int addAPersonStrings[] = new int[] {
                R.string.people_create_pin,
                R.string.people_confirm_pin
        };

        int accountCreationStrings[] = new int[] {
                R.string.Account_registration_enter_pin_code_message,
                R.string.Account_registration_confirm_pin_code_message
        };

        int addAPlaceCreationStrings[] = new int[] {
                R.string.place_creation_pin_code_title,
                R.string.people_confirm_pin
        };


        switch (variant) {
            case SETTINGS: return getString(settingsStrings[workingOn]);
            case ADD_A_PERSON: return getString(addAPersonStrings[workingOn]);
            case ADD_A_PLACE: return getString(addAPlaceCreationStrings[workingOn]);
            case ACCOUNT_CREATION:
            default:
                return getString(accountCreationStrings[workingOn]);
        }
    }

    public void onResume() {
        super.onResume();
        fragment = this;
        getActivity().setTitle(getTitle().toUpperCase());
    }

    @Override
    public String getTitle() {
        switch (variant) {
            case ACCOUNT_CREATION: // Adding a new pin
            case ADD_A_PERSON:
            case ADD_A_PLACE: return getString(R.string.people_pin_code);

            case SETTINGS: return getString(R.string.change_pincode); // Editing Existing Pin Code
            default:
                return getString(R.string.change_pincode);
        }
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_account_pin_number;
    }

    @Override
    public void showLoading() {
        showProgressBar();
    }

    @Override
    public void updateView(PersonModel personModel) {
        this.makingRequest = false;

        hideProgressBar();
        goNext();
    }

    @Override
    public void onModelLoaded(PersonModel personModel) {
        // Nothing to do
    }

    @Override
    public void onModelsLoaded(@NonNull List<PersonModel> personList) {
        // Nothing to do
    }

    @Override
    public void onError(Throwable throwable) {
        this.makingRequest = false;
        resetForm();
        hideProgressBar();

        ErrorResponseException errorResponseException = (ErrorResponseException) throwable;
        if (errorResponseException.getCode().equals("pin.notUniqueAtPlace")) {
            final AlertFloatingFragment floatingFragment =
                    AlertFloatingFragment.newInstance(
                            getActivity().getString(R.string.pin_code_not_unique_title),
                            getActivity().getString(R.string.pin_code_not_unique_text),
                            null, null, null, null,
                            new AlertFloatingFragment.AlertDismissedCallback() {
                                @Override
                                public void dismissed() { }
                            });
            BackstackManager.getInstance()
                            .navigateToFloatingFragment (floatingFragment, floatingFragment.getTag(), true);
        } else {
            ErrorManager.in(getActivity()).showGenericBecauseOf(throwable);
        }
    }

    @Override
    public void createdModelNotFound() {
        // Nothing to do
    }

    @Override
    public Integer getMenuId() {
        if(ScreenVariant.ADD_A_PERSON == variant) {
            PersonTypeSequence creationType = PersonTypeSequence.values()[((NewPersonSequenceController)getController()).getSequenceType()];
            if(PersonTypeSequence.PARTIAL_ACCESS.equals(creationType)) {
                return R.menu.menu_skip;
            }
        }
        return null;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.menu_skip) {
            YesNoPopupColored popup = YesNoPopupColored.newInstance(getString(R.string.no_pin_code), getString(R.string.no_pin_code_description));
            popup.setCallback(fragment);
            BackstackManager.getInstance().navigateToFloatingFragment(popup, popup.getClass().getSimpleName(), true);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void yes() {
        BackstackManager.getInstance().navigateBack();
        goNext();
    }

    @Override
    public void no() {
        BackstackManager.getInstance().navigateBack();
    }
}

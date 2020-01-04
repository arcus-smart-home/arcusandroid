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
package arcus.app.account.settings.pin;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.common.base.Strings;
import com.iris.client.exception.ErrorResponseException;
import com.iris.client.model.PersonModel;

import java.util.ArrayList;
import java.util.List;

import arcus.app.R;
import arcus.app.common.error.ErrorManager;
import arcus.app.common.fragments.ModalErrorBottomSheet;
import arcus.app.common.fragments.ModalErrorBottomSheetSingleButton;
import arcus.app.common.sequence.SequencedFragment;
import arcus.app.subsystems.people.controller.NewPersonSequenceController;
import arcus.app.subsystems.people.model.PersonTypeSequence;
import arcus.cornea.controller.PersonController;
import kotlin.Unit;

public class SettingsUpdatePin extends SequencedFragment implements View.OnClickListener, PersonController.Callback {

    private final static String SCREEN_VARIANT = "SCREEN_VARIANT";
    private final static String PERSON_ID = "PERSON_ID";
    private final static String PLACE_ID = "PLACE_ID";

    private int workingOn = 0;
    private boolean makingRequest = false;
    @NonNull
    private String[] pinNumbers = new String[2];

    @NonNull
    private List<ImageView> circleViews = new ArrayList<>();
    private TextView stepInstructions;

    private final int[] settingsStrings = new int[] {
            R.string.place_creation_pin_code_title,
            R.string.people_confirm_pin
    };

    private final int[] addAPersonStrings = new int[] {
            R.string.people_create_pin,
            R.string.people_confirm_pin
    };

    private final int[] accountCreationStrings = new int[] {
            R.string.Account_registration_enter_pin_code_message,
            R.string.Account_registration_confirm_pin_code_message
    };

    private final int[] addAPlaceCreationStrings = new int[] {
            R.string.place_creation_pin_code_title,
            R.string.people_confirm_pin
    };

    @DrawableRes
    private int filledCircle;

    @DrawableRes
    private int hollowCircle;

    @NonNull
    private ScreenVariant variant = ScreenVariant.SETTINGS;
    @Nullable
    private String personId;
    private String placeID;

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
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //hide the softkeyboard. In the rare event of exception, simply log the exception and continue.
        try {
            requireActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        } catch (Exception e) {
            // Log
        }

        String resourceType = "id";
        String packageName = view.getContext().getPackageName();
        for (int index = 0; index < 10; index++) {
            String idName = "pin_number_pad_" + index;
            int id = getResources().getIdentifier(idName, resourceType, packageName);
            view.findViewById(id).setOnClickListener(this);
        }
        view.findViewById(R.id.pin_number_pad_backspace).setOnClickListener(this);

        stepInstructions = view.findViewById(R.id.pin_pad_header_text);
        stepInstructions.setText(getTitleString());

        for (int index = 1; index < 5; index++) {
            String idName = "pin_number_" + index + "_circle";
            int id = getResources().getIdentifier(idName, resourceType, packageName);
            circleViews.add(view.findViewById(id));
        }

        filledCircle = R.drawable.sidemenu_settings_blackcircle_filled;
        hollowCircle = R.drawable.sidemenu_settings_blackcircle;

        PersonController.instance().edit(personId, this);
    }

    public void onClick(@NonNull View view) {
        if (view.getId() == R.id.pin_number_pad_backspace) {
            removeLastChar();
        } else {
            String pinNumber = ((TextView) view).getText().toString();
            appendChar(pinNumber);
        }
    }

    private void appendChar(String character) {
        if (makingRequest) {
            return;
        }

        if (Strings.isNullOrEmpty(pinNumbers[workingOn])) {
            pinNumbers[workingOn] = character;
            circleViews.get(0).setImageResource(filledCircle);
        } else if (pinNumbers[workingOn].length() != 4) {
            pinNumbers[workingOn] += character;
            circleViews.get(pinNumbers[workingOn].length() - 1).setImageResource(filledCircle);
        }

        if (pinNumbers[workingOn].length() == 4) {
            if (workingOn == 1) {
                processUpdate();
            } else {
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
            circleViews.get(length - 1).setImageResource(hollowCircle);
        }
    }

    private void hollowDots() {
        for (int i = 0; i < circleViews.size(); i++) {
            circleViews.get(i).setImageResource(hollowCircle);
        }
    }

    private void processUpdate() {
        if (!pinNumbers[0].equals(pinNumbers[1])) {
            ModalErrorBottomSheetSingleButton.newInstance(
                    getString(R.string.pin_code_not_match_title),
                    getString(R.string.pin_code_not_match_text),
                    getString(R.string.dismiss)
            ).show(getFragmentManager());
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

    private String getTitleString () {
        switch (variant) {
            case SETTINGS: return getString(settingsStrings[workingOn]);
            case ADD_A_PERSON: return getString(addAPersonStrings[workingOn]);
            case ADD_A_PLACE: return getString(addAPlaceCreationStrings[workingOn]);
            case ACCOUNT_CREATION:
            default:
                return getString(accountCreationStrings[workingOn]);
        }
    }

    @Override
    public String getTitle() {
        switch (variant) {
            case ACCOUNT_CREATION: // Adding a new pin
            case ADD_A_PERSON:
            case ADD_A_PLACE: return getString(R.string.people_pin_code);

            case SETTINGS:
            default:
                return getString(R.string.change_pincode); // Editing Existing Pin Code
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
            ModalErrorBottomSheetSingleButton bottomSheet = ModalErrorBottomSheetSingleButton.newInstance(
                    getString(R.string.pin_code_not_unique_title), // title
                    getString(R.string.pin_code_not_unique_text), // description
                    getString(R.string.dismiss) // bottom
            );
            bottomSheet.show(getFragmentManager());
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
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_skip) {
            ModalErrorBottomSheet bottomSheet = ModalErrorBottomSheet.newInstance(
                    getString(R.string.no_pin_code), // title
                    getString(R.string.no_pin_code_description), // description
                    getString(android.R.string.yes), // top
                    getString(android.R.string.no) // bottom
            );
            bottomSheet.setGetSupportAction(() -> {
                goNext();
                return Unit.INSTANCE;
            });
            bottomSheet.show(getFragmentManager());
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }
}

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
package arcus.app.account.registration;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.google.common.collect.ImmutableSet;
import arcus.cornea.SessionController;
import com.iris.client.capability.Person;
import com.iris.client.event.Listener;
import com.iris.client.model.PersonModel;
import com.iris.client.util.Result;
import arcus.app.R;
import arcus.app.account.registration.controller.task.SaveSecurityQuestionsTask;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.error.ErrorManager;
import arcus.app.common.popups.SecurityQuestionPickerPopup;
import arcus.app.common.utils.PreferenceUtils;
import arcus.app.common.utils.ViewUtils;
import arcus.app.common.validation.NotEmptyValidator;
import arcus.app.common.view.Version1ButtonColor;
import arcus.app.common.view.Version1EditText;
import arcus.app.common.view.Version1TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;


public class AccountSecurityQuestionsFragment extends AccountCreationStepFragment implements SecurityQuestionPickerPopup.Callback {

    public static final String SCREEN_VARIANT = "SCREEN_VARIANT";

    private Version1TextView questionOne, questionTwo, questionThree;
    private View questionOneLayout, questionTwoLayout, questionThreeLayout;
    private Version1EditText answerOne, answerTwo, answerThree;
    private Version1TextView securityQuestionsDesc;
    private int q1LastSelection, q2LastSelection, q3LastSelection;
    private SecurityQuestionPickerPopup popup;

    private Map<String, String> questionMap;
    private List<String> answerKeys;
    @Nullable
    private ScreenVariant variant = ScreenVariant.ACCOUNT_CREATION;

    @Override
    public void updatedValue(int updatedQuestion, int selection) {
        Collection<String> vals = questionMap.values();
        String[] array = vals.toArray(new String[vals.size()]);
        if (updatedQuestion == 1) {
            if(q1LastSelection != selection) {
                q1LastSelection = selection;
                answerOne.setText("");
            }
            questionOne.setText(array[q1LastSelection]);
        } else if (updatedQuestion == 2) {
            if(q2LastSelection != selection) {
                q2LastSelection = selection;
                answerTwo.setText("");
            }
            questionTwo.setText(array[q2LastSelection]);
        } else if (updatedQuestion == 3) {
            if(q3LastSelection != selection) {
                q3LastSelection = selection;
                answerThree.setText("");
            }
            questionThree.setText(array[q3LastSelection]);
        }
    }

    public enum ScreenVariant {
        SETTINGS,
        ACCOUNT_CREATION
    }

    @NonNull
    public static AccountSecurityQuestionsFragment newInstance(ScreenVariant variant) {
        AccountSecurityQuestionsFragment fragment = new AccountSecurityQuestionsFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(SCREEN_VARIANT, variant);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getArguments();
        if(bundle!=null){
            variant = (ScreenVariant) bundle.getSerializable(SCREEN_VARIANT);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        securityQuestionsDesc = (Version1TextView) view.findViewById(R.id.security_questions_defined);

        questionOne = (Version1TextView) view.findViewById(R.id.fragment_account_security_one);
        questionTwo = (Version1TextView) view.findViewById(R.id.fragment_account_security_two);
        questionThree = (Version1TextView) view.findViewById(R.id.fragment_account_security_three);

        questionOneLayout = view.findViewById(R.id.fragment_account_security_one_layout);
        questionTwoLayout = view.findViewById(R.id.fragment_account_security_two_layout);
        questionThreeLayout = view.findViewById(R.id.fragment_account_security_three_layout);

        answerOne = (Version1EditText) view.findViewById(R.id.fragment_account_security_one_answer);
        answerTwo = (Version1EditText) view.findViewById(R.id.fragment_account_security_two_answer);
        answerThree = (Version1EditText) view.findViewById(R.id.fragment_account_security_three_answer);

        answerOne.useLightColorScheme(variant == ScreenVariant.SETTINGS);
        answerTwo.useLightColorScheme(variant == ScreenVariant.SETTINGS);
        answerThree.useLightColorScheme(variant == ScreenVariant.SETTINGS);

        Drawable chevronImage = ContextCompat.getDrawable(getActivity(), R.drawable.chevron);
        if (variant == ScreenVariant.SETTINGS) {
            questionOne.setTextColor(Color.WHITE);
            questionTwo.setTextColor(Color.WHITE);
            questionThree.setTextColor(Color.WHITE);

            securityQuestionsDesc.setTextColor(getResources().getColor(R.color.white_with_60));
            getButton().setColorScheme(Version1ButtonColor.WHITE);
            getButton().setText(getString(R.string.account_setting_save_btn));
            chevronImage = ContextCompat.getDrawable(getActivity(), R.drawable.chevron_white);
        } else {
            securityQuestionsDesc.setTextColor(getResources().getColor(R.color.black_with_60));
            getButton().setColorScheme(Version1ButtonColor.BLACK);
            getButton().setText(getString(R.string.account_registration_next_btn));
        }

        ((ImageView)view.findViewById(R.id.question_one_chevron)).setImageDrawable(chevronImage);
        ((ImageView)view.findViewById(R.id.question_two_chevron)).setImageDrawable(chevronImage);
        ((ImageView)view.findViewById(R.id.question_three_chevron)).setImageDrawable(chevronImage);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle(getTitle());

        // Load question data from server and populate form
        initializeQuestionData();

    }

    private void initializeQuestionData() {
        try {
            String url = PreferenceUtils.getPlatformUrl();
            getCorneaService().setup().loadLocalizedStrings(url, ImmutableSet.of("security_question"), "en").onCompletion(new Listener<Result<Map<String, String>>>() {
                @Override
                public void onEvent(@NonNull Result<Map<String, String>> mapResult) {
                    if (mapResult.isError()) {
                        logger.debug("Caught error when getting security questions: {}", mapResult.getError());
                    } else {
                        questionMap = mapResult.getValue();
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                populateUserSelections();
                            }
                        });
                    }
                }
            });
        } catch (Exception e) {
            logger.trace("Exception when getting security questions: {}", e);
            ErrorManager.in(getActivity()).showGenericBecauseOf(e);
        }
    }

    private void populateFormDefaults() {

        Collection<String> vals = questionMap.values();
        String[] array = vals.toArray(new String[vals.size()]);

        questionOne.setText(array[0]);
        questionTwo.setText(array[1]);
        questionThree.setText(array[2]);

        q1LastSelection = 0;
        q2LastSelection = 1;
        q3LastSelection = 2;
        //if this is the first time in, then we need to clear-out the answers, especially returning from back-press
        answerOne.setText("");
        answerTwo.setText("");
        answerThree.setText("");

        questionOne.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchQuestionPicker(1, q1LastSelection, q2LastSelection, q3LastSelection);
            }
        });

        questionTwo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchQuestionPicker(2, q2LastSelection, q1LastSelection, q3LastSelection);
            }
        });

        questionThree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchQuestionPicker(3, q3LastSelection, q1LastSelection, q2LastSelection);
            }
        });
    }

    private void launchQuestionPicker(int question, int defaultSelection, int disabledOne, int disableTwo) {
        Collection<String> vals = questionMap.values();
        String[] array = vals.toArray(new String[vals.size()]);
        popup = SecurityQuestionPickerPopup.newInstance(getResources().getString(R.string.select_a_question), array,
                defaultSelection, disabledOne, disableTwo, question);
        popup.setCallback(this);
        BackstackManager.getInstance().navigateToFloatingFragment(popup, popup.getClass().getCanonicalName(), true);
    }

    private void populateUserSelections () {

        populateFormDefaults();

        PersonModel personModel = SessionController.instance().getPerson();
        if (personModel == null) {
            return;
        }

        personModel.getSecurityAnswers().onCompletion(new Listener<Result<Person.GetSecurityAnswersResponse>>() {
            @Override
            public void onEvent(@NonNull final Result<Person.GetSecurityAnswersResponse> getSecurityAnswersResponseResult) {
                if (getSecurityAnswersResponseResult.isError()) {
                    logger.debug("Caught error when getting security answers: {}", getSecurityAnswersResponseResult.getError());
                } else {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            Map<String, String> answersMap = getSecurityAnswersResponseResult.getValue().getSecurityAnswers();
                            String[] questionKeys = questionMap.keySet().toArray(new String[questionMap.keySet().size()]);

                            Collection<String> vals = questionMap.values();
                            String[] array = vals.toArray(new String[vals.size()]);
                            //set default values just in case the user managed to get in without answering.
                            questionOne.setText(array[0]);
                            questionTwo.setText(array[1]);
                            questionThree.setText(array[2]);

                            answerKeys = new ArrayList<>(answersMap.keySet());
                            List<String> answerList = new ArrayList<>(answersMap.values());

                            if (answerKeys.size() > 0) {
                                q1LastSelection = Arrays.asList(questionKeys).indexOf(answerKeys.get(0));
                                questionOne.setText(array[q1LastSelection]);
                                answerOne.setText(answerList.get(0));
                            }
                            if (answerKeys.size() > 1) {
                                q2LastSelection = Arrays.asList(questionKeys).indexOf(answerKeys.get(1));
                                questionTwo.setText(array[q2LastSelection]);
                                answerTwo.setText(answerList.get(1));
                            }
                            if (answerKeys.size() > 2) {
                                q3LastSelection = Arrays.asList(questionKeys).indexOf(answerKeys.get(2));
                                questionThree.setText(array[q3LastSelection]);
                                answerThree.setText(answerList.get(2));
                            }
                        }
                    });
                }
            }
        });
    }

    @Override
    public boolean validate() {

        Collection<String> vals = questionMap.values();
        String[] array = vals.toArray(new String[vals.size()]);

        if (new NotEmptyValidator(getActivity(), answerOne, R.string.requiredField, answerOne.getHint()).isValid()) {
            registrationContext.setSecurityAnswerOne(answerOne.getText().toString());
            registrationContext.setSecurityQuestionOne(ViewUtils.getQuestionKey(array[q1LastSelection], questionMap));
        } else {
            return false;
        }

        if (new NotEmptyValidator(getActivity(), answerTwo, R.string.requiredField, answerTwo.getHint()).isValid()) {
            registrationContext.setSecurityAnswerTwo(answerTwo.getText().toString());
            registrationContext.setSecurityQuestionTwo(ViewUtils.getQuestionKey(array[q2LastSelection], questionMap));
        } else {
            return false;
        }

        if (new NotEmptyValidator(getActivity(), answerThree, R.string.requiredField, answerThree.getHint()).isValid()) {
            registrationContext.setSecurityAnswerThree(answerThree.getText().toString());
            registrationContext.setSecurityQuestionThree(ViewUtils.getQuestionKey(array[q3LastSelection], questionMap));
        } else {
            return false;
        }

        return true;
    }

    @Override
    public boolean submit() {
        registrationContext.setSecurityAnswerOne(answerOne.getText().toString());
        registrationContext.setSecurityAnswerTwo(answerTwo.getText().toString());
        registrationContext.setSecurityAnswerThree(answerThree.getText().toString());

        new SaveSecurityQuestionsTask(getActivity(), this, this, getCorneaService(), registrationContext).execute();
        return true;
    }


    @Override
    public String getTitle() {
        return getString(R.string.account_registration_security);
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_account_security;
    }

    @Override
    public void onComplete(boolean result) {
        logger.debug("Successfully saved security questions: ", result);
        hideProgressBar();

        if(variant == ScreenVariant.SETTINGS) {
            BackstackManager.getInstance().navigateBack();
        }else {
            super.onComplete(result);
        }
    }

    @Override
    public void onError(Exception e) {
        logger.error("Get exception on security page: {}", e);
        hideProgressBar();

        if(variant == ScreenVariant.SETTINGS) {
            ErrorManager.in(getActivity()).showGenericBecauseOf(e);
        }else{
            super.onError(e);
        }
    }

    public void handleBackPress() {
        if (popup != null && popup.isVisible()) {
            popup.onBackPressed();
        }
        else {
            goBack();
        }
    }
}

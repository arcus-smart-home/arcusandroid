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

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.iris.client.capability.Account;
import com.iris.client.event.Listener;
import com.iris.client.model.AccountModel;
import com.iris.client.util.Result;
import arcus.app.R;
import arcus.app.account.registration.controller.task.ArcusTask;
import arcus.app.account.registration.model.TransitionState;
import arcus.app.common.error.ErrorManager;
import arcus.app.common.sequence.SequencedFragment;
import arcus.app.common.view.Version1Button;
import arcus.app.subsystems.place.controller.NewPlaceSequenceController;


public abstract class PlaceCreationStepFragment extends SequencedFragment<NewPlaceSequenceController> implements View.OnClickListener, ArcusTask.ArcusTaskListener {

    private Version1Button continueBtn;

    public static final String TutorialFlag = "tutorialFlagKey";

    public abstract boolean submit();
    public abstract boolean validate();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        continueBtn = (Version1Button) view.findViewById(R.id.fragment_account_parent_continue_btn);
        if(continueBtn!=null){
            continueBtn.setOnClickListener(this);
        }

        return view;
    }

    @Override
    public void onResume () {
        super.onResume();
        getActivity().setTitle(getTitle());
    }

    public Version1Button getButton(){
        return continueBtn;
    }

    @Override
    public void onComplete(boolean result) {
        hideProgressBar();
        continueBtn.setEnabled(true);
        transitionToNextState();
    }

    @Override
    public void onError(Exception e) {
        hideProgressBar();
        continueBtn.setEnabled(true);
        ErrorManager.in(getActivity()).showGenericBecauseOf(e);
    }


    @Override
    public void onClick(View v) {
        continueBtn.setEnabled(false);
        showProgressBar();

        try {
            if(validate()){
                showProgressBar();
                submit();
            }else{
                hideProgressBar();
                continueBtn.setEnabled(true);
            }

        }catch (Exception e){
            continueBtn.setEnabled(true);
            hideProgressBar();
            ErrorManager.in(getActivity()).showGenericBecauseOf(e);
        }
    }

    public void transitionToNextState() {

            final TransitionState currentState = TransitionState.fromFragment(this);
            try {
                AccountModel accountModel = registrationContext.getAccountModel();
                if (accountModel != null) {
                    logger.debug("Updating user's account state to {}.", currentState);
                    accountModel.signupTransition(currentState.getStateName()).onCompletion(new Listener<Result<Account.SignupTransitionResponse>>() {
                        @Override
                        public void onEvent(@NonNull Result<Account.SignupTransitionResponse> signupTransitionResponseResult) {

                            // Error occurred saving account state to Cornea
                            if (signupTransitionResponseResult.isError()) {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        hideProgressBar();
                                        continueBtn.setEnabled(true);
                                    }
                                });
                                logger.trace("Sign up transition returns error: {}", signupTransitionResponseResult);
                                return;
                            }

                            if (currentState.isRegistrationComplete()) {
                                logger.debug("New place created.");
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                    hideProgressBar();
                                    goNext();
                                    }
                                });

                            } else {
                                logger.debug("Registration in process; current state is :{}", currentState);
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                    hideProgressBar();
                                    goNext();
                                    }
                                });
                            }
                        }
                    });

                }

            } catch (Exception ex) {
                logger.trace("Catch exception when send sign up transition: {}", ex);
            }

    }
}

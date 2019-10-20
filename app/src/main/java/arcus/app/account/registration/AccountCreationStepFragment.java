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

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import arcus.cornea.SessionController;
import com.iris.client.capability.Account;
import com.iris.client.event.Listener;
import com.iris.client.model.AccountModel;
import com.iris.client.util.Result;
import arcus.app.R;
import arcus.app.account.registration.controller.AccountCreationSequenceController;
import arcus.app.account.registration.controller.task.ArcusTask;
import arcus.app.account.registration.model.TransitionState;
import arcus.app.activities.DashboardActivity;
import arcus.app.common.error.ErrorManager;
import arcus.app.common.sequence.SequencedFragment;
import arcus.app.common.utils.PreferenceUtils;
import arcus.app.common.view.Version1Button;
import arcus.app.subsystems.people.PersonInvitationCongratsFragment;


public abstract class AccountCreationStepFragment extends SequencedFragment<AccountCreationSequenceController> implements View.OnClickListener, ArcusTask.ArcusTaskListener {

    protected Version1Button continueBtn;

    public static final String TutorialFlag = "tutorialFlagKey";

    public abstract boolean submit();
    public abstract boolean validate();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        continueBtn = (Version1Button) view.findViewById(R.id.fragment_account_parent_continue_btn);
        continueBtn.setOnClickListener(this);

        return view;
    }

    @Override
    public void onResume () {
        super.onResume();
        getActivity().setTitle(getTitle());
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




    public void transitionToNextState() {
        boolean bInvitation = false;
        if(getController() instanceof AccountCreationSequenceController && getController().getDeviceContact() != null) {
            bInvitation = true;
        }
        if(!bInvitation) {
            final TransitionState currentState = TransitionState.fromFragment(this);
            try {
                AccountModel accountModel = SessionController.instance().getAccount();
                if (accountModel != null) {
                    logger.debug("Updating user's account state to {}.", currentState);
                    accountModel.signupTransition(currentState.getStateName()).onCompletion(new Listener<Result<Account.SignupTransitionResponse>>() {
                        @Override
                        public void onEvent(@NonNull Result<Account.SignupTransitionResponse> signupTransitionResponseResult) {

                            // Error occured saving account state to Cornea
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
                                logger.debug("Registration completed.");
                                PreferenceUtils.setCompletedTutorial(false);
                                Intent intent = new Intent(getActivity(), DashboardActivity.class);
                                startActivity(intent);
                                getActivity().finish();
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
        else {
            if (this instanceof PersonInvitationCongratsFragment) {
                logger.debug("Invitation Registration completed.");
                PreferenceUtils.setCompletedTutorial(false);
                Intent intent = new Intent(getActivity(), DashboardActivity.class);
                startActivity(intent);
                getActivity().finish();
            }
            else {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        hideProgressBar();
                        goNext();
                    }
                });
            }
        }
    }
}

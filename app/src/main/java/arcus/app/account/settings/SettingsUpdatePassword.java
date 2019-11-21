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
import android.os.Bundle;
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.iris.client.event.Listener;
import com.iris.client.service.PersonService;
import com.iris.client.util.Result;
import arcus.app.R;
import arcus.app.common.error.ErrorManager;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.fragments.BaseFragment;
import arcus.app.common.models.RegistrationContext;
import arcus.app.common.validation.PasswordValidator;
import arcus.app.common.view.Version1Button;
import arcus.app.common.view.Version1ButtonColor;
import arcus.app.common.view.Version1EditText;

public class SettingsUpdatePassword extends BaseFragment {
    private Version1EditText currentPass;
    private Version1EditText firstPass;
    private Version1EditText secondPass;
    private Version1Button doneBtn;
    private String emailAddress = RegistrationContext.getInstance().getPersonModel().getEmail();

    @NonNull
    public static SettingsUpdatePassword newInstance() {
        return new SettingsUpdatePassword();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        currentPass = (Version1EditText) view.findViewById(R.id.etPasswordCurrent);
        firstPass = (Version1EditText) view.findViewById(R.id.etPasswordReset);
        secondPass = (Version1EditText) view.findViewById(R.id.etVerifyPasswordReset);
        currentPass.useLightColorScheme(true).useUppercaseLabels();
        firstPass.useLightColorScheme(true).useUppercaseLabels();
        secondPass.useLightColorScheme(true).useUppercaseLabels();

        TextView helperText = (TextView) view.findViewById(R.id.tvPasswordInstructions);
        helperText.setTextColor(getResources().getColor(R.color.overlay_white_with_60));

        doneBtn = (Version1Button) view.findViewById(R.id.btnSubmitReset);
        doneBtn.setTextColor(Color.BLACK);
        doneBtn.setColorScheme(Version1ButtonColor.WHITE);
        doneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PasswordValidator validator = new PasswordValidator(getActivity(), firstPass, secondPass, emailAddress);
                if (validator.isValid()) {
                    showProgressBarAndDisable(currentPass, firstPass, secondPass, doneBtn);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            updatePassword();
                        }
                    }).start();
                }
            }
        });

        setHasOptionsMenu(true);

        return view;
    }

    private void updatePassword() {
        PersonService service = getCorneaService().getService(PersonService.class);
        service.changePassword(currentPass.getText().toString(), firstPass.getText()
              .toString(), null)
              .onCompletion(new Listener<Result<PersonService.ChangePasswordResponse>>() {
                  @Override
                  public void onEvent(@NonNull Result<PersonService.ChangePasswordResponse> result) {
                      handleResult(result);
                  }
              });
    }

    private void handleResult(@NonNull final Result<PersonService.ChangePasswordResponse> result) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                hideProgressBarAndEnable(currentPass, firstPass, secondPass, doneBtn);

                if (result.isError() || !result.getValue().getSuccess()) {
                    logger.debug("Received error changing password.", result.getError());
                    ErrorManager.in(getActivity()).showGenericBecauseOf(result.getError());
                }
                else {
                    BackstackManager.getInstance().navigateBack();
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle(getTitle().toUpperCase());
    }

    @Override
    public String getTitle() {
        return getString(R.string.sign_up_password_hint);
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_account_settings_change_password;
    }
}

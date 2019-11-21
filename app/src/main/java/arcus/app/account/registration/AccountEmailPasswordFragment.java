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

import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import arcus.app.R;
import arcus.app.account.registration.controller.task.ArcusTask;
import arcus.app.account.registration.controller.task.SaveEmailTask;
import arcus.app.common.error.ErrorManager;
import arcus.app.common.error.type.SignupErrorType;
import arcus.app.common.utils.GlobalSetting;
import arcus.app.common.utils.PreferenceUtils;
import arcus.app.common.utils.URLSpanNoUnderline;
import arcus.app.common.utils.ViewUtils;
import arcus.app.common.validation.EmailValidator;
import arcus.app.common.validation.PasswordValidator;
import arcus.app.common.view.Version1Button;
import arcus.app.common.view.Version1EditText;
import arcus.app.subsystems.people.model.DeviceContact;

import org.apache.commons.lang3.StringUtils;



public class AccountEmailPasswordFragment extends AccountCreationStepFragment implements View.OnClickListener {
   @Nullable
   private Version1EditText mEmail = null;
   @Nullable
   private Version1EditText mPassword = null;
   @Nullable
   private Version1EditText mVerifyPassword = null;
   private CheckBox offersAndPromotionChkbx;

   private boolean offersAndPromotionChecked;

   private ArcusTask arcusTask;

   @NonNull
   public static AccountEmailPasswordFragment newInstance() {
      return new AccountEmailPasswordFragment();
   }

   @Override
   public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
      View view = super.onCreateView(inflater, container, savedInstanceState);

      mEmail = (Version1EditText) view.findViewById(R.id.etEmail);
      mPassword = (Version1EditText) view.findViewById(R.id.etPassword);
      mVerifyPassword = (Version1EditText) view.findViewById(R.id.etVerifyPassword);
      TextView terms = (TextView) view.findViewById(R.id.fragment_account_name_terms);
      TextView privacy = (TextView) view.findViewById(R.id.fragment_account_name_privacy);
      offersAndPromotionChkbx = (CheckBox) view.findViewById(R.id.offersAndPromotionChkbx);

      String mini8 = getString(R.string.password_hint_small_text);

      SpannableString ss = ViewUtils.appendSmallTextToHint(mPassword.getHint().toString(), mini8);
      mPassword.setHint(ss);
      ss = ViewUtils.appendSmallTextToHint(mVerifyPassword.getHint().toString(), mini8);
      mVerifyPassword.setHint(ss);

      offersAndPromotionChkbx.setOnClickListener(new View.OnClickListener() {

         @Override
         public void onClick(View v) {
            offersAndPromotionChecked = offersAndPromotionChkbx.isChecked();
         }

      });

      terms.setText(Html.fromHtml("<a href=" + GlobalSetting.T_AND_C_LINK + ">Terms of Service</a>"));
      terms.setMovementMethod(LinkMovementMethod.getInstance());

      privacy.setText(Html.fromHtml("<a href=" + GlobalSetting.PRIVACY_LINK + ">Privacy Policy</a>"));
      privacy.setMovementMethod(LinkMovementMethod.getInstance());

      stripUnderlines(terms);
      stripUnderlines(privacy);

      final DeviceContact contact = getController().getDeviceContact();
      if(contact != null && contact.getEmailAddresses().size() > 0) {
         mEmail.setText(contact.getEmailAddresses().get(0).getDisplay());
      }
      continueBtn = (Version1Button) view.findViewById(R.id.fragment_account_parent_continue_btn);
      continueBtn.setOnClickListener(this);

      return view;
   }

   @Override
   public void onStop() {
      if (arcusTask != null && arcusTask.getStatus() == AsyncTask.Status.RUNNING) {
         arcusTask.cancel(true);
      }
      super.onStop();
   }


   @Override
   public String getTitle () {
      return getString(R.string.account_registration_sing_up);
   }

   @Override
   public Integer getLayoutId() {
      return R.layout.fragment_account_email;
   }

   @Override
   public void onClick(View v) {
      continueBtn.setEnabled(false);
      showProgressBar();

      try {
         if(validate()){
            submit();
         } else {
            hideProgressBar();
            continueBtn.setEnabled(true);
         }

      }catch (Exception e){
         continueBtn.setEnabled(true);
         hideProgressBar();
         ErrorManager.in(getActivity()).showGenericBecauseOf(e);
      }
   }

   @Override
   public boolean validate() {

      String devAddress;
      String emailAddress;

      if (mEmail.getText() != null && StringUtils.isBlank(mEmail.getText().toString())) {
         mEmail.setError(getActivity().getString(R.string.account_registration_email_blank_error_msg));
         return false;
      }

      if (mEmail.getText().toString().contains("!")) {

         devAddress = StringUtils.substringBefore(mEmail.getText().toString(), "!");
         emailAddress = StringUtils.substringAfter(mEmail.getText().toString(), "!");

         if (devAddress.equals("") || emailAddress.equals("")) {
            mEmail.setError(getActivity().getString(R.string.requiredField, mEmail.getHint()));
            return false;
         }
         else {
            PreferenceUtils.putPlatformUrl(devAddress);
         }
      }
      else {
         emailAddress = mEmail.getText().toString().trim();
      }

      EmailValidator emailValidator = new EmailValidator(mEmail);
      if (!emailValidator.isValid()) {
         return false;
      }

      PasswordValidator passwordValidator = new PasswordValidator(getActivity(), mPassword, mVerifyPassword, mEmail);
      return (passwordValidator.isValid());
   }

   @Override
   public void onError(Exception e) {
      hideProgressBar();
      getButton().setEnabled(true);
      ErrorManager.in(getActivity()).show(SignupErrorType.fromThrowable(e));
   }

   @Override
   public boolean submit() {
      final DeviceContact contact = getController().getDeviceContact();
         arcusTask = new SaveEmailTask(
              getActivity(), this, this, getCorneaService(),
              mEmail.getText().toString().trim(), mPassword.getText().toString().toCharArray(),
              offersAndPromotionChkbx.isChecked(), contact);
         arcusTask.execute();
      return true;
   }

   private void stripUnderlines(@NonNull TextView tv) {
      Spannable s = (Spannable) tv.getText();
      URLSpan[] spans = s.getSpans(0, s.length(), URLSpan.class);

      for (URLSpan span : spans) {
         int start = s.getSpanStart(span);
         int end = s.getSpanEnd(span);
         s.removeSpan(span);
         span = new URLSpanNoUnderline(span.getURL());
         s.setSpan(span, start, end, 0);
      }

      tv.setText(s);
   }
}

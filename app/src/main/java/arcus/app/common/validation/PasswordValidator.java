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
package arcus.app.common.validation;

import android.content.Context;
import androidx.annotation.NonNull;
import android.widget.EditText;

import com.google.android.material.textfield.TextInputLayout;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import arcus.app.R;

import org.apache.commons.lang3.StringUtils;

public class PasswordValidator implements InputValidator {
   private final Context context;
   private final EditText firstPass;
   private final EditText secondPass;
   private final String emailAddress;

   @Nullable private TextInputLayout firstPassLayout;
   @Nullable private TextInputLayout secondPassLayout;

   /**
    *
    * Create a password validator that checks for passwords matching requirements.
    * This will also post the appropriate Error Messages to {@link #firstPass}
    *
    * @param context Context used to get resources.
    * @param firstPass First Password box (Where errors are appended)
    * @param secondPass Second Password box
    * @param emailAddress Input email address
    */
   public PasswordValidator(Context context, EditText firstPass, EditText secondPass, String emailAddress) {
      this(context, null, firstPass, null, secondPass, emailAddress);
   }

   /**
    *
    * Create a password validator that checks for passwords matching requirements.
    * This will also post the appropriate Error Messages to {@link #firstPass}
    *
    * @param context Context used to get resources.
    * @param firstPass First Password box (Where errors are appended)
    * @param secondPass Second Password box
    * @param emailAddress Input email address
    */
   public PasswordValidator(
           Context context,
           @Nullable TextInputLayout firstPassLayout,
           EditText firstPass,
           @Nullable TextInputLayout secondPassLayout,
           EditText secondPass,
           String emailAddress
   ) {
      this.context = context;
      this.firstPassLayout = firstPassLayout;
      this.firstPass = firstPass;
      this.secondPassLayout = secondPassLayout;
      this.secondPass = secondPass;
      this.emailAddress = emailAddress;
   }

   /**
    *
    * Create a password validator that checks for passwords matching requirements.
    * This will also post the appropriate Error Messages to {@link #firstPass}
    *
    * @param context Context used to get resources.
    * @param firstPass First Password box (Where errors are appended)
    * @param secondPass Second Password box
    * @param emailAddress Input email address
    */
   public PasswordValidator(Context context, EditText firstPass, EditText secondPass, @NonNull EditText emailAddress) {
      this.context = context;
      this.firstPass = firstPass;
      this.secondPass = secondPass;
      this.emailAddress = emailAddress.getText().toString();
   }

   @Override
   public boolean isValid() {
      return  validatePasswordsMatch() &&
              validateNotBlank() &&
              validateNoSpaces() &&
              validateDoesntMatchEmail() &&
              validateDoesntContainEmail() &&
              validatePasswordLength() &&
              validatePasswordHasDigit() &&
              validatePasswordHasCharacter();
   }

   private boolean validateNotBlank() {
      if (StringUtils.isBlank(firstPass.getText().toString())) {
         setError(firstPassLayout, firstPass, R.string.account_registration_password_blank_error_msg);
         return false;
      }

      return true;
   }

   private boolean validateNoSpaces() {
      if (StringUtils.containsWhitespace(firstPass.getText().toString())) {
         setError(firstPassLayout, firstPass, R.string.account_registration_password_contains_space_error_msg);
         return false;
      }

      return true;
   }

   private boolean validateDoesntMatchEmail() {
      if (firstPass.getText().toString().equalsIgnoreCase(emailAddress)) {
         setError(firstPassLayout, firstPass, R.string.account_registration_password_equals_email_error_msg);
         return false;
      }

      return true;
   }

   private boolean validateDoesntContainEmail() {
      String username = emailAddress.substring(0, emailAddress.indexOf("@"));
      if (firstPass.getText().toString().toLowerCase().contains(username.toLowerCase())) {
         setError(firstPassLayout, firstPass, R.string.account_registration_password_contains_email_error_msg);
         return false;
      }

      return true;
   }

   private boolean validatePasswordLength() {
      int PASSWORD_MIN_LENGTH = 8;
      if (firstPass.getText().toString().length() < PASSWORD_MIN_LENGTH) {
         setError(firstPassLayout, firstPass, R.string.account_registration_password_size_error_msg);
         return false;
      }

      return true;
   }

   private boolean validatePasswordHasDigit() {
      String DIGITS = "0123456789";
      if (!StringUtils.containsAny(firstPass.getText().toString(), DIGITS)) {
         setError(firstPassLayout, firstPass, R.string.account_registration_password_contains_no_num_error_msg);
         return false;
      }

      return true;
   }

   private boolean validatePasswordHasCharacter() {
      if (!firstPass.getText().toString().matches(".*[a-zA-Z].*")) {
         setError(firstPassLayout, firstPass, R.string.account_registration_password_contains_no_char_error_msg);
         return false;
      }

      return true;
   }

   private boolean validatePasswordsMatch() {
      if (!firstPass.getText().toString().equals(secondPass.getText().toString())) {
         setError(firstPassLayout, firstPass, "");
         setError(secondPassLayout, secondPass, R.string.account_registration_verify_password_not_equal_to_password_error_msg);
         return false;
      }

      return true;
   }

   private void setError(
           @Nullable TextInputLayout textInputLayout,
           @NonNull EditText editText,
           @StringRes int stringRes
   ) {
      setError(textInputLayout, editText, context.getString(stringRes));
   }

   private void setError(
           @Nullable TextInputLayout textInputLayout,
           @NonNull EditText editText,
           String error
   ) {
      if (textInputLayout != null) {
         textInputLayout.setError(error);
      } else {
         editText.setError(error);
      }
   }
}

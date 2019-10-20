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
import android.support.annotation.NonNull;
import android.widget.EditText;

import arcus.app.R;

import org.apache.commons.lang3.StringUtils;

public class PasswordValidator implements InputValidator {
   private final Context context;
   private final EditText firstPass;
   private final EditText secondPass;
   private final String emailAddress;

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
      this.context = context;
      this.firstPass = firstPass;
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
         firstPass.setError(context.getResources().getString(R.string.account_registration_password_blank_error_msg));
         return false;
      }

      return true;
   }

   private boolean validateNoSpaces() {
      if (StringUtils.containsWhitespace(firstPass.getText().toString())) {
         firstPass.setError(context.getResources().getString(R.string.account_registration_password_contains_space_error_msg));
         return false;
      }

      return true;
   }

   private boolean validateDoesntMatchEmail() {
      if (firstPass.getText().toString().equalsIgnoreCase(emailAddress)) {
         firstPass.setError(context.getResources().getString(R.string.account_registration_password_equals_email_error_msg));
         return false;
      }

      return true;
   }

   private boolean validateDoesntContainEmail() {
      String username = emailAddress.substring(0, emailAddress.indexOf("@"));
      if (firstPass.getText().toString().toLowerCase().contains(username.toLowerCase())) {
         firstPass.setError(context.getResources().getString(R.string.account_registration_password_contains_email_error_msg));
         return false;
      }

      return true;
   }

   private boolean validatePasswordLength() {
      int PASSWORD_MIN_LENGTH = 8;
      if (firstPass.getText().toString().length() < PASSWORD_MIN_LENGTH) {
         firstPass.setError(context.getResources().getString(R.string.account_registration_password_size_error_msg));
         return false;
      }

      return true;
   }

   private boolean validatePasswordHasDigit() {
      String DIGITS = "0123456789";
      if (!StringUtils.containsAny(firstPass.getText().toString(), DIGITS)) {
         firstPass.setError(context.getResources().getString(R.string.account_registration_password_contains_no_num_error_msg));
         return false;
      }

      return true;
   }

   private boolean validatePasswordHasCharacter() {
      if (!firstPass.getText().toString().matches(".*[a-zA-Z].*")) {
         firstPass.setError(context.getResources().getString(R.string.account_registration_password_contains_no_char_error_msg));
         return false;
      }

      return true;
   }

   private boolean validatePasswordsMatch() {
      if (!firstPass.getText().toString().equals(secondPass.getText().toString())) {
         firstPass.setError(" ");
         secondPass.setError(context.getResources().getString(R.string.account_registration_verify_password_not_equal_to_password_error_msg));
         return false;
      }

      return true;
   }
}

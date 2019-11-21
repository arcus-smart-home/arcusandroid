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

import androidx.annotation.NonNull;
import android.widget.EditText;

import arcus.app.ArcusApplication;
import arcus.app.R;

import org.apache.commons.lang3.StringUtils;

public class EmailValidator implements InputValidator {
   @NonNull
   private final EditText emailTB;
   private final String emailAddress;

   public EmailValidator(@NonNull EditText emailTB) {
      this.emailTB = emailTB;

      CharSequence chars = emailTB.getText();
      if (chars != null) {
         this.emailAddress = chars.toString();
      } else {
         this.emailAddress = null;
      }
   }

   @Override
   public boolean isValid() {
      if (StringUtils.isEmpty(emailAddress)) {
         emailTB.setError(ArcusApplication.getContext().getString(R.string.account_registration_missing_email_error_msg));
         return false;
      }

      if (!android.util.Patterns.EMAIL_ADDRESS.matcher(emailAddress).matches()) {
         emailTB.setError(ArcusApplication.getContext().getResources().getString(R.string.account_registration_email_well_formed_error_msg));
         return false;
      }

      return true;
   }
}

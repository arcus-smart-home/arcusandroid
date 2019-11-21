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
package arcus.app.subsystems.people;

import androidx.annotation.NonNull;
import android.view.View;
import arcus.app.R;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.popups.YesNoPopupColored;


public class PersonIdentityPartialFragment extends PersonIdentityFragment implements YesNoPopupColored.Callback {
    PersonIdentityPartialFragment fragment;

    @NonNull
    public static PersonIdentityPartialFragment newInstance() {
        return new PersonIdentityPartialFragment();
    }

    @Override
    public void onResume() {
        super.onResume();
        fragment = this;
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(emailEmpty()) {
                    YesNoPopupColored popup = YesNoPopupColored.newInstance(getString(R.string.no_email_address), getString(R.string.no_email_address_description));
                    popup.setCallback(fragment);
                    BackstackManager.getInstance().navigateToFloatingFragment(popup, popup.getClass().getSimpleName(), true);
                } else if (isValidInput()) {
                    getController().setDeviceContact(getContactInformation());
                    goNext();
                }
            }
        });
    }

    @Override
    public void yes() {
        BackstackManager.getInstance().navigateBack();
        if (isValidInput()) {
            getController().setDeviceContact(getContactInformation());
            goNext();
        }
    }

    @Override
    public void no() {
        BackstackManager.getInstance().navigateBack();
    }
}

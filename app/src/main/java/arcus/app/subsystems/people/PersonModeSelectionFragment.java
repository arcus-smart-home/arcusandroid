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

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import arcus.app.R;
import arcus.app.activities.BaseActivity;
import arcus.app.common.image.IntentRequestCode;
import arcus.app.common.sequence.SequencedFragment;
import arcus.app.common.utils.GlobalSetting;
import arcus.app.common.view.Version1Button;
import arcus.app.subsystems.people.controller.NewPersonSequenceController;

import java.util.ArrayList;


public class PersonModeSelectionFragment extends SequencedFragment<NewPersonSequenceController> implements BaseActivity.PermissionCallback {
    private Version1Button contacts;
    private Version1Button manual;
    private PersonModeSelectionFragment fragment;

    @NonNull
    public static PersonModeSelectionFragment newInstance () {
        return new PersonModeSelectionFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        fragment = this;
        contacts = (Version1Button) view.findViewById(R.id.btnContacts);
        manual = (Version1Button) view.findViewById(R.id.btnManual);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        manual.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goNext();
            }
        });

        contacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((BaseActivity)getActivity()).setPermissionCallback(fragment);
                ArrayList<String> permissions = new ArrayList<String>();
                permissions.add(Manifest.permission.READ_CONTACTS);
                ((BaseActivity)getActivity()).checkPermission(permissions, GlobalSetting.PERMISSION_READ_CONTACTS, R.string.permission_rationale_contacts);
            }
        });
    }

    @Override
    public String getTitle() {
        return getString(R.string.people_add_a_person);
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_person_mode_selection;
    }

    public void doLaunchContactPicker() {
        Intent contactPickerIntent = new Intent(Intent.ACTION_PICK,
                ContactsContract.Contacts.CONTENT_URI);
        getActivity().startActivityForResult(contactPickerIntent, IntentRequestCode.DEVICE_CONTACT_SELECTION.requestCode);
    }

    @Override
    public void permissionsUpdate(int permissionType, ArrayList<String> permissionsDenied, ArrayList<String> permissionsDeniedNeverAskAgain) {
        if(permissionsDenied.contains(Manifest.permission.READ_CONTACTS)) {
            ((BaseActivity)getActivity()).showSnackBarForPermissions(getString(R.string.permission_read_contacts_denied_message));
        }
        else {
            doLaunchContactPicker();
        }
    }
}

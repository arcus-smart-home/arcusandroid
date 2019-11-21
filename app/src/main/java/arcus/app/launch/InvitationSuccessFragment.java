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
package arcus.app.launch;

import android.os.Bundle;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import arcus.app.account.registration.AccountEmailPasswordFragment;
import arcus.app.account.registration.controller.AccountCreationSequenceController;
import arcus.app.account.registration.model.AccountTypeSequence;
import arcus.app.common.fragments.BaseFragment;
import arcus.app.common.view.Version1Button;
import arcus.app.common.view.Version1TextView;
import arcus.app.R;
import arcus.app.subsystems.people.model.DeviceContact;

public class InvitationSuccessFragment extends BaseFragment {
    public static String DEVICE_CONTACT = "DEVICE_CONTACT";
    public InvitationSuccessFragment() {
        // Required empty public constructor
    }

    public static InvitationSuccessFragment newInstance(DeviceContact contact) {
        InvitationSuccessFragment fragment = new InvitationSuccessFragment();
        final Bundle arguments = new Bundle();
        arguments.putParcelable(DEVICE_CONTACT, contact);
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        final DeviceContact contact = getArguments().getParcelable(DEVICE_CONTACT);
        String firstName = contact.getFirstName();
        String lastName = contact.getLastName();

        Version1TextView title = (Version1TextView) view.findViewById(R.id.invitation_success_title);
        title.setText(String.format(getResources().getString(R.string.invitation_success_title), new String(firstName+ " "+lastName).trim()));

        Version1TextView description = (Version1TextView) view.findViewById(R.id.invitation_success_description);
        description.setText(String.format(getResources().getString(R.string.invitation_success_description), contact.getInvitorFirstName(), contact.getInvitedPlaceName()));

        Version1Button next = (Version1Button) view.findViewById(R.id.btnNext);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AccountCreationSequenceController(AccountTypeSequence.INVITATION_ACCOUNT_CREATION, contact).startSequence(getActivity(), null, AccountEmailPasswordFragment.class);
            }
        });
        return view;
    }

    @Nullable
    @Override
    public String getTitle() {
        return getResources().getString(R.string.invitation_title);
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_invitation_success;
    }
}

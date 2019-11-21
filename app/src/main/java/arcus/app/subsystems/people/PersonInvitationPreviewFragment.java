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

import android.os.Bundle;
import androidx.annotation.NonNull;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import arcus.cornea.controller.PersonController;
import arcus.cornea.model.InviteModel;
import arcus.app.R;
import arcus.app.common.error.ErrorManager;
import arcus.app.common.sequence.SequencedFragment;
import arcus.app.common.view.Version1Button;
import arcus.app.common.view.Version1TextView;
import arcus.app.subsystems.people.controller.NewPersonSequenceController;

public class PersonInvitationPreviewFragment extends SequencedFragment<NewPersonSequenceController> {
    private Version1Button sendInvitation;
    private Version1TextView description;
    private Version1TextView messageBody;
    private EditText addMessageBody;

    @NonNull
    public static PersonInvitationPreviewFragment newInstance () {
        return new PersonInvitationPreviewFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        sendInvitation = (Version1Button) view.findViewById(R.id.btnSend);
        messageBody = (Version1TextView) view.findViewById(R.id.message_body);
        description = (Version1TextView) view.findViewById(R.id.description);

        final TextView characters = (TextView) view.findViewById(R.id.character_count);
        characters.setText(String.format(getResources().getString(R.string.invitation_message_size), 0));
        addMessageBody = (EditText) view.findViewById(R.id.add_message_body);
        addMessageBody.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable editText) {
                characters.setText(String.format(getResources().getString(R.string.invitation_message_size), editText.length()));
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
            }
        });

        setDescription();
        setInvitationMessage();

        return view;
    }

    private void setDescription() {
        description.setText(getResources().getString(R.string.invitation_personalize_description));
    }

    private void setInvitationMessage() {
        messageBody.setText(getController().getInviteModel().getInvitationText());
    }

    @Override
    public void onResume() {
        super.onResume();

        sendInvitation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InviteModel inviteModel = getController().getInviteModel();
                if (inviteModel == null) {
                    ErrorManager.in(getActivity()).showGenericBecauseOf(new RuntimeException("Invite model was null, cannot continue."));
                    return;
                }

                //Don't set the personalized message if it hasn't been edited
                if(!addMessageBody.getText().toString().equals("")) {
                    inviteModel.setPersonalizedGreeting(addMessageBody.getText().toString());
                }
                showProgressBarAndDisable(sendInvitation);
                PersonController.instance().sendInvite(inviteModel, new PersonController.SendInviteCallback() {
                    @Override public void inviteError(Throwable throwable) {
                        hideProgressBarAndEnable(sendInvitation);
                        ErrorManager.in(getActivity()).showGenericBecauseOf(throwable);
                    }

                    @Override public void personInvited() {
                        hideProgressBarAndEnable(sendInvitation);
                        goNext();
                    }
                });
            }
        });
    }

    @Override
    public String getTitle() {
        return getString(R.string.people_add_a_person);
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_person_invitation_preview;
    }
}

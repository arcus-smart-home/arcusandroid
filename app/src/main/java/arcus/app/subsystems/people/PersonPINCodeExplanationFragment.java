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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import arcus.cornea.controller.PersonController;
import arcus.app.R;
import arcus.app.common.sequence.SequencedFragment;
import arcus.app.common.view.Version1Button;
import arcus.app.common.view.Version1TextView;
import arcus.app.subsystems.people.controller.NewPersonSequenceController;
import arcus.app.subsystems.people.model.DeviceContact;


public class PersonPINCodeExplanationFragment extends SequencedFragment<NewPersonSequenceController> {
    private Version1Button nextButton;

    @NonNull
    public static PersonPINCodeExplanationFragment newInstance() {
        return new PersonPINCodeExplanationFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        nextButton = (Version1Button) view.findViewById(R.id.next_button);

        DeviceContact contact = getController().getDeviceContact();
        Version1TextView alarms = (Version1TextView) view.findViewById(R.id.description_alarms);
        Version1TextView locks = (Version1TextView) view.findViewById(R.id.description_locks);
        if(contact != null) {
            alarms.setText(String.format(getResources().getString(R.string.people_pin_explanation_description_alarm), contact.getFirstName()));
            locks.setText(String.format(getResources().getString(R.string.people_pin_explanation_description_locks), contact.getFirstName()));
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle(getTitle());

        if (PersonController.instance().getPerson() == null || !PersonController.instance().getPerson().getHasPin()) {
            goNext();
        }

        PersonController.instance().startNewPerson();

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goNext();
            }
        });
    }

    @Override
    public String getTitle() {
        return getString(R.string.people_add_a_person);
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_person_pin_explanation;
    }
}

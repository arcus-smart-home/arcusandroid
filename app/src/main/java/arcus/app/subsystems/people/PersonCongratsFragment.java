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
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import arcus.cornea.provider.PlaceModelProvider;

import arcus.app.R;
import arcus.app.common.sequence.SequencedFragment;
import arcus.app.common.view.Version1TextView;
import arcus.app.subsystems.people.controller.NewPersonSequenceController;
import arcus.app.subsystems.people.model.DeviceContact;
import arcus.app.subsystems.people.model.PersonTypeSequence;


public class PersonCongratsFragment extends SequencedFragment<NewPersonSequenceController> {

    private Version1TextView congratsMessage;
    private Version1TextView congratsMessageDescription;
    private Version1TextView congratsMessageDescriptionBottom;
    private View congratsIconsAndText;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        congratsMessage = (Version1TextView) view.findViewById(R.id.you_successfully_added);
        congratsMessageDescription = (Version1TextView) view.findViewById(R.id.you_successfully_added_description);
        congratsMessageDescriptionBottom = (Version1TextView) view.findViewById(R.id.congrats_description);
        congratsIconsAndText = view.findViewById(R.id.congrats_icons_and_text_view);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        String message;
        String messageDescription;

        getActivity().setTitle(getTitle());

        DeviceContact contact = getController().getDeviceContact();

        PersonTypeSequence creationType = PersonTypeSequence.values()[(getController()).getSequenceType()];
        if(PersonTypeSequence.FULL_ACCESS.equals(creationType)) {
            message = String.format(getResources().getString(R.string.people_you_have_added_full_access),
                    contact.getFirstName(),
                    PlaceModelProvider.getCurrentPlace().get().getName());
            congratsMessage.setText(message);

            messageDescription = String.format(getResources().getString(R.string.people_you_have_added_description_full_access),
                    contact.getFirstName());
            congratsMessageDescription.setText(messageDescription);

            congratsMessageDescriptionBottom.setText(R.string.people_congrats_bottom_label_full_access);

            congratsIconsAndText.setVisibility(View.GONE);
        } else {
            message = String.format(getResources().getString(R.string.people_you_have_added_full_access),
                    contact.getFirstName() + " " + contact.getLastName(),
                    PlaceModelProvider.getCurrentPlace().get().getName());
            congratsMessage.setText(message);

            messageDescription = String.format(getResources().getString(R.string.people_you_have_added_description),
                    contact.getFirstName());
            congratsMessageDescription.setText(messageDescription);

            congratsMessageDescriptionBottom.setText(R.string.people_congrats_bottom_label);
        }

    }

    @Override
    public String getTitle() {
        return getString(R.string.people_success);
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_person_congrats;
    }

    @Override
    public Integer getMenuId() {
        return R.menu.menu_close;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        endSequence(true);
        return true;
    }
}

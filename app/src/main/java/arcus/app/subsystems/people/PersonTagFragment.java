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
import android.widget.ListView;

import com.google.common.collect.ImmutableSet;
import arcus.cornea.SessionController;
import arcus.cornea.controller.PersonController;
import arcus.cornea.model.InviteModel;
import com.iris.client.capability.Person;
import com.iris.client.model.PersonModel;
import arcus.app.R;
import arcus.app.common.error.ErrorManager;
import arcus.app.common.error.type.PersonErrorType;
import arcus.app.common.sequence.SequencedFragment;
import arcus.app.common.view.Version1Button;
import arcus.app.common.view.Version1TextView;
import arcus.app.subsystems.people.adapter.PersonRelationshipAdapter;
import arcus.app.subsystems.people.controller.NewPersonSequenceController;
import arcus.app.subsystems.people.model.PersonRelationship;
import arcus.app.subsystems.people.model.PersonRelationshipFamilyTag;
import arcus.app.subsystems.people.model.PersonRelationshipServiceTag;
import arcus.app.subsystems.people.model.PersonRelationshipTag;
import arcus.app.subsystems.people.model.PersonTypeSequence;

import java.util.ArrayList;
import java.util.List;


public class PersonTagFragment extends SequencedFragment<NewPersonSequenceController> implements PersonRelationshipAdapter.SelectionChangedListener, PersonController.Callback {

    private Version1TextView titleView;
    private Version1TextView subtitleView;
    private ListView tagList;
    private Version1Button nextButton;

    private String selectedPerson;

    @NonNull
    public static PersonTagFragment newInstance() {
        return new PersonTagFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        subtitleView = (Version1TextView) view.findViewById(R.id.list_subtitle);
        titleView = (Version1TextView) view.findViewById(R.id.list_title);
        tagList = (ListView) view.findViewById(R.id.list);
        nextButton = (Version1Button) view.findViewById(R.id.next_button);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        ArrayList<PersonRelationship> relationships = new ArrayList<>();
        PersonRelationshipTag[] relationshipTags = PersonRelationshipTag.values();
        for(PersonRelationshipTag tag : relationshipTags) {
            ArrayList<PersonRelationship> children = new ArrayList<>();
            //check for family
            if(tag.ordinal() == PersonRelationshipTag.FAMILY.ordinal()) {
                PersonRelationshipFamilyTag[] childTags = PersonRelationshipFamilyTag.values();
                for(PersonRelationshipFamilyTag childTag : childTags) {
                    children.add(new PersonRelationship(false, getString(PersonRelationshipFamilyTag.valueOf(childTag.name()).getStringResId()), new ArrayList<PersonRelationship>()));
                }
                children.get(0).setSelected(true);
            }
            else if(tag.ordinal() == PersonRelationshipTag.SERVICEPERSON.ordinal()) {
                PersonRelationshipServiceTag[] childTags = PersonRelationshipServiceTag.values();
                for(PersonRelationshipServiceTag childTag : childTags) {
                    children.add(new PersonRelationship(false, getString(PersonRelationshipServiceTag.valueOf(childTag.name()).getStringResId()), new ArrayList<PersonRelationship>()));
                }
                children.get(0).setSelected(true);
            }
            relationships.add(new PersonRelationship(false, getString(PersonRelationshipTag.valueOf(tag.name()).getStringResId()), children));
        }
        relationships.get(0).setSelected(true);
        PersonRelationshipAdapter adapter = new PersonRelationshipAdapter(getActivity(), relationships);
        adapter.setListener(this);

        titleView.setText(getString(R.string.people_how_do_you_know));
        tagList.setAdapter(adapter);

        PersonController.instance().setNewCallback(this);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedPerson == null) {
                    ErrorManager.in(getActivity()).show(PersonErrorType.NO_TAG_SELECTED);
                } else {
                    //TODO: APAP do we need to store in a format that matches the enums?
                    selectedPerson = selectedPerson.toUpperCase();
                    selectedPerson = selectedPerson.replace("(", "");
                    selectedPerson = selectedPerson.replace(")", "");
                    selectedPerson = selectedPerson.replace(" ", "");

                    PersonController.instance().set(Person.ATTR_TAGS, ImmutableSet.of(selectedPerson));
                    if (PersonTypeSequence.FULL_ACCESS.ordinal() == getController().getSequenceType()) {
                        showLoading();
                        PersonController.instance().createInvite(new PersonController.CreateInviteCallback() {
                            @Override public void inviteError(Throwable throwable) {
                                hideProgressBarAndEnable(nextButton);
                                ErrorManager.in(getActivity()).showGenericBecauseOf(throwable);
                            }

                            @Override public void inviteCreated(InviteModel inviteModel) {
                                hideProgressBarAndEnable(nextButton);
                                getController().setInviteModel(inviteModel);
                                goNext();
                            }
                        });
                    }
                    else {
                        PersonController.instance().createPerson(null, null, getController().getPlaceID());
                    }
                }
            }
        });
    }

    @Override
    public String getTitle() {
        return getString(R.string.people_add_a_person);
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_person_tag;
    }

    @Override
    public void onSelectionChanged(String selectionType, String selection) {
        selectedPerson = selection;
    }

    @Override
    public void onUpdateChildText(String selectionType, String selectionName) {
        ((PersonRelationshipAdapter)tagList.getAdapter()).notifyDataSetChanged();
    }

    @Override
    public void showLoading() {
        hideProgressBar();
        showProgressBarAndDisable(nextButton);
    }

    @Override
    public void updateView(@NonNull PersonModel personModel) {
        hideProgressBarAndEnable(nextButton);
        PersonController.instance().edit(personModel.getAddress(), this);

        // Squirrel away the person address (as future pages will need this)
        getController().setPersonAddress(personModel.getAddress());
        getController().setPlaceID(SessionController.instance().getActivePlace());

        // ... then transition to the next page in the sequence
        goNext();
    }

    @Override
    public void onError(Throwable throwable) {
        hideProgressBarAndEnable(nextButton);
        ErrorManager.in(getActivity()).showGenericBecauseOf(throwable);
    }

    @Override
    public void onModelLoaded(PersonModel personModel) {}

    @Override
    public void onModelsLoaded(@NonNull List<PersonModel> personList) {}

    @Override
    public void createdModelNotFound() {}
}

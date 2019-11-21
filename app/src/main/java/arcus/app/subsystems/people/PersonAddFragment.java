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
import android.widget.RelativeLayout;

import arcus.app.R;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.sequence.SequencedFragment;
import arcus.app.common.popups.InfoTextPopup;
import arcus.app.subsystems.people.controller.NewPersonSequenceController;
import arcus.app.subsystems.people.model.PersonTypeSequence;


public class PersonAddFragment extends SequencedFragment<NewPersonSequenceController> {

    private RelativeLayout fullAccessRegion;
    private RelativeLayout fullAccessInfoRegion;
    private RelativeLayout locksAlarmsNotificationsRegion;
    private RelativeLayout locksAlarmsNotificationsInfoRegion;

    @NonNull
    public static PersonAddFragment newInstance () {
        return new PersonAddFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        fullAccessRegion = (RelativeLayout) view.findViewById(R.id.full_access_region);
        fullAccessInfoRegion = (RelativeLayout) view.findViewById(R.id.full_access_info_region);
        locksAlarmsNotificationsRegion = (RelativeLayout) view.findViewById(R.id.locks_alarms_notifications_region);
        locksAlarmsNotificationsInfoRegion = (RelativeLayout) view.findViewById(R.id.locks_alarms_notifications_info_region);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle(getTitle());
        fullAccessRegion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(getController() == null ||  !(getController() instanceof NewPersonSequenceController)) {
                    setController(new NewPersonSequenceController(PersonTypeSequence.FULL_ACCESS));
                }
                else {
                    getController().setSequenceType(PersonTypeSequence.FULL_ACCESS);
                }
                goNext();
            }
        });

        fullAccessInfoRegion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int infoText = R.string.people_full_access_info;
                BackstackManager.getInstance().navigateToFloatingFragment(InfoTextPopup.newInstance(infoText), InfoTextPopup.class.getName(), true);
            }
        });

        locksAlarmsNotificationsRegion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(getController() == null ||  !(getController() instanceof NewPersonSequenceController)) {
                    setController(new NewPersonSequenceController(PersonTypeSequence.PARTIAL_ACCESS));
                }
                else {
                    getController().setSequenceType(PersonTypeSequence.PARTIAL_ACCESS);
                }
                goNext();
            }
        });

        locksAlarmsNotificationsInfoRegion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int infoText = R.string.people_partial_access_info;
                BackstackManager.getInstance().navigateToFloatingFragment(InfoTextPopup.newInstance(infoText), InfoTextPopup.class.getName(), true);
            }
        });

    }

    @Override
    public String getTitle() {
        return getString(R.string.people_add_a_person);
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_add_a_person;
    }
}

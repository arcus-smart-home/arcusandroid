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
package arcus.app.common.popups;

import android.os.Bundle;
import androidx.annotation.NonNull;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.common.collect.Lists;
import arcus.cornea.provider.PersonModelProvider;
import arcus.cornea.utils.AddressableListSource;
import arcus.cornea.utils.Listeners;
import com.iris.client.event.Listener;
import com.iris.client.model.ModelAddedEvent;
import com.iris.client.model.ModelEvent;
import com.iris.client.model.PersonModel;
import arcus.app.R;
import arcus.app.common.events.PersonSelected;
import arcus.app.common.popups.adapter.PopupPersonListAdapter;
import arcus.app.common.utils.CorneaUtils;

import java.util.Collection;

import de.greenrobot.event.EventBus;

public class PersonPickerPopup extends ArcusFloatingFragment {
    private static final String PERSON_SELECTED = "PERSON.SELECTED";
    private static final String PERSON_LIST = "PERSON.LIST";
    private AddressableListSource<PersonModel> personModels = PersonModelProvider.instance().newModelList();

    @NonNull
    public static PersonPickerPopup newInstance(String selected, @NonNull Collection<String> personIdentifiers) {
        PersonPickerPopup personPickerPopup = new PersonPickerPopup();

        Bundle bundle = new Bundle(2);
        bundle.putString(PERSON_SELECTED, selected);
        bundle.putStringArrayList(PERSON_LIST, Lists.newArrayList(personIdentifiers));
        personPickerPopup.setArguments(bundle);

        return personPickerPopup;
    }

    public PersonPickerPopup() {}

    @Override
    public void setFloatingTitle() {
        title.setText(getString(R.string.add_person));
    }

    @Override
    public void doContentSection() {
        personModels.setAddresses(getArguments().getStringArrayList(PERSON_LIST));
        personModels.addModelListener(Listeners.runOnUiThread(new Listener<ModelEvent>() {
            @Override
            public void onEvent(ModelEvent modelEvent) {
                if (modelEvent instanceof ModelAddedEvent) {
                    logger.debug("Model added called.");
                    setAdapterAndListeners();
                }
            }
        }));
    }

    private void setAdapterAndListeners() {
        logger.debug("Received list [{}]", getArguments().getStringArrayList(PERSON_LIST));

        final ListView devicesListView = (ListView) contentView.findViewById(R.id.floating_list_view);
        final PopupPersonListAdapter adapter = new PopupPersonListAdapter(getActivity(), CorneaUtils.sortPeopleByDisplayName(personModels.get()), getPersonSelected());
        devicesListView.setAdapter(adapter);
        devicesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                adapter.onItemClick(parent, view, position, id);
                EventBus.getDefault().post(new PersonSelected(adapter.getItem(position).getAddress()));
            }
        });
    }

    @Override
    public Integer contentSectionLayout() {
        return R.layout.floating_list_picker;
    }

    @NonNull
    @Override
    public String getTitle() {
        return "";
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.floating_list_picker_fragment;
    }

    private String getPersonSelected() {
        if (getArguments() != null) {
            return getArguments().getString(PERSON_SELECTED, "");
        }

        return "";
    }
}

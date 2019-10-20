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
package arcus.cornea.subsystem.calllist;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import arcus.cornea.error.ErrorModel;
import arcus.cornea.error.Errors;
import arcus.cornea.subsystem.BaseSubsystemController;
import arcus.cornea.utils.AddressableListSource;
import arcus.cornea.utils.Listeners;
import arcus.cornea.utils.ModelSource;
import com.iris.client.ClientEvent;
import com.iris.client.ClientRequest;
import com.iris.client.bean.CallTreeEntry;
import com.iris.client.capability.Capability;
import com.iris.client.capability.Person;
import com.iris.client.event.Listener;
import com.iris.client.model.Model;
import com.iris.client.model.ModelAddedEvent;
import com.iris.client.model.ModelChangedEvent;
import com.iris.client.model.PersonModel;
import com.iris.client.model.SubsystemModel;

import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class CallListController extends BaseSubsystemController<CallListController.Callback> {
    private static final Logger logger = LoggerFactory.getLogger(CallListController.class);

    private final String callTreeEnabledAttribute;
    private final String callTreeAttribute;
    private AddressableListSource<PersonModel> persons;
    private Mode mode = Mode.VIEW;

    private final Listener<List<PersonModel>> onPersonsChanged = Listeners.runOnUiThread(new Listener<List<PersonModel>>() {
        @Override
        public void onEvent(List<PersonModel> persons) {
            onPersonsChanged(persons);
        }
    });
    private final Listener<ModelChangedEvent> onPersonChanged = Listeners.runOnUiThread(new Listener<ModelChangedEvent>() {
        @Override
        public void onEvent(ModelChangedEvent event) {
            onPersonChanged(event);
        }
    });
    private final Listener<ClientEvent> onSaved = Listeners.runOnUiThread(new Listener<ClientEvent>() {
        @Override
        public void onEvent(ClientEvent clientEvent) {

            // TODO: This is dumb. Should return an error; and error should have different error codes.
            if (clientEvent.getType().equalsIgnoreCase("base:SetAttributesError")) {
                String errorMessage = ((List<Map<String,Object>>)clientEvent.getAttribute("errors")).get(0).get("message").toString();
                if (errorMessage.contains("pin set")) {
                    onSaveFailed(new NoPinForUserException(errorMessage));
                } else {
                    onSaveFailed(new TooManyOnCallTreeException(errorMessage));
                }

            } else {
                onSaved();
            }
        }
    });
    private final Listener<Throwable> onSaveFailed = Listeners.runOnUiThread(new Listener<Throwable>() {
        @Override
        public void onEvent(Throwable cause) {
            onSaveFailed(cause);
        }
    });

    protected CallListController(
            ModelSource<SubsystemModel> subsystem,
            AddressableListSource<PersonModel> persons,
            String callTreeEnabledAttribute,
            String callTreeAttribute
    ) {
        super(subsystem);
        this.persons = persons;
        this.callTreeEnabledAttribute = callTreeEnabledAttribute;
        this.callTreeAttribute = callTreeAttribute;
    }

    public void init() {
        super.init();
        this.persons.addListener(onPersonsChanged);
        this.persons.addModelListener(onPersonChanged, ModelChangedEvent.class);
    }

    protected List<Map<String, Object>> getCallTree() {
        SubsystemModel subsystem = getModel();
        if(subsystem == null) {
            return ImmutableList.of();
        }
        return list( (Collection<Map<String, Object>>) subsystem.get(callTreeAttribute) );
    }

    protected void onPersonsChanged(List<PersonModel> persons) {
        updateView();
    }

    protected void onPersonChanged(ModelChangedEvent event) {
        if(mode == Mode.VIEW) {
            updateView();
        }
    }

    protected List<Map<String, Object>> getCallTree(Model model) {
        Object o = model.get(callTreeAttribute);
        return list((Collection<Map<String, Object>>) o);
    }

    @Override
    protected void onSubsystemLoaded(ModelAddedEvent event) {
        List<String> addresses = getCallTreeAddresses(event.getModel());
        this.persons.setAddresses(addresses);
        super.onSubsystemLoaded(event);
    }

    @Override
    protected void onSubsystemChanged(ModelChangedEvent event) {
        Set<String> keys = event.getChangedAttributes().keySet();
        if(
            keys.contains(callTreeAttribute) ||
            keys.contains(callTreeEnabledAttribute)
        ) {
            List<String> addresses = getCallTreeAddresses(event.getModel());
            this.persons.setAddresses(addresses);
            updateView();
        }
        super.onSubsystemChanged(event);
    }

    public void show() {
        mode = Mode.VIEW;
        updateView();
    }

    /**
     * Called when the user selects 'EDIT'
     */
    public void edit() {
        mode = Mode.EDIT;
        updateView();
    }

    @Override
    protected void updateView(Callback callback) {
        SubsystemModel subsystem = getModel();
        if(subsystem == null) {
            logger.debug("Unable to update view because subsystem is not loaded yet");
            return;
        }

        boolean enabled = callTreeEnabledAttribute == null || bool(subsystem.get(callTreeEnabledAttribute), false);
        if(!enabled) {
            callback.showUpgradePlanCopy();
        }

        List<PersonModel> persons = this.persons.get();
        if(persons == null) {
            logger.debug("Unable to update view because people are not loaded yet");
            return;
        }

        List<Map<String, Object>> callTree = getCallTree();
        List<CallListEntry> entries;
        if(!enabled) {
            entries = getEnabledEntries(callTree, persons);
            callback.showActiveCallList(entries);
        }
        switch(mode) {
        case VIEW:
            entries = getEnabledEntries(callTree, persons);
            callback.showActiveCallList(entries);
            break;
        case EDIT:
            entries = getAllEntries(callTree, persons);
            callback.showEditableCallList(entries);
            break;
        case SAVE:
            callback.showSaving();
            break;

        default:
            logger.warn("Unsupported call list mode {}", mode);
        }
    }

    public void save(List<CallListEntry> contacts) {
        SubsystemModel model = getModel();
        if(model == null) {
            logger.debug("Can't update model, not loaded yet");
            return;
        }

        mode = Mode.SAVE;

        ClientRequest request = new ClientRequest();
        request.setCommand(Capability.CMD_SET_ATTRIBUTES);
        request.setAttribute(callTreeAttribute, toAttributes(contacts));
        model
            .request(request)
            .onSuccess(onSaved)
            .onFailure(onSaveFailed)
            ;
        updateView();
    }

    protected void onSaved() {
        mode = Mode.VIEW;
        updateView();
    }

    protected void onSaveFailed(Throwable cause) {
        Callback callback = getCallback();
        if(callback == null) {
            return;
        }

        // assuming they didn't navigate away, go back to edit
        if(mode == Mode.SAVE) {
            mode = Mode.EDIT;
        }
        callback.showError(Errors.translate(cause));
    }

    protected List<CallListEntry> getAllEntries(List<Map<String, Object>> callTree, List<PersonModel> persons) {
        return getEntries(callTree, persons, true);
    }

    protected List<CallListEntry> getEnabledEntries(List<Map<String, Object>> callTree, List<PersonModel> persons) {
        return getEntries(callTree, persons, false);
    }

    protected List<CallListEntry> getEntries(List<Map<String, Object>> callTree, List<PersonModel> persons, boolean includeDisabled) {
        if(persons == null || persons.isEmpty()) {
            return ImmutableList.of();
        }

        List<CallListEntry> callList = new ArrayList<>(persons.size());
        for(PersonModel person: persons) {
            String id = person.getId();
            String firstName = person.getFirstName();
            String lastName = person.getLastName();

            boolean enabled = isEnabled(person.getAddress(), callTree);

            String relationship = null;
            Collection<String> tags = person.getTags();
            if (tags != null && !tags.isEmpty()) {
                relationship = tags.iterator().next();
            }
            if(includeDisabled || enabled) {
                CallListEntry entry = new CallListEntry(id, firstName, lastName, relationship, enabled);
                callList.add(entry);
            }
        }
        return callList;
    }

    protected boolean isEnabled(String address, List<Map<String, Object>> callTree) {
        if(callTree == null || callTree.isEmpty()) {
            return false;
        }

        // TODO should cache this into a map lookup
        for(Map<String, Object> entry: callTree) {
            String personAddress = (String) entry.get(CallTreeEntry.ATTR_PERSON);
            if(ObjectUtils.equals(personAddress, address)) {
                return bool(entry.get(CallTreeEntry.ATTR_ENABLED), false);
            }
        }
        return false;
    }

    protected List<Map<String, Object>> toAttributes(List<CallListEntry> callList) {
        if(callList == null || callList.isEmpty()) {
            return ImmutableList.of();
        }

        List<Map<String, Object>> attributes = new ArrayList<>(callList.size());
        for(CallListEntry entry: callList) {
            attributes.add(ImmutableMap.<String, Object>of(
                    CallTreeEntry.ATTR_PERSON, "SERV:" + Person.NAMESPACE + ":" + entry.getId(),
                    CallTreeEntry.ATTR_ENABLED, entry.isEnabled()
            ));
        }
        return attributes;
    }

    private List<String> getCallTreeAddresses(Model model) {
        List<Map<String, Object>> callTree = getCallTree(model);
        return getCallTreeAddresses(callTree);
    }

    private List<String> getCallTreeAddresses(List<Map<String,Object>> entries) {
        List<String> addrs = new ArrayList<>(entries.size());
        for(Map<String,Object> entry : entries) {
            addrs.add((String) entry.get(CallTreeEntry.ATTR_PERSON));
        }
        return addrs;
    }

    public interface Callback {

        /**
         * Indicates the view should show the text describing
         */
        void showUpgradePlanCopy();

        void showActiveCallList(List<CallListEntry> contacts);

        void showEditableCallList(List<CallListEntry> contacts);

        /**
         * This should disable the 'save' button
         */
        void showSaving();

        void showError(ErrorModel error);
    }

    private enum Mode {
        VIEW,
        EDIT,
        SAVE
    }
}
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
package arcus.app.subsystems.alarm.promonitoring.presenters;

import arcus.cornea.error.ErrorModel;
import arcus.cornea.provider.PersonModelProvider;
import arcus.cornea.subsystem.alarm.AlarmCallListController;
import arcus.cornea.subsystem.calllist.CallListController;
import arcus.cornea.subsystem.calllist.CallListEntry;
import com.iris.client.model.PersonModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;



public class AlarmCallTreePresenter extends AlarmProviderOfflinePresenter<AlarmCallTreeContract.AlarmCallListView> implements AlarmCallTreeContract.AlarmCallListPresenter, CallListController.Callback {

    private final static Logger logger = LoggerFactory.getLogger(AlarmCallTreePresenter.class);

    @Override
    public void show() {
        registerCallListControllerCallback();
        AlarmCallListController.instance().show();
    }

    @Override
    public void edit() {
        registerCallListControllerCallback();
        AlarmCallListController.instance().edit();
    }

    @Override
    public void save(List<CallListEntry> calltree, List<CallListEntry> activeEntries) {
        if (isValidCalltree(activeEntries)) {
            registerCallListControllerCallback();
            AlarmCallListController.instance().save(calltree);
        }
    }

    private void registerCallListControllerCallback() {
        String callListListenerId = AlarmCallListController.class.getCanonicalName();
        addListener(callListListenerId, AlarmCallListController.instance().setCallback(this));
    }

    @Override
    public void showSaving() {
        // Nothing to do
    }

    @Override
    public void showError(ErrorModel error) {
        getPresentedView().onError(new RuntimeException(error.getMessage()));
    }

    @Override
    public void showUpgradePlanCopy() {
        // Nothing to do; not used in pro-monitoring
    }

    @Override
    public void showActiveCallList(List<CallListEntry> contacts) {
        getPresentedView().updateView(contacts);
    }

    @Override
    public void showEditableCallList(List<CallListEntry> contacts) {
        getPresentedView().updateViewWithEditableCallTree(contacts);
    }

    private boolean isValidCalltree(List<CallListEntry> calltree) {
        if (calltree.size() > 6) {
            getPresentedView().onCallTreeTooBigError();
            return false;
        }

        for (CallListEntry thisEntry : calltree) {
            PersonModel thisPerson = PersonModelProvider.instance().getStore().get(thisEntry.getId());
            if (thisPerson == null) {
                getPresentedView().onCallTreeEntryMissingPinError(thisPerson);
                return false;
            }
        }

        return true;
    }

}

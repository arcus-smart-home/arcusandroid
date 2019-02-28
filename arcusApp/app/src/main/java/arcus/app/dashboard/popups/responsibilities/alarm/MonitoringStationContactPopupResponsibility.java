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
package arcus.app.dashboard.popups.responsibilities.alarm;

import arcus.cornea.controller.SubscriptionController;
import arcus.cornea.utils.Listeners;
import com.iris.client.IrisClientFactory;
import com.iris.client.event.Listener;
import com.iris.client.service.ProMonitoringService;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.utils.PreferenceUtils;
import arcus.app.subsystems.alarm.promonitoring.ProMonitoringAddUccContactFragment;

import java.util.ArrayList;



public class MonitoringStationContactPopupResponsibility extends AlarmResponsibility {

    @Override
    protected boolean isQualified() {
        return !PreferenceUtils.hasSeenUccContactPrompt() && SubscriptionController.isProfessional();
    }

    @Override
    protected void showPopup() {

        IrisClientFactory.getService(ProMonitoringService.class).getMetaData().onSuccess(Listeners.runOnUiThread(new Listener<ProMonitoringService.GetMetaDataResponse>() {
            @Override
            public void onEvent(ProMonitoringService.GetMetaDataResponse event) {
                PreferenceUtils.setHasSeenUccContactPrompt(true);
                ArrayList<String> monitoringStationNumbers = new ArrayList<>(event.getMetadata());
                BackstackManager.getInstance().navigateToFloatingFragment(ProMonitoringAddUccContactFragment.newInstance(monitoringStationNumbers), ProMonitoringAddUccContactFragment.class.getSimpleName(), true);
            }
        })).onFailure(new Listener<Throwable>() {
            @Override
            public void onEvent(Throwable event) {
                logger.error("Failed to retrieve UCC monitoring station phone numbers. Not prompting to add contact.");
            }
        });
    }

    @Override
    protected boolean isVisible() {
        return BackstackManager.getInstance().isFragmentOnStack(ProMonitoringAddUccContactFragment.class);
    }
}

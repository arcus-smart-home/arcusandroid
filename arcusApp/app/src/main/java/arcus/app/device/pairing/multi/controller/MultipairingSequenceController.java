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
package arcus.app.device.pairing.multi.controller;

import android.app.Activity;

import arcus.cornea.controller.SubscriptionController;
import arcus.cornea.subsystem.alarm.AlarmSubsystemController;
import arcus.cornea.utils.ProtocolTypes;

import arcus.app.common.sequence.AbstractSequenceController;
import arcus.app.common.sequence.Sequence;
import arcus.app.common.sequence.Sequenceable;
import arcus.app.common.utils.CorneaUtils;

import arcus.app.device.pairing.catalog.controller.ProductCatalogFragmentController;
import arcus.app.device.pairing.catalog.controller.ProductCatalogSequenceController;
import arcus.app.device.pairing.multi.MultipairingListFragment;
import arcus.app.device.pairing.post.ProMonitoringAlarmActivatedFragment;
import arcus.app.device.pairing.post.controller.PostPairingSequenceController;
import arcus.app.device.zwtools.controller.ZWaveNetworkRepairSequence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Map;


public class MultipairingSequenceController extends AbstractSequenceController {

    private final static Logger logger = LoggerFactory.getLogger(MultipairingSequenceController.class);

    private Sequenceable previousSequenceable;
    private boolean rulesPromoShown = false;
    private boolean pairedZWaveDevice = false;

    @Override
    public void goNext(Activity activity, Sequenceable from, Object... data) {

        if (from instanceof MultipairingListFragment) {
            String selectedDeviceName = unpackArgument(0, String.class, data);
            String selectedDeviceAddress = unpackArgument(1, String.class, data);

            navigateForward(activity, new PostPairingSequenceController(), selectedDeviceName, selectedDeviceAddress, rulesPromoShown);

            rulesPromoShown = true;
        }
    }

    @Override
    public void goBack(Activity activity, Sequenceable from, Object... data) {

        if (from instanceof MultipairingListFragment) {
            navigateBack(activity, previousSequenceable, data);
        }
    }

    @Override
    public void endSequence(Activity activity, boolean isSuccess, Object... data) {
        ProductCatalogFragmentController.instance().stopPairing();
        getPostPostPairingSequence(pairedZWaveDevice).startSequence(activity, this, data);
    }

    @Override
    public void startSequence(Activity activity, Sequenceable from, Object... data) {
        this.previousSequenceable = from;

        ArrayList<String> deviceAddresses = unpackArgument(0, ArrayList.class, data);
        pairedZWaveDevice = hasZWaveDevice(deviceAddresses);

        navigateForward(activity, MultipairingListFragment.newInstance(deviceAddresses), data);
    }

    public static Sequence getPostPostPairingSequence(boolean pairedZWaveDevice) {
        Sequence postPostPairingSequence = new Sequence();

        if (didPairingActivateNewAlarm() && SubscriptionController.isProfessional()) {
            postPostPairingSequence.add(ProMonitoringAlarmActivatedFragment.newInstance(getAlarmsActivatedByPairing()));
        }

        if (pairedZWaveDevice) {
            postPostPairingSequence.add(new ZWaveNetworkRepairSequence(ZWaveNetworkRepairSequence.SequenceVariant.SHOW_RECOMMEND_SCREEN));
        }

        return postPostPairingSequence;
    }

    private boolean hasZWaveDevice (ArrayList<String> deviceAddresses) {

        for (String thisDeviceAddress : deviceAddresses) {

            // Treat mock as zwave for testability
            if (CorneaUtils.isDeviceProtocol(thisDeviceAddress, ProtocolTypes.ZWAVE) || CorneaUtils.isDeviceProtocol(thisDeviceAddress, ProtocolTypes.MOCK)) {
                return true;
            }
        }

        return false;
    }

    private static ArrayList<String> getAlarmsActivatedByPairing() {
        ArrayList<String> activatedAlarms = new ArrayList<>();

        Map<String,Boolean> prePairingState = ProductCatalogSequenceController.getPrePairingAlarmActivations();
        Map<String,Boolean> postPairingState = AlarmSubsystemController.getInstance().getAlarmActivations();

        // Lets do some sanity checks, first...
        if (prePairingState == null || postPairingState == null) {
            logger.error("Cannot determine if pairing activated an alarm. Pre-pairing activations: {}, post-pairing activations: {}.", prePairingState, postPairingState);
            return activatedAlarms;
        }

        if (prePairingState.keySet().size() != postPairingState.keySet().size()) {
            logger.error("Bug! The set of pre-pairing alarms do not equal the set of post-pairing alarms. That just ain't right. Pre-pairing: {}, post-pairing: {}", prePairingState.keySet(), postPairingState.keySet());
        }

        // ... then walk each alarm and look for a rising edge
        for (String thisAlarm : prePairingState.keySet()) {
            boolean thisAlarmPrePairing = prePairingState.get(thisAlarm);

            if (postPairingState.containsKey(thisAlarm)) {
                boolean thisAlarmPostPairing = postPairingState.get(thisAlarm);

                if (thisAlarmPrePairing == false && thisAlarmPostPairing == true) {
                    activatedAlarms.add(thisAlarm);
                }
            }

            else {
                logger.error("Bug! Pre-pairing knows about the alarm {}, but post-pairing does not. What gives?!");
            }

        }

        return activatedAlarms;
    }

    private static boolean didPairingActivateNewAlarm() {
        return getAlarmsActivatedByPairing().size() > 0;
    }

}

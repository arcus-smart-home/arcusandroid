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
package arcus.app.device.zwtools.controller;

import android.app.Activity;

import com.iris.client.capability.HubZwave;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.models.SessionModelManager;
import arcus.app.common.sequence.AbstractSequenceController;
import arcus.app.common.sequence.Sequenceable;
import arcus.app.common.utils.CorneaUtils;
import arcus.app.dashboard.HomeFragment;
import arcus.app.device.zwtools.ZWaveNetworkRebuildRecommendedFragment;
import arcus.app.device.zwtools.ZWaveNetworkRepairFragment;
import arcus.app.device.zwtools.ZWaveNetworkRepairProgressFragment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ZWaveNetworkRepairSequence extends AbstractSequenceController {

    public enum SequenceVariant {
        SHOW_INFO_SCREEN,           // Display the "Z-Wave devices in your home link together..." screen first
        SKIP_INFO_SCREEN,           // Skip the info screen; jump straight to rebuilding / progress
        SHOW_RECOMMEND_SCREEN       // Display the network rebuild is recommended screen (post pairing)
    }

    private final static Logger logger = LoggerFactory.getLogger(ZWaveNetworkRepairSequence.class);
    private Sequenceable previousSequence;
    private SequenceVariant variant;

    public ZWaveNetworkRepairSequence(SequenceVariant variant) {
        this.variant = variant;
    }

    @Override
    public void goNext(Activity activity, Sequenceable from, Object... data) {
        if (from instanceof ZWaveNetworkRepairFragment) {
            navigateForward(activity, ZWaveNetworkRepairProgressFragment.newInstance(), data);
        }

        else if (from instanceof ZWaveNetworkRepairProgressFragment) {
            endSequence(activity, true, data);
        }

        else if (from instanceof ZWaveNetworkRebuildRecommendedFragment) {
            navigateForward(activity, ZWaveNetworkRepairProgressFragment.newInstance(), data);
        }
    }

    @Override
    public void goBack(Activity activity, Sequenceable from, Object... data) {

        if (from instanceof ZWaveNetworkRepairFragment) {
            if (previousSequence != null) {
                navigateBack(activity, previousSequence, data);
            } else {
                BackstackManager.getInstance().navigateBack();
            }
        }

        else if (from instanceof ZWaveNetworkRepairProgressFragment) {
            // Nothing to do. User can't back out of this screen; must select nav button
        }
    }

    @Override
    public void endSequence(Activity activity, boolean isSuccess, Object... data) {
        BackstackManager.getInstance().navigateBackToFragment(HomeFragment.newInstance());
    }

    @Override
    public void startSequence(Activity activity, Sequenceable from, Object... data) {

        previousSequence = from;

        if (isRepairInProgress()) {
            variant = SequenceVariant.SKIP_INFO_SCREEN;
        }

        logger.debug("Starting ZWaveNetworkRepairSequence with variant {}.", variant);

        switch (variant) {
            case SKIP_INFO_SCREEN:
                navigateForward(activity, ZWaveNetworkRepairProgressFragment.newInstance(), data);
                break;
            case SHOW_INFO_SCREEN:
                navigateForward(activity, ZWaveNetworkRepairFragment.newInstance(), data);
                break;
            case SHOW_RECOMMEND_SCREEN:
                if (CorneaUtils.isZWaveNetworkRebuildSupported()) {
                    navigateForward(activity, ZWaveNetworkRebuildRecommendedFragment.newInstance(), data);
                } else {
                    endSequence(activity, true, data);
                }
                break;
        }
    }

    public boolean isRepairInProgress () {
        HubZwave hubZwModel = CorneaUtils.getCapability(SessionModelManager.instance().getHubModel(), HubZwave.class);
        return hubZwModel != null && Boolean.TRUE.equals(hubZwModel.getHealInProgress());
    }
}

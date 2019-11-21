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
package arcus.app.subsystems.camera.controllers;

import android.content.Context;
import androidx.annotation.NonNull;

import arcus.cornea.CorneaClientFactory;
import arcus.cornea.subsystem.cameras.ClipPreviewImageGetter;
import arcus.cornea.subsystem.cameras.CameraDashboardCardController;
import arcus.cornea.subsystem.cameras.CameraPreviewGetter;
import arcus.cornea.subsystem.cameras.model.DashboardCardModel;
import com.iris.client.event.ListenerRegistration;
import arcus.app.ArcusApplication;
import arcus.app.common.cards.SimpleDividerCard;
import arcus.app.common.controller.AbstractCardController;
import arcus.app.dashboard.settings.services.ServiceCard;
import arcus.app.subsystems.camera.cards.CameraCard;
import arcus.app.subsystems.learnmore.cards.LearnMoreCard;

import java.util.UUID;


public class CameraCardController extends AbstractCardController<SimpleDividerCard> implements CameraDashboardCardController.Callback {
    private ListenerRegistration mListener;

    public CameraCardController(Context context) {
        super(context);
        showLearnMore();

        if (CorneaClientFactory.isConnected()) {
            UUID activePlace = CorneaClientFactory.getClient().getActivePlace();
            if (activePlace != null) {
                ClipPreviewImageGetter.instance().setContext(ArcusApplication.getContext(), activePlace.toString());
            }
            CameraPreviewGetter.instance().setContext(ArcusApplication.getContext());
        }
    }

    @Override
    public void setCallback(Callback delegate) {
        super.setCallback(delegate);
        mListener = CameraDashboardCardController.instance().setCallback(this);
    }

    @Override
    public SimpleDividerCard getCard() {
        return getCurrentCard();
    }

    @Override
    public void removeCallback() {
        super.removeCallback();
        mListener.remove();
    }

    @Override
    public void showLearnMore() {
        setCurrentCard(new LearnMoreCard(getContext(), ServiceCard.CAMERAS));
    }

    @Override
    public void showSummary(@NonNull DashboardCardModel model) {
        CameraCard card = new CameraCard(getContext());
        card.setLastRecordingDate(model.getLastRecording());
        card.setData(model.getCameras());
        setCurrentCard(card);
    }
}

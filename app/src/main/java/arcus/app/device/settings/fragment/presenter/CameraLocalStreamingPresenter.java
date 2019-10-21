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
package arcus.app.device.settings.fragment.presenter;

import arcus.cornea.subsystem.cameras.CameraLocalStreamingController;
import arcus.app.device.settings.fragment.contract.CameraLocalStreaming;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

public class CameraLocalStreamingPresenter implements CameraLocalStreaming.Presenter, CameraLocalStreamingController.Callback {
    Reference<CameraLocalStreaming.UserView> userViewRef = new WeakReference<>(null);
    CameraLocalStreamingController controller;

    @Override public void setView(CameraLocalStreaming.UserView view) {
        userViewRef = new WeakReference<>(view);
    }

    @Override public void getCredentials(String deviceID) {
        controller = new CameraLocalStreamingController(this);
        controller.loadCameraCredentials(deviceID);
    }

    @Override public void onSuccess(String username, String password, String ip) {
        CameraLocalStreaming.UserView useView = userViewRef.get();
        if (useView != null) {
            useView.showCredentials(username, password, ip);
        }
    }

    @Override public void onError(Throwable error) {
        CameraLocalStreaming.UserView useView = userViewRef.get();
        if (useView != null) {
            useView.showError(error);
        }
    }
}

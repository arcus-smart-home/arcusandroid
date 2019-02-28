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
package arcus.cornea.subsystem.alarm;

import arcus.cornea.subsystem.BaseSubsystemController;
import arcus.cornea.utils.Listeners;
import arcus.cornea.utils.LooperExecutor;

import com.iris.client.capability.AlarmSubsystem;
import com.iris.client.capability.Subsystem;
import com.iris.client.event.Listener;


public class AlarmSubsystemActivationController extends BaseSubsystemController<AlarmSubsystemActivationController.Callback> {

    private final static AlarmSubsystemActivationController instance = new AlarmSubsystemActivationController();

    private final Listener<Throwable> onErrorListener = Listeners.runOnUiThread(new Listener<Throwable>() {
        @Override
        public void onEvent(final Throwable throwable) {
            LooperExecutor.getMainExecutor().execute(new Runnable() {
                @Override
                public void run() {
                    Callback cb = getCallback();
                    if (cb != null) {
                        cb.onError(throwable);
                    }
                }
            });
        }
    });

    private AlarmSubsystemActivationController() {
        super(AlarmSubsystem.NAMESPACE);
        init();
    }

    public interface Callback {
        void onError(Throwable error);
        void onActivateComplete();

    }

    public static AlarmSubsystemActivationController getInstance() {
        return instance;
    }

    public void activate() {
        if(getModel() != null && !(getModel()).getAvailable()) {
            (getModel()).activate()
                    .onFailure(Listeners.runOnUiThread(onErrorListener))
                    .onSuccess(Listeners.runOnUiThread(new Listener<Subsystem.ActivateResponse>() {
                        @Override
                        public void onEvent(Subsystem.ActivateResponse response) {
                            Callback cb = getCallback();
                            if (cb != null) {
                                cb.onActivateComplete();
                            }
                        }
                    }));
        }
    }
}

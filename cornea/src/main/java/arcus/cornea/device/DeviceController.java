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
package arcus.cornea.device;

import arcus.cornea.error.ErrorModel;
import arcus.cornea.subsystem.climate.EventMessageMonitor;
import arcus.cornea.utils.Listeners;
import arcus.cornea.utils.ModelSource;
import com.iris.client.capability.Device;
import com.iris.client.event.Listener;
import com.iris.client.event.ListenerRegistration;
import com.iris.client.model.DeviceModel;
import com.iris.client.model.ModelAddedEvent;
import com.iris.client.model.ModelChangedEvent;
import com.iris.client.model.ModelEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;


public abstract class DeviceController<M> implements Listener<ModelEvent> {
    private Logger logger = LoggerFactory.getLogger(getClass());

    private WeakReference<Callback<M>> callback = new WeakReference<Callback<M>>(null);
    private ListenerRegistration modelListener;
    private ModelSource<DeviceModel> source;
    protected EventMessageMonitor eventMesssageMonitor;
    // store as a list for easy traversal
    private List<String> properties = new ArrayList<>();

    public DeviceController(ModelSource<DeviceModel> source) {
        this.source = source;
        modelListener = this.source.addModelListener(this);
        this.properties.add(Device.ATTR_NAME);
        this.source.load();
        eventMesssageMonitor = EventMessageMonitor.getInstance();
    }

    protected void listenForProperties(String... properties) {
        for(String property: properties) {
            if(!this.properties.contains(property)) {
                this.properties.add(property);
            }
        }
    }

    @Override
    public void onEvent(ModelEvent modelEvent) {
        if(modelEvent instanceof ModelAddedEvent) {
            updateView();
        }
        else if(modelEvent instanceof ModelChangedEvent) {
            Set<String> changedProperties = ((ModelChangedEvent) modelEvent).getChangedAttributes().keySet();
            for(String property: properties) {
               if(changedProperties.contains(property)) {
                   eventMesssageMonitor.removeScheduledEvent(getDevice().getId(), property);
                   updateView();
                   break;
               }
            }
        }
        else {
            // TODO what to do with deleted events?
        }
    }

    public DeviceModel getDevice() {
        return source.get();
    }

    protected Callback getCallback() {
        return this.callback.get();
    }

    protected abstract M update(DeviceModel device);

    protected void updateView() {
        Callback<M> cb = this.callback.get();
        if(cb == null) {
            clearCallback(); // make sure the listener is cleared as well
            return;
        }

        DeviceModel device = getDevice();
        if(device == null) {
            return;
        }

        try {
            M model = update(device);
            cb.show(model);
        }
        catch(Exception e) {
            logger.warn("Error updating view", e);
        }
    }

    public ListenerRegistration setCallback(Callback<M> callback) {
        if(this.callback.get() != null) {
            logger.warn("Replacing existing callback");
        }
        if(!modelListener.isRegistered()) {
            modelListener = source.addModelListener(this);
        }

        this.callback = new WeakReference<Callback<M>>(callback);
        updateView();
        return new ListenerRegistration() {
            @Override
            public boolean isRegistered() {
                return DeviceController.this.callback.get() != null;
            }

            @Override
            public boolean remove() {
                return doClear();
            }
        };
    }

    public void clearCallback() {
        doClear();
    }

    private boolean doClear() {
        // do this just in case it was cleared behind our backs
        modelListener = Listeners.clear(modelListener);
        if(callback.get() == null) {
            return false;
        }

        callback.clear();
        return true;
    }

    public interface Callback<M> {
        /**
         * Called when the model is loaded or
         * updated.
         * @param model
         */
        void show(M model);

        void onError(ErrorModel error);


    }
}

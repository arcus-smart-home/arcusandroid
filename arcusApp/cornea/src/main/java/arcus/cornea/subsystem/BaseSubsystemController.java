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
package arcus.cornea.subsystem;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import arcus.cornea.utils.Listeners;
import arcus.cornea.utils.ModelSource;
import com.iris.client.event.Listener;
import com.iris.client.event.ListenerRegistration;
import com.iris.client.model.ModelAddedEvent;
import com.iris.client.model.ModelChangedEvent;
import com.iris.client.model.ModelDeletedEvent;
import com.iris.client.model.ModelEvent;
import com.iris.client.model.SubsystemModel;

import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public abstract class BaseSubsystemController<C> {
    private static final Logger logger =
            LoggerFactory.getLogger(BaseSubsystemController.class);

    // TODO this could be parameterized pretty easilly to generic controller

    private final ModelSource<SubsystemModel> subsystem;
    private final Listener<ModelEvent> onModelEvent = Listeners.runOnUiThread(
            new Listener<ModelEvent>() {
                @Override
                public void onEvent(ModelEvent event) {
                    if(event instanceof ModelAddedEvent) {
                        onSubsystemLoaded((ModelAddedEvent) event);
                    }
                    else if(event instanceof ModelChangedEvent) {
                        onSubsystemChanged((ModelChangedEvent) event);
                    }
                    else if(event instanceof ModelDeletedEvent) {
                        onSubsystemCleared((ModelDeletedEvent) event);
                    }
                }
            }
    );
    protected WeakReference<C> callbackRef = new WeakReference<C>(null);

    protected BaseSubsystemController(String namespace) {
        this.subsystem = SubsystemController.instance().getSubsystemModel(namespace);
    }

    protected BaseSubsystemController(ModelSource<SubsystemModel> subsystem) {
        Preconditions.checkNotNull(subsystem);
        this.subsystem = subsystem;
    }

    public void init() {
        // can't call from constructor because this results in the callback being issued
        this.subsystem.addModelListener(onModelEvent);
    }

    /**
     * Called when the subsystem is loaded, default operation
     * is to invoke updateView()
     * @param event
     */
    protected void onSubsystemLoaded(ModelAddedEvent event) {
        updateView();
    }

    /**
     * Called whenever a field on the subsystem changes, default
     * operation is a no-op.
     * @param event
     */
    protected void onSubsystemChanged(ModelChangedEvent event) {
    }

    /**
     * Called when the subsystem is cleared, generally due to a
     * session invalidation event. Default operation is no-op.
     * @param event
     */
    protected void onSubsystemCleared(ModelDeletedEvent event) {
        // no-op
    }

    protected @Nullable C getCallback() {
        return callbackRef.get();
    }

    public ListenerRegistration setCallback(C callback) {
        if(callbackRef.get() != null) {
            logger.warn("Replacing existing callback");
        }
        callbackRef = new WeakReference<C>(callback);
        updateView(callback);
        return Listeners.wrap(callbackRef);
    }

    protected void clearCallback() {
        if (callbackRef != null) {
            callbackRef.clear();
        }
    }

    protected  void updateView() {
        C callback = callbackRef.get();
        if(callback != null) {
            updateView(callback);
        }
    }

    protected void updateView(C callback) {
        // no-op
    }

    protected boolean isLoaded() {
        return subsystem.isLoaded();
    }

    protected @Nullable SubsystemModel getModel() {
        subsystem.load();
        return subsystem.get();
    }

    public static int integer(@Nullable Object attribute) {
        return integer(attribute, 0);
    }

    public static int integer(@Nullable Object attribute, int dflt) {
        if(attribute == null || !(attribute instanceof  Number)) {
            return dflt;
        }

        return ((Number) attribute).intValue();
    }

    public static boolean bool(@Nullable Object attribute) {
        return bool(attribute, false);
    }

    public static boolean bool(@Nullable Object attribute, boolean dflt) {
        if(attribute == null || !(attribute instanceof  Boolean)) {
            return dflt;
        }

        return ((Boolean) attribute).booleanValue();
    }

    public static int count(@Nullable Collection<?> attribute) {
        if(attribute == null) {
            return 0;
        }
        return  attribute.size();
    }

    public static <M> Set<M> set(@Nullable Collection<M> attribute) {
        if(attribute == null || attribute.isEmpty()) {
            return ImmutableSet.of();
        }
        if(attribute instanceof  Set) {
            return (Set<M>) attribute;
        }
        return new HashSet<>(attribute);
    }

    public static <M> List<M> list(@Nullable Collection<M> attribute) {
        if(attribute == null) {
            return ImmutableList.of();
        }
        if(attribute instanceof List) {
            return (List<M>) attribute;
        }
        return new ArrayList<>(attribute);
    }

    public static String string (@Nullable Object attribute) {
        return string(attribute, "");
    }

    public static String string (@Nullable Object attribute, String dflt) {
        if (attribute == null || !(attribute instanceof String)) {
            return dflt;
        } else {
            return (String) attribute;
        }
    }

    public static Date date(Date date) {
        return date == null ? new Date() : date;
    }

}

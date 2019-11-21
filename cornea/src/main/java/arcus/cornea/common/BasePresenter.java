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
package arcus.cornea.common;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import arcus.cornea.utils.Listeners;
import com.iris.client.event.ListenerRegistration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

/**
 * Trivial implementation of an MVP presenter; manages a weak reference to the view that
 * is being presented plus controller ListenerRegistrations that the presenter may need.
 */

public class BasePresenter<PresentedViewType extends PresentedView> implements Presenter<PresentedViewType> {

    private static final Logger logger = LoggerFactory.getLogger(BasePresenter.class);
    private WeakReference<PresentedViewType> viewReference;
    private Map<String, ListenerRegistration> listeners = new HashMap<>();

    /**
     * Begin presenting the given view. Causes a weak reference to be held on the view.
     * @param view The presented view
     */
    @Override
    public void startPresenting(@Nullable PresentedViewType view) {

        if (viewReference != null && view != null) {
            viewReference.clear();
        }

        if (view != null) {
            viewReference = new WeakReference<>(view);
        }
    }

    /**
     * Call to stop presenting the view; clears the reference to presented view and releases all
     * listener registrations associated with this presenter.
     */
    @Override
    public void stopPresenting() {
        if (viewReference != null) {
            viewReference.clear();
        }

        for (ListenerRegistration thisListener : listeners.values()) {
            Listeners.clear(thisListener);
        }

        listeners.clear();
    }

    /**
     * Determines if a listener with the given ID is registered with this presenter.
     * @param id The ID of the presenter
     * @return True if a listener with this id is currently registered
     */
    public boolean hasListener(String id) {
        return listeners.keySet().contains(id);
    }

    /**
     * Adds a listener to this presenter.
     *
     * Why? A common error in this design pattern is to allow the presenter to register as a
     * listener of a subsystem (or some other entity). If the presenter is not removed as a listener
     * when the presenter is no longer needed (i.e., not "presenting" anymore), then callback
     * methods will fire even after the view that was interested in them is no longer attached (or
     * null). This leads to crashes and other weird behaviors.
     *
     * Instead, invoke this method with the ListenerRegistration returned by the controller to
     * assure it gets cleaned up once the presenter stops presenting.
     *
     * @param id An id to associate the registration with. Typically the name of the callback class.
     * @param registration The ListenerRegistration object to manage.
     */
    public void addListener(String id, ListenerRegistration registration) {

        // If this is a duplicate registration, drop the last one...
        if (hasListener(id)) {
            ListenerRegistration oldRegistration = listeners.get(id);
            Listeners.clear(oldRegistration);
            listeners.remove(id);
        }

        listeners.put(id, registration);
    }

    /**
     * @return The value of the current view reference, or null, if the view hasn't been set or has
     * been garbage collected.
     */
    @Nullable public PresentedViewType getViewReference() {
        if (viewReference == null) {
            return null;
        }

        return viewReference.get();
    }

    /**
     * @return A non-null reference to the presented view.
     * @throws IllegalArgumentException if the view has not been set or has already been garbage
     * collected.
     */
    @NonNull public PresentedViewType getPresentedView() {

        if (viewReference == null || viewReference.get() == null) {
            throw new IllegalStateException("Presented view should be set prior to invoking this method. Please call startPresenting() first.");
        }

        return viewReference.get();
    }

    /**
     * @return True if this presenter has an active view which it is presenting, false otherwise.
     */
    public boolean isPresenting() {
        return viewReference != null && viewReference.get() != null;
    }

    protected Number numberOrNull(Object thing) {
        try {
            return (Number) thing;
        }
        catch (Exception ex) {
            logger.error("Could not coerce [{}] to number.", thing);
        }

        return null;
    }
}

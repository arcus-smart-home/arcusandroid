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
package arcus.cornea.utils;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableSet;
import com.iris.client.event.Listener;
import com.iris.client.event.ListenerRegistration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.Set;
import java.util.concurrent.Executor;


public class Listeners {
    private static final Logger logger = LoggerFactory.getLogger(Listeners.class);
    private static final ListenerRegistration EMPTY = new ListenerRegistration() {
        @Override
        public boolean isRegistered() {
            return false;
        }

        @Override
        public boolean remove() {
            return false;
        }
    };

    public static ListenerRegistration empty() {
        return EMPTY;
    }

    public static ListenerRegistration weak(Object reference) {
        if(reference == null) {
            return EMPTY;
        }
        else {
            return new ReferencedListener(new WeakReference<Object>(reference));
        }
    }

    public static ListenerRegistration soft(Object reference) {
        if(reference == null) {
            return EMPTY;
        }
        else {
            return new ReferencedListener(new SoftReference<Object>(reference));
        }
    }

    public static ListenerRegistration wrap(Reference<?> reference) {
        if(reference == null) {
            return EMPTY;
        }
        else {
            return new ReferencedListener(reference);
        }
    }

    public static ListenerRegistration wrap(Reference<?> reference, Runnable onCleared) {
        if(reference == null) {
            return EMPTY;
        }
        else {
            return new ReferencedListener(reference);
        }
    }

    /**
     * Null safe registration#remove().  Also clears the reference and returns #empty().
     * Recommended usage for member variables:
     *   {@code this.registration = Listeners.clear( this.registration ); }
     * Recommended usage for ad-hoc objects:
     *   {@code Listeners.clear( registration ); }
     * @param registration
     * @return
     */
    public static ListenerRegistration clear(ListenerRegistration registration) {
        if(registration != null) {
            registration.remove();
        }
        return empty();
    }

    /**
     * Null Safe listener registration check
     *
     * @param registration
     * @return
     */
    public static boolean isRegistered(ListenerRegistration registration) {
        return registration != null && registration.isRegistered();
    }

    public static <E> Listener<E> runOnUiThread(final Listener<? super E> delegate) {
        return runOnExecutor(delegate, LooperExecutor.getMainExecutor());
    }

    public static <E> Listener<E> runOnExecutor(final Listener<? super E> delegate, final Executor executor) {
        return new Listener<E>() {
            @Override
            public void onEvent(final E event) {
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            delegate.onEvent(event);
                        }
                        catch(Exception ex) {
                            logger.warn("Error dispatching event: " + event, ex);
                        }
                    }
                });
            }
        };
    }

    public static <E> Listener<E> filter(Predicate<? super E> predicate, Listener<? super E> delegate) {
        if(delegate instanceof FilteredListener) {
            FilteredListener<E> wrapper = (FilteredListener<E>) delegate;
            return new FilteredListener<>(Predicates.and(wrapper.predicate, predicate), wrapper.delegate);
        }
        return new FilteredListener<>(predicate, delegate);
    }

    public static PropertyChangeListener filter(String property, PropertyChangeListener l) {
        return new FilteredPropertyChangeListener(ImmutableSet.of(property), l);
    }

    public static PropertyChangeListener filter(Set<String> properties, PropertyChangeListener l) {
        return new FilteredPropertyChangeListener(properties, l);
    }

    private static class ReferencedListener implements ListenerRegistration {
        private final Reference<?> reference;

        ReferencedListener(Reference < ? > reference){
            this.reference = reference;
        }

        @Override
        public boolean isRegistered () {
            return reference.get() != null;
        }

        @Override
        public boolean remove () {
            boolean registered = isRegistered();
            reference.clear();
            return registered;
        }
    }

    private static class FilteredListener<E> implements Listener<E> {
        private final Predicate<? super E> predicate;
        private final Listener<? super E> delegate;

        FilteredListener(Predicate<? super E> predicate, Listener<? super E> delegate) {
            this.predicate = predicate;
            this.delegate = delegate;
        }

        @Override
        public void onEvent(E event) {
            if(predicate.apply(event)) {
                delegate.onEvent(event);
            }
        }
    }

    private static class FilteredPropertyChangeListener implements PropertyChangeListener {
        private final Set<String> properties;
        private final PropertyChangeListener delegate;

        FilteredPropertyChangeListener(
                Set<String> properties,
                PropertyChangeListener delegate
        ) {
            this.properties = properties;
            this.delegate = delegate;
        }


        @Override
        public void propertyChange(PropertyChangeEvent event) {
            if(properties.contains(event.getPropertyName())) {
                delegate.propertyChange(event);
            }
            // else drop it
        }
    }
}

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
package arcus.app.device.ota.controller;

import java.util.HashMap;


public class ObservableMap<K,V> extends HashMap<K,V> {

    private final MapObserver listener;

    public ObservableMap(MapObserver listener) {
        this.listener = listener;
    }

    @Override
    public V put (K key, V value) {
        boolean newKey = !containsKey(key);
        V oldValue = get(key);

        super.put(key, value);

        if (oldValue != value) {
            listener.onValueChange(key, oldValue, value);
        }
        if (newKey) {
            listener.onKeyAdded(key, value);
        }

        return oldValue;
    }

    @Override
    public V remove(Object key) {
        V removed = super.remove(key);
        listener.onKeyRemoved(key);
        return removed;
    }

    public interface MapObserver<K,V> {
        void onValueChange (K key, V oldValue, V newValue);
        void onKeyAdded (K key, V value);
        void onKeyRemoved (K key);
    }
}

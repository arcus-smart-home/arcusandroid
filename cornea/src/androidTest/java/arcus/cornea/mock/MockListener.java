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
package arcus.cornea.mock;

import com.google.common.base.Optional;
import com.iris.client.event.Listener;

import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;


public class MockListener<E> implements Listener<E> {
    private Queue<E> events = new ArrayBlockingQueue<E>(100);

    public boolean hasEvents() {
        return !events.isEmpty();
    }

    public Optional<E> peek() {
        return Optional.fromNullable(events.peek());
    }

    public Optional<E> poll() {
        return Optional.fromNullable(events.poll());
    }

    @Override
    public void onEvent(E event) {
        events.add(event);
    }

}

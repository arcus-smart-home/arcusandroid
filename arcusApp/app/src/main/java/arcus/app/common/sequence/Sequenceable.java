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
package arcus.app.common.sequence;

import android.app.Activity;

/**
 * Represents an object that can exist as part of a sequence. There are two direct subtypes that
 * together form a composite pattern:
 *
 * 1. {@link SequencedFragment}: A Fragment that can exist as part of a sequence.
 * 2. {@link SequenceController}: A sequence of zero or more fragments. 
 */
public interface Sequenceable {

    void goNext(Activity activity, Sequenceable from, Object... data);
    void goBack(Activity activity, Sequenceable from, Object... data);
    void endSequence(Activity activity, boolean isSuccess, Object... data);
    void startSequence(Activity activity, Sequenceable from, Object... data);
}
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

public interface PresentedView<ModelType> {

    /**
     * Called to indicate that the presenter has started processing a long-running, asynchronous
     * request. Typically, the PresentedView should notify the user with a progress meter or similar
     * experience.
     *
     * @param progressPercent Estimated completion progress between 0 and 99 or null if progress is
     *                        indeterminate.
     */
    void onPending(@Nullable Integer progressPercent);

    /**
     * Called to indicate the presenter has failed to complete a pending request.
     * @param throwable An object describing the nature of the error.
     */
    void onError(@NonNull Throwable throwable);

    /**
     * Called to indicate the view's model has changed and that the view should be updated or
     * redrawn with data provided in the given model.
     *
     * @param model The updated model which should be rendered by the view.
     */
    void updateView(@NonNull ModelType model);
}

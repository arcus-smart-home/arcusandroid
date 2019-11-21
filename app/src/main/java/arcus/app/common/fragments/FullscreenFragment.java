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
package arcus.app.common.fragments;

import android.app.Activity;
import androidx.annotation.Nullable;

import arcus.app.common.sequence.SequenceController;
import arcus.app.common.sequence.SequencedFragment;

/**
 * A simple abstract BaseFragment that displays itself fullscreen. That is, when displayed, it hides
 * the action bar (if one is shown), and when dismissed, restores the previous action bar.
 */
public abstract class FullscreenFragment<T extends SequenceController> extends SequencedFragment<T> {

    private Boolean showActionBarOnClose;

    @Nullable
    @Override
    public String getTitle() {
        return null;
    }

    public void onAttach(Activity activity) {
        hideActionBar();

        if (showActionBarOnClose == null) {
            this.showActionBarOnClose = super.isActionBarVisible();
        }

        super.onAttach(activity);
    }

    @Override
    public void onPause() {
        super.onPause();

        if (showActionBarOnClose != null && showActionBarOnClose) {
            showActionBar();
        }
    }

    public Boolean getShowActionBarOnClose() {
        return showActionBarOnClose;
    }

    public void setShowActionBarOnClose(Boolean showActionBarOnClose) {
        this.showActionBarOnClose = showActionBarOnClose;
    }
}

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
package arcus.app.common.error.definition;

import android.graphics.Color;

import arcus.app.common.error.base.StyleablePopup;

public class DisplayedPopupError extends DisplayedError implements StyleablePopup {
    private boolean isError = true;
    private boolean isSupportLinkVisible = false;

    public DisplayedPopupError(int title, int text, boolean isSupportLinkVisible, Object... formatArgs) {
        super(title, text, formatArgs);
        this.isSupportLinkVisible = isSupportLinkVisible;
    }

    public DisplayedPopupError(int title, int text) {
        super(title, text);
    }

    public DisplayedPopupError(int title, int text, boolean isError) {
        this(title, text);
        this.isError = isError;
    }

    @Override
    public boolean isSystemDialog() { return false; }

    @Override
    public int getBackgroundColor() {
        return isError ? Color.parseColor("#EF396B") : Color.WHITE;
    }

    @Override
    public int getTextColor() {
        return isError ? Color.WHITE : Color.BLACK;
    }

    @Override
    public boolean isSupportLinkVisible () {
        return isSupportLinkVisible;
    }
}

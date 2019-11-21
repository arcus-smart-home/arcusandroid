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
package arcus.app.common.view;

import android.content.Context;
import android.graphics.PorterDuff;
import android.os.Build;
import androidx.annotation.RequiresApi;
import android.util.AttributeSet;
import android.widget.ProgressBar;



public class Version1IndeterminateProgress extends ProgressBar {

    public Version1IndeterminateProgress(Context context) {
        super(context);
        init();
    }

    public Version1IndeterminateProgress(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public Version1IndeterminateProgress(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public Version1IndeterminateProgress(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        setIndeterminate(true);
        getIndeterminateDrawable().setColorFilter(0xc0ffffff, PorterDuff.Mode.MULTIPLY);
    }
}

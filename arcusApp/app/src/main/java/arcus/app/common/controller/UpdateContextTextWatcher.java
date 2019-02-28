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
package arcus.app.common.controller;

import android.text.Editable;
import android.text.TextWatcher;

public abstract class UpdateContextTextWatcher<T> implements TextWatcher {

    private final T context;

    public UpdateContextTextWatcher(T context) {
        this.context = context;
    }

    @Override
    public final void beforeTextChanged(CharSequence s, int start, int count, int after) {
        /* no op */
    }

    @Override
    public final void onTextChanged(CharSequence s, int start, int before, int count) {
        /* no op */
    }

    @Override
    public final void afterTextChanged(Editable s) {
        updateContext(context, s);
    }

    protected abstract void updateContext(T context, Editable s);
}

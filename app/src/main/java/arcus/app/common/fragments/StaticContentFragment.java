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

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.MenuItem;

import arcus.app.R;
import arcus.app.common.sequence.SequencedFragment;


public class StaticContentFragment extends SequencedFragment {

    private static final String TITLE = "TITLE";
    private static final String LAYOUT_ID = "LAYOUT_ID";
    private static final String WITH_CLOSE = "WITH_CLOSE";

    @NonNull
    public static StaticContentFragment newInstance (String title, int layoutId, boolean withClosebox) {
        StaticContentFragment instance = new StaticContentFragment();
        Bundle arguments = new Bundle();

        arguments.putString(TITLE, title);
        arguments.putInt(LAYOUT_ID, layoutId);
        arguments.putBoolean(WITH_CLOSE, withClosebox);

        instance.setArguments(arguments);
        return instance;
    }

    @Override
    public void onResume () {
        super.onResume();
        getActivity().setTitle(getTitle());
        getActivity().invalidateOptionsMenu();
    }

    @Nullable
    @Override
    public Integer getMenuId() {
        return getArguments().getBoolean(WITH_CLOSE) ? R.menu.menu_close : null;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        endSequence(true);
        return true;
    }

    @Nullable
    @Override
    public String getTitle() {
        return getArguments().getString(TITLE);
    }

    @Override
    public Integer getLayoutId() {
        return getArguments().getInt(LAYOUT_ID);
    }
}

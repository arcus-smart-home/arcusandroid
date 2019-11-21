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
package arcus.app.subsystems.lawnandgarden.fragments;

import androidx.annotation.Nullable;
import android.view.View;

import arcus.app.R;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.fragments.BaseFragment;

public class LawnAndGardenMoreFragment extends BaseFragment implements View.OnClickListener {

    public static LawnAndGardenMoreFragment newInstance() {
        return new LawnAndGardenMoreFragment();
    }

    @Override public void onResume() {
        super.onResume();
        View rootView = getView();
        if (rootView == null) {
            return; // onCrete returned null
        }

        View lngContainer = rootView.findViewById(R.id.lng_zone_more_container);
        if (lngContainer == null) {
            return;
        }

        lngContainer.setOnClickListener(this);
    }

    @Override public void onClick(View v) {
        if (v == null) {
            return;
        }

        switch (v.getId()) {
            case R.id.lng_zone_more_container:
                BackstackManager
                      .getInstance()
                      .navigateToFragment(LawnAndGardenMoreListFragment.newInstance(), true);
                break;

            default:
                break; /* no-op */
        }
    }

    @Nullable @Override public String getTitle() {
        return null;
    }

    @Override public Integer getLayoutId() {
        return R.layout.lawn_and_garden_more_fragment;
    }
}

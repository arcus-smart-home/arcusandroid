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
package arcus.app.seasonal.christmas.fragments;

import android.support.annotation.NonNull;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import arcus.app.R;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.view.Version1ButtonColor;
import arcus.app.seasonal.christmas.model.ChristmasModel;
import arcus.app.seasonal.christmas.util.ChristmasModelUtils;
import arcus.app.common.view.Version1Button;
import arcus.app.dashboard.HomeFragment;

public class SantaComplete extends BaseChristmasFragment {

    public static SantaComplete newInstance(ChristmasModel model) {
        SantaComplete santaComplete = new SantaComplete();

        Bundle bundle = new Bundle(1);
        bundle.putSerializable(MODEL, model);
        santaComplete.setArguments(bundle);

        return santaComplete;
    }

    @Override
    public void onResume() {
        super.onResume();

        View rootView = getView();
        if (rootView == null) {
            return;
        }

        Version1Button santaCompleteButton = (Version1Button) rootView.findViewById(R.id.santa_complete_button);
        if (santaCompleteButton != null) {
            santaCompleteButton.setColorScheme(Version1ButtonColor.WHITE);
            santaCompleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    goToHomeFragment();
                }
            });
        }
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        menu.clear();

        MenuItem menuItem = menu.add("X");
        menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        menuItem.setIcon(ContextCompat.getDrawable(getActivity(), R.drawable.button_close_black_x));
        menuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                goToHomeFragment();
                return true;
            }
        });
    }

    private void goToHomeFragment() {
        ChristmasModel model = getDataModel();
        model.setupIsComplete();
        ChristmasModelUtils.cacheModelToDisk(model);

        BackstackManager.getInstance().navigateBackToFragment(HomeFragment.newInstance());
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.santa_fragment_complete;
    }
}

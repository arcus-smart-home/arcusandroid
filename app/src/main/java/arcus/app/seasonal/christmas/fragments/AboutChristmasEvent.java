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

import android.view.View;

import arcus.app.R;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.view.Version1ButtonColor;
import arcus.app.seasonal.christmas.model.ChristmasModel;
import arcus.app.common.view.Version1Button;

public class AboutChristmasEvent extends BaseChristmasFragment {

    public static AboutChristmasEvent newInstance() {
        return new AboutChristmasEvent();
    }

    @Override
    public void onResume() {
        super.onResume();

        View view = getView();
        if (view == null) {
            return;
        }

        Version1Button button = (Version1Button) view.findViewById(R.id.santa_get_started_button);
        button.setColorScheme(Version1ButtonColor.WHITE);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChristmasModel model = getDataModel();
                BackstackManager.getInstance().navigateToFragment(SantaReindeer.newInstance(model), true);
            }
        });
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.santa_fragment_start;
    }

}

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

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import arcus.app.R;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.view.Version1ButtonColor;
import arcus.app.seasonal.christmas.fragments.adapter.SantaListAdapter;
import arcus.app.seasonal.christmas.model.ChristmasModel;
import arcus.app.seasonal.christmas.util.ChristmasModelUtils;
import arcus.app.seasonal.christmas.model.SantaListItemModel;
import arcus.app.common.view.Version1Button;

import java.util.ArrayList;
import java.util.List;

public class SantaEditList extends BaseChristmasFragment {
    public static SantaEditList newInstance(ChristmasModel model) {
        SantaEditList santaEditMain = new SantaEditList();

        Bundle bundle = new Bundle(1);
        bundle.putSerializable(MODEL, model);
        santaEditMain.setArguments(bundle);

        return santaEditMain;
    }

    @Override
    public void onResume() {
        super.onResume();

        View rootView = getView();
        if (rootView == null) {
            return;
        }

        Version1Button button = (Version1Button) rootView.findViewById(R.id.santa_save_button);
        if (button != null) {
            button.setColorScheme(Version1ButtonColor.WHITE);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ChristmasModel model = getDataModel();
                    ChristmasModelUtils.cacheModelToDisk(model);
                    BackstackManager.getInstance().navigateBack();
                }
            });
        }

        ListView santaHistoryListView = (ListView) rootView.findViewById(R.id.santa_edit_list);
        if (santaHistoryListView == null) {
            return;
        }

        santaHistoryListView.setDivider(null);
        santaHistoryListView.setAdapter(new SantaListAdapter(getActivity(), createSantaListChoices()));
        santaHistoryListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ChristmasModel model = getDataModel();
                switch (position) {
                    case 0:
                        BackstackManager.getInstance().navigateToFragment(SantaReindeer.newInstance(model), true);
                        break;
                    case 1:
                        BackstackManager.getInstance().navigateToFragment(SantaContactSensors.newInstance(model), true);
                        break;
                    case 2:
                        BackstackManager.getInstance().navigateToFragment(SantaMotionSensors.newInstance(model), true);
                        break;
                    case 3:
                        BackstackManager.getInstance().navigateToFragment(SantaPictureFragment.newInstance(model), true);
                        break;
                }
            }
        });
    }

    private List<SantaListItemModel> createSantaListChoices() {
        List<SantaListItemModel> santaEditChoices = new ArrayList<>();
        SantaListItemModel reindeer = new SantaListItemModel(
              getString(R.string.santa_edit_reindeer_title),
              getString(R.string.santa_edit_reindeer_desc),
              R.drawable.icon_deer,
              false,
              true
        );
        SantaListItemModel enterExit = new SantaListItemModel(
              getString(R.string.santa_edit_enter_exit_title),
              getString(R.string.santa_edit_enter_exit_desc),
              R.drawable.icon_house,
              false,
              true
        );
        SantaListItemModel motionSensors = new SantaListItemModel(
              getString(R.string.santa_edit_motion_sensors_title),
              getString(R.string.santa_edit_motion_sensors_desc),
              R.drawable.icon_snowflake,
              false,
              true
        );
        SantaListItemModel photo = new SantaListItemModel(
              getString(R.string.santa_edit_photo_title),
              getString(R.string.santa_edit_photo_desc),
              R.drawable.icon_tree,
              false,
              true
        );
        santaEditChoices.add(reindeer);
        santaEditChoices.add(enterExit);
        santaEditChoices.add(motionSensors);
        santaEditChoices.add(photo);

        return santaEditChoices;
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.santa_fragment_edit_list;
    }
}

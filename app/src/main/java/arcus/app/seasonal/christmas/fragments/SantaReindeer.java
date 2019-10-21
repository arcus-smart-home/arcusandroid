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
import android.widget.ImageView;
import android.widget.ListView;

import arcus.app.R;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.view.Version1ButtonColor;
import arcus.app.seasonal.christmas.fragments.adapter.SantaListAdapter;
import arcus.app.seasonal.christmas.model.ChristmasModel;
import arcus.app.seasonal.christmas.model.SantaListItemModel;
import arcus.app.common.view.Version1Button;
import arcus.app.common.view.Version1TextView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class SantaReindeer extends BaseChristmasFragment {

    public static SantaReindeer newInstance(ChristmasModel model) {
        SantaReindeer reindeer = new SantaReindeer();

        Bundle bundle = new Bundle(1);
        bundle.putSerializable(MODEL, model);
        reindeer.setArguments(bundle);

        return reindeer;
    }

    @Override
    public void onResume() {
        super.onResume();
        View rootView = getView();
        if (rootView == null) {
            return;
        }

        final ImageView logo = (ImageView) rootView.findViewById(R.id.small_image);
        if (logo != null) {
            Picasso.with(getActivity())
                  .load(R.drawable.icon_deer)
                  .into(logo);
        }

        Version1TextView logoText = (Version1TextView) rootView.findViewById(R.id.small_image_text);
        if (logoText != null) {
            logoText.setText(getString(R.string.santa_reindeer_land));
        }

        final ChristmasModel model = getDataModel();
        Version1Button nextButton = (Version1Button) rootView.findViewById(R.id.santa_next_button);
        if (nextButton != null) {
            if (model.isSetupComplete()) {
                nextButton.setVisibility(View.GONE);
            }
            else {
                nextButton.setColorScheme(Version1ButtonColor.WHITE);
                nextButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        BackstackManager
                              .getInstance()
                              .navigateToFragment(SantaContactSensors.newInstance(model), true);
                    }
                });
            }
        }

        ListView santaListView = (ListView) rootView.findViewById(R.id.santa_list_choice);
        if (santaListView == null) {
            return;
        }

        santaListView.setDivider(null);
        String[] santaItems = getActivity().getResources().getStringArray(R.array.santa_landing_options);
        List<SantaListItemModel> items = new ArrayList<>();
        for (String item : santaItems) {
            boolean checked = item.equalsIgnoreCase(model.getLandingSpot(santaItems[0]));
            items.add(new SantaListItemModel(item, checked));
        }

        final SantaListAdapter santaListAdapter = new SantaListAdapter(getActivity(), items, false);
        santaListView.setAdapter(santaListAdapter);
        santaListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                santaListAdapter.toggleCheck(position);
                model.setLandingSpot(santaListAdapter.getItem(position).getName());
                saveModelToFragment(model);
            }
        });
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.santa_fragment_list_view;
    }
}

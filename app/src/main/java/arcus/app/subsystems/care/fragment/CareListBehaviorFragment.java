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
package arcus.app.subsystems.care.fragment;

import android.app.Activity;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import arcus.cornea.subsystem.care.CareBehaviorTemplateListController;
import arcus.cornea.subsystem.care.model.BehaviorTemplate;
import arcus.cornea.utils.Listeners;
import com.iris.client.event.ListenerRegistration;
import arcus.app.R;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.fragments.BaseFragment;
import arcus.app.common.image.ImageManager;
import arcus.app.common.image.Wallpaper;
import arcus.app.common.models.ListItemModel;
import arcus.app.subsystems.care.adapter.CareTemplatesListAdapter;

import java.util.ArrayList;
import java.util.List;

public class CareListBehaviorFragment extends BaseFragment implements CareBehaviorTemplateListController.Callback {
    private ListView behaviorListView;
    private ListenerRegistration listener;

    public static CareListBehaviorFragment newInstance() {
        return new CareListBehaviorFragment();
    }

    @Nullable @Override public View onCreateView(
          LayoutInflater inflater,
          @Nullable ViewGroup container,
          @Nullable Bundle savedInstanceState)
    {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        if (view == null) {
            return view;
        }

        behaviorListView = (ListView) view.findViewById(R.id.care_behavior_lv);
        return view;
    }

    @Override public void onResume() {
        super.onResume();

        Activity activity = getActivity();
        String title = getTitle();
        if (activity != null && !TextUtils.isEmpty(title)) {
            activity.setTitle(title);
            activity.invalidateOptionsMenu();

            ImageManager.with(activity).setWallpaper(Wallpaper.ofCurrentPlace().lightend());
        }

        showProgressBar();
        listener = CareBehaviorTemplateListController.instance().setCallback(this);
        CareBehaviorTemplateListController.instance().listBehaviorTemplates();
    }

    @Override public void onPause() {
        super.onPause();
        Listeners.clear(listener);
    }

    @Nullable @Override public String getTitle() {
        return getString(R.string.care_add_behavior_activity_title);
    }

    @Override public Integer getLayoutId() {
        return R.layout.care_list_behavior;
    }

    @Override public void showTemplates(List<BehaviorTemplate> satisfiable, List<BehaviorTemplate> nonSatisfiable) {
        hideProgressBar();
        if (behaviorListView == null) {
            return;
        }

        final ArrayList<ListItemModel> satisfiableBehaviors = new ArrayList<>(15);
        ListItemModel header = new ListItemModel();
        header.setText(getString(R.string.care_behavior_more_not_required));
        header.setIsHeadingRow(true);
        satisfiableBehaviors.add(header);

        for (BehaviorTemplate template : satisfiable) {
            ListItemModel model = new ListItemModel();
            model.setText(template.getName().toUpperCase());
            model.setSubText(template.getDescription());
            model.setAddress(template.getID());

            satisfiableBehaviors.add(model);
        }

        if (!nonSatisfiable.isEmpty()) {
            ListItemModel header2 = new ListItemModel();
            header2.setText(getString(R.string.care_behavior_more_required));
            header2.setIsHeadingRow(true);
            satisfiableBehaviors.add(header2);

            for (BehaviorTemplate template : nonSatisfiable) {
                ListItemModel model = new ListItemModel();
                model.setText(template.getName().toUpperCase());
                model.setSubText(template.getDescription());
                model.setAddress(template.getID());

                satisfiableBehaviors.add(model);
            }
        }

        CareTemplatesListAdapter adapter = new CareTemplatesListAdapter(getActivity());
        adapter.addAll(satisfiableBehaviors);

        behaviorListView.setDivider(null);
        behaviorListView.setAdapter(adapter);
        behaviorListView.setOnItemClickListener(null);
        behaviorListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position < 0 || position > (satisfiableBehaviors.size() - 1)) {
                    return;
                }
                if (satisfiableBehaviors.get(position).isHeadingRow()) {
                    return;
                }

                BackstackManager.getInstance().navigateToFragment(CareAddEditBehaviorFragment.newInstance(
                      satisfiableBehaviors.get(position).getAddress(),
                      satisfiableBehaviors.get(position).getSubText(),
                      false
                ), true);
            }
        });
    }
}

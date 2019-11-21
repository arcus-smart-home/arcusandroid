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
package arcus.app.subsystems.rules;

import android.os.Bundle;
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import arcus.app.R;
import arcus.app.common.fragments.BaseFragment;
import arcus.app.common.models.ListItemModel;
import arcus.app.subsystems.rules.adapters.CheckableChevronClickListener;
import arcus.app.subsystems.rules.adapters.RuleRepeatAdapter;
import arcus.app.subsystems.rules.model.Day;


public class RuleRepeatEditorFragment extends BaseFragment implements CheckableChevronClickListener {

    private ListView repeatList;
    private RuleRepeatAdapter repeatAdapter;

    @NonNull
    public static RuleRepeatEditorFragment newInstance () {
        return new RuleRepeatEditorFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        repeatList = (ListView) view.findViewById(R.id.repeat_list);
        repeatAdapter = new RuleRepeatAdapter(getActivity());

        return view;
    }

    @Override
    public void onResume () {
        super.onResume();
        getActivity().setTitle(getTitle());

        for (Day thisDay : Day.values()) {
            ListItemModel model = new ListItemModel();

            model.setText(getString(thisDay.getNameResId()));
            model.setSubText(getAbstractForDay(thisDay));
            model.setData(new Boolean(true));

            repeatAdapter.add(model);
        }

        repeatAdapter.setCheckableChevronClickListener(this);
        repeatList.setAdapter(repeatAdapter);
    }

    private String getAbstractForDay (Day day) {
        return "All day";
    }

    @Override
    public String getTitle() {
        return getString(R.string.rules_repeat);
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_rule_repeat_editor;
    }

    @Override
    public void onCheckboxRegionClicked(int position, ListItemModel item, boolean isChecked) {
        repeatAdapter.setChecked(position, !isChecked);
    }

    @Override
    public void onChevronRegionClicked(int position, ListItemModel item) {
        // TODO: Need to show time range picker
    }
}

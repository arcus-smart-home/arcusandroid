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
import android.widget.AdapterView;
import android.widget.ListView;

import com.iris.client.model.RuleTemplateModel;
import arcus.app.R;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.error.ErrorManager;
import arcus.app.common.error.type.RuleErrorType;
import arcus.app.common.fragments.BaseFragment;
import arcus.app.common.models.ListItemModel;
import arcus.app.subsystems.rules.adapters.RuleTemplatesListAdapter;
import arcus.app.subsystems.rules.model.RuleCategory;
import arcus.cornea.provider.RuleTemplateModelProvider;
import arcus.cornea.utils.Listeners;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RuleTemplateListFragment extends BaseFragment implements AdapterView.OnItemClickListener {

    private final static String SELECTED_CATEGORY_ARG = "selected-category-arg";
    private final static String SATISFIABLE_KEY = "satisfiable";
    private final static String RULE_ADDRESS_KEY = "id";
    private final static String RULE_NAME_KEY = "name";

    private final static Logger logger = LoggerFactory.getLogger(RuleTemplateListFragment.class);

    private RuleTemplatesListAdapter rulesListAdapter;

    @NonNull
    public static RuleTemplateListFragment newInstance(RuleCategory selectedCategory) {
        Bundle arguments = new Bundle();
        arguments.putSerializable(SELECTED_CATEGORY_ARG, selectedCategory);

        RuleTemplateListFragment fragment = new RuleTemplateListFragment();
        fragment.setArguments(arguments);

        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        rulesListAdapter = new RuleTemplatesListAdapter(getActivity());

        ListView rulesList = (ListView) view.findViewById(R.id.rules_list);
        rulesList.setAdapter(rulesListAdapter);
        rulesList.setOnItemClickListener(this);
        return view;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ListItemModel selectedItemModel = rulesListAdapter.getItem(position);
        Map<String,Object> data = ((Map) selectedItemModel.getData());

        if(data != null){
            String templateId = (String) data.get(RULE_ADDRESS_KEY);
            String templateName = (String) data.get(RULE_NAME_KEY);

            logger.debug("User selected rule template {}; navigating to rule editor.", templateName);

            BackstackManager.getInstance().navigateToFragment(RuleEditorFragment.newInstance(templateName, templateId), true);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        populateRules();
        getActivity().setTitle(getTitle());
        getActivity().invalidateOptionsMenu();
    }

    @Override
    public String getTitle() {
        RuleCategory category = (RuleCategory) getArguments().getSerializable(SELECTED_CATEGORY_ARG);
        return getString(category.getTitleResId());
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_rule_templates_list;
    }

    private void populateRules () {

        // Remove any existing list items (shouldn't be any, but...)
        rulesListAdapter.clear();

        RuleCategory selectedCategory = (RuleCategory) getArguments().getSerializable(SELECTED_CATEGORY_ARG);
        if (selectedCategory != null) {
            RuleTemplateModelProvider
                    .instance()
                    .getTemplatesByCategoryName(selectedCategory.getPlatformTag())
                    .onSuccess(Listeners.runOnUiThread(this::templatesLoaded));
        }
        else {
            logger.debug("Did not receive a platform tag from previous fragment. selectedCategory is NULL");
        }
    }

    @NonNull
    private ListItemModel getListItemForRuleTemplate (@NonNull RuleTemplateModel templateModel) {
        ListItemModel itemModel = new ListItemModel();

        itemModel.setText(templateModel.getName());
        itemModel.setSubText(templateModel.getDescription());

        Map<String, Object> data = new HashMap<>();
        data.put(SATISFIABLE_KEY, templateModel.getSatisfiable());
        data.put(RULE_ADDRESS_KEY, templateModel.getAddress());
        data.put(RULE_NAME_KEY, templateModel.getName());

        itemModel.setData(data);

        return itemModel;
    }

    @NonNull
    private List<RuleTemplateModel> getRuleTemplatesBySatisfiability (@NonNull List<RuleTemplateModel> ruleTemplates, boolean satisfiability) {
        List<RuleTemplateModel> selectedRules = new ArrayList<>();

        for (RuleTemplateModel thisTemplate : ruleTemplates) {
            if (thisTemplate.getSatisfiable() == satisfiability) {
                selectedRules.add(thisTemplate);
            }
        }

        return selectedRules;
    }

    public void templatesLoaded(@NonNull List<RuleTemplateModel> models) {

        if (models.size() == 0) {
            ErrorManager.in(getActivity()).show(RuleErrorType.NO_RULES);
        }

        // Partition rules by satisfiability
        List<RuleTemplateModel> satisfiableRules = getRuleTemplatesBySatisfiability(models, true);
        List<RuleTemplateModel> unsatisfiableRules = getRuleTemplatesBySatisfiability(models, false);

        if (satisfiableRules.size() > 0) {
            rulesListAdapter.add(new ListItemModel(getString(R.string.rules_recommended_for_you)));

            for (RuleTemplateModel thisSatisfiableRule : satisfiableRules) {
                rulesListAdapter.add(getListItemForRuleTemplate(thisSatisfiableRule));
            }
        }

        if (unsatisfiableRules.size() > 0) {
            rulesListAdapter.add(new ListItemModel(getString(R.string.rules_addl_devices_needed)));

            for (RuleTemplateModel thisUnsatisfiableRule : unsatisfiableRules) {
                rulesListAdapter.add(getListItemForRuleTemplate(thisUnsatisfiableRule));
            }
        }
    }
}

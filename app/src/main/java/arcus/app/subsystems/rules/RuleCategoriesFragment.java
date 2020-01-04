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

import arcus.cornea.CorneaService;
import arcus.cornea.dto.RuleCategoryCounts;
import arcus.cornea.provider.RuleTemplateModelProvider;
import arcus.cornea.utils.Listeners;
import arcus.app.R;
import arcus.app.account.settings.WalkthroughType;
import arcus.app.account.settings.walkthroughs.WalkthroughBaseFragment;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.error.ErrorManager;
import arcus.app.common.fragments.BaseFragment;
import arcus.app.common.fragments.IShowedFragment;
import arcus.app.common.models.ListItemModel;
import arcus.app.common.utils.PreferenceCache;
import arcus.app.common.utils.PreferenceUtils;
import arcus.app.subsystems.rules.adapters.RuleCategoriesListAdapter;
import arcus.app.subsystems.rules.model.RuleCategory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class RuleCategoriesFragment extends BaseFragment implements IShowedFragment {

    private ListView categoriesList;
    private RuleCategoriesListAdapter categoriesListAdapter;
    private ArrayList<ListItemModel> categoriesArrayList = new ArrayList<>();
    private boolean alreadyShown = false;

    @NonNull
    public static RuleCategoriesFragment newInstance() {
        return new RuleCategoriesFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        categoriesList = (ListView) view.findViewById(R.id.rules_categories_list);
        categoriesListAdapter = new RuleCategoriesListAdapter(getActivity());

        categoriesList.setAdapter(categoriesListAdapter);
        categoriesList.setOnItemClickListener((parent, view1, position, id) -> {
            ListItemModel selectedItem = (ListItemModel) categoriesList.getAdapter().getItem(position);
            RuleCategory selectedCategory = (RuleCategory) selectedItem.getData();

            BackstackManager.getInstance().navigateToFragment(RuleTemplateListFragment.newInstance(selectedCategory), true);
        });

        populateRulesCategories();

        return view;
    }


    @Override
    public void onResume() {
        super.onResume();
        if (!PreferenceCache.getInstance().getBoolean(PreferenceUtils.RULES_WALKTHROUGH_DONT_SHOW_AGAIN, false) && !alreadyShown) {
            WalkthroughBaseFragment rules = WalkthroughBaseFragment.newInstance(WalkthroughType.RULES);
            BackstackManager.getInstance().navigateToFloatingFragment(rules, rules.getClass().getName(), true);
            alreadyShown = true;
        }
        getActivity().setTitle(getTitle());
        getActivity().invalidateOptionsMenu();
    }

    @NonNull
    @Override
    public String getTitle() {
        return getResources().getString(R.string.rules_add_a_rule);
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_rules_categories;
    }

    private void populateRulesCategories() {
        categoriesArrayList.clear();

        CorneaService corneaService = getCorneaService();
        if (corneaService == null) {
            return; // Fabric #199; Need to move Cornea away from a Service.
        }

        RuleTemplateModelProvider.instance().getRuleCategoryCounts().onSuccess(Listeners.runOnUiThread(ruleCategoryCounts -> {

            for (RuleCategoryCounts.RuleCountInstance ruleCount : ruleCategoryCounts.getCategoryList()) {
                ListItemModel categoryItem = new ListItemModel();

                // Skip this category if it has no rules defined
                if (ruleCount.getCount() == 0) {
                    continue;
                }

                // Build the rule(s) count string
                StringBuilder ruleCountSubtext = new StringBuilder();
                ruleCountSubtext.append(ruleCount.getCount());
                ruleCountSubtext.append(" ");

                if (ruleCount.getCount() == 1) {
                    ruleCountSubtext.append(getString(R.string.rules_rule_singular));
                } else {
                    ruleCountSubtext.append(getString(R.string.rules_rule_plural));
                }
                categoryItem.setSubText(ruleCountSubtext.toString());

                RuleCategory ruleCategory = RuleCategory.fromPlatformTag(ruleCount.getName());
                categoryItem.setData(ruleCategory);
                categoryItem.setText(getString(ruleCategory.getTitleResId()));
                categoryItem.setImageResId(ruleCategory.getImageResId());

                categoriesArrayList.add(categoryItem);
            }

            Collections.sort(categoriesArrayList, alphaOrder);
            categoriesListAdapter.addAll(categoriesArrayList);
            categoriesListAdapter.notifyDataSetChanged();
        })).onFailure(throwable -> ErrorManager.in(getActivity()).showGenericBecauseOf(throwable));
    }

    private static final Comparator<ListItemModel> alphaOrder = (lhs, rhs) -> {
        try {
            return lhs.getText().compareToIgnoreCase(rhs.getText());
        } catch (Exception ex) {
            return lhs.getText().compareToIgnoreCase(rhs.getText());
        }
    };

    @Override
    public void onShowedFragment() {
        getActivity().setTitle(getTitle());
    }

}

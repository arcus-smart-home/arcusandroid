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

import android.app.Activity;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;

import arcus.cornea.RulesScheduleStateController;
import arcus.cornea.SessionController;
import arcus.cornea.error.ErrorModel;
import arcus.cornea.rules.RuleListingController;
import arcus.cornea.rules.model.RuleDeviceSection;
import arcus.cornea.rules.model.RuleProxyModel;
import arcus.cornea.utils.Listeners;
import com.iris.client.event.ListenerRegistration;
import com.iris.client.exception.ErrorResponseException;
import com.iris.client.model.SchedulerModel;
import arcus.app.R;
import arcus.app.account.settings.data.WalkthroughType;
import arcus.app.account.settings.walkthroughs.WalkthroughBaseFragment;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.error.ErrorManager;
import arcus.app.common.fragments.BaseFragment;
import arcus.app.common.image.ImageManager;
import arcus.app.common.image.Wallpaper;
import arcus.app.common.models.ListItemModel;
import arcus.app.common.popups.AlertPopup;
import arcus.app.common.utils.PreferenceCache;
import arcus.app.common.utils.PreferenceUtils;
import arcus.app.subsystems.rules.adapters.CheckableChevronClickListener;
import arcus.app.subsystems.rules.adapters.RuleListAdapter;

import java.util.List;
import java.util.Map;


public class RuleListFragment
      extends BaseFragment
      implements RuleListingController.Callback, RulesScheduleStateController.Callback, CheckableChevronClickListener {

    private RuleListAdapter ruleListAdapter;
    private RuleListingController ruleListingController;
    private ListenerRegistration ruleListingReg;
    private boolean isEditMode = false;

    private LinearLayout noRulesLayout;
    private LinearLayout rulesLayout;
    private ListView rulesList;

    @NonNull public static RuleListFragment newInstance() {
        return new RuleListFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        noRulesLayout = (LinearLayout) view.findViewById(R.id.no_rules_layout);
        rulesLayout = (LinearLayout) view.findViewById(R.id.rules_layout);
        rulesList = (ListView) view.findViewById(R.id.rules_list);

        rulesList.setAdapter(ruleListAdapter);
        return view;
    }

    private void initializeNoRulesView() {
        noRulesLayout.setVisibility(View.VISIBLE);
        rulesLayout.setVisibility(View.GONE);
        updateEditMenu();
    }

    private void initializeRulesViewWithSections(@NonNull Map<String, RuleDeviceSection> mapList) {
        noRulesLayout.setVisibility(View.GONE);
        rulesLayout.setVisibility(View.VISIBLE);

        ruleListAdapter.clear();
        ruleListAdapter.setListener(this);

        int totalRules = 0;
        for (String key : mapList.keySet()) {
            RuleDeviceSection ruleDeviceSection = mapList.get(key);

            ListItemModel sectionHeader = new ListItemModel();
            sectionHeader.setIsHeadingRow(true);
            sectionHeader.setText(key);
            sectionHeader.setCount(ruleDeviceSection.getRules().size());
            ruleListAdapter.add(sectionHeader);

            ruleDeviceSection.sortRules(false);
            List<RuleProxyModel> ruleModelList = ruleDeviceSection.getRules();
            totalRules += ruleModelList.size();

            for (RuleProxyModel thisRule : ruleModelList) {
                ListItemModel myListItemModel = new ListItemModel();
                myListItemModel.setText(thisRule.getName());
                myListItemModel.setAddress(thisRule.getAddress());
                myListItemModel.setSubText(thisRule.getDescription());
                myListItemModel.setData(thisRule);
                myListItemModel.setChecked(thisRule.isEnabled());
                ruleListAdapter.add(myListItemModel);
            }
        }

        logger.debug("Displaying [{}] Categories, with [{}] rules (Duplicates possible)", mapList.size(), totalRules);
        ruleListAdapter.notifyDataSetChanged();
        updateEditMenu();
    }

    @Override public void onResume() {
        super.onResume();
        if (!PreferenceCache.getInstance().getBoolean(PreferenceUtils.RULES_WALKTHROUGH_DONT_SHOW_AGAIN, false)) {
            WalkthroughBaseFragment rules = WalkthroughBaseFragment.newInstance(WalkthroughType.RULES);
            BackstackManager.getInstance().navigateToFloatingFragment(rules, rules.getClass().getName(), true);
        }

        setTitle();
        ImageManager.with(getActivity()).setWallpaper(Wallpaper.ofCurrentPlace().darkened());

        if (ruleListingController == null || !Listeners.isRegistered(ruleListingReg)) {
            ruleListingController = new RuleListingController();
            ruleListingReg = ruleListingController.setCallback(this);
            if(ruleListAdapter == null) {
                ruleListAdapter = new RuleListAdapter(getActivity());
                rulesList.setAdapter(ruleListAdapter);
                showProgressBar();
            }

            ruleListingController.listAllRules();
            RulesScheduleStateController.instance().fetchAllSchedules(SessionController.instance().getPlaceIdOrEmpty(), this);
        }
    }

    @Override public void onPause() {
        super.onPause();
        ruleListingController = null;
        Listeners.clear(ruleListingReg);
    }

    @Override public void onDestroy() {
        super.onDestroy();
        hideProgressBar();
    }

    @Override public String getTitle() {
        return getString(R.string.rules_rules);
    }

    @Override public Integer getLayoutId() {
        return R.layout.fragment_rule_list;
    }

    @Override public void onCheckboxRegionClicked(int position, @NonNull final ListItemModel item, boolean isChecked) {
        if (ruleListingController == null) {
            logger.error("Lost reference to the rule listing controller.");
            return; // If this is null we can't do anything below....
        }

        RuleProxyModel rule = (RuleProxyModel) item.getData();
        if (isEditMode) { // In edit mode; delete rule
            ruleListingController.deleteRule(rule);
        }
        else { // Not in edit mode; enable or disable the rule accordingly
            isChecked = !isChecked;
            item.setChecked(isChecked);
            rule.setEnabled(isChecked);

            // FIXME: (eanderso) 7/11/16 Do we want to show progress here or just let the view update knowing the call has been made?
            ruleListingController.updateRuleEnabled(rule);
        }

        // Refresh list data
        ruleListAdapter.notifyDataSetChanged();
    }

    @Override public void onChevronRegionClicked(int position, @NonNull ListItemModel item) {
        if (!isEditMode) {
            String templateId = ((RuleProxyModel) item.getData()).getTemplate();
            String ruleAddress = ((RuleProxyModel) item.getData()).getAddress();
            BackstackManager.getInstance().navigateToFragment(
                  RuleEditorWithSchedulerFragment.newInstance(item.getText(), templateId, ruleAddress), true
            );
        }
    }

    @Nullable @Override public Integer getMenuId() {
        return (rulesLayout.getVisibility() == View.VISIBLE) ? R.menu.menu_edit_done_toggle : null;
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.menu_edit_done);
        if(item != null) {
            item.setTitle(isEditMode ? getResources().getString(R.string.card_menu_done) : getResources().getString(R.string.card_menu_edit));
        }
        super.onPrepareOptionsMenu(menu);
    }

    @Override public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        isEditMode = !isEditMode;
        ruleListAdapter.setEditMode(isEditMode);
        item.setTitle(isEditMode ? getResources().getString(R.string.card_menu_done) : getResources().getString(R.string.card_menu_edit));
        return true;
    }

    @Override public void showScheduleStates(Map<String, SchedulerModel> schedulers) {
        if (ruleListAdapter != null && (schedulers != null && !schedulers.isEmpty())) {
            ruleListAdapter.setSchedules(schedulers);
        }
    }

    @Override public void onError(@NonNull Throwable throwable) {
        // FIXME: (eanderso) 7/11/16 How do we want to handle errors?
        hideProgressBar();

        if(throwable instanceof ErrorResponseException && "request.invalid".equals(((ErrorResponseException) throwable).getCode())) {
            AlertPopup popup = AlertPopup.newInstance("",
                    getString(R.string.rules_cannot_enable_error), null, null, new AlertPopup.AlertButtonCallback() {
                        @Override
                        public boolean topAlertButtonClicked() {
                            return false;
                        }

                        @Override
                        public boolean bottomAlertButtonClicked() {
                            return false;
                        }

                        @Override
                        public boolean errorButtonClicked() {
                            return false;
                        }

                        @Override
                        public void close() {
                            BackstackManager.getInstance().navigateBack();
                            showProgressBar();
                            ruleListingController.listAllRules();
                            RulesScheduleStateController.instance().fetchAllSchedules(SessionController.instance().getPlaceIdOrEmpty(), RuleListFragment.this);
                        }
                    });
            popup.setCloseButtonVisible(true);
            BackstackManager.getInstance().navigateToFloatingFragment(popup, popup.getClass().getSimpleName(), true);
        }
        else {
            ErrorManager.in(getActivity()).showGenericBecauseOf(throwable);
        }

    }

    @Override public void onRulesLoaded(@NonNull Map<String, RuleDeviceSection> rules) {
        hideProgressBar();
        //TODO: figure out how to set the title for the edit/done button
        if (rules.isEmpty()) {
            initializeNoRulesView();
        }
        else {
            initializeRulesViewWithSections(rules);
        }
    }

    @Override public void onError(ErrorModel error) {
        hideProgressBar();
        ErrorManager.in(getActivity()).showGenericBecauseOf(new RuntimeException(error.getMessage()));
    }

    protected void updateEditMenu() {
        Activity activity = getActivity();
        if (activity != null) {
            ActivityCompat.invalidateOptionsMenu(activity);
        }
    }
}

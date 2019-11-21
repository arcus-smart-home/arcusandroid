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
import android.widget.LinearLayout;
import arcus.app.R;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.error.ErrorManager;
import arcus.app.dashboard.AddMenuFragment;
import arcus.app.subsystems.rules.schedule.RulesWeeklyScheduleFragment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class RuleEditorWithSchedulerFragment extends RuleEditorFragment {


    private static final Logger logger = LoggerFactory.getLogger(RuleEditorWithSchedulerFragment.class);


    private LinearLayout  scheduleRuleLayout;


    @NonNull
    public static RuleEditorWithSchedulerFragment newInstance (String ruleTemplateName, String ruleTemplateId) {
        RuleEditorWithSchedulerFragment instance = new RuleEditorWithSchedulerFragment();
        Bundle arguments = new Bundle();
        arguments.putString(RULE_ID_ARG, ruleTemplateId);
        arguments.putString(RULE_NAME_ARG, ruleTemplateName);
        instance.setArguments(arguments);

        return instance;
    }

    @NonNull
    public static RuleEditorWithSchedulerFragment newInstance (String ruleTemplateName, String ruleTemplateId, String existingRuleAddress) {
        RuleEditorWithSchedulerFragment instance = new RuleEditorWithSchedulerFragment();
        Bundle arguments = new Bundle();
        arguments.putString(RULE_ID_ARG, ruleTemplateId);
        arguments.putString(RULE_NAME_ARG, ruleTemplateName);
        arguments.putString(EXISTING_RULE_ADDRESS, existingRuleAddress);
        instance.setArguments(arguments);

        return instance;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        scheduleRuleLayout = (LinearLayout) view.findViewById(R.id.rule_scheduling_layout);


        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        scheduleRuleLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BackstackManager.getInstance()
                        .navigateToFragment(RulesWeeklyScheduleFragment.newInstance(controller.getAddressableModelSource().getAddress(), getTitle()),true);
            }
        });
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_rule_editor_scheduling;
    }


    @Override
    public void saveSuccess() {
        hideProgressBar();

        if (BackstackManager.getInstance().isFragmentOnStack(AddMenuFragment.class)) {
            BackstackManager.getInstance().navigateBackToFragment(AddMenuFragment.class);
        }

        else {
            BackstackManager.getInstance().navigateBack();
        }
    }

    @Override
    public void errorOccurred(Throwable throwable) {
        hideProgressBar();
        ErrorManager.in(getActivity()).showGenericBecauseOf(throwable);
    }



}

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

import arcus.cornea.controller.RuleEditorController;
import arcus.app.R;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.fragments.BaseFragment;
import arcus.app.common.view.Version1Button;
import arcus.app.common.view.Version1EditText;


public class RuleNameEditorFragment extends BaseFragment {

    private final static int MAX_NAME_LENGTH_CHARS = 36;
    private final static String ARG_RULES_NAME = "rule-name";

    private Version1EditText ruleName;
    private Version1Button saveButton;

    @NonNull
    public static RuleNameEditorFragment newInstance (String ruleName) {

        RuleNameEditorFragment instance = new RuleNameEditorFragment();

        Bundle arguments = new Bundle();
        arguments.putString(ARG_RULES_NAME, ruleName);
        instance.setArguments(arguments);

        return instance;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        ruleName = (Version1EditText) view.findViewById(R.id.rule_name);
        saveButton = (Version1Button) view.findViewById(R.id.save_button);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        ruleName.setMaxCharacters(MAX_NAME_LENGTH_CHARS);
        ruleName.setText(getArguments().getString(ARG_RULES_NAME));

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isEditedNameValid()) {
                    RuleEditorController.getInstance().setTitle(ruleName.getText().toString());
                    BackstackManager.getInstance().navigateBack();
                }
            }
        });
    }

    public boolean isEditedNameValid () {

        if (ruleName.getText().length() > MAX_NAME_LENGTH_CHARS) {
            ruleName.setError(getString(R.string.rules_36_char_limit));
            return false;
        }

        return true;
    }

    @Override
    public String getTitle() {
        return getString(R.string.rules_edit_rule_name);
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_rule_name_editor;
    }
}

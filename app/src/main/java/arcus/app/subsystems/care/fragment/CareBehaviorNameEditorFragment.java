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
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import arcus.cornea.subsystem.care.CareBehaviorController;
import arcus.cornea.subsystem.care.model.CareBehaviorModel;
import arcus.cornea.subsystem.care.model.CareBehaviorTemplateModel;
import arcus.app.R;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.error.ErrorManager;
import arcus.app.common.fragments.BaseFragment;
import arcus.app.common.utils.StringUtils;
import arcus.app.common.view.Version1Button;
import arcus.app.common.view.Version1ButtonColor;
import arcus.app.common.view.Version1EditText;

public class CareBehaviorNameEditorFragment extends BaseFragment implements CareBehaviorController.Callback {
    private static final String ID      = "ID";
    private static final String IS_EDIT = "IS_EDIT";
    private static final Integer MAX_LENGTH_NAME = 15;

    private Version1EditText careBehaviorName;
    private Version1Button careBehaviorSaveButton;

    public static CareBehaviorNameEditorFragment newInstance(@NonNull String id, boolean isEditMode) {
        CareBehaviorNameEditorFragment fragment = new CareBehaviorNameEditorFragment();
        Bundle args = new Bundle();

        args.putString(ID, id);
        args.putBoolean(IS_EDIT, isEditMode);

        fragment.setArguments(args);
        return fragment;
    }

    @Override public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        if (view != null) {
            careBehaviorName = (Version1EditText) view.findViewById(R.id.care_behavior_name);
            careBehaviorSaveButton = (Version1Button) view.findViewById(R.id.care_behavior_name_save_button);
        }
        return view;
    }

    @Nullable @Override public String getTitle() {
        return null;
    }

    @Override public void onResume () {
        super.onResume();

        boolean isEditMode = false;
        String templateAddress = StringUtils.EMPTY_STRING;
        if (getArguments() != null) {
            templateAddress = getArguments().getString(ID, StringUtils.EMPTY_STRING);
            isEditMode = getArguments().getBoolean(IS_EDIT, false);
        }

        careBehaviorName.useLightColorScheme(isEditMode);
        careBehaviorSaveButton.setColorScheme(isEditMode ? Version1ButtonColor.WHITE : Version1ButtonColor.BLACK);

        CareBehaviorController.instance().setCallback(this);
        if (!TextUtils.isEmpty(templateAddress)) {
            if (isEditMode) {
                CareBehaviorController.instance().editExistingBehaviorByID(templateAddress);
            }
            else {
                CareBehaviorController.instance().addBehaviorByTemplateID(templateAddress);
            }
        }
    }

    @Override public Integer getLayoutId() {
        return R.layout.fragment_care_behavior_name_editor;
    }

    @Override public void unsupportedTemplate() {
        hideProgressBar();
    }

    @Override public void onError(final Throwable cause) {
        hideProgressBar();
        ErrorManager.in(getActivity()).showGenericBecauseOf(cause);
    }

    @Override public void editTemplate(final CareBehaviorModel editingModel, final CareBehaviorTemplateModel templateModel) {
        String title = editingModel.getName();
        if (TextUtils.isEmpty(title)) {
            title = StringUtils.EMPTY_STRING;
        }

        careBehaviorName.setText(title);
        Activity activity = getActivity();
        if (activity != null) {
            activity.invalidateOptionsMenu();
            if (title.length() > MAX_LENGTH_NAME) {
                title = String.format("%s...", title.substring(0, MAX_LENGTH_NAME - 1));
            }
            activity.setTitle(title);
        }

        careBehaviorSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(careBehaviorName.getText())) {
                    editingModel.setName(careBehaviorName.getText().toString());
                    BackstackManager.getInstance().navigateBack();
                }
                else {
                    careBehaviorName.setError(getString(R.string.care_behavior_edit_name_error, careBehaviorName.getHint()));
                }
            }
        });
    }
}

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
package arcus.app.account.settings;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import arcus.cornea.provider.ProMonitoringSettingsProvider;
import arcus.cornea.utils.Listeners;
import com.iris.client.ClientEvent;
import com.iris.client.event.Listener;
import com.iris.client.model.ProMonitoringSettingsModel;
import arcus.app.R;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.error.ErrorManager;
import arcus.app.common.fragments.BaseFragment;



public class ProMonSettingsFirstResponderFragment extends BaseFragment {
    public static final String PLACE_ID = "PLACE_ID";
    private String viewingPlaceID, instructions;
    private EditText instructionsEditText;
    private TextView characterCounterText;
    private Button saveInstructionsButton;
    private ProMonitoringSettingsModel proMonSettingsModel;

    public static arcus.app.account.settings.ProMonSettingsFirstResponderFragment newInstance(@NonNull String placeID) {
        arcus.app.account.settings.ProMonSettingsFirstResponderFragment fragment =
                new arcus.app.account.settings.ProMonSettingsFirstResponderFragment();
        Bundle args = new Bundle(1);
        args.putString(PLACE_ID, placeID);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = super.onCreateView(inflater, container, savedInstanceState);
        viewingPlaceID = getArguments().getString(PLACE_ID);
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        View rootView = getView();

        ProMonitoringSettingsProvider.getInstance().getProMonSettings(viewingPlaceID).onSuccess(Listeners.runOnUiThread(new Listener<ProMonitoringSettingsModel>() {
            @Override
            public void onEvent(ProMonitoringSettingsModel model) {
                proMonSettingsModel = model;
                instructions = proMonSettingsModel.getInstructions();
                instructionsEditText.setText(instructions);
            }
        }));

        characterCounterText = (TextView) rootView.findViewById(R.id.settings_promon_first_responder_char_limit_text);

        instructionsEditText = (EditText) rootView.findViewById(R.id.settings_promon_first_responder_edit);
        instructionsEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String charCount =
                        String.format(getResources().getString(R.string.settings_promon_first_responder_char_limit_text), s.length());
                characterCounterText.setText(charCount);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        saveInstructionsButton = (Button) rootView.findViewById(R.id.settings_promon_first_responder_save_button);
        saveInstructionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                proMonSettingsModel.setInstructions(instructionsEditText.getText().toString());
                proMonSettingsModel.commit().onFailure(new Listener<Throwable>() {
                                                           @Override
                                                           public void onEvent(Throwable event) {
                                                               ErrorManager.in(getActivity()).showGenericBecauseOf(event);
                                                           }
                                                       }
                ).onSuccess(new Listener<ClientEvent>() {
                    @Override
                    public void onEvent(ClientEvent event) {
                        BackstackManager.getInstance().navigateBack();
                    }
                });
            }
        });

        setTitle();
    }

    @Nullable
    @Override
    public String getTitle() {
        return getString(R.string.settings_promon_first_responder_title);
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_promon_settings_first_responder;
    }
}

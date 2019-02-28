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
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
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
import arcus.app.common.utils.StringUtils;



public class ProMonSettingsGateAccessFragment extends BaseFragment {
    public static final String PLACE_ID = "PLACE_ID";
    private String viewingPlaceID, gateAccessCode;
    private EditText gateAccessCodeEditText;
    private Button saveButton;
    private ProMonitoringSettingsModel proMonSettingsModel;

    public static ProMonSettingsGateAccessFragment newInstance(@NonNull String placeID) {
        ProMonSettingsGateAccessFragment fragment = new ProMonSettingsGateAccessFragment();
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
                gateAccessCode = proMonSettingsModel.getGateCode();
                if (!StringUtils.isEmpty(gateAccessCode)) {
                    gateAccessCodeEditText.setText(gateAccessCode);
                } else {
                    gateAccessCodeEditText.setHint(getResources().getString(R.string.settings_promon_gate_access_view_edit_hint));
                }
            }
        }));

        gateAccessCodeEditText = (EditText) rootView.findViewById(R.id.settings_promon_gate_access_view_edit);
        gateAccessCodeEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_DONE && event == null) {
                    saveValueAndReturnToPreviousFragment(gateAccessCodeEditText.getText().toString());
                }
                return false;
            }
        });

        saveButton = (Button) rootView.findViewById(R.id.settings_promon_gate_access_save_button);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveValueAndReturnToPreviousFragment(gateAccessCodeEditText.getText().toString());
            }
        });

        setTitle();
    }


    @Nullable
    @Override
    public String getTitle() {
        return getString(R.string.settings_promon_gate_access_title);
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_promon_settings_gate_access;
    }

    private void saveValueAndReturnToPreviousFragment(String value) {
        proMonSettingsModel.setGateCode(gateAccessCodeEditText.getText().toString());
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
}

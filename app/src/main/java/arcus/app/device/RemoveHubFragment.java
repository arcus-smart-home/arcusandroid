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
package arcus.app.device;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import arcus.cornea.dto.HubDeviceModelDTO;

import arcus.cornea.utils.Listeners;
import com.iris.client.capability.Hub;
import com.iris.client.event.Listener;
import com.iris.client.model.HubModel;
import arcus.app.R;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.error.ErrorManager;
import arcus.app.common.fragments.BaseFragment;
import arcus.app.common.models.SessionModelManager;
import arcus.app.common.popups.AlertPopup;
import arcus.app.common.view.Version1Button;
import arcus.app.common.view.Version1ButtonColor;
import arcus.app.common.view.Version1EditText;
import arcus.app.dashboard.HomeFragment;



public class RemoveHubFragment extends BaseFragment {
    private final static String REMOVE_TEXT = "REMOVE";

    @Nullable
    private HubModel hubModel;

    private Version1EditText removeEditText;
    private Version1Button submitButton;

    @NonNull
    public static RemoveHubFragment newInstance() {
        return new RemoveHubFragment();
    }

    @Override
    public void onResume() {
        super.onResume();
        View rootView = getView();

        removeEditText = rootView.findViewById(R.id.remove_text_entry);
        if (SessionModelManager.instance().getHubModel() != null) {
            hubModel = new HubDeviceModelDTO(SessionModelManager.instance().getHubModel());

            removeEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    submitButton.setEnabled(REMOVE_TEXT.equalsIgnoreCase(removeEditText.getText().toString()));
                }
            });
        }

        submitButton = (Version1Button) rootView.findViewById(R.id.fragment_submit_button);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {removeHub(); }
        });
        submitButton.setEnabled(false);

        setTitle();
    }

    @Nullable
    @Override
    public String getTitle() {
        return getString(R.string.remove_hub_title);
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_remove_hub;
    }

    private void removeHub() {

        AlertPopup removeHubAlert = AlertPopup.newInstance(
                getString(R.string.device_remove_are_you_sure),
                getString(R.string.remove_hub_popup_description),
                Version1ButtonColor.MAGENTA,
                getString(R.string.remove_hub_popup_confirm_button_text),
                getString(R.string.cancel),
                AlertPopup.ColorStyle.WHITE,
                new AlertPopup.AlertButtonCallback() {

                    @Override
                    public boolean topAlertButtonClicked() {

                        // Removing the hub
                        BackstackManager.getInstance().navigateBack();
                        hubModel.delete()
                                .onSuccess(Listeners.runOnUiThread(new Listener<Hub.DeleteResponse>() {
                                    @Override
                                    public void onEvent(Hub.DeleteResponse deleteResponse) {
                                        BackstackManager.getInstance().navigateToFragment(HomeFragment.newInstance(), true);
                                    }
                                }))
                                .onFailure(Listeners.runOnUiThread(new Listener<Throwable>() {
                                    @Override
                                    public void onEvent(Throwable throwable) {
                                        ErrorManager.in(getActivity()).showGenericBecauseOf(throwable);
                                    }
                                }));

                        return false;
                    }

                    @Override
                    public boolean bottomAlertButtonClicked() {
                        return true;
                    }

                    @Override
                    public boolean errorButtonClicked() {
                        return false;
                    }

                    @Override
                    public void close() {
                        BackstackManager.getInstance().navigateBack();
                    }
                }
        );

        removeHubAlert.setCloseButtonVisible(true);

        BackstackManager.getInstance().navigateToFloatingFragment(removeHubAlert,
                removeHubAlert.getClass().getSimpleName(), true);
    }


}

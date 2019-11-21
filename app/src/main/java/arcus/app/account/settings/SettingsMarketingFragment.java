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
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ToggleButton;

import arcus.cornea.SessionController;
import arcus.cornea.utils.Listeners;
import com.iris.client.ClientEvent;
import com.iris.client.event.Listener;
import com.iris.client.model.PersonModel;
import com.iris.client.util.Result;
import arcus.app.R;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.error.ErrorManager;
import arcus.app.common.fragments.BaseFragment;
import arcus.app.common.view.Version1Button;
import arcus.app.common.view.Version1ButtonColor;

import java.util.Date;

public class SettingsMarketingFragment extends BaseFragment implements View.OnClickListener{

    private ToggleButton offersPromoTB;
    private ToggleButton monthlySummaryTB;
    private Version1Button saveBtn;
    transient PersonModel personModel;

    @NonNull public static SettingsMarketingFragment newInstance() {
        return new SettingsMarketingFragment();
    }

    @Override public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        offersPromoTB = (ToggleButton) view.findViewById(R.id.settings_marketing_offers_promo);
        monthlySummaryTB = (ToggleButton) view.findViewById(R.id.settings_marketing_monthly_summary);

        View offersPromoContainer = view.findViewById(R.id.fragment_setting_marketing_offers_container);
        View monthlySummaryContainer = view.findViewById(R.id.fragment_setting_marketing_monthly_summary_container);

        saveBtn = (Version1Button) view.findViewById(R.id.setting_marketing_save_btn);
        saveBtn.setColorScheme(Version1ButtonColor.WHITE);
        offersPromoContainer.setOnClickListener(this);
        monthlySummaryContainer.setOnClickListener(this);
        saveBtn.setOnClickListener(this);
        return view;
    }

    @Override public void onResume() {
        super.onResume();
        getActivity().setTitle(getTitle().toUpperCase());
        personModel = SessionController.instance().getPerson();
        populateCheckbox();
    }

    private void populateCheckbox(){
        if (personModel == null) {
            return;
        }

        Date optOut = new Date(0);
        Date offers = personModel.getConsentOffersPromotions();
        Date consent = personModel.getConsentStatement();
        offersPromoTB.setChecked(offers != null && offers.compareTo(optOut) != 0);
        monthlySummaryTB.setChecked(consent != null && consent.compareTo(optOut) != 0);
    }

    @Override public String getTitle() {
        return getString(R.string.account_settings_marketing);
    }

    @Override public Integer getLayoutId() {
        return R.layout.fragment_settings_marketing;
    }

    @Override public void onClick(@NonNull View v) {
        final int id = v.getId();
        switch (id){
            case R.id.fragment_setting_marketing_offers_container:
                offersPromoTB.setChecked(!offersPromoTB.isChecked());
                break;
            case R.id.fragment_setting_marketing_monthly_summary_container:
                monthlySummaryTB.setChecked(!monthlySummaryTB.isChecked());
                break;
            case R.id.setting_marketing_save_btn:
                saveSelection();
                break;
        }
    }

    private void saveSelection(){
        if (personModel == null) {
            return;
        }

        // Null doesn't serialize so date is purposefully zero seconds since 1970 to "opt out"
        showProgressBarAndDisable(saveBtn);
        personModel.setConsentOffersPromotions(offersPromoTB.isChecked() ? new Date() : new Date(0));
        personModel.setConsentStatement(monthlySummaryTB.isChecked() ? new Date() : new Date(0));
        personModel.commit()
              .onCompletion(Listeners.runOnUiThread(new Listener<Result<ClientEvent>>() {
                  @Override public void onEvent(Result<ClientEvent> result) {
                      hideProgressBarAndEnable(saveBtn);

                      if (result.isError()) {
                          ErrorManager.in(getActivity()).showGenericBecauseOf(result.getError());
                          logger.error("Error setting offer and promotions.", result.getError());
                      }
                      else {
                          BackstackManager.getInstance().navigateBack();
                      }
                  }
              }));
    }
}

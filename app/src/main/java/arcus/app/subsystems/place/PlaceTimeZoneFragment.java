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
package arcus.app.subsystems.place;

import android.os.Bundle;
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import arcus.cornea.subsystem.model.TimeZoneModel;
import arcus.cornea.utils.Listeners;
import arcus.cornea.utils.TimeZoneLoader;
import com.iris.client.event.ListenerRegistration;
import arcus.app.R;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.error.ErrorManager;
import arcus.app.common.popups.TimezonePickerPopup;
import arcus.app.common.sequence.SequencedFragment;
import arcus.app.common.view.Version1Button;
import arcus.app.common.view.Version1TextView;
import arcus.app.integrations.Address;
import arcus.app.subsystems.place.controller.NewPlaceSequenceController;

import java.util.List;


public class PlaceTimeZoneFragment extends SequencedFragment<NewPlaceSequenceController>
      implements TimeZoneLoader.Callback, TimezonePickerPopup.Callback {

    private Version1Button nextButton;
    private Version1TextView timeZoneDisplay;
    private View timeZoneLayout;

    ListenerRegistration tzLoadedListener;
    String currentZone = null;

    @NonNull
    public static PlaceTimeZoneFragment newInstance() {
        return new PlaceTimeZoneFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        nextButton = (Version1Button) view.findViewById(R.id.fragment_account_parent_continue_btn);
        timeZoneDisplay = (Version1TextView) view.findViewById(R.id.timezone_display);
        timeZoneLayout = view.findViewById(R.id.timezone_layout);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        timeZoneLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showProgressBarAndDisable(nextButton, timeZoneLayout);
                tzLoadedListener = TimeZoneLoader.instance().setCallback(PlaceTimeZoneFragment.this);
                TimeZoneLoader.instance().loadTimezones();
            }
        });

        nextButton.setEnabled(false);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveZone();
            }
        });
    }

    @NonNull
    @Override
    public String getTitle() {
        return getResources().getString(R.string.settings_place_timezone);
    }

    @Override public void failed(Throwable throwable) {
        hideProgressBarAndEnable(timeZoneLayout);
        ErrorManager.in(getActivity()).showGenericBecauseOf(throwable);
    }

    @Override public void loaded(List<TimeZoneModel> timeZones) {
        hideProgressBarAndEnable(timeZoneLayout);
        TimezonePickerPopup popup = TimezonePickerPopup.newInstance(currentZone, timeZones);
        popup.setCallback(this);

        BackstackManager.getInstance().navigateToFloatingFragment(popup, popup.getClass().getSimpleName(), true);
    }

    @Override public void timeZoneSelected(TimeZoneModel timeZone) {
        Address address = getController().getNewPlaceAddressEntered();
        if (address == null) {
            return;
        }

        address.setDst(timeZone.isUsesDST());
        address.setTimeZoneName(timeZone.getName());
        address.setTimeZoneId(timeZone.getId());
        address.setUtcOffset(timeZone.getOffset());

        currentZone = timeZone.getId();
        nextButton.setEnabled(true);
        timeZoneDisplay.setText(timeZone.getName());
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_place_timezone;
    }

    @Override public void onPause() {
        super.onPause();
        Listeners.clear(tzLoadedListener);
        hideProgressBarAndEnable(nextButton, timeZoneLayout);
    }

    protected void saveZone() {
        showProgressBarAndDisable(nextButton, timeZoneLayout);
        getController().addNewPlace(new NewPlaceSequenceController.CreatePlaceCallback() {
            @Override public void onError(Throwable throwable) {
                failed(throwable);
            }

            @Override public void onSuccess() {
                hideProgressBarAndEnable(nextButton, timeZoneLayout);
                goNext();
            }
        });
    }
}

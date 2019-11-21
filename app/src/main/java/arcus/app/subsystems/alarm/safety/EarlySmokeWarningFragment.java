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
package arcus.app.subsystems.alarm.safety;

import android.app.Activity;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import arcus.cornea.SessionController;
import arcus.cornea.subsystem.safety.EarlySmokeController;
import arcus.cornea.subsystem.safety.model.Alarm;
import arcus.cornea.utils.Listeners;
import com.iris.client.event.ListenerRegistration;
import com.iris.client.model.PlaceModel;
import com.iris.client.session.SessionActivePlaceSetEvent;
import arcus.app.R;
import arcus.app.activities.DashboardActivity;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.error.ErrorManager;
import arcus.app.common.fragments.BaseFragment;

import java.util.List;

import de.greenrobot.event.EventBus;

public class EarlySmokeWarningFragment extends BaseFragment implements EarlySmokeController.Callback {
    public  static final String DATE_FORMAT = "%1$s\n%2$tl:%2$tM %2$Tp";
    private ListenerRegistration listenerRegistration;
    private LinearLayout deviceLayout;
    private TextView placeName;
    private TextView placeAddress;

    public static EarlySmokeWarningFragment newInstance() {
        return new EarlySmokeWarningFragment();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        deviceLayout = (LinearLayout) view.findViewById(R.id.device_name_layout);
        placeName = (TextView) view.findViewById(R.id.place_name_view);
        placeAddress = (TextView) view.findViewById(R.id.place_address_view);
        setHasOptionsMenu(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        Listeners.clear(listenerRegistration);
        listenerRegistration = EarlySmokeController.instance().setCallback(this);

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

        loadPlaceName();
    }

    public void onEvent(SessionActivePlaceSetEvent event) {
        loadPlaceName();
    }

    private boolean loadPlaceName() {
        PlaceModel place = SessionController.instance().getPlace();
        if (place != null) {
            placeName.setText(place.getName());
            placeAddress.setText(place.getStreetAddress1());
            return true;
        }
        return false;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setTitle();

        Activity activity = getActivity();
        if (activity != null && (activity instanceof DashboardActivity)) {
            ((DashboardActivity) activity).setToolbarEarlyWarningColor();
        }
    }

    @Nullable
    @Override
    public Integer getMenuId() {
        return R.menu.menu_edit_done_toggle;
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.menu_edit_done);
        if(item != null) {
            item.setTitle(getResources().getString(R.string.card_menu_done));
        }

        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        BackstackManager.getInstance().navigateBack();
        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }

        Listeners.clear(listenerRegistration);

        Activity activity = getActivity();
        if (activity != null && (activity instanceof DashboardActivity)) {
            ((DashboardActivity) activity).setToPreviousToolbarColor();
        }
    }

    @Nullable
    @Override
    public String getTitle() {
        return getString(R.string.early_smoke_warning);
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.early_warning_fragment;
    }

    @Override
    public void showError(Throwable error) {
        ErrorManager.in(getActivity()).showGenericBecauseOf(error);
    }

    @Override
    public void showAlarm(List<Alarm> alarmDevices) {
        if(alarmDevices.size() == 0) {
            getActivity().getSupportFragmentManager().beginTransaction().remove(this).commit();
        }
        deviceLayout.removeAllViews();

        LayoutInflater inflater = LayoutInflater.from(deviceLayout.getContext());
        for (Alarm device : alarmDevices) {
            TextView textView = (TextView) inflater.inflate(R.layout.smoke_detctor_with_name, deviceLayout, false);
            textView.setText(String.format(DATE_FORMAT, device.getName(), device.getTime()));

            deviceLayout.addView(textView);
        }
    }

    @Override
    public boolean onBackPressed() {
        return true; // Must click "DONE" to exit
    }
}

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
package arcus.app.device.pairing.post;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import arcus.cornea.provider.PlaceModelProvider;
import arcus.cornea.utils.Listeners;
import com.iris.client.capability.AlarmSubsystem;
import com.iris.client.event.Listener;
import com.iris.client.model.PlaceModel;
import arcus.app.R;
import arcus.app.common.sequence.SequencedFragment;
import arcus.app.common.view.Version1Button;
import arcus.app.common.view.Version1TextView;
import arcus.app.device.pairing.post.controller.PostPairingSequenceController;

import java.util.ArrayList;



public class ProMonitoringAlarmActivatedFragment extends SequencedFragment<PostPairingSequenceController> {

    private final static String ALARM_ACTIVATED_KEY = "ALARMS_ACTIVATED_BY_PAIRING";

    private Version1Button nextButton;
    private Version1TextView subtitleText, whatToExpectText;
    private LinearLayout smokeChecked, coChecked, securityChecked, waterChecked, panicChecked;

    public static ProMonitoringAlarmActivatedFragment newInstance(ArrayList<String> alarmsActivatedByPairing) {
        ProMonitoringAlarmActivatedFragment fragment = new ProMonitoringAlarmActivatedFragment();

        Bundle arguments = new Bundle();
        arguments.putStringArrayList(ALARM_ACTIVATED_KEY, alarmsActivatedByPairing);
        fragment.setArguments(arguments);

        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        subtitleText = (Version1TextView) view.findViewById(R.id.subtitle);
        whatToExpectText = (Version1TextView) view.findViewById(R.id.what_to_expect);
        nextButton = (Version1Button) view.findViewById(R.id.next_btn);
        smokeChecked = (LinearLayout) view.findViewById(R.id.smoke_checked);
        coChecked = (LinearLayout) view.findViewById(R.id.co_checked);
        waterChecked = (LinearLayout) view.findViewById(R.id.water_checked);
        securityChecked = (LinearLayout) view.findViewById(R.id.security_checked);
        panicChecked = (LinearLayout) view.findViewById(R.id.panic_checked);

        return view;
    }

    @Override
    public void onResume () {
        super.onResume();
        getActivity().setTitle(getTitle());

        PlaceModelProvider.getCurrentPlace().load().onSuccess(Listeners.runOnUiThread(new Listener<PlaceModel>() {
            @Override
            public void onEvent(PlaceModel event) {
                subtitleText.setText(getString(R.string.pro_monitoring_post_subtitle, event.get(PlaceModel.ATTR_NAME)));
            }
        }));

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goNext();
            }
        });

        ArrayList<String> alarmsActivated = getAlarmsActivatedByPairing();

        smokeChecked.setVisibility(alarmsActivated.contains(AlarmSubsystem.ACTIVEALERTS_SMOKE) ? View.VISIBLE : View.GONE);
        coChecked.setVisibility(alarmsActivated.contains(AlarmSubsystem.ACTIVEALERTS_CO) ? View.VISIBLE : View.GONE);
        securityChecked.setVisibility(alarmsActivated.contains(AlarmSubsystem.ACTIVEALERTS_SECURITY) ? View.VISIBLE : View.GONE);
        panicChecked.setVisibility(alarmsActivated.contains(AlarmSubsystem.ACTIVEALERTS_PANIC) ? View.VISIBLE : View.GONE);
        waterChecked.setVisibility(alarmsActivated.contains(AlarmSubsystem.ACTIVEALERTS_WATER) ? View.VISIBLE : View.GONE);

        whatToExpectText.setMovementMethod(LinkMovementMethod.getInstance());
        whatToExpectText.setText(Html.fromHtml(getString(R.string.pro_monitoring_post_expect)));
    }

    @Nullable
    @Override
    public String getTitle() {
        return getString(R.string.pro_monitoring_title);
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_promonitoring_alarm_activated;
    }

    private ArrayList<String> getAlarmsActivatedByPairing() {
        return getArguments().getStringArrayList(ALARM_ACTIVATED_KEY);
    }
}

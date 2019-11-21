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
package arcus.app.common.popups;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ToggleButton;

import arcus.app.R;
import arcus.app.common.sequence.ReturnToSenderSequenceController;
import arcus.app.common.sequence.Sequenceable;
import arcus.app.common.sequence.SequencedFragment;
import arcus.app.common.view.Version1TextView;
import arcus.app.subsystems.rules.schedule.model.RulesCommand;
import arcus.app.subsystems.scenes.schedule.controller.SceneScheduleFragmentController;

import java.io.Serializable;

public class RuleChooseActivePopup extends SequencedFragment<ReturnToSenderSequenceController>  {
    public static final String STATE = "STATE";
    private View noScheduleContainer;
    private ToggleButton noScheduleToggle;
    private Version1TextView noScheduleText;
    private Version1TextView noScheduleTextDescription;
    private View dividerOne;

    private View weeklyScheduleContainer;
    private ToggleButton weeklyScheduleToggle;
    private Version1TextView weeklyScheduleText;
    private Version1TextView weeklyScheduleTextDescription;
    private View dividerTwo;
   private ImageView buttonClose;
   private Callback callback;

    public interface Callback {
        void activeInactiveSelected(RulesCommand.State state);
    }

    private Version1TextView scheduleBottomText;

    public static RuleChooseActivePopup newInstance() {
        return new RuleChooseActivePopup();
    }

    public static RuleChooseActivePopup newInstance(RulesCommand.State state) {

        RuleChooseActivePopup popup = new RuleChooseActivePopup();
        Bundle bundle = new Bundle(1);
        bundle.putSerializable(STATE, state);
        popup.setArguments(bundle);
        return popup;

    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        noScheduleContainer = view.findViewById(R.id.schedule_none_container);
        noScheduleToggle = (ToggleButton) view.findViewById(R.id.schedule_none_container_checkbox);
        noScheduleText = (Version1TextView) view.findViewById(R.id.scheduleNoneText);
        noScheduleTextDescription = (Version1TextView) view.findViewById(R.id.scheduleNoneDescription);
        dividerOne = view.findViewById(R.id.divider);

        weeklyScheduleContainer = view.findViewById(R.id.schedule_weekly_container);
        weeklyScheduleToggle = (ToggleButton) view.findViewById(R.id.schedule_weekly_container_checkbox);
        weeklyScheduleText = (Version1TextView) view.findViewById(R.id.scheduleWeeklyText);
        weeklyScheduleTextDescription = (Version1TextView) view.findViewById(R.id.scheduleWeeklyDescription);
        dividerTwo = view.findViewById(R.id.divider2);
        buttonClose = (ImageView) view.findViewById(R.id.button_close);
        buttonClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (noScheduleToggle.isChecked()){
                    callback.activeInactiveSelected(RulesCommand.State.ACTIVE);

                } else {
                    callback.activeInactiveSelected(RulesCommand.State.INACTIVE);
                }
                ((Sequenceable) RuleChooseActivePopup.this).goBack(getActivity(), (Sequenceable) RuleChooseActivePopup.this, null);
                  hideProgressBar();
            }
        });
        scheduleBottomText = (Version1TextView) view.findViewById(R.id.scene_schedule_bottom_text);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();


        noScheduleContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                noScheduleToggle.setChecked(true);
                weeklyScheduleToggle.setChecked(false);
            }
        });

        weeklyScheduleContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                noScheduleToggle.setChecked(false);
                weeklyScheduleToggle.setChecked(true);
            }
        });


        if (getArguments() != null) {
            Serializable object = getArguments().getSerializable(STATE);
            if (object != null) {
                RulesCommand.State selectedState = (RulesCommand.State) object;
                if (RulesCommand.State.ACTIVE == selectedState ){
                    noScheduleToggle.setChecked(true);
                    weeklyScheduleToggle.setChecked(false);
                } else {
                    noScheduleToggle.setChecked(false);
                    weeklyScheduleToggle.setChecked(true);
                }

            }
        }

    }

    @Override
    public void onPause() {
        super.onPause();

        SceneScheduleFragmentController.getInstance().removeListener();
    }

    @Nullable
    @Override
    public String getTitle() {
        return null;
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_rules_schedule_choose_active;
    }


}

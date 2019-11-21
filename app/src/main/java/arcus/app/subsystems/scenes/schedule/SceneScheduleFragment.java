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
package arcus.app.subsystems.scenes.schedule;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ToggleButton;

import arcus.app.R;
import arcus.app.common.sequence.SequencedFragment;
import arcus.app.common.view.Version1TextView;
import arcus.app.subsystems.scenes.editor.controller.SceneEditorSequenceController;
import arcus.app.subsystems.scenes.schedule.controller.SceneScheduleFragmentController;

public class SceneScheduleFragment extends SequencedFragment<SceneEditorSequenceController> implements SceneScheduleFragmentController.Callbacks {
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
    private RelativeLayout weeklyScheduleClickableRegion;

    private Version1TextView scheduleAbstractText;
    private ImageView chevronImage;

    private Version1TextView scheduleBottomText;

    public static SceneScheduleFragment newInstance() {
        return new SceneScheduleFragment();
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
        weeklyScheduleClickableRegion = (RelativeLayout) view.findViewById(R.id.weekly_schedule_clickable_region);

        scheduleAbstractText = (Version1TextView) view.findViewById(R.id.scheduleAbstractText);

        chevronImage = (ImageView) view.findViewById(R.id.chevron);
        scheduleBottomText = (Version1TextView) view.findViewById(R.id.scene_schedule_bottom_text);



        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (getController().isEditMode()) {
            noScheduleToggle.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.icon_checkmark_selector));
            noScheduleText.setTextColor(Color.WHITE);
            noScheduleTextDescription.setTextColor(Color.WHITE);
            dividerOne.setBackgroundColor(getResources().getColor(R.color.overlay_white_with_20));

            weeklyScheduleToggle.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.icon_checkmark_selector));
            weeklyScheduleText.setTextColor(Color.WHITE);
            weeklyScheduleTextDescription.setTextColor(Color.WHITE);
            dividerTwo.setBackgroundColor(getResources().getColor(R.color.overlay_white_with_20));

            scheduleAbstractText.setTextColor(Color.WHITE);

            chevronImage.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.chevron_white));
            scheduleBottomText.setTextColor(Color.WHITE);
        }

        noScheduleContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                noScheduleToggle.setChecked(true);
                weeklyScheduleToggle.setChecked(false);
                SceneScheduleFragmentController.getInstance().setScheduleEnabled(getController().getSceneAddress(), false);
            }
        });

        weeklyScheduleContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                noScheduleToggle.setChecked(false);
                weeklyScheduleToggle.setChecked(true);
                SceneScheduleFragmentController.getInstance().setScheduleEnabled(getController().getSceneAddress(), true);
            }
        });

        weeklyScheduleClickableRegion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getController().goWeeklyScheduleEditor(getActivity(), SceneScheduleFragment.this);
            }
        });

        showProgressBar();
        SceneScheduleFragmentController.getInstance().setListener(this);
        SceneScheduleFragmentController.getInstance().loadScheduleAbstract(getActivity(), getController().getSceneAddress());
    }

    @Override
    public void onPause() {
        super.onPause();

        SceneScheduleFragmentController.getInstance().removeListener();
    }

    @Override
    public void onAttach(@NonNull Activity activity) {
        super.onAttach(activity);

        if (getTitle() != null) {
            activity.setTitle(getTitle());
        }
    }

    @Nullable
    @Override
    public String getTitle() {
        return getString(R.string.scenes_schedule_title);
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_scene_schedule_initial;
    }

    @Override
    public void onCorneaError(Throwable cause) {
        hideProgressBar();
    }

    @Override
    public void onScheduleAbstractLoaded(boolean hasSchedule, boolean scheduleEnabled, String scheduleAbstract, String sceneAddress) {
        hideProgressBar();
        scheduleAbstractText.setText(scheduleAbstract);
        weeklyScheduleToggle.setChecked(scheduleEnabled);
        noScheduleToggle.setChecked(!scheduleEnabled);
    }
}

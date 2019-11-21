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
package arcus.app.subsystems.scenes.adapters;

import android.content.Context;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import arcus.app.ArcusApplication;
import arcus.app.R;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.image.ImageManager;
import arcus.app.common.image.picasso.transformation.BlackWhiteInvertTransformation;
import arcus.app.common.image.picasso.transformation.Invert;
import arcus.app.common.popups.AlertPopup;
import arcus.app.common.view.Version1TextView;
import arcus.app.subsystems.scenes.active.model.SceneListModel;
import arcus.app.subsystems.scenes.schedule.controller.SceneScheduleFragmentController;

import java.util.ArrayList;


public class SceneListAdapter extends ArrayAdapter<SceneListModel> implements SceneScheduleFragmentController.Callbacks {

    private OnClickListener listener;
    private boolean isEditMode = false;

    private static class Holder {
        Version1TextView title;
        Version1TextView subtitle;
        ImageView image;
        ImageView deleteButton;
        ImageView scheduleImage;
        ImageView chevron;
        LinearLayout cellClickRegion;
        CheckBox activateCheckbox;
    }

    public SceneListAdapter(Context context, ArrayList<SceneListModel> scenes) {
        super(context, 0, scenes);
        SceneScheduleFragmentController.getInstance().setListener(this);
        for(SceneListModel model : scenes) {
            SceneScheduleFragmentController.getInstance().loadScheduleAbstract(context, model.getModelAddress());
        }
    }

    public void setListener(OnClickListener listener) {
        this.listener = listener;
    }

    public void setEditMode(boolean isEditMode) {
        this.isEditMode = isEditMode;
        notifyDataSetInvalidated();
    }

    @Nullable
    @Override
    public View getView(final int position, @Nullable View convertView, ViewGroup parent) {

        View listViewRow = convertView;
        final Holder holder;

        if (listViewRow == null) {
            listViewRow = LayoutInflater.from(getContext()).inflate(R.layout.cell_scene_list_item, parent, false);
            holder = new Holder();

            holder.title = (Version1TextView) listViewRow.findViewById(R.id.title);
            holder.subtitle = (Version1TextView) listViewRow.findViewById(R.id.subtitle);
            holder.image = (ImageView) listViewRow.findViewById(R.id.image);
            holder.deleteButton = (ImageView) listViewRow.findViewById(R.id.delete_button);
            holder.scheduleImage = (ImageView) listViewRow.findViewById(R.id.schedule_image);
            holder.chevron = (ImageView) listViewRow.findViewById(R.id.chevron);
            holder.cellClickRegion = (LinearLayout) listViewRow.findViewById(R.id.cell_click_region);
            holder.activateCheckbox = (CheckBox) listViewRow.findViewById(R.id.activate_checkbox);

            listViewRow.setTag(holder);

        } else {
            holder = (Holder)convertView.getTag();
            holder.activateCheckbox.setOnCheckedChangeListener(null);
        }

        final SceneListModel thisItem = getItem(position);

        if (isEditMode) {
            holder.deleteButton.setVisibility(View.VISIBLE);
            holder.deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onDeleteClicked(thisItem);
                    }
                }
            });
            holder.chevron.setVisibility(View.INVISIBLE);

            holder.activateCheckbox.setVisibility(View.GONE);
            holder.activateCheckbox.setOnClickListener(null);

            holder.cellClickRegion.setOnClickListener(null);
        } else {
            holder.deleteButton.setVisibility(View.GONE);
            holder.deleteButton.setOnClickListener(null);
            holder.chevron.setVisibility(View.VISIBLE);

            holder.activateCheckbox.setVisibility(View.VISIBLE);
            holder.activateCheckbox.setFocusable(false);

            holder.activateCheckbox.setChecked(thisItem.isEnabled() && thisItem.hasSchedule());

            holder.activateCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (thisItem.hasSchedule()) {
                        if (isChecked) {
                            SceneScheduleFragmentController.getInstance().setScheduleEnabled(thisItem.getModelAddress(), true);
                        } else {
                            SceneScheduleFragmentController.getInstance().setScheduleEnabled(thisItem.getModelAddress(), false);
                        }
                     }
                     else {
                        holder.activateCheckbox.setChecked(false);
                        AlertPopup popup = AlertPopup.newInstance(ArcusApplication.getContext().getString(R.string.water_schedule_no_events),
                                ArcusApplication.getContext().getString(R.string.water_schedule_no_events_sub), null, null, new AlertPopup.AlertButtonCallback() {

                                    @Override
                                    public boolean topAlertButtonClicked() {
                                        return false;
                                    }

                                    @Override
                                    public boolean bottomAlertButtonClicked() {
                                        return false;
                                    }

                                    @Override
                                    public boolean errorButtonClicked() {
                                        return false;
                                    }

                                    @Override
                                    public void close() {
                                        BackstackManager.getInstance().navigateBack();
                                    }
                                });

                        popup.setCloseButtonVisible(true);
                        BackstackManager.getInstance().navigateToFloatingFragment(popup, popup.getClass().getSimpleName(), true);
                    }
                }
            });

            holder.cellClickRegion.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onItemClicked(thisItem);
                    }
                }
            });
        }

        if (thisItem.hasSchedule()) {
            holder.scheduleImage.setVisibility(View.VISIBLE);
        } else {
            holder.scheduleImage.setVisibility(View.INVISIBLE);
        }

        ImageManager.with(getContext())
                .putDrawableResource(thisItem.getSceneIconResourceID())
                .withTransform(new BlackWhiteInvertTransformation(Invert.BLACK_TO_WHITE))
                .into(holder.image)
                .execute();

        holder.title.setText(thisItem.getNameOfScene());
        holder.subtitle.setText(thisItem.getActionCount() + " Action" + (thisItem.getActionCount() != 1 ? "s" : ""));

        return listViewRow;
    }

    @Override
    public void onCorneaError(Throwable cause) {

    }

    @Override
    public void onScheduleAbstractLoaded(final boolean hasSchedule, final boolean scheduleEnabled, String scheduleAbstract, String sceneAddress) {
        int items = getCount();
        for(int nInd = 0; nInd < items; nInd++) {
            SceneListModel model = getItem(nInd);
            if(model.getModelAddress().equals(sceneAddress)) {
                model.setHasSchedule(hasSchedule);
                model.setIsEnabled(scheduleEnabled);

                ArcusApplication.getArcusApplication().getForegroundActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        notifyDataSetChanged();
                    }
                });
                break;
            }
        }
    }

    public interface OnClickListener {
        void onItemClicked(SceneListModel scene);

        void onDeleteClicked(SceneListModel scene);
    }

}

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
package arcus.app.subsystems.camera.adapter;

import android.Manifest;
import android.content.Context;
import android.graphics.Color;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import arcus.cornea.controller.SubscriptionController;
import arcus.cornea.subsystem.cameras.model.ClipModel;
import arcus.app.ArcusApplication;
import arcus.app.R;
import arcus.app.activities.BaseActivity;
import arcus.cornea.common.ViewRenderType;
import arcus.app.common.utils.GlobalSetting;
import arcus.app.common.view.ScleraTextView;
import com.squareup.picasso.Picasso;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class ClipRecyclerViewAdapter extends RecyclerView.Adapter<ClipRecyclerViewAdapter.BasicViewHolder> {
    private static final Logger logger = LoggerFactory.getLogger(ClipRecyclerViewAdapter.class);
    private final List<ClipModel> clips;
    private BaseActivity activity;

    public interface ClickListener {
        void playClip(String recordingID);

        void deleteClip(String recordingID);

        void downloadClip(String recordingID);

        void pinUpdate(boolean bMakePinned, String recordingID);

        void showExpiredClipPopup(String recordingID);
    }

    private final ClickListener clickListener;

    public ClipRecyclerViewAdapter(ClickListener clickListener, List<ClipModel> startingData, BaseActivity activity) {
        this.clickListener = clickListener;
        if (startingData != null) {
            clips = new ArrayList<>(startingData);
        } else {
            clips = new ArrayList<>(15);
        }
        this.activity = activity;
    }

    @Override
    public BasicViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View layout;

        switch (viewType) {
            case ViewRenderType.CLIP_VIEW:
                layout = LayoutInflater.from(parent.getContext()).inflate(R.layout.camera_clip_item, parent, false);
                return new ViewHolder(layout);
            case ViewRenderType.CLIP_DEVICE_FILTER:
                layout = LayoutInflater.from(parent.getContext()).inflate(R.layout.camera_filter_item, parent, false);
                return new HeaderFilterDeviceViewHolder(layout);
            default:
            case ViewRenderType.HEADER_VIEW:
                layout = LayoutInflater.from(parent.getContext()).inflate(R.layout.heading_item, parent, false);
                return new HeaderViewHolder(layout);
        }
    }

    public ClipModel getClipByRecordingId(String recordingId) {
        if(recordingId == null) {
            return null;
        }
        for(ClipModel model : clips) {
            if(recordingId.equals(model.getRecordingId())) {
                return model;
            }
        }
        return null;
    }
    @Override
    public void onBindViewHolder(BasicViewHolder holder, int position) {
        holder.bind(clips.get(position));
    }

    @Override
    public void onBindViewHolder(BasicViewHolder holder, int position, List<Object> payloads) {
        if(payloads.isEmpty()){
            onBindViewHolder(holder, position);
        } else {
            holder.unsetPin();
        }
    }

    public void unsetPinned(ClipModel clipModel) {
        int index = clips.indexOf(clipModel);
        if (index != -1) {
            notifyItemChanged(index, 0);
        }
    }

    public void addHeaders(String filterValue, String dateFilterValue) {
        clips.add(new ClipModel(ViewRenderType.CLIP_DEVICE_FILTER));
        notifyDataSetChanged();
    }

    public void add(List<ClipModel> clipModels) {
        if (clipModels == null || clipModels.isEmpty()) {
            return;
        }

        clips.addAll(clipModels);
        notifyDataSetChanged();
    }

    public void update(ClipModel clipModel) {
        int index = clips.indexOf(clipModel);
        if (index != -1) {
            clips.get(index).setCachedClipFile(clipModel.getCachedClipFile());
            notifyItemChanged(index);
        }
    }

    public void updatePlayDelete(ClipModel clipModel) {
        int index = clips.indexOf(clipModel);
        if (index != -1) {
            ClipModel existing = clips.get(index);
            if (existing.isDownloadDeleteAvailable() != clipModel.isDownloadDeleteAvailable()) {
                existing.setDownloadDeleteAvailable(clipModel.isDownloadDeleteAvailable());
                notifyItemChanged(index);
            }
        }
    }

    public void removeAll() {
        clips.clear();
        notifyDataSetChanged();
    }

    public void remove(ClipModel clipModel) {
        int index = clips.indexOf(clipModel);
        if (index == -1) { // This item was not in the list. (deleting a stream?)
            return;
        }

        clips.remove(index);
        notifyItemRemoved(index);

        if (clips.size() == 1 && clips.get(0).getType() == ViewRenderType.HEADER_VIEW) {
            clips.remove(0);
            notifyItemRemoved(0);
        } else {
            int previousIndex = Math.max(0, index - 1);
            int nextIndex = Math.min(clips.size() - 1, index);

            ClipModel previous = clips.get(previousIndex);
            ClipModel next = clips.get(nextIndex);
            if (previous.getType() == ViewRenderType.HEADER_VIEW && next.getType() == ViewRenderType.HEADER_VIEW) {
                clips.remove(previousIndex);
                notifyItemRemoved(previousIndex);
            }
        }
    }

    @Override
    public int getItemCount() {
        return clips.size();
    }

    @Override
    public int getItemViewType(int position) {
        return clips.get(position).getType();
    }

    @Override
    public long getItemId(int position) {
        return clips.get(position).hashCode();
    }

    abstract class BasicViewHolder extends RecyclerView.ViewHolder {
        BasicViewHolder(View itemView) {
            super(itemView);
        }

        public abstract void bind(ClipModel clipModel);

        public abstract void unsetPin();
    }

    class HeaderViewHolder extends BasicViewHolder {
        TextView headingText;

        HeaderViewHolder(View itemView) {
            super(itemView);
            headingText = itemView.findViewById(R.id.heading_text);
            headingText.setTextColor(Color.WHITE);
        }

        public void bind(ClipModel clipModel) {
            headingText.setText(clipModel.getTimeString());
        }

        @Override
        public void unsetPin() {
            // No-op
        }
    }

    class HeaderFilterDeviceViewHolder extends BasicViewHolder {
        HeaderFilterDeviceViewHolder(View itemView) {
            super(itemView);
            itemView.setVisibility(View.INVISIBLE);
        }

        @Override
        public void bind(ClipModel clipModel) {
            // Nothing to do
        }

        @Override
        public void unsetPin() {
            // No-op
        }
    }

    class ViewHolder extends BasicViewHolder implements View.OnClickListener {
        ImageView imageView, trashImage, downloadImage, pinImage;
        TextView durationAndSize, timeRecorded, expiration;
        ScleraTextView cameraName;
        Context context;

        public ViewHolder(View itemView) {
            super(itemView);
            context = itemView.getContext();
            trashImage = itemView.findViewById(R.id.trash_image);
            trashImage.setOnClickListener(this);
            downloadImage = itemView.findViewById(R.id.download_image);
            downloadImage.setOnClickListener(this);
            imageView = itemView.findViewById(R.id.preview_image);
            imageView.setOnClickListener(this);
            durationAndSize = itemView.findViewById(R.id.size_and_length);
            timeRecorded = itemView.findViewById(R.id.time_display);
            expiration = itemView.findViewById(R.id.expiration);
            cameraName = itemView.findViewById(R.id.camera_name);
            pinImage = itemView.findViewById(R.id.pin_image);
            pinImage.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            final int position = getAdapterPosition();
            if (clickListener == null || (position < 0 || position >= clips.size())) {
                return;
            }

            try {
                final ClipModel clipModel = clips.get(position);
                int id = v.getId();
                switch (id) {
                    case R.id.pin_image:
                        long currentMilliseconds = System.currentTimeMillis();
                        long expirationMilliseconds = clipModel.getDeleteTime().getTime();
                        long millisToExpiry = Math.max(0, expirationMilliseconds - currentMilliseconds);

                        if (millisToExpiry < TimeUnit.SECONDS.toMillis(1)) {
                            clickListener.showExpiredClipPopup(clipModel.getRecordingId());
                        } else {
                            Animation animation = AnimationUtils.loadAnimation(context, R.anim.clip_pin_anim);
                            animation.setAnimationListener(new Animation.AnimationListener() {
                                @Override
                                public void onAnimationStart(Animation animation) {
                                }

                                @Override
                                public void onAnimationRepeat(Animation animation) {
                                }

                                @Override
                                public void onAnimationEnd(Animation animation) {
                                    clickListener.pinUpdate(!clipModel.isPinned(), clipModel.getRecordingId());
                                    clipModel.setPinned(!clipModel.isPinned());
                                    if (clipModel.isPinned()) {
                                        pinImage.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.pinfilled));
                                        expiration.setVisibility(View.GONE);
                                    } else {
                                        pinImage.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.pin));
                                        expiration.setVisibility(View.VISIBLE);
                                    }
                                }
                            });
                            v.startAnimation(animation);
                        }
                        break;
                    case R.id.preview_image:
                        clickListener.playClip(clipModel.getRecordingId());
                        break;
                    case R.id.download_image:
                        activity.setPermissionCallback((permissionType, permissionsDenied, permissionsDeniedNeverAskAgain) -> {
                            if (permissionsDenied.contains(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                                activity.showSnackBarForPermissions(activity.getString(R.string.permission_storage_denied_message));
                            } else {
                                clickListener.downloadClip(clipModel.getRecordingId());
                            }
                        });
                        ArrayList<String> permissions = new ArrayList<>();
                        permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                        activity.checkPermission(permissions, GlobalSetting.PERMISSION_WRITE_EXTERNAL_STORAGE, R.string.permission_rationale_storage);
                        break;
                    case R.id.trash_image:
                        clickListener.deleteClip(clipModel.getRecordingId());
                        break;
                }
            } catch (Exception ex) {
                logger.error("Could not dispatch click.", ex);
            }
        }

        public void bind(ClipModel clipModel) {
            long currentMilliseconds = System.currentTimeMillis();
            long expirationMilliseconds = clipModel.getDeleteTime().getTime();
            long millisToExpiry = Math.max(0, expirationMilliseconds - currentMilliseconds);

            Picasso.with(itemView.getContext()).load(clipModel.getCachedClipFile()).fit().into(imageView);
            trashImage.setVisibility(View.VISIBLE);
            downloadImage.setVisibility(View.VISIBLE);
            String unknownValue = ArcusApplication.getContext().getString(R.string.camera_duration_size_unknown);
            if (unknownValue.equals(clipModel.getDurationString()) && unknownValue.equals(clipModel.getSizeString())) {
                durationAndSize.setText(ArcusApplication.getContext().getString(R.string.camera_recording_in_progress));
                trashImage.setVisibility(View.INVISIBLE);
                downloadImage.setVisibility(View.INVISIBLE);
            } else {
                durationAndSize.setText(String.format("%s", clipModel.getDurationString()));
            }

            if(clipModel.getCameraName().isEmpty()){
                cameraName.setVisibility(View.GONE);
            } else {
                cameraName.setVisibility(View.VISIBLE);
                cameraName.setText(clipModel.getCameraName());
            }
            timeRecorded.setText(clipModel.getTimeString());
            if (clipModel.isDownloadDeleteAvailable()) {
                trashImage.setEnabled(true);
                trashImage.setAlpha(1f);
                downloadImage.setEnabled(true);
                downloadImage.setAlpha(1f);
            } else {
                trashImage.setEnabled(false);
                trashImage.setAlpha(.4f);
                downloadImage.setEnabled(false);
                downloadImage.setAlpha(.4f);
            }

            setPins(clipModel, millisToExpiry);
        }

        public void unsetPin() {
            if (Objects.equals(pinImage.getDrawable().getConstantState(),
                    activity.getResources().getDrawable(R.drawable.pinfilled).getConstantState())) {
                pinImage.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.pin));
            }
        }

        private void setPins(ClipModel clipModel, long millisToExpiry) {
            expiration.setText(millisToClipExpiry(millisToExpiry));
            if (SubscriptionController.isPremiumOrPro()) {
                if (clipModel.isPinned()) {
                    expiration.setVisibility(View.GONE);
                    pinImage.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.pinfilled));
                } else {
                    expiration.setVisibility(View.VISIBLE);
                    pinImage.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.pin));
                }
            } else {
                pinImage.setVisibility(View.GONE);
            }
        }

        private String millisToClipExpiry(long millisToExpiry) {
            if(isMoreThanTwoDays(millisToExpiry)) {
                return activity.getString(
                        R.string.clip_expires_days,
                        String.valueOf(TimeUnit.MILLISECONDS.toDays(millisToExpiry))
                );
            }
            else if(isBetweenOneAndTwoDays(millisToExpiry)) {
                return activity.getString(
                        R.string.clip_expires_days_hours,
                        String.valueOf(TimeUnit.MILLISECONDS.toDays(millisToExpiry)),
                        String.valueOf(TimeUnit.MILLISECONDS.toHours(millisToExpiry) % TimeUnit.DAYS.toHours(1))
                );
            } else if(isLessThanOneDay(millisToExpiry)) {
                return activity.getString(
                        R.string.clip_expires_hours_minutes,
                        String.valueOf(TimeUnit.MILLISECONDS.toHours(millisToExpiry)),
                        String.valueOf(TimeUnit.MILLISECONDS.toMinutes(millisToExpiry) % TimeUnit.HOURS.toMinutes(1))
                );
            } else if(isLessThanOneHour(millisToExpiry)) {
                return activity.getString(
                        R.string.clip_expires_minutes,
                        String.valueOf(TimeUnit.MILLISECONDS.toMinutes(millisToExpiry))
                );
            } else {
                    return activity.getString(R.string.card_lawn_and_garden_now);
            }
        }


        private Boolean isMoreThanTwoDays(long millisToClipExpiry) {
            return TimeUnit.MILLISECONDS.toDays(millisToClipExpiry) > TimeUnit.DAYS.toDays(2);
        }

        private Boolean isBetweenOneAndTwoDays(long millisToClipExpiry) {
            return TimeUnit.MILLISECONDS.toDays(millisToClipExpiry) < TimeUnit.DAYS.toDays(2) && TimeUnit.MILLISECONDS.toDays(millisToClipExpiry) >= TimeUnit.DAYS.toDays(1);
        }

        private Boolean isLessThanOneDay(long millisToClipExpiry) {
            return TimeUnit.MILLISECONDS.toDays(millisToClipExpiry) < TimeUnit.DAYS.toDays(1);
        }

        private Boolean isLessThanOneHour(long millisToClipExpiry) {
            return TimeUnit.MILLISECONDS.toHours(millisToClipExpiry) < TimeUnit.HOURS.toHours(1);
        }
    }
}

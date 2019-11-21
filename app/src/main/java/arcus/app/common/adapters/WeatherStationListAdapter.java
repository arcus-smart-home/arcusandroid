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
package arcus.app.common.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import arcus.app.R;

import arcus.app.common.models.StationScanResult;

import java.util.List;

public class WeatherStationListAdapter extends ArrayAdapter<StationScanResult> {
    private boolean useEditModeColorScheme;
    private Drawable playImage;
    private Drawable stopImage;
    private int unchecked;
    private int checked;
    private double selectedId = 0;
    private int white60;
    private int black60;
    private int white20;
    private int black20;
    private Callback callback;
    private boolean allowChanges = true;

    public WeatherStationListAdapter(@NonNull Context context, @NonNull List<StationScanResult> models) {
        this(context, models, 0.0, true);
    }

    public interface Callback {
        void stationPlaybackChange(double id);
        void stationSelectionChange(double id);
    }

    @Override
    public boolean areAllItemsEnabled () {
        return allowChanges;
    }

    @Override
    public boolean isEnabled(int position) {
        return allowChanges;
    }

    public void setEnabled(boolean allowChanges) {
        this.allowChanges = allowChanges;
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public WeatherStationListAdapter(
          @NonNull Context context,
          @NonNull List<StationScanResult> models,
          double defaultSelectionId,
          boolean editModeColorScheme
    ) {
        super(context, 0);
        addAll(models);
        useEditModeColorScheme = editModeColorScheme;

        white60 = context.getResources().getColor(R.color.overlay_white_with_60);
        black60 = context.getResources().getColor(R.color.black_with_60);

        white20 = context.getResources().getColor(R.color.overlay_white_with_20);
        black20 = context.getResources().getColor(R.color.black_with_20);

        int playImageRes = useEditModeColorScheme ? R.drawable.play_btn_white: R.drawable.play_btn_black;
        playImage = ContextCompat.getDrawable(getContext(), playImageRes);

        int stopImageRes = useEditModeColorScheme ? R.drawable.stop_btn_white: R.drawable.stop_btn_black;
        stopImage = ContextCompat.getDrawable(getContext(), stopImageRes);

        checked = useEditModeColorScheme ? R.drawable.circle_check_white_filled: R.drawable.circle_check_black_filled;
        unchecked = useEditModeColorScheme ? R.drawable.circle_hollow_white: R.drawable.circle_hollow_black;

        selectedId = defaultSelectionId;
    }

    public void setSelectedItem(double id) {
        selectedId = id;
    }

    public double getSelectedItem() {
        return selectedId;
    }

    @Override public View getView(int position, View convertView, ViewGroup parent) {
        StationScanResult item = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.weather_station_device_item, parent, false);
        }

        TextView text = (TextView) convertView.findViewById(R.id.list_item_name);
        TextView subText = (TextView) convertView.findViewById(R.id.list_item_description);
        ImageView playStop = (ImageView) convertView.findViewById(R.id.play_stop);
        ImageView checkBox = (ImageView) convertView.findViewById(R.id.checkbox);

        text.setText(item.getTitle());
        if (TextUtils.isEmpty(item.getSubTitle())) {
            subText.setVisibility(View.GONE);
        }
        else {
            subText.setText(item.getSubTitle());
        }
        View bottomDivider = convertView.findViewById(R.id.item_divider);

        if (useEditModeColorScheme) {
            text.setTextColor(Color.WHITE);
            subText.setTextColor(white60);
            bottomDivider.setBackgroundColor(white20);
        }
        else {
            text.setTextColor(Color.BLACK);
            subText.setTextColor(black60);
            bottomDivider.setBackgroundColor(black20);
        }

        if(item.isPlaying()) {
            playStop.setImageDrawable(stopImage);
        }
        else {
            playStop.setImageDrawable(playImage);
        }

        playStop.setTag(item.getId());
        playStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(callback != null) {
                    callback.stationPlaybackChange((double)view.getTag());
                }
            }
        });


        checkBox.setImageResource(selectedId == item.getId() ? checked : unchecked);
        checkBox.setTag(item.getId());
        checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedId = (double)view.getTag();
                notifyDataSetChanged();
                if(callback != null) {
                    callback.stationSelectionChange((double)view.getTag());
                }
            }
        });

        return convertView;
    }
}

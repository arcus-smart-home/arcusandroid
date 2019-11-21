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
package arcus.app.common.schedule.adapter;

import android.content.Context;
import android.graphics.Color;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import arcus.cornea.utils.DateUtils;
import arcus.cornea.utils.SunriseSunset;
import arcus.cornea.utils.TimeOfDay;
import com.iris.client.bean.TimeOfDayCommand;
import arcus.app.R;
import arcus.app.common.image.ImageManager;
import arcus.app.common.image.picasso.transformation.BlackWhiteInvertTransformation;
import arcus.app.common.image.picasso.transformation.Invert;
import arcus.app.common.view.Version1TextView;

import java.util.List;

/**
 * Renders ListView cells for scheduled commands represented by a {@link TimeOfDayCommand} object.
 * TODO: Transition users of this class to ScheduleCommandAdapter.
 */
public class TimeOfDayCommandAdapter extends AbstractScheduleCommandAdapter<TimeOfDayCommand> {

    private final boolean useLightColorScheme;

    public TimeOfDayCommandAdapter(Context context, List<TimeOfDayCommand> commands, boolean useLightColorScheme) {
        super(context);
        addAll(commands);
        this.useLightColorScheme = useLightColorScheme;
    }

    @Override public TimeOfDay getEventTimeOfDay(int position) {
        TimeOfDayCommand tod = getItem(position);
        return TimeOfDay.fromStringWithMode(tod.getTime(), SunriseSunset.fromTimeOfDayCommand(tod), tod.getOffsetMinutes());
    }

    @Override public String getEventAbstract(int position) {
        return null;
    }

    @Nullable @Override public View getView(
          final int position,
          @Nullable View convertView,
          ViewGroup parent
    ) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.cell_time_of_day_command, parent, false);
        }

        Version1TextView leftText = (Version1TextView) convertView.findViewById(R.id.event_time);
        ImageView daytimeIcon = (ImageView) convertView.findViewById(R.id.daytime_icon);
        ImageView chevron = (ImageView) convertView.findViewById(R.id.chevron);
        Version1TextView rightText = (Version1TextView) convertView.findViewById(R.id.event_abstract);

        leftText.setText(DateUtils.format(getEventTimeOfDay(position), true));
        leftText.setTextColor(useLightColorScheme() ? Color.WHITE : Color.BLACK);
        rightText.setVisibility(View.GONE);

        ImageManager.with(getContext())
                .putDrawableResource(getDaytimeIconResId(position))
                .withTransform(new BlackWhiteInvertTransformation(Invert.WHITE_TO_BLACK), !useLightColorScheme())
                .into(daytimeIcon)
                .execute();

        chevron.setImageResource(useLightColorScheme() ? R.drawable.chevron_white : R.drawable.chevron);

        return convertView;
    }

    @Override
    public boolean useLightColorScheme() {
        return useLightColorScheme;
    }
}

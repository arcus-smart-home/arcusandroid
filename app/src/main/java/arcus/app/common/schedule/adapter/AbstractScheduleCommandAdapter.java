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
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import arcus.cornea.utils.DateUtils;
import arcus.cornea.utils.TimeOfDay;
import arcus.app.R;
import arcus.app.common.image.ImageManager;
import arcus.app.common.image.picasso.transformation.BlackWhiteInvertTransformation;
import arcus.app.common.image.picasso.transformation.Invert;
import arcus.app.common.utils.StringUtils;
import arcus.app.common.view.Version1TextView;


public abstract class AbstractScheduleCommandAdapter<T> extends ArrayAdapter<T> {
    private int white35 = 0;
    private int white60 = 0;
    private int black60 = 0;

    /**
     * Gets the time of day for the item we are rendering.
     *
     * @param position The list position of the scheduled command whose runtime is requested.
     * @return The time of day for the requested scheduled command.
     */
    public abstract TimeOfDay getEventTimeOfDay(int position);

    /**
     * Gets the abstract text (that is, the light-colored text appearing directly to the left of
     * the list chevron) for the scheduled command at the provided list position. Return an
     * empty string or null to hide the abstract text altogether.
     *
     * @param position The list position of the scheduled command whose abstract is requested.
     * @return The abstract text for the requested scheduled command.
     */
    public abstract String getEventAbstract(int position);

    /**
     * Determines whether the list cells should be rendered in white text (when true), or dark text
     * (when false).
     *
     * @return True to render cells in light text; false for dark text.
     */
    public abstract boolean useLightColorScheme();

    public AbstractScheduleCommandAdapter(Context context) {
        super(context, 0);

        white35 = context.getResources().getColor(R.color.white_with_35);
        white60 = context.getResources().getColor(R.color.white_with_60);
        black60 = context.getResources().getColor(R.color.black_with_60);
    }

    @Nullable
    @Override
    public View getView(final int position, @Nullable View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.cell_time_of_day_command, parent, false);
        }

        boolean useLightColorScheme = useLightColorScheme();
        Version1TextView leftText = (Version1TextView) convertView.findViewById(R.id.event_time);
        ImageView daytimeIcon = (ImageView) convertView.findViewById(R.id.daytime_icon);
        ImageView chevron = (ImageView) convertView.findViewById(R.id.chevron);
        Version1TextView rightText = (Version1TextView) convertView.findViewById(R.id.event_abstract);

        leftText.setText(DateUtils.format(getEventTimeOfDay(position), true));
        leftText.setTextColor(useLightColorScheme ? white60 : black60);

        rightText.setText(getEventAbstract(position));
        rightText.setTextColor(useLightColorScheme ? white35 : black60);
        rightText.setVisibility(StringUtils.isEmpty(getEventAbstract(position)) ? View.GONE : View.VISIBLE);

        ImageManager.with(getContext())
                .putDrawableResource(getDaytimeIconResId(position))
                .withTransform(new BlackWhiteInvertTransformation(Invert.WHITE_TO_BLACK), !useLightColorScheme)
                .into(daytimeIcon)
                .execute();

        chevron.setImageResource(useLightColorScheme ? R.drawable.chevron_white : R.drawable.chevron);

        return convertView;
    }

    protected Integer getDaytimeIconResId(int position) {
        switch (getEventTimeOfDay(position).getDayTime()) {
            case DAYTIME:
                return R.drawable.icon_day;
            case NIGHTTIME:
                return R.drawable.icon_night;
            case SUNRISE:
                return R.drawable.icon_sunrise;
            case SUNSET:
                return R.drawable.icon_sunset;
            default:
                return R.drawable.icon_day;
        }
    }
}

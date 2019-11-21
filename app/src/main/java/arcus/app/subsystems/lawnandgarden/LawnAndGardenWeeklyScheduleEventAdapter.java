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
package arcus.app.subsystems.lawnandgarden;

import android.content.Context;
import android.graphics.Color;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.google.gson.internal.LinkedTreeMap;
import arcus.cornea.utils.TimeOfDay;
import arcus.app.R;
import arcus.app.common.view.Version1TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;


public class LawnAndGardenWeeklyScheduleEventAdapter extends BaseAdapter {

    LayoutInflater inflater;
    Context context;
    ArrayList<LinkedTreeMap<String, Object>> events = new ArrayList<LinkedTreeMap<String, Object>>();
    ArrayList<ArrayList<String>> zoneList = new ArrayList<>();

    public LawnAndGardenWeeklyScheduleEventAdapter(Context context) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
    }

    public LawnAndGardenWeeklyScheduleEventAdapter(Context context, ArrayList<LinkedTreeMap<String, Object>> events, ArrayList<ArrayList<String>> zoneList) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.events = events;
        this.zoneList = zoneList;
    }

    @Override
    public String getItem(int position) {
        return (String) this.events.get(position).get("eventId");
    }

    @Override
    public int getCount() {
        return events.size();
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(final int position, @Nullable View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.lawn_and_garden_weekly_schedule_event, parent, false);
        }

        int durationInMin = 0;
        int zoneCount = 0;
        for(LinkedTreeMap<String, Object> eventItem : (ArrayList<LinkedTreeMap<String, Object>>) events.get(position).get("events")) {
            durationInMin += (Double)eventItem.get("duration");
            zoneCount++;
        }
        Version1TextView time = (Version1TextView) convertView.findViewById(R.id.event_time);
        ImageView chevron = (ImageView) convertView.findViewById(R.id.chevron);
        Version1TextView duration = (Version1TextView) convertView.findViewById(R.id.duration);
        Version1TextView zoneTitle = (Version1TextView) convertView.findViewById(R.id.zones_title);
        Version1TextView tvZones = (Version1TextView) convertView.findViewById(R.id.zones);

        time.setText(getPrettyTimeString((String) events.get(position).get("timeOfDay")));  //getEventAbstract(position)
        time.setTextColor(useLightColorScheme() ? context.getResources().getColor(R.color.white_with_35) : context.getResources().getColor(R.color.black_with_60));

        duration.setText(durationInMin + " min");
        duration.setTextColor(useLightColorScheme() ? context.getResources().getColor(R.color.white_with_35) : context.getResources().getColor(R.color.black_with_60));

        if(zoneCount > 1) {
            zoneTitle.setText(zoneCount + " " + context.getResources().getString(R.string.irrigation_zones));
        }
        else {
            zoneTitle.setText(zoneCount + " " + context.getResources().getString(R.string.irrigation_zone));
        }

        zoneTitle.setTextColor(useLightColorScheme() ? Color.WHITE : Color.BLACK);

        String zonelist = "";
        ArrayList<String> tmpZones = zoneList.get(position);
        for(String zone : tmpZones) {
            zonelist += zone + ", ";
        }
        if(zonelist.length() > 2) {
            zonelist = zonelist.substring(0, zonelist.length()-2);
        }
        tvZones.setText(zonelist);
        tvZones.setTextColor(useLightColorScheme() ? context.getResources().getColor(R.color.white_with_35) : context.getResources().getColor(R.color.black_with_60));

        chevron.setImageResource(useLightColorScheme() ? R.drawable.chevron_white : R.drawable.chevron);

        return convertView;
    }

    protected String getPrettyTimeString (String timeString) {
        TimeOfDay tod = TimeOfDay.fromString(timeString);
        Calendar cal = new GregorianCalendar();
        cal.set(Calendar.HOUR_OF_DAY, tod.getHours());
        cal.set(Calendar.MINUTE, tod.getMinutes());

        return new SimpleDateFormat("h:mm aa").format(cal.getTime());
    }

    public boolean useLightColorScheme() {
        return true;
    }
}

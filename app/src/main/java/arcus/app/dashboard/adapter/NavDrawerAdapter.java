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
package arcus.app.dashboard.adapter;

import android.content.Context;
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import arcus.app.R;


public class NavDrawerAdapter extends BaseAdapter {

    private String[] sideNavTitles;
    private String[] sideNavSubTitle;
    private int[] icons;
    private LayoutInflater inflater;


    public NavDrawerAdapter(@NonNull final Context context, final String[] sideNavTitles, final String[] sideNavSubTitle){
        Context context1 = context;
        this.sideNavTitles = sideNavTitles;
        this.sideNavSubTitle = sideNavSubTitle;
        icons = new int[]{R.drawable.side_menu_dashboard, R.drawable.side_menu_scenes, R.drawable.side_menu_rules,R.drawable.side_menu_devices,
                R.drawable.side_menu_settings,
                R.drawable.side_menu_support,R.drawable.side_menu_shop};
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    private static class ViewHolder{
        public ImageView icon;
        public TextView title;
        public TextView subTitle;
    }

    @Override
    public int getCount() {
        return sideNavTitles.length;
    }

    @Override
    public Object getItem(int position) {
        return sideNavTitles[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        ViewHolder holder;
        if (view == null) {
            view = inflater.inflate(R.layout.sidenav_item, parent, false);
            holder = new ViewHolder();

            //holder.icon = (ImageView) view.findViewById(R.id.sidenav_icon);
            holder.title = (TextView) view.findViewById(R.id.sidenav_title);
            holder.subTitle = (TextView) view.findViewById(R.id.sidenav_sub_title);
            holder.icon = (ImageView) view.findViewById(R.id.sidenav_icon);
            view.setTag(holder);
        }else{
            holder = (ViewHolder) view.getTag();
        }


        holder.title.setText(sideNavTitles[position]);
        holder.subTitle.setText(sideNavSubTitle[position]);
        holder.icon.setImageResource(icons[position]);

        return view;
    }
}

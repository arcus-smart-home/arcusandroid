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
package arcus.app.subsystems.doorsnlocks.adapter;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import arcus.cornea.subsystem.doorsnlocks.model.AccessState;
import com.iris.client.bean.LockAuthorizationState;
import arcus.app.R;

import java.util.ArrayList;
import java.util.List;


public class AccessPeopleListAdapter extends BaseAdapter {

    private boolean isEditable = false;
    private LayoutInflater mInflater;
    private Context context;
    private List<AccessState> accessStateList;

    public AccessPeopleListAdapter(@NonNull Context context){
        this.context = context;
        mInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        accessStateList = new ArrayList<>();
    }

    public void setAccessStateList(List<AccessState> accessStateList){
        clear();
        this.accessStateList = accessStateList;
        notifyDataSetChanged();
    }

    public List<AccessState> getAccessStateList(){
        return this.accessStateList;
    }

    public void setIsEditable(boolean isEditable) {
        this.isEditable = isEditable;
        notifyDataSetChanged();
    }

    public void clear(){
        this.accessStateList.clear();
    }

    @Override
    public int getCount() {
        return accessStateList.size();
    }

    @Override
    public Object getItem(int position) {
        return accessStateList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Nullable
    @Override
    public View getView(int position, @Nullable View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.access_people_list_item,parent,false);
            holder.checkBoxIcon = (ImageView) convertView.findViewById(R.id.access_list_person_check);
            holder.personImage = (ImageView) convertView.findViewById(R.id.access_list_person_image);
            holder.personName = (TextView) convertView.findViewById(R.id.access_list_person_name);
            holder.personRelationship = (TextView) convertView.findViewById(R.id.access_list_person_relationship);
            convertView.setTag(holder);
        }else {
            holder = (ViewHolder) convertView.getTag();
        }

        final AccessState accessState  = (AccessState) getItem(position);

        //todo hard code person icon
        holder.personImage.setImageResource(R.drawable.icon_user_small_white);

        holder.personName.setText(accessState.getFirstName() + " " + accessState.getLastName());

        holder.personRelationship.setText(accessState.getRelationship());

        holder.checkBoxIcon.setVisibility(isEditable ? View.VISIBLE : View.GONE);

        holder.checkBoxIcon.setImageResource(accessState.getAccessState().equals(LockAuthorizationState.STATE_AUTHORIZED) ? R.drawable.circle_check_white_filled : R.drawable.circle_hollow_white);

        holder.checkBoxIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                accessState.setAccessState(accessState.getAccessState().equals(LockAuthorizationState.STATE_AUTHORIZED) ? LockAuthorizationState.STATE_UNAUTHORIZED : LockAuthorizationState.STATE_AUTHORIZED);
                notifyDataSetChanged();
            }
        });

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                accessState.setAccessState(accessState.getAccessState().equals(LockAuthorizationState.STATE_AUTHORIZED) ? LockAuthorizationState.STATE_UNAUTHORIZED : LockAuthorizationState.STATE_AUTHORIZED);
                notifyDataSetChanged();
            }
        });

        return convertView;
    }

    public static class ViewHolder {
        public ImageView checkBoxIcon;
        public ImageView personImage;
        public TextView personName;
        public TextView personRelationship;
    }
}

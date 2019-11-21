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
package arcus.app.subsystems.alarm.security.adapters;

import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import arcus.app.R;
import arcus.app.common.cards.SimpleDividerCard;
import arcus.app.common.cards.view.RecyclerViewEmptyCard;
import arcus.app.subsystems.alarm.cards.AlarmActiveCard;
import arcus.app.subsystems.alarm.cards.AlarmInfoCard;
import arcus.app.subsystems.alarm.cards.AlarmStatusCard;
import arcus.app.subsystems.alarm.cards.AlarmTopCard;
import arcus.app.subsystems.alarm.cards.internal.AlarmActiveCardView;
import arcus.app.subsystems.alarm.cards.internal.AlarmInfoCardItemView;
import arcus.app.subsystems.alarm.cards.internal.AlarmStatusCardItemView;
import arcus.app.subsystems.alarm.cards.internal.AlarmTopCardItemView;


import java.util.ArrayList;


public class SecurityFragmentRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<SimpleDividerCard> alarmCards = new ArrayList<>();
    private final int ALARM_TOP = 0, ALARM_STATUS = 1, ALARM_INFO = 2, ALARM_ACTIVE = 3;

    public SecurityFragmentRecyclerAdapter(ArrayList<SimpleDividerCard> alarmCards) {
        this.alarmCards = alarmCards;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        RecyclerView.ViewHolder viewHolder;
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View view;
        switch (viewType) {
            case ALARM_TOP:
                view = inflater.inflate(R.layout.card_promon_top, viewGroup, false);
                viewHolder = new AlarmTopCardItemView(view);
                break;
            case ALARM_STATUS:
                view = inflater.inflate(R.layout.card_alarm_status, viewGroup, false);
                viewHolder = new AlarmStatusCardItemView(view);
                break;
            case ALARM_INFO:
                view = inflater.inflate(R.layout.card_alarm_info, viewGroup, false);
                viewHolder = new AlarmInfoCardItemView(view);
                break;
            case ALARM_ACTIVE:
                view = inflater.inflate(R.layout.alarm_active_card, viewGroup, false);
                viewHolder = new AlarmActiveCardView(view);
                break;
            default:
                view = inflater.inflate(R.layout.card_empty, viewGroup, false);
                viewHolder = new RecyclerViewEmptyCard(view);
                break;
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if(alarmCards.get(position) instanceof AlarmTopCard) {
            ((AlarmTopCardItemView)holder).build((AlarmTopCard)alarmCards.get(position));
        } else if(alarmCards.get(position) instanceof AlarmStatusCard) {
            ((AlarmStatusCardItemView)holder).build((AlarmStatusCard)alarmCards.get(position));
        } else if(alarmCards.get(position) instanceof AlarmInfoCard) {
            ((AlarmInfoCardItemView)holder).build((AlarmInfoCard)alarmCards.get(position));
        } else if(alarmCards.get(position) instanceof AlarmActiveCard) {
            ((AlarmActiveCardView)holder).build((AlarmActiveCard)alarmCards.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return this.alarmCards.size();
    }

    @Override
    public int getItemViewType(int position) {
        if(alarmCards.get(position) instanceof AlarmTopCard) {
            return ALARM_TOP;
        } else if(alarmCards.get(position) instanceof AlarmStatusCard) {
            return ALARM_STATUS;
        } else if(alarmCards.get(position) instanceof AlarmInfoCard) {
            return ALARM_INFO;
        } else if(alarmCards.get(position) instanceof AlarmActiveCard) {
            return ALARM_ACTIVE;
        }
        return -1;
    }

    public void add(SimpleDividerCard card) {
        alarmCards.add(card);
        notifyDataSetChanged();
    }

    public void addAtStart(SimpleDividerCard card) {
        alarmCards.add(0, card);
        notifyDataSetChanged();
    }

    public void removeAll() {
        alarmCards.clear();
        notifyDataSetChanged();
    }

    public void removeAt(int position) {
        alarmCards.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, alarmCards.size());
    }
}

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
package arcus.app.subsystems.alarm.promonitoring.adapters;

import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import arcus.app.R;
import arcus.app.subsystems.alarm.promonitoring.models.AlarmStatusModel;
import arcus.app.subsystems.alarm.promonitoring.models.AlertingAlarmStatusModel;
import arcus.app.subsystems.alarm.promonitoring.models.InactiveAlarmStatusModel;
import arcus.app.subsystems.alarm.promonitoring.models.PanicAlarmStatusModel;
import arcus.app.subsystems.alarm.promonitoring.models.SafetyAlarmStatusModel;
import arcus.app.subsystems.alarm.promonitoring.models.SecurityAlarmStatusModel;
import arcus.app.subsystems.alarm.promonitoring.views.ProMonitoringAlarmAlertingItemView;
import arcus.app.subsystems.alarm.promonitoring.views.ProMonitoringAlarmInactiveItemView;
import arcus.app.subsystems.alarm.promonitoring.views.ProMonitoringAlarmPanicItemView;
import arcus.app.subsystems.alarm.promonitoring.views.ProMonitoringAlarmSafetyItemView;
import arcus.app.subsystems.alarm.promonitoring.views.ProMonitoringAlarmSecurityItemView;

import java.util.ArrayList;
import java.util.List;


public class ProMonitoringAlarmTypeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements ProMonitoringAlarmSecurityItemView.SecurityCallback {

    private List<AlarmStatusModel> alarmStatusModels = new ArrayList<>();
    private final int SECURITY = 0, SAFETY = 1, ALERTING = 2, INACTIVE = 3, PANIC = 4;
    private Callback callback;

    @Override
    public void arm() {
        if(callback != null) {
            callback.arm();
        }
    }

    @Override
    public void partial() {
        if(callback != null) {
            callback.partial();
        }
    }

    @Override
    public void disarm() {
        if(callback != null) {
            callback.disarm();
        }
    }


    public interface Callback {
        void onItemClicked(View child);
        void arm();
        void disarm();
        void partial();
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public ProMonitoringAlarmTypeAdapter(List<AlarmStatusModel> alarmStatusModels) {
        this.alarmStatusModels = alarmStatusModels;
    }

    public AlarmStatusModel getItemAt(int position) {
        if(position < alarmStatusModels.size()) {
            return alarmStatusModels.get(position);
        }
        return null;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        RecyclerView.ViewHolder viewHolder;
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());

        View view;
        switch (viewType) {
            case SECURITY:
                view = inflater.inflate(R.layout.item_promon_alarm_security, viewGroup, false);
                viewHolder = new ProMonitoringAlarmSecurityItemView(view);
                break;
            case SAFETY:
                view = inflater.inflate(R.layout.item_promon_alarm_safety, viewGroup, false);
                viewHolder = new ProMonitoringAlarmSafetyItemView(view);
                break;
            case INACTIVE:
                view = inflater.inflate(R.layout.item_promon_alarm_inactive, viewGroup, false);
                viewHolder = new ProMonitoringAlarmInactiveItemView(view);
                break;
            case PANIC:
                view = inflater.inflate(R.layout.item_promon_alarm_safety, viewGroup, false);
                viewHolder = new ProMonitoringAlarmPanicItemView(view);
                break;
            case ALERTING:
            default:
                view = inflater.inflate(R.layout.item_promon_alarm_alerting, viewGroup, false);
                viewHolder = new ProMonitoringAlarmAlertingItemView(view);
                break;
        }
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(callback != null) {
                    callback.onItemClicked(view);
                }
            }
        });

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        AlarmStatusModel model = alarmStatusModels.get(position);

        if(model instanceof SecurityAlarmStatusModel) {
            ((ProMonitoringAlarmSecurityItemView)holder).build((SecurityAlarmStatusModel)model);
            ((ProMonitoringAlarmSecurityItemView)holder).setSecurityListener(this);
        } else if(model instanceof SafetyAlarmStatusModel) {
            ((ProMonitoringAlarmSafetyItemView)holder).build((SafetyAlarmStatusModel)model);
        } else if(model instanceof InactiveAlarmStatusModel) {
            ((ProMonitoringAlarmInactiveItemView)holder).build((InactiveAlarmStatusModel)model);
        } else if(model instanceof AlertingAlarmStatusModel) {
            ((ProMonitoringAlarmAlertingItemView)holder).build((AlertingAlarmStatusModel)model);
        } else if (model instanceof PanicAlarmStatusModel) {
            ((ProMonitoringAlarmPanicItemView)holder).build((PanicAlarmStatusModel)model);
        }
    }

    @Override
    public int getItemCount() {
        return this.alarmStatusModels.size();
    }
    @Override
    public int getItemViewType(int position) {
        if (alarmStatusModels.get(position) instanceof SecurityAlarmStatusModel) {
            return SECURITY;
        } else if (alarmStatusModels.get(position) instanceof SafetyAlarmStatusModel) {
            return SAFETY;
        } else if (alarmStatusModels.get(position) instanceof InactiveAlarmStatusModel) {
            return INACTIVE;
        } else if (alarmStatusModels.get(position) instanceof AlertingAlarmStatusModel) {
            return ALERTING;
        } else if (alarmStatusModels.get(position) instanceof PanicAlarmStatusModel) {
            return PANIC;
        }
        return -1;
    }

    public void add(AlarmStatusModel model) {
        alarmStatusModels.add(model);
        notifyDataSetChanged();
    }
}

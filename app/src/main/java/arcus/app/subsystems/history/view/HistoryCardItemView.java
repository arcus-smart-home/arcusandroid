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
package arcus.app.subsystems.history.view;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.iris.client.bean.HistoryLog;
import arcus.app.R;
import arcus.app.common.utils.CorneaUtils;
import arcus.app.common.view.Version1TextView;
import arcus.app.subsystems.history.cards.HistoryCard;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;


public class HistoryCardItemView extends RecyclerView.ViewHolder {
    @NonNull
    private int[][] layoutIDs = {
          {R.id.card_date_1, R.id.card_time_1, R.id.card_actor_1, R.id.card_subject_1},
          {R.id.card_date_2, R.id.card_time_2, R.id.card_actor_2, R.id.card_subject_2},
          {R.id.card_date_3, R.id.card_time_3, R.id.card_actor_3, R.id.card_subject_3}
    };
    @NonNull
    private int[][] rowIDs = {
          {R.id.card_history_row_1, R.id.card_history_row_1_1, R.id.card_history_row_1_2},
          {R.id.card_history_row_2, R.id.card_history_row_2_1, R.id.card_history_row_2_2},
          {R.id.card_history_row_3, R.id.card_history_row_3_1, R.id.card_history_row_3_2}
    };
    private final SimpleDateFormat headerDateFormat1 = new SimpleDateFormat("MMM d", Locale.US);
    private final SimpleDateFormat headerDateFormat2 = new SimpleDateFormat("h:mm a", Locale.US);
    Context context;
    View historyTable;
    View noHistoryItems;
    View historyLayout;
    private ImageView serviceImage;
    private Version1TextView serviceName;

    public HistoryCardItemView(View view) {
        super(view);
        context = view.getContext();
        serviceImage = (ImageView) view.findViewById(R.id.service_image);
        serviceName = (Version1TextView) view.findViewById(R.id.service_name);
        historyTable = view.findViewById(R.id.history_table);
        noHistoryItems = view.findViewById(R.id.layout_no_items);
        historyLayout = view.findViewById(R.id.history_layout);
    }

    public void build(@NonNull HistoryCard card) {
        serviceName.setText(context.getString(R.string.card_history_title));
        serviceImage.setImageResource(R.drawable.history);
        if (card.getEntries() == null || card.getEntries().getEntries().size() == 0) {
            showAllRows(historyTable, false);
            noHistoryItems.setVisibility(View.VISIBLE);
            historyLayout.setVisibility(View.GONE);
        }
        else {
            noHistoryItems.setVisibility(View.GONE);
            historyLayout.setVisibility(View.VISIBLE);
            showAllRows(historyTable, true);
            showRowsWithData(card, historyTable);
        }
    }

    private void showAllRows(@NonNull View historyTable, boolean show) {
        for (int i = 0; i < 3; i++) {
            historyTable.findViewById(rowIDs[i][0]).setVisibility(show ? View.VISIBLE : View.GONE);
            historyTable.findViewById(rowIDs[i][1]).setVisibility(show ? View.VISIBLE : View.GONE);
            historyTable.findViewById(rowIDs[i][2]).setVisibility(show ? View.VISIBLE : View.GONE);
        }
        historyTable.findViewById(rowIDs[2][2]).setVisibility(View.GONE);
    }

    private void showRowsWithData(@NonNull HistoryCard card, @NonNull View historyTable) {
        List<HistoryLog> historyLogEntries = new ArrayList<>(card.getEntries().getEntries());
        Collections.sort(historyLogEntries, CorneaUtils.DESC_HISTORY_LOG_COMPARATOR);
        Calendar calendar = Calendar.getInstance();
        int today = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
        for (int i = 0; i < 3; i++) {
            if (historyLogEntries.size() > i) {
                TextView date = (TextView) historyTable.findViewById(layoutIDs[i][0]);
                TextView time = (TextView) historyTable.findViewById(layoutIDs[i][1]);
                TextView actor = (TextView) historyTable.findViewById(layoutIDs[i][2]);
                TextView subject = (TextView) historyTable.findViewById(layoutIDs[i][3]);

                calendar.setTime(historyLogEntries.get(i).getTimestamp());
                if (today != calendar.get(Calendar.DAY_OF_YEAR)) {
                    date.setText(headerDateFormat1.format(historyLogEntries.get(i).getTimestamp()));
                }
                else {
                    date.setText("");
                }
                time.setText(headerDateFormat2.format(historyLogEntries.get(i).getTimestamp()));
                actor.setText(historyLogEntries.get(i).getSubjectName());
                subject.setText(historyLogEntries.get(i).getLongMessage().trim());
                historyTable.findViewById(rowIDs[i][0]).setVisibility(View.VISIBLE);
                historyTable.findViewById(rowIDs[i][1]).setVisibility(View.VISIBLE);
                historyTable.findViewById(rowIDs[i][2]).setVisibility(View.VISIBLE);
            }
            else { // Didn't have a history entry (Size < 3 when querying), hide the rows we don't have data for.
                historyTable.findViewById(rowIDs[i][0]).setVisibility(View.GONE);
                historyTable.findViewById(rowIDs[i][1]).setVisibility(View.GONE);
                historyTable.findViewById(rowIDs[i][2]).setVisibility(View.GONE);
            }
        }
        historyTable.findViewById(rowIDs[2][2]).setVisibility(View.GONE);
    }
}

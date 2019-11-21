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
package arcus.app.subsystems.care.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.iris.client.model.DeviceModel;
import arcus.app.R;
import arcus.app.common.image.ImageManager;
import arcus.app.common.image.picasso.transformation.BlackWhiteInvertTransformation;
import arcus.app.common.image.picasso.transformation.Invert;
import arcus.app.common.models.ListItemModel;

import java.util.ArrayList;
import java.util.List;

public class CareFilterDevicesAdapter extends RecyclerView.Adapter<CareFilterDevicesAdapter.CareFilterItemViewHolder> {
    private List<ListItemModel> displayedItems;
    private Context context;
    private RVItemClicked itemClickedListener;
    private int whiteSixty = -1;
    private int lastChecked = 0;

    public interface RVItemClicked {
        void itemClicked(@Nullable String address, int adapterPosition);
    }

    public CareFilterDevicesAdapter(
          @NonNull Context context,
          @NonNull List<ListItemModel> items
    ) {
        this.context = context;
        this.whiteSixty = context.getResources().getColor(R.color.overlay_white_with_60);
        this.displayedItems = new ArrayList<>(items);
    }

    @Override public CareFilterItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View inflated = LayoutInflater.from(context).inflate(R.layout.cell_care_devices, parent, false);
        return new CareFilterItemViewHolder(inflated);
    }

    public void setItemClickedListener(RVItemClicked clickedListener) {
        this.itemClickedListener = clickedListener;
    }

    @Override public void onBindViewHolder(CareFilterItemViewHolder holder, final int position) {
        ListItemModel item = displayedItems.get(position);

        String title = TextUtils.isEmpty(item.getText()) ? "" : item.getText();
        holder.title.setText(title);
        holder.title.setTextColor(item.isChecked() ? Color.WHITE : whiteSixty);
        holder.address = item.getAddress();

        holder.title.setTag(holder);
        holder.imageView.setTag(holder);
        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                RVItemClicked listener = itemClickedListener;
                if (listener == null) {
                    return;
                }

                CareFilterItemViewHolder itemTag = ((CareFilterItemViewHolder)v.getTag());
                String address = itemTag.getAddress();
                int position = itemTag.getAdapterPosition();
                displayedItems.get(lastChecked).setChecked(false);
                notifyItemChanged(lastChecked);

                lastChecked = position;
                displayedItems.get(position).setChecked(true);

                notifyItemChanged(position);
                listener.itemClicked(address, position);
            }
        });

        if (item.getImageResId() != null && context != null) {
            BlackWhiteInvertTransformation transform = new BlackWhiteInvertTransformation(Invert.BLACK_TO_WHITE);
            Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), item.getImageResId());
            holder.imageView.setImageBitmap(transform.transform(bitmap));
        }
        else if (item.getData() != null && (item.getData() instanceof DeviceModel) && context != null) {
            ImageManager.with(context)
                  .putSmallDeviceImage((DeviceModel) item.getData())
                  .withTransformForStockImages(new BlackWhiteInvertTransformation(Invert.BLACK_TO_WHITE))
                  .withPlaceholder(R.drawable.device_list_placeholder)
                  .withError(R.drawable.device_list_placeholder)
                  .noUserGeneratedImagery()
                  .into(holder.imageView)
                  .execute();
        }
        else {
            holder.imageView.setImageBitmap(null);
        }

        holder.imageView.setAlpha(item.isChecked() ? 1.0f : 0.4f);
    }

    @Override public int getItemCount() {
        return displayedItems == null ? 0 : displayedItems.size();
    }

    class CareFilterItemViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        ImageView imageView;
        String address;

        public CareFilterItemViewHolder(View itemView) {
            super(itemView);
            this.title = (TextView) itemView.findViewById(R.id.label);
            this.imageView = (ImageView) itemView.findViewById(R.id.image);
        }

        public @Nullable String getAddress() {
            return address;
        }

    }
}

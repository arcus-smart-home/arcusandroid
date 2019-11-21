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
package arcus.app.subsystems.favorites.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.iris.client.model.DeviceModel;
import arcus.app.R;
import arcus.app.common.adapters.AbstractListAdapter;
import arcus.app.common.image.ImageManager;
import arcus.app.common.image.picasso.transformation.BlackWhiteInvertTransformation;
import arcus.app.common.image.picasso.transformation.Invert;


public class FavoritesListAdapter extends AbstractListAdapter<FavoriteItemModel, RecyclerView.ViewHolder> {

    private static final int TYPE_ADD = 0;
    private static final int TYPE_FAVORITE = 1;

    private final Context mContext;

    public FavoritesListAdapter(Context context) {
        mContext = context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        switch (viewType){
            case TYPE_FAVORITE:
                return new FavoritesViewHolder(inflater.inflate(R.layout.cell_dashboard_favorites, viewGroup, false));
            default:
                return new AddToFavoritesViewHolder(inflater.inflate(R.layout.add_to_favorites_view, viewGroup, false));
        }
    }

    @Override
    public int getItemViewType(int position) {
        return (position == mData.size() ? TYPE_ADD : TYPE_FAVORITE);
    }

    @Override
    public int getItemCount() {
        return mData.size() + 1;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch(holder.getItemViewType()) {
            case TYPE_FAVORITE:
                ((FavoritesViewHolder) holder).bind(mData.get(position));
                break;
            default:
                ((AddToFavoritesViewHolder) holder).bind();
        }
    }

    public class FavoritesViewHolder extends RecyclerView.ViewHolder {
        @NonNull private final TextView mTextView;
        @NonNull private final ImageView mImageView;

        public FavoritesViewHolder(@NonNull View v) {
            super(v);
            mTextView = (TextView) v.findViewById(R.id.label);
            mImageView = (ImageView) v.findViewById(R.id.image);
        }

        public void bind(@NonNull FavoriteItemModel favoriteItemModel) {
            mTextView.setText(favoriteItemModel.getTitle());
            mTextView.setAlpha(favoriteItemModel.isDisabled() ? 0.4f : 1.0f);

            if (favoriteItemModel.getImageResource() != null) {
                BlackWhiteInvertTransformation transform = new BlackWhiteInvertTransformation(Invert.BLACK_TO_WHITE);
                Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), favoriteItemModel.getImageResource());
                if(favoriteItemModel.getKeepImageColor()) {
                    mImageView.setImageBitmap(bitmap);
                }
                else {
                    mImageView.setImageBitmap(transform.transform(bitmap));
                }
            } else {
                ImageManager.with(mContext)
                        .putSmallDeviceImage((DeviceModel) favoriteItemModel.getModel())
                        .withTransformForStockImages(new BlackWhiteInvertTransformation(Invert.BLACK_TO_WHITE))
                        .withPlaceholder(R.drawable.device_list_placeholder)
                        .withError(R.drawable.device_list_placeholder)
                        .noUserGeneratedImagery()
                        .into(mImageView)
                        .execute();
            }

            mImageView.setAlpha(favoriteItemModel.isDisabled() ? 0.4f : 1.0f);
        }

        @NonNull
        public TextView getTextView() {
            return mTextView;
        }

        @NonNull
        @Override
        public String toString() {
            return "FavoritesViewHolder{" + mTextView.getText() + "}";
        }
    }

    public class AddToFavoritesViewHolder extends RecyclerView.ViewHolder {
        private final AppCompatImageView image;

        AddToFavoritesViewHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.image);
        }

        public void bind() {
            image.setVisibility(View.VISIBLE);
            ImageManager.with(itemView.getContext())
                .putDrawableResource(R.drawable.favorite_45x45)
                .withTransformForStockImages(new BlackWhiteInvertTransformation(Invert.BLACK_TO_WHITE))
                .into(image)
                .execute();
         }
    }
}

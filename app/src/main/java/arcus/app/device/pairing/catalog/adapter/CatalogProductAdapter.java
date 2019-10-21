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
package arcus.app.device.pairing.catalog.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.iris.client.model.ProductModel;
import arcus.app.R;
import arcus.app.common.image.ImageManager;
import arcus.app.common.models.ListItemModel;
import arcus.app.device.pairing.catalog.model.ProductCatalogEntry;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class CatalogProductAdapter extends ArrayAdapter<ListItemModel> {

    private final boolean isSearchResult;

    public CatalogProductAdapter(final Context context, @NonNull List<ListItemModel> detail, boolean isSearchResult) {
        super(context, 0);
        this.isSearchResult = isSearchResult;

        // Move all Arcus-branded products to the top of the list
        Collections.sort(detail, new Comparator<ListItemModel>() {
            @Override
            public int compare(ListItemModel lhs, ListItemModel rhs) {
                String arcusBrand = context.getString(R.string.catalog_arcus_brand);

                // Special case: Both elements are Arcus branded
                if (lhs.getSubText().equalsIgnoreCase(arcusBrand) && rhs.getSubText().equalsIgnoreCase(arcusBrand)) {
                    if(lhs.getText().startsWith("1st") && rhs.getText().startsWith("1st")) {
                        return lhs.getText().compareTo(rhs.getText());
                    } else if(lhs.getText().startsWith("1st")) {
                        return 1;
                    } else if(rhs.getText().startsWith("1st")) {
                        return -1;
                    } else {
                        return lhs.getText().compareTo(rhs.getText());
                    }
                }

                // Special case: LHS is Arcus branded
                if (lhs.getSubText().equalsIgnoreCase(arcusBrand)) {
                    return -1;
                }

                // Special case: RHS is Arcus branded
                if (rhs.getSubText().equalsIgnoreCase(arcusBrand)) {
                    return 1;
                }

                // Normal case: Alphabetize by title
                return lhs.getText().compareTo(rhs.getText());
            }
        });

        addAll(detail);
    }

    protected static class ProductDetailViewHolder {
        public ImageView categoryImageView;
        public ImageView chevronImageView;
        public TextView text;
        public TextView subText;
    }

    @Nullable
    @Override
    public View getView(int position, @Nullable View convertView, ViewGroup parent) {

        final ProductDetailViewHolder viewHolder;
        ListItemModel catalog = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.category_item, parent, false);

            viewHolder = new ProductDetailViewHolder();
            viewHolder.categoryImageView = (ImageView) convertView.findViewById(R.id.imgCategory);
            viewHolder.chevronImageView = (ImageView) convertView.findViewById(R.id.imgChevron);
            viewHolder.text = (TextView) convertView.findViewById(R.id.tvText);
            viewHolder.subText = (TextView) convertView.findViewById(R.id.tvSubText);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ProductDetailViewHolder) convertView.getTag();
        }

        viewHolder.chevronImageView.setImageResource(R.drawable.button_add);
        viewHolder.text.setText(catalog.getText());
        viewHolder.categoryImageView.setImageResource(R.drawable.icon_cat_placeholder);
        viewHolder.subText.setText(catalog.getSubText());

        // ListItemModel.getData may return either a ProductCatalogEntry or a ProductModel;
        // need to pass the full object--not just an id--to ImageManger in order to do image fallback
        if (catalog.getData() instanceof ProductCatalogEntry) {
            ImageManager.with(getContext())
                    .putSmallProductImage((ProductCatalogEntry) catalog.getData())
                    .withPlaceholder(R.drawable.icon_cat_placeholder)
                    .withError(R.drawable.icon_cat_placeholder)
                    .into(viewHolder.categoryImageView)
                    .execute();
        }
        else if (catalog.getData() instanceof ProductModel) {
            ImageManager.with(getContext())
                    .putSmallProductImage((ProductModel) catalog.getData())
                    .withPlaceholder(R.drawable.icon_cat_placeholder)
                    .withError(R.drawable.icon_cat_placeholder)
                    .into(viewHolder.categoryImageView)
                    .execute();
        }

        return convertView;
    }
}

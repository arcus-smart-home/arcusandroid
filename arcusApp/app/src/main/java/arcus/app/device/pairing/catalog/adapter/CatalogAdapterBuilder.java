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

import android.app.Activity;
import android.widget.ListAdapter;

import arcus.cornea.dto.ProductBrandAndCount;
import arcus.cornea.dto.ProductCategoryAndCount;
import com.iris.client.model.ProductModel;
import arcus.app.R;
import arcus.app.common.models.ListItemModel;
import arcus.app.device.pairing.catalog.model.ProductCatalogEntry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;


public class CatalogAdapterBuilder {

    private final Activity activity;

    private CatalogAdapterBuilder (Activity activity) {
        this.activity = activity;
    }

    public static CatalogAdapterBuilder in (Activity activity) {
        return new CatalogAdapterBuilder(activity);
    }

    public ListAdapter buildProductListByEntry (List<Map<String, Object>> productDataEntries, boolean isSearchResult, boolean hideHubRequiredDevices) {
        ArrayList<ListItemModel> listItems = new ArrayList<>();

        for (Map<String, Object> productDataEntry : productDataEntries) {

            ProductCatalogEntry product = new ProductCatalogEntry(productDataEntry);
            if (hideHubRequiredDevices && product.isHubRequired()) {
                continue;
            }

            ListItemModel listData = new ListItemModel();

            listData.setText(product.getProductName());
            listData.setSubText(product.getVendor());
            listData.setData(product);

            listItems.add(listData);
        }

        return new CatalogProductAdapter(activity, listItems, isSearchResult);
    }

    public ListAdapter buildProductListByModel (List<ProductModel> productModels, boolean isSearchResult) {
        ArrayList<ListItemModel> listItems = new ArrayList<>();

        for (ProductModel productModel : productModels) {

            ListItemModel listData = new ListItemModel();

            listData.setText(productModel.getName());
            listData.setSubText(productModel.getVendor());
            listData.setData(productModel);

            listItems.add(listData);
        }

        return new CatalogProductAdapter(activity, listItems, isSearchResult);
    }


    public ListAdapter buildBrandList (List<ProductBrandAndCount> catalogItems) {
        ArrayList<ListItemModel> listItems = new ArrayList<>();

        for (ProductBrandAndCount catalogItem : catalogItems) {

            ListItemModel listData = new ListItemModel();

            listData.setText(catalogItem.getName());
            listData.setSubText(activity.getResources().getQuantityString(R.plurals.devices_plural, catalogItem.getCount(), catalogItem.getCount()));
            listData.setCount(catalogItem.getCount());

            listItems.add(listData);
        }

        Collections.sort(listItems, new ArcusOnTopComparator());
        return new CatalogBrandAdapter(activity, listItems);
    }

    public ListAdapter buildCategoryList (List<ProductCategoryAndCount> catalogItems) {
        ArrayList<ListItemModel> listItems = new ArrayList<>();

        for (ProductCategoryAndCount catalogItem : catalogItems) {
            ListItemModel listItem = new ListItemModel();

            listItem.setText(catalogItem.getName());
            listItem.setSubText(activity.getResources().getQuantityString(R.plurals.devices_plural, catalogItem.getCount(), catalogItem.getCount()));
            listItem.setCount(catalogItem.getCount());

            listItems.add(listItem);
        }

        Collections.sort(listItems, new ListModelTextComparator());
        return new CatalogCategoryAdapter(activity, listItems);
    }
}

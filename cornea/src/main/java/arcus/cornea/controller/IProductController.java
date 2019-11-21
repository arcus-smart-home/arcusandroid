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
package arcus.cornea.controller;

import androidx.annotation.NonNull;

import com.google.common.base.Predicate;
import arcus.cornea.dto.ProductBrandAndCount;
import arcus.cornea.dto.ProductCategoryAndCount;
import com.iris.client.event.ClientFuture;
import com.iris.client.model.ProductModel;

import java.util.List;

public interface IProductController {
    String SERVICE_ADDRESS = "SERV:prodcat:";
    String NAME_ENTRY = "name";

    Predicate<ProductModel> HUB_REQUIRED_DEVICES_FILTER = new Predicate<ProductModel>() {
        @Override
        public boolean apply(ProductModel productModel) {
            return Boolean.TRUE.equals(productModel.getHubRequired());
        }
    };
    Predicate<ProductModel> HUB_NOT_REQUIRED_DEVICES_FILTER = new Predicate<ProductModel>() {
        @Override
        public boolean apply(ProductModel productModel) {
            return Boolean.FALSE.equals(productModel.getHubRequired());
        }
    };

    ClientFuture<List<ProductModel>> getProducts(Predicate<ProductModel> matchingFilter);
    ClientFuture<List<ProductBrandAndCount>> getBrands(Predicate<ProductModel> matchingFilter);
    ClientFuture<List<ProductCategoryAndCount>> getCategories();
    ClientFuture<List<ProductCategoryAndCount>> getCategories(Predicate<ProductModel> matchingFilter);

    ClientFuture<List<ProductModel>> getByBrandName(@NonNull String brand, Predicate<ProductModel> matchingFilter);
    ClientFuture<List<ProductModel>> getByCategoryName(@NonNull String category, Predicate<ProductModel> matchingFilter);
    ClientFuture<ProductModel>       getByProductID(@NonNull String product);
}

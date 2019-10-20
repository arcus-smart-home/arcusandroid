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
package arcus.cornea;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import arcus.cornea.controller.IProductController;
import arcus.cornea.dto.ProductBrandAndCount;
import arcus.cornea.dto.ProductCategoryAndCount;
import arcus.cornea.provider.ProductModelProvider;
import com.iris.client.capability.Product;
import com.iris.client.capability.ProductCatalog;
import com.iris.client.event.ClientFuture;
import com.iris.client.event.Futures;
import com.iris.client.event.Listener;
import com.iris.client.event.SettableClientFuture;
import com.iris.client.model.Model;
import com.iris.client.model.ProductModel;
import com.iris.client.service.ProductCatalogService;
import com.iris.client.util.Result;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class ProductController implements IProductController {

    private boolean getProductsCalled = false;
    private final Predicate<ProductModel> browsableProducts = new Predicate<ProductModel>() {
        @Override public boolean apply(ProductModel input) {
            return Boolean.TRUE.equals(input.get(Product.ATTR_CANBROWSE));
        }
    };

    public ClientFuture<List<ProductBrandAndCount>> getBrands(@Nullable final Predicate<ProductModel> matchingCondition) {

        return Futures.transform(getProducts(), new Function<List<ProductModel>, List<ProductBrandAndCount>>() {
            @Override
            public List<ProductBrandAndCount> apply(List<ProductModel> productModels) {
                List<ProductBrandAndCount> brands = new ArrayList<>();
                Map<String, Integer> counts = new HashMap<>();

                for (ProductModel thisProduct : productModels) {

                    // Skip products that don't match the condition; include all products if condition is null
                    if (!getEffectivePredicate(matchingCondition).apply(thisProduct)) {
                        continue;
                    }

                    if (counts.containsKey(thisProduct.getVendor())) {
                        counts.put(thisProduct.getVendor(), counts.get(thisProduct.getVendor()) + 1);
                    } else {
                        counts.put(thisProduct.getVendor(), 1);
                    }
                }

                for (String thisBrand : counts.keySet()) {
                    brands.add(new ProductBrandAndCount(thisBrand, counts.get(thisBrand)));
                }

                return brands;
            }
        });
    }

    @Override
    public ClientFuture<List<ProductCategoryAndCount>> getCategories() {
        return getCategories(null);
    }

    @Override
    public ClientFuture<List<ProductCategoryAndCount>> getCategories(@Nullable final Predicate<ProductModel> matchingCondition) {

        return Futures.transform(getProducts(), new Function<List<ProductModel>, List<ProductCategoryAndCount>>() {
            @Override
            public List<ProductCategoryAndCount> apply(List<ProductModel> productModels) {
                List<ProductCategoryAndCount> categories = new ArrayList<>();
                Map<String, Integer> counts = new HashMap<>();

                for (ProductModel thisProduct : productModels) {

                    // Skip products that don't match the condition; include all products if condition is null
                    if (!getEffectivePredicate(matchingCondition).apply(thisProduct)) {
                        continue;
                    }

                    for (String thisCategory : thisProduct.getCategories()) {
                        if (counts.containsKey(thisCategory)) {
                            counts.put(thisCategory, counts.get(thisCategory) + 1);
                        } else {
                            counts.put(thisCategory, 1);
                        }
                    }
                }

                for (String thisCategory : counts.keySet()) {
                    categories.add(new ProductCategoryAndCount(thisCategory, counts.get(thisCategory)));
                }

                return categories;
            }
        });
    }

    private ClientFuture<List<ProductModel>> getProducts() {
        CorneaClientFactory.getStore(ProductModel.class);

        if (CorneaClientFactory.getStore(ProductModel.class).size() > 0) {
            getProductsCalled = true;
            return Futures.succeededFuture(getCachedProductModels());
        } else {
            return doGetProducts();
        }
    }

    private ClientFuture<List<ProductModel>> doGetProducts() {
        final SettableClientFuture<List<ProductModel>> clientResults = new SettableClientFuture<>();
        CorneaClientFactory
                .getService(ProductCatalogService.class)
                .getProducts(
                        ProductModelProvider.getActivePlaceAddress(),
                        ProductCatalogService.GetProductsRequest.INCLUDE_ALL,
                        null)
                .onCompletion(new Listener<Result<ProductCatalogService.GetProductsResponse>>() {
            @Override
            public void onEvent(Result<ProductCatalogService.GetProductsResponse> result) {
                if (result.isError()) {
                    clientResults.setError(result.getError());
                } else {
                    ProductCatalog.GetProductsResponse response = new ProductCatalog.GetProductsResponse(result.getValue());
                    if (!CorneaClientFactory.getModelCache().retainAll(Product.NAMESPACE, response.getProducts()).isEmpty()) {
                        getProductsCalled = true;
                    }
                    clientResults.setValue(Lists.newArrayList(Iterables.filter(CorneaClientFactory.getStore(ProductModel.class).values(), browsableProducts)));
                }
            }
        });

        return clientResults;
    }

    @Override
    public ClientFuture<List<ProductModel>> getProducts(@Nullable final Predicate<ProductModel> matchingFilter) {
        return Futures.transform(getProducts(), new Function<List<ProductModel>, List<ProductModel>>() {
            @Override
            public List<ProductModel> apply(List<ProductModel> productModels) {
                List<ProductModel> products = new ArrayList<>();

                for (ProductModel thisProduct : products) {
                    if (getEffectivePredicate(matchingFilter).apply(thisProduct)) {
                        products.add(thisProduct);
                    }
                }

                return products;
            }
        });
    }

    @Override
    public ClientFuture<List<ProductModel>> getByBrandName(@NonNull final String brand, @Nullable final Predicate<ProductModel> matchingFilter) {
        Preconditions.checkNotNull(brand, "Brand cannot be null");

        return Futures.transform(getProducts(), new Function<List<ProductModel>, List<ProductModel>>() {
            @Override
            public List<ProductModel> apply(List<ProductModel> productModels) {
                List<ProductModel> products = new ArrayList<>();

                for (ProductModel thisProduct : productModels) {
                    if (thisProduct.getVendor().equalsIgnoreCase(brand) && getEffectivePredicate(matchingFilter).apply(thisProduct)) {
                        products.add(thisProduct);
                    }
                }

                return products;
            }
        });
    }

    @Override
    public ClientFuture<List<ProductModel>> getByCategoryName(@NonNull final String category, @Nullable final Predicate<ProductModel> matchingFilter) {
        Preconditions.checkNotNull(category, "Category cannot be null");

        return Futures.transform(getProducts(), new Function<List<ProductModel>, List<ProductModel>>() {
            @Override
            public List<ProductModel> apply(List<ProductModel> productModels) {
                List<ProductModel> products = new ArrayList<>();

                for (ProductModel thisProduct : productModels) {
                    if (thisProduct.getCategories() != null && thisProduct.getCategories().contains(category) && getEffectivePredicate(matchingFilter).apply(thisProduct)) {
                        products.add(thisProduct);
                    }
                }

                return products;
            }
        });
    }

    @Override
    public ClientFuture<ProductModel> getByProductID(@NonNull String productID) {
        Preconditions.checkNotNull(productID, "Product ID cannot be null");

        List<ProductModel> productModels = getCachedProductModels();
        ProductModel output = null;
        for (ProductModel model : productModels) {
            if (model.getId().equals(productID)) {
                output = model;
                break;
            }
        }

        if (output != null) {
            return Futures.succeededFuture(output);
        } else {
            return doGetByProductID(productID);
        }
    }

    private ClientFuture<ProductModel> doGetByProductID(String productID) {
        final SettableClientFuture<ProductModel> clientResults = new SettableClientFuture<>();
        //TODO: ProductCatalog - add place id
        CorneaClientFactory.getService(ProductCatalogService.class).getProduct(null, productID).onCompletion(new Listener<Result<ProductCatalogService.GetProductResponse>>() {
            @Override
            public void onEvent(Result<ProductCatalogService.GetProductResponse> result) {
                if (result.isError()) {
                    clientResults.setError(result.getError());
                } else {
                    ProductCatalog.GetProductResponse response = new ProductCatalog.GetProductResponse(result.getValue());
                    Model model = CorneaClientFactory.getModelCache().addOrUpdate(response.getProduct());

                    clientResults.setValue(CorneaClientFactory.getStore(ProductModel.class).get(model.getId()));
                }
            }
        });

        return clientResults;
    }

    private List<ProductModel> getCachedProductModels() {
        return Lists.newArrayList(Iterables.filter(CorneaClientFactory.getStore(ProductModel.class).values(), browsableProducts));
    }

    private Predicate<ProductModel> getEffectivePredicate (Predicate<ProductModel> predicate) {
        return predicate == null ? Predicates.<ProductModel>alwaysTrue() : predicate;
    }
}

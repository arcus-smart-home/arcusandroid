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
package arcus.cornea.provider;

import androidx.annotation.Nullable;
import android.text.TextUtils;

import arcus.cornea.CorneaClientFactory;
import arcus.cornea.SessionController;
import arcus.cornea.utils.ModelSource;
import com.iris.capability.util.Addresses;
import com.iris.client.IrisClient;
import com.iris.client.capability.Place;
import com.iris.client.capability.Product;
import com.iris.client.capability.ProductCatalog;
import com.iris.client.event.ClientFuture;
import com.iris.client.event.SettableClientFuture;
import com.iris.client.model.ModelCache;
import com.iris.client.model.ProductModel;
import com.iris.client.model.Store;
import com.iris.client.service.ProductCatalogService;

import java.util.List;

// TODO:  this should really be more sophisticated.  Ideally something along these lines:
// * Try to load the product catalog from a physical cache on the device
// * If it does, it should pull down the metadata from the server
// * If there is no local cache or the metadata timestamp on the local cache is < the timestamp on the metadata from the server
//   then refetch the product catalog and cache locally
public class ProductModelProvider extends BaseModelProvider<ProductModel> {
    public static final String UNCERTIFIED = "uncertified";
    public static final String HUB_VENDOR = "iris";

    private static final ProductModelProvider INSTANCE = new ProductModelProvider();

    public static ProductModelProvider instance() {
        return INSTANCE;
    }

    private final ModelCache cache;

    ProductModelProvider() {
        this(
                CorneaClientFactory.getClient(),
                CorneaClientFactory.getModelCache(),
                CorneaClientFactory.getStore(ProductModel.class)
        );
    }

    ProductModelProvider(
            IrisClient client,
            ModelCache cache,
            Store<ProductModel> store
    ) {
        super(client, cache, store);
        this.cache = cache;
    }

    public @Nullable ProductModel getByProductIDOrNull(String productID) {
        if (TextUtils.isEmpty(productID)) {
            return null;
        }

        ModelSource<ProductModel> model = getModel(Addresses.toObjectAddress(Product.NAMESPACE, productID));
        model.load();
        return model.get();
    }

    @SuppressWarnings({"unchecked"})
    @Override
    protected ClientFuture<List<ProductModel>> doLoad(String placeId) {
        final SettableClientFuture<List<ProductModel>> clientResults = new SettableClientFuture<>();
        CorneaClientFactory
                .getService(ProductCatalogService.class)
                .getProducts(
                        getActivePlaceAddress(placeId),
                        ProductCatalogService.GetProductsRequest.INCLUDE_ALL,
                        null
                )
                .onFailure(clientResults::setError)
                .onSuccess(result -> {
                    ProductCatalog.GetProductsResponse response = new ProductCatalog.GetProductsResponse(result);

                    List<ProductModel> listOfModels = (List) cache.retainAll(Product.NAMESPACE, response.getProducts());
                    clientResults.setValue(listOfModels);
                });
        return clientResults;
    }

    public static String getActivePlaceAddress() {
        return getActivePlaceAddress(null);
    }

    public static String getActivePlaceAddress(@Nullable String placeId) {
        String addressPrefix = Addresses.toServiceAddress(Place.NAMESPACE);

        // This should be set before we make any requests to (re)load anything
        String activePlace = SessionController.instance().getActivePlace();
        if (activePlace != null) {
            return addressPrefix + activePlace;
        }

        if (placeId != null) {
            return addressPrefix + placeId;
        } else {
            String currentPlaceId = SessionController.instance().getPlaceIdOrEmpty();
            if (currentPlaceId.length() == 0) {
                return null;
            } else {
                return addressPrefix + currentPlaceId;
            }
        }
    }
}

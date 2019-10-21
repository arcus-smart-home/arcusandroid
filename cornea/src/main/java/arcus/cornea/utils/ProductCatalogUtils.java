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
package arcus.cornea.utils;

import arcus.cornea.provider.ProductModelProvider;
import com.iris.client.model.DeviceModel;
import com.iris.client.model.ProductModel;

import org.apache.commons.lang3.StringUtils;


public class ProductCatalogUtils {

    /**
     * Attempts to determine the product catalog's name for the device identified by
     * the device model. Returns null if this product name cannot be determined (e.g., because the
     * device does not exist in the catalog)
     *
     * @param deviceModel
     * @return
     */
    public static String getProductNameForDevice (DeviceModel deviceModel) {

        if (deviceModel == null || StringUtils.isEmpty(deviceModel.getProductId())) {
            return null;
        }

        ProductModel model = ProductModelProvider.instance().getByProductIDOrNull(deviceModel.getProductId());
        if (model != null && !StringUtils.isEmpty(model.getName()) && !StringUtils.isEmpty(model.getVendor())) {
            return model.getVendor() + " " + model.getName();
        }

        return null;
    }

}

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
package arcus.app.device.pairing.catalog.model;

import androidx.annotation.NonNull;

import java.util.Map;



public class ProductCatalogEntry {

    private Map<String,Object> attributes;  //  contains entire json object

    public ProductCatalogEntry(Map<String,Object> attributes) {
        this.attributes = attributes;
    }

    @NonNull
    public String getProductName() {
        return (String) attributes.get("product:name");
    }


    @NonNull
    public String getBaseAddress() {
        return (String) attributes.get("base:address");
    }


    @NonNull
    public String getAdded() {
        return (String) attributes.get("product:added");
    }


    @NonNull
    public String getVendor() {
        return (String) attributes.get("product:vendor");
    }


    @NonNull
    public String getType() {
        return (String) attributes.get("base:type");
    }


    @NonNull
    public String getDescription() {
        return (String) attributes.get("product:description");
    }


    @NonNull
    public String getId() {
        return (String) attributes.get("product:id");
    }


    @NonNull
    public String getScreen() {
        return(String) attributes.get("product:screen");
    }

    @NonNull
    public boolean isHubRequired() {
        return (Boolean) attributes.get("product:hubRequired");
    }

    public String getMinAppVersion() {
        return(String) attributes.get("product:minAppVersion");
    }

    public String getDeviceRequiredId() {
        return(String) attributes.get("product:devRequired");
    }


}

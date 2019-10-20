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
package arcus.cornea.subsystem.safety.model;

import com.iris.client.capability.DeviceConnection;
import com.iris.client.model.DeviceModel;

public class SafetyDevice {
    private final String id;
    private final String name;
    private final String productName;
    private final boolean online;

    public SafetyDevice(DeviceModel device, String productName) {
        this.id = device.getId();
        this.name = device.getName();
        this.productName = productName;
        this.online = DeviceConnection.STATE_ONLINE.equals(device.get(DeviceConnection.ATTR_STATE));
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getProductName() {
        return productName;
    }

    public boolean isOnline() {
        return online;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SafetyDevice that = (SafetyDevice) o;

        if (online != that.online) return false;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        return !(productName != null ? !productName.equals(that.productName) : that.productName != null);

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (productName != null ? productName.hashCode() : 0);
        result = 31 * result + (online ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "SafetyDevice{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", productName='" + productName + '\'' +
                ", online=" + online +
                '}';
    }
}

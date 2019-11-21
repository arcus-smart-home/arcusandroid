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
package arcus.cornea.mock;

import androidx.annotation.Nullable;

import com.google.common.base.Supplier;
import com.iris.client.event.ClientFuture;
import com.iris.client.event.Futures;
import com.iris.client.model.DeviceModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MockSupplier implements Supplier<ClientFuture<List<DeviceModel>>> {
    private final List<DeviceModel> devices;

    public MockSupplier(@Nullable Set<String> capsToInclude, List<String> addresses) {
        devices = new ArrayList<>();
        if (addresses != null) {
            for (String address : addresses) {
                devices.add(new MockDeviceModel(address, capsToInclude));
            }
        }
    }

    @Override
    public ClientFuture<List<DeviceModel>> get() {
        return Futures.succeededFuture(devices);
    }
}

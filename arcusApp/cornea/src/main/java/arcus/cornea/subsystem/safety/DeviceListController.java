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
package arcus.cornea.subsystem.safety;

import arcus.cornea.provider.DeviceModelProvider;
import arcus.cornea.provider.ProductModelProvider;
import arcus.cornea.subsystem.SubsystemController;
import arcus.cornea.subsystem.safety.model.SafetyDevice;
import arcus.cornea.utils.AddressableListSource;
import arcus.cornea.utils.Listeners;
import arcus.cornea.utils.ModelSource;
import com.iris.client.capability.Device;
import com.iris.client.capability.DeviceConnection;
import com.iris.client.capability.Product;
import com.iris.client.capability.SafetySubsystem;
import com.iris.client.event.Listener;
import com.iris.client.event.ListenerRegistration;
import com.iris.client.model.DeviceModel;
import com.iris.client.model.ModelAddedEvent;
import com.iris.client.model.ModelChangedEvent;
import com.iris.client.model.ModelEvent;
import com.iris.client.model.ProductModel;
import com.iris.client.model.SubsystemModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public final class DeviceListController {

    public interface Callback {
        void showDevices(List<SafetyDevice> devices);
        void updateDevice(SafetyDevice device);
    }

    private static final Logger logger = LoggerFactory.getLogger(DeviceListController.class);

    private static final DeviceListController instance = new DeviceListController(
            SubsystemController.instance().getSubsystemModel(SafetySubsystem.NAMESPACE),
            DeviceModelProvider.instance().getModels(Collections.<String>emptySet()),
            ProductModelProvider.instance().getModels(Collections.<String>emptySet()));

    public static DeviceListController instance() {
        return instance;
    }

    private ModelSource<SubsystemModel> safety;
    private AddressableListSource<DeviceModel> devices;
    private AddressableListSource<ProductModel> products;
    private WeakReference<Callback> callback = new WeakReference<>(null);
    private Set<String> expectedProducts = new HashSet<>();

    DeviceListController(ModelSource<SubsystemModel> safety,
                         AddressableListSource<DeviceModel> devices,
                         AddressableListSource<ProductModel> products) {

        this.safety = safety;
        this.devices = devices;
        this.products = products;
        attachListeners();
    }

    private void attachListeners() {
        this.devices.addListener(Listeners.runOnUiThread(new Listener<List<DeviceModel>>() {
            @Override
            public void onEvent(List<DeviceModel> deviceModels) {
                onDevicesLoaded(deviceModels);
            }
        }));
        this.devices.addModelListener(Listeners.runOnUiThread(new Listener<ModelEvent>() {
            @Override
            public void onEvent(ModelEvent modelEvent) {
                if (modelEvent instanceof ModelChangedEvent) {
                    onDeviceChange((ModelChangedEvent) modelEvent);
                }
            }
        }));

        this.products.addListener(Listeners.runOnUiThread(new Listener<List<ProductModel>>() {
            @Override
            public void onEvent(List<ProductModel> productModels) {
                onProductsLoaded();
            }
        }));
        this.products.addModelListener(Listeners.runOnUiThread(new Listener<ModelEvent>() {
            @Override
            public void onEvent(ModelEvent modelEvent) {
                if (modelEvent instanceof ModelChangedEvent) {
                    onProductChange((ModelChangedEvent) modelEvent);
                }
            }
        }));
        this.safety.addModelListener(Listeners.runOnUiThread(new Listener<ModelEvent>() {
            @Override
            public void onEvent(ModelEvent modelEvent) {
                if (modelEvent instanceof ModelAddedEvent) {
                    onAdded();
                } else if (modelEvent instanceof ModelChangedEvent) {
                    onChanged(((ModelChangedEvent) modelEvent).getChangedAttributes().keySet());
                }
            }
        }));
    }

    public ListenerRegistration setCallback(Callback callback) {
        if(this.callback.get() != null) {
            logger.warn("Replacing existing callback");
        }
        this.callback = new WeakReference<>(callback);
        update();
        return Listeners.wrap(this.callback);
    }

    private SafetySubsystem getSubsystem() {
        safety.load();
        return (SafetySubsystem) safety.get();
    }

    private List<DeviceModel> getDevices() {
        devices.load();
        return devices.get();
    }

    private List<ProductModel> getProducts() {
        return products.get();
    }

    private void onAdded() {
        devices.setAddresses(new LinkedList<>(getSubsystem().getTotalDevices()));
    }

    private void onChanged(Set<String> changes) {
        if(changes.contains(SafetySubsystem.ATTR_TOTALDEVICES)) {
            devices.setAddresses(new LinkedList<>(getSubsystem().getTotalDevices()));
        }
    }

    private void onDevicesLoaded(List<DeviceModel> devices) {
        if(getSubsystem().getTotalDevices().size() == devices.size()) {
            Set<String> productAddresses = new HashSet<>();
            for (DeviceModel d : devices) {
                productAddresses.add("SERV:" + Product.NAMESPACE + ":" + d.getProductId());
            }
            if(productAddresses.equals(expectedProducts)) {
                update();
            } else {
                expectedProducts = productAddresses;
                products.setAddresses(new LinkedList<>(productAddresses));
            }
        }
    }

    private void onDeviceChange(ModelChangedEvent event) {
        Set<String> changes = event.getChangedAttributes().keySet();
        // TODO:  handle updates to product id
        if(changes.contains(Device.ATTR_NAME) || changes.contains(DeviceConnection.ATTR_STATE)) {
            updateDevice((DeviceModel) event.getModel());
        }
    }

    private void onProductChange(ModelChangedEvent event) {
        Set<String> changes = event.getChangedAttributes().keySet();
        if(changes.contains(Product.ATTR_SHORTNAME)) {
            updateProduct((ProductModel) event.getModel());
        }
    }

    private void onProductsLoaded() {
        update();
    }

    private void updateDevice(DeviceModel m) {
        Callback callback = this.callback.get();
        if(callback == null) {
            return;
        }

        // make sure we don't invoke any updates on device changes before the devices and products
        // have been loaded
        SafetySubsystem safety = getSubsystem();
        if(safety == null) {
            return;
        }

        List<DeviceModel> devices = getDevices();
        if(devices == null /*|| devices.size() != safety.getTotalDevices().size()*/) {
            return;
        }

        List<ProductModel> products = getProducts();
        if(products == null /*|| products.size() != expectedProducts.size()*/) {
            return;
        }
        callback.updateDevice(new SafetyDevice(m, getProductName(getProducts(), m.getProductId())));
    }

    private void updateProduct(ProductModel m) {
        Callback callback = this.callback.get();
        if(callback == null) {
            return;
        }
        List<DeviceModel> models = getDevices();
        if(models == null) {
            return;
        }

        List<SafetyDevice> devicesToUpdate = new LinkedList<>();
        for(DeviceModel d : models) {
            if(m.getId().equals(d.getProductId())) {
                devicesToUpdate.add(new SafetyDevice(d, m.getName()));
            }
        }
        for(SafetyDevice d : devicesToUpdate) {
            callback.updateDevice(d);
        }
    }

    private void update() {
        Callback callback = this.callback.get();
        if(callback == null) {
            logger.debug("DeviceListController not calling callback because none is set");
            return;
        }
        SafetySubsystem safety = getSubsystem();
        if(safety == null) {
            logger.debug("DeviceListController not calling callback because subsystem is null");
            return;
        }
        List<DeviceModel> devices = getDevices();
        if(devices == null /*|| devices.size() != safety.getTotalDevices().size()*/) {
            logger.debug("DeviceListController not calling callback because devices are null or not equal to {}", safety.getTotalDevices().size());
            return;
        }
        List<ProductModel> products = getProducts();
        if(products == null /*|| products.size() != expectedProducts.size()*/) {
            logger.debug("DeviceListController not calling callback because products are null or not equal to {}", expectedProducts.size());
            return;
        }

        callback.showDevices(getSafetyDevices(devices, products));
    }

    private List<SafetyDevice> getSafetyDevices(List<DeviceModel> devices, List<ProductModel> products) {
        List<SafetyDevice> safetyDevices = new ArrayList<>(devices.size());
        for(DeviceModel d : devices) {
            safetyDevices.add(new SafetyDevice(d, getProductName(products, d.getProductId())));
        }
        return safetyDevices;
    }

    private String getProductName(List<ProductModel> products, String id) {
        for(ProductModel p : products) {
            if(p.getId().equals(id)) {
                return p.getName();
            }
        }
        return null;
    }
}

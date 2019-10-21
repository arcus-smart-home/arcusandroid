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
package arcus.app.device.pairing.catalog.controller;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.widget.ListAdapter;

import com.google.common.base.Predicate;
import arcus.cornea.CorneaClientFactory;
import arcus.cornea.dto.ProductBrandAndCount;
import arcus.cornea.dto.ProductCategoryAndCount;
import arcus.cornea.provider.DeviceModelProvider;
import arcus.cornea.provider.HubModelProvider;
import arcus.cornea.provider.PlaceModelProvider;
import arcus.cornea.utils.Listeners;
import com.iris.client.ClientMessage;
import com.iris.client.capability.Capability;
import com.iris.client.capability.Hub;
import com.iris.client.capability.Place;
import com.iris.client.event.Listener;
import com.iris.client.event.ListenerRegistration;
import com.iris.client.model.DeviceModel;
import com.iris.client.model.HubModel;
import com.iris.client.model.ProductModel;
import com.iris.client.service.ProductCatalogService;
import arcus.app.ArcusApplication;
import arcus.app.common.controller.FragmentController;
import arcus.app.common.utils.CorneaUtils;
import arcus.app.common.utils.GlobalSetting;
import arcus.app.device.pairing.catalog.adapter.CatalogAdapterBuilder;
import arcus.app.device.pairing.catalog.model.CatalogDisplayMode;
import arcus.app.device.pairing.catalog.model.HubPairingState;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;


public class ProductCatalogFragmentController extends FragmentController<ProductCatalogFragmentController.Callbacks> implements PropertyChangeListener {

    private static final Logger logger = LoggerFactory.getLogger(ProductCatalogFragmentController.class);
    private static final ProductCatalogFragmentController instance = new ProductCatalogFragmentController();

    private Activity activity;
    private Stack<CatalogDisplayMode> modeStack = new Stack<>();
    private Map<String, String> devicesPairedAddresses = new HashMap<>();
    private ListenerRegistration hubStatusListener;
    private Predicate<ProductModel> filter;

    public interface Callbacks {
        void onHubPairingStateChanged (HubPairingState newPairingState);
        void onDevicesPaired(Map<String, String> devicesPaired);
        void onCatalogDisplayModeChanged(CatalogDisplayMode displayMode, ListAdapter catalogList);
        void onCorneaError (Throwable cause);
        void onCatalogCancelled();
    }

    private ProductCatalogFragmentController() {}

    public static ProductCatalogFragmentController instance () { return instance; }

    public void enterProductCatalog(Activity activity, Predicate<ProductModel> showProductsMatching) {
        if (activity == null) {
            throw new IllegalArgumentException("Activity and listener cannot be null.");
        }

        logger.debug("Starting product catalog in activity {} with listener {}.", activity.getClass().getSimpleName(), getListener().getClass().getSimpleName());

        this.activity = activity;
        this.modeStack = new Stack<>();
        this.devicesPairedAddresses = new HashMap<>();
        this.filter = showProductsMatching;

        showBrands();
        startPairing();
        monitorForPairedDevices();
    }

    public void showProductsForBrand (String brandName) {
        logger.debug("Showing products for brand {}.", brandName);

        ArcusApplication.getArcusApplication().getCorneaService().products().getByBrandName(brandName, filter).onSuccess(new Listener<List<ProductModel>>() {
            @Override
            public void onEvent(List<ProductModel> productModels) {
                setDisplayMode(CatalogDisplayMode.BY_PRODUCT_BY_BRAND, CatalogAdapterBuilder.in(activity).buildProductListByModel(productModels, false));
            }
        }).onFailure(new Listener<Throwable>() {
            @Override
            public void onEvent(Throwable throwable) {
                fireOnCorneaError(throwable);
            }
        });
    }

    public void showProductsForCategory (String categoryName) {
        logger.debug("Showing products for category {}.", categoryName);

        ArcusApplication.getArcusApplication().getCorneaService().products().getByCategoryName(categoryName, filter).onSuccess(new Listener<List<ProductModel>>() {
            @Override
            public void onEvent(List<ProductModel> productModels) {
                setDisplayMode(CatalogDisplayMode.BY_PRODUCT_BY_CATEGORY, CatalogAdapterBuilder.in(activity).buildProductListByModel(productModels, false));
            }
        }).onFailure(new Listener<Throwable>() {
            @Override
            public void onEvent(Throwable throwable) {
                fireOnCorneaError(throwable);
            }
        });
    }

    public void showBrands () {
        logger.debug("Showing product catalog by brand.");

        ArcusApplication.getArcusApplication().getCorneaService().products().getBrands(filter).onSuccess(new Listener<List<ProductBrandAndCount>>() {
            @Override
            public void onEvent(List<ProductBrandAndCount> productBrandAndCounts) {
                setDisplayMode(CatalogDisplayMode.BY_BRAND, CatalogAdapterBuilder.in(activity).buildBrandList(productBrandAndCounts));
            }
        }).onFailure(new Listener<Throwable>() {
            @Override
            public void onEvent(Throwable throwable) {
                fireOnCorneaError(throwable);
            }
        });
    }

    public void showCategories () {
        logger.debug("Showing product catalog by category.");

        ArcusApplication.getArcusApplication().getCorneaService().products().getCategories(filter).onSuccess(new Listener<List<ProductCategoryAndCount>>() {
            @Override
            public void onEvent(List<ProductCategoryAndCount> productCategoryAndCounts) {
                ListAdapter adapter = CatalogAdapterBuilder.in(activity).buildCategoryList(productCategoryAndCounts);
                setDisplayMode(CatalogDisplayMode.BY_CATEGORY, adapter);
            }
        }).onFailure(new Listener<Throwable>() {
            @Override
            public void onEvent(Throwable throwable) {
                fireOnCorneaError(throwable);
            }
        });
    }

    public void search (String searchString, final boolean hideHubRequiredDevices) {
        logger.debug("Searching catalog for query string '{}'.", searchString);

        try {

            //TODO: ProductCatalog - add place id
            CorneaClientFactory.getService(ProductCatalogService.class).findProducts(null, searchString).onSuccess(new Listener<ProductCatalogService.FindProductsResponse>() {
                @SuppressWarnings("unchecked")
                @Override
                public void onEvent(@NonNull ProductCatalogService.FindProductsResponse event) {
                    ListAdapter adapter = CatalogAdapterBuilder.in(activity).buildProductListByEntry((List<Map<String, Object>>) event.getAttributes().get("products"), true, hideHubRequiredDevices);
                    setDisplayMode(CatalogDisplayMode.BY_SEARCH, adapter);
                }
            });

        } catch (Exception e) {
            fireOnCorneaError(e);
        }
    }

    public void cancel() {
        CatalogDisplayMode currentMode = modeStack.isEmpty() ? null : modeStack.pop();
        CatalogDisplayMode lastMode = modeStack.isEmpty() ? null : modeStack.peek();

        logger.debug("Catalog search canceled; current mode is {}, last mode was {}.", currentMode, lastMode);

        // If stack is empty or user is at top of catalog, then back out
        if (currentMode == null || currentMode.isTopLevel()) {
            stopPairing();
            fireOnCatalogCanceled();
        }

        else {
            // Show previous mode (it's impossible to cancel into a search, so that case isn't handled)
            if (lastMode == CatalogDisplayMode.BY_CATEGORY || lastMode == CatalogDisplayMode.BY_PRODUCT_BY_CATEGORY) {
                showCategories();
            } else {
                showBrands();
            }
        }
    }

    private void setDisplayMode(CatalogDisplayMode mode, ListAdapter catalogList) {

        // Don't push the same mode onto the stack multiple times just because the user clicked the
        // same button over and over again.
        if (modeStack.isEmpty() || !modeStack.contains(mode)) {
            modeStack.push(mode);
        }

        fireOnCatalogListChanged(mode, catalogList);
    }

    public void startPairing () {
        logger.debug("Attempting to start pairing mode.");
        Listeners.clear(hubStatusListener);

        // Let the UI know we've request pairing mode
        fireOnHubPairingStateChanged(HubPairingState.PAIRING_REQUESTED);

        // Put the place into pairing mode ...
        PlaceModelProvider.getCurrentPlace().get().startAddingDevices(GlobalSetting.HUB_PAIRING_MODE_TIME).onSuccess(new Listener<Place.StartAddingDevicesResponse>() {
            @Override
            public void onEvent(Place.StartAddingDevicesResponse startAddingDevicesResponse) {

                HubModel activeHub = HubModelProvider.instance().getHubModel();

                // User has a hub...
                if (activeHub != null) {
                    HubPairingState pairingState = HubPairingState.fromHubState(String.valueOf(activeHub.get(Hub.ATTR_STATE)));

                    // If the current state isn't idle, then notify they user interface. Note that
                    // the hub may continue to report its state as idle for a few moments after the
                    // startAddingDevices() request; in that case, we don't want to fire a hub state
                    // of NOT_IN_PAIRING_MODE to the UI because it will interpret that as a hub timeout.
                    if (pairingState != HubPairingState.NOT_IN_PAIRING_MODE) {
                        fireOnHubPairingStateChanged(pairingState);
                    }

                    // ... then monitor changes to hub mode
                    Listeners.clear(hubStatusListener);
                    hubStatusListener = activeHub.addPropertyChangeListener(ProductCatalogFragmentController.this);
                }

                // ... user does not have a hub ...
                else {
                    logger.debug("Starting pairing on place; account has no hub.");
                    fireOnHubPairingStateChanged(HubPairingState.NO_HUB);
                }
            }
        }).onFailure(new Listener<Throwable>() {
            @Override
            public void onEvent(Throwable throwable) {
                fireOnCorneaError(throwable);
            }
        });
    }

    public void getProductById(String productId, Listener<ProductModel> listener) {
        ArcusApplication.getArcusApplication().getCorneaService().products().getByProductID(productId).onSuccess(listener).onFailure(new Listener<Throwable>() {
            @Override
            public void onEvent(Throwable throwable) {
                fireOnCorneaError(throwable);
            }
        });
    }

    private void monitorForPairedDevices () {
        ArcusApplication.getArcusApplication().getCorneaService().addMessageListener(new Listener<ClientMessage>() {
            @Override
            public void onEvent(@NonNull ClientMessage clientMessage) {
                if (clientMessage.getEvent() instanceof Capability.AddedEvent) {
                    String address = String.valueOf(clientMessage.getEvent().getAttribute(Capability.ATTR_ADDRESS));

                    if (CorneaUtils.isDeviceAddress(address) && !devicesPairedAddresses.keySet().contains(address)) {
                        DeviceModelProvider.instance().getModel(address).load().onSuccess(new Listener<DeviceModel>() {
                            @Override
                            public void onEvent(DeviceModel deviceModel) {
                                logger.debug("Product catalog detected paired device: {}", deviceModel);
                                devicesPairedAddresses.put(deviceModel.getAddress(), deviceModel.getName());
                                fireOnDevicesPaired();
                            }
                        }).onFailure(new Listener<Throwable>() {
                            @Override
                            public void onEvent(Throwable throwable) {
                                fireOnCorneaError(throwable);
                            }
                        });
                    }
                }
            }
        });
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        if (event.getPropertyName().equals(Hub.ATTR_STATE)) {
            logger.debug("Got new hub pairing state: {}", event.getNewValue());

            if (Hub.STATE_PAIRING.equals(event.getNewValue())) {
                fireOnHubPairingStateChanged(HubPairingState.PAIRING);
            } else if (Hub.STATE_NORMAL.equals(event.getNewValue())) {
                fireOnHubPairingStateChanged(HubPairingState.NOT_IN_PAIRING_MODE);
            }
        }
    }

    public void stopPairing () {
        logger.debug("Attempting to stop pairing mode.");

        PlaceModelProvider.getCurrentPlace().get().stopAddingDevices().onFailure(new Listener<Throwable>() {
            @Override
            public void onEvent(Throwable throwable) {
                fireOnCorneaError(throwable);
            }
        });
    }

    public boolean isAppVersionOlderThan(Activity activity, String minimumVersion) {
        String appVersionString;

        if (minimumVersion == null) {
            return false;
        }

        try {
            PackageInfo pInfo = activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0);
            appVersionString = pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            logger.error("An error occurred getting the version of this app.", e);
            return true;
        }

        String[] minVersionComponents = minimumVersion.split("\\.");
        String[] appVersionComponents = appVersionString.split("\\.");

        // Get rid of the commit hash
        if(appVersionComponents.length == 3 && appVersionComponents[2].indexOf("-") > 0) {
            appVersionComponents[2] = appVersionComponents[2].substring(0, appVersionComponents[2].indexOf("-"));
        } else if(appVersionComponents.length == 2 && appVersionComponents[1].indexOf("-") > 0) {
            appVersionComponents[1] = appVersionComponents[1].substring(0, appVersionComponents[1].indexOf("-"));
        } else if(appVersionComponents.length == 1 && appVersionComponents[0].indexOf("-") > 0) {
            appVersionComponents[0] = appVersionComponents[0].substring(0, appVersionComponents[0].indexOf("-"));
        }

        try {
            int appMajor = appVersionComponents.length >= 1 ? Integer.parseInt(appVersionComponents[0]) : 0;
            int appMinor = appVersionComponents.length >= 2 ? Integer.parseInt(appVersionComponents[1]) : 0;
            int appMaint = appVersionComponents.length >= 3 ? Integer.parseInt(appVersionComponents[2]) : 0;

            int minMajor = minVersionComponents.length >= 1 ? Integer.parseInt(minVersionComponents[0]) : 0;
            int minMinor = minVersionComponents.length >= 2 ? Integer.parseInt(minVersionComponents[1]) : 0;
            int minMaint = minVersionComponents.length >= 3 ? Integer.parseInt(minVersionComponents[2]) : 0;

            return  minMajor > appMajor ||
                    minMajor == appMajor && minMinor > appMinor ||
                    minMajor == appMajor && minMinor == appMinor && minMaint > appMaint;

        } catch (NumberFormatException e) {
            logger.error("Failed to parse version numbers. App version: " + appVersionComponents + " Min version: " + minVersionComponents, e);
            return false;
        }
    }

    /**
     * Gets a map of device address to device name for each device that has paired while this
     * controller has been monitoring status.
     *
     * @return
     */
    public Map<String, String> getDevicesPaired() {
        return devicesPairedAddresses;
    }

    private void fireOnCatalogCanceled() {
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Callbacks listener = getListener();
                    if (listener != null) {
                        listener.onCatalogCancelled();
                    }
                }
            });
        }
    }

    private void fireOnCatalogListChanged (final CatalogDisplayMode displayMode, final ListAdapter catalogList) {
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Callbacks listener = getListener();
                    if (listener != null) {
                        listener.onCatalogDisplayModeChanged(displayMode, catalogList);
                    }
                }
            });
        }
    }

    private void fireOnHubPairingStateChanged (final HubPairingState newHubState) {
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Callbacks listener = getListener();
                    if (listener != null) {
                        listener.onHubPairingStateChanged(newHubState);
                    }
                }
            });
        }
    }

    private void fireOnCorneaError (final Throwable throwable) {
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Callbacks listener = getListener();
                    if (listener != null) {
                        listener.onCorneaError(throwable);
                    }
                }
            });
        }
    }

    private void fireOnDevicesPaired() {
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Callbacks listener = getListener();
                    if (listener != null) {
                        listener.onDevicesPaired(devicesPairedAddresses);
                    }
                }
            });
        }
    }
}

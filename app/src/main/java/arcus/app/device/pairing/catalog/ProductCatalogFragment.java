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
package arcus.app.device.pairing.catalog;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuItemCompat;
import androidx.appcompat.widget.SearchView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.google.common.base.Predicate;
import arcus.cornea.controller.IProductController;
import arcus.cornea.provider.HubModelProvider;
import com.iris.client.event.Listener;
import com.iris.client.model.DeviceModel;
import com.iris.client.model.ProductModel;
import arcus.app.R;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.error.ErrorManager;
import arcus.app.common.error.listener.DismissListener;
import arcus.app.common.error.type.DeviceErrorType;
import arcus.app.common.image.ImageManager;
import arcus.app.common.image.Wallpaper;
import arcus.app.common.models.SessionModelManager;
import arcus.app.common.popups.AlertPopup;
import arcus.app.common.popups.UpdateAppPopup;
import arcus.app.common.sequence.SequencedFragment;
import arcus.app.common.utils.StringUtils;
import arcus.app.common.view.Version1TextView;
import arcus.app.dashboard.HomeFragment;
import arcus.app.device.model.DeviceType;
import arcus.app.device.pairing.catalog.adapter.CatalogBrandAdapter;
import arcus.app.device.pairing.catalog.adapter.CatalogCategoryAdapter;
import arcus.app.device.pairing.catalog.adapter.CatalogProductAdapter;
import arcus.app.device.pairing.catalog.controller.ProductCatalogFragmentController;
import arcus.app.device.pairing.catalog.controller.ProductCatalogSequenceController;
import arcus.app.device.pairing.catalog.model.CatalogDisplayMode;
import arcus.app.device.pairing.catalog.model.HubPairingState;
import arcus.app.device.pairing.catalog.model.ProductCatalogEntry;
import arcus.app.subsystems.alarm.AlertFloatingFragment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ProductCatalogFragment extends SequencedFragment<ProductCatalogSequenceController> implements ProductCatalogFragmentController.Callbacks {

    private static Logger logger = LoggerFactory.getLogger(ProductCatalogFragment.class);
    private static final String HIDE_HUB_DEVICES = "HIDE_HUB_DEVICES";

    private Version1TextView hubState;
    private Version1TextView pairedDevicesCount;
    private Version1TextView categoryIndicator;
    private Version1TextView brandIndicator;
    private ListView catalogItems;
    private RelativeLayout categoryBrandButtons;
    private ImageView chevron;

    private Map<String,String> deviceAddresses = new HashMap<>();
    private boolean timeoutMessageVisible = false;

    @NonNull
    public static ProductCatalogFragment newInstance (boolean hideHubRequiredDevices) {
        ProductCatalogFragment instance = new ProductCatalogFragment();
        Bundle arguments = new Bundle();
        arguments.putBoolean(HIDE_HUB_DEVICES, hideHubRequiredDevices);
        instance.setArguments(arguments);
        return instance;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        hubState = (Version1TextView) view.findViewById(R.id.tvHub);
        pairedDevicesCount = (Version1TextView) view.findViewById(R.id.tvPairedDevices);
        categoryIndicator = (Version1TextView) view.findViewById(R.id.txtCategory);
        brandIndicator = (Version1TextView) view.findViewById(R.id.txtBrand);
        catalogItems = (ListView) view.findViewById(R.id.catalog_items);
        categoryBrandButtons = (RelativeLayout) view.findViewById(R.id.rlButtons);
        chevron = (ImageView) view.findViewById(R.id.chevron);

        return view;
    }

    @Override
    public void onResume () {
        super.onResume();

        getActivity().setTitle(getTitle());
        getActivity().invalidateOptionsMenu();

        // User clicked "CATEGORIES" tab
        categoryIndicator.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProductCatalogFragmentController.instance().showCategories();
            }
        });

        // User clicked "BRAND" tab
        brandIndicator.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProductCatalogFragmentController.instance().showBrands();
            }
        });

        // User clicked an item in the list...
        catalogItems.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Adapter adapter = parent.getAdapter();

                // User clicked a brand
                if (adapter instanceof CatalogBrandAdapter) {
                    String selectedBrand = ((CatalogBrandAdapter) adapter).getItem(position).getText();
                    ProductCatalogFragmentController.instance().showProductsForBrand(selectedBrand);
                }

                // User clicked a category
                else if (adapter instanceof CatalogCategoryAdapter) {
                    String selectedCategory = ((CatalogCategoryAdapter) adapter).getItem(position).getText();
                    ProductCatalogFragmentController.instance().showProductsForCategory(selectedCategory);
                }

                // User clicked a product
                else if (adapter instanceof CatalogProductAdapter) {
                    Object selectedItem = ((CatalogProductAdapter) adapter).getItem(position).getData();
                    String productAddress;
                    String productScreen;
                    if (selectedItem instanceof ProductModel) {
                        ProductModel productModel = (ProductModel) selectedItem;
                        productAddress = productModel.getAddress();
                        productScreen = productModel.getScreen();

                        boolean bNewAppRequired = ProductCatalogFragmentController.instance().isAppVersionOlderThan(getActivity(), productModel.getMinAppVersion());
                        if (bNewAppRequired) {
                            showNewAppPopup();
                            return;
                        }

                        // Device requires a hub, but the user doesn't appear to have one
                        if (HubModelProvider.instance().getHubModel() == null && productModel.getHubRequired()) {
                            BackstackManager.getInstance().navigateToFloatingFragment(HubRequiredPopup.newInstance(), HubRequiredPopup.class.getSimpleName(), true);
                            return;
                        }


                        if(showRequiredPopup(productModel.getDevRequired())) {
                            return;
                        }


                    } else {
                        ProductCatalogEntry productCatalogEntry = (ProductCatalogEntry) selectedItem;
                        productAddress = productCatalogEntry.getBaseAddress();
                        productScreen = productCatalogEntry.getScreen();
                        String minAppVersion = productCatalogEntry.getMinAppVersion();

                        // Device requires a hub, but the user doesn't appear to have one
                        if (HubModelProvider.instance().getHubModel() == null && productCatalogEntry.isHubRequired()) {
                            BackstackManager.getInstance().navigateToFloatingFragment(HubRequiredPopup.newInstance(), HubRequiredPopup.class.getSimpleName(), true);
                            return;
                        }

                        if(!TextUtils.isEmpty(minAppVersion)) {
                            boolean bNewAppRequired = ProductCatalogFragmentController.instance().isAppVersionOlderThan(getActivity(), minAppVersion);
                            if (bNewAppRequired) {
                                showNewAppPopup();
                                return;
                            }
                        }

                        if(showRequiredPopup(productCatalogEntry.getDeviceRequiredId())) {
                            return;
                        }

                    }

                    // TODO: MJD-Not a fan of this logic (or the HubRequiredPopup logic above) embedded here; perhaps move to controller
                    if(DeviceType.fromHint(productScreen).equals(DeviceType.SOMFYV1BLINDS)) {
                        boolean showPopup = true;
                        List<DeviceModel> devicesPaired = SessionModelManager.instance().getDevicesWithHub();
                        if (devicesPaired != null) {
                            for(DeviceModel pairedDevice : devicesPaired) {
                                if (DeviceType.fromHint(pairedDevice.getDevtypehint()).equals(DeviceType.SOMFYV1BRIDGE)) {
                                    showPopup = false;
                                    break;
                                }
                            }
                        }
                        if(showPopup) {
                            AlertPopup popup = AlertPopup.newInstance(getString(R.string.somfy_pairing_controller_first_title),
                                    getString(R.string.somfy_pairing_controller_first_description),
                                    null, null, new AlertPopup.AlertButtonCallback() {
                                        @Override
                                        public boolean topAlertButtonClicked() {
                                            return false;
                                        }

                                        @Override
                                        public boolean bottomAlertButtonClicked() {
                                            return false;
                                        }

                                        @Override
                                        public boolean errorButtonClicked() {
                                            return false;
                                        }

                                        @Override
                                        public void close() {
                                            BackstackManager.getInstance().navigateBack();
                                            getActivity().invalidateOptionsMenu();
                                        }
                                    });
                            BackstackManager.getInstance().navigateToFloatingFragment(popup, popup.getClass().getName(), true);
                            return;
                        }
                    }

                    goNext(productAddress, productScreen);
                }
            }
        });

        // User clicked device count indicator
        pairedDevicesCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (deviceAddresses.size() >= 1) {
                    ArrayList<String> deviceAddressList = new ArrayList<>(deviceAddresses.keySet());
                    getController().goMultipairingSequence(getActivity(), deviceAddressList);
                }
                // Nothing to do for zero devices found
            }
        });

        ImageManager.with(getActivity()).setWallpaper(Wallpaper.ofCurrentPlace().lightend());

        Predicate<ProductModel> showProductsMatching = null;        // Show all devices
        if (isHubRequiredDevicesHidden()) {
            showProductsMatching = IProductController.HUB_NOT_REQUIRED_DEVICES_FILTER;
        }

        ProductCatalogFragmentController.instance().setListener(this);
        ProductCatalogFragmentController.instance().enterProductCatalog(getActivity(), showProductsMatching);
    }

    private boolean showRequiredPopup(String requiredDeviceId) {
        if(requiredDeviceId != null && !requiredDeviceId.equals("")) {
            boolean showPopup = true;
            List<DeviceModel> devicesPaired = SessionModelManager.instance().getDevicesWithHub();
            if (devicesPaired != null) {
                for (DeviceModel pairedDevice : devicesPaired) {
                    if (requiredDeviceId.equals(pairedDevice.getProductId())) {
                        showPopup = false;
                        break;
                    }
                }
            }
            if (showPopup) {
                ProductCatalogFragmentController.instance().getProductById(requiredDeviceId, new Listener<ProductModel>() {

                    @Override
                    public void onEvent(ProductModel event) {
                        AlertFloatingFragment popup = AlertFloatingFragment.newInstance(getString(R.string.generic_bridge_pairing_controller_first_title,
                                event.getManufacturer().toUpperCase(), event.getName().toUpperCase()),
                                getString(R.string.generic_bridge_pairing_controller_first_description, event.getManufacturer(), event.getName()),
                                null, null, null);
                        BackstackManager.getInstance().navigateToFloatingFragment(popup, popup.getClass().getName(), true);
                    }
                });
                return true;
            }
        }
        return false;
    }

    private String getSanitizedPackageName() {
        String appPackageName = getActivity().getPackageName(); // getPackageName() from Context or Activity object
        appPackageName = appPackageName.replace(".debug", "");
        appPackageName = appPackageName.replace(".qa", "");
        appPackageName = appPackageName.replace(".beta", "");
        return appPackageName;
    }

    private void showNewAppPopup() {
        final Activity activity = getActivity();
        UpdateAppPopup popup = UpdateAppPopup.newInstance(R.string.app_out_of_date, R.string.update_app,
                new UpdateAppPopup.UpdateAppPopupCallback() {
                    @Override
                    public boolean buttonClicked() {
                        //http://stackoverflow.com/questions/11753000/how-to-open-the-google-play-store-directly-from-my-android-application
                        String appPackageName = getSanitizedPackageName();

                        getController().endSequence(getActivity(), false);
                        BackstackManager.getInstance().popAllFragments();
                        BackstackManager.getInstance().navigateToFragment(HomeFragment.newInstance(), true);
                        try {
                            activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                        } catch (android.content.ActivityNotFoundException anfe) {
                            activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                        }
                        return true;
                    }

                    @Override
                    public void close() {
                        activity.invalidateOptionsMenu();
                    }
                });
        BackstackManager.getInstance().navigateToFloatingFragment(popup, popup.getClass().getName(), true);
    }

    @Override
    public void onPause () {
        super.onPause();
        ProductCatalogFragmentController.instance().removeListener();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        final MenuItem searchItem = menu.findItem(R.id.search);

        if (searchItem != null) {
            final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
            searchView.setIconifiedByDefault(false);
            searchView.setSubmitButtonEnabled(false);

            MenuItemCompat.setOnActionExpandListener(searchItem, new MenuItemCompat.OnActionExpandListener() {
                @Override
                public boolean onMenuItemActionExpand(MenuItem item) {
                    // User expanded search action; nothing to do
                    return true;
                }

                @Override
                public boolean onMenuItemActionCollapse(MenuItem item) {
                    ProductCatalogFragmentController.instance().cancel();
                    return true;
                }
            });

            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    ProductCatalogFragmentController.instance().search(query, isHubRequiredDevicesHidden());
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    if (!StringUtils.isEmpty(newText)) {
                        ProductCatalogFragmentController.instance().search(newText, isHubRequiredDevicesHidden());
                    }
                    return true;
                }
            });
        }
    }

    @Override
    public Integer getMenuId() {
        return R.menu.options_menu;
    }

    @Nullable
    @Override
    public String getTitle() {
        return getString(R.string.choose_device_fragment_title);
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_product_catalog;
    }

    @Override
    public void onHubPairingStateChanged(HubPairingState newPairingState) {
        hubState.setText(getString(newPairingState.getStringResId()));

        if (newPairingState == HubPairingState.NOT_IN_PAIRING_MODE) {
            showHubTimeoutDialog();
        }
    }

    @Override
    public void onDevicesPaired(Map<String, String> devicesPairedAddresses) {
        deviceAddresses = devicesPairedAddresses;
        pairedDevicesCount.setText(getResources().getQuantityString(R.plurals.devices_plural, deviceAddresses.size(), deviceAddresses.size()));
        chevron.setVisibility(View.VISIBLE);
    }

    @Override
    public void onCatalogDisplayModeChanged(CatalogDisplayMode displayMode, ListAdapter catalogList) {
        logger.debug("Catalog list changed; mode = {}, list item count = {}", displayMode, catalogList.getCount());

        switch (displayMode) {
            case BY_BRAND:
            case BY_PRODUCT_BY_BRAND:
                brandIndicator.setTextColor(Color.BLACK);
                categoryIndicator.setTextColor(Color.GRAY);
                categoryBrandButtons.setVisibility(View.VISIBLE);
                break;

            case BY_CATEGORY:
            case BY_PRODUCT_BY_CATEGORY:
                brandIndicator.setTextColor(Color.GRAY);
                categoryIndicator.setTextColor(Color.BLACK);
                categoryBrandButtons.setVisibility(View.VISIBLE);
                break;

            case BY_SEARCH:
                categoryBrandButtons.setVisibility(View.GONE);
                break;
        }

        catalogItems.setAdapter(catalogList);
    }

    private void showHubTimeoutDialog() {

        if (timeoutMessageVisible) {
            return;
        }

        timeoutMessageVisible = true;
        ErrorManager.in(getActivity()).withDialogDismissedListener(new DismissListener() {
            @Override
            public void dialogDismissedByReject() {
                timeoutMessageVisible = false;
                endSequence(false);
            }

            @Override
            public void dialogDismissedByAccept() {
                timeoutMessageVisible = false;
                ProductCatalogFragmentController.instance().startPairing();
            }

        }).show(DeviceErrorType.PAIRING_MODE_TIMEOUT);
    }


    @Override
    public void onCorneaError(Throwable cause) {
        ErrorManager.in(getActivity()).showGenericBecauseOf(cause);
    }

    @Override
    public void onCatalogCancelled() {
        getController().endSequence(getActivity(), false);
    }

    private boolean isHubRequiredDevicesHidden () {
        return getArguments().getBoolean(HIDE_HUB_DEVICES, false);
    }
}

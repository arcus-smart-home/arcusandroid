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
package arcus.app.device.pairing.specialty.nest;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.MenuItem;
import android.view.WindowManager;
import android.webkit.WebView;

import arcus.cornea.provider.DeviceModelProvider;
import arcus.cornea.provider.PlaceModelProvider;
import com.iris.client.IrisClientFactory;
import com.iris.client.model.DeviceModel;
import arcus.app.R;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.error.ErrorManager;
import arcus.app.dashboard.HomeFragment;
import arcus.app.device.details.DeviceDetailParentFragment;
import arcus.app.device.model.DeviceType;
import arcus.app.device.pairing.catalog.controller.ProductCatalogFragmentController;
import arcus.app.device.pairing.steps.AbstractPairingStepFragment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;



public class NestCloudToCloudPairingStep extends AbstractPairingStepFragment implements NestCloudToCloudListener {

    private final Logger logger = LoggerFactory.getLogger(NestCloudToCloudPairingStep.class);
    private final static String ABORT_ADDRESS = "abort-address";

    private WebView webview;
    private boolean showCancelButton = false;

    public static NestCloudToCloudPairingStep newInstance() {
        return newInstance(null);
    }

    public static NestCloudToCloudPairingStep newInstance(String abortToDeviceAddress) {
        NestCloudToCloudPairingStep instance = new NestCloudToCloudPairingStep();
        Bundle arguments = new Bundle();
        arguments.putString(ABORT_ADDRESS, abortToDeviceAddress);
        instance.setArguments(arguments);
        return instance;
    }

    @Nullable
    @Override
    public String getTitle() {
        return getString(R.string.link_account);
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_honeywell_webview;
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public void onResume() {
        super.onResume();
        super.setTitle();

        ProductCatalogFragmentController.instance().stopPairing();

        webview = (WebView) getView().findViewById(R.id.webview);
        webview.getSettings().setJavaScriptEnabled(true);
        webview.getSettings().setAllowUniversalAccessFromFileURLs(true);    // Prevent CORS issues on page resources
        webview.setWebViewClient(new NestCloudToCloudClient(this));
        webview.loadUrl(getNestPairingUrl());

        // Push content out of the way when entering text
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE|WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
    }

    @Override
    public Integer getMenuId() {
        return showCancelButton ? R.menu.menu_cancel : null;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        String abortAddress = getAbortAddress();

        if (abortAddress == null) {
            BackstackManager.getInstance().navigateBackToFragment(HomeFragment.newInstance());
        } else {
            BackstackManager.getInstance().navigateBackToFragment(DeviceDetailParentFragment.newInstance(getAbortAddress()));
        }

        return true;
    }

    @Override
    public void onShowCancelButton(boolean visible) {
        logger.debug("Showing cancel button: " + visible);
        showCancelButton = visible;
        Activity activity = getActivity();
        if (activity != null) {
            activity.invalidateOptionsMenu();
        }
    }

    @Override
    public void onAbortToDashboard() {
        logger.debug("Aborting Nest account linking; returning user to dashboard.");
        BackstackManager.getInstance().navigateBackToFragment(HomeFragment.newInstance());
    }

    @Override
    public void onAccountLinkSuccess() {
        logger.debug("Successfully linked Nest account.");
        getController().goMultipairingSequence(getActivity(), getNestDeviceAddressesInPlace());
    }

    @Override
    public void onWebError() {
        ErrorManager.in(getActivity()).showGenericBecauseOf(new IllegalStateException("An error occurred loading Nest account linking pages."));
        onAbortToDashboard();
    }

    @Override
    public boolean onBackPressed() {
        return true;        // Swallow back presses when on this page; user must use "Cancel" menu item to escape
    }

    private String getNestPairingUrl() {
        StringBuilder pairingUrl = new StringBuilder();
        String nestClientId = IrisClientFactory.getClient().getSessionInfo().getNestClientId();
        String nestBaseUrl = IrisClientFactory.getClient().getSessionInfo().getNestLoginBaseUrl();

        pairingUrl.append(nestBaseUrl);
        pairingUrl.append("?client_id=");
        pairingUrl.append(nestClientId);
        pairingUrl.append("&state=pair:");
        pairingUrl.append(PlaceModelProvider.getCurrentPlace().get().getId());

        return pairingUrl.toString();
    }

    private ArrayList<String> getNestDeviceAddressesInPlace() {
        ArrayList<String> deviceAddresses = new ArrayList<>();

        for (DeviceModel thisDevice : DeviceModelProvider.instance().getStore().values()) {
            if (DeviceType.fromHint(thisDevice.getDevtypehint()) == DeviceType.NEST_THERMOSTAT) {
                deviceAddresses.add(thisDevice.getAddress());
            }
        }

        return deviceAddresses;
    }

    private String getAbortAddress() {
        return getArguments().getString(ABORT_ADDRESS, null);
    }

}
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
package arcus.app.device.pairing.specialty.lutron;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.MenuItem;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.WebView;

import arcus.cornea.provider.PlaceModelProvider;
import com.iris.client.IrisClientFactory;
import arcus.app.R;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.error.ErrorManager;
import arcus.app.dashboard.HomeFragment;
import arcus.app.device.details.DeviceDetailParentFragment;
import arcus.app.device.pairing.catalog.controller.ProductCatalogFragmentController;
import arcus.app.device.pairing.multi.controller.MultipairingListFragmentController;
import arcus.app.device.pairing.steps.AbstractPairingStepFragment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;


public class LutronCloudToCloudPairingStep extends AbstractPairingStepFragment implements LutronCloudToCloudListener {

    private final Logger logger = LoggerFactory.getLogger(LutronCloudToCloudPairingStep.class);
    private final static String ABORT_ADDRESS = "abort-address";
    private final static String SUPPORT_URL = "support-url";

    private WebView webview;
    private boolean showCancelButton = false;

    public static LutronCloudToCloudPairingStep newInstance() {
        return newInstance(null, null);
    }

    public static LutronCloudToCloudPairingStep newInstance(String abortToDeviceAddress, String supportUrl) {
        LutronCloudToCloudPairingStep instance = new LutronCloudToCloudPairingStep();
        Bundle arguments = new Bundle();
        arguments.putString(ABORT_ADDRESS, abortToDeviceAddress);
        arguments.putString(SUPPORT_URL, supportUrl);
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

        CookieManager cmgr = CookieManager.getInstance();
        cmgr.setCookie(IrisClientFactory.getClient().getSessionInfo().getLutronLoginBaseUrl(), getArcusAuthCookieValue());

        webview.getSettings().setJavaScriptEnabled(true);
        webview.getSettings().setAllowUniversalAccessFromFileURLs(true);    // Prevent CORS issues on page resources
        webview.setWebViewClient(new LutronCloudToCloudClient(this));
        if (getArguments().getString(SUPPORT_URL) == null) {
            webview.loadUrl(getLutronPairingUrl());
        }
        else {
            webview.loadUrl(getLutronErrorUrl());
        }

        // Push content out of the way when entering text
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE|WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        MultipairingListFragmentController.getInstance().startMonitoringForNewDevices();
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
        logger.debug("Aborting Lutron account linking; returning user to dashboard.");
        BackstackManager.getInstance().navigateBackToFragment(HomeFragment.newInstance());
    }

    @Override
    public void onAccountLinkSuccess() {
        logger.debug("Successfully linked Lutron account.");
        getController().goMultipairingSequence(getActivity(), new ArrayList<String>());
    }

    @Override
    public void onWebError() {
        ErrorManager.in(getActivity()).showGenericBecauseOf(new IllegalStateException("An error occurred loading Lutron account linking pages."));
        onAbortToDashboard();
    }

    @Override
    public boolean onBackPressed() {
        return true;        // Swallow back presses when on this page; user must use "Cancel" menu item to escape
    }

    private String getLutronPairingUrl() {
        StringBuilder pairingUrl = new StringBuilder();

        String lutronBaseUrl = IrisClientFactory.getClient().getSessionInfo().getLutronLoginBaseUrl();
        pairingUrl.append(lutronBaseUrl);
        pairingUrl.append("?place=");
        pairingUrl.append(PlaceModelProvider.getCurrentPlace().get().getId());

        return pairingUrl.toString();
    }

    private String getLutronErrorUrl() {
        return getArguments().getString(SUPPORT_URL);
    }

    private String getAbortAddress() {
        return getArguments().getString(ABORT_ADDRESS, null);
    }

    private String getArcusAuthCookieValue() {
        StringBuilder sb = new StringBuilder();
        sb.append("secretz");
        sb.append("=");
        sb.append(IrisClientFactory.getClient().getSessionInfo().getSessionToken());
        return sb.toString();
    }
}

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
package arcus.app.device.pairing.specialty.honeywelltcc;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.WebView;

import arcus.cornea.CorneaClientFactory;
import com.iris.client.model.DeviceModel;
import com.iris.client.session.SessionInfo;
import arcus.app.R;
import arcus.app.device.pairing.catalog.controller.ProductCatalogFragmentController;
import arcus.app.device.pairing.steps.AbstractPairingStepFragment;

import java.util.ArrayList;
import java.util.UUID;

public class HoneywellWebViewSequence extends AbstractPairingStepFragment {
    public static final String PAIR = "pair:";
    public static final String URL_FORMAT = "%s?response_type=code&client_id=%s&redirect_uri=%s&scope=Basic%%20Power&state=%s%s";
    private ArrayList<String> deviceIDsFound = new ArrayList<>();

    protected WebView webview;
    protected HoneywellWebViewClientSequence client;
    private final HoneywellWebViewClientSequence.Callback callback = new HoneywellWebViewClientSequence.Callback() {
        @Override public void redirected() {
            hideProgressBar();
            getController().goTo(getActivity(), HoneywellSearching.newInstance(true, deviceIDsFound));
        }

        @Override public void errorEncountered(int errorCode, String description, String failingUrl) {
            getController().goTo(getActivity(), HoneywellError.newInstance());
            try {
                String message = String.format(
                      "Honeywell ERROR encountered [CODE:%s] [DESC:%s] [URL:%s]", errorCode, description, failingUrl
                );
            } catch (Exception ignored) {}
        }

        @Override public void pageLoading() {
            showProgressBar();
        }

        @Override public void pageLoaded() {
            hideProgressBar();
        }
    };

    public static HoneywellWebViewSequence newInstance() {
        return new HoneywellWebViewSequence();
    }

    @Nullable @Override public View onCreateView(
          LayoutInflater inflater,
          @Nullable ViewGroup container,
          @Nullable Bundle savedInstanceState
    ) {
        Activity activity = getActivity();
        if (activity != null) {
            activity.getWindow().setSoftInputMode(
                  WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
            );
        }
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Nullable @Override public String getTitle() {
        return "";
    }

    @Override public Integer getLayoutId() {
        return R.layout.fragment_honeywell_webview;
    }

    @Override @SuppressWarnings("deprecation") public void onResume() {
        super.onResume();
        ProductCatalogFragmentController.instance().stopPairing();

        Activity activity = getActivity();
        if (activity == null) {
            return;
        }

        activity.setTitle(getString(R.string.catalog_arcus_brand));
        activity.invalidateOptionsMenu();

        CookieManager.getInstance().removeAllCookie();
        View view = getView();
        if (view == null) {
            return;
        }

        // To override the default "go" "next" buttons we'd (on the keyboard)
        // have to subclass webview and override it's onCreateInputConnection() method to
        // strip out the IME options we don't want, and add in/retain only the ones we do want.
        SessionInfo clientSession = CorneaClientFactory.getClient().getSessionInfo();
        UUID placeID = CorneaClientFactory.getClient().getActivePlace();
        // In the event we get disconnected and this gets called while we are trying to relaunch the app.
        if (clientSession == null || placeID == null || isDetached()) {
            return;
        }

        webview = (WebView) view.findViewById(R.id.webview);
        webview.getSettings().setJavaScriptEnabled(true);
        client = new HoneywellWebViewClientSequence();
        client.setRedirectURL(clientSession.getHoneywellRedirectUri());
        client.setResumePairing(true);
        client.setCallback(callback);

        webview.setWebViewClient(client);
        String url = String.format(URL_FORMAT,
              clientSession.getHoneywellLoginBaseUrl(),
              clientSession.getHoneywellClientId(),
              clientSession.getHoneywellRedirectUri(),
              PAIR,
              placeID.toString()
        );
        logger.trace("Attempting to load [{}]", url);
        webview.loadUrl(url);
    }

    @Override public void onPause() {
        super.onPause();
        hideProgressBar();
    }

    @Override public void onHubPairingTimeout() {} // No-Op

    @Override
    public void onDeviceFound(DeviceModel deviceModel) {
        deviceIDsFound.add(deviceModel.getAddress());
    }

    @Override public void onDestroy() {
        super.onDestroy(); // Set back to what manifest shows.
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
    }

    @Override public boolean onBackPressed() {
        if (webview == null) {
            return super.onBackPressed();
        }

        if (webview.canGoBack()) {
            webview.goBack();
        }
        else {
            ProductCatalogFragmentController.instance().startPairing();
            goBack();
        }

        return true; // consumed back press.
    }
}

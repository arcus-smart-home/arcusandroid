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

import android.graphics.Bitmap;
import android.net.http.SslError;
import android.support.annotation.Nullable;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import arcus.app.device.pairing.catalog.controller.ProductCatalogFragmentController;

import java.util.Date;

public class HoneywellWebViewClientSequence extends WebViewClient {
    private String redirectURL = "NO REDIRECT URL SET";

    private boolean resumePairing = false;
    private boolean redirected = false;
    @Nullable  private Callback callback;

    @Override public boolean shouldOverrideUrlLoading(WebView view, String url) {
        if(url.startsWith(redirectURL)) {
            redirected = true;
            if (resumePairing) { // We were redirected, put place back into pairing mode.
                ProductCatalogFragmentController.instance().startPairing();
            }
        }
        return false;
    }

    @Override
    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
        if (callback != null) {
            callback.errorEncountered(errorCode, description, failingUrl);
        }
        super.onReceivedError(view, errorCode, description, failingUrl);
    }

    @Override
    public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
        Date today = new Date();
        today.setTime(System.currentTimeMillis());
        if(error.getPrimaryError() == SslError.SSL_DATE_INVALID ||
                error.getPrimaryError() == SslError.SSL_IDMISMATCH ||
                error.getPrimaryError() == SslError.SSL_INVALID ||
                error.getPrimaryError() == SslError.SSL_UNTRUSTED) {
            handler.cancel();
        }
        else {
            handler.proceed();
        }
    }

    public void setCallback(@Nullable Callback callback) {
        this.callback = callback;
    }

    public void setRedirectURL(String redirectURL) {
        this.redirectURL = redirectURL;
    }

    public void setResumePairing(boolean resumePairing) {
        this.resumePairing = resumePairing;
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        if (callback != null) {
            // FIXME: 2/5/16 Should we start a timer here and only give the honeywell page [x] seconds to load - otherwise call errorEncountered() ?
            callback.pageLoading();
        }
        super.onPageStarted(view, url, favicon);
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        if (callback != null) {
            if (redirected) {
                callback.redirected();
            }
            else {
                callback.pageLoaded();
            }
        }
        super.onPageFinished(view, url);
    }

    public interface Callback {
        void redirected();
        void pageLoading();
        void pageLoaded();
        void errorEncountered(int errorCode, String description, String failingUrl);
    }
}

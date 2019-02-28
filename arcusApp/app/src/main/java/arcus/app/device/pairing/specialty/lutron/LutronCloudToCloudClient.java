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

import android.net.http.SslError;
import android.os.Build;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.WeakReference;
import java.util.Date;



public class LutronCloudToCloudClient extends WebViewClient {

    private final Logger logger = LoggerFactory.getLogger(LutronCloudToCloudClient.class);
    private WeakReference<LutronCloudToCloudListener> listenerRef = new WeakReference<>(null);
    private String currentUrl;

    public LutronCloudToCloudClient(LutronCloudToCloudListener listener) {
        this.listenerRef = new WeakReference<>(listener);
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

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        logger.debug("Loading {}", url);
        currentUrl = url;

        LutronCloudToCloudListener listener = listenerRef.get();
        if (listener != null) {
            listener.onShowCancelButton(!url.toLowerCase().contains("example.com"));
        }

        return false;
    }

    @Override
    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
        super.onReceivedError(view, errorCode, description, failingUrl);
        logger.error("Received WebView error {} : {} when loading {}.", errorCode, description, failingUrl);

        LutronCloudToCloudListener listener = listenerRef.get();
        if (listener != null) {
            listener.onWebError();
        }
    }

    @Override
    public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
        super.onReceivedHttpError(view, request, errorResponse);

        LutronCloudToCloudListener listener = listenerRef.get();
        listener.onShowCancelButton(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            logger.error("Received WebView HTTP error {} : {} when loading {}.", errorResponse.getStatusCode(), errorResponse.getReasonPhrase(), request.getUrl());
            if(currentUrl != null && currentUrl.equals(request.getUrl().toString())) {
                if (listener != null) {
                    listener.onWebError();
                }
            }
        }
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        logger.debug("Finished loading " + url);

        LutronCloudToCloudListener listener = listenerRef.get();
        if (listener != null) {
            listener.onShowCancelButton(url.contains("device-login.lutron.com") || url.contains("www.example.com") || url.contains("support.example.com"));

            if (url.contains("pair/success")) {
                listener.onAccountLinkSuccess();
            }

            if (url.contains("/pair/cancel")) {
                listener.onAbortToDashboard();
            }
        }
    }

}

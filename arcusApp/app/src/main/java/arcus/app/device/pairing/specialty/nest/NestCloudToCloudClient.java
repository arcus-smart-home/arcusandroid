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

import android.net.http.SslError;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.WeakReference;
import java.util.Date;



public class NestCloudToCloudClient extends WebViewClient {

    private final Logger logger = LoggerFactory.getLogger(NestCloudToCloudClient.class);
    private WeakReference<NestCloudToCloudListener> listenerRef = new WeakReference<>(null);

    public NestCloudToCloudClient(NestCloudToCloudListener listener) {
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

        NestCloudToCloudListener listener = listenerRef.get();
        if (listener != null) {
            listener.onShowCancelButton(!url.toLowerCase().contains("example.com"));
        }

        return false;
    }

    @Override
    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
        super.onReceivedError(view, errorCode, description, failingUrl);
        logger.error("Received WebView error {} : {} when loading {}.", errorCode, description, failingUrl);

        NestCloudToCloudListener listener = listenerRef.get();
        if (listener != null) {
            listener.onWebError();
        }
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        logger.debug("Finished loading " + url);
        NestCloudToCloudListener listener = listenerRef.get();
        if (listener != null) {
            listener.onShowCancelButton(url.contains("home.nest.com") || url.contains("www.example.com"));

            if (url.contains("state=success")) {
                listener.onAccountLinkSuccess();
            }

            if (url.contains("state=error")) {
                listener.onAbortToDashboard();
            }
        }
    }

}

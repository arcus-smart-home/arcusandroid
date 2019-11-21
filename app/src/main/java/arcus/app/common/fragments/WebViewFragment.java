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
package arcus.app.common.fragments;

import android.net.http.SslError;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import arcus.app.R;

import java.util.Date;


public class WebViewFragment extends BaseFragment {

    public static final String TAG = WebViewFragment.class.getCanonicalName();
    public static final String KEY_ARGUMENT_URL = TAG + ".Url";
    public static final String KEY_ARGUMENT_DATA = TAG + ".Data";
    public static final String KEY_ARGUMENT_ENCODEING = TAG + ".Encoding";

    private String url;
    private String data;
    private String encoding;

    @Nullable
    private WebView webView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Bundle arguments = getArguments();
        if (arguments != null) {
            url = arguments.getString(KEY_ARGUMENT_URL, null);
            data = arguments.getString(KEY_ARGUMENT_DATA, null);
            encoding = arguments.getString(KEY_ARGUMENT_ENCODEING, null);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        webView = (WebView) super.onCreateView(inflater, container, savedInstanceState);

        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new BaseWebViewClient());

//        webView.getSettings().setBuiltInZoomControls(true);
//        webView.getSettings().setSupportZoom(true);

        webView.setOnKeyListener(new View.OnKeyListener()
        {
            @Override
            public boolean onKey(View v, int keyCode, @NonNull KeyEvent event)
            {
                if(event.getAction() == KeyEvent.ACTION_DOWN)
                {
                    WebView webView = (WebView) v;

                    switch(keyCode)
                    {
                        case KeyEvent.KEYCODE_BACK:
                            if(webView.canGoBack())
                            {
                                webView.goBack();
                                return true;
                            }
                            break;
                    }
                }

                return false;
            }
        });

        return webView;
    }

    @Override
    public void onResume() {
        super.onResume();
        showProgressBar();
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                hideProgressBar();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();

        // TODO Restore WebView state upon configuration change

        if (data != null) {
            if(encoding!=null){
                webView.loadData(data, "text/html", encoding);
            }
            webView.loadData(data, "text/html", null);
        } else if (url != null) {
                webView.loadUrl(url);
        }

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        webView.saveState(outState);
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        webView.restoreState(savedInstanceState);
    }

    @NonNull
    @Override
    public String getTitle() {
        return "";
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_webview;
    }

    public class BaseWebViewClient extends WebViewClient {
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
    }
}

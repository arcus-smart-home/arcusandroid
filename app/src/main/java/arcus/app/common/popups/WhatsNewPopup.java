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
package arcus.app.common.popups;

import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;

import arcus.app.R;
import arcus.app.common.fragments.BaseFragment;
import arcus.app.common.utils.ActivityUtils;

public class WhatsNewPopup extends BaseFragment {
    public static final String WHATS_NEW_URL = "URL";

    private Callback callback;
    private WebView webview;
    private String whatsNewUrl;

    public interface Callback {
        void closed();
    }

    @SuppressWarnings({"ConstantConditions"})
    @NonNull
    public static WhatsNewPopup newInstance() {
        WhatsNewPopup fragment = new WhatsNewPopup();
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view =  super.onCreateView(inflater, container, savedInstanceState);

        if(view !=null) {
            Bundle arguments = getArguments();
            if (arguments != null) {
                whatsNewUrl = arguments.getString(WHATS_NEW_URL, "");
            }

            ImageView close = (ImageView) view.findViewById(R.id.fragment_arcus_pop_up_close_btn);
            ImageView logo = (ImageView) view.findViewById(R.id.title_logo);

            logo.setVisibility(View.VISIBLE);
            close.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    getActivity().onBackPressed();
                    return false;
                }
            });
        }
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        webview = (WebView) getView().findViewById(R.id.whatsnew_webview);
        webview.getSettings().setJavaScriptEnabled(true);
        webview.getSettings().setAllowUniversalAccessFromFileURLs(true);    // Prevent CORS issues on page resources
        webview.setVerticalFadingEdgeEnabled(false);    // Get rid of overscroll glow
        webview.setOverScrollMode(View.OVER_SCROLL_NEVER);      // Get rid of overscroll effect
        webview.setWebViewClient(new WebViewClient(){
                 @Override
                 public boolean shouldOverrideUrlLoading(WebView view, String url) {
                     Uri uri = Uri.parse(url);
                     ActivityUtils.launchUrl(uri);
                     return true;
                 }
             }
        );
        webview.loadUrl(whatsNewUrl);
    }


    public void doClose() {
        if (callback == null) {
            return;
        }
        callback.closed();
    }

    @Override
    @NonNull
    public String getTitle() {
        return "";
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.floating_whatsnew;
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }
}

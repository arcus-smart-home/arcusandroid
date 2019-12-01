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
package arcus.app.dashboard.popups.responsibilities.dashboard;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.Nullable;
import arcus.cornea.CorneaClientFactory;
import arcus.app.BuildConfig;
import arcus.app.activities.FullscreenFragmentActivity;
import arcus.app.common.popups.WhatsNewPopup;
import arcus.app.common.utils.PreferenceUtils;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class WhatsNewPopupResponsibility extends DashboardPopupResponsibility implements WhatsNewPopup.Callback {

    private final static int ANIMATION_DELAY_MS = 2000;
    private final static ExecutorService executor = Executors.newSingleThreadExecutor();
    private String url;
    private boolean isVisible = false;

    @Override
    public boolean isQualified() {
        throw new IllegalStateException("Bug! Qualification is asynchronous. This method should never be invoked.");
    }

    @Override
    public void showPopup() {
        PreferenceUtils.setSeenWhatsNew(true);
        WhatsNewPopup popup = new WhatsNewPopup();

        this.isVisible = true;
        popup.setCallback(this);

        Bundle bundle = new Bundle(1);
        bundle.putString(WhatsNewPopup.WHATS_NEW_URL, url);
        FullscreenFragmentActivity.launchWithoutResult(popup.getClass(), ActivityInfo.SCREEN_ORIENTATION_PORTRAIT, bundle);
    }

    @Override
    protected boolean isVisible() {
        return this.isVisible;
    }

    @Override
    public void closed() {
        this.isVisible = false;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                DashboardPopupManager.getInstance().triggerPopups();
            }
        }, ANIMATION_DELAY_MS);
    }

    public Future<Boolean> isAsynchronouslyQualified() {
        return executor.submit(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return isDashboardVisible() && isNot404(getVersion()) && !PreferenceUtils.hasSeenWhatsNew();
            }
        });
    }

    private boolean isNot404(String version) {
        String baseUrl = getBaseUrl();
        if (baseUrl == null) return false;

        String urlString =  makeWhatsNewUrl(version, baseUrl);

        try {
            URL whatsNewUrl = new URL(urlString);
            HttpURLConnection urlConnection =  (HttpURLConnection) whatsNewUrl.openConnection();
            urlConnection.setRequestMethod("HEAD");

            int responseCode = urlConnection.getResponseCode();

            if(responseCode == 200) {
                url = urlString;
                return true;
            } else {
                String clipped = clipVersion(version);

                if(clipped != "") {
                    return isNot404(clipped);
                }
                else {
                    logger.debug("Did not receive a 404 or 200. Was [" + responseCode + "]");}
                    return false;
                }

        } catch (Exception ex){
            logger.info("An error occurred determining status of platform-provided What's New", ex);
        }

        // Can't determine status; assume platform has no what's new for us
        return false;
    }

    private String getVersion() {
        String versionString = BuildConfig.VERSION_NAME;

        if(null!=versionString && versionString.length()>0){
            int lastIndex = versionString.lastIndexOf("-");
            if(lastIndex!= -1){
                versionString = versionString.substring(0, lastIndex);
            }
        } else {
            versionString = "";
        }

        String version = versionString.replace(".", "/");
        return version;
    }

    private String clipVersion(String version) {
        String trimmedVersion="";
        if(null!=version && version.length()>0){
            int lastIndex = version.lastIndexOf("/");
            if(lastIndex!= -1){
                trimmedVersion = version.substring(0, lastIndex);
            }
            return trimmedVersion;
        } else {
            return "";
        }
    }

    @Nullable
    private String getBaseUrl() {
        try {
            return CorneaClientFactory.getClient().getSessionInfo().getStaticResourceBaseUrl();
        } catch (Exception ex) {
            return null;
        }
    }

    private String makeWhatsNewUrl(String version, String whatsNewBaseUrl) {
        return whatsNewBaseUrl +
                "/o/release/android/" + // Android What's New
                version +
                "/notes.html";
    }
}

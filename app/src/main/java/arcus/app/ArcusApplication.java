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
package arcus.app;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;
import androidx.multidex.MultiDex;

import arcus.cornea.CorneaService;

import arcus.app.common.models.RegistrationContext;
import arcus.app.common.utils.PreferenceUtils;
import arcus.cornea.network.NetworkConnectionMonitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;

public class ArcusApplication extends Application {
    private static final AtomicBoolean shouldReload = new AtomicBoolean(false);
    private static final Logger logger = LoggerFactory.getLogger(ArcusApplication.class);
    private static ArcusApplication arcusApplication;
    @Nullable private static RegistrationContext registrationContext;

    private Handler handler;
    private static int DELAY_BEFORE_CLOSE_MS = 1000 * 30; // 30 seconds, not 1000 * 60 * 10; // 10 Minutes.
    private static final int NORMAL_DELAY_BEFORE_CLOSE_MS = 1000 * 30; // 30 seconds
    private static final int LONGEST_DELAY_BEFORE_CLOSE_MS = 1000 * 60 * 10; // 10 Minutes.

    @NonNull
    private Runnable closeCorneaRunnable = () -> {
        shouldReload.set(CorneaService.INSTANCE.isConnected());
        getCorneaService().silentClose();
    };

    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        arcusApplication = this;
        String agent = String.format("Android/%s (%s %s)", Build.VERSION.RELEASE, Build.MANUFACTURER, Build.MODEL);
        CorneaService.initialize(agent, BuildConfig.VERSION_NAME);

        setupLifecycleListener();
        registrationContext = RegistrationContext.getInstance();
        handler = new Handler();

        PreferenceUtils.hasUserUpgraded();

        NotificationChannelsSetup setup = new NotificationChannelsSetup();
        setup.setupChannels(this);
    }

    public static ArcusApplication getArcusApplication(){
        return arcusApplication;
    }

    public static Context getContext() {
        return getArcusApplication().getApplicationContext();
    }

    public static SharedPreferences getSharedPreferences(){
        return getArcusApplication().getSharedPreferences(getArcusApplication().getPackageName(), Context.MODE_PRIVATE);
    }

    @Nullable
    public static RegistrationContext getRegistrationContext(){
        return registrationContext;
    }

    public CorneaService getCorneaService() {
        return CorneaService.INSTANCE;
    }

    public static boolean shouldReload() {
        return shouldReload.getAndSet(false);
    }

    public static void selectingGalleryImage() {
        DELAY_BEFORE_CLOSE_MS = LONGEST_DELAY_BEFORE_CLOSE_MS;
    }

    private static void useNormalTimeoutDelay() {
        DELAY_BEFORE_CLOSE_MS = NORMAL_DELAY_BEFORE_CLOSE_MS;
    }

    private void setupLifecycleListener() {
        ProcessLifecycleOwner
                .get()
                .getLifecycle()
                .addObserver(new LifecycleObserver() {
                    @OnLifecycleEvent(Lifecycle.Event.ON_START)
                    public void onForeground() {
                        NetworkConnectionMonitor.getInstance().startListening(ArcusApplication.this);

                        // Go back to 30-second timeout
                        useNormalTimeoutDelay();

                        logger.debug("Application is resumed, cancelling connection close.");
                        handler.removeCallbacks(closeCorneaRunnable);
                    }

                    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
                    public void onBackground() {
                        logger.debug("Application is backgrounded, posting delayed connection close.");
                        handler.postDelayed(closeCorneaRunnable, DELAY_BEFORE_CLOSE_MS);
                        NetworkConnectionMonitor.getInstance().stopListening(ArcusApplication.this);
                    }
                });
    }
}

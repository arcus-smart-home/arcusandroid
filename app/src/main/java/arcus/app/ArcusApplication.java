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
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;
import androidx.multidex.MultiDex;

import arcus.cornea.CorneaClientFactory;
import arcus.cornea.CorneaService;

import arcus.app.common.models.RegistrationContext;
import arcus.app.common.utils.PreferenceUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;

public class ArcusApplication extends Application {

    private static final AtomicBoolean shouldReload = new AtomicBoolean(false);
    private static final Logger logger = LoggerFactory.getLogger(ArcusApplication.class);
    private static ArcusApplication arcusApplication;
    @Nullable private static RegistrationContext registrationContext;

    private CorneaService corneaService;
    private boolean corneaServiceBound = false;
    private boolean corneaBindInProgress = false;

    private Handler handler;
    private static int DELAY_BEFORE_CLOSE_MS = 1000 * 30; // 30 seconds, not 1000 * 60 * 10; // 10 Minutes.
    private static final int NORMAL_DELAY_BEFORE_CLOSE_MS = 1000 * 30; // 30 seconds
    private static final int LONGEST_DELAY_BEFORE_CLOSE_MS = 1000 * 60 * 10; // 10 Minutes.

    @NonNull
    private Runnable closeCorneaRunnable = new Runnable() {
        @Override
        public void run() {
            if (corneaServiceBound) {
                unbindService(mConnection);
                corneaServiceBound = false;
            }

            if (corneaService != null && corneaService.isConnected()) {
                shouldReload.set(true);
            }
        }
    };
    
    @NonNull
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            if (service instanceof CorneaService.CorneaBinder) {
                CorneaService.CorneaBinder binder = (CorneaService.CorneaBinder) service;
                corneaService = binder.getService();
                corneaServiceBound = true;
                corneaBindInProgress = false;
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            corneaServiceBound = false;
        }
    };

    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        setupLifecycleListener();
        arcusApplication = this;
        registrationContext = RegistrationContext.getInstance();

        String agent = String.format("Android/%s (%s %s)", Build.VERSION.RELEASE, Build.MANUFACTURER, Build.MODEL);
        CorneaClientFactory.init(agent, BuildConfig.VERSION_NAME);
        handler = new Handler();
        bindCornea();

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
        return arcusApplication.getSharedPreferences(arcusApplication.getPackageName(), Context.MODE_PRIVATE);
    }

    @Nullable
    public static RegistrationContext getRegistrationContext(){
        return registrationContext;
    }

    public CorneaService getCorneaService() {
        return corneaService;
    }

    private void bindCornea() {
        if (!corneaServiceBound && !corneaBindInProgress) {
            corneaBindInProgress = true;
            Intent intent = new Intent(this, CorneaService.class);
            bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
            logger.debug("Cornea service not bound. Calling bind bindService()");
        }
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
                    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
                    public void onCreated() {
                        bindCornea();
                    }

                    @OnLifecycleEvent(Lifecycle.Event.ON_START)
                    public void onForeground() {
                        // Go back to 30-second timeout
                        useNormalTimeoutDelay();

                        bindCornea();

                        logger.debug("Application is resumed, cancelling connection close.");
                        handler.removeCallbacks(closeCorneaRunnable);
                    }

                    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
                    public void onBackground() {
                        logger.debug("Application is backgrounded, posting delayed connection close.");
                        handler.postDelayed(closeCorneaRunnable, DELAY_BEFORE_CLOSE_MS);
                    }
                });
    }
}

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
package arcus.app.common.utils;

import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import androidx.annotation.Nullable;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;




public class MobileDeviceInfo {

    @Nullable private String osType = null;
    @Nullable private String osVersion = null;
    @Nullable private String formFactor = null;  //  mdphi ....
    @Nullable private String resolution = null;  //  W x H
    @Nullable private String deviceModel = null;
    @Nullable private String deviceVendor = null;
    @Nullable private String deviceIdentifier = null;
    @Nullable private String phoneNumber = null;
    @Nullable private Context mContext = null;


    public MobileDeviceInfo(Context context) {

        mContext = context;

        getDeviceBasicInfo();
        getScreenResolution();
        getScreenDensity();
    }


    private void getDeviceBasicInfo() {

        deviceModel = Build.MODEL;
        deviceVendor = Build.MANUFACTURER;
        osVersion = Build.VERSION.CODENAME + " : " + Build.VERSION.RELEASE;
        osType = "Android";

        //  The official Android blog recommends this way of doing it but this value can
        //  change if the device is reset or rooted.

        deviceIdentifier = Settings.Secure.getString(mContext.getContentResolver(),
                Settings.Secure.ANDROID_ID);

    }


    private void getScreenResolution()
    {
        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);

        Display display;
        DisplayMetrics metrics;

        display = wm.getDefaultDisplay();
        metrics = new DisplayMetrics();

        display.getMetrics(metrics);
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;

        resolution = width + " X " + height;

        return;
    }


    //  form factor

    private void getScreenDensity() {

        int density;

        density = mContext.getResources().getDisplayMetrics().densityDpi;

        switch(density)
        {
            case DisplayMetrics.DENSITY_LOW:
                //  not used; but can't hurt adding it in case something changes
                formFactor = "LDPI";
                break;
            case DisplayMetrics.DENSITY_MEDIUM:
                formFactor = "MDPI";
                break;
            case DisplayMetrics.DENSITY_HIGH:
                formFactor = "HDPI";
                break;
            case DisplayMetrics.DENSITY_XHIGH:
                formFactor = "XHDPI";
                break;
            case DisplayMetrics.DENSITY_XXHIGH:
                formFactor = "XXHDPI";
                break;
            case DisplayMetrics.DENSITY_XXXHIGH:
                formFactor = "XXXHDPI";
                break;
            default:
                formFactor = "UNKNOWN";
                break;
        }
    }

    @Nullable
    public String getOsType() {
        return(osType);
    }

    @Nullable
    public String getOsVersion() {
        return(osVersion);
    }

    @Nullable
    public String getFormFactor() {
        return(formFactor);
    }

    @Nullable
    public String getPhoneNumber() {
        return(phoneNumber);
    }

    @Nullable
    public String getDeviceIdentifier() {
        return(deviceIdentifier);
    }

    @Nullable
    public String getDeviceModel() {
        return(deviceModel);
    }

    @Nullable
    public String getDeviceVendor() {
        return(deviceVendor);
    }

    @Nullable
    public String getResolution() {
        return(resolution);
    }
}

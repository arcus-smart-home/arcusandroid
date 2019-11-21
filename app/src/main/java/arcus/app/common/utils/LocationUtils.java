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

import android.app.Activity;
import android.content.Context;
import android.content.IntentSender;
import android.location.Location;
import android.location.LocationManager;
import androidx.annotation.NonNull;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import arcus.app.ArcusApplication;
import arcus.app.common.image.IntentRequestCode;


public class LocationUtils {

    public static Location getLastKnownCoarseLocation(@NonNull Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        // Walk through each enabled location provider and return the first found, last-known location
        for (String thisLocProvider : locationManager.getProviders(true)) {
            Location lastKnown = locationManager.getLastKnownLocation(thisLocProvider);

            if (lastKnown != null) {
                return lastKnown;
            }
        }

        // Always possible there's no means to determine location
        return null;
    }

    public static void requestEnableLocation(@NonNull Activity activity) {

        /* First, quit if location is enabled */
        LocationManager locationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
        boolean locationEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (locationEnabled) {
            return;
        }

        /* Otherwise, do the stuff */
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(LocationRequest.create())
                .setAlwaysShow(true);

        SettingsClient client = LocationServices.getSettingsClient(getContext());

        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
            @Override
            public void onComplete(@NonNull Task<LocationSettingsResponse> task) {
                try {
                    task.getResult(ResolvableApiException.class);
                } catch (ResolvableApiException ex) {
                    try {
                        ex.startResolutionForResult(activity,
                                // An arbitrary constant to disambiguate activity results.
                                IntentRequestCode.TURN_ON_LOCATION.requestCode);
                    } catch (IntentSender.SendIntentException sendEx) {
                        // Ignore the error.
                    }
                }
                // success
            }
        });
    }

    private static Context getContext(){
        return ArcusApplication.getArcusApplication().getApplicationContext();
    }

}

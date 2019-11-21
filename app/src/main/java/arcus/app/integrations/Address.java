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
package arcus.app.integrations;

import android.Manifest;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;
import arcus.app.R;
import arcus.app.activities.BaseActivity;
import arcus.app.common.utils.GlobalSetting;

import java.util.ArrayList;
import java.util.TimeZone;


public class Address implements Parcelable, BaseActivity.PermissionCallback {

    private static final int MS_PER_HOUR = 1000 * 60 * 60;

    @SerializedName("text")
    private String text;
    @SerializedName("street_line")
    private String street;
    private String street2;
    @SerializedName("city")
    private String city;
    @SerializedName("state")
    private String state;
    private String zipCode;
    private String timeZoneName;
    private String timeZoneId;
    private boolean dst;
    private double utcOffset;
    private double lat;
    private double lng;
    private BaseActivity activity;

    public Address() {

    }
    public Address(@NonNull BaseActivity activity) {
        this.activity = activity;

        //TODO - Permissions: Have the caller do this for us?
        activity.setPermissionCallback(this);
        ArrayList<String> permissions = new ArrayList<String>();
        permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        activity.checkPermission(permissions, GlobalSetting.PERMISSION_ACCESS_COARSE_LOCATION, R.string.permission_rationale_location);

        this.timeZoneName = TimeZone.getDefault().getDisplayName();
        this.dst = TimeZone.getDefault().useDaylightTime();
        this.utcOffset = TimeZone.getDefault().getOffset(System.currentTimeMillis()) / MS_PER_HOUR;

        Location lastKnownLocation = getLastKnownCoarseLocation();
        if (lastKnownLocation != null) {
            this.lat = lastKnownLocation.getLatitude();
            this.lng = lastKnownLocation.getLongitude();
        }
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getStreet2() {
        return street2;
    }

    public void setStreet2(String street2) {
        this.street2 = street2;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public String getTimeZoneName() {
        return timeZoneName;
    }

    public void setTimeZoneName(String timeZone) {
        this.timeZoneName = timeZone;
    }

    public String getTimeZoneId() {
        return timeZoneId;
    }

    public void setTimeZoneId(String timeZoneId) {
        this.timeZoneId = timeZoneId;
    }

    public double getUtcOffset() {
        return utcOffset;
    }

    public void setUtcOffset(double utcOffset) {
        this.utcOffset = utcOffset;
    }

    public boolean isDst() {
        return dst;
    }

    public void setDst(boolean dst) {
        this.dst = dst;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.text);
        dest.writeString(this.street);
        dest.writeString(this.street2);
        dest.writeString(this.city);
        dest.writeString(this.state);
        dest.writeString(this.zipCode);
        dest.writeString(this.timeZoneName);
        dest.writeByte(dst ? (byte) 1 : (byte) 0);
    }

    protected Address(Parcel in) {
        this.text = in.readString();
        this.street = in.readString();
        this.street2 = in.readString();
        this.city = in.readString();
        this.state = in.readString();
        this.zipCode = in.readString();
        this.timeZoneName = in.readString();
        this.dst = in.readByte() != 0;
    }

    public static final Creator<Address> CREATOR = new Creator<Address>() {
        public Address createFromParcel(Parcel source) {
            return new Address(source);
        }

        public Address[] newArray(int size) {
            return new Address[size];
        }
    };

    @Override
    public void permissionsUpdate(int permissionType, ArrayList<String> permissionsDenied, ArrayList<String> permissionsDeniedNeverAskAgain) {
        this.timeZoneName = TimeZone.getDefault().getDisplayName();
        this.dst = TimeZone.getDefault().useDaylightTime();
        this.utcOffset = TimeZone.getDefault().getOffset(System.currentTimeMillis()) / MS_PER_HOUR;

        Location lastKnownLocation = getLastKnownCoarseLocation();
        if (lastKnownLocation != null) {
            this.lat = lastKnownLocation.getLatitude();
            this.lng = lastKnownLocation.getLongitude();
        }
    }

    public Location getLastKnownCoarseLocation () {
        LocationManager locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
        try {
            // Walk through each enabled location provider and return the first found, last-known location
            for (String thisLocProvider : locationManager.getProviders(true)) {
                Location lastKnown = locationManager.getLastKnownLocation(thisLocProvider);

                if (lastKnown != null) {
                    return lastKnown;
                }
            }
        } catch(SecurityException exception) {

        }
        // Always possible there's no means to determine location
        return null;
    }
}

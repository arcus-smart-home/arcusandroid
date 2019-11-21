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
package arcus.cornea.model.smartystreets;

import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

public class Metadata {
    @SerializedName("record_type") private String recordType;
    @SerializedName("zip_type") private String zipType;
    @SerializedName("county_fips") private String countyFips;
    @SerializedName("county_name") private String countyName;
    @SerializedName("carrier_route") private String carrierRoute;
    @SerializedName("congressional_district") private String congressionalDistrict;
    @SerializedName("rdi") private String rdi;
    @SerializedName("elot_sequence") private String elotSequence;
    @SerializedName("elot_sort") private String elotSort;
    @SerializedName("latitude") private Number latitude;
    @SerializedName("longitude") private Number longitude;
    @SerializedName("precision") private String precision;
    @SerializedName("time_zone") private String timeZone;
    @SerializedName("utc_offset") private Number utcOffset;
    @SerializedName("dst") private String dst;

    public Metadata() {
    }

    public @Nullable String getRecordType() {
        return recordType;
    }

    public void setRecordType(String recordType) {
        this.recordType = recordType;
    }

    public @Nullable String getZipType() {
        return zipType;
    }

    public void setZipType(String zipType) {
        this.zipType = zipType;
    }

    public @Nullable String getCountyFips() {
        return countyFips;
    }

    public void setCountyFips(String countyFips) {
        this.countyFips = countyFips;
    }

    public @Nullable String getCountyName() {
        return countyName;
    }

    public void setCountyName(String countyName) {
        this.countyName = countyName;
    }

    public @Nullable String getCarrierRoute() {
        return carrierRoute;
    }

    public void setCarrierRoute(String carrierRoute) {
        this.carrierRoute = carrierRoute;
    }

    public @Nullable String getCongressionalDistrict() {
        return congressionalDistrict;
    }

    public void setCongressionalDistrict(String congressionalDistrict) {
        this.congressionalDistrict = congressionalDistrict;
    }

    public @Nullable String getRdi() {
        return rdi;
    }

    public void setRdi(String rdi) {
        this.rdi = rdi;
    }

    public @Nullable String getElotSequence() {
        return elotSequence;
    }

    public void setElotSequence(String elotSequence) {
        this.elotSequence = elotSequence;
    }

    public @Nullable String getElotSort() {
        return elotSort;
    }

    public void setElotSort(String elotSort) {
        this.elotSort = elotSort;
    }

    public @Nullable Number getLatitude() {
        return latitude;
    }

    public void setLatitude(Number latitude) {
        this.latitude = latitude;
    }

    public @Nullable Number getLongitude() {
        return longitude;
    }

    public void setLongitude(Number longitude) {
        this.longitude = longitude;
    }

    public @Nullable String getPrecision() {
        return precision;
    }

    public void setPrecision(String precision) {
        this.precision = precision;
    }

    public @Nullable String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    public @Nullable Number getUtcOffset() {
        return utcOffset;
    }

    public void setUtcOffset(Number utcOffset) {
        this.utcOffset = utcOffset;
    }

    public @Nullable String getDst() {
        return dst;
    }

    public void setDst(String dst) {
        this.dst = dst;
    }

    @Override public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Metadata metadata = (Metadata) o;

        if (recordType != null ? !recordType.equals(metadata.recordType) : metadata.recordType != null) {
            return false;
        }
        if (zipType != null ? !zipType.equals(metadata.zipType) : metadata.zipType != null) {
            return false;
        }
        if (countyFips != null ? !countyFips.equals(metadata.countyFips) : metadata.countyFips != null) {
            return false;
        }
        if (countyName != null ? !countyName.equals(metadata.countyName) : metadata.countyName != null) {
            return false;
        }
        if (carrierRoute != null ? !carrierRoute.equals(metadata.carrierRoute) : metadata.carrierRoute != null) {
            return false;
        }
        if (congressionalDistrict != null ? !congressionalDistrict.equals(metadata.congressionalDistrict) : metadata.congressionalDistrict != null) {
            return false;
        }
        if (rdi != null ? !rdi.equals(metadata.rdi) : metadata.rdi != null) {
            return false;
        }
        if (elotSequence != null ? !elotSequence.equals(metadata.elotSequence) : metadata.elotSequence != null) {
            return false;
        }
        if (elotSort != null ? !elotSort.equals(metadata.elotSort) : metadata.elotSort != null) {
            return false;
        }
        if (latitude != null ? !latitude.equals(metadata.latitude) : metadata.latitude != null) {
            return false;
        }
        if (longitude != null ? !longitude.equals(metadata.longitude) : metadata.longitude != null) {
            return false;
        }
        if (precision != null ? !precision.equals(metadata.precision) : metadata.precision != null) {
            return false;
        }
        if (timeZone != null ? !timeZone.equals(metadata.timeZone) : metadata.timeZone != null) {
            return false;
        }
        if (utcOffset != null ? !utcOffset.equals(metadata.utcOffset) : metadata.utcOffset != null) {
            return false;
        }
        return dst != null ? dst.equals(metadata.dst) : metadata.dst == null;

    }

    @Override public int hashCode() {
        int result = recordType != null ? recordType.hashCode() : 0;
        result = 31 * result + (zipType != null ? zipType.hashCode() : 0);
        result = 31 * result + (countyFips != null ? countyFips.hashCode() : 0);
        result = 31 * result + (countyName != null ? countyName.hashCode() : 0);
        result = 31 * result + (carrierRoute != null ? carrierRoute.hashCode() : 0);
        result = 31 * result + (congressionalDistrict != null ? congressionalDistrict.hashCode() : 0);
        result = 31 * result + (rdi != null ? rdi.hashCode() : 0);
        result = 31 * result + (elotSequence != null ? elotSequence.hashCode() : 0);
        result = 31 * result + (elotSort != null ? elotSort.hashCode() : 0);
        result = 31 * result + (latitude != null ? latitude.hashCode() : 0);
        result = 31 * result + (longitude != null ? longitude.hashCode() : 0);
        result = 31 * result + (precision != null ? precision.hashCode() : 0);
        result = 31 * result + (timeZone != null ? timeZone.hashCode() : 0);
        result = 31 * result + (utcOffset != null ? utcOffset.hashCode() : 0);
        result = 31 * result + (dst != null ? dst.hashCode() : 0);
        return result;
    }

    @Override public String toString() {
        return "Metadata{" +
              "recordType='" + recordType + '\'' +
              ", zipType='" + zipType + '\'' +
              ", countyFips='" + countyFips + '\'' +
              ", countyName='" + countyName + '\'' +
              ", carrierRoute='" + carrierRoute + '\'' +
              ", congressionalDistrict='" + congressionalDistrict + '\'' +
              ", rdi='" + rdi + '\'' +
              ", elotSequence='" + elotSequence + '\'' +
              ", elotSort='" + elotSort + '\'' +
              ", latitude=" + latitude +
              ", longitude=" + longitude +
              ", precision='" + precision + '\'' +
              ", timeZone='" + timeZone + '\'' +
              ", utcOffset=" + utcOffset +
              ", dst='" + dst + '\'' +
              '}';
    }
}

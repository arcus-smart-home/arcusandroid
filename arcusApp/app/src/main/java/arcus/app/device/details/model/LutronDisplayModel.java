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
package arcus.app.device.details.model;



public class LutronDisplayModel {

    private boolean isBannerVisible;
    private boolean isCloudConnected;
    private String cloudErrorCode;

    public boolean isBannerVisible() {
        return isBannerVisible;
    }

    public void setBannerVisible(boolean bannerVisible) {
        isBannerVisible = bannerVisible;
    }

    public boolean isCloudConnected() {
        return isCloudConnected;
    }

    public void setCloudConnected(boolean isCloudConnected) {
        this.isCloudConnected = isCloudConnected;
    }

    public String getCloudErrorCode() {
        return cloudErrorCode;
    }

    public void setCloudErrorCode(String errorcode) {
        this.cloudErrorCode = errorcode;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LutronDisplayModel that = (LutronDisplayModel) o;

        if (isBannerVisible != that.isBannerVisible) return false;
        if (isCloudConnected != that.isCloudConnected) return false;
        return cloudErrorCode != null ? cloudErrorCode.equals(that.cloudErrorCode) : that.cloudErrorCode == null;

    }

    @Override
    public int hashCode() {
        int result = (isBannerVisible ? 1 : 0);
        result = 31 * result + (isCloudConnected ? 1 : 0);
        result = 31 * result + (cloudErrorCode != null ? cloudErrorCode.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "LutronBridgeDisplayModel{" +
                "isBannerVisible=" + isBannerVisible +

                ", isCloudConnected=" + isCloudConnected +
                ", cloudErrorCode='" + cloudErrorCode + '\'' +
                '}';
    }

}

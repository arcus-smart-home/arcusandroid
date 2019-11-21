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
package arcus.app.common.models;

import androidx.annotation.Nullable;

import com.iris.client.capability.Device;
import com.iris.client.capability.Person;
import com.iris.client.capability.Place;
import com.iris.client.capability.Scene;

import org.apache.commons.lang3.builder.ToStringBuilder;




//  Object used for storing data for various ListViews in ChooseDevice
//  Simplifies handling the creating of each list and using the correct icon


public class ListItemModel {

    private boolean isHeadingRow = false;
    private String text;
    private String subText;
    private String abstractText;
    private int count = 0;
    private Integer imageResId;
    private int stringResId;
    private Object data;
    private boolean checked;
    private String state;
    private static final String scenePrefix = "SERV:" + Scene.NAMESPACE + ":";
    private static final String personPrefix = "SERV:" + Person.NAMESPACE + ":";
    private static final String placePrefix = "SERV:" + Place.NAMESPACE + ":";
    private static final String devicePrefix = "DRIV:" + Device.NAMESPACE + ":";

    private String address;

    public ListItemModel() {

    }

    public ListItemModel(String mainText) {
        this.text = mainText;
    }


    public ListItemModel(String mainText, String subText) {
        this.text = mainText;
        this.subText = subText;
    }

    public boolean isPersonModel() {
        return address != null && address.startsWith(personPrefix);
    }

    public boolean isDeviceModel() {
        return address != null && address.startsWith(devicePrefix);
    }

    public boolean isPlaceModel() {
        return address != null && address.startsWith(placePrefix);
    }

    public boolean isSceneModel() {
        return address != null && address.startsWith(scenePrefix);
    }

    public boolean isSupportedModel() {
        return isPersonModel() || isDeviceModel() || isPlaceModel() || isSceneModel();
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public
    @Nullable
    String getAddress() {
        return address;
    }

    public void setImageResId(Integer imageResId) {
        this.imageResId = imageResId;
    }

    public Integer getImageResId() {
        return imageResId;
    }

    public String getText() {
        return (text);
    }

    public String getSubText() {
        return (subText);
    }

    public String getAbstractText() {
        return abstractText;
    }

    public int getCount() {
        return (count);
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setSubText(String subText) {
        this.subText = subText;
    }

    public void setAbstractText(String abstractText) {
        this.abstractText = abstractText;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public void setChecked(boolean isChecked) {
        this.checked = isChecked;
    }

    public boolean isChecked() {
        return this.checked;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public Object getData() {
        return this.data;
    }

    public boolean isHeadingRow() {
        return isHeadingRow;
    }

    public void setIsHeadingRow(boolean isHeadingRow) {
        this.isHeadingRow = isHeadingRow;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).build();
    }
}

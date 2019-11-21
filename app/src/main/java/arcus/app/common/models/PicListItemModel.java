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
import android.text.TextUtils;

import arcus.cornea.subsystem.presence.model.PresenceState;
import com.iris.client.model.DeviceModel;
import arcus.app.common.utils.StringUtils;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class PicListItemModel {
    private String deviceName;
    private String relationship;
    private DeviceModel deviceModel;
    private String personId;
    private PresenceState presenceState;
    private String firstName;
    private String lastName;

    private String blurb;

    private String headerName;
    private int listItems;

    public PicListItemModel(String blurb) {
        this.blurb = blurb;
    }

    public PicListItemModel(String header, int items) {
        this.headerName = header;
        this.listItems = items;
    }

    public PicListItemModel(String deviceName, String firstName, String lastName, String personRelationship, String personId, DeviceModel deviceModel, PresenceState presenceState) {
        this.deviceName = deviceName;
        this.firstName = firstName;
        this.lastName = lastName;
        this.relationship = personRelationship;
        this.personId = personId;
        this.deviceModel = deviceModel;
        this.presenceState = presenceState;
    }

    public String getPersonId() {
        return personId;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPersonName () {
        return getFirstName() + " " + getLastName();
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getRelationship() {
        return relationship;
    }

    public DeviceModel getDeviceModel() {
        return deviceModel;
    }

    public void setDeviceModel(DeviceModel deviceModel) {
        this.deviceModel = deviceModel;
    }

    public PresenceState getPresenceState() {
        return presenceState;
    }

    public boolean hasAssignedPerson () {
        return !StringUtils.isEmpty(personId);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    @Override
    public int hashCode () {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public boolean equals (Object o) {
        return EqualsBuilder.reflectionEquals(this, o);
    }

    public @Nullable String getHeaderName(){
        return headerName;
    }

    public int getListItems(){
        return listItems;
    }

    public String getBlurb() {
        return blurb;
    }

    public void setBlurb(String blurb) {
        this.blurb = blurb;
    }

    public boolean isHeaderRow() { return !TextUtils.isEmpty(headerName); }

    public boolean isBlurb() { return !TextUtils.isEmpty(blurb); }
}

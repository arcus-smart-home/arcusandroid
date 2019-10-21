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
package arcus.app.subsystems.people.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import java.util.ArrayList;


public class DeviceContact implements Parcelable {
    private String firstName;
    private String lastName;
    private String validationCode;
    private String placeID;
    private String invitationEmail;
    private String invitorFirstName;
    private String invitorLastName;
    private String invitedPlaceName;
    private ArrayList<DeviceContactData> phoneNumbers = new ArrayList<>();
    private ArrayList<DeviceContactData> emailAddresses = new ArrayList<>();
    public DeviceContact() {

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

    public String getValidationCode() {
        return validationCode;
    }

    public void setValidationCode(String validationCode) {
        this.validationCode = validationCode;
    }

    public void addPhoneNumber(String number, String type) {
        this.phoneNumbers.add(new DeviceContactData(number, type));
    }

    public ArrayList<DeviceContactData> getPhoneNumbers() {
        return phoneNumbers;
    }

    public void setPhoneNumbers(ArrayList<DeviceContactData> phoneNumbers) {
        this.phoneNumbers = phoneNumbers;
    }

    public void addEmailAddress(String email, String type) {
        this.emailAddresses.add(new DeviceContactData(email, type));
    }

    public ArrayList<DeviceContactData> getEmailAddresses() {
        return emailAddresses;
    }

    public void setEmailAddresses(ArrayList<DeviceContactData> emailAddresses) {
        this.emailAddresses = emailAddresses;
    }

    public boolean hasPlaceIDSet() {
        return !TextUtils.isEmpty(placeID);
    }

    public String getPlaceID() {
        return placeID;
    }

    public void setPlaceID(String placeID) {
        this.placeID = placeID;
    }

    public String getInvitationEmail() {
        return invitationEmail;
    }

    public void setInvitationEmail(String invitationEmail) {
        this.invitationEmail = invitationEmail;
    }

    public String getInvitorFirstName() {
        return invitorFirstName;
    }

    public void setInvitorFirstName(String invitorFirstName) {
        this.invitorFirstName = invitorFirstName;
    }

    public String getInvitorLastName() {
        return invitorLastName;
    }

    public void setInvitorLastName(String invitorLastName) {
        this.invitorLastName = invitorLastName;
    }

    public String getInvitedPlaceName() {
        return invitedPlaceName;
    }

    public void setInvitedPlaceName(String invitedPlaceName) {
        this.invitedPlaceName = invitedPlaceName;
    }

    @Override
    public int hashCode() {
        int result = firstName != null ? firstName.hashCode() : 0;
        result = 31 * result + (lastName != null ? lastName.hashCode() : 0);
        result = 31 * result + (validationCode != null ? validationCode.hashCode() : 0);
        result = 31 * result + (placeID != null ? placeID.hashCode() : 0);
        result = 31 * result + (phoneNumbers != null ? phoneNumbers.hashCode() : 0);
        result = 31 * result + (emailAddresses != null ? emailAddresses.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "DeviceContact{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", placeID='" + placeID + '\'' +
                ", validationCode='" + validationCode + '\'' +
                ", phoneNumbers=" + phoneNumbers.toString() +
                ", emailAddresses=" + emailAddresses.toString() +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(firstName);
        parcel.writeString(lastName);
        parcel.writeString(validationCode);
        parcel.writeString(placeID);
        parcel.writeList(phoneNumbers);
        parcel.writeList(emailAddresses);
    }

    protected DeviceContact(Parcel in) {
        firstName = in.readString();
        lastName = in.readString();
        validationCode = in.readString();
        placeID = in.readString();
        phoneNumbers = in.readArrayList(DeviceContactData.class.getClassLoader());
        emailAddresses = in.readArrayList(DeviceContactData.class.getClassLoader());
    }

    public static final Creator<DeviceContact> CREATOR = new Creator<DeviceContact>() {
        public DeviceContact createFromParcel(Parcel source) {
            return new DeviceContact(source);
        }

        public DeviceContact[] newArray(int size) {
            return new DeviceContact[size];
        }
    };
}

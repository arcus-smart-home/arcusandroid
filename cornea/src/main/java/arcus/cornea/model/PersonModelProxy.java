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
package arcus.cornea.model;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.TextUtils;

import com.iris.capability.util.Addresses;
import com.iris.client.bean.PersonAccessDescriptor;
import com.iris.client.capability.Person;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class PersonModelProxy implements Parcelable, Serializable, Comparable<PersonModelProxy> {
    final String personID;
    final String firstName;
    final String lastName;
    final String emailAddress;
    final String role;
    final long invitedDate;
    final String code;

    protected PersonModelProxy(
          String personID, String firstName, String lastName, String role,
          long invitedDate, String emailAddress, String code
    ) {
        this.personID = personID;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
        this.invitedDate = invitedDate;
        this.emailAddress = emailAddress;
        this.code = code;
    }

    public PersonModelProxy(String personID) {
        this(personID, "", "", "", -1, "", "");
    }

    public PersonModelProxy(@NonNull Map<String, Object> personMap, String role) {
        String id = (String) personMap.get(Person.ATTR_ID);
        String first = (String) personMap.get(Person.ATTR_FIRSTNAME);
        String last = (String) personMap.get(Person.ATTR_LASTNAME);
        String email = (String) personMap.get(Person.ATTR_EMAIL);

        this.personID  = TextUtils.isEmpty(id) ? "" : id;
        this.firstName = TextUtils.isEmpty(first) ? "" : first;
        this.lastName  = TextUtils.isEmpty(last) ? "" : last;
        this.emailAddress  = TextUtils.isEmpty(email) ? "" : email;
        this.role = role;

        this.invitedDate = -1;
        this.code = "";
    }

    @SuppressWarnings({"ConstantConditions"})
    public static PersonModelProxy fromInvitation(@NonNull Map<String, Object> inviteResponse) {
        if (inviteResponse == null) {
            inviteResponse = new HashMap<>();
        }

        String first = (String) inviteResponse.get("inviteeFirstName");
        String last = (String) inviteResponse.get("inviteeLastName");
        String email = (String) inviteResponse.get("inviteeEmail");
        String code = (String) inviteResponse.get("code");
        Number created = (Number) inviteResponse.get("created");
        String id = (String) inviteResponse.get(Person.ATTR_ID);

        return new PersonModelProxy(
              TextUtils.isEmpty(id) ? "" : id,
              TextUtils.isEmpty(first) ? "" : first,
              TextUtils.isEmpty(last) ? "" : last,
              "",
              created != null ? created.longValue() : -1,
              TextUtils.isEmpty(email) ? "" : email,
              TextUtils.isEmpty(code) ? "" : code
        );
    }

    public String getPersonID() {
        return personID;
    }

    public String getPersonAddress() {
        return Addresses.toObjectAddress(Person.NAMESPACE, personID);
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public @NonNull String getFullName() {
        return String.format("%s %s", firstName, lastName);
    }

    public boolean isInvited() {
        return invitedDate != -1;
    }

    public long getInvitedDate() {
        return invitedDate;
    }

    public boolean isOwner() {
        return PersonAccessDescriptor.ROLE_OWNER.equals(role);
    }

    public boolean isClone() {
        return PersonAccessDescriptor.ROLE_FULL_ACCESS.equals(role);
    }

    public boolean isHobbit() {
        return PersonAccessDescriptor.ROLE_HOBBIT.equals(role);
    }

    public @Nullable String getCode() {
        return code;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    @Override public int compareTo(@NonNull PersonModelProxy another) {
        return getFullName().compareToIgnoreCase(another.getFullName());
    }

    @Override public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PersonModelProxy that = (PersonModelProxy) o;

        if (invitedDate != that.invitedDate) {
            return false;
        }
        if (personID != null ? !personID.equals(that.personID) : that.personID != null) {
            return false;
        }
        if (firstName != null ? !firstName.equals(that.firstName) : that.firstName != null) {
            return false;
        }
        if (lastName != null ? !lastName.equals(that.lastName) : that.lastName != null) {
            return false;
        }
        if (emailAddress != null ? !emailAddress.equals(that.emailAddress) : that.emailAddress != null) {
            return false;
        }
        if (code != null ? !code.equals(that.code) : that.code != null) {
            return false;
        }
        return role != null ? role.equals(that.role) : that.role == null;

    }

    @Override public int hashCode() {
        int result = personID != null ? personID.hashCode() : 0;
        result = 31 * result + (firstName != null ? firstName.hashCode() : 0);
        result = 31 * result + (lastName != null ? lastName.hashCode() : 0);
        result = 31 * result + (emailAddress != null ? emailAddress.hashCode() : 0);
        result = 31 * result + (role != null ? role.hashCode() : 0);
        result = 31 * result + (int) (invitedDate ^ (invitedDate >>> 32));
        result = 31 * result + (code != null ? code.hashCode() : 0);
        return result;
    }

    @Override public String toString() {
        return "PersonModelProxy{" +
              "personID='" + personID + '\'' +
              ", firstName='" + firstName + '\'' +
              ", lastName='" + lastName + '\'' +
              ", emailAddress='" + emailAddress + '\'' +
              ", role='" + role + '\'' +
              ", invitedDate=" + invitedDate +
              ", code=" + code +
              '}';
    }

    @Override public int describeContents() {
        return 0;
    }

    @Override public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.personID);
        dest.writeString(this.firstName);
        dest.writeString(this.lastName);
        dest.writeString(this.emailAddress);
        dest.writeString(this.role);
        dest.writeLong(this.invitedDate);
        dest.writeString(this.code);
    }

    protected PersonModelProxy(Parcel in) {
        this.personID = in.readString();
        this.firstName = in.readString();
        this.lastName = in.readString();
        this.emailAddress = in.readString();
        this.role = in.readString();
        this.invitedDate = in.readLong();
        this.code = in.readString();
    }

    public static final Creator<PersonModelProxy> CREATOR = new Creator<PersonModelProxy>() {
        @Override public PersonModelProxy createFromParcel(Parcel source) {
            return new PersonModelProxy(source);
        }

        @Override public PersonModelProxy[] newArray(int size) {
            return new PersonModelProxy[size];
        }
    };
}

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

import java.util.HashMap;
import java.util.Map;

public class InviteModel implements Parcelable {
    private final String inviteeEmail;
    private final String inviteeFirstName;
    private final String inviteeLastName;
    private final String relationship;
    private final String invitationText;
    private String personalizedGreeting;

    public InviteModel(Map<String, Object> fromThis) {
        this.inviteeEmail = (String) fromThis.get("inviteeEmail");
        this.inviteeFirstName = (String) fromThis.get("inviteeFirstName");
        this.inviteeLastName = (String) fromThis.get("inviteeLastName");
        this.relationship = (String) fromThis.get("relationship");
        this.invitationText = (String) fromThis.get("invitationText");
    }

    public String getInviteeEmail() {
        return inviteeEmail;
    }

    public String getInviteeFirstName() {
        return inviteeFirstName;
    }

    public String getInviteeLastName() {
        return inviteeLastName;
    }

    public String getRelationship() {
        return relationship;
    }

    public String getInvitationText() {
        return invitationText;
    }

    public String getPersonalizedGreeting() {
        return personalizedGreeting;
    }

    public void setPersonalizedGreeting(String personalizedGreeting) {
        this.personalizedGreeting = personalizedGreeting;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> retValue = new HashMap<>(7);

        retValue.put("inviteeEmail", this.inviteeEmail);
        retValue.put("inviteeFirstName", this.inviteeFirstName);
        retValue.put("inviteeLastName", this.inviteeLastName);
        retValue.put("relationship", this.relationship);
        retValue.put("invitationText", this.invitationText);
        retValue.put("personalizedGreeting", this.personalizedGreeting);

        return retValue;
    }

    @Override public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        InviteModel that = (InviteModel) o;

        if (inviteeEmail != null ? !inviteeEmail.equals(that.inviteeEmail) : that.inviteeEmail != null) {
            return false;
        }
        if (inviteeFirstName != null ? !inviteeFirstName.equals(that.inviteeFirstName) : that.inviteeFirstName != null) {
            return false;
        }
        if (inviteeLastName != null ? !inviteeLastName.equals(that.inviteeLastName) : that.inviteeLastName != null) {
            return false;
        }
        if (relationship != null ? !relationship.equals(that.relationship) : that.relationship != null) {
            return false;
        }
        if (invitationText != null ? !invitationText.equals(that.invitationText) : that.invitationText != null) {
            return false;
        }
        return personalizedGreeting != null ? personalizedGreeting.equals(that.personalizedGreeting) : that.personalizedGreeting == null;

    }

    @Override public int hashCode() {
        int result = inviteeEmail != null ? inviteeEmail.hashCode() : 0;
        result = 31 * result + (inviteeFirstName != null ? inviteeFirstName.hashCode() : 0);
        result = 31 * result + (inviteeLastName != null ? inviteeLastName.hashCode() : 0);
        result = 31 * result + (relationship != null ? relationship.hashCode() : 0);
        result = 31 * result + (invitationText != null ? invitationText.hashCode() : 0);
        result = 31 * result + (personalizedGreeting != null ? personalizedGreeting.hashCode() : 0);
        return result;
    }

    @Override public String toString() {
        return "InviteModel{" +
              "inviteeEmail='" + inviteeEmail + '\'' +
              ", inviteeFirstName='" + inviteeFirstName + '\'' +
              ", inviteeLastName='" + inviteeLastName + '\'' +
              ", relationship='" + relationship + '\'' +
              ", invitationText='" + invitationText + '\'' +
              ", personalizedGreeting='" + personalizedGreeting + '\'' +
              '}';
    }

    @Override public int describeContents() {
        return 0;
    }

    @Override public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.inviteeEmail);
        dest.writeString(this.inviteeFirstName);
        dest.writeString(this.inviteeLastName);
        dest.writeString(this.relationship);
        dest.writeString(this.invitationText);
        dest.writeString(this.personalizedGreeting);
    }

    protected InviteModel(Parcel in) {
        this.inviteeEmail = in.readString();
        this.inviteeFirstName = in.readString();
        this.inviteeLastName = in.readString();
        this.relationship = in.readString();
        this.invitationText = in.readString();
        this.personalizedGreeting = in.readString();
    }

    public static final Parcelable.Creator<InviteModel> CREATOR = new Parcelable.Creator<InviteModel>() {
        @Override public InviteModel createFromParcel(Parcel source) {
            return new InviteModel(source);
        }

        @Override public InviteModel[] newArray(int size) {
            return new InviteModel[size];
        }
    };
}

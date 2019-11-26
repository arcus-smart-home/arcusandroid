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

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import arcus.cornea.SessionController;
import arcus.cornea.dto.ProductBrandAndCount;
import arcus.cornea.dto.ProductCategoryAndCount;
import com.iris.client.model.AccountModel;
import com.iris.client.model.DeviceModel;
import com.iris.client.model.PersonModel;
import com.iris.client.model.PlaceModel;
import com.iris.client.model.ProductModel;
import com.iris.client.session.SessionInfo;
import arcus.app.pairing.hub.original.model.ArcusStep;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class RegistrationContext implements Parcelable {

    private String firstName;
    private String lastName;
    private String emailAddress;
    private String password;
    private String mobileNumber;

    private String securityQuestionOne;
    private String securityQuestionTwo;
    private String securityQuestionThree;
    private String securityAnswerOne;
    private String securityAnswerTwo;
    private String securityAnswerThree;
    private boolean newsAndOffer = false;
    private String billingFirstName;
    private String billingLastName;
    private String homeNickName;
    private String address1;
    private String address2;
    private String city;
    private String state;
    private String zip;
    private String country;
    private boolean premium = false;
    private boolean cellBackup = false;
    private boolean cellPrimary = false;
    private boolean extraVideo = false;
    @Nullable
    private UUID accountId;
    @Nullable
    private UUID personId;

    @Nullable
    private ArrayList<ArcusStep> devicePairingArcusStepList = null;

    @Nullable
    private List<ProductCategoryAndCount> categoryCountList = null;
    @Nullable
    private List<ProductBrandAndCount> brandCountList = null;
    @Nullable
    private List<ProductModel> productModelList = null;

    private String hubID;
    private String hubState;

    private HashMap<String,String> creditInfo;

    private ArrayList<DeviceModel> multiPairedDevices;

    private int multiPairingDeviceCount = 0;
    private int imageHeight = 0;

    private boolean[] multiDevicePairedNamed;

    private DeviceModel pairedModel;

    private static RegistrationContext registrationContext = null;
    private boolean foundSingleDevice = false;


    private RegistrationContext() {
    }

    public static RegistrationContext getInstance(){
        if(registrationContext == null){
            registrationContext = new RegistrationContext();
        }
        return registrationContext;
    }

    public DeviceModel getPairedModel() {
        return pairedModel;
    }

    public HashMap<String, String> getCreditInfo() {
        return creditInfo;
    }

    public void setCreditInfo(HashMap<String, String> creditInfo) {
        this.creditInfo = creditInfo;
    }

    public String getHubID() {
        return hubID;
    }

    public void setHubID(String hubID) {
        this.hubID = hubID;
    }

    @Nullable
    public AccountModel getAccountModel() {
        return SessionController.instance().getAccount();
    }

    @Nullable
    public PersonModel getPersonModel() {
        return SessionController.instance().getPerson();
    }

    @Nullable
    public PlaceModel getPlaceModel() {
        return SessionController.instance().getPlace();
    }

    @Nullable public SessionInfo getSessionInfo() {
        return SessionController.instance().getSessionInfo();
    }

    public String getFirstName() {
        return getPersonModel().getFirstName();
    }

    public void setFirstName(String firstName) {
        getPersonModel().setFirstName(firstName);
    }

    public String getLastName() {
        return getPersonModel().getLastName();
    }

    public void setLastName(String lastName) {
        getPersonModel().setLastName(lastName);
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getMobileNumber() {
        return getPersonModel().getMobileNumber();
    }

    public void setMobileNumber(String mobileNumber) {
        getPersonModel().setMobileNumber(mobileNumber);
    }

    // TODO: Security Q&As should be fetched from PersonModel
    public String getSecurityQuestionOne() {
        return securityQuestionOne;
    }
    public void setSecurityQuestionOne(String securityQuestionOne) {
        this.securityQuestionOne = securityQuestionOne;
    }
    public String getSecurityQuestionTwo() {
        return securityQuestionTwo;
    }
    public void setSecurityQuestionTwo(String securityQuestionTwo) {
        this.securityQuestionTwo = securityQuestionTwo;
    }
    public String getSecurityQuestionThree() {
        return securityQuestionThree;
    }
    public void setSecurityQuestionThree(String securityQuestionThree) {
        this.securityQuestionThree = securityQuestionThree;
    }
    public String getSecurityAnswerOne() {
        return securityAnswerOne;
    }
    public void setSecurityAnswerOne(String securityAnswerOne) {
        this.securityAnswerOne = securityAnswerOne;
    }
    public String getSecurityAnswerTwo() {
        return securityAnswerTwo;
    }
    public void setSecurityAnswerTwo(String securityAnswerTwo) {
        this.securityAnswerTwo = securityAnswerTwo;
    }
    public String getSecurityAnswerThree() {
        return securityAnswerThree;
    }
    public void setSecurityAnswerThree(String securityAnswerThree) {
        this.securityAnswerThree = securityAnswerThree;
    }

    public String getCity() {
        return getPlaceModel().getCity();
    }

    public void setCity(String city) {
        getPlaceModel().setCity(city);
    }

    public String getState() {
        return getPlaceModel().getState();
    }

    public void setState(String state) {
        getPlaceModel().setState(state);
        getPlaceModel().commit();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(firstName);
        dest.writeString(lastName);
        dest.writeString(emailAddress);
        dest.writeString(password);
        dest.writeString(mobileNumber);
        dest.writeString(securityQuestionOne);
        dest.writeString(securityQuestionTwo);
        dest.writeString(securityQuestionThree);
        dest.writeString(securityAnswerOne);
        dest.writeString(securityAnswerTwo);
        dest.writeString(securityAnswerThree);
        dest.writeByte((byte) (newsAndOffer ? 1 : 0));
        dest.writeString(billingFirstName);
        dest.writeString(billingLastName);
        dest.writeString(homeNickName);
        dest.writeString(address1);
        dest.writeString(address2);
        dest.writeString(city);
        dest.writeString(state);
        dest.writeString(zip);
        dest.writeString(country);
        dest.writeByte((byte)(premium ? 1 : 0));
        dest.writeByte((byte)(cellBackup ? 1 : 0));
        dest.writeByte((byte)(cellPrimary ? 1 : 0));
        dest.writeByte((byte)(extraVideo ? 1 : 0));
        dest.writeString(accountId == null ? null : accountId.toString());
        dest.writeString(personId == null ? null : personId.toString());
        dest.writeByte((byte) (foundSingleDevice ? 1 : 0) );
    }

    private RegistrationContext(@NonNull Parcel in) {
        firstName = in.readString();
        lastName = in.readString();
        emailAddress = in.readString();
        password = in.readString();
        mobileNumber = in.readString();
        securityQuestionOne = in.readString();
        securityQuestionTwo = in.readString();
        securityQuestionThree = in.readString();
        securityAnswerOne = in.readString();
        securityAnswerTwo = in.readString();
        securityAnswerThree = in.readString();
        newsAndOffer = in.readByte() == 1;
        billingFirstName = in.readString();
        billingLastName = in.readString();
        homeNickName = in.readString();
        address1 = in.readString();
        address2 = in.readString();
        city = in.readString();
        state = in.readString();
        zip = in.readString();
        country = in.readString();
        premium = in.readByte() == 1;
        cellBackup = in.readByte() == 1;
        cellPrimary = in.readByte() == 1;
        extraVideo = in.readByte() == 1;

        String accountIdStr = in.readString();
        accountId = StringUtils.isBlank(accountIdStr) ? null : UUID.fromString(accountIdStr);

        String personIdStr = in.readString();
        personId = StringUtils.isBlank(personIdStr) ? null : UUID.fromString(personIdStr);
        this.foundSingleDevice = in.readByte() == 1;
    }

    public static final Creator<RegistrationContext> CREATOR = new Creator<RegistrationContext>() {
        @NonNull
        public RegistrationContext createFromParcel(@NonNull Parcel in) {
            return new RegistrationContext(in);
        }

        @NonNull
        public RegistrationContext[] newArray(int size) {
            return new RegistrationContext[size];
        }
    };

}

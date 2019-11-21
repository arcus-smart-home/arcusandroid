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
package arcus.cornea.subsystem.connection.model;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.iris.client.capability.CellBackupSubsystem;

public class CellBackupModel implements Parcelable {
    private final String cellStatus;
    private final String cellErrorState;
    private final String cellNotReadyState;
    private final Long suspensionDate;

    public static CellBackupModel empty() {
        return new CellBackupModel();
    }

    CellBackupModel() {
        this.cellStatus = this.cellErrorState = this.cellNotReadyState = "";
        this.suspensionDate = null;
    }

    public CellBackupModel(
          @NonNull String cellStatus,
          @NonNull String cellErrorState,
          @NonNull String cellNotReadyState,
          @Nullable Long suspensionDate
    ) {
        this.cellStatus = cellStatus;
        this.cellErrorState = cellErrorState;
        this.cellNotReadyState = cellNotReadyState;
        this.suspensionDate = suspensionDate;
    }

    public @Nullable Long getSuspensionDate() {
        return suspensionDate;
    }

    public boolean cellularConnectionActive() {
        return CellBackupSubsystem.STATUS_ACTIVE.equals(cellStatus);
    }

    // Customer has had their backup service disabled by support for some reason
    public boolean serviceSuspended() {
        return CellBackupSubsystem.STATUS_ERRORED.equals(this.cellStatus) &&
              CellBackupSubsystem.ERRORSTATE_BANNED.equals(this.cellErrorState);
    }

    // Have subscription and modem but modem has no SIM card
    // OR
    // Have subscription and modem but modem SIM was not provisioned properly
    public boolean requiresConfiguration() {
        return CellBackupSubsystem.STATUS_ERRORED.equals(this.cellStatus) &&
              (CellBackupSubsystem.ERRORSTATE_NOSIM.equals(this.cellErrorState) ||
                    CellBackupSubsystem.ERRORSTATE_NOTPROVISIONED.equals(this.cellErrorState));
    }

    // Have modem but no subscription
    public boolean needsServicePlan() {
        return CellBackupSubsystem.STATUS_NOTREADY.equals(this.cellStatus) &&
               CellBackupSubsystem.NOTREADYSTATE_NEEDSSUB.equals(this.cellNotReadyState);
    }

    @SuppressWarnings("ConstantConditions") @Override public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        CellBackupModel that = (CellBackupModel) o;

        if (cellStatus != null ? !cellStatus.equals(that.cellStatus) : that.cellStatus != null) {
            return false;
        }
        if (cellErrorState != null ? !cellErrorState.equals(that.cellErrorState) : that.cellErrorState != null) {
            return false;
        }
        if (cellNotReadyState != null ? !cellNotReadyState.equals(that.cellNotReadyState) : that.cellNotReadyState != null) {
            return false;
        }
        return suspensionDate != null ? suspensionDate.equals(that.suspensionDate) : that.suspensionDate == null;

    }

    @SuppressWarnings("ConstantConditions") @Override public int hashCode() {
        int result = cellStatus != null ? cellStatus.hashCode() : 0;
        result = 31 * result + (cellErrorState != null ? cellErrorState.hashCode() : 0);
        result = 31 * result + (cellNotReadyState != null ? cellNotReadyState.hashCode() : 0);
        result = 31 * result + (suspensionDate != null ? suspensionDate.hashCode() : 0);
        return result;
    }

    @Override public String toString() {
        return "CellBackupModel{" +
              "cellStatus='" + cellStatus + '\'' +
              ", cellErrorState='" + cellErrorState + '\'' +
              ", cellNotReadyState='" + cellNotReadyState + '\'' +
              ", suspensionDate=" + suspensionDate +
              '}';
    }

    @Override public int describeContents() {
        return 0;
    }

    @Override public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.cellStatus);
        dest.writeString(this.cellErrorState);
        dest.writeString(this.cellNotReadyState);
        dest.writeValue(this.suspensionDate);
    }

    protected CellBackupModel(Parcel in) {
        this.cellStatus = in.readString();
        this.cellErrorState = in.readString();
        this.cellNotReadyState = in.readString();
        this.suspensionDate = (Long) in.readValue(Long.class.getClassLoader());
    }

    public static final Parcelable.Creator<CellBackupModel> CREATOR = new Parcelable.Creator<CellBackupModel>() {
        @Override public CellBackupModel createFromParcel(Parcel source) {
            return new CellBackupModel(source);
        }

        @Override public CellBackupModel[] newArray(int size) {
            return new CellBackupModel[size];
        }
    };
}

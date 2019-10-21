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
package arcus.cornea.device.lightsnswitches;

import android.os.Parcel;
import android.os.Parcelable;

public class LightSwitchProxyModel implements Parcelable {
    private int dimPercent;
    private int colorTemp;
    private boolean on;

    private boolean supportsColorTemp;
    private boolean supportsOnOff;
    private boolean supportsDim;

    public LightSwitchProxyModel() {
    }

    public int getDimPercent() {
        return dimPercent;
    }

    public void setDimPercent(int dimPercent) {
        this.dimPercent = dimPercent;
    }

    public int getColorTemp() {
        return colorTemp;
    }

    public void setColorTemp(int colorTemp) {
        this.colorTemp = colorTemp;
    }

    public boolean isSwitchOn() {
        return on;
    }

    public void setSwitchOn(boolean on) {
        this.on = on;
    }

    public boolean supportsColorTemp() {
        return supportsColorTemp;
    }

    public void setSupportsColorTemp(boolean supportsColorTemp) {
        this.supportsColorTemp = supportsColorTemp;
    }

    public boolean supportsOnOff() {
        return supportsOnOff;
    }

    public void setSupportsOnOff(boolean supportsOnOff) {
        this.supportsOnOff = supportsOnOff;
    }

    public boolean supportsDim() {
        return supportsDim;
    }

    public void setSupportsDim(boolean supportsDim) {
        this.supportsDim = supportsDim;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        LightSwitchProxyModel that = (LightSwitchProxyModel) o;

        if (dimPercent != that.dimPercent) {
            return false;
        }
        if (colorTemp != that.colorTemp) {
            return false;
        }
        if (on != that.on) {
            return false;
        }
        if (supportsColorTemp != that.supportsColorTemp) {
            return false;
        }
        if (supportsOnOff != that.supportsOnOff) {
            return false;
        }
        return supportsDim == that.supportsDim;

    }

    @Override
    public int hashCode() {
        int result = dimPercent;
        result = 31 * result + colorTemp;
        result = 31 * result + (on ? 1 : 0);
        result = 31 * result + (supportsColorTemp ? 1 : 0);
        result = 31 * result + (supportsOnOff ? 1 : 0);
        result = 31 * result + (supportsDim ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "LightSwitchProxyModel{" +
              "dimPercent=" + dimPercent +
              ", colorTemp=" + colorTemp +
              ", on=" + on +
              ", supportsColorTemp=" + supportsColorTemp +
              ", supportsOnOff=" + supportsOnOff +
              ", supportsDim=" + supportsDim +
              '}';
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.dimPercent);
        dest.writeInt(this.colorTemp);
        dest.writeByte(on ? (byte) 1 : (byte) 0);
        dest.writeByte(supportsColorTemp ? (byte) 1 : (byte) 0);
        dest.writeByte(supportsOnOff ? (byte) 1 : (byte) 0);
        dest.writeByte(supportsDim ? (byte) 1 : (byte) 0);
    }

    protected LightSwitchProxyModel(Parcel in) {
        this.dimPercent = in.readInt();
        this.colorTemp = in.readInt();
        this.on = in.readByte() != 0;
        this.supportsColorTemp = in.readByte() != 0;
        this.supportsOnOff = in.readByte() != 0;
        this.supportsDim = in.readByte() != 0;
    }

    public static final Parcelable.Creator<LightSwitchProxyModel> CREATOR = new Parcelable.Creator<LightSwitchProxyModel>() {
        public LightSwitchProxyModel createFromParcel(Parcel source) {
            return new LightSwitchProxyModel(source);
        }

        public LightSwitchProxyModel[] newArray(int size) {
            return new LightSwitchProxyModel[size];
        }
    };
}

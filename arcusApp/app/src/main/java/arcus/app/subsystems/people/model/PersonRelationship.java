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

import java.util.ArrayList;


public class PersonRelationship implements Parcelable {
    boolean selected;
    String name;
    String description;
    ArrayList<PersonRelationship> children = new ArrayList<>();

    public PersonRelationship(boolean selected, String name, ArrayList<PersonRelationship> children) {
        this.selected = selected;
        this.name = name;
        this.children = children;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ArrayList<PersonRelationship> getChildren() {
        return children;
    }

    public void setChildren(ArrayList<PersonRelationship> children) {
        this.children = children;
    }

    protected PersonRelationship(Parcel in) {
        this.selected = in.readByte() != 0;
        this.name = in.readString();
        this.children = in.readArrayList(PersonRelationship.class.getClassLoader());
    }

    public static final Creator<PersonRelationship> CREATOR = new Creator<PersonRelationship>() {
        @Override
        public PersonRelationship createFromParcel(Parcel in) {
            return new PersonRelationship(in);
        }

        @Override
        public PersonRelationship[] newArray(int size) {
            return new PersonRelationship[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeByte(selected ? (byte) 1 : (byte) 0);
        parcel.writeString(name);
        parcel.writeList(children);
    }
}

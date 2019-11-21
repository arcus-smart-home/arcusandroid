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
package arcus.cornea.rules.model;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;

import com.iris.capability.util.Addresses;
import com.iris.client.capability.Rule;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class RuleProxyModel implements Parcelable, Comparable<RuleProxyModel> {
    private String id, name, description, template;
    private boolean enabled;
    private HashSet<String> categories;

    public RuleProxyModel(String id, String name, String description, String template, boolean enabled) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.template = template;
        this.enabled = enabled;
    }

    public RuleProxyModel() {
    }

    public @NonNull String getName() {
        return name == null ? "" : name;
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

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAddress() {
        return Addresses.toObjectAddress(Rule.NAMESPACE, id);
    }

    public void setCategories(Collection<String> categories) {
        if (categories != null) {
            this.categories = new HashSet<>(categories);
        }
    }

    public @NonNull Set<String> getCategories() {
        return (categories == null) ? Collections.<String>emptySet() : Collections.unmodifiableSet(categories);
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    @Override public int compareTo(@NonNull RuleProxyModel another) {
        return getName().compareToIgnoreCase(another.getName());
    }

    @Override public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        RuleProxyModel that = (RuleProxyModel) o;

        return id != null ? id.equals(that.id) : that.id == null;

    }

    @Override public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override public String toString() {
        return "RuleProxyModel{" +
              "id='" + id + '\'' +
              ", name='" + name + '\'' +
              ", description='" + description + '\'' +
              ", enabled=" + enabled +
              ", categories=" + categories +
              ", template=" + template +
              '}';
    }

    @Override public int describeContents() {
        return 0;
    }

    @Override public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.name);
        dest.writeString(this.description);
        dest.writeString(this.template);
        dest.writeByte(this.enabled ? (byte) 1 : (byte) 0);
        dest.writeSerializable(this.categories);
    }

    @SuppressWarnings("unchecked") protected RuleProxyModel(Parcel in) {
        this.id = in.readString();
        this.name = in.readString();
        this.description = in.readString();
        this.template = in.readString();
        this.enabled = in.readByte() != 0;
        this.categories = (HashSet<String>) in.readSerializable();
    }

    public static final Creator<RuleProxyModel> CREATOR = new Creator<RuleProxyModel>() {
        @Override public RuleProxyModel createFromParcel(Parcel source) {
            return new RuleProxyModel(source);
        }

        @Override public RuleProxyModel[] newArray(int size) {
            return new RuleProxyModel[size];
        }
    };
}

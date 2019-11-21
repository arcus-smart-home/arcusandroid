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
package arcus.cornea.utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.common.base.Function;

import java.lang.ref.Reference;

public class ResultInstructions {
    private final String attribute;
    private final Object value;
    private final Function<String, ?> updateFailedFunction;
    private final Reference<PropertyChangeMonitor.Callback> updateCallback;

    public ResultInstructions(
          @NonNull String attribute,
          @Nullable Object value,
          @Nullable Reference<PropertyChangeMonitor.Callback> updateCallback,
          @Nullable Function<String, ?> updateFailedFunction
    ) {
        this.attribute = attribute;
        this.value = value;
        this.updateFailedFunction = updateFailedFunction;
        this.updateCallback = updateCallback;
    }

    public String getAttribute() {
        return attribute;
    }

    public Object getValue() {
        return value;
    }

    public Function<String, ?> getUpdateFailedFunction() {
        return updateFailedFunction;
    }

    public PropertyChangeMonitor.Callback getUpdateCallback() {
        if (updateCallback == null) {
            return null;
        }

        return updateCallback.get();
    }

    @Override public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ResultInstructions that = (ResultInstructions) o;

        if (attribute != null ? !attribute.equals(that.attribute) : that.attribute != null) {
            return false;
        }
        if (value != null ? !value.equals(that.value) : that.value != null) {
            return false;
        }
        if (updateFailedFunction != null ? !updateFailedFunction.equals(that.updateFailedFunction) : that.updateFailedFunction != null) {
            return false;
        }
        return !(updateCallback != null ? !updateCallback.equals(that.updateCallback) : that.updateCallback != null);

    }

    @Override public int hashCode() {
        int result = attribute != null ? attribute.hashCode() : 0;
        result = 31 * result + (value != null ? value.hashCode() : 0);
        result = 31 * result + (updateFailedFunction != null ? updateFailedFunction.hashCode() : 0);
        result = 31 * result + (updateCallback != null ? updateCallback.hashCode() : 0);
        return result;
    }

    @Override public String toString() {
        return "PropertyChangeResult{" +
              "attribute='" + attribute + '\'' +
              ", value=" + value +
              ", updateFailedFunction=" + updateFailedFunction +
              ", updateCallback=" + updateCallback +
              '}';
    }
}

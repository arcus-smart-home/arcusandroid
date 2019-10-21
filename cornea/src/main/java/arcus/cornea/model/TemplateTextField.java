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

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

public class TemplateTextField {
    /**
     * The text the {@link com.iris.client.model.RuleTemplateModel} shows for this portion of the string.
     */
    private String  text;

    /**
     * The name of the field according to the resolve attributes.
     */
    private String  fieldName;

    /**
     * Used for title casing names/other fields that may want to be title cased.
     */
    private boolean properName = false;

    /**
     * UIHint
     *
     * If the rule text portion is able to be edited.
     */
    private boolean isEditable;

    public TemplateTextField(String text, String fieldName, boolean isEditable) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(text));
        Preconditions.checkArgument(!Strings.isNullOrEmpty(fieldName));

        this.text = text;
        this.fieldName = fieldName;
        this.isEditable = isEditable;
    }

    public void setText(String text) {
        if (!Strings.isNullOrEmpty(text)) {
            this.text = text;
        }
    }

    public String getText() {
        return text;
    }

    public boolean isEditable() {
        return isEditable;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setProperName(boolean isProperName) {
        this.properName = isProperName;
    }

    public boolean isProperName() {
        return properName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        TemplateTextField that = (TemplateTextField) o;

        if (isEditable != that.isEditable) {
            return false;
        }
        if (!text.equals(that.text)) {
            return false;
        }
        return fieldName.equals(that.fieldName);

    }

    @Override
    public int hashCode() {
        int result = text.hashCode();
        result = 31 * result + (isEditable ? 1 : 0);
        result = 31 * result + fieldName.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "TemplateTextField{" +
              "text='" + text + '\'' +
              ", fieldName='" + fieldName + '\'' +
              ", isEditable=" + isEditable +
              '}';
    }
}

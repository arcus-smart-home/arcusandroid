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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RuleDisplayModel {
    private Set<String> editedFieldNames;
    private Set<String> editableFieldNames;
    private List<TemplateTextField> templateTextFields;

    public RuleDisplayModel() {
        editedFieldNames = new HashSet<>();
        editableFieldNames = new HashSet<>();
        this.templateTextFields = new ArrayList<>();
    }

    public RuleDisplayModel(List<TemplateTextField> fields) {
        this();

        if (fields != null && !fields.isEmpty()) {
            for (TemplateTextField field : fields) {
                this.templateTextFields.add(field);
                if (field.isEditable()) {
                    editableFieldNames.add(field.getFieldName());
                }
            }
        }
    }

    public int getEditableFieldsCount() {
        return editableFieldNames.size();
    }

    public boolean allFieldsEdited() {
        return editedFieldNames.containsAll(editableFieldNames);
    }

    public void edited(String name) {
        editedFieldNames.add(name);
    }

    public List<TemplateTextField> getTemplateTextFields() {
        return Collections.unmodifiableList(templateTextFields);
    }

    public void setFieldAsProperName(String fieldname) {
        for (TemplateTextField field : templateTextFields) {
            if (field.getFieldName().equalsIgnoreCase(fieldname)) {
                field.setProperName(true);
            }
        }
    }

    @Override
    public String toString() {
        return "RuleDisplayModel{" +
              "editedFieldNames=" + editedFieldNames +
              ", editableFieldNames=" + editableFieldNames +
              ", templateTextFields=" + templateTextFields +
              '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        RuleDisplayModel that = (RuleDisplayModel) o;

        if (editedFieldNames != null ? !editedFieldNames.equals(that.editedFieldNames) : that.editedFieldNames != null) {
            return false;
        }
        if (editableFieldNames != null ? !editableFieldNames.equals(that.editableFieldNames) : that.editableFieldNames != null) {
            return false;
        }
        return !(templateTextFields != null ? !templateTextFields.equals(that.templateTextFields) : that.templateTextFields != null);

    }

    @Override
    public int hashCode() {
        int result = editedFieldNames != null ? editedFieldNames.hashCode() : 0;
        result = 31 * result + (editableFieldNames != null ? editableFieldNames.hashCode() : 0);
        result = 31 * result + (templateTextFields != null ? templateTextFields.hashCode() : 0);
        return result;
    }
}

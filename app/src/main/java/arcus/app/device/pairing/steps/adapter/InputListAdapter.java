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
package arcus.app.device.pairing.steps.adapter;

import android.content.Context;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import arcus.app.R;
import arcus.app.common.view.Version1EditText;
import arcus.app.device.pairing.steps.model.PairingStepInput;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class InputListAdapter extends ArrayAdapter<PairingStepInput> {

    private final List<PairingStepInput> hiddenInputs = new ArrayList<>();
    private final ListView view;

    public InputListAdapter(Context context, ListView boundListView, List<PairingStepInput> inputs) {
        super(context, 0);
        this.view = boundListView;

        for (PairingStepInput thisInput : inputs) {
            if (thisInput.isVisible()) {
                add(thisInput);
            } else {
                hiddenInputs.add(thisInput);
            }
        }

        boundListView.setAdapter(this);
    }

    private class ViewHolder {
        public Version1EditText textField;
    }

    @Nullable
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;
        PairingStepInput input = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.cell_pairing_step_text_input, parent, false);
            holder = new ViewHolder();
            holder.textField = (Version1EditText) convertView.findViewById(R.id.input_field);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.textField.setHint(input.getLabel());
        return convertView;
    }

    /**
     * Returns the data model associated with view, updated to represent any changes made within
     * the list itself.
     *
     * @return
     */
    public List<PairingStepInput> getPairingStepInputs() {

        // Update data model with changes to view
        rebindModelToView();

        List<PairingStepInput> inputs = new ArrayList<>();
        inputs.addAll(hiddenInputs);

        for (int index = 0; index < getCount(); index++) {
            inputs.add(getItem(index));
        }

        return inputs;
    }

    /**
     * Determines if the current set of input in valid; updates the views as needed to represent
     * error conditions.
     *
     * @param context
     * @return
     */
    public boolean validate(Context context) {

        // Update data model with changes to view
        rebindModelToView();

        for (int position = 0; position < getCount(); position++) {
            PairingStepInput model = getItem(position);

            if (!model.isValid()) {
                String errorString = context.getString(R.string.pairing_input_wrong_length, model.getRequiredLength());
                getView(position).textField.setError(errorString);

                return false;
            }
        }

        return true;
    }

    /**
     * Generates a map-of-maps representing the IPCD registration attributes from the data represented
     * by this adapter.
     *
     * @return
     */
    public HashMap<String, Object> getIpcdRegistrationAttributes() {

        HashMap<String,Object> ipcdAttributes = new HashMap<>();
        HashMap<String,Object> ipcdAttrs = new HashMap<>();
        ipcdAttributes.put("attrs", ipcdAttrs);

        for (PairingStepInput thisInput : getPairingStepInputs()) {
            ipcdAttrs.put(thisInput.getName(), thisInput.getValue());
        }

        return ipcdAttributes;
    }

    /**
     * Performs the view-to-model data binding required of a ListView that allows the user to modify
     * the underlying data model from within the view itself.
     *
     * Walks through each view element and sets the data model "value" to the view's value
     */
    public void rebindModelToView () {
        for (int position = 0; position < getCount(); position++) {
            ViewHolder cellView = getView(position);
            PairingStepInput model = getItem(position);

            model.setValue(cellView.textField.getText().toString());
        }
    }

    private ViewHolder getView(int position) {
        return (ViewHolder) view.getChildAt(position).getTag();
    }
}
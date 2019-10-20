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
package arcus.app.subsystems.people.adapter;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;

import arcus.app.R;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.popups.RelationshipPickerPopup;
import arcus.app.common.view.Version1TextView;
import arcus.app.subsystems.people.model.PersonRelationship;


import java.util.ArrayList;


public class PersonRelationshipAdapter extends BaseAdapter {
    private ArrayList<PersonRelationship> relationships = new ArrayList<>();
    private Context context;
    private boolean lightColorScheme = false;
    private SelectionChangedListener listener = null;

    public PersonRelationshipAdapter(Context context, ArrayList<PersonRelationship> relationships) {
        this.context = context;
        this.relationships = relationships;
    }

    private void setCheckedItem (String relationship) {
        clearSelections();
        for (int index = 0; index < getCount(); index++) {
            if(relationships.get(index).getName().equals(relationship)) {
                relationships.get(index).setSelected(true);
                break;
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return relationships.size();
    }

    @Override
    public Object getItem(int position) {
        return relationships.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.cell_checkable_chevron_item, parent, false);
        }

        ImageView checkbox = (ImageView) convertView.findViewById(R.id.checkbox);
        checkbox.setVisibility(View.VISIBLE);

        int checked = isLightColorScheme() ? R.drawable.circle_check_white_filled : R.drawable.circle_check_black_filled;
        int unchecked = isLightColorScheme() ? R.drawable.circle_hollow_white : R.drawable.circle_hollow_black;
        checkbox.setImageResource(relationships.get(position).isSelected() ? checked : unchecked);

        Version1TextView title = (Version1TextView) convertView.findViewById(R.id.title);
        title.setText(relationships.get(position).getName().toUpperCase());
        final EditText etOther = (EditText) convertView.findViewById(R.id.title_description);
        if(relationships.get(position).getName().toLowerCase().equals(context.getString(R.string.people_other).toLowerCase())) {
            etOther.setHint(R.string.please_describe);
            etOther.setVisibility(View.VISIBLE);
            etOther.setTag(position);
            etOther.setText(relationships.get(position).getDescription());
            etOther.addTextChangedListener(new TextWatcher() {

                @Override
                public void afterTextChanged(Editable editText) {
                    relationships.get((int)etOther.getTag()).setDescription(editText.toString());
                    fireSelectionChangedListener();
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start,
                                              int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start,
                                          int before, int count) {
                }
            });
        }
        else {
            etOther.setVisibility(View.GONE);
        }

        View subItem = convertView.findViewById(R.id.chevron_click_region);
        if(relationships.get(position).getChildren().size() == 0) {
            subItem.setVisibility(View.GONE);
        }
        else {
            subItem.setVisibility(View.VISIBLE);
            Version1TextView subtitle = (Version1TextView) convertView.findViewById(R.id.subtitle);
            boolean hasSelection = false;
            for(PersonRelationship rel : relationships.get(position).getChildren()) {
                if(rel.isSelected()) {
                    hasSelection = true;
                    if(rel.getName().toLowerCase().equals(context.getString(R.string.people_other).toLowerCase())) {
                        subtitle.setText(rel.getDescription());
                    }
                    else {
                        subtitle.setText(rel.getName());
                    }
                    break;
                }
            }
            if(!hasSelection) {
                PersonRelationship firstChild = relationships.get(position).getChildren().get(0);
                firstChild.setSelected(true);
                subtitle.setText(firstChild.getName());
            }
            subItem.setTag(position);
            subItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View view) {
                    RelationshipPickerPopup popup = RelationshipPickerPopup.newInstance(relationships.get((int)view.getTag()).getChildren());
                    popup.setCallback(new RelationshipPickerPopup.Callback() {

                        @Override
                        public void updatedValue(String selectionType, String selection) {
                            for(PersonRelationship relationship : relationships.get((int)view.getTag()).getChildren()) {
                                if(selectionType.toLowerCase().equals(context.getString(R.string.people_other).toLowerCase())) {
                                    if(relationship.getName().equals(selectionType)) {
                                        relationship.setSelected(true);
                                        relationship.setDescription(selection);
                                    }
                                    else {
                                        relationship.setSelected(false);
                                    }
                                }
                                else if(relationship.getName().equals(selection)) {
                                    relationship.setSelected(true);
                                }
                                else {
                                    relationship.setSelected(false);
                                }
                            }
                            if(listener != null) {
                                listener.onUpdateChildText(selectionType, selection);
                            }
                            fireSelectionChangedListener();
                            notifyDataSetChanged();
                        }
                    });
                    BackstackManager.getInstance().navigateToFloatingFragment(popup, popup.getClass().getCanonicalName(), true);
                }
            });
        }

        convertView.setTag(position);
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearSelections();
                relationships.get((int)v.getTag()).setSelected(true);
                fireSelectionChangedListener();
            }
        });

        return convertView;
    }

    public void clearSelections () {
        for (int index = 0; index < getCount(); index++) {
            ((PersonRelationship)getItem(index)).setSelected(false);
        }

        notifyDataSetInvalidated();
    }

    private void fireSelectionChangedListener() {
        String selection = null;
        String selectionType = null;
        for (int index = 0; index < getCount(); index++) {
            PersonRelationship rel = (PersonRelationship)getItem(index);
            if (rel.isSelected()) {
                selectionType = rel.getName();
                if(rel.getName().toLowerCase().equals(context.getString(R.string.people_other).toLowerCase())) {
                    selection = ((PersonRelationship)getItem(index)).getDescription();
                    break;
                }
                else if(rel.getChildren().size() == 0) {
                    selection = ((PersonRelationship)getItem(index)).getName();
                    break;
                }
                else {
                    for(int child = 0; child < rel.getChildren().size(); child++) {
                        PersonRelationship childRel = rel.getChildren().get(child);
                        if(childRel.isSelected()) {
                            selection = childRel.getName();
                            if(selection.toLowerCase().equals(context.getString(R.string.people_other).toLowerCase())) {
                                selection = childRel.getDescription();
                            }
                            break;
                        }
                    }
                }
            }
        }
        if (listener != null) {
            listener.onSelectionChanged(selectionType, selection);
        }
    }

    public void setLightColorScheme (boolean lightColorScheme) {
        this.lightColorScheme = lightColorScheme;
    }

    public boolean isLightColorScheme () { return lightColorScheme; }

    public interface SelectionChangedListener {
        void onSelectionChanged(String selectionType, String selectionName);
        void onUpdateChildText(String selectionType, String selectionName);
    }

    public void setListener(SelectionChangedListener listener) {
        this.listener = listener;
        fireSelectionChangedListener();
    }
}
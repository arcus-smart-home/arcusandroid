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
package arcus.app.subsystems.rules.views;

import android.content.Context;
import android.graphics.Color;
import androidx.annotation.NonNull;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import arcus.cornea.model.TemplateTextField;

import org.apache.commons.lang3.text.WordUtils;

import java.util.List;


public class RuleTemplateView extends TextView {

    private boolean enabled = true;

    public RuleTemplateView(Context context) {
        super(context);
    }

    public RuleTemplateView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public RuleTemplateView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setEnabled (boolean enabled) {
        this.enabled = enabled;
        this.setMovementMethod(enabled ? LinkMovementMethod.getInstance() : null);
    }

    public boolean isEnabled () { return this.enabled; }

    public void setRuleTemplate (@NonNull List<TemplateTextField> fields, OnTemplateFieldClickListener listener) {

        this.setText("");

        for (TemplateTextField thisField : fields) {

            // Capitalize proper nouns; lowercase all other editable fields.
            String displayText = thisField.getText();
            if (thisField.isEditable()) {
                displayText = thisField.isProperName() ? WordUtils.capitalizeFully(displayText) : displayText.toLowerCase();
            }

            SpannableStringBuilder span = SpannableStringBuilder.valueOf(displayText);

            if (thisField.isEditable()) {
                span.setSpan(new EditableSpan(thisField, listener), 0, span.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

            int textColor = thisField.isEditable() ? Color.BLACK : Color.GRAY;
            span.setSpan(new ForegroundColorSpan(textColor), 0, span.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            this.append(span);
        }

        this.setHighlightColor(Color.TRANSPARENT);

        if (enabled) {
            this.setMovementMethod(LinkMovementMethod.getInstance());
        }
    }

    private static class EditableSpan extends ClickableSpan {

        private final TemplateTextField textField;
        private final OnTemplateFieldClickListener listener;

        public EditableSpan (TemplateTextField field, OnTemplateFieldClickListener listener) {
            this.textField = field;
            this.listener = listener;
        }

        @Override
        public void onClick(View widget) {
            listener.onTemplateFieldClicked(textField);
        }
    }
}

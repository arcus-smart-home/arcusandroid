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
package arcus.app.common.view;

import android.content.Context;
import android.graphics.Paint;
import android.net.Uri;
import androidx.annotation.NonNull;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatTextView;
import arcus.app.common.utils.ActivityUtils;

public class ScleraLinkView extends AppCompatTextView {

    public ScleraLinkView(Context context) {
        this(context, null);
    }

    public ScleraLinkView(@NonNull Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ScleraLinkView(@NonNull Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        setPaintFlags(this.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        setClickable(true);
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        if (!isInEditMode()) { // Normal use case.
            super.setText(text, type);
        } else {
            int length;
            if (text == null) {
                length = 0;
            } else {
                length = text.length();
            }

            SpannableString spannableString = new SpannableString(text);
            spannableString.setSpan(new UnderlineSpan(), 0, length, 0);
            super.setText(spannableString, BufferType.SPANNABLE);
        }
    }

    public void setLinkTextAndTarget(String displayText, @NonNull String targetUrl) {
        setLinkTextAndTarget(displayText, Uri.parse(targetUrl));
    }

    public void setLinkTextAndTarget(String displayText, Uri targetUri) {
        setText(displayText);
        setOnClickListener(view -> ActivityUtils.launchUrl(targetUri));
    }
}

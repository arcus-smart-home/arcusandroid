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
import android.content.res.TypedArray;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.AttributeSet;

import arcus.app.R;
import arcus.app.common.utils.FontUtils;

import org.apache.commons.lang3.StringUtils;

public class ScleraTextView extends AppCompatTextView {

    public ScleraTextView(Context context) {
        super(context);
        setFont(-1);
    }

    public ScleraTextView(@NonNull Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public ScleraTextView(@NonNull Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    private void init(@NonNull Context ctx, AttributeSet attrs) {
        TypedArray a = ctx.obtainStyledAttributes(attrs, R.styleable.ScleraTextView);
        int customFont = a.getInteger(R.styleable.ScleraTextView_scleraFontStyle, -1);
        int htmlResId = a.getResourceId(R.styleable.ScleraTextView_html, 0);
        String htmlString = a.getString(R.styleable.ScleraTextView_html);

        setFont(customFont);
        setHtmlContent(htmlResId, htmlString);

        a.recycle();
    }

    private void setHtmlContent(int htmlResId, String htmlString) {

        if(htmlResId > 0) {
            htmlString = getResources().getString(htmlResId);
        }

        if (!StringUtils.isEmpty(htmlString)) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                setText(Html.fromHtml(htmlString, Html.FROM_HTML_MODE_LEGACY));
            } else {
                setText(Html.fromHtml(htmlString));
            }

            setLinksClickable(true);
            setMovementMethod(LinkMovementMethod.getInstance());
        }
    }

    private void setFont(int customFont) {
        if (isInEditMode()) {
            return;
        }

        switch (customFont) {
            case 0:
                setNormalTypeface();
                break;
            case 1:
                setLightTypeface();
                break;
            case 2:
                setLightItalicTypeface();
                break;
            case 3:
                setBoldTypeface();
                break;
            case 4:
                setItalicTypeface();
                break;
            case 5:
                setDemiTypeface();
                break;
            default:
                setNormalTypeface();

        }
        setText(this.getText(), BufferType.NORMAL);
    }

    public void setNormalTypeface() {
        setTypeface(FontUtils.getNormal());
    }

    public void setLightTypeface() {
        setTypeface(FontUtils.getLight());
    }

    public void setLightItalicTypeface() {
        setTypeface(FontUtils.getLightItalic());
    }

    public void setBoldTypeface() {
        setTypeface(FontUtils.getBold());
    }

    public void setItalicTypeface() {
        setTypeface(FontUtils.getItalic());
    }

    public void setDemiTypeface() {
        setTypeface(FontUtils.getDemi());
    }
}

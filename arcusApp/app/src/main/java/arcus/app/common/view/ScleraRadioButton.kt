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
package arcus.app.common.view

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.widget.AppCompatRadioButton
import android.util.AttributeSet
import arcus.app.R
import arcus.app.common.utils.FontUtils

class ScleraRadioButton : AppCompatRadioButton {
    constructor(context: Context) : super(context) {
        setUp(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        setUp(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        setUp(context, attrs)
    }

    private fun setUp(context: Context, attrs: AttributeSet? = null) {
        val a = context.obtainStyledAttributes(attrs, R.styleable.ScleraTextView)
        val customFont = a.getInteger(R.styleable.ScleraRadioButton_scleraFontStyle, 0)
        setFont(customFont)
        a.recycle()

        buttonDrawable = ContextCompat.getDrawable(context, R.drawable.sclera_checkbox_selector)
    }

    private fun setFont(customFont: Int) {
        if (isInEditMode) {
            return
        }

        val typeface = when (customFont) {
            0 -> FontUtils.getNormal()
            1 -> FontUtils.getLight()
            2 -> FontUtils.getLightItalic()
            3 -> FontUtils.getBold()
            4 -> FontUtils.getItalic()
            5 -> FontUtils.getDemi()
            else -> FontUtils.getNormal()
        }

        setTypeface(typeface)
    }
}
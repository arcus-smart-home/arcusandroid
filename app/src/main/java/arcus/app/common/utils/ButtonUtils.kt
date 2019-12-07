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
@file:JvmName("ButtonUtils")
package arcus.app.common.utils

import android.widget.Button
import androidx.core.content.ContextCompat
import arcus.app.R
import arcus.app.common.view.ButtonColor

/**
 * Sets a button to have a rounded white background with red text.
 */
fun Button.setColorSchemeWhiteRedText() {
    background = context.getDrawable(R.drawable.button_rounded_white)
    backgroundTintList = ContextCompat.getColorStateList(context, R.color.button_white_color)
    setTextColor(ContextCompat.getColorStateList(context, R.color.button_red_text_color))
}

/**
 * Sets a button to have a rounded white background with blue text.
 */
fun Button.setColorSchemeWhiteBlueText() {
    background = context.getDrawable(R.drawable.button_rounded_white)
    backgroundTintList = ContextCompat.getColorStateList(context, R.color.button_white_color)
    setTextColor(ContextCompat.getColorStateList(context, R.color.button_blue_text_color))
}

/**
 * Sets a button to have a rounded white background.
 */
fun Button.setColorSchemeWhiteOutline() {
    background = context.getDrawable(R.drawable.outline_rounded_button_style)
    backgroundTintList = ContextCompat.getColorStateList(context, R.color.button_white_outline_color)
    setTextColor(ContextCompat.getColorStateList(context, R.color.button_white_outline_text_color))
}

/**
 * Sets a button to have a rounded solid purple background.
 */
fun Button.setColorSchemePurple() {
    background = context.getDrawable(R.drawable.button_rounded_white)
    backgroundTintList = ContextCompat.getColorStateList(context, R.color.button_purple_color)
    setTextColor(ContextCompat.getColorStateList(context, R.color.button_purple_text_color))
}

/**
 * Sets a button to have a rounded purple background outline.
 */
fun Button.setColorSchemePurpleOutline() {
    background = context.getDrawable(R.drawable.outline_rounded_button_style)
    backgroundTintList = ContextCompat.getColorStateList(context, R.color.button_purple_outline_color)
    setTextColor(ContextCompat.getColorStateList(context, R.color.button_purple_outline_text_color))
}

infix fun Button.applyColorScheme(colorScheme: ButtonColor) = when (colorScheme) {
    ButtonColor.SOLID_PURPLE -> setColorSchemePurple()
    ButtonColor.OUTLINE_PURPLE -> setColorSchemePurpleOutline()
    ButtonColor.OUTLINE_WHITE -> setColorSchemeWhiteOutline()
    ButtonColor.SOLID_WHITE_BLUE_TEXT -> setColorSchemeWhiteBlueText()
    ButtonColor.SOLID_WHITE_RED_TEXT -> setColorSchemeWhiteRedText()
}

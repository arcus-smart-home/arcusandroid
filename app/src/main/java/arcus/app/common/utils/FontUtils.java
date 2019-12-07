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
package arcus.app.common.utils;

import android.content.Context;
import android.graphics.Typeface;
import androidx.annotation.NonNull;

import androidx.core.content.res.ResourcesCompat;
import arcus.app.ArcusApplication;
import arcus.app.R;


public class FontUtils {
    private static final Typeface NORMAL;
    private static final Typeface LIGHT;
    private static final Typeface DEMI;
    private static final Typeface BOLD;
    private static final Typeface ITALIC;
    static {
        Context context = ArcusApplication.getContext();
        NORMAL = ResourcesCompat.getFont(context, R.font.nunito);
        LIGHT = ResourcesCompat.getFont(context, R.font.nunito_light);
        DEMI = ResourcesCompat.getFont(context, R.font.nunito_demi);
        BOLD = ResourcesCompat.getFont(context, R.font.nunito_bold);
        ITALIC = ResourcesCompat.getFont(context, R.font.nunito_italic);
    }

    @Deprecated
    public static Typeface getNormal(@NonNull final Context context) {
        return getNormal();
    }

    @Deprecated
    public static Typeface getLight(@NonNull final Context context) {
        return getLight();
    }

    @Deprecated
    public static Typeface getLightItalic(@NonNull final Context context) {
        return getLightItalic();
    }

    @Deprecated
    public static Typeface getDemi(@NonNull final Context context) {
        return getDemi();
    }

    @Deprecated
    public static Typeface getBold(@NonNull final Context context) {
        return getBold();
    }

    //Avenir Next Medium = Lato Normal 400
    public static Typeface getNormal() {
        return NORMAL;
    }

    public static Typeface getLight() {
        return LIGHT;
    }

    //Avenir Next Italics = Lato Light 300 Italic
    public static Typeface getLightItalic() {
        return ITALIC;
    }


    public static Typeface getDemi() {
        return DEMI;
    }


    //Avenir Next Demi Bold = Lato Bold 700
    public static Typeface getBold() {
        return BOLD;
    }

    public static Typeface getItalic() {
        return ITALIC;
    }
}

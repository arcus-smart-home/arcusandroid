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

import android.graphics.Color;


public class I2ColorUtils {
    public static final float hsvValue = 0.75f;
    public static float [] endColor = {203f, 1f, hsvValue};
    public static float [] midColor = { 240f, 0.04f, 0.93f};
    public static float [] startColor = {34f, 1f, hsvValue};

    public static float[] getTemperatureColor3Point(float percentage) {
        float [] result = { 1f, 1f, 1f };

        int rgbGradientStartColor = 0;
        int rgbGradientEndColor = 0;

        float perc = percentage;
        //lower half of gradient
        if(percentage < 0.5f && percentage >= 0f) {
            rgbGradientStartColor = Color.HSVToColor(startColor);
            rgbGradientEndColor = Color.HSVToColor(midColor);
            perc = percentage*2;
        }
        //upper half of gradient
        else if(percentage >= 0.5f && percentage < 1f) {
            rgbGradientStartColor = Color.HSVToColor(midColor);
            rgbGradientEndColor = Color.HSVToColor(endColor);
            perc = (percentage-0.5f)*2;
        }

        int r1 = Color.red(rgbGradientStartColor);
        int g1 = Color.green(rgbGradientStartColor);
        int b1 = Color.blue(rgbGradientStartColor);

        int r2 = Color.red(rgbGradientEndColor);
        int g2 = Color.green(rgbGradientEndColor);
        int b2 = Color.blue(rgbGradientEndColor);

        int colorValue = Color.rgb((int)(r1*(1-perc)+r2*perc),
                (int)(g1*(1-perc)+g2*perc),
                (int)(b1*(1-perc)+b2*perc));

        Color.colorToHSV(colorValue, result);
        return result;
    }
}

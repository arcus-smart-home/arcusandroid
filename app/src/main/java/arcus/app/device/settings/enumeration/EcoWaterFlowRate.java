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
package arcus.app.device.settings.enumeration;

import arcus.app.R;

import android.content.Context;
import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;



public enum EcoWaterFlowRate {

    OFF(0.0, R.string.water_softener_ecowater_off_abstract,
            R.string.water_softener_ecowater_off_title,
            R.string.water_softener_ecowater_off_description),
    LOW(0.3, R.string.water_softener_ecowater_low_abstract,
            R.string.water_softener_ecowater_low_title,
            R.string.water_softener_ecowater_low_description),
    MEDIUM(2.0, R.string.water_softener_ecowater_medium_abstract,
            R.string.water_softener_ecowater_medium_title,
            R.string.water_softener_ecowater_medium_description),
    HIGH(8.0, R.string.water_softener_ecowater_high_abstract,
            R.string.water_softener_ecowater_high_title,
            R.string.water_softener_ecowater_high_description);

    private Double flowRate;
    private int abstractTextId;
    private int titleTextId;
    private int descritionTextId;

    EcoWaterFlowRate(Double flowRate, int abstractTextId, int titleTextId, int descritionTextId) {
        this.flowRate = flowRate;
        this.abstractTextId = abstractTextId;
        this.titleTextId = titleTextId;
        this.descritionTextId = descritionTextId;
    }

    public Double getFlowRate() {
        return flowRate;
    }

    public int getAbstractTextId() {
        return abstractTextId;
    }

    public int getTitleTextId() {
        return titleTextId;
    }

    public int getDescritionTextID() {
        return descritionTextId;
    }

    @NonNull
    public static EcoWaterFlowRate fromFlowRate(Double flowRateValue) {
        if (flowRateValue < 0.3) return OFF;
        if (flowRateValue < 2.0) return LOW;
        if (flowRateValue < 8.0) return MEDIUM;
        if (flowRateValue >= 8.0) return HIGH;

        return null;
    }

    @NonNull
    public static EcoWaterFlowRate fromtitleText (Context context, String titleValue) {
        if (titleValue.equals(context.getString(R.string.water_softener_ecowater_off_title))) return OFF;
        if (titleValue.equals(context.getString(R.string.water_softener_ecowater_low_title))) return LOW;
        if (titleValue.equals(context.getString(R.string.water_softener_ecowater_medium_title))) return MEDIUM;
        if (titleValue.equals(context.getString(R.string.water_softener_ecowater_high_title))) return HIGH;

        return null;
    }

    public List<String> getTitlesList (Context context) {
        List<String> waterFlowTitles = new ArrayList<>();
        waterFlowTitles.add(context.getString(OFF.getTitleTextId()));
        waterFlowTitles.add(context.getString(LOW.getTitleTextId()));
        waterFlowTitles.add(context.getString(MEDIUM.getTitleTextId()));
        waterFlowTitles.add(context.getString(HIGH.getTitleTextId()));

        return waterFlowTitles;
    }

    public List<String> getDescriptionsList (Context context) {
        List<String> waterFlowTitles = new ArrayList<>();
        waterFlowTitles.add(context.getString(OFF.getDescritionTextID()));
        waterFlowTitles.add(context.getString(LOW.getDescritionTextID()));
        waterFlowTitles.add(context.getString(MEDIUM.getDescritionTextID()));
        waterFlowTitles.add(context.getString(HIGH.getDescritionTextID()));

        return waterFlowTitles;
    }
}

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
package arcus.app.subsystems.alarm.promonitoring.models;

import arcus.app.common.view.Version1ButtonColor;

public class AlarmTrackerStateModel {

    private int selectedPizzaIconResId;
    private int unselectedPizzaIconResId;

    private String incidentStateName;               // Name of this incident state
    private String placeName;                       // Name of the place this incident applies to
    private boolean isPro;                          // User is pro subscriber; show pro badge
    private int tintColor;                          // Color of the icons
    private Version1ButtonColor buttonColor; // Color of the cancel button (for non-pro users)
    private Integer countdown;                      // When non-null, show a countdown in the "selected pizza" circle instead of an icon

    public int getSelectedPizzaIconResId() {
        return selectedPizzaIconResId;
    }

    public void setSelectedPizzaIconResId(int selectedPizzaIconResId) {
        this.selectedPizzaIconResId = selectedPizzaIconResId;
    }

    public int getUnselectedPizzaIconResId() {
        return unselectedPizzaIconResId;
    }

    public void setUnselectedPizzaIconResId(int unselectedPizzaIconResId) {
        this.unselectedPizzaIconResId = unselectedPizzaIconResId;
    }

    public String getIncidentStateName() {
        return incidentStateName;
    }

    public void setIncidentStateName(String incidentStateName) {
        this.incidentStateName = incidentStateName;
    }

    public String getPlaceName() {
        return placeName;
    }

    public void setPlaceName(String placeName) {
        this.placeName = placeName;
    }

    public boolean isPro() {
        return isPro;
    }

    public void setPro(boolean pro) {
        isPro = pro;
    }

    public int getTintColor() {
        return tintColor;
    }

    public void setTintColor(int tintColor) {
        this.tintColor = tintColor;
    }

    public Integer getCountdown() {
        return countdown;
    }

    public void setCountdown(Integer countdown) {
        this.countdown = countdown;
    }

    public Version1ButtonColor getButtonColor() {
        return buttonColor;
    }

    public void setButtonColor(Version1ButtonColor buttonColor) {
        this.buttonColor = buttonColor;
    }

    @Override
    public String toString() {
        return "AlarmTrackerStateModel{" +
                "selectedPizzaIconResId=" + selectedPizzaIconResId +
                ", unselectedPizzaIconResId=" + unselectedPizzaIconResId +
                ", incidentStateName='" + incidentStateName + '\'' +
                ", placeName='" + placeName + '\'' +
                ", isPro=" + isPro +
                ", tintColor=" + tintColor +
                ", buttonColor=" + buttonColor +
                ", countdown=" + countdown +
                '}';
    }
}

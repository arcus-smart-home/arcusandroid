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
package arcus.app.subsystems.care.cards;

import android.content.Context;

import arcus.cornea.subsystem.care.model.CareBehaviorModel;
import arcus.app.R;
import arcus.app.common.cards.SimpleDividerCard;
import arcus.app.subsystems.care.adapter.CardListenerInterface;


public class CareScheduledListItemCard extends SimpleDividerCard{

    private String behaviorID;
    private boolean mChevronShown = false;
    private CardListenerInterface listener;
    private boolean isEditMode = false;
    private boolean isScheduled = false;
    private int position;
    private boolean checked = false;
    public CareBehaviorModel careBehaviorModel;


    public CareScheduledListItemCard(Context context) {
        super(context);
    }

    @Override
    public int getLayout() {
        return R.layout.cell_twoline_scheduled_item;
    }

    public void showChevron() {
        mChevronShown = true;
    }

    public Boolean isChevronShown() {
        return mChevronShown;
    }

    public void setListener (CardListenerInterface listener) {
        this.listener = listener;
    }

    public void setEditMode (boolean isEditMode) {
        this.isEditMode = isEditMode;
    }

    public void setScheduled(Boolean scheduled){
        this.isScheduled=scheduled;
    }

    public void setPosition(int i){
        this.position=i;
    }

    public Boolean getScheduled(){
        return isScheduled;
    }

    public int getPosition(){
        return position;
    }

    public CardListenerInterface getListener(){
        return listener;
    }

    public boolean getEditMode(){
        return isEditMode;
    }

    public void setItemChecked(Boolean i){
        this.checked=i;
    }

    public Boolean getItemChecked(){
        return checked;
    }

    public void setCareBehaviorModel(CareBehaviorModel a){
        this.careBehaviorModel=a;
    }

    public String getBehaviorID() {
        return behaviorID;
    }

    public void setBehaviorID(String behaviorID) {
        this.behaviorID = behaviorID;
    }


}

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
package arcus.app.subsystems.care.fragment;

import android.app.Activity;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.dexafree.materialList.model.Card;
import com.dexafree.materialList.view.MaterialListView;
import arcus.cornea.subsystem.care.CareBehaviorListController;
import arcus.cornea.subsystem.care.model.CareBehaviorModel;
import arcus.cornea.utils.Listeners;
import com.iris.client.event.ListenerRegistration;
import arcus.app.R;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.error.ErrorManager;
import arcus.app.common.fragments.BaseFragment;
import arcus.app.common.image.ImageManager;
import arcus.app.common.image.Wallpaper;
import arcus.app.common.view.Version1TextView;
import arcus.app.subsystems.care.adapter.CardListenerInterface;
import arcus.app.subsystems.care.cards.CareScheduledListItemCard;

import java.util.ArrayList;
import java.util.List;


public class CareBehaviorsFragment extends BaseFragment implements CareBehaviorListController.Callback{
    private final List<Card> cards = new ArrayList<>();

    private boolean isEditMode = false;

    private Version1TextView careBehavDes;
    private Version1TextView careBehavHead;
    private MaterialListView careBehaviorsView;
    private Version1TextView ruleCount;
    private View careBehaviorItemsHeader;
    private ListenerRegistration listener;
    private List<CareBehaviorModel> templates;

    @NonNull
    public static CareBehaviorsFragment newInstance() {
        return new CareBehaviorsFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);


        careBehaviorItemsHeader = view.findViewById(R.id.care_behavior_mto);
        careBehaviorsView = (MaterialListView) view.findViewById(R.id.care_behavior_list);
        careBehavHead = (Version1TextView) view.findViewById(R.id.care_behavior_head);
        careBehavDes = (Version1TextView) view.findViewById(R.id.care_behavior_des);
        ruleCount = (Version1TextView) view.findViewById(R.id.rules_count);

        return view;
    }


    @Override public void onResume() {
        super.onResume();
        setHasOptionsMenu(true);
        getActivity().setTitle(getTitle());
        listener = CareBehaviorListController.instance().setCallback(this);

        ImageManager.with(getActivity()).setWallpaper(Wallpaper.ofCurrentPlace().darkened());
    }

    @Override public void onPause() {
        super.onPause();
        Listeners.clear(listener);
    }

    @Override
    public String getTitle() {
        return "Care Behaviors";
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.care_behaviors_fragment;
    }

    @Override public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        isEditMode = !isEditMode;
        updateNewCards();
        if(cards.isEmpty()){
            item.setTitle("");
        } else if(isEditMode){
            item.setTitle(getResources().getString(R.string.card_menu_done));
        } else{
            item.setTitle(getResources().getString(R.string.card_menu_edit));
        }
        return true;
    }

    @Nullable @Override public Integer getMenuId() {
        return (templates != null && !templates.isEmpty()) ? R.menu.menu_edit_done_toggle : null;
    }

    @Override public void onError(Throwable throwable) {
        hideProgressBar();
        ErrorManager.in(getActivity()).showGenericBecauseOf(throwable);
    }


    @Override
    public void showBehaviors(List<CareBehaviorModel> templates) {
        this.templates=templates;
        updateNewCards();

        Activity activity = getActivity();
        if (activity != null) {
            activity.invalidateOptionsMenu();
        }
    }

    protected void updateList(){
        cards.clear();
        int i = 0;
        if(!templates.isEmpty()) {
            for (CareBehaviorModel careBehaviorModel : templates){
                CareScheduledListItemCard careBehavior = new CareScheduledListItemCard(getActivity());
                if(i!=0)
                {
                    careBehavior.showDivider();
                }
                careBehavior.setBehaviorID(careBehaviorModel.getBehaviorID());
                careBehavior.setScheduled(careBehaviorModel.hasScheduleEvents());
                careBehavior.setListener(cardListenerInterface());
                careBehavior.setItemChecked(careBehaviorModel.isEnabled());
                careBehavior.setDescription(careBehaviorModel.getDescription());
                careBehavior.setTitle(careBehaviorModel.getName());
                careBehavior.setCareBehaviorModel(careBehaviorModel);
                careBehavior.setEditMode(isEditMode);
                cards.add(careBehavior);
                i++;
            }
        }
        setLayout();
    }

    protected void updateNewCards() {
        careBehaviorsView.clear();
        updateList();
        careBehaviorsView.addAll(cards);
    }

    public void setLayout() {

        int size = cards.size();
        if(size != 0){
            careBehavDes.setText("Turn on the behavior by selecting the checkmark.\nUncheck to deactivate the behavior.");
            careBehaviorItemsHeader.setVisibility(View.VISIBLE);
            ruleCount.setText(String.format("%s", size));
        }
        else{
            careBehavDes.setText("On the dashboard, tap the + sign,\nthen Care Behaviors to get Started.");
            careBehaviorItemsHeader.setVisibility(View.GONE);
            getActivity().invalidateOptionsMenu();
        }


    }

    public CardListenerInterface cardListenerInterface(){
        return new CardListenerInterface(){
            @Override public void onCheckboxRegionClicked(CareScheduledListItemCard card, boolean isChecked) {
                if(isEditMode){
                    CareBehaviorListController.instance().deleteBehavior(card.getBehaviorID());
                }
                else {
                    CareBehaviorListController.instance().enableBehavior(card.getBehaviorID(), !isChecked);
                }
                updateNewCards();
            }

            @Override public void onChevronRegionClicked(CareScheduledListItemCard card) {
                BackstackManager.getInstance().navigateToFragment(
                      CareAddEditBehaviorFragment.newInstance(
                            card.getBehaviorID(),
                            card.getDescription(),
                            true
                      ),
                      true
                );
            }
        };

    }

}

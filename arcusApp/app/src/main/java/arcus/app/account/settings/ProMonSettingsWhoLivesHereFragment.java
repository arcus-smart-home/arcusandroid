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
package arcus.app.account.settings;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.common.collect.Lists;
import arcus.cornea.provider.ProMonitoringSettingsProvider;
import arcus.cornea.utils.Listeners;
import com.iris.client.ClientEvent;
import com.iris.client.event.Listener;
import com.iris.client.model.ProMonitoringSettingsModel;
import arcus.app.R;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.error.ErrorManager;
import arcus.app.common.fragments.BaseFragment;
import arcus.app.common.popups.CheckboxListPopup;
import arcus.app.common.utils.StringUtils;
import arcus.app.common.view.Version1TextView;

import java.util.ArrayList;
import java.util.List;



public class ProMonSettingsWhoLivesHereFragment extends BaseFragment {
    public static final String PLACE_ID = "PLACE_ID";
    private String viewingPlaceID, numberOfAdults, numberOfChildren, numberOfPets;
    private Version1TextView numberOfAdultsText, numberOfChildrenText, numberOfPetsText;
    private View whoLivesHereAdultsLayout, whoLivesHereChildrenLayout, whoLivesHerePetsLayout;
    private ProMonitoringSettingsModel proMonSettingsModel;

    public static ProMonSettingsWhoLivesHereFragment newInstance(@NonNull String placeID) {
        ProMonSettingsWhoLivesHereFragment fragment = new ProMonSettingsWhoLivesHereFragment();
        Bundle args = new Bundle(1);
        args.putString(PLACE_ID, placeID);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = super.onCreateView(inflater, container, savedInstanceState);
        viewingPlaceID = getArguments().getString(PLACE_ID);
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        View rootView = getView();

        ProMonitoringSettingsProvider.getInstance().getProMonSettings(viewingPlaceID).onSuccess(Listeners.runOnUiThread(new Listener<ProMonitoringSettingsModel>() {
            @Override
            public void onEvent(ProMonitoringSettingsModel model) {
                proMonSettingsModel = model;

                numberOfAdults = String.valueOf(proMonSettingsModel.getAdults());
                if (!StringUtils.isEmpty(numberOfAdults)) {

                    numberOfAdultsText.setText(setReturnedItemCountText(Integer.valueOf(numberOfAdults)));
                }
                else {
                    numberOfAdultsText.setText("1");
                }

                numberOfChildren = String.valueOf(proMonSettingsModel.getChildren());
                if (!StringUtils.isEmpty(numberOfChildren)) {
                    numberOfChildrenText.setText(setReturnedItemCountText(Integer.valueOf(numberOfChildren)));
                }
                else {
                    numberOfChildrenText.setText("0");
                }

                numberOfPets = String.valueOf(proMonSettingsModel.getPets());
                if (!StringUtils.isEmpty(numberOfPets)) {
                    numberOfPetsText.setText(setReturnedItemCountText(Integer.valueOf(numberOfPets)));
                }
                else {
                    numberOfPetsText.setText("0");
                }
            }
        }));

        numberOfAdultsText = (Version1TextView) rootView.findViewById(R.id.settings_promon_who_lives_here_number_of_adults);
        numberOfChildrenText = (Version1TextView) rootView.findViewById(R.id.settings_promon_who_lives_here_number_of_children);
        numberOfPetsText = (Version1TextView) rootView.findViewById(R.id.settings_promon_who_lives_here_number_of_pets);

        whoLivesHereAdultsLayout = rootView.findViewById(R.id.settings_promon_who_lives_here_adults_layout);
        whoLivesHereAdultsLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String itemSelectedType = getString(R.string.settings_promon_who_lives_here_adults_popup_item_type);

                String itemText = numberOfAdultsText.getText().toString();
                String stringItemCount = itemText.substring(0, 1);
                int intItemCount = Integer.parseInt(stringItemCount);

                CheckboxListPopup pop = CheckboxListPopup.newInstance(
                        getString(R.string.settings_promon_who_lives_here_adults_text),
                        Lists.newArrayList(getPopupItemslist(itemSelectedType, 1, 5)),
                        Lists.newArrayList(setItemLabel(itemSelectedType, intItemCount)),
                        true,
                        true);
                pop.setCallback(new CheckboxListPopup.Callback() {
                    @Override
                    public void onItemsSelected(List<String> selections) {
                        onItemSelectedCallback(selections, itemSelectedType);
                    }
                });
                BackstackManager.getInstance().navigateToFloatingFragment(pop, pop.getClass().getSimpleName(), true);
            }
        });

        whoLivesHereChildrenLayout = rootView.findViewById(R.id.settings_promon_who_lives_here_children_layout);
        whoLivesHereChildrenLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String itemSelectedType = getString(R.string.settings_promon_who_lives_here_children_popup_item_type);

                String itemText = numberOfChildrenText.getText().toString();
                String stringItemCount = itemText.substring(0, 1);
                int intItemCount = Integer.parseInt(stringItemCount);

                CheckboxListPopup pop = CheckboxListPopup.newInstance(
                        getString(R.string.settings_promon_who_lives_here_children_text),
                        Lists.newArrayList(getPopupItemslist(itemSelectedType, 0, 5)),
                        Lists.newArrayList(setItemLabel(itemSelectedType, intItemCount)),
                        true,
                        true);
                pop.setCallback(new CheckboxListPopup.Callback() {
                    @Override
                    public void onItemsSelected(List<String> selections) {
                        onItemSelectedCallback(selections, itemSelectedType);
                    }
                });
                BackstackManager.getInstance().navigateToFloatingFragment(pop, pop.getClass().getSimpleName(), true);
            }
        });

        whoLivesHerePetsLayout = rootView.findViewById(R.id.settings_promon_who_lives_here_pets_layout);
        whoLivesHerePetsLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String itemSelectedType = getString(R.string.settings_promon_who_lives_here_pets_popup_item_type);

                String itemText = numberOfPetsText.getText().toString();
                String stringItemCount = itemText.substring(0, 1);
                int intItemCount = Integer.parseInt(stringItemCount);

                CheckboxListPopup pop = CheckboxListPopup.newInstance(
                        getString(R.string.settings_promon_who_lives_here_pets_text),
                        Lists.newArrayList(getPopupItemslist(itemSelectedType, 0, 5)),
                        Lists.newArrayList(setItemLabel(itemSelectedType, intItemCount)),
                        true,
                        true);
                pop.setCallback(new CheckboxListPopup.Callback() {
                    @Override
                    public void onItemsSelected(List<String> selections) {
                        onItemSelectedCallback(selections, itemSelectedType);
                    }
                });
                BackstackManager.getInstance().navigateToFloatingFragment(pop, pop.getClass().getSimpleName(), true);
            }
        });

        setTitle();
    }


    @Nullable
    @Override
    public String getTitle() {
        return getString(R.string.settings_promon_who_lives_here_title_text);
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_promon_settings_who_lives_here;
    }

    public void onItemSelectedCallback(List<String> selections, String itemType) {
        String selectedItemName = selections.get(0);
        String stringNumberOfItems = selectedItemName.substring(0, 1);
        int intNumberOfItems = Integer.parseInt(stringNumberOfItems);

        if(itemType.equals(getString(R.string.settings_promon_who_lives_here_adults_popup_item_type))) {
            numberOfAdultsText.setText(setReturnedItemCountText(intNumberOfItems));
            proMonSettingsModel.setAdults(intNumberOfItems);
        }
        else if(itemType.equals(getString(R.string.settings_promon_who_lives_here_children_popup_item_type))) {
            numberOfChildrenText.setText(setReturnedItemCountText(intNumberOfItems));
            proMonSettingsModel.setChildren(intNumberOfItems);
        }
        else if(itemType.equals(getString(R.string.settings_promon_who_lives_here_pets_popup_item_type))) {
            numberOfPetsText.setText(setReturnedItemCountText(intNumberOfItems));
            proMonSettingsModel.setPets(intNumberOfItems);
        }

        proMonSettingsModel.commit().onFailure(new Listener<Throwable>() {
            @Override
            public void onEvent(Throwable event) {
                ErrorManager.in(getActivity()).showGenericBecauseOf(event);
            }
        }
        ).onSuccess(new Listener<ClientEvent>() {
            @Override
            public void onEvent(ClientEvent event) {
                BackstackManager.getInstance().navigateBack();
            }
        });
    }

    private List<String> getPopupItemslist(String itemType, int itemCountStart, int itemCountEnd) {
        List<String> itemsList = new ArrayList<>();

        for (int i = itemCountStart; i <= itemCountEnd; i++) {
            itemsList.add(setItemLabel(itemType, i));
        }
        return itemsList;
    }

    private String setItemLabel(String itemType, int itemIndex) {
        String itemLabel;
        String childrenItemType =  getString(R.string.settings_promon_who_lives_here_children_popup_item_type);

        if (itemType.equals(childrenItemType)){
            if (itemIndex == 5) {
                itemLabel = itemIndex + "+ " + itemType;
            }
            else {
                itemLabel = itemIndex + " " + itemType;
            }
        } else {
            if (itemIndex == 1) {
                itemLabel = itemIndex + " " + itemType;
            }
            else if (itemIndex == 5) {
                itemLabel = itemIndex + "+ " + itemType + "S";
            }
            else {
                itemLabel = itemIndex + " " + itemType + "S";
            }
        }
        return itemLabel;
    }

    private String setReturnedItemCountText(int itemCount) {
        return itemCount >= 5 ? "5+" : String.valueOf(itemCount);
    }


}

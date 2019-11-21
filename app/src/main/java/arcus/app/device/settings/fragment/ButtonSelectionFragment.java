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
package arcus.app.device.settings.fragment;

import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import arcus.app.R;
import arcus.app.common.adapters.EnumAdapter;
import arcus.app.common.sequence.SequencedFragment;
import arcus.app.common.view.Version1Button;
import arcus.app.common.view.Version1ButtonColor;
import arcus.app.common.view.Version1TextView;
import arcus.app.device.buttons.controller.ButtonActionSequenceController;
import arcus.app.device.buttons.model.Button;
import arcus.app.device.buttons.model.ButtonSequenceVariant;
import arcus.app.device.buttons.model.FobButton;
import arcus.app.device.settings.fragment.contract.ButtonSelectionContract;
import arcus.app.device.settings.fragment.presenter.ButtonSelectionPresenter;



public class ButtonSelectionFragment extends SequencedFragment<ButtonActionSequenceController>
        implements ButtonSelectionContract.ButtonSelectionView{

    private static final String DEVICE_BUTTONS = "BUTTONS";
    private static final String SCREEN_VARIANT = "SCREEN_VARIANT";
    private static final String DEVICE_ADDRESS = "DEVICE_ADDRESS";


    private Version1TextView listTitle;
    private ListView buttonsList;
    private Version1Button nextButton;
    private Button[] buttons;

    @Nullable
    private EnumAdapter buttonAdapter;
    private ButtonSelectionPresenter presenter = new ButtonSelectionPresenter();

    @NonNull
    public static ButtonSelectionFragment newInstance(ButtonSequenceVariant screenVariant,
                                                      Button[] buttons, String deviceAddress) {
        ButtonSelectionFragment instance = new ButtonSelectionFragment();
        Bundle arguments = new Bundle();

        arguments.putSerializable(DEVICE_BUTTONS, buttons);
        arguments.putSerializable(SCREEN_VARIANT, screenVariant);
        arguments.putSerializable(DEVICE_ADDRESS, deviceAddress);

        instance.setArguments(arguments);
        return instance;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        nextButton = (Version1Button) view.findViewById(R.id.next_button);
        buttonsList = (ListView) view.findViewById(R.id.list);
        listTitle = (Version1TextView) view.findViewById(R.id.list_title);

        buttonsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Button selectedButton = (Button) buttonAdapter.getEnumAt(position);
                getController().setSelectedButton(selectedButton);
            }
        });

        buttons = (Button[]) getArguments().getSerializable(DEVICE_BUTTONS);
        buttonAdapter = new EnumAdapter<>(getActivity(), buttons);

        buttonsList.setAdapter(buttonAdapter);

        return view;
    }


    @Override
    public void onResume() {
        super.onResume();

        buttonAdapter.clear();

        refreshButtons();

        setButtonViews();
    }

    @NonNull
    @Override
    public String getTitle() {
        return "";
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_checkable_list;
    }


    @Override
    public void onPending(@Nullable Integer progressPercent) {

    }

    @Override
    public void onError(@NonNull Throwable throwable) {

    }

    @Override
    public void updateView(@NonNull FobButton[] buttons) {

        buttonAdapter = new EnumAdapter(getContext(), buttons);
        buttonsList.setAdapter(buttonAdapter);

        setButtonViews();
    }

    public void refreshButtons () {
        presenter.startPresenting(this);
        presenter.getButtonsActions(getArguments().getSerializable(DEVICE_ADDRESS).toString(), buttons);
    }

    private void setButtonViews() {
        ButtonSequenceVariant variant = (ButtonSequenceVariant) getArguments().getSerializable(SCREEN_VARIANT);

        buttonAdapter.setShowChevrons(true);
        if (ButtonSequenceVariant.DEVICE_PAIRING.equals(variant)) {
            nextButton.setVisibility(View.VISIBLE);
            nextButton.setColorScheme(Version1ButtonColor.BLACK);
            nextButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    endSequence(true);
                }
            });
            buttonAdapter.setLightColorScheme(false);
        } else if (ButtonSequenceVariant.SETTINGS.equals(variant)) {
            buttonAdapter.setLightColorScheme(true);
            listTitle.setTextColor(Color.WHITE);
            nextButton.setVisibility(View.GONE);
        }
    }


}

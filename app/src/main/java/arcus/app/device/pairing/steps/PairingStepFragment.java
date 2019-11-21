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
package arcus.app.device.pairing.steps;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.google.common.collect.ImmutableSet;
import arcus.app.R;
import arcus.app.common.error.ErrorManager;
import arcus.app.common.image.ImageManager;
import arcus.app.common.image.Wallpaper;
import arcus.app.common.view.Version1Button;
import arcus.app.common.view.Version1TextView;
import arcus.app.device.pairing.steps.adapter.InputListAdapter;
import arcus.app.device.pairing.steps.controller.PairingStepFragmentController;
import arcus.app.device.pairing.steps.model.PairingStep;
import arcus.app.device.pairing.steps.model.PairingStepInput;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Set;

/**
 * Displays a single, product catalog-specified pairing step consisting of a pairing illustration,
 * descriptive text and step number icon.
 */
public class PairingStepFragment extends AbstractPairingStepFragment implements PairingStepFragmentController.Callbacks {

    private final static String PRODUCT_ADDRESS = "PRODUCT_ADDRESS";
    private final static String LAST_FIRST = "LAST_FIRST";

    private ImageView stepIllustration;
    private ImageView stepNumberIcon;
    private Version1TextView stepInstruction;
    private Version1TextView stepSubInstruction;
    private Version1Button nextButton;
    private RelativeLayout tutorialVideoClickableRegion;
    private ListView inputFields;
    private View paddingElement;
    private InputListAdapter inputListAdapter;

    private final Set<String> honeywellDevices = ImmutableSet.of("d9685c", "1dbb3f", "973d58");

    public static PairingStepFragment newInstance (String productAddress, boolean displayLastStepFirst) {

        PairingStepFragment instance = new PairingStepFragment();

        Bundle arguments = new Bundle();
        arguments.putString(PRODUCT_ADDRESS, productAddress);
        arguments.putBoolean(LAST_FIRST, displayLastStepFirst);
        instance.setArguments(arguments);

        return instance;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        stepIllustration = (ImageView) view.findViewById(R.id.pairing_illustration);
        stepNumberIcon = (ImageView) view.findViewById(R.id.step_number);
        stepInstruction = (Version1TextView) view.findViewById(R.id.step_instruction);
        stepSubInstruction = (Version1TextView) view.findViewById(R.id.step_sub_instruction);
        nextButton = (Version1Button) view.findViewById(R.id.next_button);
        tutorialVideoClickableRegion = (RelativeLayout) view.findViewById(R.id.video_tab);
        inputFields = (ListView) view.findViewById(R.id.pairing_step_input_fields);
        paddingElement = (View) view.findViewById(R.id.padding_element);

        return view;
    }

    @Override
    public void onResume () {
        super.onResume();

        String productAddress = getArguments().getString(PRODUCT_ADDRESS);
        boolean displayLastStepFirst = getArguments().getBoolean(LAST_FIRST);

        if (!StringUtils.isEmpty(productAddress)) {
            PairingStepFragmentController.instance().setListener(this);
            PairingStepFragmentController.instance().showInitialPairingStep(getActivity(), productAddress, displayLastStepFirst);
        }
    }

    @Override
    public void onPause () {
        super.onPause();
        PairingStepFragmentController.instance().removeListener();
    }

    @Nullable
    @Override
    public String getTitle() {
        return "";
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_device_pairing_step;
    }

    @Override
    public void onShowPairingStep(final PairingStep step) {
        hideProgressBar();

        getActivity().setTitle(step.getDeviceName());
        getActivity().invalidateOptionsMenu();

        ImageManager.with(getActivity()).setWallpaper(Wallpaper.ofCurrentPlace().lightend());

        // 0 Based index. 2nd step should show this image.
        if (step.getStepNumber() == 1 && honeywellDevices.contains(String.valueOf(step.getProductId()))) {
            inputFields.setVisibility(View.GONE);
            paddingElement.setVisibility(View.GONE);
            ImageManager.with(getActivity())
                  .putPairingStepImage(step.getProductId(), String.valueOf(step.getStepNumber() + 1))
                  .into(stepIllustration)
                  .execute();
        }
        // TODO : Image should be determined by pairing step img field and not hardcoded checks for Genie Controller
        // Step requires user input; hide pairing illustration and show inputs
        else if (step.requiresInput() && !step.getProductId().equals("aeda43") && !step.getProductId().equals("162918")) {
            stepIllustration.setVisibility(View.GONE);
            paddingElement.setVisibility(View.VISIBLE);

            inputFields.setVisibility(View.VISIBLE);
            inputListAdapter = new InputListAdapter(getActivity(), inputFields, step.getInputs());
        }

        // Step requires user input but should show illustration such as the genie
        else if (step.requiresInput() && !step.getProductId().equals("162918")) {
            stepIllustration.setVisibility(View.VISIBLE);
            paddingElement.setVisibility(View.GONE);

            inputFields.setVisibility(View.VISIBLE);
            inputListAdapter = new InputListAdapter(getActivity(), inputFields, step.getInputs());

            ImageManager.with(getActivity())
                    .putPairingStepImage(step.getProductId(), String.valueOf(step.getStepNumber() + 1))
                    .into(stepIllustration)
                    .execute();
        }

        // Step requires no input; show illustration and hide inputs
        else {
            stepIllustration.setVisibility(View.VISIBLE);
            inputFields.setVisibility(View.GONE);
            paddingElement.setVisibility(View.GONE);
            stepSubInstruction.setVisibility(View.GONE);

            ImageManager.with(getActivity())
                    .putPairingStepImage(step.getProductId(), String.valueOf(step.getStepNumber() + 1))
                    .into(stepIllustration)
                    .execute();
        }

        stepNumberIcon.setImageResource(getStepNumberDrawableResId(step.getStepNumber()));
        stepInstruction.setText(step.getText());
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Step requires no user input (all zwave/zigbee steps)
                if (!step.requiresInput() || step.getProductId().equals("162918")) {
                    goNext(honeywellDevices.contains(String.valueOf(step.getProductId())));
                }

                // Step does require input and user-provided input is valid (some IPCD steps)
                else if (step.requiresInput() && inputListAdapter.validate(getActivity())) {
                    HashMap<String, Object> ipcdRegistrationAttributes = inputListAdapter.getIpcdRegistrationAttributes();
                    String requiredFieldName = getRequiredFieldName();

                    goNext(ipcdRegistrationAttributes, requiredFieldName);
                }

                // Step requires input, but current input is not valid
                else {
                    // Nothing to do
                }
            }
        });

        // Display and wire-up tutorial video when available
        tutorialVideoClickableRegion.setVisibility(step.hasTutorialVideo() ? View.VISIBLE : View.GONE);
        if (step.hasTutorialVideo()) {
            tutorialVideoClickableRegion.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(Intent.ACTION_VIEW, step.getTutorialVideoUri()));
                }
            });
        }

    }

    @Override
    public void onLoading() {
        showProgressBar();
    }

    @Override
    public void onError(Throwable cause) {
        hideProgressBar();
        ErrorManager.in(getActivity()).showGenericBecauseOf(cause);
    }

    private String getRequiredFieldName () {
        for (PairingStepInput thisInput : inputListAdapter.getPairingStepInputs()) {
            if (thisInput.getRequiredLength() > 0) {
                return thisInput.getLabel();
            }
        }

        return null;
    }
}

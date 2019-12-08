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
package arcus.app.pairing.hub.original;

import android.app.Activity;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputLayout;
import com.iris.client.model.DeviceModel;
import com.iris.client.model.HubModel;
import arcus.app.R;
import arcus.app.activities.DashboardActivity;
import arcus.app.common.controller.UpdateContextTextWatcher;
import arcus.app.common.fragments.BaseFragment;
import arcus.app.common.fragments.IShowedFragment;
import arcus.app.common.image.ImageManager;
import arcus.app.common.image.picasso.transformation.AlphaPreset;
import arcus.app.common.image.picasso.transformation.BlackWhiteInvertTransformation;
import arcus.app.common.image.picasso.transformation.CropCircleTransformation;
import arcus.app.common.image.picasso.transformation.Invert;
import arcus.app.common.models.RegistrationContext;
import arcus.app.common.utils.GlobalSetting;
import arcus.app.pairing.hub.original.model.ArcusStep;
import arcus.cornea.provider.HubModelProvider;

import org.apache.commons.lang3.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ArcusStepFragment extends BaseFragment implements View.OnClickListener,IShowedFragment {

    private ImageView stepImage;
    private ImageView profileImage;
    public ImageView indexIcon;
    private TextView stepText;
    private TextView stepSubText;
    private EditText deviceName;
    private TextInputLayout deviceNameContainer;
    private FrameLayout photoLayout;
    private boolean mIsLastStep;
    @Nullable
    private ArcusStep mStep;
    private DashboardActivity dashboardActivity;
    private boolean isMatch = true;
    boolean formOk;
    private boolean isHub;

    private ButtonCallback buttonCallback;

    public interface ButtonCallback {
        void enableButton();
        void disableButton();
        void searchForHub();
    }

    public void setCallback(ButtonCallback callback) {
        buttonCallback = callback;
    }

    @NonNull
    public static ArcusStepFragment newInstance(ArcusStep step, boolean isLastStep, boolean isHub){
        ArcusStepFragment fragment = new ArcusStepFragment();
        Bundle b = new Bundle();
        b.putParcelable(GlobalSetting.STEP_STRING, step);
        b.putBoolean(GlobalSetting.IS_LAST_STEP, isLastStep);
        b.putBoolean(GlobalSetting.IS_HUB_OR_DEVICE, isHub);
        fragment.setArguments(b);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dashboardActivity = (DashboardActivity) getActivity();
        final Bundle arguments = getArguments();
        if(arguments !=null){
            mIsLastStep = arguments.getBoolean(GlobalSetting.IS_LAST_STEP);
            mStep = arguments.getParcelable(GlobalSetting.STEP_STRING);
            isHub = arguments.getBoolean(GlobalSetting.IS_HUB_OR_DEVICE);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        stepImage = view.findViewById(R.id.step_view_image);
        photoLayout = view.findViewById(R.id.sclera_photo_layout);
        photoLayout.setOnClickListener(this);
        photoLayout.setVisibility(View.GONE);

        //  place user selected image into this
        profileImage = view.findViewById(R.id.fragment_account_camera);
        indexIcon = view.findViewById(R.id.pager_index);

        stepText = view.findViewById(R.id.step_view_text);

        stepSubText = view.findViewById(R.id.step_view_sub_text);

        deviceName = view.findViewById(R.id.step_view_device_name);
        deviceNameContainer = view.findViewById(R.id.step_view_device_name_container);

        deviceName.addTextChangedListener(new UpdateContextTextWatcher<RegistrationContext>(registrationContext) {
            @Override
            protected void updateContext(@NonNull RegistrationContext context, @NonNull Editable s) {
                if (dashboardActivity.isHub()) {
                    if (mStep.getCurrentStep() == 6) {
                        if (s.toString().length() >= 8) {
                            context.setHubID(s.toString().toUpperCase());
                        }

                    }
                }
            }
        });

        init();

        return view;
    }

    private void init(){
        if(mStep!=null) {
            try {
                if (! arcus.app.common.utils.StringUtils.isEmpty(mStep.getStepImageResource())) {
                    stepImage.setImageResource(mStep.getStepImageResource());
                } else {
                    ImageManager.with(getActivity())
                            // Pairing steps are numbered from 1
                            .putPairingStepImage(mStep.getProductId(), String.valueOf(mStep.getCurrentStep()))
                            .withError(R.drawable.image_placeholder)
                            .withPlaceholder(R.drawable.image_placeholder)
                            .into(stepImage)
                            .execute();
                }
            }
            catch (Exception ex) {
                logger.trace("Could not set step image. Reason:", ex);
            }
            stepText.setText(mStep.getStepText());

            if(isHub) {
                loadHubData();
            }
            else {
                loadDeviceData();
            }
            getActivity().setTitle(getTitle());
        }
    }

    private void loadHubData(){
        stepSubText.setVisibility(View.GONE);
        if(mIsLastStep) {
            deviceNameContainer.setVisibility(View.VISIBLE);
            deviceName.setImeOptions(EditorInfo.IME_ACTION_DONE);
            if(mStep.getCurrentStep() ==6){
                deviceName.setFilters(new InputFilter[] {new InputFilter.AllCaps()});
                deviceNameContainer.setHint(getString(R.string.hub_id_hint_title));

                deviceName.addTextChangedListener(new TextWatcher() {
                    int len =0;
                    @Override
                    public void beforeTextChanged(@NonNull CharSequence s, int start, int count, int after) {
                        len = s.length();
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                    }

                    @Override
                    public void afterTextChanged(@NonNull Editable s) {
                        if(s.length() == 3 && len < s.length()){
                            s.append("-");
                        }

                        Pattern mPattern = Pattern.compile("^[a-zA-Z]{3,}\\-\\d{4,}");

                        Matcher matcher = mPattern.matcher(s.toString());
                        if((!matcher.find() || s.length()>8))
                        {
                            if (s.equals("") || s.length() == 0) {
                                deviceNameContainer.setError(getString(R.string.hub_id_missing));
                            }
                            else if (s.length() >= 8) {
                                deviceNameContainer.setError(getString(R.string.hub_id_wrong_format));
                            } else {
                                deviceNameContainer.setError(null);
                            }
                            isMatch = false;

                            if(buttonCallback != null){
                                buttonCallback.disableButton();
                            }
                        } else{
                            isMatch = true;

                            if(buttonCallback != null){
                                buttonCallback.enableButton();
                            }
                        }
                    }
                });
                // Listen for "Enter" on the keyboard and search for the hub ID
                deviceName.setOnEditorActionListener((v, actionId, event) -> {
                    if(actionId == EditorInfo.IME_ACTION_DONE && isMatch) {
                        buttonCallback.searchForHub();
                        InputMethodManager imm = (InputMethodManager)deviceName.getContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

                    }
                    return false;
                });

                stepImage.setVisibility(View.VISIBLE);
                photoLayout.setVisibility(View.GONE);
            } else {
                indexIcon.setVisibility(View.GONE);
                deviceNameContainer.setHint(getString(R.string.hub_name_hint));

                HubModel hubModel = HubModelProvider.instance().getHubModel();
                if (hubModel != null && !StringUtils.isEmpty(hubModel.getName())) {
                    deviceName.setText(hubModel.getName());
                }

                photoLayout.setVisibility(View.VISIBLE);
                stepImage.setVisibility(View.GONE);

                ImageManager.with(getActivity())
                        .putDrawableResource(R.drawable.device_list_placeholder)
                        .withTransform(new CropCircleTransformation())
                        .fit()
                        .into(profileImage)
                        .execute();
            }
        }
    }

    private void loadDeviceData(){
        if(mIsLastStep){
            deviceNameContainer.setVisibility(View.VISIBLE);
            deviceNameContainer.setHint(getString(R.string.device_name_hint));
            photoLayout.setVisibility(View.VISIBLE);
            stepImage.setVisibility(View.GONE);

            applyDeviceImage();
        }
    }

    public ImageView getIndexIcon(){
        return indexIcon;
    }

    @NonNull
    @Override
    public String getTitle() {
        final Bundle arguments = getArguments();
        if(arguments !=null){
            mStep = arguments.getParcelable(GlobalSetting.STEP_STRING);
            if(mStep == null) {
                return "";
            }
            return mStep.getStepTitle().toUpperCase();
        }
        return "";
    }

    public ArcusStep getStep() {
        final Bundle arguments = getArguments();
        if(arguments !=null){
            mStep = arguments.getParcelable(GlobalSetting.STEP_STRING);
            return mStep;
        }
        return null;
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.step_view_temp;
    }


    @Override
    public void onClick(@NonNull View v) {
        final int id = v.getId();

        String deviceId = null;     //  needed to be initialized

        HubModel hubModel;   //  needed to be initialized
        DeviceModel deviceModel;

        switch (id){
            case R.id.sclera_photo_layout:

                String placeId = registrationContext.getPlaceModel().getId();
                if (isHub) {
                    hubModel = HubModelProvider.instance().getHubModel();
                    if (hubModel != null) {
                        deviceId = hubModel.getId();
                    }
                }
                else {
                    deviceModel = registrationContext.getPairedModel();
                    if (deviceModel != null) {
                        deviceId = deviceModel.getId();
                    }
                }

                if (deviceId != null) {  // could only be null if both hub and model are null

                    ImageManager.with(getActivity())
                            .putUserGeneratedDeviceImage(placeId, deviceId)
                            .fromCameraOrGallery()
                            .useAsWallpaper(AlphaPreset.DARKEN)
                            .withTransform(new CropCircleTransformation())
                            .into(profileImage)
                            .execute();
                }
                else {
                    logger.error("Pairing Device: ArcusStepFragment [Taking Picture] - Model is null");
                }
                break;

            default:
                break;
        }
    }


    private void applyDeviceImage() {

        //  hub gets treated as a device

        if (isHub) {

            HubModel hubModel = HubModelProvider.instance().getHubModel();
            if (hubModel != null) {
                ImageManager.with(getActivity())
                        .putLargeDeviceImage(hubModel)
                        .withTransformForStockImages(new BlackWhiteInvertTransformation(Invert.WHITE_TO_BLACK))
                        .withTransform(new CropCircleTransformation())
                        .into(profileImage)
                        .execute();
        }
            else {
                logger.error("Pairing Hub: ArcusStepFragment [applyDeviceImage()]- HubModel is NULL");
            }
        }
        else {

            DeviceModel deviceModel = registrationContext.getPairedModel();

            if (deviceModel != null) {

                ImageManager.with(getActivity())
                        .putLargeDeviceImage(deviceModel)
                        .withTransformForStockImages(new BlackWhiteInvertTransformation(Invert.WHITE_TO_BLACK))
                        .withTransform(new CropCircleTransformation())
                        .into(profileImage)
                        .execute();
            }
            else {
                logger.error("Pairing Device: ArcusStepFragment [applyDeviceImage()]- DeviceModel is NULL");
            }
        }

    }


    @Override
    public boolean validate() {
        if(deviceNameContainer.getVisibility() == View.VISIBLE){
            if(StringUtils.isBlank(deviceName.getText())) {
                deviceNameContainer.setError(getActivity().getString(R.string.requiredField, deviceName.getHint()));
                formOk = false;
            }else{
                deviceNameContainer.setError(null);
                formOk = isMatch;
            }

            return formOk;
        }else{
            return super.validate();
        }
    }

    @Override
    public boolean submit() {
        if(deviceNameContainer.getVisibility() == View.VISIBLE){
            return true;
        }else{
            return super.submit();
        }
    }

    @Override
    public void onShowedFragment() {
        init();
    }

    public void updateIndexIcon(int position) {
        final ImageView index = this.indexIcon;
        switch (position) {
            case 0:
                index.setImageResource(R.drawable.step1);
                break;
            case 1:
                index.setImageResource(R.drawable.step2);
                break;
            case 2:
                index.setImageResource(R.drawable.step3);
                break;
            case 3:
                index.setImageResource(R.drawable.step4);
                break;
            case 4:
                index.setImageResource(R.drawable.step5);
                break;
            case 5:
                index.setImageResource(R.drawable.step6);
                break;
        }
    }
}

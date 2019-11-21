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
package arcus.app.device.details;

import android.app.Activity;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import android.view.View;
import android.widget.ImageButton;

import arcus.cornea.device.DeviceController;
import arcus.cornea.device.blinds.SomfyBlindsDeviceController;
import arcus.cornea.device.blinds.model.SomfyBlindsSettingModel;
import arcus.cornea.error.ErrorModel;
import com.iris.client.capability.Somfyv1;
import com.iris.client.model.DeviceModel;
import arcus.app.R;
import arcus.app.common.banners.IrrigationAutoModeBanner;
import arcus.app.common.banners.core.BannerManager;
import arcus.app.common.error.ErrorManager;
import arcus.app.common.fragments.IClosedFragment;
import arcus.app.common.fragments.IShowedFragment;
import arcus.app.common.view.GlowableImageView;

public class SomfyBlindsFragment extends ArcusProductFragment
      implements SomfyBlindsDeviceController.Callback,
        DeviceController.Callback,
      View.OnClickListener,
      IShowedFragment, IClosedFragment
{
    private static final float BUTTON_ENABLED_ALPHA = 1.0f;
    private static final float BUTTON_DISABLED_ALPHA = 0.4f;

    private SomfyBlindsDeviceController controller;
    private SomfyBlindsSettingModel controllerDetailsModel;

    protected ImageButton leftButton;
    protected ImageButton middleButton;
    protected ImageButton rightButton;

    int leftImage = 0;
    int middleImage = 0;
    int rightImage = R.drawable.button_fav;

    @NonNull public static SomfyBlindsFragment newInstance() {
        return new SomfyBlindsFragment();
    }

    @Override public Integer topSectionLayout() {
        return R.layout.device_top_schedule;
    }

    @Override public void doTopSection() {

    }

    @Override
    public void doStatusSection() {
        leftButton = (ImageButton) statusView.findViewById(R.id.left_button);
        leftButton.setOnClickListener(this);
        middleButton = (ImageButton) statusView.findViewById(R.id.middle_button);
        middleButton.setOnClickListener(this);
        rightButton = (ImageButton) statusView.findViewById(R.id.right_button);
        rightButton.setOnClickListener(this);

        setImageGlowMode(GlowableImageView.GlowMode.OFF);
    }

    @Override
    public Integer statusSectionLayout() {
        return R.layout.somfy_blinds_status;
    }

    @Override public void onClick(View v) {
        if (v == null) {
            return;
        }

        switch (v.getId()) {
            case R.id.left_button:
                controller.leftButtonAction();
                break;
            case R.id.middle_button:
                controller.middleButtonAction();
                break;
            case R.id.right_button:
                controller.rightButtonAction();
                break;
            default:
                break;
        }
        runLoadingBar();
    }

    @Override public void onResume() {
        super.onResume();

        DeviceModel model = getDeviceModel();
        if (model == null) {
            return;
        }

        controller = SomfyBlindsDeviceController.newController(model.getId(), this, this);

    }

    @Override public void onPause() {
        super.onPause();
        controller.removeCallback();
    }


    @Override public void errorOccurred(Throwable throwable) {
        ErrorManager.in(getActivity()).showGenericBecauseOf(throwable);
    }

    @Override
    public void updateView() {
        if (!controllerDetailsModel.isOnline()) {
            enableButtons(false);
        }
        else {
            enableButtons(true);
        }
    }

    @Override public void onShowedFragment() {
        checkConnection();
        if (controllerDetailsModel == null) {
            return;
        }
        updateView();
    }

    private void updateButton(final ImageButton button, final boolean enabled, final int image, final int visibility) {
        button.setVisibility(visibility);
        button.setEnabled(enabled);
        button.setAlpha(enabled ? BUTTON_ENABLED_ALPHA : BUTTON_DISABLED_ALPHA);
        Activity a = getActivity();
        if(a != null){
            button.setBackground(ContextCompat.getDrawable(a, image));
        }
    }

    @Override public void onClosedFragment() {
        BannerManager.in(getActivity()).removeBanner(IrrigationAutoModeBanner.class);
        setEnabled(false);
    }

    @Override
    public void show(Object model) {
        if (model == null) {
            return;
        }
        controllerDetailsModel = (SomfyBlindsSettingModel)model;
        updateView();
    }

    @Override
    public void onError(ErrorModel error) {

    }

    public void runLoadingBar(){
        enableButtons(false);
        leftButton.postDelayed(new Runnable() {
            @Override
            public void run() {
                enableButtons(true);
            }
        }, 5000);

    }


    public void enableButtons(Boolean enabled){
        if(Somfyv1.TYPE_SHADE.equals(controllerDetailsModel.getType())) {
            this.leftImage = R.drawable.button_up;
            this.middleImage = R.drawable.button_down;
        }
        else {
            this.leftImage = R.drawable.button_open;
            this.middleImage = R.drawable.button_close;
        }

        updateButton(leftButton, enabled, leftImage, View.VISIBLE);
        updateButton(middleButton, enabled, middleImage, View.VISIBLE);
        updateButton(rightButton, enabled, rightImage, View.VISIBLE);
    }
}

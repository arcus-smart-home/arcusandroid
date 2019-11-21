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
package arcus.app.device.pairing.post;

import android.os.Bundle;
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;

import com.iris.client.model.DeviceModel;
import arcus.app.R;
import arcus.app.common.error.ErrorManager;
import arcus.app.common.image.ImageManager;
import arcus.app.common.image.Wallpaper;
import arcus.app.common.image.picasso.transformation.CircularTransformation;
import arcus.app.common.sequence.SequencedFragment;
import arcus.app.common.utils.GlobalSetting;
import arcus.app.common.view.CircularImageView;
import arcus.app.device.pairing.post.controller.PostPairingSequenceController;
import arcus.app.device.pairing.post.controller.TiltSensorOrientationFragmentController;


public class TiltSensorOrientationFragment extends SequencedFragment<PostPairingSequenceController> implements TiltSensorOrientationFragmentController.Callbacks {

    private static final String DEVICE_ADDRESS = "DEVICE_ADDRESS";

    private CheckBox horizontalCheckBox;
    private CheckBox verticalCheckBox;
    private Button nextBtn;
    private CircularImageView circularImageView;

    @NonNull
    public static TiltSensorOrientationFragment newInstance(String deviceAddress) {

        TiltSensorOrientationFragment fragment = new TiltSensorOrientationFragment();

        Bundle bundle = new Bundle();
        bundle.putString(DEVICE_ADDRESS, deviceAddress);
        fragment.setArguments(bundle);

        return fragment;
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        horizontalCheckBox = (CheckBox) view.findViewById(R.id.cbTiltSensorPairingHorizontal);
        verticalCheckBox = (CheckBox) view.findViewById(R.id.cbTiltSensorPairingVertical);
        circularImageView = (CircularImageView) view.findViewById(R.id.tilt_sensor_extra_image);
        nextBtn = (Button) view.findViewById(R.id.tilt_nextBtn);

        return view;
    }

    @Override
    public void onResume () {
        super.onResume();

        //  default to horizontal being checked saved
        horizontalCheckBox.setChecked(true);
        verticalCheckBox.setChecked(false);

        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goNext();
            }
        });

        showProgressBar();
        TiltSensorOrientationFragmentController.getInstance().setListener(this);
        TiltSensorOrientationFragmentController.getInstance().loadDeviceModel(getActivity(), getArguments().getString(DEVICE_ADDRESS));
    }

    @Override
    public void onPause () {
        super.onPause();
        TiltSensorOrientationFragmentController.getInstance().removeListener();
    }

    @NonNull
    @Override
    public String getTitle() {
        return "";
    }


    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_tilt_sensor_pairing_extra;
    }

    @Override
    public void onDeviceModelLoaded(DeviceModel deviceModel) {
        hideProgressBar();
        boolean bClosedOnVertical = deviceModel.getTags().contains(GlobalSetting.VERTICAL_TILT_TAG);
        if(bClosedOnVertical) {
            horizontalCheckBox.setChecked(true);
            verticalCheckBox.setChecked(false);
        }
        else {
            horizontalCheckBox.setChecked(false);
            verticalCheckBox.setChecked(true);
        }

        horizontalCheckBox.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (horizontalCheckBox.isChecked()) {
                    verticalCheckBox.setChecked(false);

                    TiltSensorOrientationFragmentController.getInstance().setClosedOnVertical(true);
                }
            }

        });

        verticalCheckBox.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (verticalCheckBox.isChecked()) {
                    horizontalCheckBox.setChecked(false);

                    TiltSensorOrientationFragmentController.getInstance().setClosedOnVertical(false);
                }
            }

        });

        ImageManager.with(getActivity())
                .putLargeDeviceImage(deviceModel)
                .withTransform(new CircularTransformation())
                .into(circularImageView)
                .execute();

        ImageManager.with(getActivity()).setWallpaper(Wallpaper.ofDevice(deviceModel).lightend());
    }

    @Override
    public void onSuccess() {
        hideProgressBar();
    }

    @Override
    public void onFailure(Throwable cause) {
        hideProgressBar();
        ErrorManager.in(getActivity()).showGenericBecauseOf(cause);
    }
}

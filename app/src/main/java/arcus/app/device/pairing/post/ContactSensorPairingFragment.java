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
import android.widget.RelativeLayout;

import com.iris.client.capability.Contact;
import com.iris.client.model.DeviceModel;
import arcus.app.R;
import arcus.app.common.error.ErrorManager;
import arcus.app.common.image.ImageManager;
import arcus.app.common.image.Wallpaper;
import arcus.app.common.image.picasso.transformation.BlackWhiteInvertTransformation;
import arcus.app.common.image.picasso.transformation.CircularTransformation;
import arcus.app.common.image.picasso.transformation.Invert;
import arcus.app.common.sequence.SequencedFragment;
import arcus.app.common.view.CircularImageView;
import arcus.app.device.pairing.post.controller.ContactSensorPairingFragmentController;
import arcus.app.device.pairing.post.controller.ContactSensorPairingSequenceController;


public class ContactSensorPairingFragment extends SequencedFragment<ContactSensorPairingSequenceController> implements ContactSensorPairingFragmentController.Callbacks {

    private final static String DEVICE_ADDRESS = "DEVICE_ADDRESS";
    private final static String DEVICE_NAME = "DEVICE_NAME";

    private CheckBox doorCheckBox;
    private CheckBox windowCheckBox;
    private CheckBox otherCheckBox;
    private Button nextBtn;
    private CircularImageView circularImageView;
    private RelativeLayout nextBtnRelativeLayout;

    @NonNull
    public static ContactSensorPairingFragment newInstance(String deviceName, String deviceAddress) {
        ContactSensorPairingFragment instance = new ContactSensorPairingFragment();
        Bundle arguments = new Bundle();
        arguments.putString(DEVICE_ADDRESS, deviceAddress);
        arguments.putString(DEVICE_NAME, deviceName);
        instance.setArguments(arguments);

        return instance;
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        doorCheckBox = (CheckBox) view.findViewById(R.id.cbSensorDoor);
        windowCheckBox = (CheckBox) view.findViewById(R.id.cbSensorWindow);
        otherCheckBox = (CheckBox) view.findViewById(R.id.cbSensorOther);
        circularImageView = (CircularImageView) view.findViewById(R.id.sensor_extra_image);
        nextBtn = (Button) view.findViewById(R.id.nextBtn);
        nextBtnRelativeLayout = (RelativeLayout) view.findViewById(R.id.next_btn_relative_layout);

        return view;
    }

    @Override
    public void onResume () {
        super.onResume();

        //  default to door being checked saved
        doorCheckBox.setChecked(true);
        windowCheckBox.setChecked(false);
        otherCheckBox.setChecked(false);

        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goNext();
            }
        });

        ContactSensorPairingFragmentController.getInstance().setListener(this);
        ContactSensorPairingFragmentController.getInstance().loadDevice(getActivity(), getArguments().getString(DEVICE_ADDRESS));
    }

    @Override
    public void onPause () {
        super.onPause();
        ContactSensorPairingFragmentController.getInstance().removeListener();
    }

    @NonNull
    @Override
    public String getTitle() {
        return getArguments().getString(DEVICE_NAME);
    }


    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_contact_sensor_pairing_extra;
    }

    @Override
    public void onDeviceModelLoaded(DeviceModel model) {

        // Set default setting to door
        ContactSensorPairingFragmentController.getInstance().setContactSensorHint(Contact.USEHINT_DOOR);

        doorCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (doorCheckBox.isChecked()) {
                    windowCheckBox.setChecked(false);
                    otherCheckBox.setChecked(false);

                    ContactSensorPairingFragmentController.getInstance().setContactSensorHint(Contact.USEHINT_DOOR);
                }
            }
        });

        windowCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (windowCheckBox.isChecked()) {
                    doorCheckBox.setChecked(false);
                    otherCheckBox.setChecked(false);

                    ContactSensorPairingFragmentController.getInstance().setContactSensorHint(Contact.USEHINT_WINDOW);
                }
            }
        });

        otherCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (otherCheckBox.isChecked()) {
                    doorCheckBox.setChecked(false);
                    windowCheckBox.setChecked(false);

                    ContactSensorPairingFragmentController.getInstance().setContactSensorHint(Contact.USEHINT_OTHER);
                }
            }
        });

        ImageManager.with(getActivity())
                .putLargeDeviceImage(model)
                .withTransformForStockImages(new BlackWhiteInvertTransformation(Invert.WHITE_TO_BLACK))
                .withTransform(new CircularTransformation())
                .into(circularImageView)
                .execute();

        ImageManager.with(getActivity()).setWallpaper(Wallpaper.ofDevice(model).lightend());
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

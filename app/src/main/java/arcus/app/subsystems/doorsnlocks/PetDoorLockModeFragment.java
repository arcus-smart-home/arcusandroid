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
package arcus.app.subsystems.doorsnlocks;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RelativeLayout;

import arcus.cornea.provider.DeviceModelProvider;
import arcus.cornea.utils.Listeners;
import com.iris.client.capability.PetDoor;
import com.iris.client.event.Listener;
import com.iris.client.model.DeviceModel;
import arcus.app.R;
import arcus.app.common.error.ErrorManager;
import arcus.app.common.sequence.ReturnToSenderSequenceController;
import arcus.app.common.sequence.SequencedFragment;
import arcus.app.common.utils.CorneaUtils;
import arcus.app.common.view.Version1Button;
import arcus.app.common.view.Version1ButtonColor;


public class PetDoorLockModeFragment extends SequencedFragment<ReturnToSenderSequenceController> {

    private final static String DEVICE_ID = "DEVICE_ID";

    private RadioButton lockedButton;
    private RelativeLayout lockedClickRegion;
    private RadioButton unlockedButton;
    private RelativeLayout unlockedClickRegion;
    private RadioButton autoButton;
    private RelativeLayout autoClickRegion;
    private Version1Button saveButton;

    public static PetDoorLockModeFragment newInstance (String petDoorDeviceId) {
        PetDoorLockModeFragment instance = new PetDoorLockModeFragment();
        Bundle arguments = new Bundle();
        arguments.putString(DEVICE_ID, petDoorDeviceId);
        instance.setArguments(arguments);

        return instance;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        lockedButton = (RadioButton) view.findViewById(R.id.locked_checkbox);
        lockedClickRegion = (RelativeLayout) view.findViewById(R.id.locked_click_region);
        unlockedButton = (RadioButton) view.findViewById(R.id.unlocked_checkbox);
        unlockedClickRegion = (RelativeLayout) view.findViewById(R.id.unlocked_click_region);
        autoButton = (RadioButton) view.findViewById(R.id.auto_checkbox);
        autoClickRegion = (RelativeLayout) view.findViewById(R.id.auto_click_region);
        saveButton = (Version1Button) view.findViewById(R.id.save_button);

        return view;
    }


    @Override
    public void onResume () {
        super.onResume();
        getActivity().setTitle(getTitle());

        saveButton.setColorScheme(Version1ButtonColor.WHITE);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setPlatformLockState(getSelectedLockState());
                goNext();
            }
        });

        lockedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSelectedLockState(R.id.locked_checkbox);
            }
        });

        lockedClickRegion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSelectedLockState(R.id.locked_checkbox);
            }
        });

        unlockedClickRegion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSelectedLockState(R.id.unlocked_checkbox);
            }
        });

        unlockedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSelectedLockState(R.id.unlocked_checkbox);
            }
        });

        autoClickRegion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSelectedLockState(R.id.auto_checkbox);
            }
        });

        autoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSelectedLockState(R.id.auto_checkbox);
            }
        });

        loadPlatformLockState();
    }

    @Nullable
    @Override
    public String getTitle() {
        return getString(R.string.petdoor_mode_choose);
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_choose_pet_door_mode;
    }

    private void setSelectedLockState (int checkedRadioButtonId) {
        lockedButton.setChecked(checkedRadioButtonId == R.id.locked_checkbox);
        unlockedButton.setChecked(checkedRadioButtonId == R.id.unlocked_checkbox);
        autoButton.setChecked(checkedRadioButtonId == R.id.auto_checkbox);
    }

    private String getSelectedLockState () {
        int checkedButton = getCheckedRadioButtonId();

        switch (checkedButton) {
            case R.id.locked_checkbox:
                return PetDoor.LOCKSTATE_LOCKED;
            case R.id.unlocked_checkbox:
                return PetDoor.LOCKSTATE_UNLOCKED;
            case R.id.auto_checkbox:
                return PetDoor.LOCKSTATE_AUTO;

            default:
                throw new IllegalStateException("Bug! Unhandled lock state radio button.");
        }
    }

    private int getCheckedRadioButtonId () {
        if (lockedButton.isChecked()) {
            return R.id.locked_checkbox;
        } else if (unlockedButton.isChecked()) {
            return R.id.unlocked_checkbox;
        } else {
            return R.id.auto_checkbox;
        }
    }

    private String getDeviceId () {
        return getArguments().getString(DEVICE_ID);
    }

    private void loadPlatformLockState () {

        DeviceModelProvider.instance().getModel(CorneaUtils.getDeviceAddress(getDeviceId())).load().onSuccess(new Listener<DeviceModel>() {
            @Override
            public void onEvent(DeviceModel deviceModel) {
                PetDoor petDoor = CorneaUtils.getCapability(deviceModel, PetDoor.class);

                if (petDoor != null) {
                    switch (petDoor.getLockstate()) {
                        case PetDoor.LOCKSTATE_LOCKED:
                            setSelectedLockState(R.id.locked_checkbox);
                            break;

                        case PetDoor.LOCKSTATE_UNLOCKED:
                            setSelectedLockState(R.id.unlocked_checkbox);
                            break;

                        case PetDoor.LOCKSTATE_AUTO:
                            setSelectedLockState(R.id.auto_checkbox);
                            break;
                    }
                }
            }
        }).onFailure(Listeners.runOnUiThread(new Listener<Throwable>() {
            @Override
            public void onEvent(Throwable throwable) {
                ErrorManager.in(getActivity()).showGenericBecauseOf(throwable);
            }
        }));
    }

    private void setPlatformLockState (final String lockState) {
        DeviceModelProvider.instance().getModel(CorneaUtils.getDeviceAddress(getDeviceId())).load().onSuccess(new Listener<DeviceModel>() {
            @Override
            public void onEvent(DeviceModel deviceModel) {
                PetDoor petDoor = CorneaUtils.getCapability(deviceModel, PetDoor.class);

                // TODO: What happens if we can't set lock state... nada?
                if (petDoor != null) {
                    petDoor.setLockstate(lockState);
                    deviceModel.commit().onFailure(Listeners.runOnUiThread(new Listener<Throwable>() {
                        @Override
                        public void onEvent(Throwable throwable) {
                            ErrorManager.in(getActivity()).showGenericBecauseOf(throwable);
                        }
                    }));
                }
            }
        }).onFailure(Listeners.runOnUiThread(new Listener<Throwable>() {
            @Override
            public void onEvent(Throwable throwable) {
                ErrorManager.in(getActivity()).showGenericBecauseOf(throwable);
            }
        }));
    }
}

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
package arcus.app.device.pairing.specialty.honeywelltcc;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.google.common.collect.Lists;
import arcus.cornea.CorneaClientFactory;
import arcus.cornea.utils.Listeners;
import com.iris.client.ClientMessage;
import com.iris.client.capability.Capability;
import com.iris.client.capability.Device;
import com.iris.client.capability.Thermostat;
import com.iris.client.event.Listener;
import com.iris.client.event.ListenerRegistration;
import com.iris.client.model.DeviceModel;
import com.iris.client.model.Model;
import arcus.app.R;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.view.Version1Button;
import arcus.app.common.view.Version1ButtonColor;
import arcus.app.common.view.Version1TextView;
import arcus.app.dashboard.HomeFragment;
import arcus.app.device.model.DeviceType;
import arcus.app.device.pairing.catalog.controller.ProductCatalogFragmentController;
import arcus.app.device.pairing.steps.AbstractPairingStepFragment;
import arcus.app.subsystems.alarm.AlertFloatingFragment;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class HoneywellSearching extends AbstractPairingStepFragment {
    public static final String SHOULD_CHECK_FOR_DEVICES = "SHOULD_CHECK_FOR_DEVICES";
    public static final String DEVICE_IDS_FOUND = "DEVICES_FOUND";
    private AtomicInteger honeywellDevicesFound = new AtomicInteger(0);
    private AtomicReference<String> devName = new AtomicReference<>("");
    private AtomicReference<String> devAddress = new AtomicReference<>("");

    private Set<String> devicesFound = new HashSet<>();

    private ListenerRegistration deviceAddedListenerReg;
    private final long TIMEOUT = TimeUnit.SECONDS.toMillis(60);
    private LinearLayout devicesAdded;
    private Version1Button deviceButton;
    private Handler handler = new Handler(Looper.getMainLooper());

    // This just looks up the model in the cache and passes it off.  Shouldn't be too heavy on Main Thread.
    private final Listener<ClientMessage> deviceAddedListener = Listeners.runOnUiThread(
          new Listener<ClientMessage>() {
              @Override
              public void onEvent(@NonNull ClientMessage clientMessage) {
                  if (clientMessage.getEvent() instanceof Capability.AddedEvent) {
                      try {
                          String deviceAddress = String.valueOf(clientMessage.getEvent().getAttribute(Capability.ATTR_ADDRESS));
                          Model model = CorneaClientFactory.getModelCache().get(deviceAddress);
                          if (model == null) {
                              logger.error("Received base:Added but cound not find in cache. Address [{}]", deviceAddress);
                              return;
                          }
                          if (model.getCaps() == null || !model.getCaps().contains(Device.NAMESPACE)) {
                              logger.error("Received Add for Non-Device, Ignoring. Address [{}]", deviceAddress);
                              return;
                          }

                          onDeviceFound((DeviceModel) model);
                      } catch (Exception ignored) {}
                  }
              }
          }
    );

    public static HoneywellSearching newInstance(boolean shouldCheckForNewDevices) {
        HoneywellSearching fragment = new HoneywellSearching();
        Bundle args = new Bundle();
        args.putBoolean(SHOULD_CHECK_FOR_DEVICES, shouldCheckForNewDevices);
        fragment.setArguments(args);
        return fragment;
    }

    public static HoneywellSearching newInstance(boolean shouldCheckForNewDevices, ArrayList<String> deviceIDsfound) {
        HoneywellSearching fragment = new HoneywellSearching();
        Bundle args = new Bundle();
        args.putBoolean(SHOULD_CHECK_FOR_DEVICES, shouldCheckForNewDevices);
        args.putStringArrayList("DEVICES_FOUND", deviceIDsfound);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable @Override public View onCreateView(
          LayoutInflater inflater,
          @Nullable ViewGroup container,
          @Nullable Bundle savedInstanceState
    ) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        if (view == null) {
            return view;
        }

        devicesAdded = (LinearLayout) view.findViewById(R.id.honeywell_devices_added);
        deviceButton = (Version1Button) view.findViewById(R.id.honeywell_continue_button);
        if (deviceButton != null) {
            deviceButton.setColorScheme(Version1ButtonColor.WHITE);
        }
        return view;
    }

    @Override public void onResume() {
        super.onResume();

        Activity activity = getActivity();
        View view = getView();
        if (view == null || activity == null) {
            return;
        }

        Bundle args = getArguments();
        if (args == null) {
            args = new Bundle(1);
        }

        if (args.getBoolean(SHOULD_CHECK_FOR_DEVICES, false)) {
            deviceAddedListenerReg = CorneaClientFactory.getClient().addMessageListener(deviceAddedListener);
        }

        ArrayList<String> devicesFoundOnPreviousScreen = args.getStringArrayList(DEVICE_IDS_FOUND);
        if (devicesFoundOnPreviousScreen != null && !devicesFoundOnPreviousScreen.isEmpty()) {
            for (String address : devicesFoundOnPreviousScreen) {
                Model model = CorneaClientFactory.getModelCache().get(address);
                if (model instanceof DeviceModel) {
                    onDeviceFound((DeviceModel) model);
                }
            }
        }

        if (honeywellDevicesFound.get() > 0) {
            return;
        }

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (honeywellDevicesFound.get() > 0) {
                    return;
                }

                AlertFloatingFragment alert = AlertFloatingFragment.newInstance(
                      getString(R.string.honeywell_no_devices_found_title),
                      getString(R.string.honeywell_no_devices_found_desc),
                      getString(R.string.honeywell_no_devices_retry),
                      getString(R.string.honeywell_no_devices_cancel),
                      new AlertFloatingFragment.AlertButtonCallback() {
                          @Override
                          public boolean topAlertButtonClicked() {
                              try {
                                  goBack(); // Go back to entering credentials screen
                              } catch (Exception ignored) {}
                              return false; // We handle the closing here.
                          }

                          @Override
                          public boolean bottomAlertButtonClicked() {
                              try {
                                  logger.debug("Ending sequence {}; Navigating to the dashboard b/c of a cancel click.", this);
                                  ProductCatalogFragmentController.instance().stopPairing();
                                  BackstackManager.getInstance().navigateBackToFragment(HomeFragment.newInstance());
                              } catch (Exception ignored) {}
                              return false; // We handle the closing here.
                          }
                      }
                );

                String stackName = alert.getClass().getCanonicalName();
                BackstackManager.getInstance().navigateToFloatingFragment(alert, stackName, true);
            }
        }, TIMEOUT);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        deviceAddedListenerReg = Listeners.clear(deviceAddedListenerReg);
    }

    @Override public void onDeviceFound(DeviceModel deviceModel) {
        Activity activity = getActivity();
        if (devicesAdded == null || deviceButton == null || activity == null) {
            return;
        }

        DeviceType addedModel = DeviceType.fromHint(deviceModel.getDevtypehint());
        devicesFound.add(deviceModel.getAddress());

        if (DeviceType.TCC_THERM.equals(addedModel)) {
            foundHoneywellDevice(deviceModel.getName(), deviceModel.getAddress());
        }
        else if (honeywellDevicesFound.get() == 0) {
            super.onDeviceFound(deviceModel);
        } // Else we wait for the user to press "I'm done" button for honeywell devices added.
    }

    protected void foundHoneywellDevice(String name, String address) {
        int totalHoneywellDevicesFound = honeywellDevicesFound.incrementAndGet();

        Version1TextView textView = new Version1TextView(getActivity());
        if (TextUtils.isEmpty(name)) {
            // Default will be "Thermostat #" where # is the # of devices we've found
            name = String.format("%s %d", Thermostat.NAME, totalHoneywellDevicesFound + 1);
        }
        textView.setText(name);
        textView.setTextColor(Color.WHITE);
        devicesAdded.addView(textView);

        devName.set(name);
        devAddress.set(address);

        if (deviceButton.getVisibility() != View.VISIBLE) {
            deviceButton.setVisibility(View.VISIBLE);
            deviceButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (devicesFound.size() == 1) {
                        getController().goSinglePairingSequence(getActivity(), devName.get(), devAddress.get());
                    }
                    else {
                        getController().goMultipairingSequence(getActivity(), Lists.newArrayList(devicesFound));
                    }
                }
            });
        }
    }
    @Nullable @Override public String getTitle() {
        return null;
    }

    @Override public Integer getLayoutId() {
        return R.layout.fragment_honeywell_pairing_searching;
    }
}

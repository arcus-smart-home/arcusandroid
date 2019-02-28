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
package arcus.app.device.pairing.multi;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import arcus.cornea.provider.ProductModelProvider;
import com.iris.client.capability.Product;
import com.iris.client.model.DeviceModel;
import arcus.app.R;
import arcus.app.common.error.ErrorManager;
import arcus.app.common.image.ImageManager;
import arcus.app.common.image.Wallpaper;
import arcus.app.common.models.ListItemModel;
import arcus.app.common.sequence.SequencedFragment;
import arcus.app.common.view.Version1Button;
import arcus.app.device.pairing.multi.adapter.MultipairingListAdapter;
import arcus.app.device.pairing.multi.controller.MultipairingListFragmentController;
import arcus.app.device.pairing.multi.controller.MultipairingSequenceController;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MultipairingListFragment extends SequencedFragment<MultipairingSequenceController> implements MultipairingListFragmentController.Callbacks {

    private final static Logger logger = LoggerFactory.getLogger(MultipairingSequenceController.class);
    private final static String DEVICE_PAIR_STATUS = "DEVICE_PAIR_STATUS";
    private final static int REFRESH_DEVICE_LIST_TIMEOUT = 1000;

    private ListView deviceList;
    private Version1Button nextButton;

    public static MultipairingListFragment newInstance (List<String> deviceAddresses) {
        MultipairingListFragment instance = new MultipairingListFragment();

        // By definition, no devices have been provisioned when we start this fragment
        HashMap<String,Boolean> deviceAddressStatus = new HashMap<>();
        for (String thisAddress : deviceAddresses) {
            deviceAddressStatus.put(thisAddress, false);
        }

        Bundle arguments = new Bundle();
        arguments.putSerializable(DEVICE_PAIR_STATUS, deviceAddressStatus);
        instance.setArguments(arguments);
        return instance;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        deviceList = (ListView) view.findViewById(R.id.lvMultiPairList);
        nextButton = (Version1Button) view.findViewById(R.id.deviceFoundDoneBtn);

        return view;
    }

    public void onResume () {
        super.onResume();
        getActivity().setTitle(getTitle());

        deviceList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ListItemModel selectedItem = (ListItemModel) parent.getAdapter().getItem(position);

                String selectedDeviceName = selectedItem.getText();
                String selectedDeviceAddress = ((DeviceModel) selectedItem.getData()).getAddress();
                setDeviceDoneProvisioning(selectedDeviceAddress);
                goNext(selectedDeviceName, selectedDeviceAddress);
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isDoneProvisioning()) {
                    getController().endSequence(getActivity(), true);
                } else {
                    promptForConfirmation();
                }
            }
        });

        ImageManager.with(getActivity()).setWallpaper(Wallpaper.ofCurrentPlace().lightend());

        MultipairingListFragmentController.getInstance().setListener(this);
        MultipairingListFragmentController.getInstance().startMultipairing(new ArrayList<String>(getDeviceAddressStatus().keySet()));

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                MultipairingListFragmentController.getInstance().startMultipairing(new ArrayList<String>());
            }
        }, REFRESH_DEVICE_LIST_TIMEOUT);
    }

    public void onPause () {
        super.onPause();
        MultipairingListFragmentController.getInstance().removeListener();
        MultipairingListFragmentController.getInstance().stopMonitoringForNewDevices();
    }

    @Nullable
    @Override
    public String getTitle() {
        return getString(R.string.multipairinglist_screen_title);
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_multi_pairing_list;
    }

    @Override
    public void onDeviceProvisioningStatusChanged(List<DeviceModel> devices) {

        List<ListItemModel> listItems = new ArrayList<>();

        for (DeviceModel device : devices) {
            ListItemModel deviceListItem = new ListItemModel();

            String deviceName = device.getName();
            if (TextUtils.isEmpty(deviceName)) {
                deviceListItem.setText(device.getModel());
            }
            else {
                deviceListItem.setText(deviceName);
            }

            String productID = device.getProductId();
            if (StringUtils.isEmpty(productID) ||
                ProductModelProvider.instance().getByProductIDOrNull(productID) == null ||
                Product.CERT_NONE.equals(ProductModelProvider.instance().getByProductIDOrNull(productID).get(Product.ATTR_CERT)))
            {
                deviceListItem.setSubText(getResources().getString(R.string.pairing_uncertified_device));
            } else {
                deviceListItem.setSubText(ProductModelProvider.instance().getByProductIDOrNull(productID).getVendor());
            }

            deviceListItem.setChecked(getDeviceProvisioningStatus(device.getAddress()));
            deviceListItem.setData(device);

            listItems.add(deviceListItem);
        }
        
        deviceList.setAdapter(new MultipairingListAdapter(getActivity(), listItems));
    }

    @Override
    public void onError(Throwable cause) {
        ErrorManager.in(getActivity()).showGenericBecauseOf(cause);
    }

    private void promptForConfirmation() {

        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        dialogBuilder.setCancelable(false);

        dialogBuilder.setMessage(getString(R.string.multipairinglist_dialog_msg));
        dialogBuilder.setTitle(getString(R.string.multipairinglist_dialog_title));

        dialogBuilder.setPositiveButton(getString(R.string.multipairinglist_dialog_pos_btn_text), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // Nothing to do
            }
        });

        dialogBuilder.setNegativeButton(getString(R.string.multipairinglist_dialog_neg_btn_text), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                endSequence(getActivity(), false);
            }
        });

        dialogBuilder.create().show();
    }

    private boolean getDeviceProvisioningStatus (String deviceAddress) {
        if(getDeviceAddressStatus() != null && deviceAddress != null){
            if (getDeviceAddressStatus().get(deviceAddress) == null) {
                return false;
            } else
                return getDeviceAddressStatus().get(deviceAddress);
        }
        return false;
    }

    private boolean isDoneProvisioning () {
        return !getDeviceAddressStatus().values().contains(false);
    }

    private void setDeviceDoneProvisioning (String deviceAddress) {
        getDeviceAddressStatus().put(deviceAddress, true);
    }

    private Map<String,Boolean> getDeviceAddressStatus() {
        return (Map<String,Boolean>) getArguments().getSerializable(DEVICE_PAIR_STATUS);
    }
}

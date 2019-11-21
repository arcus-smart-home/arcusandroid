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
package arcus.app.device.more;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.common.base.Strings;
import arcus.cornea.CorneaClientFactory;
import arcus.cornea.utils.Listeners;

import com.iris.client.ClientEvent;
import com.iris.client.capability.DeviceAdvanced;
import com.iris.client.capability.DeviceOta;
import com.iris.client.capability.Hub;
import com.iris.client.capability.HubAdvanced;
import com.iris.client.capability.HubConnection;
import com.iris.client.capability.IpInfo;
import com.iris.client.capability.ProductCatalog;
import com.iris.client.event.Listener;
import com.iris.client.model.DeviceModel;
import com.iris.client.model.HubModel;
import com.iris.client.model.Model;
import com.iris.client.model.ProductModel;
import arcus.app.R;
import arcus.app.common.fragments.BaseFragment;
import arcus.app.common.utils.CorneaUtils;
import arcus.app.common.view.Version1TextView;
import arcus.app.device.model.DeviceType;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ProductInfoFragment extends BaseFragment {
    private static final String DEVICE_ID = "DEVICE.ID";
    private final DateFormat displayFormat = new SimpleDateFormat("MMM d, yyyy h:mm a", Locale.getDefault());

    @Nullable
    private String mDeviceId;
    private DeviceModel deviceModel;
    private HubModel hubModel;

    private Version1TextView mDeviceName;
    private Version1TextView mDeviceProductName;
    private Version1TextView mDeviceManufacturer;
    private Version1TextView mDeviceItemNumber;
    private Version1TextView mDeviceModelNumber;
    private Version1TextView mDeviceCertification;
    private Version1TextView mDeviceLastPaired;
    private Version1TextView mDeviceWireless;
    private Version1TextView mDeviceHubID;
    private Version1TextView mDeviceInstallDate;
    private Version1TextView mDeviceLastContact;

    private Version1TextView mDeviceWirelessTitle;
    private Version1TextView mDeviceModelNumberTitle;
    private Version1TextView mDeviceManufacturerTitle;
    private Version1TextView mDeviceItemNumberTitle;
    private Version1TextView mDeviceCertificationTitle;
    private Version1TextView mDeviceLastPairedTitle;
    private Version1TextView mDeviceHubIDTitle;
    private Version1TextView mDeviceInstallDateTitle;
    private Version1TextView mDeviceLastContactTitle;
    private Version1TextView mDeviceInformation;
    private Version1TextView mDeviceInformationTitle;

    private Version1TextView mNestRoom;
    private Version1TextView mNestTempLock;

    private Version1TextView hueHubIP;
    private Version1TextView hueHubFirmware;

    private Version1TextView lutronBridgeOperationgMode;
    private Version1TextView LutronBridgeSerialNumber;

    private LinearLayout productBottom;
    private LinearLayout deviceBottom;
    private LinearLayout nestBottom;
    private LinearLayout hueHubInfo;
    private LinearLayout lutronBridgeInfo;

    private TextView firmwareTitle;
    private TextView deviceFirmware;

    private View productInfoMacContainer;
    private TextView deviceMac;

    private Listener<ClientEvent> successListener = Listeners.runOnUiThread(new Listener<ClientEvent>() {
        @Override
        public void onEvent(@NonNull ClientEvent result) {
            ProductCatalog.GetProductResponse r = new ProductCatalog.GetProductResponse(result);
            Model m = CorneaClientFactory.getModelCache().addOrUpdate(r.getProduct());
            ProductModel pm = CorneaClientFactory.getStore(ProductModel.class).get(m.getId());

            if (pm != null) {
                updateProductInformation(pm);
            }
        }
    });

    private Listener<Throwable> failureListener = Listeners.runOnUiThread(new Listener<Throwable>() {
        @Override public void onEvent(Throwable throwable) {
            setProductName(null);
            setProductManufacturer(null);
            setProductItemNumber(null);
            setProductModelNumber(null);
            setProductCertification(null);
            setProductWireless(null);
        }
    });


    @NonNull
    public static ProductInfoFragment newInstance(String deviceId) {
        ProductInfoFragment fragment = new ProductInfoFragment();

        Bundle bundle = new Bundle(1);
        bundle.putString(DEVICE_ID, deviceId);
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        View mView = view;
        ImageView productImage = (ImageView) view.findViewById(R.id.fragment_device_info_image);

        mDeviceName = (Version1TextView) view.findViewById(R.id.device_name);
        mDeviceProductName = (Version1TextView) view.findViewById(R.id.device_product_name);

        mDeviceManufacturer = (Version1TextView) view.findViewById(R.id.device_manufacturer);
        mDeviceItemNumber = (Version1TextView) view.findViewById(R.id.device_item_number);
        mDeviceModelNumber = (Version1TextView) view.findViewById(R.id.device_model_number);
        mDeviceCertification = (Version1TextView) view.findViewById(R.id.device_certification);
        mDeviceLastPaired = (Version1TextView) view.findViewById(R.id.device_last_paired);
        mDeviceWireless = (Version1TextView) view.findViewById(R.id.device_wireless);
        mDeviceHubID = (Version1TextView) view.findViewById(R.id.device_hub_id);
        mDeviceInstallDate = (Version1TextView) view.findViewById(R.id.device_install_date);
        mDeviceLastContact = (Version1TextView) view.findViewById(R.id.device_last_contact);

        mDeviceWirelessTitle = (Version1TextView) view.findViewById(R.id.device_wireless_title);
        mDeviceModelNumberTitle = (Version1TextView) view.findViewById(R.id.lowes_model_number_title);
        mDeviceManufacturerTitle = (Version1TextView) view.findViewById(R.id.device_manufacturer_title);
        mDeviceCertificationTitle = (Version1TextView) view.findViewById(R.id.device_certification_title);
        mDeviceLastPairedTitle = (Version1TextView) view.findViewById(R.id.device_last_paired_title);
        mDeviceItemNumberTitle = (Version1TextView) view.findViewById(R.id.lowes_item_number_title);
        mDeviceHubIDTitle = (Version1TextView) view.findViewById(R.id.device_hub_id_title);
        mDeviceInstallDateTitle = (Version1TextView) view.findViewById(R.id.device_install_date_title);
        mDeviceLastContactTitle = (Version1TextView) view.findViewById(R.id.device_last_contact_title);

        mDeviceInformation = (Version1TextView) view.findViewById(R.id.device_information);
        mDeviceInformationTitle = (Version1TextView) view.findViewById(R.id.device_information_title);

        mNestRoom = (Version1TextView) view.findViewById(R.id.nest_room);
        mNestTempLock = (Version1TextView) view.findViewById(R.id.nest_templock);

        productBottom = (LinearLayout) view.findViewById(R.id.product_info_bottom);
        deviceBottom = (LinearLayout) view.findViewById(R.id.device_info_bottom);
        nestBottom = (LinearLayout) view.findViewById(R.id.nest_info);

        hueHubInfo = (LinearLayout) view.findViewById(R.id.hue_hub_info);
        hueHubIP = (Version1TextView) view.findViewById(R.id.hue_hub_firmware_internal_ip_address);
        hueHubFirmware = (Version1TextView) view.findViewById(R.id.hue_hub_firmware_version);

        lutronBridgeInfo = (LinearLayout) view.findViewById(R.id.lutron_bridge_info);
        lutronBridgeOperationgMode = (Version1TextView) view.findViewById(R.id.lutron_bridge_operating_mode);
        LutronBridgeSerialNumber = (Version1TextView) view.findViewById(R.id.lutron_bridge_serial_number);

        firmwareTitle = view.findViewById(R.id.firmware_title);
        deviceFirmware = view.findViewById(R.id.device_firmware);

        productInfoMacContainer = view.findViewById(R.id.product_info_mac_container);
        deviceMac = view.findViewById(R.id.device_mac);

        getActivity().setTitle(getTitle());

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        mDeviceId = getNonNullArguments().getString(DEVICE_ID);
        if (Strings.isNullOrEmpty(mDeviceId)) {
            return;
        }

        deviceModel = getCorneaService().getStore(DeviceModel.class).get(mDeviceId);
        if (deviceModel != null) {
            displayDevice();
            return;
        }

        hubModel = getCorneaService().getStore(HubModel.class).get(mDeviceId);
        if (hubModel != null) {
            displayHub();
        }
    }

    private void displayDevice() {
        hideView(false);
        mDeviceName.setText(deviceModel.getName());


        if (deviceModel.getCaps().contains(DeviceAdvanced.NAMESPACE)) {
            DeviceAdvanced deviceAdvanced = (DeviceAdvanced) deviceModel;
            Date added = deviceAdvanced.getAdded();
            if (added != null) {
                mDeviceLastPaired.setText(displayFormat.format(added));
            }
            else {
                mDeviceLastPaired.setVisibility(View.GONE);
                mDeviceLastPairedTitle.setVisibility(View.GONE);
            }
        }

        getProductInformation(deviceModel.getProductId());

        String firmware = (String) deviceModel.get(DeviceOta.ATTR_CURRENTVERSION);
        if (firmware != null) {
            firmwareTitle.setVisibility(View.VISIBLE);
            deviceFirmware.setText(firmware);
            deviceFirmware.setVisibility(View.VISIBLE);
        } else {
            firmwareTitle.setVisibility(View.GONE);
            deviceFirmware.setVisibility(View.GONE);
        }

        String mac = (String) deviceModel.get(IpInfo.ATTR_MAC);
        if (mac != null) {
            deviceMac.setText(mac);
            productInfoMacContainer.setVisibility(View.VISIBLE);
        } else {
            productInfoMacContainer.setVisibility(View.GONE);
        }

        if(DeviceType.fromHint(deviceModel.getDevtypehint()) == DeviceType.TCC_THERM) {
            deviceBottom.setVisibility(View.VISIBLE);
            mDeviceInformation.setText(getString(R.string.device_more_honeywell_c2c_device_information));
            mDeviceInformationTitle.setText(getString(R.string.device_more_auto_mode));
        }
        else {
            deviceBottom.setVisibility(View.GONE);
        }
    }

    private void displayHub() {
        hideView(true);
        mDeviceName.setText(hubModel.getName());
        mDeviceModelNumber.setText((String)hubModel.get(Hub.ATTR_MODEL));
        mDeviceHubID.setText(hubModel.getId());

        if (hubModel.getCaps().contains(HubAdvanced.NAMESPACE)) {
            HubAdvanced hubAdvanced = (HubAdvanced) hubModel;
            mDeviceManufacturer.setText(hubAdvanced.getMfgInfo());
        }
        if (hubModel.getCaps().contains(HubConnection.NAMESPACE)) {
            HubConnection hubConnection = (HubConnection) hubModel;
            Date lastChange = hubConnection.getLastchange();
            if (lastChange != null) {
                mDeviceLastContact.setText(displayFormat.format(lastChange));
            }
            else {
                mDeviceLastContact.setText(displayFormat.format(new Date(System.currentTimeMillis())));
            }
        }

        // FIXME: These fields should not be hidden but did not have the info available.
        mDeviceProductName.setText(getString(R.string.smart_hub_title));

        mDeviceItemNumberTitle.setVisibility(View.GONE);
        mDeviceItemNumber.setVisibility(View.GONE);

        mDeviceInstallDateTitle.setVisibility(View.GONE);
        mDeviceInstallDate.setVisibility(View.GONE);
    }

    private void hideView(boolean isHub){
        mDeviceCertificationTitle.setVisibility(isHub ? View.GONE : View.VISIBLE);
        mDeviceLastPairedTitle.setVisibility(isHub ? View.GONE : View.VISIBLE);

        mDeviceHubIDTitle.setVisibility(isHub ? View.VISIBLE : View.GONE);
        mDeviceInstallDateTitle.setVisibility(isHub ? View.VISIBLE : View.GONE);
        mDeviceLastContactTitle.setVisibility(isHub?View.VISIBLE:View.GONE);

        mDeviceCertification.setVisibility(isHub?View.GONE:View.VISIBLE);
        mDeviceLastPaired.setVisibility(isHub ? View.GONE : View.VISIBLE);

        mDeviceHubID.setVisibility(isHub?View.VISIBLE:View.GONE);
        mDeviceInstallDate.setVisibility(isHub ? View.VISIBLE : View.GONE);
        mDeviceLastContact.setVisibility(isHub ? View.VISIBLE : View.GONE);

        productBottom.setVisibility(isHub ? View.GONE : View.VISIBLE);

        firmwareTitle.setVisibility(isHub ? View.GONE : View.VISIBLE);
        deviceFirmware.setVisibility(isHub ? View.GONE : View.VISIBLE);
        productInfoMacContainer.setVisibility(isHub ? View.GONE : View.VISIBLE);
    }

    @NonNull
    @Override
    public String getTitle() {
        return "PRODUCT INFORMATION";
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_product_info;
    }

    private Bundle getNonNullArguments() {
        Bundle bundle = getArguments();
        if (bundle == null) {
            bundle = new Bundle();
        }
        return bundle;
    }

    private void getProductInformation(String productID) {
        ProductCatalog.GetProductRequest request = new ProductCatalog.GetProductRequest();
        request.setId(productID);
        request.setAddress(CorneaUtils.getServiceAddress(ProductCatalog.NAMESPACE, ""));

        CorneaClientFactory
              .getClient()
              .request(request)
              .onSuccess(successListener)
              .onFailure(failureListener);
    }

    private void updateProductInformation(@NonNull ProductModel model) {
        setProductName(model.getName());
        setProductManufacturer(model.getVendor());
        setProductCertification(model.getCert());
        setProductWireless(model.getProtoFamily());
    }

    private void setProductName(String value) {
        if (Strings.isNullOrEmpty(value)) {
            mDeviceProductName.setVisibility(View.GONE);
        }
        else {
            mDeviceProductName.setText(value);
        }
    }

    private void setProductManufacturer(String value) {
        if (Strings.isNullOrEmpty(value)) {
            mDeviceManufacturer.setVisibility(View.GONE);
            mDeviceManufacturerTitle.setVisibility(View.GONE);
        }
        else {
            mDeviceManufacturer.setText(value);
        }
    }

    private void setProductItemNumber(String value) {
        if (Strings.isNullOrEmpty(value)) {
            mDeviceItemNumber.setVisibility(View.GONE);
            mDeviceItemNumberTitle.setVisibility(View.GONE);
        }
        else {
            mDeviceItemNumberTitle.setVisibility(View.VISIBLE);
            mDeviceItemNumber.setText(value);
        }
    }

    private void setProductModelNumber(String value) {
        if (Strings.isNullOrEmpty(value)) {
            mDeviceModelNumberTitle.setVisibility(View.GONE);
            mDeviceModelNumber.setVisibility(View.GONE);
        }
        else {
            mDeviceModelNumber.setText(value);
        }
    }

    private void setProductCertification(String value) {
        if (Strings.isNullOrEmpty(value) || !ProductModel.CERT_WORKS.equals(value)) {
            mDeviceCertification.setVisibility(View.GONE);
            mDeviceCertificationTitle.setVisibility(View.GONE);
        }
        else {
            mDeviceCertification.setText(getString(R.string.works_with_arcus));
        }
    }

    private void setProductWireless(String value) {
        if (Strings.isNullOrEmpty(value)) {
            mDeviceWirelessTitle.setVisibility(View.GONE);
            mDeviceWireless.setVisibility(View.GONE);
        }
        else {
            mDeviceWireless.setText(value);
        }
    }
}

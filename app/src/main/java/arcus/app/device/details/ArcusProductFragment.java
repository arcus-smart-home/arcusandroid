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

import android.graphics.ColorFilter;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import arcus.cornea.dto.HubDeviceModelDTO;
import arcus.cornea.provider.ProductModelProvider;
import arcus.cornea.subsystem.connection.CellularBackup;
import arcus.cornea.subsystem.connection.model.CellBackupModel;
import arcus.cornea.utils.ProtocolTypes;
import arcus.cornea.utils.TemperatureUtils;
import com.iris.client.capability.Camera;
import com.iris.client.capability.Capability;
import com.iris.client.capability.Device;
import com.iris.client.capability.DeviceConnection;
import com.iris.client.capability.DeviceOta;
import com.iris.client.capability.DevicePower;
import com.iris.client.capability.HubPower;
import com.iris.client.capability.PowerUse;
import com.iris.client.capability.Product;
import com.iris.client.capability.Test;
import com.iris.client.event.ListenerRegistration;
import com.iris.client.model.DeviceModel;
import com.iris.client.model.HubModel;
import com.iris.client.model.Model;
import com.iris.client.model.ProductModel;
import arcus.app.R;
import arcus.app.common.banners.FirmwareUpdatingBanner;
import arcus.app.common.banners.NoConnectionBanner;
import arcus.app.common.banners.RunningOnBatteryBanner;
import arcus.app.common.banners.TextOnlyBanner;
import arcus.app.common.banners.core.BannerManager;
import arcus.app.common.fragments.BaseFragment;
import arcus.app.common.image.ImageManager;
import arcus.app.common.image.picasso.target.DeviceImageTarget;
import arcus.app.common.image.picasso.transformation.BlackWhiteInvertTransformation;
import arcus.app.common.image.picasso.transformation.CircularTransformation;
import arcus.app.common.image.picasso.transformation.Invert;
import arcus.app.common.models.SessionModelManager;
import arcus.app.common.utils.CorneaUtils;
import arcus.app.common.utils.DeviceSeekArc;
import arcus.app.common.utils.ImageUtils;
import arcus.app.common.utils.StringUtils;
import arcus.app.common.view.GlowableImageView;
import arcus.app.common.view.Version1TextView;
import arcus.app.device.adapter.DeviceDetailPagerAdapter;
import com.squareup.picasso.Target;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Date;

public abstract class ArcusProductFragment extends BaseFragment{

    private boolean isBottomViewAlerting = false;

    @Nullable
    public View mView;
    public ViewStub topViewStub;
    public ViewStub statusViewStub;
    public ViewStub deviceImageViewStub;
    public View topView;
    public View statusView;
    public View deviceImageView;
    public View bottomView;
    public ImageView productIcon;
    public ImageView cloudIcon;
    public TextView productBrandName;
    public GlowableImageView deviceImage;
    public DeviceSeekArc seekArc;
    public View justAMoment;
    public Version1TextView waitingLabel;

    public ImageView alarmIcon;
    private ImageView leftNav;
    private ImageView rightNav;

    private ViewPager viewPager;
    private DeviceModel deviceModel;

    public static final int LOW_CONNECTION_THRESHOLD = 15;
    public static final int DEVICE_IMAGE_SIZE_DP = 190;

    @NonNull
    private NumberFormat decimalFormat = new DecimalFormat("#.#");
    @NonNull
    private TemperatureDisplayType temperatureDisplayType = TemperatureDisplayType.FAHRENHEIT;

    public static final float BUTTON_ENABLED_ALPHA = 1.0f;
    public static final float BUTTON_DISABLED_ALPHA = 0.4f;

    private ListenerRegistration propertyChangeRegistry;

    public enum TemperatureDisplayType {
        FAHRENHEIT,
        CELSIUS
    }

    public abstract Integer topSectionLayout();
    public abstract void doTopSection();
    public abstract void doStatusSection();
    public abstract Integer statusSectionLayout();
    
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        decimalFormat.setMaximumFractionDigits(1);
        decimalFormat.setRoundingMode(RoundingMode.HALF_UP);

        mView = view;
        deviceImage = (GlowableImageView) view.findViewById(R.id.fragment_device_info_image);
        seekArc = (DeviceSeekArc) view.findViewById(R.id.seekArc);
        topViewStub = (ViewStub) view.findViewById(R.id.top_part);
        justAMoment = view.findViewById(R.id.loading_label);
        waitingLabel = (Version1TextView) view.findViewById(R.id.waiting_on_label_device_details);

        deviceImageViewStub = (ViewStub) view.findViewById(R.id.device_image_part);

        statusViewStub = (ViewStub) view.findViewById(R.id.device_status_part);

        bottomView = view.findViewById(R.id.device_bottom_part);

        cloudIcon = (ImageView) view.findViewById(R.id.cloud_icon);
        productIcon = (ImageView) view.findViewById(R.id.product_icon);
        productBrandName = (TextView) view.findViewById(R.id.product_brand_name);

        leftNav = (ImageView) view.findViewById(R.id.left_nav);
        rightNav = (ImageView) view.findViewById(R.id.right_nav);

        viewPager = (ViewPager) getActivity().findViewById(R.id.fragment_device_detail_child_view_pager);

        return view;
    }

    public DeviceModel getDeviceModel() {
        return deviceModel;
    }

    public void setDeviceModel(DeviceModel deviceModel) {
        this.deviceModel = deviceModel;
    }

    public Fragment getCurrentFragment(){
        final DeviceDetailPagerAdapter adapter = (DeviceDetailPagerAdapter) viewPager.getAdapter();
        return adapter.getFragment(viewPager.getCurrentItem());
    }

    private boolean isFromCurrentFragment(){
        final ArcusProductFragment fragment = (ArcusProductFragment) getCurrentFragment();
        return getDeviceModel().equals(fragment.getDeviceModel());
    }

    private void loadTopSection () {
        // Try to inflate the top section if it hasn't been already
        if(topSectionLayout() !=null){
            topViewStub.setLayoutResource(topSectionLayout());

            try {
                topView = topViewStub.inflate();
            } catch (IllegalStateException e) {}
        }

        // Initialize it if we were able to inflate it
        if (topView != null) {
            doTopSection();
        }
    }

    private void loadDeviceImageSection () {
        // If the device has a custom image section layout, use it...
        if(deviceImageSectionLayout() !=null){
            deviceImageViewStub.setLayoutResource(deviceImageSectionLayout());
        }

        // Otherwise, use the default layout
        else{
            deviceImageViewStub.setLayoutResource(R.layout.device_image_section);
        }

        // If the view hasn't already been inflated, inflate it
        try {
            deviceImageView = deviceImageViewStub.inflate();
        } catch (IllegalStateException e) {}

        // Initialize the image section layout
        doDeviceImageSection();
    }

    private void loadDeviceStatusSection () {
        // If the status view exists and hasn't been inflated, then inflate it
        if(statusSectionLayout() !=null){
            statusViewStub.setLayoutResource(statusSectionLayout());

            try {
                statusView = statusViewStub.inflate();
            } catch (IllegalStateException e) {}
        }

        // If if exists, initialize it
        if (statusView != null) {
            doStatusSection();
        }
    }

    private void load(){
        try {
            loadTopSection();
            loadDeviceImageSection();
            loadDeviceStatusSection();
            doBottomSection();
            checkFirmwareIsUpdating();
        }
        catch (Exception ex) {
            // Should not hit this now with load being in onResume(), but just in case.
            logger.debug("Exception loading product fragment.", ex);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        removePropertyChangeListener(getDeviceModel());
        if (BannerManager.in(getActivity()).containsBanner(FirmwareUpdatingBanner.class)) {
            BannerManager.in(getActivity()).removeBanner(FirmwareUpdatingBanner.class);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        load();
        addPropertyChangeListener(getDeviceModel());
        populateNav(viewPager.getCurrentItem());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        removePropertyChangeListener(getDeviceModel());
    }

    public void populateNav(final int position){

        ImageManager.with(getActivity())
                .putDrawableResource(R.drawable.chevron_white)
                .rotate(180)
                .into(leftNav)
                .execute();

        ImageManager.with(getActivity())
                .putDrawableResource(R.drawable.chevron_white)
                .into(rightNav)
                .execute();

        leftNav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPager.setCurrentItem(getPreviousDeviceIndex(), false);
            }
        });

        rightNav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPager.setCurrentItem(getNextDeviceIndex(), false);
            }
        });
    }

    private int getNextDeviceIndex () {
        int page = viewPager.getCurrentItem();
        return (page == SessionModelManager.instance().deviceCount(true) - 1) ? 0 : page + 1;
    }

    private int getPreviousDeviceIndex () {
        int page = viewPager.getCurrentItem();
        return (page == 0) ? SessionModelManager.instance().deviceCount(true) - 1 : page - 1;
    }

    public void setNavThumbsVisible (boolean visible) {
        if (leftNav != null) leftNav.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
        if (rightNav != null) rightNav.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
    }
    
    @Nullable
    public Integer deviceImageSectionLayout(){return null;}

    public void doDeviceImageSection(){
        deviceImage = (GlowableImageView) deviceImageView.findViewById(R.id.fragment_device_info_image);
        alarmIcon = (ImageView) deviceImageView.findViewById(R.id.alarm_icon);
        if (deviceModel == null) {
            return; // Can't do anything anyhow.
        }

        // TODO: Check ViewUtils method, it's got some "padding" in it to accommodate "unknown" devices.
        Target imageTarget = new DeviceImageTarget(deviceImage, getActivity());
        deviceImage.setTag(imageTarget);

        ImageManager.with(getActivity())
                .putLargeDeviceImage(deviceModel)
                .withTransformForUgcImages(new CircularTransformation())
                .resize(ImageUtils.dpToPx(getContext(), DEVICE_IMAGE_SIZE_DP), ImageUtils.dpToPx(getContext(), DEVICE_IMAGE_SIZE_DP))
                .into(deviceImage)
                .execute();
    }


    // TODO: Check ViewUtils method, it's got some "padding" in it to accommodate "unknown" devices.
    public void doBottomSection() {
        if (deviceModel == null) {
            return; // Can't do anything anyhow.
        }

        String vendorName = getVendorName();
        if (!TextUtils.isEmpty(vendorName) && ProtocolTypes.NEST.equals(vendorName)) {
            ImageManager.with(getActivity())
                    .putBrandImage(vendorName)
                    .into(productIcon)
                    .execute();
        } else if (!TextUtils.isEmpty(vendorName)) {
            ImageManager.with(getActivity())
                    .putBrandImage(vendorName)
                    .withTransform(new BlackWhiteInvertTransformation(Invert.BLACK_TO_WHITE))
                    .into(productIcon)
                    .execute();
        }
    }

    protected @Nullable String getVendorName() {
        if (deviceModel == null) {
            return null;
        }

        if (deviceModel instanceof HubModel) {
            return ProductModelProvider.HUB_VENDOR;
        }

        ProductModel loadedModel  = ProductModelProvider.instance().getByProductIDOrNull(deviceModel.getProductId());
        if (loadedModel == null) {
            CorneaUtils.logMissingProductCatalogID(deviceModel.getAddress(), deviceModel.getProductId());
            return ProductModelProvider.UNCERTIFIED;
        }

        if (!Product.CERT_NONE.equals(loadedModel.get(Product.ATTR_CERT))) {
            return String.valueOf(loadedModel.getVendor()).toUpperCase();
        }
        else {
            return ProductModelProvider.UNCERTIFIED;
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            //do nothing, allow onResume to handle it
        }
        else {
            if(deviceImage != null) {
                deviceImage.setImageDrawable(null);
            }
        }
    }

    /**
     * Gets the title of this product fragment screen; equal to the human readable name assigned
     * to the device by the user.
     *
     * @return The title of this product fragment.
     */
    public final String getTitle() {
        Device device = getCapability(Device.class);
        if (device == null) {
            logger.error("Should not be null at this point. Title could not be set.");
            return "";
        }

        String userAssignedDeviceName = device.getName();
        if (userAssignedDeviceName == null || userAssignedDeviceName.isEmpty()) {
            logger.error("Unable to determine name of device");
            userAssignedDeviceName = "(Untitled Device)";
        }

        return userAssignedDeviceName;
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_device_info;
    }

    /**
     * Convience method to get the {@link Capability} from the device if it is present.
     *
     * **Returns {@code null} if the capability is NOT present.
     *
     * @param cap Capability to get from the device model.
     * @param <T> Capability Class.
     *
     * @return Capability from the {@link #deviceModel} or {@code null}
     */
    @Nullable
    public final <T extends Capability> T getCapability(@NonNull Class<T> cap) {
        return CorneaUtils.getCapability(deviceModel, cap);
    }

    /**
     * Called when a property update event is caught. This is typically on a
     * {@link com.iris.client.model.DeviceModel} though can be used for other events.
     *
     * @param event
     */
    protected void propertyUpdated(@NonNull PropertyChangeEvent event) {
        logger.debug("Received update for [{}]::[{}]", event.getPropertyName(), event.getNewValue());

        if(!isFromCurrentFragment()) return;

        switch (event.getPropertyName()) {

            case DeviceOta.ATTR_STATUS:
                if (event.getNewValue().equals(DeviceOta.STATUS_INPROGRESS)) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override public void run() {
                            try {
                                displayDeviceFirmwareUpdatingBanner();
                                updateBackground(false);
                            }
                            catch (Exception ex) {
                                logger.error("Could not dispatch callback.", ex);
                            }
                        }
                    });
                } else {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override public void run() {
                            try {
                                BannerManager.in(getActivity()).removeBanner(FirmwareUpdatingBanner.class);
                                updateBackground(true);
                            }
                            catch (Exception ex) {
                                logger.error("Could not dispatch callback.", ex);
                            }
                        }
                    });
                }
                break;

            case DeviceConnection.ATTR_STATE:
                if (event.getNewValue().equals(DeviceConnection.STATE_OFFLINE)) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            displayNoConnectionBanner();
                            updateBackground(false);
                        }
                    });

                } else {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            deviceReconnected();
                            BannerManager.in(getActivity()).removeBanner(NoConnectionBanner.class);
                            updateBackground(true);
                        }
                    });

                }
                break;
            case DeviceConnection.ATTR_SIGNAL:
                try {
                    Double signalValue = Double.parseDouble(event.getNewValue().toString());
                    if (signalValue.intValue() <= LOW_CONNECTION_THRESHOLD) {
                        logger.debug(
                                "Signal is < 25%, Signal: [{}]",
                                event.getNewValue().toString());
                    }
                } catch (Exception ex) {
                    logger.error("Could not parse Integer, Ex: [{}], Value: [{}]", event.getNewValue());
                }
                break;
        }
    }

    /**
     * Attempts to set a property change listener for the {@link com.iris.client.model.Model}.
     * <p/>
     * This checks to see if the model is already registered and does nothing if it is.
     * If the device model is null the register fails safely and does not add a listener.
     */
    public void addPropertyChangeListener(@Nullable Model model) {
        if (propertyChangeRegistry != null && propertyChangeRegistry.isRegistered()) {
            return;
        }
        else if (model == null) {
            logger.debug("Cannot add property change listener to null model.");
            return;
        }

        propertyChangeRegistry = model.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(@NonNull PropertyChangeEvent e) {
                try {
                    if (!CorneaUtils.isInstanceCapabilityUpdate(e)) {
                        propertyUpdated(e);
                    }
                    else {
                        String cap = CorneaUtils.getInstancePropertyUpdatePropertyName(e);
                        String instanceName = CorneaUtils.getInstancePropertyUpdateInstanceName(e);
                        instancePropertyUpdated(instanceName, new PropertyChangeEvent(e.getSource(), cap, e.getOldValue(), e.getNewValue()));
                    }
                }
                catch (Exception ex) {
                    logger.error("Caught exception while trying to update property.", ex);
                }
            }
        });
    }

    /**
     * Remove the property change listener if the model's listener is registered.
     */
    public void removePropertyChangeListener(Model model) {
        if (propertyChangeRegistry == null || !propertyChangeRegistry.isRegistered()) {
            return;
        }

        propertyChangeRegistry.remove();
    }

    public void checkFirmwareIsUpdating () {
        if (deviceModel != null && DeviceOta.STATUS_INPROGRESS.equals(deviceModel.get(DeviceOta.ATTR_STATUS))) {
            BannerManager.in(getActivity()).showBanner(new FirmwareUpdatingBanner());
            setEnabled(false);
        } else {
            BannerManager.in(getActivity()).removeBanner(FirmwareUpdatingBanner.class);
        }
    }

    public void checkConnection() {

        /*
         * WARNING: This fragment may not be the fragment presently visible in the view pager. In
         * which case, do not update the UI based on connection.
         */
        if (getCurrentFragment() != this) {
            return;
        }

        BannerManager.in(getActivity()).removeBanner(TextOnlyBanner.class);

        // Current page is the Hub; get hub connection state
        if (getCurrentFragment() instanceof HubFragment) {
            checkHubConnection();
            return;
        }

        // Current page is a device page; get device connection state
        DeviceConnection connection = getCapability(DeviceConnection.class);
        if (connection == null) {
            return;
        }

        boolean isConnected = !DeviceConnection.STATE_OFFLINE.equals(getDeviceModel().get(DeviceConnection.ATTR_STATE));

        if (isConnected) {
            BannerManager.in(getActivity()).removeBanner(NoConnectionBanner.class);
            if (getCapability(Camera.class) != null) {
                CellBackupModel model = CellularBackup.instance().getStatus();
                isConnected = !model.cellularConnectionActive(); // Invert to disable the screen if we're on cell.
                if (model.cellularConnectionActive()) {
                    displayNoStreamingBanner();
                    topView.setVisibility(View.INVISIBLE);
                }
            }
        } else {
            displayNoConnectionBanner();
            topView.setVisibility(View.INVISIBLE);
        }

        updateBackground(isConnected);
    }

    public void checkPowerSource(){
        try {
            final HubModel hubModel = SessionModelManager.instance().getHubModel();
            if (hubModel == null) {
                return;
            }

            updatePowerOrBattery((String) hubModel.get(HubPower.ATTR_SOURCE));
        }
        catch (Exception e) {
            logger.error("Can't get hub power capability.", e);
        }
    }

    public void updatePowerOrBattery(@NonNull final String source){
        if(HubPower.SOURCE_BATTERY.equals(source)) {
            displayRunOnBatteryBanner();
        }
        else{
            BannerManager.in(getActivity()).removeBanner(RunningOnBatteryBanner.class);
        }
    }

    public void setEnabled (boolean isEnabled) {
        // When disabled, apply a grey overlay to the fragment
        applyGreyScale(!isEnabled);

        // Also disable any views on the page...
        setEnabledRecursively(viewPager, isEnabled);

        if (isEnabled) {
            load();
        }

        // ... except for the nav thumbs; they're always clickable
        this.leftNav.setEnabled(true);
        this.rightNav.setEnabled(true);
    }

    public void setEnabledNoLoad (boolean isEnabled) {
        // When disabled, apply a grey overlay to the fragment
        applyGreyScale(!isEnabled);

        // Also disable any views on the page...
        setEnabledRecursively(viewPager, isEnabled);
        // ... except for the nav thumbs; they're always clickable
        this.leftNav.setEnabled(true);
        this.rightNav.setEnabled(true);
    }

    /**
     * convert the current screen color to grey
     * @param apply apply grey scale or not
     */
    private void applyGreyScale(final boolean apply){

        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0f);
        final ColorFilter filter = new ColorMatrixColorFilter(cm);

        if (apply) {
            if (deviceImage != null) this.deviceImage.setColorFilter(filter);
            if (this.leftNav != null) this.leftNav.setColorFilter(null);
            if (this.rightNav != null) this.rightNav.setColorFilter(null);
            if (this.bottomView != null && !isBottomViewAlerting) this.bottomView.getBackground().setColorFilter(filter);
        } else {
            if (deviceImage != null) this.deviceImage.setColorFilter(null);
            if (this.leftNav != null) this.leftNav.setColorFilter(null);
            if (this.rightNav != null) this.rightNav.setColorFilter(null);
            if (this.bottomView != null && !isBottomViewAlerting) this.bottomView.getBackground().setColorFilter(null);
        }
    }

    /**
     * Recursively sets the enabled state on the given the view (and any child views).
     *
     * @param root
     * @param enabled
     */
    protected void setEnabledRecursively(@NonNull View root, boolean enabled) {
        root.setEnabled(enabled);

        if (root instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) root;
            for (int index = 0; index < group.getChildCount(); index++) {
                setEnabledRecursively(group.getChildAt(index), enabled);
            }
        } else {
            root.setAlpha(enabled ? BUTTON_ENABLED_ALPHA : BUTTON_DISABLED_ALPHA);
        }
    }

    /**
     *
     * Convience method used to update a text view with {@code replacementValue}.  This tries to set the
     * value directly, if unable, it tries to set on the UI thread.
     *
     * @param view the view to update
     * @param replacementValue the new value
     *
     */
    public final void updateTextView(@NonNull final TextView view, @Nullable final Object replacementValue) {
        try {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (replacementValue == null || String.valueOf(replacementValue).toLowerCase().startsWith("null")) {
                        view.setText(getString(R.string.unknown_value));
                    } else {
                        view.setText(replacementValue.toString());
                    }
                }
            });
        }
        catch (Exception ex) {
            logger.error("Could not updateTextView, Ex: [{}], Value: [{}]", getSimpleName(ex), replacementValue);
        }
    }

    public final void updateTextView(@NonNull TextView view, Date dateValue) {
        updateTextView(view, StringUtils.getTimestampString(dateValue));
    }

    @Nullable
    public final String getDecimalFormat(Object value) {
        try {
            return decimalFormat.format(value);
        }
        catch (Exception ex) {
            logger.error("Exception in getDecimalFormat, Ex: [{}], Value: [{}]", getSimpleName(ex), value);
            return null;
        }
    }

    public final void updateTemperatureTextView(@NonNull TextView view, Object doubleNumber) {
        try {
            Double number = Double.valueOf(String.valueOf(doubleNumber));
            if (temperatureDisplayType.equals(TemperatureDisplayType.FAHRENHEIT)) {
                updateTextView(view, decimalFormat.format(TemperatureUtils.celsiusToFahrenheit(number)) + (char) 0x00B0);
            }
            else {
                updateTextView(view, decimalFormat.format(number) + (char) 0x00B0);
            }
        }
        catch (Exception ex) {
            logger.error("Could not updateTemperatureTextView, Ex: [{}], Value: [{}]", getSimpleName(ex), doubleNumber);
        }
    }

    public void setImageGlowMode(GlowableImageView.GlowMode mode) {
        if (deviceImage != null) {
            deviceImage.setGlowMode(mode);
        }
        else {
            logger.warn("Cannot set glow mode on a null device image.");
        }
    }

    public void updateImageGlow() {
        try {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    deviceImage.setGlowing(shouldGlow());
                }
            });
        }
        catch (Exception ex) {
            logger.error("Could not change glow mode [{}] for device [{}]", shouldGlow(), getDeviceModel().getId());
        }
    }

    public boolean shouldGlow() {
        return false;
    }

    public void updatePowerSourceAndBattery(@Nullable TextView sourceTV, @Nullable TextView batteryTV) {
        if (getDeviceModel() instanceof HubDeviceModelDTO) {
            handleHubPowerSourceAndBattery(sourceTV, batteryTV);
        }
        else {
            if (sourceTV != null) {
                if (getDeviceModel().get(DevicePower.ATTR_SOURCE) != null) {
                    String sourceText = String.valueOf(getDeviceModel().get(DevicePower.ATTR_SOURCE));
                    updateTextView(sourceTV, sourceText.replace(DevicePower.SOURCE_LINE, getString(R.string.power)));
                }
                else {
                    updateTextView(sourceTV, getString(R.string.power));
                }
            }

            if (batteryTV == null) {
                return; // Nothing left to do.
            }

            if (String.valueOf(getDeviceModel().get(DevicePower.ATTR_SOURCE)).equals(DevicePower.SOURCE_LINE)) {
                updateTextView(batteryTV, getString(R.string.power_source_ac));
            } else {
                DevicePower power = getCapability(DevicePower.class);
                if (power != null && power.getBattery() != null) {
                    if (power.getBattery() > 30) {
                        updateTextView(batteryTV, "OK");
                    } else {
                        updateTextView(batteryTV, getDecimalFormat(power.getBattery()) + "%");
                    }
                } else {
                    updateTextView(batteryTV, getString(R.string.unknown_value));
                }
            }
        }
    }



    public void updatePowerSourceAndBatteryAndTemp(@Nullable TextView sourceTV, @Nullable TextView batteryTV, @Nullable TextView tempTopTV, @Nullable TextView tempBotomTV) {
        //make call to set the battery as required.
        updatePowerSourceAndBattery(sourceTV, batteryTV);
        updateTextView(tempTopTV, getActivity().getString(R.string.climate_keypad_temp));

        if (getDeviceModel().get(getActivity().getString(R.string.climate_keypad_temp_attribute)) != null) {
            Double celcValue = (Double) getDeviceModel().get(getActivity().getString(R.string.climate_keypad_temp_attribute));
            updateTextView(tempBotomTV, TemperatureUtils.roundCelsiusToFahrenheit(celcValue)+ " " + '\u00B0');
        }
        else {
            updateTextView(tempBotomTV, "unknown");
        }

    }

    private void handleHubPowerSourceAndBattery(@Nullable TextView sourceTV, @Nullable TextView batteryTV) {
        if (sourceTV != null) {
            if (getDeviceModel().get(HubPower.ATTR_SOURCE) != null) {
                String sourceText = String.valueOf(getDeviceModel().get(HubPower.ATTR_SOURCE));
                updateTextView(sourceTV, sourceText.replace(HubPower.SOURCE_MAINS, getString(R.string.power)));
            }
            else {
                updateTextView(sourceTV, getString(R.string.power));
            }
        }

        if (batteryTV == null) {
            return; // Nothing left to do.
        }

        if (String.valueOf(getDeviceModel().get(HubPower.ATTR_SOURCE)).equals(HubPower.SOURCE_MAINS)) {
            updateTextView(batteryTV, getString(R.string.power_source_ac));
        }
        else {
            if (getDeviceModel().get(HubPower.ATTR_BATTERY) != null) {
                if (Double.parseDouble(getDecimalFormat(getDeviceModel().get(HubPower.ATTR_BATTERY))) > 30) {
                    updateTextView(batteryTV, "OK");
                } else {
                    updateTextView(batteryTV, getDecimalFormat(getDeviceModel().get(HubPower.ATTR_BATTERY)) + "%");
                }
            }
            else {
                updateTextView(batteryTV, getString(R.string.unknown_value));
            }
        }
    }

    protected void updatePowerUsage(@NonNull final TextView powerUsageView) {

        double instantaneousPowerUsage = 0.0;

        PowerUse powerUse = getCapability(PowerUse.class);
        if (powerUse != null && powerUse.getInstantaneous() != null) {
            instantaneousPowerUsage = powerUse.getInstantaneous();
        }

        final double effectiveInstaneousPowerUsage = instantaneousPowerUsage;

        try {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        powerUsageView.setText(getDecimalFormat(effectiveInstaneousPowerUsage) + getString(R.string.power_units_abbrev));
                    }
                    catch (Exception ex) {
                        powerUsageView.setText("0" + getResourceString(R.string.power_units_abbrev));
                    }
                }
            });
        }
        catch (Exception ex) {
            logger.error("Could not updatePowerUsageWithValue, Ex: [{}], Value: [{}]", getSimpleName(ex), effectiveInstaneousPowerUsage);
        }
    }

    public void updateToggleButton(@NonNull final ToggleButton button, final boolean checked) {
        try {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    button.setChecked(checked);
                }
            });
        }
        catch (Exception ex) {
            logger.error("Could not updateToggleButton, Ex: [{}], Value: [{}]", getSimpleName(ex), checked);
        }
    }

    private String getSimpleName(@NonNull Throwable t) {
        return t.getClass().getSimpleName();
    }

    protected String getLastTested() {
        Test test = getCapability(Test.class);
        if (test != null && test.getLastTestTime() != null) {
            // Now - Test converted in seconds instead of milliseconds
            long differenceInSeconds = (System.currentTimeMillis() - test.getLastTestTime().getTime()) / 1000;
            int ONE_DAY_IN_SECONDS = 86400;
            int days = (int) (differenceInSeconds / ONE_DAY_IN_SECONDS);

            if (days < 1) {
                return getActivity().getResources().getString(R.string.smoke_detector_last_test_today);
            }
            else if (days < 2) {
                return getActivity().getResources().getString(R.string.smoke_detector_last_test_yesterday);
            }
            else {
                return getActivity().getResources().getQuantityString(R.plurals.days_plural, days, days);
            }
        }

        logger.debug("Cannot get last tested time. Capability/Time was NULL.");
        return "-";
    }

    // Determine if the buttons are clickable, if not, they should be "greyed" out.
    protected void setOpenCloseButtonEnabled(@NonNull ImageButton button, boolean enabled) {
        button.setAlpha(enabled ? BUTTON_ENABLED_ALPHA : BUTTON_DISABLED_ALPHA);
        button.setEnabled(enabled);
    }

    public boolean isBottomViewAlerting() {
        return isBottomViewAlerting;
    }

    public void setBottomViewAlerting(boolean bottomViewAlerting) {
        isBottomViewAlerting = bottomViewAlerting;
        if (isAdded()) {
            if (bottomViewAlerting) {
                bottomView.setBackgroundColor(getResources().getColor(R.color.pink_banner));
                bottomView.getBackground().setColorFilter(null);
            } else {
                bottomView.setBackgroundColor(getResources().getColor(R.color.overlay_white_with_20));
            }
        }
    }
}

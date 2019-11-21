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
package arcus.app.subsystems.lightsnswitches.adapter;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.h6ah4i.android.widget.advrecyclerview.draggable.DraggableItemAdapter;
import com.h6ah4i.android.widget.advrecyclerview.draggable.ItemDraggableRange;
import arcus.cornea.device.lightsnswitches.LightAndSwitchController;
import arcus.cornea.device.smokeandco.HaloContract;
import arcus.cornea.provider.DeviceModelProvider;
import arcus.cornea.subsystem.lightsnswitches.model.LightsNSwitchesDevice;
import com.iris.client.capability.Light;
import com.iris.client.model.DeviceModel;
import arcus.app.R;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.image.ImageManager;
import arcus.app.common.image.picasso.transformation.BlackWhiteInvertTransformation;
import arcus.app.common.image.picasso.transformation.CropCircleTransformation;
import arcus.app.common.image.picasso.transformation.Invert;
import arcus.app.common.models.SessionModelManager;
import arcus.app.common.popups.ColorTemperaturePopup;
import arcus.app.common.popups.InfoTextPopup;
import arcus.app.common.popups.NumberPickerPopup;
import arcus.app.common.utils.CorneaUtils;
import arcus.app.common.utils.DeviceSeekArc;
import arcus.app.common.utils.I2ColorUtils;
import arcus.app.common.utils.ImageUtils;
import arcus.app.common.view.GlowableImageView;
import arcus.app.device.details.DeviceDetailParentFragment;
import arcus.app.device.model.DeviceType;

import java.util.ArrayList;
import java.util.List;


public class LightsNSwitchesDeviceControlAdapter extends RecyclerView.Adapter<LightsNSwitchesDeviceControlViewHolder> implements DraggableItemAdapter<LightsNSwitchesDeviceControlViewHolder>  {

    private final static int MIN_PERCENTAGE = 0;
    private final static int MIN_SETTABLE_PERCENTAGE = 10;
    private final static int MAX_PERCENTAGE = 100;
    private final static int STEP_PERCENTAGE = 10;
    private final static String LUTRON = "LUTRON";

    private boolean isEditMode;
    private List<LightsNSwitchesDevice> devices;
    private Context context;

    private View.OnClickListener emptyClickListener = new View.OnClickListener() {
        public void onClick(View v) {
        }
    };

    public LightsNSwitchesDeviceControlAdapter(Context context, List<LightsNSwitchesDevice> devices) {
        this.devices = devices;
        this.context = context;

        setHasStableIds(true);
    }

    @Override
    public long getItemId(int position) {
        return System.identityHashCode(devices.get(position).getAddress());
    }

    public LightsNSwitchesDevice getItem(int position) {
        return devices.get(position);
    }

    @Override
    public LightsNSwitchesDeviceControlViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.
                from(parent.getContext()).
                inflate(R.layout.cell_lightsnswitches_switch, parent, false);

        return new LightsNSwitchesDeviceControlViewHolder(itemView);
    }

    protected void add(LightsNSwitchesDevice device) {
        if (devices == null) {
            devices = new ArrayList<>();
        }

        devices.add(device);

        notifyItemInserted(devices.size());
    }

    @Override
    public void onBindViewHolder(LightsNSwitchesDeviceControlViewHolder holder, int position) {
        LightsNSwitchesDevice device = devices.get(position);

        if (device.isOffline()) {
            bindOfflineDevice(device, holder);
            return;
        }

        holder.bannerServiceCardErrorContainer.setVisibility(View.GONE);
        holder.bannerDescription.setText("");
        holder.offlineDeviceName.setText(null);
        holder.onlineContainer.setVisibility(View.VISIBLE);
        holder.offlineContainer.setVisibility(View.GONE);
        holder.chevronClickRegion.setBackgroundColor(Color.TRANSPARENT);
        holder.colorTempSettings.setVisibility(View.GONE);
        holder.percentageArc.setLowArcColor(context.getResources().getColor(R.color.white_with_35));
        switch (device.getDeviceType()) {
            case LIGHT:
                if (device.isDimmable()) {
                    bindDimmerForDeviceType(device, position, holder, DeviceType.LIGHT);
                } else {
                    bindSwitchForDeviceType(device, position, holder, DeviceType.LIGHT);
                }
                break;

            case SWITCH:
                bindSwitchForDeviceType(device, position, holder, DeviceType.SWITCH);
                break;

            case HALO:
                bindDimmerForDeviceType(device, position, holder, DeviceType.HALO);
                break;
                
            case DIMMER:
                bindDimmerForDeviceType(device, position, holder, DeviceType.DIMMER);
                break;

            case UNKNOWN:
            default: // Can we not bind a blank cell here instead of possibly crashing the user every time they try to load this data?
                throw new IllegalStateException("Got lights and switches device of an unknown type; don't know how to render a cell for this device.");
        }

        if(!TextUtils.isEmpty(device.getErrorText())) {
            if (device.getErrorType().contains(LUTRON)) {
                bindOfflineDevice(device, holder);
                holder.fullBannerDescription.setText(device.getErrorText());
            }
            else {
                bindDeviceBanner(device, holder);
            }
        }
    }

    @Override
    public int getItemCount() {
        return devices.size();
    }

    @Override
    public boolean onCheckCanStartDrag(LightsNSwitchesDeviceControlViewHolder holder, int position, int x, int y) {
        return (isEditMode && ImageUtils.isRightOfView(x, holder.dragHandle, 20));
    }

    @Override
    public boolean onCheckCanDrop(int draggingPosition, int dropPosition) {
        return isEditMode;
    }


    @Override
    public ItemDraggableRange onGetItemDraggableRange(LightsNSwitchesDeviceControlViewHolder holder, int position) {
        // Any item can be dragged to any position; no range restrictions.
        return null;
    }

    @Override
    public void onMoveItem(int fromPosition, int toPosition) {
        if (fromPosition != toPosition) {
            LightsNSwitchesDevice device = devices.remove(fromPosition);
            devices.add(toPosition, device);

            LightsNSwitchesPreferenceDelegate.saveLightsAndSwitchesDeviceOrder(devices);
        }
    }

    public void setEditMode (boolean isEditMode) {
        this.isEditMode = isEditMode;
        notifyDataSetChanged();
    }

    private void bindOfflineDevice(final LightsNSwitchesDevice device, LightsNSwitchesDeviceControlViewHolder holder) {
        holder.onlineContainer.setVisibility(View.GONE);
        holder.offlineContainer.setVisibility(View.VISIBLE);
        holder.chevronClickRegion.setBackgroundColor(context.getResources().getColor(R.color.black_with_20));
        holder.chevronClickRegion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isEditMode) {
                    navigateToDevice(device);
                }
            }
        });

        holder.offlineDeviceName.setText(device.getDeviceName());

        if (device.isCloudDevice()) {
            holder.cloudIcon.setVisibility(View.VISIBLE);
        }
        else {
            holder.cloudIcon.setVisibility(View.GONE);
        }

        final DeviceModel deviceModel = DeviceModelProvider
                .instance()
                .getStore()
                .get(device.getDeviceId());

        if (deviceModel != null) {
            ImageManager
                    .with(context)
                    .putSmallDeviceImage(deviceModel)
                    .withTransformForUgcImages(new CropCircleTransformation())
                    .resize(ImageUtils.dpToPx(context, 45), ImageUtils.dpToPx(context, 45))
                    .withTransformForStockImages(new BlackWhiteInvertTransformation(Invert.BLACK_TO_WHITE))
                    .into(holder.offlineDeviceImage)
                    .execute();
        }
    }

    private void bindDeviceBanner(final LightsNSwitchesDevice device, LightsNSwitchesDeviceControlViewHolder holder) {
        holder.bannerServiceCardErrorContainer.setVisibility(View.VISIBLE);
        holder.bannerDescription.setText(device.getErrorText());
        holder.leftButton.setOnClickListener(emptyClickListener);
        holder.rightButton.setOnClickListener(emptyClickListener);
        holder.colorTempSettings.setOnClickListener(emptyClickListener);
    }


    private void bindSwitchForDeviceType (final LightsNSwitchesDevice switchDevice, final int position, final LightsNSwitchesDeviceControlViewHolder holder, DeviceType type) {

        holder.chevron.setVisibility(isEditMode ? View.GONE : View.VISIBLE);
        holder.dragHandle.setVisibility(isEditMode ? View.VISIBLE : View.GONE);

        holder.deviceName.setText(switchDevice.getDeviceName());
        holder.percentageArc.setVisibility(View.GONE);

        holder.leftButton.setText(context.getString(R.string.lightsnswitches_on));
        holder.leftButton.setEnabled(!switchDevice.isOn());

        holder.rightButton.setText(context.getString(R.string.lightsnswitches_off));
        holder.rightButton.setEnabled(switchDevice.isOn());

        holder.deviceImage.setGlowMode(GlowableImageView.GlowMode.ON_OFF);
        holder.deviceImage.setGlowing(switchDevice.isOn());
        holder.deviceImage.setBevelVisible(false);

        final DeviceModel model = DeviceModelProvider
                .instance()
                .getStore()
                .get(switchDevice.getDeviceId());

        ImageManager.with(context)
                .putSmallDeviceImage(model)
                .withTransformForUgcImages(new CropCircleTransformation())
                .resize(ImageUtils.dpToPx(context, 45), ImageUtils.dpToPx(context, 45))
                .withTransformForStockImages(new BlackWhiteInvertTransformation(Invert.BLACK_TO_WHITE))
                .into(holder.deviceImage)
                .execute();

        holder.chevronClickRegion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isEditMode) {
                    navigateToDevice(switchDevice);
                }
            }
        });

        holder.leftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSwitchState(true, switchDevice, position);
            }
        });

        holder.rightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSwitchState(false, switchDevice, position);
            }
        });
    }

    private void bindDimmerForDeviceType(final LightsNSwitchesDevice dimmerDevice, final int position, LightsNSwitchesDeviceControlViewHolder holder, DeviceType type) {

        holder.chevron.setVisibility(isEditMode ? View.GONE : View.VISIBLE);
        holder.dragHandle.setVisibility(isEditMode ? View.VISIBLE : View.GONE);

        holder.deviceName.setText(dimmerDevice.getDeviceName());

        holder.deviceImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isEditMode) {
                    navigateToDevice(dimmerDevice);
                }
            }
        });

        if((dimmerDevice.isColorTempChangeable() || dimmerDevice.isColorChangeable())) {
            holder.colorTempSettings.setVisibility(View.VISIBLE);
            holder.colorTempSettings.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(dimmerDevice != null) {
                        ColorTemperaturePopup colorTemperaturePopup;

                        if (dimmerDevice.getDeviceType() == LightsNSwitchesDevice.Type.HALO) {
                            colorTemperaturePopup = ColorTemperaturePopup.newHaloInstance(dimmerDevice.getDeviceId(), HaloContract.RED_BOUNDS_HSV, HaloContract.BG_BOUNDS_HSV);
                        } else {
                            colorTemperaturePopup = ColorTemperaturePopup.newInstance(dimmerDevice.getDeviceId());
                        }

                        BackstackManager.getInstance().navigateToFloatingFragment(colorTemperaturePopup, colorTemperaturePopup.getClass().getCanonicalName(), true);
                    }
                }
            });
        }

        if(Light.COLORMODE_COLOR.equals(dimmerDevice.getColorMode())) {
            float [] colorHSV = new float[] {dimmerDevice.getColorHue(), dimmerDevice.getColorSaturation(), I2ColorUtils.hsvValue};
            holder.percentageArc.setLowArcColor(Color.HSVToColor(colorHSV));
        }
        else if (Light.COLORMODE_COLORTEMP.equals(dimmerDevice.getColorMode())) {
            int temp = dimmerDevice.getColorTemp();
            int minTemp = dimmerDevice.getColorMinTemp();
            int maxTemp = dimmerDevice.getColorMaxTemp();

            float percentage = ((float) (temp - minTemp) / (float) (maxTemp - minTemp));
            float [] colorHSV = I2ColorUtils.getTemperatureColor3Point(percentage);
            holder.percentageArc.setLowArcColor(Color.HSVToColor(colorHSV));
        }

        holder.percentageArc.setVisibility(View.VISIBLE);
        holder.percentageArc.setSelectedResource(R.drawable.seek_arc_control_selector_small, null);
        holder.percentageArc.setProgress(DeviceSeekArc.THUMB_LOW, dimmerDevice.getDimPercent());
        holder.percentageArc.setDrawThumbsWhenDisabled(true);
        holder.percentageArc.setEnabled(false);
        holder.percentageArc.setArcWidth(ImageUtils.dpToPx(context, 5));
        holder.percentageArc.setProgressWidth(ImageUtils.dpToPx(context, 5));

        if (isDimmerOn(dimmerDevice)) {
            holder.leftButton.setText(context.getString(R.string.lightsnswitches_percentage, dimmerDevice.getDimPercent()));
        } else {
            holder.leftButton.setText(context.getString(R.string.lightsnswitches_on));
        }
        holder.leftButton.setEnabled(true);

        holder.rightButton.setText(context.getString(R.string.lightsnswitches_off));
        holder.rightButton.setEnabled(isDimmerOn(dimmerDevice));

        holder.deviceImage.setGlowMode(GlowableImageView.GlowMode.OFF);
        holder.deviceImage.setBevelVisible(false);

        ImageManager.with(context)
                .putSmallDeviceImage(type)
                .withTransformForStockImages(new BlackWhiteInvertTransformation(Invert.BLACK_TO_WHITE))
                .withTransformForUgcImages(new CropCircleTransformation())
                .resize(ImageUtils.dpToPx(context, 45), ImageUtils.dpToPx(context, 45))
                .into(holder.deviceImage)
                .execute();

        holder.chevronClickRegion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isEditMode) {
                    navigateToDevice(dimmerDevice);
                }
            }
        });

        holder.leftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isDimmerOn(dimmerDevice)) {
                    promptForDimmerPercentage(dimmerDevice, position);
                } else if (!isHaloAndIsOnBattery(dimmerDevice)) {
                    toggleDimmerSwitchState(dimmerDevice, position);
                }
            }
        });

        holder.rightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleDimmerSwitchState(dimmerDevice, position);
            }
        });
    }

    boolean isHaloAndIsOnBattery(LightsNSwitchesDevice device) {
        if (LightsNSwitchesDevice.Type.HALO.equals(device.getDeviceType())) {
            if (device.isOnBattery()) {
                Fragment f = InfoTextPopup.newInstance(R.string.halo_on_battery_popup_desc, R.string.halo_on_battery_popup_title);
                BackstackManager.getInstance().navigateToFloatingFragment(f, f.getClass().getCanonicalName(), true);
                return true;
            }
        }

        return false;
    }

    private void navigateToDevice (LightsNSwitchesDevice device) {
        int position = SessionModelManager.instance().indexOf(CorneaUtils.getIdFromAddress(device.getAddress()), true);
        if (position == -1) return;

        BackstackManager.getInstance()
                .navigateToFragment(DeviceDetailParentFragment.newInstance(position), true);
    }

    private void promptForDimmerPercentage (final LightsNSwitchesDevice device, final int inPosition) {
        NumberPickerPopup percentPicker = NumberPickerPopup.newInstance(NumberPickerPopup.NumberPickerType.PERCENT, MIN_SETTABLE_PERCENTAGE, MAX_PERCENTAGE, device.getDimPercent(), STEP_PERCENTAGE);
        percentPicker.setOnValueChangedListener(new NumberPickerPopup.OnValueChangedListener() {
            @Override
            public void onValueChanged(int value) {
                LightAndSwitchController.newController(device.getAddress(), null).setDimPercent(value);
                device.setDimPercent(value);
                refreshItem(inPosition);
            }
        });
        BackstackManager.getInstance().navigateToFloatingFragment(percentPicker, percentPicker.getClass().getSimpleName(), true);
    }

    private void setSwitchState (boolean on, LightsNSwitchesDevice device, int inPosition) {

        if (on) {
            LightAndSwitchController.newController(device.getAddress(), null).turnSwitchOn();
        }
        else {
            LightAndSwitchController.newController(device.getAddress(), null).turnSwitchOff();
        }

        device.setOn(on);
    }

    private void toggleDimmerSwitchState (LightsNSwitchesDevice device, int inPosition) {

        // Dimmer supports switch capability
        if (device.isSwitchable()) {
            setSwitchState(!device.isOn(), device, inPosition);
        }

        // Dimmer does not also support the switch capability
        else {
            if (device.getDimPercent() > 0) {
                device.setDimPercent(MIN_PERCENTAGE);
                LightAndSwitchController.newController(device.getAddress(), null).setDimPercent(MIN_PERCENTAGE);
            } else {
                // TODO: What to do in this case?
                device.setDimPercent(MAX_PERCENTAGE);
                LightAndSwitchController.newController(device.getAddress(), null).setDimPercent(MAX_PERCENTAGE);
            }
        }
    }

    private boolean isDimmerOn (LightsNSwitchesDevice dimmer) {
        if (dimmer.isSwitchable()) {
            return dimmer.isOn();
        } else {
            return dimmer.getDimPercent() > 0;
        }
    }

    public void refreshItem (final int inPosition) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                // Must be run in UI thread or causes list to scroll to the top
                notifyItemChanged(inPosition);
            }
        });
    }

    public void refreshItem(final int inPosition, final LightsNSwitchesDevice device) {
        try { // We should already be on the main thread when this is called..
            devices.set(inPosition, device);
            notifyItemChanged(inPosition);
        } catch (Exception ignored) {}
    }

    @Override
    public void onItemDragStarted(int position) {

    }

    @Override
    public void onItemDragFinished(int fromPosition, int toPosition, boolean result) {

    }
}

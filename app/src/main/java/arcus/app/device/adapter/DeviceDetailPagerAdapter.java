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
package arcus.app.device.adapter;

import android.app.Activity;
import android.content.Context;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.ViewPager;

import com.iris.client.capability.Dimmer;
import com.iris.client.capability.PowerUse;
import com.iris.client.capability.Temperature;
import com.iris.client.model.DeviceModel;
import arcus.app.activities.DashboardActivity;
import arcus.app.common.adapters.ArcusPagerAdapter;
import arcus.app.common.banners.NoConnectionBanner;
import arcus.app.common.banners.RunningOnBatteryBanner;
import arcus.app.common.banners.core.BannerManager;
import arcus.app.common.fragments.IClosedFragment;
import arcus.app.common.fragments.IShowedFragment;
import arcus.app.common.image.ImageManager;
import arcus.app.common.image.ImageSuccessCallback;
import arcus.app.common.image.picasso.transformation.AlphaPreset;
import arcus.app.common.models.SessionModelManager;
import arcus.app.device.details.AccessoryFragment;
import arcus.app.device.details.CameraFragment;
import arcus.app.device.details.CarePendantFragment;
import arcus.app.device.details.ContactSensorFragment;
import arcus.app.device.details.DeviceDetailFragment;
import arcus.app.device.details.DeviceNotSupportFragment;
import arcus.app.device.details.DimmerFragment;
import arcus.app.device.details.DoorLockFragment;
import arcus.app.device.details.HaloFragment;
import arcus.app.device.details.HueFallbackFragment;
import arcus.app.device.details.LutronBridgeFragment;
import arcus.app.device.details.ShadeFragment;
import arcus.app.device.details.SmokeDetectorFragment;
import arcus.app.device.details.SpaceHeaterFragment;
import arcus.app.device.details.FanFragment;
import arcus.app.device.details.GlassBreakFragment;
import arcus.app.device.details.HubFragment;
import arcus.app.device.details.ImageOnlyFragment;
import arcus.app.device.details.ArcusProductFragment;
import arcus.app.device.details.IrrigationFragment;
import arcus.app.device.details.KeyFobFragment;
import arcus.app.device.details.KeypadFragment;
import arcus.app.device.details.LightBulbFragment;
import arcus.app.device.details.LightSwitchFragment;
import arcus.app.device.details.MotionSensorFragment;
import arcus.app.device.details.PetDoorFragment;
import arcus.app.device.details.SirenFragment;
import arcus.app.device.details.SmartButtonFragment;
import arcus.app.device.details.SmartPlugFragment;
import arcus.app.device.details.SomfyBlindsFragment;
import arcus.app.device.details.ThermostatFragment;
import arcus.app.device.details.TiltSensorFragment;
import arcus.app.device.details.VentFragment;
import arcus.app.device.details.WaterHeaterFragment;
import arcus.app.device.details.WaterLeakFragment;
import arcus.app.device.details.WaterSoftenerFragment;
import arcus.app.device.details.WaterValveFragment;
import arcus.app.device.details.garage.GarageDoorControllerFragment;
import arcus.app.device.details.garage.GarageDoorFragment;
import arcus.app.device.model.DeviceType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static arcus.presentation.pairing.PairingConstants.BLE_GS_INDOOR_PLUG_PRODUCT_ID;
import static arcus.presentation.pairing.PairingConstants.BLE_GS_OUTDOOR_PLUG_PRODUCT_ID;


public class DeviceDetailPagerAdapter extends ArcusPagerAdapter {

    private List<DeviceModel> deviceModels;
    private int mCurrentSelectedPosition = 0;
    private int mLastSelectedPosition = 0;
    private Context context;
    private FragmentManager fragmentManager;
    private DeviceDetailFragment parent;


    public DeviceDetailPagerAdapter(DeviceDetailFragment fragment, FragmentManager fragmentManager, Context context, List<DeviceModel> deviceModels) {
        super(fragmentManager);

        this.context = context;
        this.fragmentManager = fragmentManager;
        this.parent = fragment;

        // Make sure that device models can never equal null
        if (deviceModels == null) this.deviceModels = new ArrayList<>();
        else this.deviceModels = deviceModels;
    }

    public void setUpPageListener(@NonNull final ViewPager viewPager) {
        CircularViewPagerHandler circularViewPagerHandler = new CircularViewPagerHandler(viewPager);
        viewPager.setOnPageChangeListener(circularViewPagerHandler);
    }

    public int getCurrentSelectedPosition() {
        return mCurrentSelectedPosition;
    }

    @Override
    public Fragment getItem(int position) {
        if(position >= deviceModels.size()) {
            return null;
        }
        return getFragmentForDevice(deviceModels.get(position));
    }

    @Override
    public int getCount() {
        return deviceModels.size();
    }

    private Fragment getFragmentForDevice(@NonNull DeviceModel device) {
        DeviceType type = DeviceType.fromHint(device.getDevtypehint());
        ArcusProductFragment product;
        switch (type) {
            case V1_RANGE_EXTENDER:
            case ACCESSORY:
                product = AccessoryFragment.newInstance();
                break;
            case BUTTON:
                product = SmartButtonFragment.newInstance();
                break;
            case CONTACT:
                product = ContactSensorFragment.newInstance();
                break;
            case DIMMER:
                product = DimmerFragment.newInstance();
                break;
            case LOCK:
                product = DoorLockFragment.newInstance();
                break;
            case FAN_CONTROL:
                product = FanFragment.newInstance();
                break;
            case MAIN_HUB:
                product = HubFragment.newInstance();
                break;
            case WATER_LEAK_SENSOR:
                product = WaterLeakFragment.newInstance();
                break;
            case MOTION_SENSOR:
                product = MotionSensorFragment.newInstance();
                break;
            case KEYFOB:
                product = KeyFobFragment.newInstance();
                break;
            case SWITCH:
                if (device.getCaps().contains(Temperature.NAMESPACE)) {
                    product = SmartPlugFragment.newInstance();
                } else if(device.getProductId().contains(BLE_GS_INDOOR_PLUG_PRODUCT_ID) ||
                        device.getProductId().contains(BLE_GS_OUTDOOR_PLUG_PRODUCT_ID)) {
                    product = LightSwitchFragment.newInstance(false);
                }
                else {
                    Collection<String> caps = device.getCaps();
                    product = LightSwitchFragment.newInstance(caps == null || caps.contains(PowerUse.NAMESPACE));
                }
                break;
            case SMOKECO:
                product = SmokeDetectorFragment.newInstance(true);
                break;
            case SMOKE:
                product = SmokeDetectorFragment.newInstance(false);
                break;
            case THERMOSTAT:
                product = ThermostatFragment.newInstance();
                break;
            case LIGHT:
                if (device.getCaps().contains(Dimmer.NAMESPACE)) {
                    product = DimmerFragment.newInstance();
                } else {
                    product = LightBulbFragment.newInstance();
                }
                break;
            case SIREN:
                product = SirenFragment.newInstance();
                break;
            case TILT_SENSOR:
                product = TiltSensorFragment.newInstance();
                break;
            case WATER_HEATER:
                product = WaterHeaterFragment.newInstance();
                break;
            case WATER_VALVE:
                product = WaterValveFragment.newInstance();
                break;
            case VENT:
                product = VentFragment.newInstance();
                break;
            case PENDANT:
                product = CarePendantFragment.newInstance();
                break;
            case GENIE_GARAGE_DOOR:
            case GARAGE_DOOR:
                product = GarageDoorFragment.newInstance();
                break;
            case GENIE_GARAGE_DOOR_CONTROLLER:
            case GARAGE_DOOR_CONTROLLER:
                product = GarageDoorControllerFragment.newInstance();
                break;
            case CAMERA:
                product = CameraFragment.newInstance();
                break;
            case GLASS_BREAK_DETECTOR:
                product = GlassBreakFragment.newInstance();
                break;
            case KEYPAD:
                product = KeypadFragment.newInstance();
                break;
            case IRRIGATION:
                product = IrrigationFragment.newInstance();
                break;
            case WATER_SOFTENER:
                product = WaterSoftenerFragment.newInstance();
                break;
            case PET_DOOR:
                product = PetDoorFragment.newInstance();
                break;
            case SOMFYV1BLINDS:
                product = SomfyBlindsFragment.newInstance();
                break;
            case SOMFYV1BRIDGE:
            case HUE_BRIDGE:
                product = ImageOnlyFragment.newInstance();
                break;
            case SPACE_HEATER:
                product = SpaceHeaterFragment.newInstance();
                break;
            case HALO:
                product = HaloFragment.newInstance();
                break;
            case SHADE:
                product = ShadeFragment.newInstance();
                break;
            case HUE_FALLBACK:
                product = HueFallbackFragment.newInstance();
                break;
            case LUTRON_BRIDGE:
                product = LutronBridgeFragment.newInstance();
                break;
            default:
                //if can't match, show device not support fragment
                product = DeviceNotSupportFragment.newInstance();
                break;
        }

        product.setDeviceModel(device);
        product.setBaseFragmentCallback(parent);
        return product;
    }

    public void removeDevice(@NonNull final DeviceModel model) {
        for (DeviceModel contextModel : deviceModels) {
            if (contextModel.getId().equals(model.getId())) {
                deviceModels.remove(contextModel);
                break;
            }
        }

        notifyDataSetChanged();
    }

    @Nullable
    @Override
    public Parcelable saveState() {
        return null;
    }

    @Override
    public int getItemPosition(Object object) {
        final ArcusProductFragment fragment = (ArcusProductFragment) object;
        if (fragmentManager.getFragments().contains(fragment)) {
            return POSITION_NONE;
        } else {
            return POSITION_UNCHANGED;
        }
    }

    public void updatePageUI(int currentPosition, int lastPosition) {
        final DashboardActivity activity = (DashboardActivity) context;
        final ArcusProductFragment currentFragment = (ArcusProductFragment) getFragment(currentPosition);
        final ArcusProductFragment lastFragment = (ArcusProductFragment) getFragment(lastPosition);

        if (currentFragment != null) {
            activity.setTitle(currentFragment.getTitle());
            activity.invalidateOptionsMenu();
            if (SessionModelManager.instance().deviceCount(true) > 1) {
                currentFragment.populateNav(currentPosition);
            }

            ImageManager.with(context)
                    .putDeviceBackgroundImage(deviceModels.get(currentPosition))
                    .withSuccessCallback(new ImageSuccessCallback() {
                        @Override
                        public void onImagePlacementSuccess() {
                            currentFragment.checkConnection();
                        }
                    })
                    .intoWallpaper(AlphaPreset.DARKEN)
                    .execute();

            if (currentFragment instanceof HubFragment) {
                BannerManager.in(activity).removeBanner(NoConnectionBanner.class);
                currentFragment.checkPowerSource();
                currentFragment.checkHubConnection();
            } else {

                currentFragment.checkFirmwareIsUpdating();

                if (context != null) {
                    BannerManager.in((Activity) context).removeBanner(RunningOnBatteryBanner.class);
                }

                if (!(currentFragment instanceof KeyFobFragment) && !(currentFragment instanceof CarePendantFragment)) {
                    currentFragment.checkConnection();
                }
            }

        }

        if (lastFragment instanceof IClosedFragment && currentPosition != lastPosition) {
            ((IClosedFragment) lastFragment).onClosedFragment();
        }

        if (currentFragment instanceof IShowedFragment) {
            ((IShowedFragment) currentFragment).onShowedFragment();
        }

    }

    public class CircularViewPagerHandler implements ViewPager.OnPageChangeListener {
        private ViewPager mViewPager;
        private int mCurrentPosition;
        private int mScrollState;

        public CircularViewPagerHandler(final ViewPager viewPager) {
            mViewPager = viewPager;
        }

        @Override
        public void onPageSelected(final int position) {
            mCurrentPosition = position;
            mLastSelectedPosition = mCurrentSelectedPosition;
            mCurrentSelectedPosition = position;
            updatePageUI(mCurrentSelectedPosition, mLastSelectedPosition);
        }

        @Override
        public void onPageScrollStateChanged(final int state) {
            handleScrollState(state);
            mScrollState = state;

            int nextIndex = mCurrentPosition + 1 == getCount() ? 0 : mCurrentPosition + 1;
            int prevIndex = mCurrentPosition - 1 < 0 ? getCount() - 1 : mCurrentPosition - 1;

            final ArcusProductFragment fragment = (ArcusProductFragment) instantiateItem(mViewPager, mCurrentSelectedPosition);
            final ArcusProductFragment next = (ArcusProductFragment) instantiateItem(mViewPager, nextIndex);
            final ArcusProductFragment prev = (ArcusProductFragment) instantiateItem(mViewPager, prevIndex);

            switch (state) {
                case ViewPager.SCROLL_STATE_DRAGGING:
                    fragment.setNavThumbsVisible(false);
                    next.setNavThumbsVisible(false);
                    prev.setNavThumbsVisible(false);
                    break;
                case ViewPager.SCROLL_STATE_IDLE:
                    fragment.setNavThumbsVisible(true);
                    next.setNavThumbsVisible(true);
                    prev.setNavThumbsVisible(true);
                    break;
                case ViewPager.SCROLL_STATE_SETTLING:
                    fragment.setNavThumbsVisible(false);
                    next.setNavThumbsVisible(false);
                    prev.setNavThumbsVisible(false);
                    break;
                default:
                    fragment.setNavThumbsVisible(true);
                    next.setNavThumbsVisible(true);
                    prev.setNavThumbsVisible(true);
                    break;
            }
        }

        private void handleScrollState(final int state) {
            if (state == ViewPager.SCROLL_STATE_IDLE) {
                setNextItemIfNeeded();
            }
        }

        private void setNextItemIfNeeded() {
            if (!isScrollStateSettling()) {
                handleSetNextItem();
            }
        }

        private boolean isScrollStateSettling() {
            return mScrollState == ViewPager.SCROLL_STATE_SETTLING;
        }

        private void handleSetNextItem() {
            final int lastPosition = mViewPager.getAdapter().getCount() - 1;
            if (mCurrentPosition == 0) {
                mViewPager.setCurrentItem(lastPosition, false);
            } else if (mCurrentPosition == lastPosition) {
                mViewPager.setCurrentItem(0, false);
            }
        }

        @Override
        public void onPageScrolled(final int position, final float positionOffset, final int positionOffsetPixels) {
        }
    }
}

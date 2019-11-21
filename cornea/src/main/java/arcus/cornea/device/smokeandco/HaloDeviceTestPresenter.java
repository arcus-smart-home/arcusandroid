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
package arcus.cornea.device.smokeandco;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;

import com.google.common.collect.ImmutableSet;
import arcus.cornea.CorneaClientFactory;
import arcus.cornea.provider.DeviceModelProvider;
import arcus.cornea.utils.Listeners;
import com.iris.client.capability.DeviceAdvanced;
import com.iris.client.capability.Halo;
import com.iris.client.capability.Test;
import com.iris.client.event.ListenerRegistration;
import com.iris.client.model.DeviceModel;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Date;
import java.util.Locale;
import java.util.Set;

public class HaloDeviceTestPresenter extends BaseHaloPresenter implements HaloContract.DeviceTestPresenter {
    static final Set<String> UPDATE_ON = ImmutableSet.of(
          Test.ATTR_LASTTESTTIME,
          Halo.ATTR_REMOTETESTRESULT
    );

    @SuppressWarnings("FieldCanBeLocal") private final String DATE_FORMAT = "%1$tb %1$te, %1$tY";
    private final HaloController haloController;
    private ListenerRegistration listenerRegistration;
    private Reference<HaloContract.View> viewRef = new WeakReference<>(null);

    public HaloDeviceTestPresenter(String deviceAddress) {
        this(new HaloController(
              DeviceModelProvider.instance().getModel(deviceAddress == null ? "DRIV:dev:" : deviceAddress),
              CorneaClientFactory.getClient(), // This probably shouldn't be exposed to the presenter...
              UPDATE_ON
        ));
    }

    @VisibleForTesting HaloDeviceTestPresenter(HaloController haloController) {
        this.haloController = haloController;
    }

    @Override public void testDevice() {
        haloController.testDevice();
    }

    @Override public void startPresenting(HaloContract.View view) {
        this.viewRef = new WeakReference<>(view);
        listenerRegistration = this.haloController.setCallback(getHaloCallback());
    }

    @Override public void stopPresenting() {
        Listeners.clear(listenerRegistration);
        viewRef.clear();
    }

    @Override protected HaloContract.View getView() {
        return viewRef.get();
    }

    @Override @SuppressWarnings("ConstantConditions") protected @NonNull HaloModel buildModel(@NonNull DeviceModel halo) {
        if (halo == null) {
            return HaloModel.empty();
        }

        HaloModel haloModel = new HaloModel(halo.getAddress());

        Number testedAt = numberOrNull(halo.get(Test.ATTR_LASTTESTTIME));
        String tested = null;
        if (testedAt != null) {
            tested = String.format(Locale.ROOT, DATE_FORMAT, new Date(testedAt.longValue()));
        }

        haloModel.setLastTested(tested);
        haloModel.setLastTestPassed(Halo.REMOTETESTRESULT_SUCCESS.equals(halo.get(Halo.ATTR_REMOTETESTRESULT)));
        if (haloModel.isLastTestPassed()) {
            haloModel.setLastTestResult("");
        } else {
            haloModel.setLastTestResult(errorStringValuesFrom(halo));
        }

        return haloModel;
    }

    private String errorStringValuesFrom(@NonNull DeviceModel halo) {
        if (!(halo instanceof DeviceAdvanced)) {
            return "";
        }

        return (String)halo.get(Halo.ATTR_REMOTETESTRESULT);
    }
}

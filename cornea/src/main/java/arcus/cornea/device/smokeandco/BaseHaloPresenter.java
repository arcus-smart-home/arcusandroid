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
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import arcus.cornea.utils.LooperExecutor;
import arcus.cornea.utils.TemperatureUtils;
import arcus.cornea.utils.WrappedRunnable;
import com.iris.client.model.DeviceModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;

abstract class BaseHaloPresenter {
    private static final Logger logger = LoggerFactory.getLogger(BaseHaloPresenter.class);
    private final HaloController.Callback haloCallback = new HaloController.Callback() {
        @Override public void onError(final Throwable throwable) {
            final HaloContract.View view = getView();
            if (view != null) {
                LooperExecutor.getMainExecutor().execute(new WrappedRunnable() {
                    @Override public void onRun() throws Exception {
                        view.onError(throwable);
                    }
                });
            }
        }

        @Override public void onSuccess(DeviceModel deviceModel) {
            final HaloContract.View view = getView();
            if (view != null) {
                final HaloModel haloModel = buildModel(deviceModel);
                LooperExecutor.getMainExecutor().execute(new WrappedRunnable() {
                    @Override public void onRun() throws Exception {
                        view.updateView(haloModel);
                    }
                });
            }
        }
    };

    protected @NonNull String tempString(@Nullable Object value) {
        Number temp = numberOrNull(value);

        if (temp != null) {
            return String.format(Locale.ROOT, "%d", TemperatureUtils.roundCelsiusToFahrenheit(temp.doubleValue()));
        }

        return "-";
    }

    protected String stringWithMultiplier(@Nullable Object value, boolean withDecimalPlaces, double multiplier) {
        Number number = numberOrNull(value);

        if (number != null) {
            if (withDecimalPlaces) {
                return String.format(Locale.ROOT, "%.01f", number.doubleValue() * multiplier);
            }
            else {
                return String.format(Locale.ROOT, "%.0f", number.doubleValue() * multiplier);
            }
        }

        return "-";
    }

    protected Number numberOrNull(Object thing) {
        try {
            return (Number) thing;
        }
        catch (Exception ex) {
            logger.error("Could not coerce [{}] to number.", thing);
        }

        return null;
    }

    protected Collection nonNullCollection(Collection collection) {
        return collection == null ? Collections.emptySet() : collection;
    }

    @SuppressWarnings("unchecked") protected Map<String, Object> mapOrNull(Object value) {
        try {
            return (Map<String, Object>) value;
        }
        catch (Exception ex) {
            logger.debug("Could not convert to map.", ex);
            return null;
        }
    }

    @VisibleForTesting protected HaloController.Callback getHaloCallback() {
        return haloCallback;
    }

    @VisibleForTesting protected abstract HaloContract.View getView();
    protected abstract @NonNull HaloModel buildModel(@NonNull DeviceModel deviceModel);
}

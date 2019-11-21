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
package arcus.app.device.more.presenter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import arcus.cornea.device.smokeandco.WeatherAlertModel;
import arcus.cornea.provider.DeviceModelProvider;
import arcus.cornea.provider.EASCodeProvider;
import arcus.cornea.utils.LooperExecutor;
import arcus.cornea.utils.ModelSource;
import arcus.cornea.utils.WrappedRunnable;
import com.iris.client.bean.EasCode;
import com.iris.client.capability.WeatherRadio;
import com.iris.client.event.Listener;
import com.iris.client.model.DeviceModel;
import arcus.app.device.more.view.WeatherAlertCategoryView;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class WeatherAlertCategoryPresenterImpl implements WeatherAlertCategoryPresenter {
    private Reference<WeatherAlertCategoryView> viewRef = new WeakReference<>(null);
    private volatile ModelSource<DeviceModel> weatherDevice;

    public WeatherAlertCategoryPresenterImpl(@NonNull String weatherDeviceAddress) {
        weatherDevice = DeviceModelProvider.instance().getModel(weatherDeviceAddress);
        weatherDevice.load();
    }

    @Override
    public void startPresenting(WeatherAlertCategoryView view) {
        this.viewRef = new WeakReference<>(view);
    }

    @Override
    public void stopPresenting() {
        this.viewRef.clear();
    }

    @Override
    public void getEASCodes() {
        DeviceModel deviceModel = getDevice();
        if (deviceModel != null) {
            getEasCodes(deviceModel);
        } else {
            weatherDevice.load().onSuccess(new Listener<DeviceModel>() {
                @Override
                public void onEvent(DeviceModel deviceModel) {
                    getEasCodes(deviceModel);
                }
            });
        }
    }

    @Override
    public void setSelections(@NonNull final List<WeatherAlertModel> selectedEASAlerts) {
        DeviceModel deviceModel = getDevice();
        if (deviceModel != null) {
            setEASCodes(selectedEASAlerts, deviceModel);
        } else {
            weatherDevice.load().onSuccess(new Listener<DeviceModel>() {
                @Override
                public void onEvent(DeviceModel deviceModel) {
                    setEASCodes(selectedEASAlerts, deviceModel);
                }
            });
        }
    }

    protected void setEASCodes(@NonNull List<WeatherAlertModel> selectedEASAlerts, @NonNull DeviceModel deviceModel) {
        Set<String> selected = new HashSet<>(selectedEASAlerts.size() + 1);
        for (WeatherAlertModel selectedEASAlert : selectedEASAlerts) {
            if (!selectedEASAlert.isInfoItem && !selectedEASAlert.isHeader) {
                selected.add(selectedEASAlert.itemCode);
            }
        }

        deviceModel.set(WeatherRadio.ATTR_ALERTSOFINTEREST, selected);
        deviceModel.commit()
              .onFailure(new Listener<Throwable>() {
                  @Override
                  public void onEvent(Throwable throwable) {
                      showError(throwable);
                  }
              });
    }

    protected void getEasCodes(final DeviceModel deviceModel) {
        EASCodeProvider.instance().load()
              .onSuccess(new Listener<List<EasCode>>() {
                  @Override
                  public void onEvent(List<EasCode> easCodes) {
                      int initialSize = (easCodes.size() / 3) + 1;
                      List<WeatherAlertModel> selected = new ArrayList<>(initialSize);
                      List<WeatherAlertModel> popular = new ArrayList<>(initialSize);
                      List<WeatherAlertModel> other = new ArrayList<>(initialSize);

                      Collection<String> currentOnDevice = collectionOrEmpty(deviceModel.get(WeatherRadio.ATTR_ALERTSOFINTEREST));
                      for (EasCode easCode : easCodes) {
                          WeatherAlertModel model = new WeatherAlertModel(easCode.getEas());
                          model.mainText = easCode.getName();
                          model.isPopular = easCode.getGroup().toLowerCase().startsWith("p");

                          if (currentOnDevice.contains(easCode.getEas())) {
                              model.isChecked = true;
                              selected.add(model);
                          } else if (model.isPopular) {
                              popular.add(model);
                          } else {
                              other.add(model);
                          }
                      }

                      Collections.sort(selected);
                      Collections.sort(popular);
                      Collections.sort(other);

                      showEasCodes(selected, popular, other);
                  }
              });
    }

    @Nullable
    protected DeviceModel getDevice() {
        if (!weatherDevice.isLoaded()) {
            return null;
        } else {
            return weatherDevice.get();
        }
    }

    protected void showError(final Throwable throwable) {
        final WeatherAlertCategoryView view = viewRef.get();
        if (view != null) {
            LooperExecutor.getMainExecutor().execute(new WrappedRunnable() {
                @Override
                public void onRun() throws Exception {
                    view.onError(throwable);
                }
            });
        }
    }

    protected void showEasCodes(@NonNull final List<WeatherAlertModel> selected,
                                @NonNull final List<WeatherAlertModel> popular,
                                @NonNull final List<WeatherAlertModel> other
    ) {
        final WeatherAlertCategoryView view = viewRef.get();
        if (view != null) {
            LooperExecutor.getMainExecutor().execute(new WrappedRunnable() {
                @Override
                public void onRun() throws Exception {
                    view.currentSelections(selected, popular, other);
                }
            });
        }
    }

    @SuppressWarnings("unchecked")
    protected Collection<String> collectionOrEmpty(Object fromThis) {
        if (fromThis == null) {
            return Collections.emptySet();
        } else {
            try {
                return (Collection<String>) fromThis;
            } catch (Exception ex) {
                return Collections.emptySet();
            }
        }
    }
}

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

import android.app.Activity;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;

import arcus.cornea.device.smokeandco.WeatherAlertModel;
import arcus.app.R;
import arcus.app.common.error.ErrorManager;
import arcus.app.common.fragments.BaseFragment;
import arcus.app.common.adapters.decorator.HeaderDecorator;
import arcus.app.device.adapter.WeatherAlertCategoryAdapter;
import arcus.app.device.more.presenter.WeatherAlertCategoryPresenter;
import arcus.app.device.more.presenter.WeatherAlertCategoryPresenterImpl;
import arcus.app.device.more.view.WeatherAlertCategoryView;

import java.util.List;

public class WeatherAlertCategorySelection extends BaseFragment implements WeatherAlertCategoryView, WeatherAlertCategoryAdapter.Callback {
    public static final String DEVICE_ADDRESS = "DEVICE_ADDRESS";
    private WeatherAlertCategoryPresenter presenter;

    public static WeatherAlertCategorySelection newInstance(String deviceAddress) {
        WeatherAlertCategorySelection fragment = new WeatherAlertCategorySelection();

        Bundle args = new Bundle();
        args.putString(DEVICE_ADDRESS, deviceAddress);
        fragment.setArguments(args);

        return fragment;
    }

    private RecyclerView weatherAlerts;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (view == null) {
            return;
        }

        weatherAlerts = (RecyclerView) view.findViewById(R.id.weather_alerts_list);
        weatherAlerts.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

        Bundle bundle = getArguments();
        if (bundle != null) {
            presenter = new WeatherAlertCategoryPresenterImpl(bundle.getString(DEVICE_ADDRESS, ""));
            presenter.startPresenting(this);
            presenter.getEASCodes();
        }
    }


    @Override
    public void onResume () {
        super.onResume();
        Activity activity = getActivity();
        if (activity != null) {
            activity.setTitle(getTitle());
        }
    }

    @Nullable
    @Override
    public String getTitle() {
        return getString(R.string.weather_alerts_text).toUpperCase();
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.weather_alert_category_selection;
    }

    @Override
    public void currentSelections(List<WeatherAlertModel> current, List<WeatherAlertModel> popular, List<WeatherAlertModel> other) {
        current.add(0, WeatherAlertModel.headerModelType(getString(R.string.my_alerts)));
        popular.add(0, WeatherAlertModel.headerModelType(getString(R.string.popular_alerts)));
        other.add(0, WeatherAlertModel.headerModelType(getString(R.string.more_alerts)));

        WeatherAlertCategoryAdapter adapter = new WeatherAlertCategoryAdapter(current, popular, other, getNotCheckedItem(), getContext());
        adapter.setCallback(this);

        weatherAlerts.setAdapter(adapter);
        weatherAlerts.addItemDecoration(new HeaderDecorator(1));
    }

    @Override
    public void onError(Throwable throwable) {
        ErrorManager.in(getActivity()).got(throwable);
    }

    /**
     * The item that is shown if no alerts at all are selected
     *
     * @return
     */
    WeatherAlertModel getNotCheckedItem() {
        return WeatherAlertModel.infoModelType(getString(R.string.no_selected_alerts_desc));
    }

    @Override
    public void updateSelected(List<WeatherAlertModel> checkedItems) {
        presenter.setSelections(checkedItems);
    }
}

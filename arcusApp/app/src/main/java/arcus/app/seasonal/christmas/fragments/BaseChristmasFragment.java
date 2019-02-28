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
package arcus.app.seasonal.christmas.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import arcus.cornea.CorneaClientFactory;
import arcus.app.R;
import arcus.app.common.fragments.BaseFragment;
import arcus.app.seasonal.christmas.model.ChristmasModel;
import arcus.app.seasonal.christmas.util.ChristmasModelUtils;
import arcus.app.common.utils.GlobalSetting;
import arcus.app.common.utils.ImageUtils;
import arcus.app.common.utils.StringUtils;

import java.io.Serializable;

public abstract class BaseChristmasFragment extends BaseFragment {
    public static final String MODEL = "MODEL_ID";

    @Override @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (getLayoutId() != null) {
            setHasOptionsMenu(true);
            return inflater.inflate(getLayoutId(), container, false);
        }

        return null;
    }

    protected ChristmasModel getDataModel() {
        if (getArguments() != null) {
            Serializable model = getArguments().getSerializable(MODEL);
            if (model != null && model instanceof ChristmasModel) {
                return (ChristmasModel) model;
            }
        }

        return new ChristmasModel();
    }

    protected void saveModelToFragment(ChristmasModel model) {
        if (model == null) {
            return;
        }

        Bundle bundle = getArguments();
        if (bundle == null) {
            bundle = new Bundle(1);
            bundle.putSerializable(MODEL, model);
            setArguments(bundle);
        }

        bundle.remove(MODEL);
        bundle.putSerializable(MODEL, model);
    }

    protected boolean saveModelToDisk(ChristmasModel model) {
        if (model == null) {
            return false;
        }

        return ChristmasModelUtils.cacheModelToDisk(model);
    }

    public String getBaseUrl() {
        String defaultURL = GlobalSetting.IMAGE_SERVER_BASE_URL;
        try {
            return CorneaClientFactory.getClient().getSessionInfo().getStaticResourceBaseUrl();
        }
        catch (Exception ex) {
            return defaultURL;
        }
    }

    protected String getDeviceImageURL(String deviceTypeHint) {
        return String.format("%s/o/products/%s/product_small-and-%s.png",
              getBaseUrl(),
              StringUtils.sanitize(deviceTypeHint),
              ImageUtils.getScreenDensity());
    }

    @Override
    public void onAttach(@NonNull Activity activity) {
        super.onAttach(activity);

        String title = getTitle();
        if (title != null) {
            activity.setTitle(Html.fromHtml(title));
        }
    }

    @Override @Nullable
    public String getTitle() {
        return getString(R.string.santa_tracker_title);
    }
}

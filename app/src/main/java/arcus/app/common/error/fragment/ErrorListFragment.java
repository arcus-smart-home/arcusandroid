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
package arcus.app.common.error.fragment;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import arcus.app.R;
import arcus.app.activities.DashboardActivity;
import arcus.app.common.adapters.ErrorListDataAdapter;
import arcus.app.common.fragments.BaseFragment;
import arcus.app.common.image.ImageManager;
import arcus.app.common.image.picasso.transformation.AlphaPreset;
import arcus.app.common.image.picasso.transformation.GreyScaleTransformation;
import arcus.app.common.models.RegistrationContext;
import arcus.app.common.models.FullScreenErrorModel;
import arcus.app.common.utils.GlobalSetting;
import arcus.app.common.view.Version1TextView;
import arcus.app.device.model.DeviceType;

import java.util.ArrayList;
import java.util.List;

public class ErrorListFragment extends BaseFragment {

    private static final String LIST = "LIST";
    private static final String HIDE_TITLE = "HIDE_TITLE";
    private static final String CUSTOM_TITLE = "CUSTOM_TITLE";
    private static final String DEVTYPE = "DEVTYPE";

    @Nullable private ArrayList<FullScreenErrorModel> mData;
    private boolean bHideTitle = false;
    private String devType;
    private String customTitle;

    @NonNull
    public static ErrorListFragment newInstance(ArrayList<FullScreenErrorModel> listdata) {
        ErrorListFragment fragment = new ErrorListFragment();

        Bundle bundle = new Bundle();
        bundle.putSerializable(LIST, listdata);
        fragment.setArguments(bundle);

        return fragment;
    }

    @NonNull
    public static ErrorListFragment newInstance(ArrayList<FullScreenErrorModel> listdata, boolean hideTitle) {
        ErrorListFragment fragment = new ErrorListFragment();

        Bundle bundle = new Bundle();
        bundle.putSerializable(LIST, listdata);
        bundle.putBoolean(HIDE_TITLE, hideTitle);
        fragment.setArguments(bundle);

        return fragment;
    }

    @NonNull
    public static ErrorListFragment newInstance(ArrayList<FullScreenErrorModel> listdata, String customTitle, String devType) {
        ErrorListFragment fragment = new ErrorListFragment();

        Bundle bundle = new Bundle();
        bundle.putSerializable(LIST, listdata);
        bundle.putString(CUSTOM_TITLE, customTitle);
        bundle.putString(DEVTYPE, devType);
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Bundle arguments = getArguments();
        if (arguments != null) {
            Object suspect = arguments.getSerializable(LIST);
            if (suspect != null && (suspect instanceof List)) {
                mData = (ArrayList<FullScreenErrorModel>) suspect;
            }
            bHideTitle = arguments.getBoolean(HIDE_TITLE, false);
            customTitle = arguments.getString(CUSTOM_TITLE);
            devType = arguments.getString(DEVTYPE);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view =  super.onCreateView(inflater, container, savedInstanceState);

        getActivity().setTitle(getTitle());

        ListView lvErrors = (ListView) view.findViewById(R.id.lv_water_heater_errors);
        ErrorListDataAdapter adapter = new ErrorListDataAdapter(getActivity(), mData);
        lvErrors.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        if(bHideTitle) {
            view.findViewById(R.id.water_heater_title).setVisibility(View.GONE);
        }
        else {
            View callArcus = view.findViewById(R.id.call_arcus);
            DeviceType type = DeviceType.fromHint(devType);
            Version1TextView title = (Version1TextView) view.findViewById(R.id.error_list_title);
            Version1TextView titleButton = (Version1TextView) view.findViewById(R.id.error_list_title_button);
            if(!TextUtils.isEmpty(customTitle)) {
                title.setText(customTitle);
            }
            switch (type) {
                case HALO:
                    titleButton.setText(getString(R.string.get_support).toUpperCase());
                    callArcus.setOnClickListener(new View.OnClickListener() {
                        @Override public void onClick(View view) {
                            Intent call = new Intent(Intent.ACTION_VIEW, GlobalSetting.SUPPORT_NUMBER_URI);
                            if(getActivity()!=null){
                                getActivity().startActivity(call);
                            }
                        }
                    });
                    break;
                case WATER_HEATER:
                default:
                    callArcus.setVisibility(View.VISIBLE);
                    callArcus.setOnClickListener(new View.OnClickListener() {
                        @Override public void onClick(View view) {
                            Intent call = new Intent(Intent.ACTION_DIAL, GlobalSetting.WATERHEATER_HEATER_SUPPORT_NUMBER);
                            if(getActivity()!=null){
                                getActivity().startActivity(call);
                            }
                        }
                    });
                    break;
            }

        }

        return view;
    }

    @Override public void onResume() {
        super.onResume();
        ((DashboardActivity) getActivity()).setToolbarError();
        try {
            // In the event place model is null; not sure how putPlaceImage responds to empty string here.
            ImageManager.with(getActivity())
                  .putPlaceImage(RegistrationContext.getInstance().getPlaceModel().getId())
                  .withTransform(new GreyScaleTransformation())
                  .withTransformForStockImages(new GreyScaleTransformation())
                  .intoWallpaper(AlphaPreset.DARKEN)
                  .execute();
        } catch (Exception ignored) {}
    }

    @Override public void onPause() {
        super.onPause();
        ((DashboardActivity) getActivity()).setToPreviousToolbarColor();
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_error_list;
    }

    @NonNull
    @Override
    public String getTitle() {
        return "ERRORS";
    }
}

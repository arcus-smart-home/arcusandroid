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
package arcus.app.common.popups;

import android.os.Bundle;
import androidx.annotation.Nullable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import arcus.app.common.utils.CenteredImageSpan;
import arcus.cornea.subsystem.alarm.model.AlertDeviceStateModel;
import arcus.app.R;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.view.Version1Button;
import arcus.app.common.view.Version1ButtonColor;
import arcus.app.common.view.Version1TextView;

import java.util.ArrayList;



public class BypassedAndOfflineDeviceScrollablePopup extends ArcusFloatingFragment {

    public interface Callback {
        boolean onTopButtonClicked();
        boolean onBottomButtonClicked();
    }

    private final static String TOP_COLOR_KEY = "top-color-key";
    private final static String TOP_TITLE_KEY = "top-title-key";
    private final static String BOTTOM_COLOR_KEY = "bottom-color-key";
    private final static String BOTTOM_TITLE_KEY = "bottom-title-key";
    private final static String DEVICES = "devices";

    private Version1TextView title;
    private Version1TextView description;
    private Version1TextView scrollableDeviceList;
    private View offlineDescription;
    private Version1Button topButton;
    private Version1Button bottomButton;
    private boolean hasOfflineDevices;
    private boolean hasTriggeredDevices;
    private ArrayList<AlertDeviceStateModel> devices;

    private BypassedAndOfflineDeviceScrollablePopup.Callback callback;

    public static BypassedAndOfflineDeviceScrollablePopup newInstance(Version1ButtonColor topButtonColor,
                                                                      String topButtonText, Version1ButtonColor bottomButtonColor, String bottomButtonText,
                                                                      ArrayList<AlertDeviceStateModel> deviceStateModelList,
                                                                      BypassedAndOfflineDeviceScrollablePopup.Callback callback) {
        BypassedAndOfflineDeviceScrollablePopup fragment = new BypassedAndOfflineDeviceScrollablePopup();

        Bundle bundle = new Bundle();
        bundle.putSerializable(TOP_COLOR_KEY, topButtonColor);
        bundle.putSerializable(TOP_TITLE_KEY, topButtonText);
        bundle.putSerializable(BOTTOM_COLOR_KEY, bottomButtonColor);
        bundle.putSerializable(BOTTOM_TITLE_KEY, bottomButtonText);
        bundle.putParcelableArrayList(DEVICES, deviceStateModelList);
        fragment.setArguments(bundle);

        fragment.callback = callback;

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        devices = getArguments().getParcelableArrayList(DEVICES);
        for(AlertDeviceStateModel model : devices) {
            if(model.isOnline) {
                hasTriggeredDevices = true;
            }
            if(!model.isOnline) {
                hasOfflineDevices = true;
            }
            if(hasTriggeredDevices && hasOfflineDevices) {
                break;
            }
        }
        View view = super.onCreateView(inflater, container, savedInstanceState);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        showFullScreen(true);
        setHasCloseButton(false);
    }

    @Override
    public void setFloatingTitle() {
        // Nothing to do
    }

    @Override
    public void doContentSection() {
        this.title = (Version1TextView) contentView.findViewById(R.id.title);
        this.description = (Version1TextView) contentView.findViewById(R.id.description);
        this.topButton = (Version1Button) contentView.findViewById(R.id.top_button);
        this.bottomButton = (Version1Button) contentView.findViewById(R.id.bottom_button);
        this.offlineDescription = contentView.findViewById(R.id.offline_description);
        this.scrollableDeviceList = (Version1TextView) contentView.findViewById(R.id.device_list);

        if(!hasOfflineDevices) {
            offlineDescription.setVisibility(View.GONE);
        } else {
            Version1TextView offlineText = (Version1TextView) contentView.findViewById(R.id.offline_text);
            SpannableStringBuilder ssb = new SpannableStringBuilder("  "+getActivity().getString(R.string.security_offline_devices_desc));
            CenteredImageSpan imageSpan = new CenteredImageSpan(getActivity(), R.drawable.error_dot);
            ssb.setSpan(imageSpan, 0, 1, Spannable.SPAN_INCLUSIVE_EXCLUSIVE );
            offlineText.setText( ssb, TextView.BufferType.SPANNABLE );

            offlineDescription.setVisibility(View.VISIBLE);
        }

        if(hasOfflineDevices && !hasTriggeredDevices) {
            description.setVisibility(View.GONE);
        } else {
            description.setVisibility(View.VISIBLE);
        }

        title.setText(getTitle());
        description.setText(getActivity().getString(R.string.security_bypass_devices_desc));

        topButton.setText(getTopButtonText());
        topButton.setColorScheme(getTopButtonColor());

        bottomButton.setText(getBottomButtonText());
        bottomButton.setColorScheme(getBottomButtonColor());

        topButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (callback != null) {
                    if (callback.onTopButtonClicked()) {
                        BackstackManager.getInstance().navigateBack();
                    }
                }
            }
        });

        bottomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (callback != null) {
                    if (callback.onBottomButtonClicked()) {
                        BackstackManager.getInstance().navigateBack();
                    }
                }
            }
        });

        scrollableDeviceList.setText(getScrollableDescription(), TextView.BufferType.SPANNABLE);

    }

    @Override
    public Integer contentSectionLayout() {
        return R.layout.fullscreen_bypassed_and_offline_devices;
    }

    @Nullable
    @Override
    public String getTitle() {
        if(hasOfflineDevices && hasTriggeredDevices) {
            return getActivity().getString(R.string.security_bypass_and_offline_devices_title);
        } else if(hasOfflineDevices) {
            return getActivity().getString(R.string.security_offline_devices_title);
        } else {
            return getActivity().getString(R.string.security_bypass_devices_title);
        }
    }

    private String getTopButtonText() {
        return getArguments().getString(TOP_TITLE_KEY);
    }

    private String getBottomButtonText() {
        return getArguments().getString(BOTTOM_TITLE_KEY);
    }

    private Version1ButtonColor getTopButtonColor() {
        return (Version1ButtonColor) getArguments().getSerializable(TOP_COLOR_KEY);
    }

    private Version1ButtonColor getBottomButtonColor() {
        return (Version1ButtonColor) getArguments().getSerializable(BOTTOM_COLOR_KEY);
    }

    private SpannableStringBuilder getScrollableDescription() {
        SpannableStringBuilder deviceDisplay = new SpannableStringBuilder("\n");
        for (AlertDeviceStateModel thisDevice : devices) {
            SpannableStringBuilder ssb = new SpannableStringBuilder(thisDevice.name+"   \n\n");
            if(!thisDevice.isOnline) {
                CenteredImageSpan imageSpan = new CenteredImageSpan(getActivity(), R.drawable.error_dot);
                ssb.setSpan(imageSpan, thisDevice.name.length()+1, thisDevice.name.length()+2, Spannable.SPAN_INCLUSIVE_EXCLUSIVE );
            }
            deviceDisplay.append(ssb);
        }
        return deviceDisplay;
    }


}

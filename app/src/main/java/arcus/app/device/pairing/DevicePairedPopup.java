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
package arcus.app.device.pairing;

import android.os.Bundle;
import androidx.annotation.NonNull;
import android.view.View;

import arcus.app.R;
import arcus.app.common.popups.ArcusFloatingFragment;
import arcus.app.common.view.Version1Button;
import arcus.app.common.view.Version1TextView;


public class DevicePairedPopup extends ArcusFloatingFragment {

    private static final String TITLE_KEY = "TITLE";
    private static final String DESC_KEY = "DESC";
    private static final String BTN_TEXT_KEY = "TEXT";

    private InfoButtonCallback mListener;

    private String mTitle;
    private String mDescription;
    private String mButtonText;

    @NonNull
    public static DevicePairedPopup newInstance(String title, String description, String buttonText, InfoButtonCallback listener){
        DevicePairedPopup fragment = new DevicePairedPopup();

        Bundle bundle = new Bundle();
        bundle.putString(TITLE_KEY, title);
        bundle.putString(DESC_KEY, description);
        bundle.putString(BTN_TEXT_KEY, buttonText);
        fragment.setArguments(bundle);

        fragment.setListener(listener);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Bundle arguments = getArguments();
        if (arguments != null) {
            mTitle = arguments.getString(TITLE_KEY, "");
            mDescription = arguments.getString(DESC_KEY, "");
            mButtonText = arguments.getString(BTN_TEXT_KEY, "");
        }
    }

    @Override
    public void setFloatingTitle() {
        title.setVisibility(View.GONE);
    }

    @Override
    public void doContentSection() {
        Version1TextView titleText = (Version1TextView) contentView.findViewById(R.id.title);
        Version1TextView descriptionText = (Version1TextView) contentView.findViewById(R.id.description);
        Version1Button infoButton = (Version1Button) contentView.findViewById(R.id.info_button);

        titleText.setText(mTitle);
        descriptionText.setText(mDescription);
        infoButton.setText(mButtonText);

        if (mListener != null) {
            infoButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.infoButtonClicked();
                    doClose();
                }
            });

        }
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.dialog_info;
    }

    @Override
    public Integer contentSectionLayout() {
        return R.layout.dialog_info_content;
    }

    @NonNull
    @Override
    public String getTitle() {
        return "";
    }

    private void setListener(InfoButtonCallback listener) {
        mListener = listener;
    }

    public interface InfoButtonCallback {
        void infoButtonClicked();
    }

}

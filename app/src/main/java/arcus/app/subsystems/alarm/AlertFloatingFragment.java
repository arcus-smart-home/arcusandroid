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
package arcus.app.subsystems.alarm;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;

import arcus.app.R;
import arcus.app.common.popups.ArcusFloatingFragment;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.utils.StringUtils;
import arcus.app.common.view.Version1Button;
import arcus.app.common.view.Version1ButtonColor;
import arcus.app.common.view.Version1TextView;


public class AlertFloatingFragment extends ArcusFloatingFragment {

    private static final String TITLE_KEY = "TITLE";
    private static final String SUB_TITLE_KEY = "SUB_TITLE_KEY";
    private static final String DESC_KEY = "DESC";
    private static final String TOP_TEXT_KEY = "TOP TEXT";
    private static final String BOTTOM_TEXT_KEY = "BOTTOM TEXT";

    private AlertButtonCallback mListener;
    private AlertDismissedCallback mDismissedListener;

    private String mTitle;
    private String subTitleText;
    private String mDescription;

    private String mTopButtonText;
    private String mBottomButtonText;

    @NonNull
    public static AlertFloatingFragment newInstance(String title, String description, String topButtonText, String bottomButtonText, AlertButtonCallback listener){
        return newInstance(title, description, topButtonText ,bottomButtonText, null, listener);
    }

    @NonNull public static AlertFloatingFragment newInstance(
          String title,
          String description,
          @Nullable String topButtonText,
          @Nullable String bottomButtonText,
          @Nullable String subTitleText,
          AlertButtonCallback listener
    ){
        AlertFloatingFragment fragment = new AlertFloatingFragment();

        Bundle bundle = new Bundle(5);
        bundle.putString(TITLE_KEY, title);
        bundle.putString(SUB_TITLE_KEY, subTitleText);
        bundle.putString(DESC_KEY, description);
        bundle.putString(TOP_TEXT_KEY, topButtonText);
        bundle.putString(BOTTOM_TEXT_KEY, bottomButtonText);
        fragment.setArguments(bundle);

        fragment.setListener(listener);

        return fragment;
    }

    @NonNull public static AlertFloatingFragment newInstance(
            String title,
            String description,
            @Nullable String topButtonText,
            @Nullable String bottomButtonText,
            @Nullable String subTitleText,
            AlertButtonCallback listener,
            AlertDismissedCallback dismissedListener
    ){
        AlertFloatingFragment fragment = newInstance(title, description, topButtonText, bottomButtonText, subTitleText, listener);
        fragment.setDismissedListener(dismissedListener);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Bundle arguments = getArguments();
        if (arguments != null) {
            mTitle = arguments.getString(TITLE_KEY, "");
            subTitleText = arguments.getString(SUB_TITLE_KEY, "");
            mDescription = arguments.getString(DESC_KEY, "");
            mTopButtonText = arguments.getString(TOP_TEXT_KEY, "");
            mBottomButtonText = arguments.getString(BOTTOM_TEXT_KEY, "");
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
        Version1Button topButton = (Version1Button) contentView.findViewById(R.id.top_button);
        Version1Button bottomButton = (Version1Button) contentView.findViewById(R.id.bottom_button);
        Version1TextView errorText = (Version1TextView) contentView.findViewById(R.id.error_text);
        Version1TextView subTitleTextView = (Version1TextView) contentView.findViewById(R.id.sub_title_text);

        titleText.setText(mTitle);
        descriptionText.setText(mDescription);

        if (StringUtils.isEmpty(mTopButtonText)) {
            topButton.setVisibility(View.GONE);
        } else {
            topButton.setText(mTopButtonText);
        }

        if (StringUtils.isEmpty(mBottomButtonText)) {
            bottomButton.setVisibility(View.GONE);
        } else {
            bottomButton.setText(mBottomButtonText);
        }

        if (!TextUtils.isEmpty(subTitleText)) {
            subTitleTextView.setText(subTitleText);
            subTitleTextView.setVisibility(View.VISIBLE);
            descriptionText.setTextColor(getResources().getColor(R.color.white_with_60));
        }

        errorText.setVisibility(View.GONE);

        topButton.setColorScheme(Version1ButtonColor.WHITE);
        bottomButton.setColorScheme(Version1ButtonColor.WHITE);

        if (mListener != null) {
            topButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener.topAlertButtonClicked()) {
                        doClose();
                        BackstackManager.getInstance().navigateBack();
                    }
                }
            });

            bottomButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener.bottomAlertButtonClicked()) {
                        doClose();
                        BackstackManager.getInstance().navigateBack();
                    }
                }
            });
        }
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.dialog_alert;
    }

    @Override
    public Integer contentSectionLayout() {
        return R.layout.dialog_alert_content;
    }

    @NonNull
    @Override
    public String getTitle() {
        return "";
    }

    private void setListener(AlertButtonCallback listener) {
        mListener = listener;
    }
    private void setDismissedListener(AlertDismissedCallback listener) {
        mDismissedListener = listener;
    }

    public interface AlertButtonCallback {
        boolean topAlertButtonClicked();
        boolean bottomAlertButtonClicked();
    }

    public interface AlertDismissedCallback {
        void dismissed();
    }

    @Override
    public void onClick(@NonNull View v) {
        super.onClick(v);
        if(mDismissedListener != null) {
            final int id = v.getId();
            switch (id) {
                case R.id.fragment_arcus_pop_up_close_btn:
                    mDismissedListener.dismissed();
                    break;
            }
        }
    }

    @Override
    public boolean onBackPressed() {
        if(mDismissedListener != null) {
            mDismissedListener.dismissed();
            return true;
        }
        return super.onBackPressed();
    }
}

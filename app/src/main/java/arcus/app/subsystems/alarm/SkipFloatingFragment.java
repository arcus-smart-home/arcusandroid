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
import androidx.appcompat.app.ActionBar;
import android.text.TextUtils;
import android.view.View;

import arcus.app.R;
import arcus.app.activities.BaseActivity;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.popups.ArcusFloatingFragment;
import arcus.app.common.view.Version1Button;
import arcus.app.common.view.Version1ButtonColor;

public class SkipFloatingFragment extends ArcusFloatingFragment {

    private static final String TOP_TEXT_KEY = "TOP TEXT";
    private static final String BOTTOM_TEXT_KEY = "BOTTOM TEXT";

    private AlertButtonCallback mListener;

    private String mTopButtonText;
    private String mBottomButtonText;

    public interface AlertButtonCallback {
        boolean topAlertButtonClicked();

        boolean bottomAlertButtonClicked();

        void dialogClosed();
    }


    @NonNull
    public static SkipFloatingFragment newInstance(
            String topButtonText,
            String bottomButtonText,
            AlertButtonCallback listener
    ) {
        SkipFloatingFragment fragment = new SkipFloatingFragment();

        Bundle bundle = new Bundle(2);
        bundle.putString(TOP_TEXT_KEY, topButtonText);
        bundle.putString(BOTTOM_TEXT_KEY, bottomButtonText);

        fragment.setArguments(bundle);
        fragment.mListener = listener;

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Bundle arguments = getArguments();
        if (arguments != null) {
            mTopButtonText = arguments.getString(TOP_TEXT_KEY);
            mBottomButtonText = arguments.getString(BOTTOM_TEXT_KEY);
        }


    }

    @Override
    public void setFloatingTitle() {
        title.setVisibility(View.GONE);
    }

    @Override
    public void doContentSection() {

        this.setHasCloseButton(false);

        Version1Button topButton = (Version1Button) contentView.findViewById(R.id.top_button);
        Version1Button bottomButton = (Version1Button) contentView.findViewById(R.id.bottom_button);

        topButton.setVisibility(TextUtils.isEmpty(mTopButtonText) ? View.GONE : View.VISIBLE);
        topButton.setText(mTopButtonText);
        topButton.setColorScheme(Version1ButtonColor.BLACK);
        topButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null && mListener.topAlertButtonClicked()) {
                    doClose();
                    BackstackManager.getInstance().navigateBack();
                }
            }
        });

        bottomButton.setVisibility(TextUtils.isEmpty(mBottomButtonText) ? View.GONE : View.VISIBLE);
        bottomButton.setText(mBottomButtonText);
        bottomButton.setColorScheme(Version1ButtonColor.BLACK);
        bottomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null && mListener.bottomAlertButtonClicked()) {
                    doClose();
                    BackstackManager.getInstance().navigateBack();
                }
            }
        });

    }

    @Override
    public void doClose() {
        super.doClose();
        if (mListener != null) {
            mListener.dialogClosed();
        }
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.dialog_skip_cc;
    }

    @Override
    public Integer contentSectionLayout() {
        return R.layout.skip_cc_dialog_content;
    }

    @NonNull
    @Override
    public String getTitle() {
        return "";
    }

    @Override
    public void onResume() {
        super.onResume();
        ActionBar actionBar = getActionBarOrNull();
        if (actionBar != null) {
            actionBar.hide();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        ActionBar actionBar = getActionBarOrNull();
        if (actionBar != null) {
            actionBar.show();
        }
    }

    protected ActionBar getActionBarOrNull() {
        BaseActivity activity = (BaseActivity) getActivity();
        if (activity == null) {
            return null;
        }

        return activity.getSupportActionBar();
    }
}

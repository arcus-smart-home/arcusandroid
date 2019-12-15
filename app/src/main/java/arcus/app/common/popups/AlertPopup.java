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

import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.NonNull;
import android.text.method.LinkMovementMethod;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import arcus.app.R;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.utils.StringUtils;
import arcus.app.common.view.Version1Button;
import arcus.app.common.view.Version1ButtonColor;
import arcus.app.common.view.Version1TextView;


public class AlertPopup extends ArcusFloatingFragment {

    private static final String TITLE_KEY = "TITLE";
    private static final String DESC_KEY = "DESC";
    private static final String TOP_TEXT_KEY = "TOP TEXT";
    private static final String BOTTOM_TEXT_KEY = "BOTTOM TEXT";
    private static final String ERROR_TEXT_KEY = "ERROR TEXT";
    private static final String STYLE_KEY = "STYLE KEY";
    private static final String TOP_BUTTON_COLOR_KEY = "TOP BUTTON COLOR";

    private AlertButtonCallback mListener;

    private String mTitle;
    private CharSequence mDescription;

    private String mTopButtonText;
    private String mBottomButtonText;

    private String mErrorText;

    private ColorStyle mStyle;
    private Version1ButtonColor mTopButtonColor;
    private boolean closeButtonVisible;

    public enum ColorStyle {
        PINK,
        WHITE
    }

    @NonNull
    public static AlertPopup newInstance(String title, CharSequence description, String errorText, AlertButtonCallback listener){
        AlertPopup fragment = new AlertPopup();

        Bundle bundle = new Bundle();
        bundle.putString(TITLE_KEY, title);
        bundle.putCharSequence(DESC_KEY, description);
        bundle.putString(ERROR_TEXT_KEY, errorText);
        fragment.setArguments(bundle);

        fragment.setListener(listener);

        return fragment;
    }

    @NonNull
    public static AlertPopup newInstance(String title, CharSequence description, String topButtonText, String bottomButtonText, AlertButtonCallback listener){
        return newInstance(title, description, topButtonText, bottomButtonText, ColorStyle.PINK, listener);
    }

    @NonNull
    public static AlertPopup newInstance(String title, CharSequence description, String topButtonText, String bottomButtonText, ColorStyle style, AlertButtonCallback listener) {
        return newInstance(title, description, null, topButtonText, bottomButtonText, style, listener);
    }

    @NonNull
    public static AlertPopup newInstance(String title, CharSequence description, Version1ButtonColor topButtonColor, String topButtonText, String bottomButtonText, ColorStyle style, AlertButtonCallback listener) {
        AlertPopup fragment = new AlertPopup();

        Bundle bundle = new Bundle();
        bundle.putString(TITLE_KEY, title);
        bundle.putCharSequence(DESC_KEY, description);
        bundle.putString(TOP_TEXT_KEY, topButtonText);
        bundle.putString(BOTTOM_TEXT_KEY, bottomButtonText);
        bundle.putSerializable(STYLE_KEY, style);
        bundle.putSerializable(TOP_BUTTON_COLOR_KEY, topButtonColor);

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
            mDescription = arguments.getCharSequence(DESC_KEY, "");
            mTopButtonText = arguments.getString(TOP_TEXT_KEY, "");
            mBottomButtonText = arguments.getString(BOTTOM_TEXT_KEY, "");
            mErrorText = arguments.getString(ERROR_TEXT_KEY, "");
            mStyle = (ColorStyle) arguments.getSerializable(STYLE_KEY);
            mTopButtonColor = (Version1ButtonColor) arguments.getSerializable(TOP_BUTTON_COLOR_KEY);

            if (mStyle == null) mStyle = ColorStyle.PINK;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        closeBtn.setOnClickListener(this);

        view.setFocusableInTouchMode(true);
        view.requestFocus();
        view.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
                    doClose();
                    return true;
                } else {
                    return false;
                }
            }
        });

        return view;
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

        titleText.setText(mTitle);
        descriptionText.setText(mDescription);
        descriptionText.setMovementMethod(LinkMovementMethod.getInstance());

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

        if (StringUtils.isEmpty(mErrorText)) {
            errorText.setVisibility(View.GONE);
        } else {
            errorText.setText(mErrorText);
        }

        if (mStyle == ColorStyle.PINK) {
            descriptionText.setLinkTextColor(Color.WHITE);
            topButton.setColorScheme(Version1ButtonColor.WHITE);
            bottomButton.setColorScheme(Version1ButtonColor.WHITE);

            closeBtn.setImageResource(R.drawable.button_close_box_white);
        } else if (mStyle == ColorStyle.WHITE) {
            topButton.setColorScheme(Version1ButtonColor.BLACK);
            bottomButton.setColorScheme(Version1ButtonColor.BLACK);

            Resources resources = getResources();
            titleText.setTextColor(resources.getColor(R.color.black));
            descriptionText.setTextColor(resources.getColor(R.color.black_with_60));
            errorText.setTextColor(resources.getColor(R.color.black));

            closeBtn.setImageResource(R.drawable.button_close_black_x);
        }

        if (mTopButtonColor != null) {
            topButton.setColorScheme(mTopButtonColor);
        }

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
            errorText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener.errorButtonClicked()) {
                        // ?
                    }
                }
            });
        }

        closeBtn.setVisibility(isCloseButtonVisible() ? View.VISIBLE : View.GONE);
    }

    @Override
    public Integer floatingBackgroundColor() {
        int color = getResources().getColor(R.color.pink_banner);

        if (mStyle == ColorStyle.WHITE) {
            color = getResources().getColor(R.color.white);
        }

        return color;
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.dialog_alert_popup;
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

    @Override
    public void onClick(@NonNull View v) {
        final int id = v.getId();
        switch (id){
            case R.id.fragment_arcus_pop_up_close_btn:
                doClose();
                break;
        }
    }

    @Override
    public void doClose() {
        if (mListener != null) {
            mListener.close();
        }
    }

    public boolean isCloseButtonVisible() {
        return closeButtonVisible;
    }

    public void setCloseButtonVisible(boolean closeButtonVisible) {
        this.closeButtonVisible = closeButtonVisible;
    }

    public interface AlertButtonCallback {
        boolean topAlertButtonClicked();
        boolean bottomAlertButtonClicked();
        boolean errorButtonClicked();
        void close();
    }

    /**
     * A No-Op alert button callback, allowing the implementing class to override only the methods
     * needed while providing "default behavior" for the remaining methods.
     */
    public static abstract class DefaultAlertButtonCallback implements AlertButtonCallback {
        @Override
        public boolean topAlertButtonClicked() {
            return false;
        }

        @Override
        public boolean bottomAlertButtonClicked() {
            return false;
        }

        @Override
        public boolean errorButtonClicked() {
            return false;
        }

        @Override
        public void close() {
            /* No-Op */
        }
    }
}

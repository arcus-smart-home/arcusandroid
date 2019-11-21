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
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;
import android.view.View;
import android.widget.TextView;

import arcus.app.R;
import arcus.app.common.utils.ActivityUtils;
import arcus.app.common.view.Version1TextView;


public class InfoTextPopup extends ArcusFloatingFragment {
    private final static String INFO_TEXT_TITLE_ID = "INFO.TEXT.TITLE.ID";
    private final static String INFO_TEXT_ID = "INFO_TEXT_ID";
    private final static String INFO_TEXT = "INFO_TEXT";
    private final static String SHOW_SHOP_NOW = "SHOW_SHOP_NOW";

    private Version1TextView infoText;

    @NonNull
    public static InfoTextPopup newInstance (int infoTextResId) {
        InfoTextPopup instance = new InfoTextPopup();

        Bundle arguments = new Bundle();
        arguments.putInt(INFO_TEXT_ID, infoTextResId);
        instance.setArguments(arguments);

        return instance;
    }

    @NonNull
    public static InfoTextPopup newInstance (String infoText, @StringRes int infoTextTitle) {
        InfoTextPopup instance = new InfoTextPopup();

        Bundle arguments = new Bundle();
        arguments.putString(INFO_TEXT, String.valueOf(infoText));   // Handle null gracefully
        arguments.putInt(INFO_TEXT_TITLE_ID, infoTextTitle);
        instance.setArguments(arguments);

        return instance;
    }

    @NonNull
    public static InfoTextPopup newInstance (@StringRes int infoTextResId, @StringRes int infoTextTitle) {
        InfoTextPopup instance = new InfoTextPopup();

        Bundle arguments = new Bundle();
        arguments.putInt(INFO_TEXT_ID, infoTextResId);
        arguments.putInt(INFO_TEXT_TITLE_ID, infoTextTitle);
        instance.setArguments(arguments);

        return instance;
    }

    @NonNull
    public static InfoTextPopup newInstance (@StringRes int infoTextResId, @StringRes int infoTextTitle, Boolean showShopNow) {
        InfoTextPopup instance = new InfoTextPopup();

        Bundle arguments = new Bundle();
        arguments.putBoolean(SHOW_SHOP_NOW, showShopNow);
        arguments.putInt(INFO_TEXT_ID, infoTextResId);
        arguments.putInt(INFO_TEXT_TITLE_ID, infoTextTitle);
        instance.setArguments(arguments);

        return instance;
    }

    @Override
    public void setFloatingTitle() {
        title.setText(getTitle());
    }

    @Override
    public void doContentSection() {
        infoText = (Version1TextView) contentView.findViewById(R.id.info_text);

        String infoTextString = getArguments().getString(INFO_TEXT);
        if (infoTextString != null) {
            infoText.setText(infoTextString);
        } else {
            infoText.setText(getString(getArguments().getInt(INFO_TEXT_ID)));
        }

        if (Boolean.TRUE.equals(getArguments().getBoolean(SHOW_SHOP_NOW, false))) {
            TextView shopNow = (TextView) contentView.findViewById(R.id.learn_more_button);
            if (shopNow != null) {
                shopNow.setVisibility(View.VISIBLE);
                shopNow.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.outline_button_style_black));
                shopNow.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ActivityUtils.launchShopNow();
                    }
                });
            }
        }
    }

    @Override
    public Integer contentSectionLayout() {
        return R.layout.floating_info_text;
    }

    @Override
    public String getTitle() {
        return getString(getArguments().getInt(INFO_TEXT_TITLE_ID, R.string.people_more_info));
    }

    public Version1TextView getInfoText() {
        return infoText;
    }
}

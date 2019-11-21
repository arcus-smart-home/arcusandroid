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

import arcus.app.R;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.view.Version1TextView;


public class UpdateAppPopup extends ArcusFloatingFragment {
    private final static String INFO_TEXT_ID = "INFO_TEXT_ID";
    private final static String BUTTON_TEXT_ID = "BUTTON_TEXT_ID";

    private UpdateAppPopupCallback mListener;

    @NonNull
    public static UpdateAppPopup newInstance(@StringRes int infoTextResId, @StringRes int buttonTextId, UpdateAppPopupCallback listener) {
        UpdateAppPopup instance = new UpdateAppPopup();

        Bundle arguments = new Bundle();
        arguments.putInt(INFO_TEXT_ID, infoTextResId);
        arguments.putInt(BUTTON_TEXT_ID, buttonTextId);
        instance.setArguments(arguments);

        instance.setListener(listener);

        return instance;
    }

    @Override
    public void setFloatingTitle() {
        title.setVisibility(View.GONE);
    }

    @Override
    public void doContentSection() {

        Version1TextView titleText = (Version1TextView) contentView.findViewById(R.id.title);
        Version1TextView descriptionText = (Version1TextView) contentView.findViewById(R.id.description);
        Version1TextView button = (Version1TextView) contentView.findViewById(R.id.button);

        titleText.setText("");
        descriptionText.setText(getString(getArguments().getInt(INFO_TEXT_ID)));
        button.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.outline_button_style));

        closeBtn.setImageResource(R.drawable.button_close_box_white);
        button.setVisibility(View.VISIBLE);

        int buttonResId = getArguments().getInt(BUTTON_TEXT_ID, -1);

        if (buttonResId != -1) {
            button.setText(getResources().getString(buttonResId));
        }

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener.buttonClicked()) {
                    doClose();
                }
            }
        });
    }

    @Override
    public Integer contentSectionLayout() {
        return R.layout.dialog_update_app_content;
    }

    @Override
    public String getTitle() {
        return "";
    }

    private void setListener(UpdateAppPopupCallback listener) {
        mListener = listener;
    }

    @Override
    public void onClick(@NonNull View v) {
        final int id = v.getId();
        switch (id) {
            case R.id.fragment_arcus_pop_up_close_btn:
                doClose();
                BackstackManager.getInstance().navigateBack();
                break;
        }
    }

    @Override
    public void doClose() {
        if (mListener != null) {
            mListener.close();
        }
    }

    @Override
    public Integer floatingBackgroundColor() {
        return getResources().getColor(R.color.pink_banner);
    }

    public interface UpdateAppPopupCallback {
        boolean buttonClicked();

        void close();
    }
}

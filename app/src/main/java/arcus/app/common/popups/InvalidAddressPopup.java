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
import android.view.View;

import arcus.app.R;
import arcus.app.common.view.Version1TextView;


public class InvalidAddressPopup extends ArcusFloatingFragment {
    private static String TITLE = "TITLE";
    private static String DESCRIPTION = "DESCRIPTION";

    public static InvalidAddressPopup newInstance (String title, String description) {
        InvalidAddressPopup popup = new InvalidAddressPopup();
        Bundle bundle = new Bundle();
        bundle.putString(TITLE, title);
        bundle.putString(DESCRIPTION, description);
        popup.setArguments(bundle);
        return popup;
    }

    @Override
    public void onResume() {
        super.onResume();
        showFullScreen(true);
    }

    @Override
    public void setFloatingTitle() {
        title.setVisibility(View.GONE);
    }

    @Override
    public void doContentSection() {
        Version1TextView title = (Version1TextView) contentView.findViewById(R.id.title);
        Version1TextView description = (Version1TextView) contentView.findViewById(R.id.description);

        Bundle bundle = getArguments();
        title.setText(bundle.getString(TITLE));
        description.setText(bundle.getString(DESCRIPTION));
        closeBtn.setImageResource(R.drawable.button_close_box_white);
    }

    @Override
    public Integer contentSectionLayout() {
        return R.layout.popup_invalid_address;
    }

    @Override
    public Integer floatingBackgroundColor() {
        return getResources().getColor(R.color.pink_banner);
    }

    @Nullable
    @Override
    public String getTitle() {
        return "";
    }
}

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
import android.view.View;

import arcus.app.R;
import arcus.app.common.view.Version1Button;
import arcus.app.common.view.Version1ButtonColor;
import arcus.app.common.view.Version1TextView;


public class YesNoPopupColored extends ArcusFloatingFragment {

    public static final String POP_UP_TITLE = "POP UP TITLE";
    public static final String POP_UP_DESCRIPTION = "POP UP DESCRIPTION";
    private Version1Button no;
    private Version1Button yes;
    private Callback callback;

    public interface Callback {
        void yes();
        void no();
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    @NonNull
    public static YesNoPopupColored newInstance(String popupTitle, String description) {
        YesNoPopupColored popup = new YesNoPopupColored();
        Bundle bundle = new Bundle();
        bundle.putString(POP_UP_TITLE,popupTitle);
        bundle.putString(POP_UP_DESCRIPTION,description);
        popup.setArguments(bundle);

        return popup;
    }

    @Override
    public void setFloatingTitle() {
        title.setText(getArguments().getString(POP_UP_TITLE));
    }

    @Override
    public void doContentSection() {
        closeBtn.setVisibility(View.GONE);

        Version1TextView description = (Version1TextView) contentView.findViewById(R.id.fragment_arcus_pop_up_description);
        no = (Version1Button) contentView.findViewById(R.id.no);
        yes = (Version1Button) contentView.findViewById(R.id.yes);
        yes.setColorScheme(Version1ButtonColor.MAGENTA);
        description.setText(getArguments().getString(POP_UP_DESCRIPTION));
        no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(callback!=null) {
                    callback.no();
                }
            }
        });
        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(callback!=null) {
                    callback.yes();
                }
            }
        });
    }


    @Override
    public Integer contentSectionLayout() {
        return R.layout.yes_no_popup;
    }

    @NonNull
    @Override
    public String getTitle() {
        return "";
    }
}

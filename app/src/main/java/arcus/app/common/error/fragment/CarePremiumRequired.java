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

import android.os.Bundle;
import androidx.annotation.Nullable;
import android.view.View;

import arcus.app.R;
import arcus.app.common.fragments.BaseFragment;

public class CarePremiumRequired extends BaseFragment {

    public static CarePremiumRequired newInstance() {
        return new CarePremiumRequired();
    }

    @Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (view == null) {
            return;
        }

        view.findViewById(R.id.close_container).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                getActivity().finish();
            }
        });
    }

    @Nullable @Override public String getTitle() {
        return getString(R.string.error_premium_required);
    }

    @Override public Integer getLayoutId() {
        return R.layout.fragment_care_premium_required;
    }
}

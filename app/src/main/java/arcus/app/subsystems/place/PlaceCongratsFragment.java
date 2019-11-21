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
package arcus.app.subsystems.place;

import android.app.Activity;
import android.os.Bundle;
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import arcus.app.R;


public class PlaceCongratsFragment extends PlaceCreationStepFragment {

    TextView message;

    @NonNull
    public static PlaceCongratsFragment newInstance() {
        return new PlaceCongratsFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        message = (TextView) view.findViewById(R.id.place_title);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        message.setText(String.format(getString(R.string.place_congrats_message_title), getController().getNewPlaceNickname()));

        Activity activity = getActivity();
        if (activity != null) {
            activity.setTitle(getTitle());
        }
    }

    @Override
    public boolean submit() {
        transitionToNextState();
        return true;
    }

    @Override
    public boolean validate() {
        // Nothing to validate
        return true;
    }

    @NonNull
    @Override
    public String getTitle() {
        return getString(R.string.account_registration_done).toUpperCase();
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_place_done;
    }

}

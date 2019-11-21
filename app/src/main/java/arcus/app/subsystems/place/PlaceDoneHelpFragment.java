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

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import arcus.app.R;
import arcus.app.activities.BaseActivity;


public class PlaceDoneHelpFragment extends PlaceCreationStepFragment {

    private static final String SHOW_HUB_PAIRING = "show_hub_pairing";
    private boolean showHubPairing = false;
    
    @NonNull
    public static PlaceDoneHelpFragment newInstance(boolean showHubPairingView) {
        PlaceDoneHelpFragment fragment = new PlaceDoneHelpFragment();
        Bundle args = new Bundle(1);
        args.putBoolean(SHOW_HUB_PAIRING, showHubPairingView);
        fragment.setArguments(args);
        return fragment;
    }
    
    @Override
    public boolean submit() {
        transitionToNextState();
        return true;
    }
    
    @Override
    public void onResume() {
        super.onResume();
        ((BaseActivity) getActivity()).getSupportActionBar().hide();
    }
    
    @Override
    public void onPause() {
        super.onPause();
        ((BaseActivity)getActivity()).getSupportActionBar().show();
    }
    
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = super.onCreateView(inflater, container, savedInstanceState);
        this.showHubPairing = getArguments().getBoolean(SHOW_HUB_PAIRING);

        if (view != null) {
            View closeButton = view.findViewById(R.id.exit_view);
            closeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getActivity().onBackPressed();
                }
            });
        }
        return view;
    }
    
    @Override
    public boolean validate() {
        // Nothing to validate
        return true;
    }
    
    @NonNull
    @Override
    public String getTitle() {
        return "";
    }
    
    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_place_done_help;
    }
    
    @Override
    public Integer getMenuId() {
        return R.menu.menu_close;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        endSequence(true);
        return true;
    }
}

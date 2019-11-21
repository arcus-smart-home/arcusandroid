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
package arcus.app.subsystems.camera;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import arcus.app.R;
import arcus.app.common.controller.BackstackPopListener;
import arcus.app.common.fragments.HeaderNavigationViewPagerFragment;
import arcus.app.common.image.ImageManager;
import arcus.app.common.image.Wallpaper;

import java.util.ArrayList;
import java.util.List;

public class CameraParentFragment extends HeaderNavigationViewPagerFragment implements BackstackPopListener {
    @NonNull public static CameraParentFragment newInstance(int position){
        CameraParentFragment fragment = new CameraParentFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(SELECTED_POSITION,position);
        fragment.setArguments(bundle);
        return fragment;
    }

    @NonNull @Override public String getTitle() {
        return getActivity().getString(R.string.card_cameras_title);
    }

    @Override public void onResume() {
        super.onResume();
        setTitle();
    }

    @NonNull @Override protected List<Fragment> getFragments() {
        ArrayList<Fragment> fragments = new ArrayList<>();
        fragments.add(CameraFragment.newInstance());
        fragments.add(ClipsFragment.newInstance());
        fragments.add(CameraMoreFragment.newInstance());
        return fragments;
    }

    @Override public void onPopped() {
        ImageManager.with(getActivity()).setWallpaper(Wallpaper.ofCurrentPlace().darkened());
    }

    @NonNull @Override protected String[] getTitles() {
        return new String[] {"DEVICES", "CLIPS", "MORE"};
    }
}

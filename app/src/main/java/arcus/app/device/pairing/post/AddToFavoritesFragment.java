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
package arcus.app.device.pairing.post;

import android.app.Activity;
import android.os.Bundle;
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import arcus.app.R;
import arcus.app.common.error.ErrorManager;
import arcus.app.common.image.ImageManager;
import arcus.app.common.image.Wallpaper;
import arcus.app.common.sequence.SequencedFragment;
import arcus.app.common.view.Version1Button;
import arcus.app.device.pairing.post.controller.AddToFavoritesFragmentController;


public class AddToFavoritesFragment extends SequencedFragment implements AddToFavoritesFragmentController.Callbacks {

    private CheckBox favoriteCheckBox;
    private Version1Button nextButton;

    private final static String DEVICE_ADDRESS = "DEVICE_ADDRESS";

    @NonNull
    public static AddToFavoritesFragment newInstance(String deviceAddress) {
        AddToFavoritesFragment fragment = new AddToFavoritesFragment();
        Bundle bundle = new Bundle();
        bundle.putString(DEVICE_ADDRESS, deviceAddress);
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup parentGroup = (ViewGroup) super.onCreateView(inflater, container, savedInstanceState);

        favoriteCheckBox = (CheckBox) parentGroup.findViewById(R.id.device_to_favorites);
        nextButton = (Version1Button) parentGroup.findViewById(R.id.device_to_favorites_next_btn);

        return parentGroup;
    }

    @Override
    public void onResume () {
        super.onResume();
        final String deviceAddress = getArguments().getString(DEVICE_ADDRESS);

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goNext();
            }
        });

        favoriteCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showProgressBar();
                AddToFavoritesFragmentController.getInstance().setIsFavorite(getActivity(), deviceAddress, favoriteCheckBox.isChecked());
            }
        });

        ImageManager.with(getActivity()).setWallpaper(Wallpaper.ofCurrentPlace().lightend());

        // Devices are added to favorites by default
        AddToFavoritesFragmentController.getInstance().setListener(this);
        AddToFavoritesFragmentController.getInstance().setIsFavorite(getActivity(), deviceAddress, true);

        Activity activity = getActivity();
        if (activity != null) {
            activity.setTitle(getTitle());
        }
    }

    @Override
    public void onPause () {
        super.onPause();
        AddToFavoritesFragmentController.getInstance().removeListener();
    }

    @Override
    public String getTitle() {
        return getString(R.string.device_paired_favorites_title);
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_device_to_favorites;
    }

    @Override
    public void onSuccess() {
        hideProgressBar();
    }

    @Override
    public void onFailure(Throwable cause) {
        hideProgressBar();
        ErrorManager.in(getActivity()).showGenericBecauseOf(cause);
    }
}

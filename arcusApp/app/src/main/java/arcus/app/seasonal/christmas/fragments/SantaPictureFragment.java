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
package arcus.app.seasonal.christmas.fragments;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import arcus.app.R;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.image.UGCImageSelectionBuilder;
import arcus.app.common.image.UGCImageSelectionListener;
import arcus.app.common.image.picasso.transformation.CircularTransformation;
import arcus.app.common.view.Version1ButtonColor;
import arcus.app.seasonal.christmas.util.SantaPhoto;
import arcus.app.seasonal.christmas.model.ChristmasModel;
import arcus.app.common.view.Version1Button;

public class SantaPictureFragment extends BaseChristmasFragment {

    public static SantaPictureFragment newInstance(ChristmasModel model) {
        SantaPictureFragment pictureFragment = new SantaPictureFragment();

        Bundle bundle = new Bundle(1);
        bundle.putSerializable(MODEL, model);
        pictureFragment.setArguments(bundle);

        return pictureFragment;
    }

    @Override
    public void onResume() {
        super.onResume();

        View rootView = getView();
        if (rootView == null) {
            return;
        }

        final ChristmasModel model = getDataModel();
        Version1Button nextButton = (Version1Button) rootView.findViewById(R.id.santa_next_button);
        if (nextButton != null) {
            if (model.isSetupComplete()) {
                nextButton.setVisibility(View.GONE);
            }
            else {
                nextButton.setColorScheme(Version1ButtonColor.WHITE);
                nextButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        BackstackManager.getInstance().navigateToFragment(SantaComplete.newInstance(model), true);
                    }
                });
            }
        }

        SantaPhoto photo = new SantaPhoto();
        Bitmap ref = photo.getSantaPhoto();
        if (ref != null) {
            model.setHasImageSaved(true);
            saveModelToFragment(model);
        }

        ImageView photoHolder = (ImageView) rootView.findViewById(R.id.camera_placeholder);
        if (photoHolder != null && ref != null) {
            CircularTransformation transformation = new CircularTransformation();
            photoHolder.setImageBitmap(transformation.transform(ref));
        }

        ImageView takePhotoButton = (ImageView) rootView.findViewById(R.id.take_photo_button);
        if (takePhotoButton != null) {
            takePhotoButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    UGCImageSelectionBuilder builder = new UGCImageSelectionBuilder(getActivity(), null, null, null);
                    builder.fromCameraOrGallery().withCallback(new UGCImageSelectionListener() {
                        @Override
                        public void onUGCImageSelected(Bitmap selectedImage) {
                            SantaPhoto photo = new SantaPhoto();
                            photo.saveFile(selectedImage);
                        }
                    }).execute();
                }
            });
        }
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.santa_fragment_take_picture;
    }

}

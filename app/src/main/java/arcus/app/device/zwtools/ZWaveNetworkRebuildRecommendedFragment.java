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
package arcus.app.device.zwtools;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import arcus.app.R;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.image.ImageManager;
import arcus.app.common.image.Wallpaper;
import arcus.app.common.image.picasso.transformation.BlackWhiteInvertTransformation;
import arcus.app.common.image.picasso.transformation.Invert;
import arcus.app.common.popups.AlertPopup;
import arcus.app.common.sequence.SequencedFragment;
import arcus.app.common.view.Version1Button;
import arcus.app.device.zwtools.controller.ZWaveNetworkRepairSequence;


public class ZWaveNetworkRebuildRecommendedFragment extends SequencedFragment<ZWaveNetworkRepairSequence> {

    private Version1Button rebuildNow;
    private Version1Button rebuildLater;
    private ImageView devicesIcon;

    public static ZWaveNetworkRebuildRecommendedFragment newInstance() {
        return new ZWaveNetworkRebuildRecommendedFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        rebuildLater = (Version1Button) view.findViewById(R.id.rebuild_later);
        rebuildNow = (Version1Button) view.findViewById(R.id.rebuild_now);
        devicesIcon = (ImageView) view.findViewById(R.id.devices_icon);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        ImageManager.with(getContext()).putDrawableResource(R.drawable.icon_devices)
                .withTransform(new BlackWhiteInvertTransformation(Invert.BLACK_TO_WHITE))
                .into(devicesIcon)
                .execute();

        rebuildNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goNext();
            }
        });

        rebuildLater.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmRebuildLater();
            }
        });

        ImageManager.with(getContext()).setWallpaper(Wallpaper.ofCurrentPlace().darkened());
    }

    @Nullable
    @Override
    public String getTitle() {
        return getString(R.string.zwtools_network_rebuild);
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_zwave_network_rebuild_recommended;
    }

    private void confirmRebuildLater() {
        AlertPopup areYouSurePop = AlertPopup.newInstance(getString(R.string.zwtools_cancel_title), getString(R.string.zwtools_network_rebuild_confirm), getString(R.string.zwtools_yes), getString(R.string.zwtools_no), AlertPopup.ColorStyle.PINK, new AlertPopup.AlertButtonCallback() {
            @Override
            public boolean topAlertButtonClicked() {
                BackstackManager.getInstance().navigateToFloatingFragment(ZWaveNetworkRebuildLaterPopup.newInstance(), ZWaveNetworkRebuildLaterPopup.class.getSimpleName(), true);
                return false;
            }

            @Override
            public boolean bottomAlertButtonClicked() {
                return true;
            }

            @Override
            public boolean errorButtonClicked() {
                return false;
            }

            @Override
            public void close() {}
        });

        BackstackManager.getInstance().navigateToFloatingFragment(areYouSurePop, areYouSurePop.getClass().getSimpleName(), true);
    }
}

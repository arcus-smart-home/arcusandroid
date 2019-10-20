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
package arcus.app.device.pairing.specialty.petdoor;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;

import arcus.app.R;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.error.ErrorManager;
import arcus.app.common.image.ImageManager;
import arcus.app.common.image.Wallpaper;
import arcus.app.common.image.picasso.transformation.BlackWhiteInvertTransformation;
import arcus.app.common.image.picasso.transformation.CropCircleTransformation;
import arcus.app.common.image.picasso.transformation.Invert;
import arcus.app.common.sequence.SequencedFragment;
import arcus.app.common.utils.StringUtils;
import arcus.app.common.view.Version1Button;
import arcus.app.common.view.Version1ButtonColor;
import arcus.app.common.view.Version1TextView;
import arcus.app.device.pairing.specialty.petdoor.controller.PetDoorKeyListFragmentController;
import arcus.app.device.pairing.specialty.petdoor.controller.PetDoorSmartKeyPairingSequenceController;

import java.util.List;


public class PetDoorKeyListFragment extends SequencedFragment implements PetDoorKeyListFragmentController.Callbacks {

    private final static String DEVICE_ADDRESS = "DEVICE_ADDRESS";

    private Version1Button addSmartKeyButton;
    private ListView keyList;

    public static PetDoorKeyListFragment newInstance (String deviceAddress) {
        PetDoorKeyListFragment instance = new PetDoorKeyListFragment();
        Bundle arguments = new Bundle();
        arguments.putString(DEVICE_ADDRESS, deviceAddress);
        instance.setArguments(arguments);

        return instance;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        addSmartKeyButton = (Version1Button) view.findViewById(R.id.add_key_button);
        keyList = (ListView) view.findViewById(R.id.key_list);

        return view;
    }

    @Override
    public void onResume () {
        super.onResume();

        addSmartKeyButton.setColorScheme(Version1ButtonColor.WHITE);
        addSmartKeyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new PetDoorSmartKeyPairingSequenceController().startSequence(getActivity(), PetDoorKeyListFragment.this, getDeviceAddress());
            }
        });

        ImageManager.with(getActivity()).setWallpaper(Wallpaper.ofCurrentPlace().darkened());

        PetDoorKeyListFragmentController.getInstance().setListener(this);
        PetDoorKeyListFragmentController.getInstance().loadKeyListItems(getDeviceAddress());
    }

    @Override
    public void onPause () {
        super.onPause();
        PetDoorKeyListFragmentController.getInstance().removeListener();
    }

    @Nullable
    @Override
    public String getTitle() {
        return getString(R.string.petdoor);
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_pet_door_smart_key_list;
    }

    @Override
    public void onKeyListItemsLoaded(List<PetDoorKeyListFragmentController.PetDoorSmartKey> items) {
        keyList.setAdapter(new PetListAdapter(getActivity(), items));
        keyList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int tokenId = ((PetListAdapter) parent.getAdapter()).getItem(position).tokenId;
                BackstackManager.getInstance().navigateToFragment(PetDoorSmartKeyNameFragment.newInstance(getDeviceAddress(), tokenId, true), true);
            }
        });
    }

    @Override
    public void onCorneaError(Throwable cause) {
        ErrorManager.in(getActivity()).showGenericBecauseOf(cause);
    }

    private String getDeviceAddress () {
        return getArguments().getString(DEVICE_ADDRESS);
    }

    private static class PetListAdapter extends ArrayAdapter<PetDoorKeyListFragmentController.PetDoorSmartKey> {

        public PetListAdapter(Context context, List<PetDoorKeyListFragmentController.PetDoorSmartKey> items) {
            super(context, 0, items);
        }

        @Nullable
        @Override
        public View getView(int position, @Nullable View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.listdata_item_pet_keys, parent, false);
            }

            PetDoorKeyListFragmentController.PetDoorSmartKey thisItem = getItem(position);

            View divider = convertView.findViewById(R.id.divider);
            View iconOffsetDivider = convertView.findViewById(R.id.icon_offset_divider);
            Version1TextView petName = (Version1TextView) convertView.findViewById(R.id.tvTopText);
            ImageView imageIcon = (ImageView) convertView.findViewById(R.id.imageIcon);
            ImageView chevron = (ImageView) convertView.findViewById(R.id.imageChevron);

            divider.setVisibility(View.GONE);
            iconOffsetDivider.setVisibility(View.VISIBLE);
            iconOffsetDivider.setBackgroundColor(getContext().getResources().getColor(R.color.white_with_10));
            petName.setText(StringUtils.isEmpty(thisItem.petName) ? getContext().getString(R.string.petdoor_unnamed_key) : thisItem.petName);
            imageIcon.setVisibility(View.VISIBLE);
            chevron.setImageResource(R.drawable.chevron_white);

            ImageManager.with(getContext())
                    .putSmallPetImage(String.valueOf(thisItem.tokenId))
                    .withTransformForStockImages(new BlackWhiteInvertTransformation(Invert.BLACK_TO_WHITE))
                    .withTransformForUgcImages(new CropCircleTransformation())
                    .into(imageIcon)
                    .execute();

            return convertView;
        }
    }
}

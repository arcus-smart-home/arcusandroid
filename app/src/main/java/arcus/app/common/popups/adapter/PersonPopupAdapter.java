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
package arcus.app.common.popups.adapter;

import android.content.Context;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import arcus.cornea.provider.DeviceModelProvider;
import arcus.cornea.utils.Listeners;
import arcus.cornea.utils.ModelSource;
import com.iris.client.event.Listener;
import com.iris.client.model.DeviceModel;
import com.iris.client.model.ModelAddedEvent;
import com.iris.client.model.ModelEvent;
import com.iris.client.model.PersonModel;
import arcus.app.R;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.device.pairing.post.controller.NameDeviceFragmentController;

import java.util.List;

public class PersonPopupAdapter extends ArrayAdapter<PersonModel> {

    private DeviceModel deviceModel;

    public PersonPopupAdapter(Context context) {
        super(context, 0);
    }

    public PersonPopupAdapter(Context context, List<PersonModel> data) {
        super(context, 0);
        super.addAll(data);
    }

    @Nullable
    @Override
    public View getView(final int position, @Nullable View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.multicheckbox_item, parent, false);
        }

        final PersonModel mListData = getItem(position);

        TextView topText = (TextView) convertView.findViewById(R.id.tvTopText);
        TextView bottomText = (TextView) convertView.findViewById(R.id.tvBottomText);
        ImageView deviceImage = (ImageView) convertView.findViewById(R.id.imgPic);
        final CheckBox checkbox = (CheckBox) convertView.findViewById(R.id.checkbox);

        topText.setText(mListData.getFirstName() + " " + mListData.getLastName());

        // bottomText should be relation in presence model?
        bottomText.setText("");

        ModelSource<DeviceModel> m = DeviceModelProvider.instance().getModel("Address of model looking for");
        m.addModelListener(Listeners.runOnUiThread(new Listener<ModelEvent>() {
            public void onEvent(ModelEvent e) {
                if (e instanceof ModelAddedEvent) {
                    // model is loaded
                } else { // Handle other events? Deleted, Changed, ?
                }
            }
        }));
        m.load();

        if (m.get() != null) {
            deviceModel = m.get();
        }

        checkbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (position == 0) {
                    // unassign
                    NameDeviceFragmentController.getInstance().assignPersonToDevice(deviceModel, "UNSET");
                } else {
                    // assign person to device
                    NameDeviceFragmentController.getInstance().assignPersonToDevice(deviceModel,mListData.getAddress());
                }
                BackstackManager.getInstance().navigateBack();
            }
        });

        if (position == 0) {
//            ImageManager.with(getContext())
//                    .putSmallDeviceImage(deviceModel)
//                    .withTransformForStockImages(new BlackWhiteInvertTransformation(Invert.WHITE_TO_BLACK))
//                    .withTransform(new CropCircleTransformation())
//                    .into(deviceImage)
//                    .execute();
        } else {
//        ImageManager.with(getContext())
//                .putPersonImage("test")   // todo: put assigned person image
//                .withTransformForUgcImages(new CropCircleTransformation())
//                .withPlaceholder(R.drawable.icon_user_small_black)
//                .withError(R.drawable.icon_user_small_black)
//                .into(deviceImage)
//                .execute();
        }

        return (convertView);
    }
}

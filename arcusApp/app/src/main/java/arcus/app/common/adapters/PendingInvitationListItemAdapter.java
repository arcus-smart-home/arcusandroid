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
package arcus.app.common.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.iris.client.bean.Invitation;
import arcus.app.R;
import arcus.app.common.image.picasso.transformation.BlackWhiteInvertTransformation;
import arcus.app.common.image.picasso.transformation.Invert;
import arcus.app.common.view.Version1TextView;

import java.util.List;


public class PendingInvitationListItemAdapter extends ArrayAdapter<Invitation> {
    private Context context;
    private List<Invitation> items;

    public PendingInvitationListItemAdapter(Context context) {
        super(context, R.layout.list_item_pending_invitation);
        this.context = context;
    }

    public PendingInvitationListItemAdapter(Context context, List<Invitation> items) {
        super(context, R.layout.list_item_pending_invitation, items);
        this.context = context;
        this.items = items;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;

        if(v == null) {
            v = LayoutInflater.from(context).inflate(R.layout.list_item_pending_invitation, null);
        }

        ImageView placeImage = (ImageView) v.findViewById(R.id.place_image);
        placeImage.setColorFilter(context.getResources().getColor(R.color.white), android.graphics.PorterDuff.Mode.MULTIPLY);

        BlackWhiteInvertTransformation transformation = new BlackWhiteInvertTransformation(Invert.BLACK_TO_WHITE);
        Bitmap bmp = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.add_place);
        placeImage.setImageBitmap(transformation.transform(bmp));

        Version1TextView placeName = (Version1TextView) v.findViewById(R.id.place_name);
        Version1TextView placeLocation = (Version1TextView) v.findViewById(R.id.place_location);
        Version1TextView invitor = (Version1TextView) v.findViewById(R.id.invitor);

        Invitation invitation = items.get(position);
        placeName.setText(invitation.getPlaceName());
        String address = invitation.getStreetAddress1();
        String address2 = invitation.getStreetAddress2() == null ? "" : String.format(" %s", invitation.getStreetAddress2());
        String city = invitation.getCity() == null ? "" : String.format(" %s", invitation.getCity());
        String state = invitation.getStateProv() == null ? "" : String.format(", %s", invitation.getStateProv());
        String zipCode = invitation.getZipCode() == null ? "" : String.format(" %s", invitation.getZipCode());
        placeLocation.setText(String.format("%s%s%s%s%s", address, address2, city, state, zipCode));

        invitor.setText(String.format(context.getResources().getString(R.string.invitations_invited_by), String.format("%s %s", invitation.getInvitorFirstName(), invitation.getInvitorLastName()).trim()));
        return v;
    }

}

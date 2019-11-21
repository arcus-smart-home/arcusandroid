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
package arcus.app.activities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import arcus.app.R;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.image.ImageManager;
import arcus.app.launch.InvitationFragment;

public class InvitationActivity extends BaseActivity {

    public static void start(Activity fromActivity) {
        Intent intent = new Intent(fromActivity, InvitationActivity.class);
        intent.putExtra(InvitationFragment.IS_SETTINGS, true);
        fromActivity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invitation);
        toolbar = (androidx.appcompat.widget.Toolbar) findViewById(R.id.my_toolbar);

        View root = findViewById(R.id.container);
        ImageManager.setWallpaperView(this, root);

        setTitle(getResources().getString(R.string.invitation_title));

        Intent intent = getIntent();
        Uri data = intent.getData();
        String emailAddress = "";
        String invitationCode = "";
        String firstName = "";
        String lastName = "";
        if(data != null) {
            emailAddress = data.getQueryParameter("emailAddress");
            invitationCode = data.getQueryParameter("invitationCode");
            firstName = data.getQueryParameter("firstName");
            lastName = data.getQueryParameter("lastName");
        }

        BackstackManager.getInstance().navigateToFragment(InvitationFragment.newInstance(emailAddress, invitationCode, firstName, lastName), true);
    }

    @Override
    public void setTitle(CharSequence title) {
        setToolbarTitle(title);
        toolBarTitle.setTextColor(getResources().getColor(android.R.color.black));
    }
}

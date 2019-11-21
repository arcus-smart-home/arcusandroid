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
package arcus.app.subsystems.alarm.promonitoring;

import android.Manifest;
import android.content.ContentProviderOperation;
import android.os.Bundle;
import android.provider.ContactsContract;
import androidx.annotation.Nullable;
import android.view.View;

import arcus.app.R;
import arcus.app.activities.BaseActivity;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.error.ErrorManager;
import arcus.app.common.error.type.OtherErrorTypes;
import arcus.app.common.popups.ArcusFloatingFragment;
import arcus.app.common.utils.GlobalSetting;
import arcus.app.common.utils.LinkBuilder;
import arcus.app.common.utils.PreferenceUtils;
import arcus.app.common.view.Version1Button;
import arcus.app.common.view.Version1TextView;

import java.util.ArrayList;



public class ProMonitoringAddUccContactFragment extends ArcusFloatingFragment implements BaseActivity.PermissionCallback {

    private final static String PHONE_NUMBERS_ARG = "phone-numbers";

    private Version1Button addToContactsButton;
    private Version1TextView learnMoreLink;
    private ProMonitoringAddUccContactFragment fragment;

    public static ProMonitoringAddUccContactFragment newInstance(ArrayList<String> phoneNumbers) {
        ProMonitoringAddUccContactFragment instance = new ProMonitoringAddUccContactFragment();
        Bundle arguments = new Bundle();
        arguments.putStringArrayList(PHONE_NUMBERS_ARG, phoneNumbers);
        instance.setArguments(arguments);

        return instance;
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fullscreen_arcus_popup_fragment_nopadding;
    }

    @Override
    public void setFloatingTitle() {
        // Nothing to do
    }

    @Override
    public void doContentSection() {
        addToContactsButton = (Version1Button) contentView.findViewById(R.id.add_contact_button);
        learnMoreLink = (Version1TextView) contentView.findViewById(R.id.learn_more_link);

        new LinkBuilder(learnMoreLink)
                .startLinkSpan(GlobalSetting.PRO_MONITORING_ADD_CONTACT_LEARN_MORE)
                .appendText(R.string.learn_more)
                .endLinkSpan()
                .build();

        fragment = this;
        addToContactsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((BaseActivity)getActivity()).setPermissionCallback(fragment);
                ArrayList<String> permissions = new ArrayList<String>();
                permissions.add(Manifest.permission.WRITE_CONTACTS);
                ((BaseActivity)getActivity()).checkPermission(permissions, GlobalSetting.PERMISSION_READ_CONTACTS, R.string.permission_rationale_contacts);
            }
        });
    }
    
    @Override
    public void onResume() {
        super.onResume();

        showFullScreen(true);
        setHasCloseButton(true);
        showTitleLogo(true);
        showTitle(false);
    }

    @Override
    public Integer contentSectionLayout() {
        return R.layout.fragment_pro_monitoring_add_ucc_contact;
    }

    private void addMonitoringStationContactToAddressBook() {
        try {
            ArrayList<ContentProviderOperation> ops = new ArrayList<>();

            ops.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null).build());

            // Set contact display name
            ops.add(ContentProviderOperation
                    .newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.RawContacts.Data.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.RawContacts.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, getString(R.string.ucc_contact_name))
                    .build());

            // Add phone numbers
            for (String thisNumber : getMonitoringStationNumbers()) {
                ops.add(ContentProviderOperation
                        .newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                        .withValue(ContactsContract.RawContacts.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, thisNumber)
                        .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_OTHER).build());
            }
            getActivity().getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
            PreferenceUtils.setHasAddedUccContact(true);
            BackstackManager.getInstance().navigateBack();
            ProMonitoringAddUccContactFragment.super.fireOnClose();
        } catch (Exception e) {
            logger.error("Failed to save monitoring station contact to user's address book.");
            ErrorManager.in(getActivity()).show(OtherErrorTypes.CANT_WRITE_CONTACTS);
        }
    }

    @Nullable
    @Override
    public String getTitle() {
        return null;
    }

    private ArrayList<String> getMonitoringStationNumbers() {
        return getArguments().getStringArrayList(PHONE_NUMBERS_ARG);
    }

    @Override
    public void permissionsUpdate(int permissionType, ArrayList<String> permissionsDenied, ArrayList<String> permissionsDeniedNeverAskAgain) {
        if(permissionsDenied.contains(Manifest.permission.WRITE_CONTACTS)) {
            ((BaseActivity)getActivity()).showSnackBarForPermissions(getString(R.string.permission_read_contacts_denied_message));
        } else {
            addMonitoringStationContactToAddressBook();
        }
    }
}

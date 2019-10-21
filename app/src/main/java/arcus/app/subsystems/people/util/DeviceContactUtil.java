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
package arcus.app.subsystems.people.util;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;

import arcus.app.R;
import arcus.app.subsystems.people.model.DeviceContact;


public class DeviceContactUtil {
    public static void getNameInfo(ContentResolver contentResolver, DeviceContact person, String contactId) {
        Cursor nameCursor = contentResolver.query(ContactsContract.Data.CONTENT_URI, null,
                ContactsContract.Data.CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?",
                new String[]{contactId, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE},
                null);
        while (nameCursor.moveToNext()) {
            person.setFirstName(nameCursor.getString(nameCursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME)));
            person.setLastName(nameCursor.getString(nameCursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME)));
        }
        nameCursor.close();
    }

    public static void getPhoneInfo(Context context, ContentResolver contentResolver, DeviceContact person, String contactId) {

        Cursor phoneCursor = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[] { contactId }, null);
        while (phoneCursor.moveToNext()) {
            String number = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            int type = phoneCursor.getInt(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));
            String typeDisplay = "";
            switch (type) {
                case ContactsContract.CommonDataKinds.Phone.TYPE_HOME:
                    typeDisplay = context.getString(R.string.type_home);
                    break;
                case ContactsContract.CommonDataKinds.Phone.TYPE_WORK:
                    typeDisplay = context.getString(R.string.type_work);
                    break;
                case ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE:
                    typeDisplay = context.getString(R.string.type_mobile);
                    break;
                case ContactsContract.CommonDataKinds.Phone.TYPE_OTHER:
                    typeDisplay = context.getString(R.string.type_other);
                    break;
                case ContactsContract.CommonDataKinds.Phone.TYPE_CUSTOM:
                    typeDisplay = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.LABEL));
                    break;
            }

            person.addPhoneNumber(number, typeDisplay);
        }
        phoneCursor.close();
    }

    public static void getEmailInfo(Context context, ContentResolver contentResolver, DeviceContact person, String contactId) {
        Cursor emailCursor = contentResolver.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, null,
                ContactsContract.CommonDataKinds.Email.CONTACT_ID+ " = ?", new String[] { contactId }, null);
        while (emailCursor.moveToNext()) {
            String address = emailCursor.getString(emailCursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
            int type = emailCursor.getInt(emailCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));
            String typeDisplay = "";
            switch (type) {
                case ContactsContract.CommonDataKinds.Email.TYPE_HOME:
                    typeDisplay = context.getString(R.string.type_home);
                    break;
                case ContactsContract.CommonDataKinds.Email.TYPE_WORK:
                    typeDisplay = context.getString(R.string.type_work);
                    break;
                case ContactsContract.CommonDataKinds.Email.TYPE_MOBILE:
                    typeDisplay = context.getString(R.string.type_mobile);
                    break;
                case ContactsContract.CommonDataKinds.Email.TYPE_OTHER:
                    typeDisplay = context.getString(R.string.type_other);
                    break;
                case ContactsContract.CommonDataKinds.Email.TYPE_CUSTOM:
                    typeDisplay = emailCursor.getString(emailCursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.LABEL));
                    break;
            }
            person.addEmailAddress(address, typeDisplay);
        }
        emailCursor.close();
    }
}

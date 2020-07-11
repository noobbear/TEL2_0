package com.fision.beans;

import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.ContactsContract;

import java.util.ArrayList;
import java.util.List;

public class SystemContactUtil {
    public final static List<MyContact> readContacts(ContentResolver contentResolver) {
        List<MyContact> list = new ArrayList<>();
        try {
            Cursor cursor = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    MyContact myContact = new MyContact();
                    myContact.setName(cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)));
                    myContact.setPhonenum(cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));
                    myContact.setTx(cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI)));
                    myContact.setId(Integer.parseInt(cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_ID))));
                    list.add(myContact);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;

    }
}

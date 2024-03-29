package com.tesla.contacts;

import android.annotation.SuppressLint;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

public class ContactUtils {
    private static final String TAG = "ContactUtils";

    /**
     * 清空通讯录
     *
     * @param context
     */
    public void clearContacts(Context context) {
        ContentResolver contentResolver = context.getContentResolver();
        Uri uri = ContactsContract.Contacts.CONTENT_URI;
        Cursor cursor = contentResolver.query(uri, null, null, null, null);

        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                @SuppressLint("Range") String lookupKey = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY));
                Uri contactUri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI, lookupKey);
                contentResolver.delete(contactUri, null, null);
            }
            cursor.close();
        }
        Log.i(TAG, "clearContacts: finish");
        Toast.makeText(context, "clearContacts finish", Toast.LENGTH_SHORT).show();
    }

    /**
     * 读取系统联系人信息
     */
    public HashSet<String> readContacts(Context context) {
        HashSet<String> contactSet = new HashSet<>();

        // 通过内容解析者,查询所有系统联系人
        Cursor cursor = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null, null, null, null);
        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                @SuppressLint("Range") String displayName = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                @SuppressLint("Range") String number = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                contactSet.add(number);
                Log.i(TAG, "displayName " + displayName + "number " + number);
            }
            cursor.close();
        }

        Log.i(TAG, "readContacts: finish");
        Toast.makeText(context, "readContacts finish", Toast.LENGTH_SHORT).show();
        return contactSet;
    }

    /**
     * 逐行读取文本文件 返回phoneList
     *
     * @param filePath 文件路径
     * @return phoneList
     */
    private ArrayList<String> readLines(String filePath) {
        ArrayList<String> phoneList = new ArrayList<>();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(filePath));
            String line;
            while ((line = reader.readLine()) != null) {
                // 处理每一行文本
                phoneList.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.i(TAG, e.toString());
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return phoneList;
    }

    /**
     * 插入联系人
     */
    public void addContacts(Context context, String fileName) {
        ContentResolver contentResolver = context.getContentResolver();
        File downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File file = new File(downloadDir, fileName);
        // 非root用户adb push后默认root权限无法读取
        // ArrayList<String> phoneList = this.readLines(file.getAbsolutePath());
        ArrayList<String> phoneList = this.readLines("/data/local/tmp/" + fileName);
        HashSet<String> contactSet = this.readContacts(context);

        int i = 0;
        for (String phone : phoneList) {
            // 控制索引
//            if (!(i >= 0 && i < 400)) {
//                continue;
//            }

            // 避免重复插入
            if(contactSet.contains(phone)){
                continue;
            }

            ArrayList<ContentProviderOperation> ops = new ArrayList<>();
            ops.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI).withValue("account_type", null).withValue("account_name", null).build());
            ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI).withValueBackReference("raw_contact_id", 0).withValue("mimetype", "vnd.android.cursor.item/name").withValue("data1", phone).build());
            ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI).withValueBackReference("raw_contact_id", 0).withValue("mimetype", "vnd.android.cursor.item/phone_v2").withValue("data1", phone).withValue("data2", 2).build());
            try {
                ContentProviderResult[] results = contentResolver.applyBatch("com.android.contacts", ops);
                ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, Long.parseLong(results[0].uri.getLastPathSegment()));
                Log.i(TAG, "addContact: " + phone + " success");
            } catch (Exception e) {
                String msg = "addContact: " + phone + " error contactUri";
                Log.i(TAG, msg);
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
            i += 1;
        }
        Log.i(TAG, "addContact: finish");
        Toast.makeText(context, "addContact finish", Toast.LENGTH_SHORT).show();
    }
}


package com.tesla.contacts;

import android.Manifest;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * 导入号码到通讯录
 * 0.授权 通讯录 + SD卡(手动赋予)
 * 1.将contact.txt文件一行一个号码 放置在/sdcard/Download目录中
 * 2.点击按钮import 自动逐行导入到通讯录(导入前先清理 会重复)
 */
public class MainActivity extends AppCompatActivity {
    private EditText et_fpath = null;
    private static final int PERMISSION_REQUEST_CODE = 1;
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.initView();

        // 检查权限
        this.checkAndRequestPermissions();
    }

    private void initView() {
        et_fpath = findViewById(R.id.et_fpath);

        findViewById(R.id.bt_import).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addContact();
            }
        });
        findViewById(R.id.bt_extract).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this, "导出功能未实现", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkAndRequestPermissions() {
        String[] permissions = {
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.WRITE_CONTACTS,
                // 添加其他需要的权限
        };

        // 检查是否已经获取了所有权限
        if (checkPermissions(permissions)) {
            // 已经获取了所有权限，可以执行你的操作
//            addContact();
        } else {
            // 请求权限
            requestPermissions(permissions);
        }
    }

    private boolean checkPermissions(String[] permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                return false; // 有一个权限未被授予
            }
        }
        return true; // 所有权限已经被授予
    }

    private void requestPermissions(String[] permissions) {
        ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            // 检查权限请求的结果
            if (checkPermissions(permissions)) {
                // 所有权限已经被授予，可以执行你的操作
//                addContact();
            } else {
                // 一些权限被拒绝，可以根据需要处理
                this.finish();
            }
        }
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
    private void addContact() {
        ContentResolver contentResolver = getContentResolver();
        File downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        String file_name = this.et_fpath.getText().toString().trim();
        File file = new File(downloadDir, file_name);
        ArrayList<String> phoneList = readLines(file.getAbsolutePath());

        int i = 0;
        for (String phone : phoneList) {
            // 控制索引
//            if (!(i >= 0 && i < 400)) {
//                continue;
//            }

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
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
            i += 1;
        }
        Toast.makeText(this, "addContact finish", Toast.LENGTH_SHORT).show();
    }

}
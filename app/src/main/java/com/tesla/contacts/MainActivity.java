package com.tesla.contacts;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;

/**
 * 导入号码到通讯录
 * 0.授权 通讯录 + SD卡(手动赋予)
 * 1.将contact.txt文件一行一个号码 放置在/sdcard/Download目录中
 * 2.点击按钮clear 清空通讯录
 * 3.点击按钮import 自动逐行导入到通讯录(导入前先清理 会重复)
 */
public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private EditText et_fpath = null;
    private static final int PERMISSION_REQUEST_CODE = 1;
    private ContactUtils contactUtils = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 检查权限
        this.checkAndRequestPermissions();

        this.initView();

        this.initData();

        // this.startServer();

        // 自动触发clear + addContact
        contactUtils.clearContacts(MainActivity.this);
        contactUtils.addContact(MainActivity.this, et_fpath.getText().toString().trim());

        // 发送收起重启广播 无效 可以直接adb命令发广播 adb shell am broadcast --user 0 android.intent.action.BOOT_COMPLETED
        // RebootUtil.sendRebootBroadcast(MainActivity.this);
        // RebootUtil.rebootDevice(MainActivity.this);
    }

    private void initView() {
        et_fpath = findViewById(R.id.et_fpath);

        findViewById(R.id.bt_clear).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                contactUtils.clearContacts(MainActivity.this);
            }
        });
        findViewById(R.id.bt_import).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                contactUtils.addContact(MainActivity.this, et_fpath.getText().toString().trim());
            }
        });
        findViewById(R.id.bt_extract).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this, "导出功能未实现", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initData() {
        this.contactUtils = new ContactUtils();
    }

    private void startServer() {
        try {
            new App();
        } catch (IOException ioe) {
            System.err.println("Couldn't start server:\n" + ioe);
        }
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

}
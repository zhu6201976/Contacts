package com.tesla.contacts;

import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.PersistableBundle;

public class RebootUtil {

    public static void sendRebootBroadcast(Context context) {
        Intent intent = new Intent(Intent.ACTION_REBOOT);
        intent.putExtra("nowait", 1);
        intent.putExtra("interval", 1);
        intent.putExtra("window", 0);
        context.sendBroadcast(intent);
    }

    public static void rebootDevice(Context context) {
        DevicePolicyManager devicePolicyManager = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        ComponentName adminComponent = new ComponentName(context, DeviceAdminReceiver.class);

        if (devicePolicyManager.isAdminActive(adminComponent)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                PersistableBundle persistableBundle = new PersistableBundle();
                persistableBundle.putString("reason", "Reboot for update");
                devicePolicyManager.reboot(adminComponent);
            } else {
                devicePolicyManager.reboot(adminComponent);
            }
        } else {
            // Request admin permission
            Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponent);
            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Permission needed to reboot the device");
            context.startActivity(intent);
        }
    }
}


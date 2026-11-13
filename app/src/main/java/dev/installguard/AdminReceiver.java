package dev.installguard;

import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;

// Being an active device admin stops Install Guard from being uninstalled directly
public class AdminReceiver extends DeviceAdminReceiver {
    @Override
    public CharSequence onDisableRequested(Context c, Intent intent) {
        return c.getString(R.string.admin_disable_warning);
    }
}

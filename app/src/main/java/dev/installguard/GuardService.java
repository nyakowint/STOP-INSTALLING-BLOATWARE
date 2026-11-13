package dev.installguard;

import android.accessibilityservice.AccessibilityService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.accessibility.AccessibilityEvent;

// Watches for new installs and for unapproved apps coming to the foreground
public class GuardService extends AccessibilityService {
    private final BroadcastReceiver installReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context c, Intent intent) {
            if (intent.getData() == null || intent.getBooleanExtra(Intent.EXTRA_REPLACING, false)) return;
            String pkg = intent.getData().getSchemeSpecificPart();
            if (!Guard.isBlocked(c, pkg)) return;
            if (Guard.fromSamsung(c, pkg)) Guard.setApproved(c, pkg, true);
            else Guard.showBlock(c, pkg);
        }
    };

    @Override
    protected void onServiceConnected() {
        IntentFilter filter = new IntentFilter(Intent.ACTION_PACKAGE_ADDED);
        filter.addDataScheme("package");
        registerReceiver(installReceiver, filter);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return;
        CharSequence pkg = event.getPackageName();
        if (pkg != null && Guard.isBlocked(this, pkg.toString())) Guard.showBlock(this, pkg.toString());
    }

    @Override
    public void onInterrupt() {}

    @Override
    public void onDestroy() {
        unregisterReceiver(installReceiver);
        super.onDestroy();
    }
}

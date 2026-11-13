package dev.installguard;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import java.util.List;

// Setup, PIN lock, and the allowlist manager
public class MainActivity extends Activity {
    private boolean unlocked;
    // Set while we open system settings ourselves, so coming back doesn't re-lock
    private boolean keepUnlocked;

    @Override
    protected void onResume() {
        super.onResume();
        keepUnlocked = false;
        if (!Guard.isSetUp(this)) showSetup();
        else if (!unlocked) showPin();
        else showManager();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (!keepUnlocked) unlocked = false;
    }

    private void showSetup() {
        LinearLayout c = Ui.column(this);
        c.addView(Ui.text(this, "Set up Install Guard", 26));
        c.addView(Ui.text(this,
                "Every app on this phone right now will be approved. Anything installed after this "
                        + "gets blocked until you approve it with your PIN.", 17));
        EditText pin = Ui.pinField(this, "New PIN (4+ digits)");
        EditText confirm = Ui.pinField(this, "Confirm PIN");
        c.addView(pin);
        c.addView(confirm);
        c.addView(Ui.button(this, "Start protecting", v -> {
            String p = pin.getText().toString();
            if (p.length() < 4) toast("PIN must be at least 4 digits");
            else if (!p.equals(confirm.getText().toString())) toast("PINs don't match");
            else {
                Guard.setUp(this, p);
                unlocked = true;
                showManager();
            }
        }));
        setScreen(c);
    }

    private void showPin() {
        LinearLayout c = Ui.column(this);
        c.addView(Ui.text(this, "Enter PIN", 26));
        EditText pin = Ui.pinField(this, "PIN");
        c.addView(pin);
        c.addView(Ui.button(this, "Unlock", v -> {
            if (Guard.checkPin(this, pin.getText().toString())) {
                unlocked = true;
                showManager();
            } else {
                pin.setText("");
                toast("Wrong PIN");
            }
        }));
        setScreen(c);
    }

    private void showManager() {
        LinearLayout c = Ui.column(this);
        c.addView(Ui.text(this, "Install Guard", 26));

        if (Guard.serviceEnabled(this)) {
            c.addView(Ui.text(this, "Protection is ON", 18));
        } else {
            c.addView(Ui.text(this, "Protection is OFF. Turn on \"Install Guard\" under Installed apps.", 18));
            c.addView(Ui.button(this, "Open accessibility settings", v ->
                    openSettings(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))));
        }

        ComponentName admin = new ComponentName(this, AdminReceiver.class);
        if (!getSystemService(DevicePolicyManager.class).isAdminActive(admin)) {
            c.addView(Ui.button(this, "Prevent Install Guard removal", v ->
                    openSettings(new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
                            .putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, admin)
                            .putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                                    "Stops Install Guard from being uninstalled without turning this off first."))));
        }

        if (!getSystemService(PowerManager.class).isIgnoringBatteryOptimizations(getPackageName())) {
            c.addView(Ui.button(this, "Keep running in background", this::requestBatteryExemption));
        }

        List<String> blocked = Guard.blockedApps(this);
        c.addView(Ui.text(this, "Blocked apps (" + blocked.size() + ")", 22));
        for (String pkg : blocked) {
            c.addView(Ui.row(this, Guard.label(this, pkg),
                    small("Approve", () -> Guard.setApproved(this, pkg, true)),
                    small("Uninstall", () -> openSettings(
                            new Intent(Intent.ACTION_DELETE, Uri.fromParts("package", pkg, null))))));
        }

        List<String> approved = Guard.approvedApps(this);
        c.addView(Ui.text(this, "Approved apps (" + approved.size() + ")", 22));
        for (String pkg : approved) {
            c.addView(Ui.row(this, Guard.label(this, pkg),
                    small("Revoke", () -> Guard.setApproved(this, pkg, false))));
        }

        c.addView(Ui.text(this, "Change PIN", 22));
        EditText newPin = Ui.pinField(this, "New PIN");
        c.addView(newPin);
        c.addView(Ui.button(this, "Save PIN", v -> {
            String p = newPin.getText().toString();
            if (p.length() < 4) toast("PIN must be at least 4 digits");
            else {
                Guard.setPin(this, p);
                newPin.setText("");
                toast("PIN changed");
            }
        }));

        setScreen(c);
    }

    // Button that runs an action then redraws the manager
    private Button small(String label, Runnable action) {
        Button b = Ui.button(this, label, v -> {
            action.run();
            showManager();
        });
        b.setTextSize(15);
        return b;
    }

    @SuppressLint("BatteryLife")
    private void requestBatteryExemption(android.view.View v) {
        openSettings(new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                Uri.fromParts("package", getPackageName(), null)));
    }

    private void openSettings(Intent intent) {
        keepUnlocked = true;
        startActivity(intent);
    }

    private void setScreen(LinearLayout content) {
        ScrollView scroll = new ScrollView(this);
        scroll.addView(content);
        setContentView(scroll);
    }

    private void toast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }
}

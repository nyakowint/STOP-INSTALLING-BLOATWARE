package dev.installguard;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.provider.Settings;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// Allowlist store and app-classification helpers shared by the service and UI
final class Guard {
    private static final String KEY_PIN = "pin";
    private static final String KEY_APPROVED = "approved";
    private static final String SAMSUNG_INSTALLER = "com.samsung.android.app.omcagent";

    private Guard() {}

    private static SharedPreferences prefs(Context c) {
        return c.getSharedPreferences("guard", Context.MODE_PRIVATE);
    }

    static boolean isSetUp(Context c) {
        return prefs(c).contains(KEY_PIN);
    }

    // Approves every app already installed so only future installs get blocked
    static void setUp(Context c, String pin) {
        prefs(c).edit()
                .putString(KEY_PIN, hash(pin))
                .putStringSet(KEY_APPROVED, new HashSet<>(userApps(c)))
                .apply();
    }

    static boolean checkPin(Context c, String pin) {
        return hash(pin).equals(prefs(c).getString(KEY_PIN, null));
    }

    static void setPin(Context c, String pin) {
        prefs(c).edit().putString(KEY_PIN, hash(pin)).apply();
    }

    static Set<String> approved(Context c) {
        return new HashSet<>(prefs(c).getStringSet(KEY_APPROVED, Set.of()));
    }

    static void setApproved(Context c, String pkg, boolean ok) {
        Set<String> set = approved(c);
        if (ok) set.add(pkg);
        else set.remove(pkg);
        prefs(c).edit().putStringSet(KEY_APPROVED, set).apply();
    }

    static boolean isBlocked(Context c, String pkg) {
        if (!isSetUp(c) || pkg.equals(c.getPackageName())) return false;
        if (prefs(c).getStringSet(KEY_APPROVED, Set.of()).contains(pkg)) return false;
        try {
            return isUserApp(c.getPackageManager().getApplicationInfo(pkg, 0));
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    // Samsung's own region apps (Calendar, Clock, ...) arrive through this installer
    static boolean fromSamsung(Context c, String pkg) {
        try {
            return SAMSUNG_INSTALLER.equals(
                    c.getPackageManager().getInstallSourceInfo(pkg).getInstallingPackageName());
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    static List<String> blockedApps(Context c) {
        List<String> out = new ArrayList<>();
        for (String pkg : userApps(c)) if (isBlocked(c, pkg)) out.add(pkg);
        return out;
    }

    static List<String> approvedApps(Context c) {
        Set<String> approved = approved(c);
        List<String> out = new ArrayList<>();
        for (String pkg : userApps(c)) if (approved.contains(pkg)) out.add(pkg);
        return out;
    }

    // Apps installed by the user, sorted by name. Preinstalled system apps are never blocked
    static List<String> userApps(Context c) {
        PackageManager pm = c.getPackageManager();
        List<String> out = new ArrayList<>();
        for (ApplicationInfo ai : pm.getInstalledApplications(0)) {
            if (isUserApp(ai) && !ai.packageName.equals(c.getPackageName())) out.add(ai.packageName);
        }
        out.sort(Comparator.comparing(pkg -> label(c, pkg).toLowerCase()));
        return out;
    }

    private static boolean isUserApp(ApplicationInfo ai) {
        return (ai.flags & (ApplicationInfo.FLAG_SYSTEM | ApplicationInfo.FLAG_UPDATED_SYSTEM_APP)) == 0;
    }

    static String label(Context c, String pkg) {
        PackageManager pm = c.getPackageManager();
        try {
            return pm.getApplicationLabel(pm.getApplicationInfo(pkg, 0)).toString();
        } catch (PackageManager.NameNotFoundException e) {
            return pkg;
        }
    }

    static void showBlock(Context c, String pkg) {
        c.startActivity(new Intent(c, BlockActivity.class)
                .putExtra(BlockActivity.EXTRA_PKG, pkg)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP));
    }

    static boolean serviceEnabled(Context c) {
        String enabled = Settings.Secure.getString(c.getContentResolver(),
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
        String me = new ComponentName(c, GuardService.class).flattenToString();
        return enabled != null && enabled.contains(me);
    }

    private static String hash(String pin) {
        try {
            byte[] d = MessageDigest.getInstance("SHA-256").digest(pin.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : d) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}

package dev.installguard;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.window.OnBackInvokedDispatcher;

// Full-screen cover shown over any unapproved app
public class BlockActivity extends Activity {
    static final String EXTRA_PKG = "pkg";

    private String pkg;
    private ImageView icon;
    private TextView title;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);

        LinearLayout root = Ui.column(this);
        root.setGravity(Gravity.CENTER);

        icon = new ImageView(this);
        int size = Ui.dp(this, 112);
        root.addView(icon, new LinearLayout.LayoutParams(size, size));

        title = Ui.text(this, "", 30);
        title.setGravity(Gravity.CENTER);
        root.addView(title);

        TextView body = Ui.text(this, "It looks like this app will slow down and mess up your phone. I recommend removing it!", 22);
        body.setGravity(Gravity.CENTER);
        root.addView(body);

        Button remove = bigButton("Remove this app", Color.parseColor("#1E6B4F"), Color.WHITE);
        remove.setOnClickListener(v -> uninstall());
        root.addView(remove, fullWidth(32));

        Button close = bigButton("Not now", Color.parseColor("#DDDDDD"), Color.BLACK);
        close.setOnClickListener(v -> confirmKeep());
        root.addView(close, fullWidth(16));

        TextView manage = Ui.text(this, "Manage", 14);
        manage.setAlpha(0.4f);
        manage.setPadding(Ui.dp(this, 24), Ui.dp(this, 48), Ui.dp(this, 24), Ui.dp(this, 8));
        manage.setOnClickListener(v -> startActivity(new Intent(this, MainActivity.class)));
        LinearLayout.LayoutParams manageLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        manageLp.gravity = Gravity.CENTER_HORIZONTAL;
        root.addView(manage, manageLp);

        setContentView(root);

        if (Build.VERSION.SDK_INT >= 33) {
            getOnBackInvokedDispatcher().registerOnBackInvokedCallback(
                    OnBackInvokedDispatcher.PRIORITY_DEFAULT, this::goHome);
        }
        show(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        show(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Uninstalled or approved since the screen was shown
        if (pkg == null || !Guard.isBlocked(this, pkg)) finish();
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onBackPressed() {
        goHome();
    }

    private void show(Intent intent) {
        pkg = intent.getStringExtra(EXTRA_PKG);
        if (pkg == null) return;
        title.setText(Guard.label(this, pkg));
        try {
            icon.setImageDrawable(getPackageManager().getApplicationIcon(pkg));
        } catch (PackageManager.NameNotFoundException e) {
            icon.setImageDrawable(null);
        }
    }

    private void uninstall() {
        startActivity(new Intent(Intent.ACTION_DELETE, Uri.fromParts("package", pkg, null)));
    }

    private void confirmKeep() {
        TextView msg = Ui.text(this, "Ask a friend or family member if you really need this app.", 20);
        int p = Ui.dp(this, 24);
        msg.setPadding(p, p / 2, p, 0);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Are you sure?")
                .setView(msg)
                .setPositiveButton("Remove it", (d, w) -> uninstall())
                .setNegativeButton("Keep it", (d, w) -> goHome())
                .show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextSize(20);
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextSize(20);
    }

    private Button bigButton(String label, int bg, int fg) {
        Button b = new Button(this);
        b.setText(label);
        b.setAllCaps(false);
        b.setTextSize(24);
        b.setTextColor(fg);
        b.setBackgroundTintList(ColorStateList.valueOf(bg));
        b.setMinHeight(Ui.dp(this, 72));
        return b;
    }

    private LinearLayout.LayoutParams fullWidth(int topMarginDp) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.topMargin = Ui.dp(this, topMarginDp);
        return lp;
    }

    private void goHome() {
        startActivity(new Intent(Intent.ACTION_MAIN)
                .addCategory(Intent.CATEGORY_HOME)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        finish();
    }
}

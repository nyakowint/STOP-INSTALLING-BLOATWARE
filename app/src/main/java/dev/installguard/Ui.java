package dev.installguard;

import android.app.Activity;
import android.content.res.TypedArray;
import android.graphics.Insets;
import android.text.InputType;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.WindowInsets;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

// Small builders for the code-built layouts
final class Ui {
    private Ui() {}

    static int dp(Activity a, int v) {
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v,
                a.getResources().getDisplayMetrics()));
    }

    // Vertical layout padded clear of the status bar, nav bar and keyboard
    static LinearLayout column(Activity a) {
        LinearLayout l = new LinearLayout(a);
        l.setOrientation(LinearLayout.VERTICAL);
        int p = dp(a, 24);
        l.setOnApplyWindowInsetsListener((v, insets) -> {
            Insets bars = insets.getInsets(WindowInsets.Type.systemBars() | WindowInsets.Type.ime());
            v.setPadding(p + bars.left, p + bars.top, p + bars.right, p + bars.bottom);
            return insets;
        });
        return l;
    }

    static TextView text(Activity a, String s, int sp) {
        TextView t = new TextView(a);
        t.setText(s);
        t.setTextSize(TypedValue.COMPLEX_UNIT_SP, sp);
        t.setPadding(0, dp(a, 8), 0, dp(a, 8));
        TypedArray ta = a.obtainStyledAttributes(new int[] {android.R.attr.textColorPrimary});
        t.setTextColor(ta.getColorStateList(0));
        ta.recycle();
        return t;
    }

    static Button button(Activity a, String s, View.OnClickListener onClick) {
        Button b = new Button(a);
        b.setText(s);
        b.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        b.setOnClickListener(onClick);
        return b;
    }

    static EditText pinField(Activity a, String hint) {
        EditText e = new EditText(a);
        e.setHint(hint);
        e.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        e.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
        e.setGravity(Gravity.CENTER);
        return e;
    }

    // App name with action buttons on the right
    static LinearLayout row(Activity a, String label, Button... buttons) {
        LinearLayout r = new LinearLayout(a);
        r.setOrientation(LinearLayout.HORIZONTAL);
        r.setGravity(Gravity.CENTER_VERTICAL);
        TextView t = text(a, label, 17);
        r.addView(t, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        for (Button b : buttons) r.addView(b);
        return r;
    }
}

package com.raival.quicktools.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.raival.quicktools.App;
import com.raival.quicktools.R;

public class ToastUtil {

    public static final int TOAST_NORMAL = 0;
    public static final int TOAST_WARNING = 1;

    public static Toast makeToast(Context context, CharSequence charSequence, int duration) {
        return makeToast(context, charSequence, duration, 48, 0.0f, 64.0f, TOAST_NORMAL);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    public static Toast makeToast(Context context, CharSequence charSequence, int duration, int gravity, float xOffset, float yOffset, int toastType) {
        View inflate = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.custom_toast, null);
        LinearLayout linearLayout = inflate.findViewById(R.id.custom_toast_container);
        TextView textView = inflate.findViewById(R.id.tv_toast);

        if (toastType != TOAST_WARNING) {
            linearLayout.setBackground(context.getDrawable(R.drawable.bg_toast_normal));
            textView.setTextColor(context.getColor(R.color.onSurfaceContrast));
        } else {
            linearLayout.setBackground(context.getDrawable(R.drawable.bg_toast_warning));
            textView.setTextColor(context.getColor(R.color.red));
        }

        textView.setText(charSequence.toString());
        Toast toast = new Toast(context);
        toast.setDuration(duration);
        toast.setGravity(
                gravity,
                (int) xOffset,
                (int) yOffset
        );
        toast.setView(inflate);
        return toast;
    }

    public static Toast makeWarningToast(Context context, CharSequence charSequence, int duration) {
        return makeToast(context, charSequence, duration, 48, 0.0f, 64.0f, TOAST_WARNING);
    }
}
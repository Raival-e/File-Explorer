package com.raival.fileexplorer.common.dialog;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.raival.fileexplorer.R;

import java.util.ArrayList;

public class CustomDialog extends BottomSheetDialogFragment {
    private final ArrayList<View> views = new ArrayList<>();
    private Drawable icon;
    private String title;
    private String msg;
    private String positiveButton;
    private Listener positiveListener;
    private String negativeButton;
    private Listener negativeListener;
    private String neutralButton;
    private Listener neutralListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        return inflater.inflate(R.layout.common_custom_dialog, container, true);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public int getTheme() {
        return R.style.ThemeOverlay_Material3_BottomSheetDialog;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView titleView = view.findViewById(R.id.dialog_title);
        TextView msgView = view.findViewById(R.id.dialog_msg);
        ShapeableImageView imageView = view.findViewById(R.id.dialog_icon);
        LinearLayout containerView = view.findViewById(R.id.dialog_container);
        MaterialButton positiveButtonView = view.findViewById(R.id.dialog_positive_button);
        MaterialButton negativeButtonView = view.findViewById(R.id.dialog_negative_button);
        MaterialButton neutralButtonView = view.findViewById(R.id.dialog_neutral_button);

        if (icon != null) {
            imageView.setVisibility(View.VISIBLE);
            imageView.setImageDrawable(icon);
        }

        if (title != null) {
            titleView.setVisibility(View.VISIBLE);
            titleView.setText(title);
        }

        if (msg != null) {
            msgView.setVisibility(View.VISIBLE);
            msgView.setText(msg);
        }

        if (views.size() > 0) {
            containerView.setVisibility(View.VISIBLE);
            for (View view1 : views) {
                containerView.addView(view1);
            }
        }

        if (positiveButton != null) {
            positiveButtonView.setVisibility(View.VISIBLE);
            positiveButtonView.setText(positiveButton);
            positiveButtonView.setOnClickListener(view1 -> {
                if (positiveListener.listener != null) positiveListener.listener.onClick(view1);
                if (positiveListener.dismiss) dismiss();
            });
        }

        if (negativeButton != null) {
            negativeButtonView.setVisibility(View.VISIBLE);
            negativeButtonView.setText(negativeButton);
            negativeButtonView.setOnClickListener(view1 -> {
                if (negativeListener.listener != null) negativeListener.listener.onClick(view1);
                if (negativeListener.dismiss) dismiss();
            });
        }

        if (neutralButton != null) {
            neutralButtonView.setVisibility(View.VISIBLE);
            neutralButtonView.setText(neutralButton);
            neutralButtonView.setOnClickListener(view1 -> {
                if (neutralListener.listener != null) neutralListener.listener.onClick(view1);
                if (neutralListener.dismiss) dismiss();
            });
        }
    }

    public CustomDialog showDialog(@NonNull FragmentManager fragmentManager, String tag) {
        super.show(fragmentManager, tag);
        return this;
    }

    public String getTitle() {
        return title;
    }

    public CustomDialog setTitle(String title) {
        this.title = title;
        return this;
    }

    public CustomDialog setMsg(String msg) {
        this.msg = msg;
        return this;
    }

    public CustomDialog setIconDrawable(Drawable drawable) {
        icon = drawable;
        return this;
    }

    public CustomDialog setIcon(int resId) {
        icon = AppCompatResources.getDrawable(requireActivity(), resId);
        return this;
    }

    public CustomDialog addView(View view) {
        views.add(view);
        return this;
    }

    public CustomDialog setPositiveButton(String label, View.OnClickListener listener, boolean dismiss) {
        positiveButton = label;
        positiveListener = new Listener(listener, dismiss);
        return this;
    }

    public CustomDialog setNegativeButton(String label, View.OnClickListener listener, boolean dismiss) {
        negativeButton = label;
        negativeListener = new Listener(listener, dismiss);
        return this;
    }

    public CustomDialog setNeutralButton(String label, View.OnClickListener listener, boolean dismiss) {
        neutralButton = label;
        neutralListener = new Listener(listener, dismiss);
        return this;
    }

    private static class Listener {
        public View.OnClickListener listener;
        public boolean dismiss;

        public Listener(View.OnClickListener listener1, boolean dismiss1) {
            listener = listener1;
            dismiss = dismiss1;
        }
    }
}

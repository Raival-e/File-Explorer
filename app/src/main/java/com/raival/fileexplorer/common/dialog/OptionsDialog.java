package com.raival.fileexplorer.common.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.raival.fileexplorer.R;

import java.util.ArrayList;

public class OptionsDialog extends BottomSheetDialogFragment {
    private final String title;
    private final ArrayList<OptionHolder> options = new ArrayList<>();
    private String message;
    private LinearLayout container;

    public OptionsDialog(String title) {
        this.title = title;
    }

    public OptionsDialog(String title, String msg) {
        this.title = title;
        message = msg;
    }

    public void addOption(String label, int resId, View.OnClickListener listener, boolean dismissOnClick) {
        OptionHolder optionHolder = new OptionHolder();
        optionHolder.dismissOnClick = dismissOnClick;
        optionHolder.label = label;
        optionHolder.listener = listener;
        optionHolder.res = resId;
        options.add(optionHolder);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.common_options_dialog, container, false);
    }

    @Override
    public int getTheme() {
        return R.style.ThemeOverlay_Material3_BottomSheetDialog;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        this.container = view.findViewById(R.id.container);
        ((TextView) view.findViewById(R.id.title)).setText(title);

        if (message == null) {
            view.findViewById(R.id.msg).setVisibility(View.GONE);
        } else {
            ((TextView) view.findViewById(R.id.msg)).setText(message);
        }
        addOptions();
    }

    private void addOptions() {
        for (OptionHolder optionHolder : options) {
            View v = getLayoutInflater().inflate(R.layout.common_options_dialog_item, container, false);
            ((ImageView) v.findViewById(R.id.icon)).setImageResource(optionHolder.res);
            ((TextView) v.findViewById(R.id.label)).setText(optionHolder.label);
            v.findViewById(R.id.background).setOnClickListener(view -> {
                optionHolder.listener.onClick(view);
                if (optionHolder.dismissOnClick) dismiss();
            });
            container.addView(v);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        return super.onCreateDialog(savedInstanceState);
    }

    private class OptionHolder {
        String label;
        int res;
        View.OnClickListener listener;
        boolean dismissOnClick;

        public OptionHolder() {
        }
    }
}

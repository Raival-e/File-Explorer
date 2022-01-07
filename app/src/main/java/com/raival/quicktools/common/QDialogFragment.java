package com.raival.quicktools.common;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.button.MaterialButton;
import com.raival.quicktools.R;

import java.util.ArrayList;

public class QDialogFragment extends DialogFragment {
    String title;
    String msg;

    String positiveButton;
    Listener positiveListener;

    String negativeButton;
    Listener negativeListener;

    String neutralButton;
    Listener neutralListener;

    ArrayList<View> views = new ArrayList<>();

    private static class Listener{
        public View.OnClickListener listener;
        public boolean dismiss;
        public Listener(View.OnClickListener listener1, boolean dismiss1){
            listener = listener1;
            dismiss = dismiss1;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.q_dialog_fragment_layout, container, true);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        TextView titleView = view.findViewById(R.id.dialog_title);
        TextView msgView = view.findViewById(R.id.dialog_msg);
        LinearLayout containerView = view.findViewById(R.id.dialog_container);
        MaterialButton positiveButtonView = view.findViewById(R.id.dialog_positive_button);
        MaterialButton negativeButtonView = view.findViewById(R.id.dialog_negative_button);
        MaterialButton neutralButtonView = view.findViewById(R.id.dialog_neutral_button);

        if(msg == null){
            msgView.setVisibility(View.GONE);
        } else {
            msgView.setText(msg);
        }

        if(title == null){
            titleView.setVisibility(View.GONE);
        } else {
            titleView.setText(title);
        }

        if(views.size() == 0){
            containerView.setVisibility(View.GONE);
        } else {
            for(View view1 : views){
                containerView.addView(view1);
            }
        }

        if(positiveButton == null){
            positiveButtonView.setVisibility(View.GONE);
        } else {
            positiveButtonView.setText(positiveButton);
            positiveButtonView.setOnClickListener(view1 -> {
                if(positiveListener.listener!=null) positiveListener.listener.onClick(view1);
                if(positiveListener.dismiss) dismiss();
            });
        }

        if(negativeButton == null){
            negativeButtonView.setVisibility(View.GONE);
        } else {
            negativeButtonView.setText(negativeButton);
            negativeButtonView.setOnClickListener(view1 -> {
                if(negativeListener.listener!=null) negativeListener.listener.onClick(view1);
                if(negativeListener.dismiss) dismiss();
            });
        }

        if(neutralButton == null){
            neutralButtonView.setVisibility(View.GONE);
        } else {
            neutralButtonView.setText(neutralButton);
            neutralButtonView.setOnClickListener(view1 -> {
                if(neutralListener.listener!=null) neutralListener.listener.onClick(view1);
                if(neutralListener.dismiss) dismiss();
            });
        }
    }

    public QDialogFragment showDialog(@NonNull FragmentManager fragmentManager, String tag){
        super.show(fragmentManager, tag);
        return this;
    }

    public String getTitle() {
        return title;
    }

    public QDialogFragment setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getMsg() {
        return msg;
    }

    public QDialogFragment setMsg(String msg) {
        this.msg = msg;
        return this;
    }

    public QDialogFragment addView(View view){
        views.add(view);
        return this;
    }

    public QDialogFragment setPositiveButton(String label, View.OnClickListener listener, boolean dismiss){
        positiveButton = label;
        positiveListener = new Listener(listener, dismiss);
        return this;
    }

    public QDialogFragment setNegativeButton(String label, View.OnClickListener listener, boolean dismiss){
        negativeButton = label;
        negativeListener = new Listener(listener, dismiss);
        return this;
    }

    public QDialogFragment setNeutralButton(String label, View.OnClickListener listener, boolean dismiss){
        neutralButton = label;
        neutralListener = new Listener(listener, dismiss);
        return this;
    }
}

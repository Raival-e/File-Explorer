package com.raival.quicktools.common;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.raival.quicktools.R;
import com.raival.quicktools.interfaces.QTab;
import com.raival.quicktools.interfaces.QTask;

import java.util.ArrayList;

public class TasksDialog extends BottomSheetDialogFragment {
    ArrayList<QTask> tasks;
    ViewGroup container;
    QTab tab;

    public TasksDialog(ArrayList<QTask> tasks, QTab qTab){
        this.tasks = tasks;
        tab = qTab;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.common_tasks_dialog_layout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        container = view.findViewById(R.id.container);

        if(tasks.size() != 0){
            view.findViewById(R.id.place_holder).setVisibility(View.GONE);
        }

        for(QTask task : tasks){
            addTask(task);
        }

    }

    private void addTask(QTask task){
        View v = getLayoutInflater().inflate(R.layout.common_tasks_dialog_item, container, false);

        ((TextView)v.findViewById(R.id.label)).setText(task.getName());
        ((TextView)v.findViewById(R.id.task_details)).setText(task.getDetails());

        v.findViewById(R.id.background).setOnClickListener(view -> {
            tab.handleTask(task);
            tasks.remove(task);
            dismiss();
        });

        container.addView(v);
    }

}

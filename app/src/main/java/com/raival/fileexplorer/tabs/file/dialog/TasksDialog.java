package com.raival.fileexplorer.tabs.file.dialog;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.raival.fileexplorer.App;
import com.raival.fileexplorer.R;
import com.raival.fileexplorer.activities.model.MainViewModel;
import com.raival.fileexplorer.tabs.file.FileExplorerTabFragment;
import com.raival.fileexplorer.tabs.file.model.Task;

import java.util.ArrayList;

public class TasksDialog extends BottomSheetDialogFragment {
    private final FileExplorerTabFragment fileExplorerTabFragment;
    private ViewGroup container;
    private MainViewModel mainViewModel;
    private AlertDialog alertDialog;
    private View placeHolder;

    public TasksDialog(FileExplorerTabFragment explorerTabFragment) {
        super();
        fileExplorerTabFragment = explorerTabFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.file_explorer_tab_task_dialog, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        container = view.findViewById(R.id.container);
        placeHolder = view.findViewById(R.id.place_holder);

        mainViewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        final ArrayList<Task> tasks = mainViewModel.tasks;

        if (!tasks.isEmpty()) {
            placeHolder.setVisibility(View.GONE);
        }

        for (Task task : tasks) {
            addTask(task, task.isValid());
        }
    }

    @Override
    public int getTheme() {
        return R.style.ThemeOverlay_Material3_BottomSheetDialog;
    }

    private void addTask(Task task, boolean valid) {
        View v = getLayoutInflater().inflate(R.layout.file_explorer_tab_task_dialog_item, container, false);

        ((TextView) v.findViewById(R.id.label)).setText(task.getName());
        ((TextView) v.findViewById(R.id.task_details)).setText(task.getDetails());

        v.findViewById(R.id.background).setOnClickListener(view -> {
            if (!valid) {
                App.showWarning("This task is invalid, some files are missing, long click to execute it");
                return;
            }
            run(task);
        });

        v.findViewById(R.id.remove).setOnClickListener(view -> {
            mainViewModel.tasks.remove(task);
            container.removeView(v);
            if (container.getChildCount() == 0) {
                placeHolder.setVisibility(View.GONE);
            }
        });

        v.setOnLongClickListener((view -> {
            if (!valid) {
                run(task);
                return true;
            }
            return false;
        }));

        container.addView(v);
    }

    private void run(Task task) {
        dismiss();

        task.setActiveDirectory(fileExplorerTabFragment.getCurrentDirectory());

        View view = getProgressView();
        TextView progress = view.findViewById(R.id.msg);
        progress.setText("Processing...");

        alertDialog = new MaterialAlertDialogBuilder(requireActivity())
                .setCancelable(false)
                .setView(view)
                .show();

        task.start(progress::setText, result -> {
            mainViewModel.tasks.remove(task);
            App.showMsg(result);
            fileExplorerTabFragment.refresh();
            alertDialog.dismiss();
        });
    }

    private View getProgressView() {
        return requireActivity().getLayoutInflater().inflate(R.layout.progress_view, null);
    }
}

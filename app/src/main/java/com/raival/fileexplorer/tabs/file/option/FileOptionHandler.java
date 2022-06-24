package com.raival.fileexplorer.tabs.file.option;

import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;
import com.raival.fileexplorer.App;
import com.raival.fileexplorer.R;
import com.raival.fileexplorer.activities.MainActivity;
import com.raival.fileexplorer.activities.TextEditorActivity;
import com.raival.fileexplorer.activities.model.MainViewModel;
import com.raival.fileexplorer.common.BackgroundTask;
import com.raival.fileexplorer.common.dialog.CustomDialog;
import com.raival.fileexplorer.common.dialog.OptionsDialog;
import com.raival.fileexplorer.tabs.file.FileExplorerTabFragment;
import com.raival.fileexplorer.tabs.file.dialog.FileInfoDialog;
import com.raival.fileexplorer.tabs.file.dialog.SearchDialog;
import com.raival.fileexplorer.tabs.file.executor.Executor;
import com.raival.fileexplorer.tabs.file.model.FileItem;
import com.raival.fileexplorer.tabs.file.tasks.APKSignerTask;
import com.raival.fileexplorer.tabs.file.tasks.CompressTask;
import com.raival.fileexplorer.tabs.file.tasks.CopyTask;
import com.raival.fileexplorer.tabs.file.tasks.CutTask;
import com.raival.fileexplorer.tabs.file.tasks.ExtractTask;
import com.raival.fileexplorer.tabs.file.tasks.Jar2DexTask;
import com.raival.fileexplorer.utils.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;

public class FileOptionHandler {
    private final FileExplorerTabFragment parentFragment;
    private MainViewModel mainViewModel;

    public FileOptionHandler(FileExplorerTabFragment parentFragment) {
        this.parentFragment = parentFragment;
    }

    public void showOptions(FileItem fileItem) {
        ArrayList<File> selectedFiles = new ArrayList<>();
        for (FileItem item : parentFragment.getSelectedFiles()) {
            selectedFiles.add(item.file);
        }
        if (!fileItem.isSelected) selectedFiles.add(fileItem.file);

        String title = "";
        if (selectedFiles.size() == 1) {
            title = fileItem.file.getName();
        } else {
            title = "" + selectedFiles.size() + " Files selected";
        }

        OptionsDialog bottomDialog = new OptionsDialog(title);
        bottomDialog.show(parentFragment.getParentFragmentManager(), "FileOptionsDialog");

        //______________| Options |_______________\\

        if (FileUtils.isSingleFile(selectedFiles)) {
            if (selectedFiles.get(0).getName().toLowerCase().endsWith(".java")
                    || selectedFiles.get(0).getName().toLowerCase().endsWith(".kt")) {
                bottomDialog.addOption("Execute", R.drawable.ic_round_code_24, view1 -> {
                    exeJava(selectedFiles.get(0).getParentFile());
                }, true);
            }
        }

        if (FileUtils.isSingleFolder(selectedFiles)) {
            bottomDialog.addOption("Open in a new tab", R.drawable.ic_round_tab_24, view1 -> {
                if (parentFragment.requireActivity() instanceof MainActivity) {
                    FileExplorerTabFragment fragment = new FileExplorerTabFragment(selectedFiles.get(0));
                    ((MainActivity) parentFragment.requireActivity()).addNewTab(fragment, "FileExplorerTabFragment_" + ((MainActivity) parentFragment.requireActivity()).generateRandomTag());
                }
            }, true);
        }

        if (FileUtils.isOnlyFiles(selectedFiles)) {
            bottomDialog.addOption("Share", R.drawable.ic_round_share_24, view1 -> {
                FileUtils.shareFiles(selectedFiles, parentFragment.requireActivity());
                parentFragment.setSelectAll(false);
            }, true);
        }

        if (FileUtils.isSingleFile(selectedFiles)) {
            bottomDialog.addOption("Open with", R.drawable.ic_baseline_open_in_new_24, view1 -> {
                FileUtils.openFileWith(selectedFiles.get(0), true);
                parentFragment.setSelectAll(false);
            }, true);
        }

        bottomDialog.addOption("Copy", R.drawable.ic_baseline_file_copy_24, view1 -> {
            getMainViewModel().tasks.add(new CopyTask(selectedFiles));
            parentFragment.setSelectAll(false);
            notifyNewTask();
        }, true);

        bottomDialog.addOption("Cut", R.drawable.ic_round_content_cut_24, view1 -> {
            getMainViewModel().tasks.add(new CutTask(selectedFiles));
            parentFragment.setSelectAll(false);
            notifyNewTask();
        }, true);

        if (selectedFiles.size() == 1) {
            bottomDialog.addOption("Rename", R.drawable.ic_round_edit_24, view1 -> {
                showRenameDialog(selectedFiles);
            }, true);
        }

        bottomDialog.addOption("Delete", R.drawable.ic_round_delete_forever_24, view1 -> {
            confirmDeletion(selectedFiles);
        }, true);

        if (FileUtils.isSingleFile(selectedFiles)) {
            bottomDialog.addOption("Edit with code editor", R.drawable.ic_round_edit_note_24, view1 -> {
                openWithTextEditor(selectedFiles.get(0));
            }, true);
        }

        if (FileUtils.isArchiveFiles(selectedFiles)) {
            bottomDialog.addOption("Extract", R.drawable.ic_baseline_logout_24, view1 -> {
                getMainViewModel().tasks.add(new ExtractTask(selectedFiles));
                parentFragment.setSelectAll(false);
                notifyNewTask();
            }, true);
        }

        bottomDialog.addOption("Compress", R.drawable.ic_round_compress_24, view1 -> {
            compressFiles(new CompressTask(selectedFiles));
        }, true);

        if (FileUtils.isSingleFile(selectedFiles)) {
            if (FileUtils.getFileExtension(selectedFiles.get(0)).equalsIgnoreCase("jar")) {
                bottomDialog.addOption("Jar2Dex", R.drawable.ic_round_code_24, view1 -> {
                    jar2Dex(new Jar2DexTask(selectedFiles.get(0)));
                }, true);
            }
        }

        if (FileUtils.isSingleFile(selectedFiles)) {
            if (FileUtils.getFileExtension(selectedFiles.get(0)).equalsIgnoreCase("apk")) {
                bottomDialog.addOption("Sign with test key", R.drawable.ic_round_key_24, view1 -> {
                    signApkWithTestKey(new APKSignerTask(selectedFiles.get(0)));
                }, true);
            }
        }

        if (selectedFiles.size() == 1) {
            bottomDialog.addOption("Details", R.drawable.ic_baseline_info_24, view1 -> {
                showFileInfoDialog(selectedFiles.get(0));
            }, true);
        }

        bottomDialog.addOption("Search", R.drawable.ic_round_manage_search_24, view1 -> {
            SearchDialog searchFragment = new SearchDialog(parentFragment, selectedFiles);
            searchFragment.show(parentFragment.getParentFragmentManager(), "");
            parentFragment.setSelectAll(false);
        }, true);
    }

    private void jar2Dex(Jar2DexTask task) {
        if (task.isValid()) {
            task.setActiveDirectory(parentFragment.getCurrentDirectory());

            View view = getProgressView();
            TextView progress = view.findViewById(R.id.msg);
            progress.setText("Processing...");

            AlertDialog dialog = getDialog().setView(view).show();

            task.start(progress::setText, result -> {
                App.showMsg(result);
                parentFragment.refresh();
                dialog.dismiss();
            });
        } else {
            App.showMsg("The jar file doesn't exist");
        }
    }

    private void compressFiles(CompressTask task) {
        TextInputLayout input = (TextInputLayout) parentFragment.requireActivity().getLayoutInflater().inflate(R.layout.input, null, false);
        input.setHint("Archive name");
        input.getEditText().setText(".zip");
        FileUtils.setFileValidator(input, parentFragment.getCurrentDirectory());

        new CustomDialog()
                .setTitle("Compress")
                .addView(input)
                .setPositiveButton("Save", _view -> {
                    if (input.getError() == null) {
                        if (task.isValid()) {
                            task.setActiveDirectory(new File(parentFragment.getCurrentDirectory(), input.getEditText().getText().toString()));

                            View view = getProgressView();
                            TextView progress = view.findViewById(R.id.msg);
                            progress.setText("Processing...");

                            AlertDialog dialog = getDialog().setView(view).show();

                            task.start(progress::setText, result -> {
                                App.showMsg(result);
                                parentFragment.refresh();
                                dialog.dismiss();
                            });
                        } else {
                            App.showMsg("The files to compress are missing");
                        }
                    } else {
                        App.showMsg("Compress canceled");
                    }
                }, true)
                .showDialog(parentFragment.getChildFragmentManager(), "");
    }

    private void openWithTextEditor(File file) {
        Intent intent = new Intent();
        intent.setClass(parentFragment.requireActivity(), TextEditorActivity.class);
        intent.putExtra("file", file.getAbsolutePath());
        parentFragment.requireActivity().startActivity(intent);
    }

    private MainViewModel getMainViewModel() {
        if (mainViewModel == null) {
            mainViewModel = new ViewModelProvider(parentFragment.requireActivity()).get(MainViewModel.class);
        }
        return mainViewModel;
    }

    private void notifyNewTask() {
        App.showMsg("A new task has been added");
    }

    private void showFileInfoDialog(File file) {
        new FileInfoDialog(file).setUseDefaultFileInfo(true).show(parentFragment.getParentFragmentManager(), "");
    }

    private void signApkWithTestKey(APKSignerTask task) {
        if (task.isValid()) {
            task.setActiveDirectory(parentFragment.getCurrentDirectory());

            View view = getProgressView();
            TextView progress = view.findViewById(R.id.msg);
            progress.setText("Processing...");

            AlertDialog dialog = getDialog().setView(view).show();

            task.start(progress::setText, result -> {
                App.showMsg(result);
                parentFragment.refresh();
                dialog.dismiss();
            });
        } else {
            App.showMsg("The APK file doesn't exist");
        }
    }

    private View getProgressView() {
        return parentFragment.requireActivity().getLayoutInflater().inflate(R.layout.progress_view, null);
    }

    private AlertDialog.Builder getDialog() {
        return new MaterialAlertDialogBuilder(parentFragment.requireActivity())
                .setCancelable(false);
    }

    private void exeJava(File file) {
        Executor executor = new Executor(file, (AppCompatActivity) parentFragment.requireActivity());
        BackgroundTask backgroundTask = new BackgroundTask();

        AtomicReference<String> error = new AtomicReference<>("");

        backgroundTask.setTasks(() -> {
            backgroundTask.showProgressDialog("compiling files...", parentFragment.requireActivity());
        }, () -> {
            try {
                executor.execute();
            } catch (Exception exception) {
                error.set(App.getStackTrace(exception));
            }
        }, () -> {
            try {
                if (!error.get().equals("")) {
                    backgroundTask.dismiss();
                    App.log(error.get());
                    parentFragment.showDialog("Error", error.get());
                    return;
                }
                executor.invoke();
                backgroundTask.dismiss();
            } catch (Exception exception) {
                backgroundTask.dismiss();
                App.log(exception);
                parentFragment.showDialog("Error", App.getStackTrace(exception));
            }
            parentFragment.refresh();
        });
        backgroundTask.run();
    }

    private void showRenameDialog(ArrayList<File> selectedFiles) {
        TextInputLayout input = (TextInputLayout) parentFragment.getLayoutInflater().inflate(R.layout.input, null, false);
        input.setHint("File name");
        input.getEditText().setText(selectedFiles.get(0).getName());
        input.getEditText().setSingleLine();
        FileUtils.setFileValidator(input, selectedFiles.get(0), selectedFiles.get(0).getParentFile());

        new CustomDialog()
                .setTitle("Rename")
                .addView(input)
                .setPositiveButton("Save", view -> {
                    if (input.getError() == null) {
                        if (!FileUtils.rename(selectedFiles.get(0), input.getEditText().getText().toString())) {
                            App.showMsg("Cannot rename this file");
                        } else {
                            App.showMsg("File has been renamed");
                            parentFragment.refresh();
                        }
                    } else {
                        App.showMsg("Rename canceled");
                    }
                }, true)
                .showDialog(parentFragment.getParentFragmentManager(), "");
    }

    private void confirmDeletion(ArrayList<File> selectedFiles) {
        new CustomDialog()
                .setTitle("Delete")
                .setMsg("Do you want to delete selected files? this action cannot be redone.")
                .setPositiveButton("Confirm", (view -> doDelete(selectedFiles)), true)
                .setNegativeButton("Cancel", null, true)
                .showDialog(parentFragment.getParentFragmentManager(), "");
    }

    private void doDelete(ArrayList<File> selectedFiles) {
        parentFragment.setSelectAll(false);
        BackgroundTask backgroundTask = new BackgroundTask();
        backgroundTask.setTasks(() -> {
            backgroundTask.showProgressDialog("Deleting files...", parentFragment.requireActivity());
        }, () -> FileUtils.deleteFiles(selectedFiles), () -> {
            backgroundTask.dismiss();
            App.showMsg("Files have been deleted");
            parentFragment.refresh();
        });
        backgroundTask.run();
    }
}

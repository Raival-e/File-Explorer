package com.raival.quicktools.utils;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;

import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.raival.quicktools.App;
import com.raival.quicktools.MainActivity;
import com.raival.quicktools.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Locale;

public class FileUtil {
    public final static String INTERNAL_STORAGE = "Internal Storage";
    public static Comparator<File> sortFoldersFirst(){
        return (file1, file2) -> {
            if (file1.isDirectory() && !file2.isDirectory()) {
                return -1;
            } else if (!file1.isDirectory() && file2.isDirectory()) {
                return 1;
            } else {
                return 0;
            }
        };
    }

    public static Comparator<File> sortFilesFirst(){
        return (file2, file1) -> {
            if (file1.isDirectory() && !file2.isDirectory()) {
                return -1;
            } else if (!file1.isDirectory() && file2.isDirectory()) {
                return 1;
            } else {
                return 0;
            }
        };
    }

    public static Comparator<File> sortDateAsc(){
        return Comparator.comparingLong(File::lastModified);
    }

    public static Comparator<File> sortDateDesc(){
        return (file1, file2) -> Long.compare(file2.lastModified(), file1.lastModified());
    }

    public static Comparator<File> sortNameAsc(){
        return Comparator.comparing(file -> file.getName().toLowerCase(Locale.getDefault()));
    }

    public static Comparator<File> sortNameDesc(){
        return (file1, file2) -> file2.getName().toLowerCase(Locale.getDefault()).compareTo(file1.getName().toLowerCase(Locale.getDefault()));
    }

    public static Comparator<File> sortSizeAsc(){
        return Comparator.comparingLong(File::length);
    }

    public static Comparator<File> sortSizeDesc(){
        return (file1, file2) -> Long.compare(file2.length(), file1.length());
    }

    public static void setFileIcon(ImageView icon, File file){
        if(!file.isFile()){
            icon.setImageResource(R.drawable.folder_icon);
            return;
        }

        final String ext = getFileExtension(file).toLowerCase();

        if(ext.equals(FileExtensions.apkType)){
            PackageInfo info = App.appContext.getPackageManager().getPackageArchiveInfo(file.getAbsolutePath(), PackageManager.GET_ACTIVITIES);
            if(info != null){
                ApplicationInfo applicationInfo = info.applicationInfo;
                applicationInfo.sourceDir = file.getAbsolutePath();
                applicationInfo.publicSourceDir = file.getAbsolutePath();
                icon.setImageDrawable(applicationInfo.loadIcon(App.appContext.getPackageManager()));
            }
            return;
        }
        if(ext.equals(FileExtensions.pdfType)){
            icon.setImageResource(R.drawable.pdf_file);
            return;
        }
        if(isTextFile(ext)){
            icon.setImageResource(R.drawable.text_file);
            return;
        }
        if(isCodeFile(ext)){
            icon.setImageResource(R.drawable.java_file);
            return;
        }
        if(isArchiveFile(ext) || ext.equals(FileExtensions.rarType)){
            icon.setImageResource(R.drawable.zip_file);
            return;
        }
        if(isVideoFile(ext)){
            Glide.with(App.appContext)
                    .load(file)
                    .error(R.drawable.video_file)
                    .placeholder(R.drawable.video_file)
                    .into(icon);
            return;
        }
        if(isAudioFile(ext)){
            icon.setImageResource(R.drawable.sound_file);
            return;
        }
        if(isImageType(ext)){
            Glide.with(App.appContext)
                    .applyDefaultRequestOptions(new RequestOptions().override(65).encodeQuality(30))
                    .load(file)
                    .error(R.drawable.unknown_file)
                    .into(icon);
            return;
        }
        icon.setImageResource(R.drawable.unknown_file);
    }

    public static boolean isExternalStorageFolder(File file){
        return file.getAbsolutePath().equals(Environment.getExternalStorageDirectory().getAbsolutePath());
    }

    private static boolean isImageType(String ext) {
        for(String extension : FileExtensions.imageType){
            if(extension.equals(ext))
                return true;
        }
        return false;
    }

    public static String getFormattedFileCount(File file) {
        final String noItemsString = "Empty folder";
        if(file.isFile()){
         return noItemsString;
        }

        int files = 0;
        int folders = 0;
        final File[] fileList = file.listFiles();

        if(fileList == null){
            return noItemsString;
        }

        for(File item : fileList){
            if(item.isFile()) files++;
            else folders++;
        }
        StringBuilder sb = new StringBuilder();
        if(folders > 0){
            sb.append(folders);
            sb.append(" folder");
            if(folders > 1) sb.append("s");
            if(files > 0) sb.append(", ");
        }
        if(files > 0){
            sb.append(files);
            sb.append(" file");
            if(files > 1) sb.append("s");
        }
        return (folders==0&&files==0)? noItemsString : sb.toString();
    }

    private static boolean isAudioFile(String extension) {
        for(String ext : FileExtensions.audioType){
            if(extension.equals(ext))
                return true;
        }
        return false;
    }

    private static boolean isVideoFile(String ext) {
        for(String extension : FileExtensions.videoType){
            if(extension.equals(ext))
                return true;
        }
        return false;
    }

    private static boolean isArchiveFile(String ext) {
        for(String extension : FileExtensions.archiveType){
            if(extension.equals(ext))
                return true;
        }
        return false;
    }

    private static boolean isCodeFile(String ext) {
        for(String extension : FileExtensions.codeType){
            if(extension.equals(ext))
                return true;
        }
        return false;
    }

    private static boolean isTextFile(String ext) {
        for(String extension : FileExtensions.textType){
            if(extension.equals(ext))
                return true;
        }
        return false;
    }

    /**
     * @return A filename without its extension,
     * e.g. "FileUtil" for "FileUtil.java", or "FileUtil" for "/sdcard/Documents/FileUtil.java"
     */
    public static String getFileNameNoExtension(File file) {
        String filePath = file.getAbsolutePath();
        if (filePath.trim().isEmpty()) return "";

        int lastPos = filePath.lastIndexOf('.');
        int lastSep = filePath.lastIndexOf(File.separator);

        if (lastSep == -1) {
            return (lastPos == -1 ? filePath : filePath.substring(0, lastPos));
        } else if (lastPos == -1 || lastSep > lastPos) {
            return filePath.substring(lastSep + 1);
        }
        return filePath.substring(lastSep + 1, lastPos);
    }

    public static String getFileExtension(File file){
        final String name = file.getName();
        final int last = name.lastIndexOf(".");
        if(name.isEmpty() || !name.contains(".") || last == -1){
            return "";
        }
        return name.substring(last + 1);
    }



    public static void writeFile(File file, String content) throws IOException{
        if(!file.createNewFile()){
            throw new IOException("Cannot create file " + file.getAbsolutePath());
        }
        FileWriter fileWriter = new FileWriter(file, false);
        fileWriter.write(content);
        fileWriter.flush();
        fileWriter.close();
    }

    public static String getMimeTypeFromFile(File file) {
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(getFileExtension(file));
    }

    public static void openFileWith(File file, boolean anonymous){
        Intent i = new Intent(Intent.ACTION_VIEW);
        Uri uri = FileProvider.getUriForFile(App.appContext, App.appContext.getPackageName() + ".provider", file);
        i.setDataAndType(uri, anonymous? "*/*" : getMimeTypeFromFile(file));
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        i.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        try {
            App.appContext.startActivity(i);
        } catch (ActivityNotFoundException e) {
            if(!anonymous){
                openFileWith(file, true);
            } else {
                App.showMsg("Couldn't find any app that can open this type of files");
            }
        } catch (Exception e){
            App.showMsg("Cannot open this file");
        }
    }


    public static boolean isSingleFolder(ArrayList<File> selectedFiles) {
        return (selectedFiles.size() == 1 && !selectedFiles.get(0).isFile());
    }

    public static boolean isOnlyFolders(ArrayList<File> selectedFiles) {
        for(File file : selectedFiles){
            if(file.isFile())
                return false;
        }
        return true;
    }

    public static boolean isSingleFile(ArrayList<File> selectedFiles) {
        return (selectedFiles.size() == 1 && selectedFiles.get(0).isFile());
    }

    public static boolean isOnlyFiles(ArrayList<File> selectedFiles) {
        for(File file : selectedFiles){
            if(!file.isFile())
                return false;
        }
        return true;
    }

    public static boolean isSingleArchive(ArrayList<File> selectedFiles) {
        return (selectedFiles.size() == 1
                && (isArchiveFile(getFileExtension(selectedFiles.get(0)))
                || getFileExtension(selectedFiles.get(0)).equals(FileExtensions.apkType)
                ||getFileExtension(selectedFiles.get(0)).equals(FileExtensions.rarType)));
    }

    public static String getFormattedFileSize(File file) {
        final long size = file.length();
        if(size <= 0) return "0 B";
        final String[] units = new String[] { "B", "kB", "MB", "GB", "TB" };
        int digitGroups = (int) (Math.log10(size)/Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size/Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    public static void copyFiles(ArrayList<File> selectedFiles, File destination) throws IOException{
        for(File file : selectedFiles){
            copy(file, destination);
        }
    }

    private static void copy(File file, File to) throws IOException{
        if(file.isFile()){
            copyFile(file, new File(to, file.getName()));
        } else {
            File parent = new File(to, file.getName());
            if(parent.mkdir()) {
                final File[] files = file.listFiles();
                if (files != null) {
                    for (File child : files) {
                        copy(child, parent);
                    }
                }
            } else {
                throw new IOException("Cannot create directory " + parent.getAbsolutePath());
            }
        }
    }

    private static void copyFile(File sourcePath, File destPath) throws IOException {
        if(!destPath.createNewFile()){
            throw new IOException("Cannot create file " + destPath.getAbsolutePath());
        }

        FileInputStream fis = null;
        FileOutputStream fos = null;

        fis = new FileInputStream(sourcePath);
        fos = new FileOutputStream(destPath, false);

        byte[] buff = new byte[1024];
        int length = 0;

        while ((length = fis.read(buff)) > 0) {
            fos.write(buff, 0, length);
        }

        fis.close();
        fos.close();
    }

    private static void deleteFile(File file) {
        if (!file.exists()) return;

        if (file.isFile()) {
            file.delete();
            return;
        }

        File[] fileArr = file.listFiles();

        if (fileArr != null) {
            for (File subFile : fileArr) {
                if (subFile.isDirectory()) {
                    deleteFile(subFile);
                }

                if (subFile.isFile()) {
                    subFile.delete();
                }
            }
        }

        file.delete();
    }

    public static void deleteFiles(ArrayList<File> selectedFiles) {
        for(File file : selectedFiles){
            deleteFile(file);
        }
    }
}

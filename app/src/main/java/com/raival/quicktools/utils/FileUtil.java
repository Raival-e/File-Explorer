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
import android.webkit.MimeTypeMap;
import android.widget.ImageView;

import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.raival.quicktools.App;
import com.raival.quicktools.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Locale;

public class FileUtil {
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
        final String ext = getFileExtension(file).toLowerCase();
        if(!file.isFile()){
            icon.setImageResource(R.drawable.folder_icon);
            return;
        }
        if(isTextFile(ext)){
            icon.setImageResource(R.drawable.text_file);
            return;
        }
        if(isCodeFile(ext)){
            icon.setImageResource(R.drawable.code_file);
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
            icon.setImageResource(R.drawable.audio_file);
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
        icon.setImageResource(R.drawable.unknown_file);
    }

    private static boolean isImageType(String ext) {
        for(String extension : FileExtensions.imageType){
            if(extension.equals(ext))
                return true;
        }
        return false;
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

    /**
     * Copies an entire directory, recursively.
     *
     * @param source   The directory whose contents to copy.
     * @param copyInto The directory to copy files into.
     * @throws IOException Thrown when something goes wrong while copying.
     */
    public static void copyDirectory(File source, File copyInto) throws IOException {
        if (!source.isDirectory()) {
            File parentFile = copyInto.getParentFile();
            if (parentFile == null || parentFile.exists() || parentFile.mkdirs()) {
                FileInputStream fileInputStream = new FileInputStream(source);
                FileOutputStream fileOutputStream = new FileOutputStream(copyInto);
                byte[] bArr = new byte[2048];
                while (true) {
                    int read = fileInputStream.read(bArr);
                    if (read <= 0) {
                        fileInputStream.close();
                        fileOutputStream.close();
                        return;
                    }
                    fileOutputStream.write(bArr, 0, read);
                }
            } else {
                throw new IOException("Cannot create dir " + parentFile.getAbsolutePath());
            }
        } else if (copyInto.exists() || copyInto.mkdirs()) {
            String[] list = source.list();
            if (list != null) {
                for (String s : list) {
                    copyDirectory(new File(source, s), new File(copyInto, s));
                }
            }
        } else {
            throw new IOException("Cannot create dir " + copyInto.getAbsolutePath());
        }
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
}

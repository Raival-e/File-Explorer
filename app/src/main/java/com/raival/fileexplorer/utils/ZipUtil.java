package com.raival.fileexplorer.utils;

import net.lingala.zip4j.ZipFile;

import java.io.File;
import java.util.ArrayList;

public class ZipUtil {

    public static void archive(ArrayList<File> filesToCompress, File zipFile) throws Exception {
        ZipFile zip = new ZipFile(zipFile);
        for (File file : filesToCompress) {
            if (file.isFile()) {
                zip.addFile(file);
            } else {
                zip.addFolder(file);
            }
        }
    }

    public static void extract(ArrayList<File> filesToExtract, File directory) throws Exception {
        for (File file : filesToExtract) {
            if (file.isFile()) {
                File output = new File(directory, file.getName().substring(0, file.getName().lastIndexOf(".")));
                if (output.mkdir()) {
                    new ZipFile(file).extractAll(output.getAbsolutePath());
                } else {
                    new ZipFile(file).extractAll(directory.getAbsolutePath());
                }
            }
        }
    }
}

package com.raival.quicktools.exe.java;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

public class JarPackager
{
    private final String input;
    private final String output;
    private final Attributes attr;

    private static Attributes getDefAttrs() {
        Attributes attrs = new Attributes();
        //Thanks to Hey!Studios
        attrs.put(new Attributes.Name("Created-By"), "heystudios");

        return attrs;
    }

    public JarPackager(String input, String output) {
        this(input, output, getDefAttrs());
    }

    public JarPackager(String input, String output, Attributes attr) {
        this.input = input;
        this.output = output;
        this.attr = attr;
    }

    public void create() throws IOException {
        //input file
        File classesFolder = new File(input);

        // Open archive file
        FileOutputStream stream = new FileOutputStream(new File(output));

        Manifest manifest = buildManifest(attr);

        //Create the jar file
        JarOutputStream out = new JarOutputStream(stream, manifest);

        //Add the files..
        if (classesFolder.listFiles() != null) {
            for (File clazz : classesFolder.listFiles()) {
                add(classesFolder.getPath(), clazz, out);
            }
        }

        out.close();
        stream.close();
    }

    private Manifest buildManifest(Attributes options) {
        Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        if (options != null) {
            manifest.getMainAttributes().putAll(options);
        }
        return manifest;
    }

    private void add(String parentPath, File source, JarOutputStream target) throws IOException {
        String name = source.getPath().substring(parentPath.length() + 1);

        BufferedInputStream in = null;
        try {
            if (source.isDirectory()) {
                if (!name.isEmpty()) {
                    if (!name.endsWith("/"))
                        name += "/";

                    //Add the Entry
                    JarEntry entry = new JarEntry(name);
                    entry.setTime(source.lastModified());
                    target.putNextEntry(entry);
                    target.closeEntry();
                }

                for (File nestedFile : source.listFiles()) {
                    add(parentPath, nestedFile, target);
                }
                return;
            }

            JarEntry entry = new JarEntry(name);
            entry.setTime(source.lastModified());
            target.putNextEntry(entry);
            in = new BufferedInputStream(new FileInputStream(source));
            byte[] buffer = new byte[1024];
            while (true) {
                int count = in.read(buffer);
                if (count == -1)
                    break;
                target.write(buffer, 0, count);
            }
            target.closeEntry();

        } finally {
            if (in != null) {
                in.close();
            }
        }
    }
}
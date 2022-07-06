package com.raival.fileexplorer.tab.file.executor;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.android.tools.r8.D8;
import com.raival.fileexplorer.tab.file.util.D8Utils;
import com.raival.fileexplorer.tab.file.util.FileUtils;

import org.jetbrains.kotlin.cli.common.arguments.K2JVMCompilerArguments;
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity;
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSourceLocation;
import org.jetbrains.kotlin.cli.common.messages.MessageCollector;
import org.jetbrains.kotlin.cli.jvm.K2JVMCompiler;
import org.jetbrains.kotlin.config.Services;

import java.io.File;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Objects;

public class Executor {
    private final ArrayList<File> javaFiles = new ArrayList<>();
    private final ArrayList<File> kotlinFiles = new ArrayList<>();
    private final ArrayList<File> jarFiles = new ArrayList<>();
    private File project;
    private File output;
    private File libs;

    private AppCompatActivity activity;

    public Executor(File folder, AppCompatActivity activity) {
        if (folder.isFile()) {
            return;
        }
        this.activity = activity;
        project = folder;
        parseInputFolder(project);
    }

    public void execute() throws Exception {
        if (!clearOutput()) {
            throw new Exception("Failed cleaning output folder");
        }
        if (kotlinFiles.size() > 0) {
            compileKotlin();
        }
        if (javaFiles.size() > 0) {
            runECJ();
        }
        runD8();
    }

    public void invoke() throws Exception {
        new DexRunner(new File(output, "classes.extension"), activity).setProjectDir(project).setLibsDir(libs).run();
    }

    private void runD8() {
        ArrayList<String> opt = new ArrayList<>();

        opt.add("--intermediate");
        opt.add("--lib");
        opt.add(D8Utils.getBootstrapJarFile().getAbsolutePath());
        opt.add("--output");
        opt.add(output.getAbsolutePath());
        ArrayList<String> classes = FileUtils.getAllFilesInDir(new File(output, "classes"), "class");
        if (classes.size() > 0)
            opt.addAll(classes);

        D8.main(opt.toArray(new String[0]));

        // Rename dex files to .extension to be able to run them with ExtensionRunner
        for (File file : Objects.requireNonNull(output.listFiles())) {
            if (file.isFile()) {
                if (file.getName().endsWith(".dex")) {
                    final String path = file.getAbsolutePath();
                    file.renameTo(new File(path.substring(0, path.length() - 3) + "extension"));
                }
            }
        }
    }

    /**
     * Sources:
     * - https://github.com/Sketchware-Pro/Sketchware-Pro/tree/main/app/src/minApi26/java/mod/hey/studios/compiler/kotlin
     * - https://github.com/MikeAndrson/kotlinc-android
     */
    private void compileKotlin() throws Exception {
        final File ktHome = new File(output, "ktHome");
        if (ktHome.mkdir()) {
            throw new Exception("Unable to create file: " + ktHome);
        }
        final File classes = new File(output, "classes");
        if (!classes.mkdir()) {
            throw new Exception("Unable to create file: " + classes);
        }

        K2JVMCompiler k2JVMCompiler = new K2JVMCompiler();
        DiagnosticCollector messageCollector = new DiagnosticCollector();

        final ArrayList<String> args = new ArrayList<>();
        args.add("-cp");

        final StringBuilder sb = new StringBuilder();
        for (File jar : jarFiles) sb.append(":").append(jar.getAbsolutePath());
        sb.append(":").append(D8Utils.getBootstrapJarFile().getAbsolutePath());
        args.add(sb.substring(1));

        for (File file : kotlinFiles) {
            args.add(file.getAbsolutePath());
        }
        for (File file : javaFiles) {
            args.add(file.getAbsolutePath());
        }

        K2JVMCompilerArguments compilerArguments = new K2JVMCompilerArguments();
        compilerArguments.setCompileJava(false);
        compilerArguments.setIncludeRuntime(false);
        compilerArguments.setNoJdk(true);
        compilerArguments.setNoReflect(true);
        compilerArguments.setNoStdlib(true);
        compilerArguments.setKotlinHome(ktHome.getAbsolutePath());
        compilerArguments.setDestination(classes.getAbsolutePath());

        k2JVMCompiler.parseArguments(args.toArray(new String[0]), compilerArguments);
        k2JVMCompiler.exec(messageCollector, Services.EMPTY, compilerArguments);

        final File file = new File(classes, "META-INF");
        if (file.exists()) FileUtils.deleteFile(file);

        if (messageCollector.hasErrors()) {
            throw new Exception(messageCollector.getDiagnostics());
        }
    }

    private void runECJ() throws Exception {
        ArrayList<String> opt = new ArrayList<>();
        final File classes = new File(output, "classes");
        if (!classes.mkdir()) {
            throw new Exception("Unable to create file: " + classes);
        }

        opt.add("-11");
        opt.add("-nowarn");
        opt.add("-deprecation");
        opt.add("-d");
        opt.add(classes.getAbsolutePath());
        opt.add("-cp");

        final StringBuilder sb = new StringBuilder();
        for (File jar : jarFiles)
            sb.append(":").append(jar.getAbsolutePath());

        sb.append(":")
                .append(classes.getAbsolutePath())
                .append(":")
                .append(D8Utils.getLambdaStubsJarFile().getAbsolutePath())
                .append(":")
                .append(D8Utils.getBootstrapJarFile().getAbsolutePath());
        opt.add(sb.substring(1));

        opt.add("-proc:none");
        opt.add("-sourcepath");
        opt.add(" ");
        for (File file : javaFiles)
            opt.add(file.getAbsolutePath());

        PrintWriter printWriter = new PrintWriter(new OutputStream() {
            @Override
            public void write(int i) {
            }
        });
        final StringBuilder errors = new StringBuilder();
        PrintWriter printWriter1 = new PrintWriter(new OutputStream() {
            @Override
            public void write(int i) {
                errors.append((char) i);
            }
        });

        org.eclipse.jdt.internal.compiler.batch.Main main = new org.eclipse.jdt.internal.compiler.batch.Main(
                printWriter,
                printWriter1,
                false,
                null,
                null);
        main.compile(opt.toArray(new String[0]));

        if (main.globalErrorsCount > 0) {
            throw new Exception(errors.toString());
        }
    }

    private boolean clearOutput() throws Exception {
        if (output == null) {
            output = new File(project, "output");
            return output.mkdir();
        }

        FileUtils.deleteFile(output);
        return output.mkdir();
    }

    private void parseInputFolder(File input) {
        for (File file : Objects.requireNonNull(input.listFiles())) {
            if (file.isFile()) {
                if (file.getName().toLowerCase().endsWith(".java")) {
                    javaFiles.add(file);
                } else if (file.getName().toLowerCase().endsWith(".kt")) {
                    kotlinFiles.add(file);
                }
            } else {
                if (file.getName().equals("output")) {
                    output = file;
                } else if (file.getName().equals("libs")) {
                    libs = file;
                    for (File subFile : Objects.requireNonNull(file.listFiles())) {
                        if (subFile.isFile()) {
                            if (subFile.getName().toLowerCase().endsWith(".jar")) {
                                jarFiles.add(subFile);
                            }
                        }
                    }
                }
            }

        }
    }

    /**
     * Sources:
     * - https://github.com/Sketchware-Pro/Sketchware-Pro/tree/main/app/src/minApi26/java/mod/hey/studios/compiler/kotlin
     * - https://github.com/MikeAndrson/kotlinc-android
     */
    private static class DiagnosticCollector implements MessageCollector {
        private final ArrayList<Diagnostic> diagnostics = new ArrayList<>();

        @Override
        public void clear() {
            diagnostics.clear();
        }

        @Override
        public void report(@NonNull CompilerMessageSeverity compilerMessageSeverity, @NonNull String s, CompilerMessageSourceLocation compilerMessageSourceLocation) {
            diagnostics.add(new Diagnostic(compilerMessageSeverity, s, compilerMessageSourceLocation));
        }

        @Override
        public boolean hasErrors() {
            for (Diagnostic diagnostic : diagnostics) {
                if (diagnostic.compilerMessageSeverity.isError()) return true;
            }
            return false;
        }

        public String getDiagnostics() {
            final StringBuilder sb = new StringBuilder();
            int i = 1;
            for (Diagnostic diagnostic : diagnostics) {
                if (diagnostic.compilerMessageSeverity.isError()) {
                    sb.append("----------");
                    sb.append(System.lineSeparator());
                    sb.append(i);
                    sb.append(". ");
                    sb.append(diagnostic);
                    sb.append(System.lineSeparator());
                    i++;
                }
            }
            return sb.toString();
        }

        @NonNull
        @Override
        public String toString() {
            return getDiagnostics();
        }
    }

    /**
     * Sources:
     * - https://github.com/Sketchware-Pro/Sketchware-Pro/tree/main/app/src/minApi26/java/mod/hey/studios/compiler/kotlin
     * - https://github.com/MikeAndrson/kotlinc-android
     */
    private static class Diagnostic {
        public final CompilerMessageSeverity compilerMessageSeverity;
        public final String message;
        public final CompilerMessageSourceLocation compilerMessageSourceLocation;

        private Diagnostic(CompilerMessageSeverity compilerMessageSeverity, String message, CompilerMessageSourceLocation compilerMessageSourceLocation) {
            this.compilerMessageSeverity = compilerMessageSeverity;
            this.message = message;
            this.compilerMessageSourceLocation = compilerMessageSourceLocation;
        }

        @NonNull
        @Override
        public String toString() {
            final StringBuilder stringBuilder = new StringBuilder();

            stringBuilder.append(compilerMessageSeverity.getPresentableName());
            if (compilerMessageSourceLocation != null) {
                stringBuilder.append(" in ");
                stringBuilder.append(compilerMessageSourceLocation.getPath());
                stringBuilder.append(" (at ");
                stringBuilder.append(compilerMessageSourceLocation.getLine());
                stringBuilder.append(":");
                stringBuilder.append(compilerMessageSourceLocation.getColumn());
                stringBuilder.append(")");
                stringBuilder.append(System.lineSeparator());
                final String content = compilerMessageSourceLocation.getLineContent();
                if (content != null) stringBuilder.append(content);
            }
            stringBuilder.append(System.lineSeparator());
            stringBuilder.append(message);

            return stringBuilder.toString();
        }
    }
}

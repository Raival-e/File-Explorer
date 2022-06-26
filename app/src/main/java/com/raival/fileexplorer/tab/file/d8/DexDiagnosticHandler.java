package com.raival.fileexplorer.tab.file.d8;

import android.util.Log;

import com.android.tools.r8.Diagnostic;
import com.android.tools.r8.DiagnosticsHandler;
import com.android.tools.r8.DiagnosticsLevel;

public class DexDiagnosticHandler implements DiagnosticsHandler {

    private static final String METAINF_ERROR = "Resource 'META-INF/MANIFEST.MF' already exists.";

    public DexDiagnosticHandler() {

    }

    @Override
    public void error(Diagnostic diagnostic) {
        Log.println(Log.ERROR, "D8 Error", wrap(diagnostic, DiagnosticsLevel.ERROR).toString());
    }

    @Override
    public void warning(Diagnostic diagnostic) {
        Log.println(Log.ERROR, "D8 Error", wrap(diagnostic, DiagnosticsLevel.WARNING).toString());
    }

    @Override
    public void info(Diagnostic diagnostic) {
        Log.println(Log.ERROR, "D8 Error", wrap(diagnostic, DiagnosticsLevel.INFO).toString());
    }

    @Override
    public DiagnosticsLevel modifyDiagnosticsLevel(DiagnosticsLevel diagnosticsLevel,
                                                   Diagnostic diagnostic) {
        if (diagnostic.getDiagnosticMessage().equals(METAINF_ERROR)) {
            return DiagnosticsLevel.WARNING;
        }

        Log.d("DiagnosticHandler", diagnostic.getDiagnosticMessage());
        return diagnosticsLevel;
    }

    private DiagnosticWrapper wrap(Diagnostic diagnostic, DiagnosticsLevel level) {
        DiagnosticWrapper wrapper = new DiagnosticWrapper();
        wrapper.setMessage(diagnostic.getDiagnosticMessage());
        switch (level) {
            case WARNING:
                wrapper.setKind(javax.tools.Diagnostic.Kind.WARNING);
                break;
            case ERROR:
                wrapper.setKind(javax.tools.Diagnostic.Kind.ERROR);
                break;
            case INFO:
                wrapper.setKind(javax.tools.Diagnostic.Kind.NOTE);
                break;
        }
        return wrapper;
    }
}

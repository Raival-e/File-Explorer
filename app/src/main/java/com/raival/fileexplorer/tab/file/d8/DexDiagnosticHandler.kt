package com.raival.fileexplorer.tab.file.d8

import android.util.Log
import com.android.tools.r8.Diagnostic
import com.android.tools.r8.DiagnosticsHandler
import com.android.tools.r8.DiagnosticsLevel

class DexDiagnosticHandler : DiagnosticsHandler {
    override fun error(diagnostic: Diagnostic) {
        Log.println(Log.ERROR, "D8 Error", wrap(diagnostic, DiagnosticsLevel.ERROR).toString())
    }

    override fun warning(diagnostic: Diagnostic) {
        Log.println(Log.ERROR, "D8 Error", wrap(diagnostic, DiagnosticsLevel.WARNING).toString())
    }

    override fun info(diagnostic: Diagnostic) {
        Log.println(Log.ERROR, "D8 Error", wrap(diagnostic, DiagnosticsLevel.INFO).toString())
    }

    override fun modifyDiagnosticsLevel(
        diagnosticsLevel: DiagnosticsLevel,
        diagnostic: Diagnostic
    ): DiagnosticsLevel {
        if (diagnostic.diagnosticMessage == METAINF_ERROR) {
            return DiagnosticsLevel.WARNING
        }
        Log.d("DiagnosticHandler", diagnostic.diagnosticMessage)
        return diagnosticsLevel
    }

    private fun wrap(diagnostic: Diagnostic, level: DiagnosticsLevel): DiagnosticWrapper {
        val wrapper = DiagnosticWrapper()
        wrapper.setMessage(diagnostic.diagnosticMessage)
        when (level) {
            DiagnosticsLevel.WARNING -> wrapper.kind = javax.tools.Diagnostic.Kind.WARNING
            DiagnosticsLevel.ERROR -> wrapper.kind = javax.tools.Diagnostic.Kind.ERROR
            DiagnosticsLevel.INFO -> wrapper.kind = javax.tools.Diagnostic.Kind.NOTE
        }
        return wrapper
    }

    companion object {
        private const val METAINF_ERROR = "Resource 'META-INF/MANIFEST.MF' already exists."
    }
}
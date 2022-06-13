package com.raival.fileexplorer.d8;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.util.Locale;
import java.util.Objects;

import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

public class DiagnosticWrapper implements Diagnostic<File> {
    public static final int USE_LINE_POS = -31;

    private String code;
    private File source;
    private Diagnostic.Kind kind;

    private long position;
    private long startPosition;
    private long endPosition;

    private long lineNumber;
    private long columnNumber;
    private View.OnClickListener onClickListener;
    private String message;

    /**
     * Extra information for this diagnostic
     */
    private Object mExtra;
    private int startLine;
    private int endLine;
    private int startColumn;
    private int endColumn;

    public DiagnosticWrapper() {

    }

    public DiagnosticWrapper(Diagnostic<? extends JavaFileObject> obj) {
        try {
            this.code = obj.getCode();
            if (obj.getSource() != null) {
                this.source = new File(obj.getSource().toUri());
            }
            this.kind = obj.getKind();

            this.position = obj.getPosition();
            this.startPosition = obj.getStartPosition();
            this.endPosition = obj.getEndPosition();

            this.lineNumber = obj.getLineNumber();
            this.columnNumber = obj.getColumnNumber();

            this.message = obj.getMessage(Locale.getDefault());
        } catch (Throwable e) {
            // ignored
        }
    }

    public View.OnClickListener getOnClickListener() {
        return onClickListener;
    }

    public void setOnClickListener(View.OnClickListener listener) {
        onClickListener = listener;
    }

    @Override
    public Kind getKind() {
        return kind;
    }

    public void setKind(Kind kind) {
        this.kind = kind;
    }

    @Override
    public File getSource() {
        return source;
    }

    public void setSource(File source) {
        this.source = source;
    }

    @Override
    public long getPosition() {
        return position;
    }

    public void setPosition(long position) {
        this.position = position;
    }

    @Override
    public long getStartPosition() {
        return startPosition;
    }

    public void setStartPosition(long startPosition) {
        this.startPosition = startPosition;
    }

    @Override
    public long getEndPosition() {
        return endPosition;
    }

    public void setEndPosition(long endPosition) {
        this.endPosition = endPosition;
    }

    @Override
    public long getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(long lineNumber) {
        this.lineNumber = lineNumber;
    }

    @Override
    public long getColumnNumber() {
        return columnNumber;
    }

    public void setColumnNumber(long columnNumber) {
        this.columnNumber = columnNumber;
    }

    @Override
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @Override
    public String getMessage(Locale locale) {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getExtra() {
        return mExtra;
    }

    public void setExtra(Object mExtra) {
        this.mExtra = mExtra;
    }

    @NonNull
    @Override
    public String toString() {
        return "startOffset: " + startPosition + "\n" +
                "endOffset: " + endPosition + "\n" +
                "position: " + position + "\n" +
                "startLine: " + startLine + "\n" +
                "startColumn: " + startColumn + "\n" +
                "endLine: " + endLine + "\n" +
                "endColumn: " + endColumn + "\n" +
                "message: " + message;
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, source, kind, position, startPosition, endPosition, lineNumber,
                columnNumber, message, mExtra);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj instanceof DiagnosticWrapper) {
            DiagnosticWrapper that = (DiagnosticWrapper) obj;

            if (that.message != null && this.message == null) {
                return false;
            }

            if (that.message == null && this.message != null) {
                return false;
            }

            if (!Objects.equals(that.message, this.message)) {
                return false;
            }

            if (!Objects.equals(that.source, this.source)) {
                return false;
            }

            if (that.lineNumber != this.lineNumber) {
                return false;
            }

            return that.columnNumber == this.columnNumber;
        }
        return super.equals(obj);
    }

    public int getStartLine() {
        return startLine;
    }

    public void setStartLine(int line) {
        this.startLine = line;
    }

    public int getEndLine() {
        return endLine;
    }

    public void setEndLine(int endLine) {
        this.endLine = endLine;
    }

    public int getStartColumn() {
        return startColumn;
    }

    public void setStartColumn(int startColumn) {
        this.startColumn = startColumn;
    }

    public int getEndColumn() {
        return endColumn;
    }

    public void setEndColumn(int endColumn) {
        this.endColumn = endColumn;
    }
}

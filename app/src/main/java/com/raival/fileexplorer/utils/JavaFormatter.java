package com.raival.fileexplorer.utils;

import java.util.Arrays;
import java.util.stream.Collectors;

public class JavaFormatter {
    private final String source;
    private final StringBuilder result = new StringBuilder();

    public JavaFormatter(String source) {
        this.source = source;
    }

    public String format() {
        final char[] charArray = trimSource().toCharArray();
        final int length = charArray.length;
        int index = 0;
        int indents = 0;

        boolean isSingleLineComment = false;
        boolean isMultiLineComment = false;
        boolean isJavaDoc = false;
        boolean isEscape = false;
        boolean isChar = false;
        boolean isString = false;

        while (index < length) {
            final char currentChar = charArray[index];
            final int nextCharIndex = index + 1;
            final boolean isValidNextChar = isValidIndex(nextCharIndex, charArray);

            if (isSingleLineComment) {
                if (currentChar == '\n') {
                    result.append(currentChar);
                    addIndent(indents);
                    isSingleLineComment = false;
                } else {
                    result.append(currentChar);
                }
            } else if (isEscape) {
                result.append(currentChar);
                isEscape = false;
            } else if (currentChar == '\\') {
                result.append(currentChar);
                isEscape = true;
            } else if (isChar) {
                if (currentChar == '\'') {
                    result.append(currentChar);
                    isChar = false;
                } else {
                    result.append(currentChar);
                }
            } else if (isString) {
                if (currentChar == '\"') {
                    result.append(currentChar);
                    isString = false;
                } else {
                    result.append(currentChar);
                }
            } else {
                if (isMultiLineComment) {
                    if (currentChar == '*') {
                        if (isValidNextChar) {
                            final char nextChar = charArray[nextCharIndex];
                            if (nextChar == '/') {
                                isMultiLineComment = false;
                                isJavaDoc = false;
                            }
                        }
                    }
                } else {
                    if (currentChar == '/') {
                        if (isValidNextChar) {
                            final char nextChar = charArray[nextCharIndex];
                            if (nextChar == '/') {
                                result.append(currentChar);
                                result.append(nextChar);
                                isSingleLineComment = true;
                                index = nextCharIndex + 1;
                                continue;
                            }
                            if (nextChar == '*') {
                                result.append(currentChar);
                                result.append(nextChar);
                                isMultiLineComment = true;
                                index = nextCharIndex + 1;
                                continue;
                            }
                        }
                    }

                    if (currentChar == '\'') {
                        isChar = true;
                    }

                    if (currentChar == '\"') {
                        isString = true;
                    }
                }

                if (!isJavaDoc) {
                    if (currentChar == '{') {
                        ++indents;
                    }

                    if (currentChar == '}') {
                        --indents;
                        if (result.charAt(result.length() - 1) == '\t') {
                            result.deleteCharAt(result.length() - 1);
                        }
                    }
                }

                result.append(currentChar);

                if (currentChar == '\n') {
                    addIndent(indents);
                    if (isMultiLineComment) {
                        if (isValidNextChar) {
                            final char nextChar = charArray[nextCharIndex];
                            if (nextChar == '*') {
                                isJavaDoc = true;
                                result.append(" ");
                            } else {
                                isJavaDoc = false;
                            }
                        }
                    }
                    if (isValidNextChar) {
                        final char nextChar = charArray[nextCharIndex];
                        if (nextChar == '.' || nextChar == '?' || nextChar == ':' || nextChar == '&' || nextChar == '|' || nextChar == '+') {
                            result.append("\t");
                        }
                    }
                }

            }
            ++index;
        }
        return result.toString();
    }

    private boolean isValidIndex(int index, char[] source) {
        return index < source.length && index > -1;
    }

    private void addIndent(int indents) {
        for (int i = 0; i < indents; i++) {
            result.append('\t');
        }
    }

    private String trimSource() {
        return Arrays.stream(source.split("\n"))
                .map(String::trim)
                .collect(Collectors.joining("\n"));
    }
}
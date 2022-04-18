package com.raival.quicktools.utils;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

public class CodeFormatter {
    public static String xmlFormat(String xml) throws Exception {
        XmlPullParser xpp = XmlPullParserFactory.newInstance().newPullParser();
        StringReader reader = new StringReader(xml);
        xpp.setInput(reader);

        int currentTag = xpp.getEventType();
        while (currentTag != XmlPullParser.START_DOCUMENT) {
            currentTag = xpp.next();
        }
        ArrayList<String> tags = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        String indent = "";
        boolean isEmpty = false;
        currentTag = xpp.next();
        while (currentTag != XmlPullParser.END_DOCUMENT) {
            if (currentTag == XmlPullParser.END_TAG) {
                indent = indent.substring(1);
                int lastItem = tags.size() - 1;
                if (lastItem < 0) lastItem = 0;
                if (!isEmpty) sb.append(indent).append("</").append(tags.get(lastItem)).append(">").append("\n");
                tags.remove(lastItem);
                isEmpty = false;
            } else if (currentTag == XmlPullParser.START_TAG) {
                final String tagName = xpp.getName();
                tags.add(tagName);
                sb.append(indent).append("<").append(tagName);
                indent = indent + "	";
                final int attrCount = xpp.getAttributeCount();
                if (attrCount > 0) sb.append("\n");
                for (int i = 0; i < attrCount; ++i) {
                    final String attrName = xpp.getAttributeName(i);
                    final String attrValue = xpp.getAttributeValue(i);
                    sb.append(indent).append(attrName).append("=\"").append(attrValue).append("\"").append(i == (attrCount - 1) ? "" : "\n");
                }
                if (xpp.isEmptyElementTag()) {
                    sb.append("/>" + "\n");
                    isEmpty = true;
                } else {
                    sb.append(">" + "\n");
                    isEmpty = false;
                }
            }
            currentTag = xpp.next();
        }

        reader.close();
        return sb.toString();
    }

    public static String javaFormat(String text) {
        return javaCodeFormat(Arrays.stream(text.split("\n"))
                .map(String::trim)
                .collect(Collectors.joining("\n")));
    }

    private static void addIndents(StringBuilder content, int indents) {
        for (int i = 0; i < indents; i++) {
            content.append('\t');
        }
    }

    private static String javaCodeFormat(final String s) {
        final StringBuilder sb = new StringBuilder(4096);
        final char[] charArray = s.toCharArray();
        final int length = charArray.length;
        int currentCharIndex = 0;
        boolean isSingleLineComment = false;
        boolean isMultiLinesComment = false;
        boolean isBackSlash = false;
        int indents = 0;
        boolean isSingleQuote = false;
        boolean IsDoubleQuotes = false;
        while (currentCharIndex < length) {
            final char currentChar = charArray[currentCharIndex];
            int currentLineIndex;
            boolean currentLineQuotesOpened;
            label_line:{
                label_currentChar:{
                    if (isSingleLineComment) {
                        if (currentChar == '\n') {
                            sb.append(currentChar);
                            addIndents(sb, indents);
                            isSingleLineComment = false;
                        } else {
                            sb.append(currentChar);
                        }
                    } else if (isMultiLinesComment) {
                        if (currentChar == '*') {
                            final int nextCharIndex = currentCharIndex + 1;
                            final char c2 = charArray[nextCharIndex];
                            if (c2 == '/') {
                                sb.append(currentChar);
                                sb.append(c2);
                                isMultiLinesComment = false;
                                currentCharIndex = nextCharIndex;
                                break label_currentChar;
                            }
                        }
                        sb.append(currentChar);
                        //commit
                        if (currentChar == '\n')
                            addIndents(sb, indents);
                    } else if (isBackSlash) {
                        sb.append(currentChar);
                        isBackSlash = false;
                    } else if (currentChar == '\\') {
                        sb.append(currentChar);
                        isBackSlash = true;
                    } else if (isSingleQuote) {
                        if (currentChar == '\'') {
                            sb.append(currentChar);
                            isSingleQuote = false;
                        } else {
                            sb.append(currentChar);
                        }
                    } else if (IsDoubleQuotes) {
                        if (currentChar == '\"') {
                            sb.append(currentChar);
                            IsDoubleQuotes = false;
                        } else {
                            sb.append(currentChar);
                        }
                    } else {
                        //check start of comments
                        if (currentChar == '/') {
                            final int nextCharIndex = currentCharIndex + 1;
                            final char nextChar = charArray[nextCharIndex];
                            //start of one line comment
                            if (nextChar == '/') {
                                sb.append(currentChar);
                                sb.append(nextChar);
                                isSingleLineComment = true;
                                currentCharIndex = nextCharIndex;
                                break label_currentChar;
                            }
                            //start of multiple lines comment
                            if (nextChar == '*') {
                                sb.append(currentChar);
                                sb.append(nextChar);
                                isMultiLinesComment = true;
                                currentCharIndex = nextCharIndex;
                                break label_currentChar;
                            }
                        }
                        if (currentChar != '\n') {
                            if (currentChar == '\'') {
                                isSingleQuote = true;
                            }
                            boolean doubleQuotes;
                            doubleQuotes = currentChar == '\"';
                            if (currentChar == '{') {
                                ++indents;
                            }
                            currentLineIndex = indents;
                            if (currentChar == '}') {
                                currentLineIndex = --indents;
                                if (sb.charAt(sb.length() - 1) == '\t') {
                                    sb.deleteCharAt(sb.length() - 1);
                                    currentLineIndex = indents;
                                }
                            }
                            sb.append(currentChar);
                            currentLineQuotesOpened = doubleQuotes;
                            break label_line;
                        }
                        sb.append(currentChar);
                        addIndents(sb, indents);
                    }
                }
                currentLineIndex = indents;
                currentLineQuotesOpened = IsDoubleQuotes;
            }
            ++currentCharIndex;
            IsDoubleQuotes = currentLineQuotesOpened;
            indents = currentLineIndex;
        }
        return sb.toString();
    }
}

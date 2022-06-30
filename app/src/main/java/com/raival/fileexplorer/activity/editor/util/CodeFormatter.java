package com.raival.fileexplorer.activity.editor.util;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.StringReader;
import java.util.ArrayList;

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
                if (!isEmpty)
                    sb.append(indent).append("</").append(tags.get(lastItem)).append(">").append("\n");
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

    public static String internalJavaFormat(String text) {
        return new JavaFormatter(text).format();
    }
}

/*
 * Copyright (C) 2016 Minglangjun Li <liminglangjun@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.osfans.mcpdict;


import org.xml.sax.XMLReader;
import android.text.Html;
import android.text.Editable;
import android.text.Spannable;

/**
 * Simple ruby tag handler which may only work with well-formed
 * HTML document. Because RubySpan is inherited from ReplacementSpan,
 * this tag handler should not be used when you need to style
 * the text (e.g. changing the text color). For such complex cases,
 * you'd better write a descent HTML parser.
 */
public class RubyTagHandler implements Html.TagHandler {

    private String ruby;

    @Override
    public void handleTag(boolean opening, String tag, Editable output, XMLReader xmlReader) {
        if (opening) {
            if (tag.equalsIgnoreCase("ruby")) {
                start(output, new RubyTag());
            } else if (tag.equalsIgnoreCase("rt")) {
                start(output, new RtTag());
            } else if (tag.equalsIgnoreCase("rb")) {
                start(output, new RbTag());
            } else if (tag.equalsIgnoreCase("rp")) {
                start(output, new RpTag());
            } else {
                // unknown tags, leave it out
            }
        } else {
            if (tag.equalsIgnoreCase("ruby") || tag.equalsIgnoreCase("rb") ||
                    tag.equalsIgnoreCase("rt") || tag.equalsIgnoreCase("rp")) {
                handleTagInternal(output);
            }
        }
    }

    private void handleTagInternal(Editable output) {
        Object obj = getLast(output, RubyBaseTag.class);
        if (obj == null) {
            return;
        }
        int len = output.length();
        int where = output.getSpanStart(obj);

        output.removeSpan(obj);
        if (obj instanceof RpTag) {
            // simply ignore <rp> currently
            output.delete(where, len);
        } else if (obj instanceof RbTag) {
        } else if (obj instanceof RtTag) {
            ruby = output.subSequence(where, len).toString();
            output.delete(where, len);
        } else { // <ruby>
            output.setSpan(new RubySpan(ruby), where, len, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    private static Object getLast(Spannable text, Class kind) {
        Object[] objs = text.getSpans(0, text.length(), kind);
        if (objs.length == 0) {
            return null;
        } else {
            return objs[objs.length - 1];
        }
    }

    private static void start(Spannable text, Object mark) {
        int len = text.length();
        text.setSpan(mark, len, len, Spannable.SPAN_MARK_MARK);
    }

    private abstract class RubyBaseTag {}
    private class RubyTag extends RubyBaseTag {}
    private class RbTag extends RubyBaseTag {}
    private class RtTag extends RubyBaseTag {}
    private class RpTag extends RubyBaseTag {}
}

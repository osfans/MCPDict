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


import android.graphics.Paint;
import android.graphics.Canvas;
import android.graphics.Rect;
import androidx.annotation.NonNull;
import android.text.Spanned;
import android.text.style.ReplacementSpan;

public final class RubySpan extends ReplacementSpan {

    private static final String TAG = "RubySpan";

    // font measureSize scale for ruby
    private static final float FONTSIZE_SCALE = 0.5f;

    // constants that used to adjust text spacing for optimal layout
    private static final float FACTORL = 0.5f;
    private static final float FACTORS = 0.2f;

    private String ruby;
    private int rubyLength;

    private boolean useColor;
    private int color;

    private boolean underline;

    /**
     * Describes how the ruby width is relative to the text width.
     *
     *  UNKNOWN: hasn't been determined
     *  NORMAL: requires no special treatment
     *  TOO_LONG: ruby is too long, the text should be stretched
     *  TOO_SHORT: ruby is too short, itself should be stretched
     */
    enum RelativeLength {
        UNKNOWN,
        NORMAL,
        TOO_LONG,
        TOO_SHORT
    }

    private RelativeLength relativeLength = RelativeLength.UNKNOWN;


    // compensatory width for either text or annotation depends on which is shorter
    private float widthDiff;

    // raw metrics
    private float fontSize, textWidth, rubyWidth;

    private float newTextWidth, newRubyWidth;

    // horizontal offset of ruby
    private float offsetX;

    // whether there is a ruby span right before head
    private boolean consecutiveRuby;

    // extra horizontal offset to avoid messing up with the previous RubySpan
    private float extraSkip;

    // space left between the current text and the right border of the
    // underlying canvas
    // This value should be set in draw() and should only be queried
    // in the next RubySpan.
    private float spaceLeft;
    private boolean enoughSpaceLeft;

    private float measureSize;

    private Paint.FontMetrics fm;

    public RubySpan(@NonNull String ruby) {
        init(ruby, false, 0, false);
    }

    public RubySpan(@NonNull String ruby, int color) {
        init(ruby, true, color, false);
    }

    public RubySpan(@NonNull String ruby, boolean underline) {
        init(ruby, false, 0, underline);
    }

    public RubySpan(@NonNull String ruby, int color, boolean underline) {
        init(ruby, true, color, underline);
    }

    private void init(String ruby, boolean useColor, int color, boolean underline) {
        this.ruby = ruby;
        this.useColor = useColor;
        this.color = color;
        this.underline = underline;
    }

    /**
     * Returns the width that ruby exceeds the text end.
     * This method is supposed to be called in the next RubySpan.
     */
    public float getExceededWidth() {
        return Math.max(0, newRubyWidth - newTextWidth);
    }

    /**
     * Returns the width between the text end and the right border
     * of the canvas. This method is supposed to used only in the
     * next RubySpan.
     */
    public float getSpaceLeft() {
        return spaceLeft;
    }

    private void measureRun(Paint paint, CharSequence text, int start, int end) {

        final int textLength = end - start;
        rubyLength = ruby.length();
        fontSize = paint.getTextSize();
        textWidth = fontSize * textLength;
        fm = paint.getFontMetrics();
        rubyWidth = fontSize * FONTSIZE_SCALE * rubyLength;

        /**
         * Layout strategy:
         * 1) Don't do anything if the text contains only one character.
         * 2) Align ruby and text if they have equal number of characters.
         * 3) Stretch the text if `rubyWith > textWidth + FACTORS * fontSize`.
         * 4) Stretch the ruby if the text has fewer characters and `rubyWith < textWidth`.
         */
        newTextWidth = textWidth;
        newRubyWidth = rubyWidth;
        if (textLength == rubyLength) {
            relativeLength = RelativeLength.TOO_SHORT;
            // both left and right have half textSize left
            widthDiff = textWidth - FACTORL * fontSize - rubyWidth;
            newRubyWidth = rubyWidth + widthDiff;
        } else if (textLength > 1 && rubyWidth > textWidth + FACTORS * fontSize) {
            relativeLength = RelativeLength.TOO_LONG;
            widthDiff = rubyWidth - (textWidth + FACTORS * fontSize);
            newTextWidth = textWidth + widthDiff;
        } else if (textLength > 1 && textLength < rubyLength && rubyWidth < textWidth) {
            relativeLength = RelativeLength.TOO_SHORT;
            widthDiff = textWidth - FACTORS * fontSize - rubyWidth;
            newRubyWidth = textWidth - FACTORS * fontSize;
        } else {
            relativeLength = RelativeLength.NORMAL;
        }

        offsetX = (newTextWidth - newRubyWidth) / 2;

        // FIXME If the ruby is too long, it will exceed the right border
        // and becomes partially invisible.
        measureSize = newTextWidth;

        Object[] objs = ((Spanned)text).getSpans(0, start, this.getClass());
        int len = objs.length;
        if (len > 0 && ((Spanned)text).getSpanEnd(objs[len - 1]) == start) {
            consecutiveRuby = true;
            final RubySpan prevRubySpan = (RubySpan)objs[len - 1];

            /* ------------------------------------------------------
             *
             *       exceeded width   abs(offsetX)
             *                   |       |
             *                   v       v
             *                 |  |   |     |
             *  ruby ruby ruby ruby   ruby ruby ruby ruby ruby ruby
             *    TEXT TEXT TEXT            TEXT TEXT TEXT
             *
             * ------------------------------------------------------
             */
            extraSkip = prevRubySpan.getExceededWidth();
            extraSkip += Math.max(0, -offsetX) + 5;

            // FIXME buggy
            float spaceLeft = prevRubySpan.getSpaceLeft();
            if (spaceLeft > measureSize + extraSkip) {
                enoughSpaceLeft = true;
                // FIXME
                measureSize += extraSkip;
            } else {
                // FIXME In this case, does the text automatically goes to the next line?
                enoughSpaceLeft = false;
            }
        } else {
            consecutiveRuby = false;
            enoughSpaceLeft = false;
            extraSkip = 0;
        }
    }

    @Override
    public int getSize(Paint paint, CharSequence text,
                       int start, int end, Paint.FontMetricsInt fm) {
        if (start >= end) {
            return 0;
        }
        measureRun(paint, text, start, end);
        return (int)measureSize;
    }

    @Override
    public void draw(Canvas canvas, CharSequence text, int start, int end,
                     float x, int top, int y, int bottom, Paint paint) {
        if (start >= end) {
            return;
        }

        // getSize() always runs before this method. There's no need to measure again.
        //measureRun(paint, text, start, end);

        final int oldColor = paint.getColor();
        if (useColor) {
            paint.setColor(color);
        }
        final boolean oldUnderline = paint.isUnderlineText();
        paint.setUnderlineText(underline);

        // FIXME hard coded constant, needs more testing
        final float offsetY = fm.ascent * 1.1f;

        /* avoid drawings outside the borders being clipped out
         * leftmost case
         */
        Rect bounds = new Rect();
        canvas.getClipBounds(bounds);  // canvas.left is 0

        spaceLeft = bounds.right - x - measureSize;

        // Translate the canvas to avoid overlapping with previous RubySpan.
        if (x + offsetX >= bounds.left && consecutiveRuby && enoughSpaceLeft) {
            canvas.save();
            canvas.translate(extraSkip, 0);
        }

        // Draw the text
        if (relativeLength == RelativeLength.TOO_LONG) {
            float step = 0.0f;
            if (end - start > 1) {
                step = widthDiff / (end - start - 1) + fontSize;
            }
            for (int i = start; i < end; ++i) {
                canvas.drawText(text, i, i + 1, x + step * (i - start), y, paint);
            }
        } else {
            canvas.drawText(text, start, end, x, y, paint);
        }

        // ruby don't need to be underlined
        paint.setUnderlineText(oldUnderline);

        // Translate the canvas to avoid drawing ruby outside the left border and thus
        // getting cut off. The reason to not translate the text in this case is it
        // will leads to many bugs.
        if (x + offsetX < bounds.left) {
            canvas.save();
            canvas.translate(-x - offsetX, 0);
        }

        // Draw the ruby
        paint.setTextSize(fontSize * FONTSIZE_SCALE);
        if (relativeLength == RelativeLength.TOO_SHORT) {
            float step = 0.0f;
            if (rubyLength > 1) {
                step = widthDiff / (rubyLength - 1) + rubyWidth / rubyLength;
            }
            for (int i = 0; i < rubyLength; ++i) {
                canvas.drawText(ruby, i, i + 1, x + offsetX + step * i, y + offsetY, paint);
            }
        } else {
            canvas.drawText(ruby, 0, rubyLength, x + offsetX, y + offsetY, paint);
        }
        paint.setTextSize(fontSize);

        if (x + offsetX < 0) {
            canvas.restore();
        }

        if (x + offsetX >= bounds.left && consecutiveRuby && enoughSpaceLeft) {
            canvas.restore();
        }
        paint.setColor(oldColor);
    }
}

package com.osfans.mcpdict.audio;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import com.osfans.mcpdict.R;

import java.util.ArrayList;
import java.util.ArrayDeque;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;

/** Waveform and spectrogram share one horizontal time viewport. */
final class AudioPlotView extends View {
    enum Layer {
        SPECTRUM(R.string.audio_demo_show_spectrum), PITCH(R.string.audio_demo_show_pitch),
        INTENSITY(R.string.audio_demo_intensity), FORMANTS(R.string.audio_demo_formants),
        PULSES(R.string.audio_demo_pulses);

        final int title;
        Layer(int title) { this.title = title; }
    }

    private record Viewport(float start, float span) {}
    private record Panels(float top, float waveBottom, float spectrumTop, float bottom) {}
    private static final float FRAME_SECONDS = (float) AudioAnalysis.HOP / AudioAnalysis.RATE;
    private static final int INTENSITY_MIN = 50;
    private static final int INTENSITY_MAX = 100;
    private static final float MIN_SPAN = 0.025f;
    private static final float LIVE_SECONDS = 8;
    private static final int WAVE = 0xFF335B6B;
    private static final int PITCH = 0xFF155BC9;
    private static final int FORMANT = 0xFFC42C24;
    private static final int LEVEL = 0xFF246E3E;
    private static final int PULSE = 0xFF077C91;
    private static final int CURSOR = 0xFFC74622;
    private static final int AXIS = 0xFF545D66;
    private static final int GRID = 0xFFDCE2E6;
    private final List<AudioAnalysis.Frame> frames = new ArrayList<>();
    private final EnumSet<Layer> layers = EnumSet.of(Layer.SPECTRUM, Layer.PITCH);
    private final ArrayDeque<Viewport> history = new ArrayDeque<>();
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint bitmapPaint = new Paint(Paint.FILTER_BITMAP_FLAG);
    private final Path pitchPath = new Path();
    private final Path selectionPath = new Path();
    private final DashPathEffect cursorDash;
    private final RectF spectrumBounds = new RectF();
    private final ScaleGestureDetector scale;
    private final GestureDetector gestures;
    private Bitmap spectrum;
    private int[] pixels;
    private boolean bitmapDirty = true;
    private boolean live;
    private int sampleCount;
    private float start;
    private float span = 1;
    private float selectionStart = Float.NaN;
    private float selectionEnd = Float.NaN;
    private int frequencyCursor = -1;
    private boolean pinched;
    private Runnable onChange = () -> {};

    AudioPlotView(Context context) {
        super(context);
        setBackgroundColor(Color.WHITE);
        setContentDescription(context.getString(R.string.audio_demo_plot_description));
        cursorDash = new DashPathEffect(new float[]{dp(4), dp(3)}, 0);
        scale = new ScaleGestureDetector(context, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override public boolean onScaleBegin(ScaleGestureDetector detector) {
                if (!canInteract()) return false;
                remember();
                return true;
            }
            @Override public boolean onScale(ScaleGestureDetector detector) {
                float center = clamp((detector.getFocusX() - dp(45)) / Math.max(1, getWidth() - dp(91)), 0, 1);
                float width = clamp(span / detector.getScaleFactor(), MIN_SPAN, totalSpan());
                setViewport(start + center * (span - width), width);
                return true;
            }
        });
        gestures = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override public boolean onDown(MotionEvent event) { return canInteract(); }
            @Override public boolean onScroll(MotionEvent first, MotionEvent last, float dx, float dy) {
                if (!canInteract() || pinched || first == null) return false;
                select(timeAt(first.getX()), timeAt(last.getX()), -1);
                return true;
            }
            @Override public boolean onDoubleTap(MotionEvent event) {
                showAll();
                return true;
            }
            @Override public boolean onSingleTapConfirmed(MotionEvent event) {
                if (!canInteract() || pinched) return false;
                float time = timeAt(event.getX());
                select(time, time, frequencyAt(event.getX(), event.getY()));
                return performClick();
            }
        });
    }

    void beginRecording() {
        frames.clear();
        sampleCount = 0;
        live = true;
        selectionStart = selectionEnd = Float.NaN;
        frequencyCursor = -1;
        history.clear();
        setViewport(0, 1.5f);
    }

    void append(List<AudioAnalysis.Frame> batch, int samples) {
        frames.addAll(batch);
        sampleCount = samples;
        float end = Math.max(1.5f, seconds());
        setViewport(Math.max(0, end - LIVE_SECONDS), Math.min(LIVE_SECONDS, end));
    }

    void setImport(List<AudioAnalysis.Frame> results, int samples) {
        frames.clear();
        frames.addAll(results);
        sampleCount = samples;
        selectionStart = selectionEnd = Float.NaN;
        frequencyCursor = -1;
        history.clear();
        endRecording();
    }

    void endRecording() {
        live = false;
        setViewport(0, frames.isEmpty() ? 1 : totalSpan());
    }

    private float seconds() { return (float) sampleCount / AudioAnalysis.RATE; }
    private float totalSpan() { return Math.max(MIN_SPAN, seconds()); }

    void setOnChange(Runnable listener) { onChange = listener; }
    boolean visible(Layer layer) { return layers.contains(layer); }
    void setVisible(Layer layer, boolean value) {
        if (value) layers.add(layer);
        else layers.remove(layer);
        if (!value && layer == Layer.SPECTRUM) frequencyCursor = -1;
        invalidate();
        onChange.run();
    }

    boolean canInteract() { return !live && !frames.isEmpty(); }
    boolean canZoomIn() { return canInteract() && span > MIN_SPAN + 0.0001f; }
    boolean canZoomOut() { return canInteract() && span < totalSpan() - 0.0001f; }
    boolean hasSelection() { return canInteract() && selectionEnd > selectionStart; }
    boolean canGoBack() { return canInteract() && !history.isEmpty(); }
    boolean hasCursor() { return canInteract() && Float.isFinite(selectionStart); }

    AudioPlayback.Range playbackRange() {
        if (!canInteract()) return null;
        int end = waveformEnd();
        if (!hasSelection()) return new AudioPlayback.Range(new ArrayList<>(frames), 0, end);
        int first = Math.min(end - 1, (int) (selectionStart * AudioAnalysis.RATE));
        int last = Math.min(end, (int) Math.ceil(selectionEnd * AudioAnalysis.RATE));
        return new AudioPlayback.Range(new ArrayList<>(frames), first, Math.max(first + 1, last));
    }

    private int waveformEnd() {
        // During recording, sampleCount also includes samples waiting for the next frame.
        return frames.get(frames.size() - 1).endSample();
    }

    void showAll() { applyZoom(0, totalSpan()); }

    void zoom(float factor) {
        float center = hasCursor() ? clamp((selectionStart + selectionEnd) / 2, start, start + span) : start + span / 2;
        float width = clamp(span * factor, MIN_SPAN, totalSpan());
        applyZoom(center - width / 2, width);
    }

    void zoomToSelection() {
        if (!hasSelection()) return;
        float width = Math.max(MIN_SPAN, selectionEnd - selectionStart);
        applyZoom((selectionStart + selectionEnd - width) / 2, width);
    }

    void previous() {
        if (canGoBack()) {
            Viewport previous = history.removeLast();
            setViewport(previous.start(), previous.span());
        }
    }

    private void applyZoom(float left, float width) {
        if (!canInteract()) return;
        left = clamp(left, 0, Math.max(0, totalSpan() - width));
        if (Math.abs(start - left) < 0.0001f && Math.abs(span - width) < 0.0001f) return;
        remember();
        setViewport(left, width);
    }

    private void remember() {
        if (history.size() == 24) history.removeFirst();
        history.addLast(new Viewport(start, span));
    }

    float scrollPosition() {
        return canZoomOut() ? start / (totalSpan() - span) : 0;
    }

    void scrollTo(float fraction) {
        if (canZoomOut()) setViewport(fraction * (totalSpan() - span), span);
    }

    private float timeAt(float x) {
        float ratio = clamp((x - dp(45)) / Math.max(1, getWidth() - dp(91)), 0, 1);
        return clamp(start + span * ratio, 0, seconds());
    }

    private void select(float one, float two, int frequency) {
        selectionStart = Math.min(one, two);
        selectionEnd = Math.max(one, two);
        frequencyCursor = frequency;
        invalidate();
        onChange.run();
    }

    String selectionReadout() {
        if (!hasCursor()) return "";
        boolean region = hasSelection();
        StringBuilder result = new StringBuilder(region
                ? getResources().getString(R.string.audio_demo_range, selectionStart, selectionEnd, selectionEnd - selectionStart)
                : getResources().getString(R.string.audio_demo_cursor, selectionStart));
        if (visible(Layer.PITCH)) {
            float hz = average(region, 512, AudioAnalysis.Frame::pitch, false);
            if (Float.isFinite(hz)) result.append(String.format(Locale.ROOT, "  ·  F0 %.0f Hz", hz));
        }
        if (visible(Layer.INTENSITY)) {
            float level = selectionLevel();
            if (Float.isFinite(level)) result.append(String.format(Locale.ROOT, "  ·  %.1f dB", level));
        }
        if (visible(Layer.FORMANTS)) {
            for (int i = 0; i < 3; i++) {
                int formant = i;
                float hz = average(region, AudioMeasures.WINDOW / 2,
                        frame -> frame.formants().length > formant ? frame.formants()[formant] : Float.NaN, false);
                if (Float.isFinite(hz)) result.append(String.format(Locale.ROOT, "  ·  F%d %.0f Hz", i + 1, hz));
            }
        }
        if (region && visible(Layer.PULSES)) {
            int count = 0;
            for (int i = Math.max(0, (int) (selectionStart / FRAME_SECONDS));
                 i < Math.min(frames.size(), (int) (selectionEnd / FRAME_SECONDS) + 3); i++) {
                for (int sample : frames.get(i).pulses()) {
                    double time = sample / (double) AudioAnalysis.RATE;
                    if (time >= selectionStart && time <= selectionEnd) count++;
                }
            }
            result.append("  ·  ").append(getResources().getString(R.string.audio_demo_pulse_count, count));
        }
        return result.toString();
    }

    private float selectionLevel() {
        return hasCursor() ? average(hasSelection(), AudioMeasures.INTENSITY_DELAY, AudioAnalysis.Frame::intensity, true) : Float.NaN;
    }

    private float average(boolean region, int delay, java.util.function.ToDoubleFunction<AudioAnalysis.Frame> value,
                          boolean decibels) {
        if (!region) {
            if (decibels) return intensityAt(selectionStart);
            int closest = Math.round((selectionStart * AudioAnalysis.RATE + delay) / AudioAnalysis.HOP) - 1;
            return (float) value.applyAsDouble(frames.get(Math.max(0, Math.min(closest, frames.size() - 1))));
        }
        int first = Math.max(0, (int) (selectionStart / FRAME_SECONDS));
        int last = Math.min(frames.size(), (int) (selectionEnd / FRAME_SECONDS) + delay / AudioAnalysis.HOP + 2);
        int count = 0;
        double sum = 0;
        for (int i = first; i < last; i++) {
            AudioAnalysis.Frame frame = frames.get(i);
            double time = Math.max(0, frame.endSample() - delay) / (double) AudioAnalysis.RATE;
            if (time < selectionStart || time > selectionEnd) continue;
            double measured = value.applyAsDouble(frame);
            if (!Double.isFinite(measured)) continue;
            sum += decibels ? Math.pow(10, measured / 10) : measured;
            count++;
        }
        if (count == 0) return decibels ? intensityAt((selectionStart + selectionEnd) / 2) : Float.NaN;
        double mean = sum / count;
        return (float) (decibels ? 10 * Math.log10(mean) : mean);
    }

    private float intensityAt(float time) {
        float index = (time * AudioAnalysis.RATE + AudioMeasures.INTENSITY_DELAY) / AudioAnalysis.HOP - 1;
        int first = Math.max(0, (int) Math.floor(index));
        int last = Math.min(frames.size() - 1, first + 1);
        if (first > last) return Float.NaN;
        AudioAnalysis.Frame left = frames.get(first);
        AudioAnalysis.Frame right = frames.get(last);
        float leftTime = (left.endSample() - AudioMeasures.INTENSITY_DELAY) / (float) AudioAnalysis.RATE;
        float rightTime = (right.endSample() - AudioMeasures.INTENSITY_DELAY) / (float) AudioAnalysis.RATE;
        if (time > leftTime && time < rightTime && Float.isFinite(left.intensity()) && Float.isFinite(right.intensity())) {
            return left.intensity() + (right.intensity() - left.intensity()) * (time - leftTime) / (rightTime - leftTime);
        }
        if (Math.abs(time - leftTime) <= FRAME_SECONDS / 2 && Float.isFinite(left.intensity())) return left.intensity();
        return Math.abs(time - rightTime) <= FRAME_SECONDS / 2 ? right.intensity() : Float.NaN;
    }

    private void setViewport(float left, float width) {
        span = width;
        start = clamp(left, 0, Math.max(0, totalSpan() - span));
        bitmapDirty = true;
        invalidate();
        onChange.run();
    }

    @Override public boolean onTouchEvent(MotionEvent event) {
        if (event.getActionMasked() == MotionEvent.ACTION_DOWN) pinched = false;
        if (event.getActionMasked() == MotionEvent.ACTION_POINTER_DOWN) pinched = true;
        scale.onTouchEvent(event);
        gestures.onTouchEvent(event);
        return true;
    }

    @Override public boolean performClick() { return super.performClick(); }

    private boolean hasAnalysisPanel() {
        return visible(Layer.SPECTRUM) || visible(Layer.PITCH) || visible(Layer.FORMANTS) || visible(Layer.INTENSITY);
    }

    private Panels panels() {
        float top = dp(22);
        float bottom = getHeight() - dp(27);
        float gap = dp(21);
        float waveBottom = hasAnalysisPanel() ? top + (bottom - top - gap) * 0.34f : bottom;
        return new Panels(top, waveBottom, waveBottom + gap, bottom);
    }

    private int frequencyAt(float x, float y) {
        if (!visible(Layer.SPECTRUM) || x < dp(45) || x > getWidth() - dp(46)) return -1;
        Panels area = panels();
        if (y < area.spectrumTop() || y > area.bottom()) return -1;
        float height = area.bottom() - area.spectrumTop();
        return height > 0 ? Math.round(AudioAnalysis.SPECTRUM_TOP * (area.bottom() - y) / height) : -1;
    }

    private static float frequencyY(int hz, Panels area) {
        return area.bottom() - (area.bottom() - area.spectrumTop()) * hz / AudioAnalysis.SPECTRUM_TOP;
    }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float left = dp(45);
        float right = getWidth() - dp(46);
        float width = right - left;
        Panels area = panels();
        float top = area.top();
        float bottom = area.bottom();
        float waveBottom = area.waveBottom();
        float spectTop = area.spectrumTop();
        boolean spectral = hasAnalysisPanel();
        if (width < dp(40) || bottom - top < dp(55)) return;

        paint.setColor(AXIS);
        paint.setTextSize(dp(11));
        paint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText(getResources().getString(R.string.audio_demo_waveform), left, top - dp(6), paint);
        if (visible(Layer.PULSES)) {
            paint.setColor(PULSE);
            paint.setTextAlign(Paint.Align.RIGHT);
            canvas.drawText(getResources().getString(R.string.audio_demo_pulses), right, top - dp(6), paint);
        }
        if (spectral) {
            paint.setColor(AXIS);
            paint.setTextAlign(Paint.Align.LEFT);
            if (visible(Layer.SPECTRUM)) {
                canvas.drawText(getResources().getString(R.string.audio_demo_spectrum), left, spectTop - dp(6), paint);
            }
            if (visible(Layer.FORMANTS)) {
                paint.setColor(FORMANT);
                float x = visible(Layer.SPECTRUM) ? left + dp(66) : left;
                canvas.drawText(getResources().getString(R.string.audio_demo_formants), x, spectTop - dp(6), paint);
            }
            if (visible(Layer.PITCH)) {
                paint.setColor(PITCH);
                paint.setTextAlign(Paint.Align.RIGHT);
                canvas.drawText(getResources().getString(R.string.audio_demo_f0), right, spectTop - dp(6), paint);
            }
        }
        paint.setColor(0xFFF4F7F9);
        canvas.drawRect(left, top, right, waveBottom, paint);
        float middle = (top + waveBottom) * 0.5f;
        paint.setColor(GRID);
        canvas.drawLine(left, middle, right, middle, paint);
        int columns = Math.max(1, (int) width);
        if (!frames.isEmpty()) {
            drawWave(canvas, left, columns, middle, (waveBottom - top) * 0.47f);
            if (visible(Layer.PULSES)) drawPulses(canvas, left, width, top, waveBottom);
            if (visible(Layer.SPECTRUM)) {
                prepareSpectrum(columns);
                spectrumBounds.set(left, spectTop, right, bottom);
                canvas.drawBitmap(spectrum, null, spectrumBounds, bitmapPaint);
            }
            if (visible(Layer.FORMANTS)) drawFormants(canvas, left, width, spectTop, bottom);
            if (visible(Layer.PITCH)) drawContour(canvas, left, width, spectTop, bottom, true);
            if (visible(Layer.INTENSITY)) drawContour(canvas, left, width, spectTop, bottom, false);
        }
        drawAxes(canvas, left, right, area, spectral);
        drawSelection(canvas, left, width, top, bottom);
        if (frequencyCursor >= 0) drawFrequencyCursor(canvas, left, right, area);
        if (visible(Layer.INTENSITY)) drawIntensityScale(canvas, right, area);
    }

    private void drawWave(Canvas canvas, float left, int columns, float center, float amplitude) {
        paint.setColor(WAVE);
        paint.setStrokeWidth(1);
        float count = AudioAnalysis.RATE * span / columns;
        int firstSample = (int) (start * AudioAnalysis.RATE);
        int waveformEnd = waveformEnd();
        for (int x = 0; x < columns; x++) {
            int from = firstSample + (int) (x * count);
            int to = Math.min(waveformEnd, firstSample + Math.max((int) ((x + 1) * count), from - firstSample + 1));
            if (from >= to) break;
            short low = Short.MAX_VALUE;
            short high = Short.MIN_VALUE;
            for (int i = from; i < to;) {
                AudioAnalysis.Frame frame = frames.get(i / AudioAnalysis.HOP);
                int offset = i % AudioAnalysis.HOP;
                int available = Math.min(to - i, frame.waveform().length - offset);
                if (available <= 0) break;
                if (available == frame.waveform().length) {
                    low = (short) Math.min(low, frame.low());
                    high = (short) Math.max(high, frame.high());
                } else {
                    for (int j = offset; j < offset + available; j++) {
                        short value = frame.waveform()[j];
                        low = (short) Math.min(low, value);
                        high = (short) Math.max(high, value);
                    }
                }
                i += available;
            }
            if (low > high) continue;
            float a = center - high * amplitude / 32768;
            float b = center - low * amplitude / 32768;
            canvas.drawLine(left + x, a, left + x, Math.max(a + 1, b), paint);
        }
    }

    private void prepareSpectrum(int width) {
        int bins = AudioAnalysis.BINS;
        if (spectrum == null || spectrum.getWidth() != width) {
            spectrum = Bitmap.createBitmap(width, bins, Bitmap.Config.ARGB_8888);
            pixels = new int[width * bins];
            bitmapDirty = true;
        }
        if (!bitmapDirty) return;
        int end = waveformEnd();
        int hop = AudioAnalysis.SPECTRUM_HOP;
        int lastSlice = (end - 1) / hop;
        for (int x = 0; x < width; x++) {
            float time = start + span * (x + 0.5f) / width;
            float sample = time * AudioAnalysis.RATE;
            byte[] first = null;
            byte[] second = null;
            float fraction = 0;
            if (sample < end) {
                float windowEnd = sample + AudioAnalysis.SPECTRUM_WINDOW / 2f;
                int before = Math.max(0, Math.min(lastSlice, (int) (windowEnd / hop) - 1));
                int after = Math.min(lastSlice, before + 1);
                first = spectrumSlice(before);
                second = spectrumSlice(after);
                if (after > before) {
                    int beforeEnd = (before + 1) * hop;
                    int afterEnd = Math.min(end, (after + 1) * hop);
                    fraction = clamp((windowEnd - beforeEnd) / (afterEnd - beforeEnd), 0, 1);
                }
            }
            for (int y = 0; y < bins; y++) {
                int bin = bins - 1 - y;
                int gray = 255;
                if (first != null) {
                    int a = first[bin] & 255;
                    int b = second[bin] & 255;
                    gray = Math.round(a + (b - a) * fraction);
                }
                pixels[y * width + x] = 0xFF000000 | gray * 0x010101;
            }
        }
        spectrum.setPixels(pixels, 0, width, 0, 0, width, bins);
        bitmapDirty = false;
    }

    private byte[] spectrumSlice(int index) {
        int perFrame = AudioAnalysis.SPECTRA_PER_FRAME;
        return frames.get(index / perFrame).spectra()[index % perFrame];
    }

    private void drawContour(Canvas canvas, float left, float width, float top, float bottom, boolean pitch) {
        int saved = canvas.save();
        canvas.clipRect(left, top, left + width, bottom);
        pitchPath.rewind();
        paint.setColor(pitch ? PITCH : LEVEL);
        paint.setStrokeWidth(dp(1.65f));
        paint.setStrokeCap(Paint.Cap.ROUND);
        boolean connected = false;
        int first = Math.max(0, (int) (start / FRAME_SECONDS));
        int last = Math.min(frames.size(), (int) ((start + span) / FRAME_SECONDS) + 4);
        for (int index = first; index < last; index++) {
            AudioAnalysis.Frame frame = frames.get(index);
            float value = pitch ? frame.pitch() : frame.intensity();
            if (!Float.isFinite(value)) {
                connected = false;
                continue;
            }
            int delay = pitch ? 512 : AudioMeasures.INTENSITY_DELAY;
            float time = Math.max(0, frame.endSample() - delay) / (float) AudioAnalysis.RATE;
            float x = xAt(time, left, width);
            float y = pitch ? bottom - (bottom - top) * clamp((value - AudioAnalysis.PITCH_MIN)
                    / (AudioAnalysis.PITCH_MAX - AudioAnalysis.PITCH_MIN), 0, 1) : intensityY(value, top, bottom);
            if (connected) pitchPath.lineTo(x, y);
            else {
                pitchPath.moveTo(x, y);
                canvas.drawPoint(x, y, paint);
            }
            connected = true;
        }
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawPath(pitchPath, paint);
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeCap(Paint.Cap.BUTT);
        paint.setStrokeWidth(1);
        canvas.restoreToCount(saved);
    }

    private void drawFormants(Canvas canvas, float left, float width, float top, float bottom) {
        int saved = canvas.save();
        canvas.clipRect(left, top, left + width, bottom);
        paint.setColor(FORMANT);
        int first = Math.max(0, (int) (start / FRAME_SECONDS));
        int last = Math.min(frames.size(), (int) ((start + span) / FRAME_SECONDS) + 3);
        int stride = Math.max(1, (int) ((last - first) / width));
        for (int i = first; i < last; i += stride) {
            AudioAnalysis.Frame frame = frames.get(i);
            float time = (frame.endSample() - AudioMeasures.WINDOW / 2) / (float) AudioAnalysis.RATE;
            float x = xAt(time, left, width);
            for (float frequency : frame.formants()) {
                float y = bottom - (bottom - top) * frequency / AudioAnalysis.SPECTRUM_TOP;
                canvas.drawCircle(x, y, dp(1.6f), paint);
            }
        }
        canvas.restoreToCount(saved);
    }

    private void drawPulses(Canvas canvas, float left, float width, float top, float bottom) {
        int saved = canvas.save();
        canvas.clipRect(left, top, left + width, bottom);
        paint.setColor(PULSE);
        paint.setAlpha(135);
        int first = Math.max(0, (int) (start / FRAME_SECONDS));
        int last = Math.min(frames.size(), (int) ((start + span) / FRAME_SECONDS) + 3);
        float lastDrawn = -Float.MAX_VALUE;
        for (int i = first; i < last; i++) {
            for (int index : frames.get(i).pulses()) {
                float x = xAt(index / (float) AudioAnalysis.RATE, left, width);
                if (x - lastDrawn < 1) continue;
                canvas.drawLine(x, top, x, bottom, paint);
                lastDrawn = x;
            }
        }
        paint.setAlpha(255);
        canvas.restoreToCount(saved);
    }

    private float xAt(float time, float left, float width) { return left + (time - start) * width / span; }

    private void drawSelection(Canvas canvas, float left, float width, float top, float bottom) {
        if (!hasCursor()) return;
        float a = xAt(selectionStart, left, width);
        float b = xAt(selectionEnd, left, width);
        int saved = canvas.save();
        canvas.clipRect(left, top, left + width, bottom);
        paint.setColor(0x20DB511D);
        if (hasSelection()) canvas.drawRect(a, top, b, bottom, paint);
        paint.setColor(CURSOR);
        paint.setStrokeWidth(dp(1.3f));
        paint.setStyle(Paint.Style.STROKE);
        paint.setPathEffect(cursorDash);
        selectionPath.rewind();
        selectionPath.moveTo(a, top);
        selectionPath.lineTo(a, bottom);
        if (hasSelection()) {
            selectionPath.moveTo(b, top);
            selectionPath.lineTo(b, bottom);
        }
        canvas.drawPath(selectionPath, paint);
        paint.setPathEffect(null);
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(1);
        canvas.restoreToCount(saved);
    }

    private void drawFrequencyCursor(Canvas canvas, float left, float right, Panels area) {
        float y = frequencyY(frequencyCursor, area);
        paint.setColor(CURSOR);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(1));
        paint.setPathEffect(cursorDash);
        selectionPath.rewind();
        selectionPath.moveTo(left, y);
        selectionPath.lineTo(right, y);
        canvas.drawPath(selectionPath, paint);
        paint.setPathEffect(null);
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(1);
        paint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText(frequencyCursor + " Hz", left - dp(4), y + dp(3), paint);
    }

    private void drawIntensityScale(Canvas canvas, float right, Panels area) {
        float top = area.spectrumTop();
        float bottom = area.bottom();
        float level = selectionLevel();
        float levelY = Float.NaN;
        if (Float.isFinite(level)) {
            levelY = clamp(intensityY(level, top, bottom), top + dp(26), bottom - dp(18));
        }
        drawIntensityLabel(canvas, right, top + dp(12), INTENSITY_MAX + " dB");
        drawIntensityLabel(canvas, right, bottom - dp(4), INTENSITY_MIN + " dB");
        if (Float.isFinite(levelY)) {
            drawIntensityLabel(canvas, right, levelY, String.format(Locale.ROOT, "%.1f dB", level));
        }
    }

    private float intensityY(float level, float top, float bottom) {
        return bottom - (bottom - top) * (level - INTENSITY_MIN) / (INTENSITY_MAX - INTENSITY_MIN);
    }

    private void drawIntensityLabel(Canvas canvas, float right, float y, String label) {
        float x = right - dp(4);
        float labelWidth = paint.measureText(label);
        paint.setColor(0xDBFFFFFF);
        canvas.drawRect(x - labelWidth - dp(2), y - dp(10), x + dp(2), y + dp(2), paint);
        paint.setColor(LEVEL);
        paint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText(label, x, y, paint);
    }

    private void drawAxes(Canvas canvas, float left, float right, Panels area, boolean spectral) {
        float bottom = area.bottom();
        paint.setColor(GRID);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawRect(left, area.top(), right, area.waveBottom(), paint);
        if (spectral) canvas.drawRect(left, area.spectrumTop(), right, bottom, paint);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(AXIS);
        paint.setTextAlign(Paint.Align.RIGHT);
        if (visible(Layer.SPECTRUM) || visible(Layer.FORMANTS)) {
            for (int frequency : new int[]{0, 1000, 3000, 5000}) {
                float y = frequencyY(frequency, area);
                if (frequencyCursor >= 0 && Math.abs(y - frequencyY(frequencyCursor, area)) < dp(13)) continue;
                canvas.drawText(frequency == 0 ? "0" : (frequency / 1000) + "k", left - dp(4), y + dp(3), paint);
            }
        }
        paint.setColor(PITCH);
        paint.setTextAlign(Paint.Align.LEFT);
        if (visible(Layer.PITCH)) {
            for (int hz : new int[]{AudioAnalysis.PITCH_MIN, 200, 350, AudioAnalysis.PITCH_MAX}) {
                float y = bottom - (bottom - area.spectrumTop()) * (hz - AudioAnalysis.PITCH_MIN)
                        / (AudioAnalysis.PITCH_MAX - AudioAnalysis.PITCH_MIN);
                canvas.drawText(String.valueOf(hz), right + dp(4), y + dp(3), paint);
            }
        }
        paint.setColor(AXIS);
        int divisions = Math.max(2, (int) ((right - left) / dp(70)));
        String precision = span < 0.5f ? "%.3f" : span < 5 ? "%.2f" : "%.1f";
        for (int i = 0; i <= divisions; i++) {
            float ratio = (float) i / divisions;
            float x = left + (right - left) * ratio;
            paint.setTextAlign(i == 0 ? Paint.Align.LEFT : i == divisions ? Paint.Align.RIGHT : Paint.Align.CENTER);
            String label = String.format(Locale.ROOT, precision, start + span * ratio);
            canvas.drawLine(x, bottom, x, bottom + dp(3), paint);
            canvas.drawText(label + " s", x, bottom + dp(16), paint);
        }
    }

    private float dp(float size) { return getResources().getDisplayMetrics().density * size; }
    private static float clamp(float n, float low, float high) { return Math.min(high, Math.max(low, n)); }
}

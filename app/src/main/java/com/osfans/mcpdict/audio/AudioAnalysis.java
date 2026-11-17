package com.osfans.mcpdict.audio;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntToDoubleFunction;

/** Streaming analysis at a fixed rate so imports and recordings share the same axes. */
final class AudioAnalysis {
    static final int RATE = 16_000;
    static final int HOP = 256;
    static final int SECONDS_LIMIT = 120;
    static final int MAX_SAMPLES = RATE * SECONDS_LIMIT;
    static final int SPECTRUM_TOP = 5000;
    static final int PITCH_MIN = 55;
    static final int PITCH_MAX = 550;
    private static final int FFT = 512;
    static final int BINS = SPECTRUM_TOP * FFT / RATE + 1;
    private static final int PITCH_WINDOW = 1024;
    private static final int HISTORY_SIZE = 2048;
    private static final int PITCH_RATE = RATE / 2;
    private static final int MIN_LAG = PITCH_RATE / PITCH_MAX;
    private static final int MAX_LAG = PITCH_RATE / PITCH_MIN;
    private static final int COMPARE = PITCH_WINDOW / 2 - MAX_LAG - 1;
    private static final float PITCH_THRESHOLD = 0.16f;

    record Frame(short[] waveform, short low, short high, byte[] spectrum, float pitch,
                 float intensity, float[] formants, int[] pulses, int endSample) {}

    private final float[] history = new float[HISTORY_SIZE];
    private final float[] window = new float[FFT];
    private final float[] real = new float[FFT];
    private final float[] imaginary = new float[FFT];
    private final float[] cosine = new float[FFT / 2];
    private final float[] sine = new float[FFT / 2];
    private final float[] downsampled = new float[PITCH_WINDOW / 2];
    private final float[] difference = new float[MAX_LAG + 2];
    private final AudioMeasures measures = new AudioMeasures();
    private final IntToDoubleFunction samplesAt = this::sampleAt;
    private List<Frame> pending = new ArrayList<>();
    private short[] waveform = new short[HOP];
    private int used;
    private int samples;
    private int inputRate = RATE;
    private int weight;
    private float sum;

    AudioAnalysis() {
        for (int i = 0; i < FFT; i++) {
            window[i] = (float) (0.5 - 0.5 * Math.cos(2 * Math.PI * i / (FFT - 1)));
        }
        for (int i = 0; i < FFT / 2; i++) {
            double phase = -2 * Math.PI * i / FFT;
            cosine[i] = (float) Math.cos(phase);
            sine[i] = (float) Math.sin(phase);
        }
    }

    void setInputRate(int rate) {
        if (rate <= 0) throw new IllegalArgumentException("Invalid audio sample rate");
        if (rate == inputRate) return;
        inputRate = rate;
        weight = 0;
        sum = 0;
    }

    void accept(float sample) {
        if (!Float.isFinite(sample)) sample = 0;
        sample = Math.max(-1, Math.min(1, sample));
        if (inputRate == RATE) {
            append(sample);
            return;
        }
        // Average samples by their overlap with each output interval.
        int remaining = RATE;
        while (remaining > 0 && !isFull()) {
            int overlap = Math.min(remaining, inputRate - weight);
            sum += sample * overlap;
            weight += overlap;
            remaining -= overlap;
            if (weight == inputRate) {
                append(sum / inputRate);
                weight = 0;
                sum = 0;
            }
        }
    }

    private void append(float value) {
        if (isFull()) return;
        history[samples & (HISTORY_SIZE - 1)] = value;
        waveform[used++] = (short) Math.round(value * 32767);
        samples++;
        if (used == HOP) addFrame();
    }

    void finish() {
        if (weight > 0) {
            append(sum / weight);
            weight = 0;
            sum = 0;
        }
        if (used > 0) addFrame();
    }

    int sampleCount() { return samples; }
    boolean isFull() { return samples == MAX_SAMPLES; }

    List<Frame> drain() {
        List<Frame> frames = pending;
        pending = new ArrayList<>();
        return frames;
    }

    private float sampleAt(int position) {
        return position < 0 ? 0 : history[position & (HISTORY_SIZE - 1)];
    }

    private void addFrame() {
        short low = waveform[0];
        short high = low;
        for (int i = 1; i < used; i++) {
            low = (short) Math.min(low, waveform[i]);
            high = (short) Math.max(high, waveform[i]);
        }
        short[] pcm = used == HOP ? waveform : java.util.Arrays.copyOf(waveform, used);
        float f0 = pitch();
        pending.add(new Frame(pcm, low, high, spectrum(), f0, measures.intensity(samples, samplesAt),
                measures.formants(samples, samplesAt), measures.pulses(samples, used, f0, samplesAt), samples));
        waveform = new short[HOP];
        used = 0;
    }

    private byte[] spectrum() {
        for (int i = 0; i < FFT; i++) {
            int j = Integer.reverse(i) >>> (32 - Integer.numberOfTrailingZeros(FFT));
            real[j] = sampleAt(samples - FFT + i) * window[i];
            imaginary[j] = 0;
        }
        for (int size = 2; size <= FFT; size *= 2) {
            int half = size / 2;
            int step = FFT / size;
            for (int start = 0; start < FFT; start += size) {
                for (int i = 0; i < half; i++) {
                    int a = start + i;
                    int b = a + half;
                    float c = cosine[i * step];
                    float s = sine[i * step];
                    float r = real[b] * c - imaginary[b] * s;
                    float q = real[b] * s + imaginary[b] * c;
                    real[b] = real[a] - r;
                    imaginary[b] = imaginary[a] - q;
                    real[a] += r;
                    imaginary[a] += q;
                }
            }
        }
        byte[] bins = new byte[BINS];
        for (int i = 0; i < BINS; i++) {
            double amplitude = Math.hypot(real[i], imaginary[i]) * 4 / FFT;
            double db = 20 * Math.log10(Math.max(1e-6, amplitude));
            int shade = (int) (255 * Math.max(0, Math.min(1, (db + 80) / 62)));
            bins[i] = (byte) (255 - shade);
        }
        return bins;
    }

    private float pitch() {
        if (samples < PITCH_WINDOW) return Float.NaN;
        float energy = 0;
        float mean = 0;
        for (int i = 0; i < downsampled.length; i++) {
            int offset = samples - PITCH_WINDOW + 2 * i;
            float value = (sampleAt(offset) + sampleAt(offset + 1)) * 0.5f;
            downsampled[i] = value;
            energy += value * value;
            mean += value;
        }
        mean /= downsampled.length;
        if (energy / downsampled.length - mean * mean < 0.000036f) return Float.NaN;

        // YIN's cumulative mean-normalized difference rejects non-periodic frames.
        float cumulative = 0;
        difference[0] = 1;
        for (int lag = 1; lag <= MAX_LAG + 1; lag++) {
            float distance = 0;
            for (int i = 0; i < COMPARE; i++) {
                float delta = downsampled[i] - downsampled[i + lag];
                distance += delta * delta;
            }
            cumulative += distance;
            difference[lag] = cumulative > 0 ? distance * lag / cumulative : 1;
        }
        for (int lag = MIN_LAG; lag <= MAX_LAG; lag++) {
            float center = difference[lag];
            float right = difference[lag + 1];
            if (center >= PITCH_THRESHOLD || center > right) continue;
            float left = difference[lag - 1];
            float curvature = left - 2 * center + right;
            float offset = curvature > 0 ? 0.5f * (left - right) / curvature : 0;
            float hz = PITCH_RATE / (lag + Math.max(-0.5f, Math.min(0.5f, offset)));
            return hz >= PITCH_MIN && hz <= PITCH_MAX ? hz : Float.NaN;
        }
        return Float.NaN;
    }
}

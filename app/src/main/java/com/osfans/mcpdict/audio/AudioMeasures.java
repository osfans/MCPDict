package com.osfans.mcpdict.audio;

import java.util.Arrays;
import java.util.function.IntToDoubleFunction;

/** Short-time intensity, LPC resonances and waveform pulse estimates. */
final class AudioMeasures {
    static final int WINDOW = 512;
    static final int FORMANT_COUNT = 5;
    private static final int INTENSITY_PITCH_FLOOR = 75;
    private static final double INTENSITY_RADIUS = 3.2 * AudioAnalysis.RATE / INTENSITY_PITCH_FLOOR;
    static final int INTENSITY_DELAY = (int) INTENSITY_RADIUS + 1;
    private static final int INTENSITY_WINDOW = 2 * INTENSITY_DELAY - 1;
    private static final double REFERENCE_PRESSURE = 20e-6;
    private static final double[] INTENSITY_WEIGHTS = new double[INTENSITY_WINDOW];
    private static final int ORDER = 16;
    private static final int STEP = 25;
    private static final double FORMANT_MIN_POWER = Math.pow(10, -55.0 / 10);
    private static final int BINS = AudioAnalysis.SPECTRUM_TOP / STEP + 1;
    private static final float[] NO_FORMANTS = {};
    private static final int[] NO_PULSES = {};
    private static final double PREEMPHASIS = Math.exp(-2 * Math.PI * 50 / AudioAnalysis.RATE);
    private static final double[][] COSINE = new double[BINS][ORDER];
    private static final double[][] SINE = new double[BINS][ORDER];

    static {
        // Praat's Kaiser-20 window spans 6.4 pitch periods at the intensity pitch floor.
        double beta = 2 * Math.PI * Math.PI + 0.5;
        double total = 0;
        for (int i = 0; i < INTENSITY_WINDOW; i++) {
            double position = (i - (INTENSITY_DELAY - 1)) / INTENSITY_RADIUS;
            INTENSITY_WEIGHTS[i] = besselI0(beta * Math.sqrt(1 - position * position));
            total += INTENSITY_WEIGHTS[i];
        }
        for (int i = 0; i < INTENSITY_WINDOW; i++) INTENSITY_WEIGHTS[i] /= total;
        for (int bin = 0; bin < BINS; bin++) {
            for (int order = 1; order <= ORDER; order++) {
                double angle = 2 * Math.PI * bin * STEP * order / AudioAnalysis.RATE;
                COSINE[bin][order - 1] = Math.cos(angle);
                SINE[bin][order - 1] = Math.sin(angle);
            }
        }
    }

    private final double[] signal = new double[WINDOW];
    private final double[] window = new double[WINDOW];
    private final double[] correlation = new double[ORDER + 1];
    private final double[] lpc = new double[ORDER + 1];
    private final double[] updated = new double[ORDER + 1];
    private final double[] envelope = new double[BINS];
    private final int[] pulses = new int[AudioAnalysis.HOP * AudioAnalysis.PITCH_MAX / AudioAnalysis.RATE + 4];
    private int lastPulse = -1;
    private double polarity;

    AudioMeasures() {
        for (int i = 0; i < WINDOW; i++) window[i] = 0.54 - 0.46 * Math.cos(2 * Math.PI * i / (WINDOW - 1));
    }

    private static double besselI0(double x) {
        double sum = 1;
        double term = 1;
        for (int k = 1; term > sum * 1e-15; k++) {
            double ratio = x / (2 * k);
            term *= ratio * ratio;
            sum += term;
        }
        return sum;
    }

    float intensity(int end, IntToDoubleFunction input) {
        if (end < INTENSITY_WINDOW) return Float.NaN;
        int start = end - INTENSITY_WINDOW;
        double mean = 0;
        for (int i = start; i < end; i++) mean += input.applyAsDouble(i);
        mean /= INTENSITY_WINDOW;
        double power = 0;
        for (int i = 0; i < INTENSITY_WINDOW; i++) {
            double pressure = input.applyAsDouble(start + i) - mean;
            power += pressure * pressure * INTENSITY_WEIGHTS[i];
        }
        double relativePower = power / (REFERENCE_PRESSURE * REFERENCE_PRESSURE);
        return (float) (10 * Math.log10(Math.max(1e-30, relativePower)));
    }

    private boolean hasFormantSignal(int end, IntToDoubleFunction input) {
        int start = end - WINDOW;
        double energy = 0;
        double sum = 0;
        for (int i = start; i < end; i++) {
            double sample = input.applyAsDouble(i);
            energy += sample * sample;
            sum += sample;
        }
        double mean = sum / WINDOW;
        double power = energy / WINDOW - mean * mean;
        return power >= FORMANT_MIN_POWER;
    }

    float[] formants(int end, IntToDoubleFunction input) {
        if (end < WINDOW || !hasFormantSignal(end, input)) return NO_FORMANTS;
        double previous = input.applyAsDouble(end - WINDOW - 1);
        for (int i = 0; i < WINDOW; i++) {
            double value = input.applyAsDouble(end - WINDOW + i);
            signal[i] = (value - PREEMPHASIS * previous) * window[i];
            previous = value;
        }
        for (int lag = 0; lag <= ORDER; lag++) {
            double sum = 0;
            for (int i = lag; i < WINDOW; i++) sum += signal[i] * signal[i - lag];
            correlation[lag] = sum;
        }
        correlation[0] *= 1.0001;
        double error = correlation[0];
        if (error < 1e-8) return NO_FORMANTS;
        lpc[0] = 1;
        for (int order = 1; order <= ORDER; order++) {
            double prediction = correlation[order];
            for (int i = 1; i < order; i++) prediction += lpc[i] * correlation[order - i];
            double reflection = -prediction / error;
            if (Math.abs(reflection) >= 0.999 || !Double.isFinite(reflection)) return NO_FORMANTS;
            for (int i = 1; i < order; i++) updated[i] = lpc[i] + reflection * lpc[order - i];
            System.arraycopy(updated, 1, lpc, 1, order - 1);
            lpc[order] = reflection;
            error *= 1 - reflection * reflection;
        }
        for (int bin = 0; bin < BINS; bin++) {
            double real = 1;
            double imaginary = 0;
            for (int order = 1; order <= ORDER; order++) {
                real += lpc[order] * COSINE[bin][order - 1];
                imaginary += lpc[order] * SINE[bin][order - 1];
            }
            envelope[bin] = 1 / Math.max(1e-10, real * real + imaginary * imaginary);
        }

        float[] frequencies = new float[FORMANT_COUNT];
        int count = 0;
        for (int bin = 4; bin < BINS - 4 && count < frequencies.length; bin++) {
            double top = envelope[bin];
            if (top <= envelope[bin - 1] || top <= envelope[bin + 1]) continue;
            int left = bin - 1;
            int right = bin + 1;
            while (left > 0 && bin - left < 18 && envelope[left] > top * 0.55) left--;
            while (right < BINS - 1 && right - bin < 18 && envelope[right] > top * 0.55) right++;
            if (envelope[left] > top * 0.55 || envelope[right] > top * 0.55 || (right - left) * STEP > 550) continue;
            double before = Math.log(envelope[bin - 1]);
            double after = Math.log(envelope[bin + 1]);
            double bend = before - 2 * Math.log(top) + after;
            double offset = bend < 0 ? 0.5 * (before - after) / bend : 0;
            frequencies[count++] = (float) ((bin + offset) * STEP);
        }
        return count == 0 ? NO_FORMANTS : Arrays.copyOf(frequencies, count);
    }

    int[] pulses(int end, int blockSize, float pitch, IntToDoubleFunction input) {
        if (!Float.isFinite(pitch)) {
            lastPulse = -1;
            return NO_PULSES;
        }
        int period = Math.round(AudioAnalysis.RATE / pitch);
        int radius = Math.max(1, period / 5);
        int first = end - blockSize;
        int count = 0;
        if (lastPulse < first - 2 * period) lastPulse = -1;
        if (lastPulse == -1) {
            int seed = first;
            for (int i = first + 1; i < Math.min(end, first + period); i++) {
                if (Math.abs(input.applyAsDouble(i)) > Math.abs(input.applyAsDouble(seed))) seed = i;
            }
            double sample = input.applyAsDouble(seed);
            if (Math.abs(sample) < 0.005) return NO_PULSES;
            polarity = Math.copySign(1, sample);
            lastPulse = seed;
            pulses[count++] = seed;
        }
        while (lastPulse + period + radius < end && count < pulses.length) {
            int target = lastPulse + period;
            int best = target - radius;
            for (int i = best + 1; i <= target + radius; i++) {
                if (input.applyAsDouble(i) * polarity > input.applyAsDouble(best) * polarity) best = i;
            }
            if (input.applyAsDouble(best) * polarity < 0.005) {
                lastPulse = -1;
                break;
            }
            lastPulse = best;
            pulses[count++] = best;
        }
        return count == 0 ? NO_PULSES : Arrays.copyOf(pulses, count);
    }
}

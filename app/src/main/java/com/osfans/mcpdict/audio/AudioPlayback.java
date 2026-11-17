package com.osfans.mcpdict.audio;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioTrack;
import android.os.SystemClock;

import java.io.IOException;
import java.util.List;
import java.util.function.BooleanSupplier;

/** Plays the same samples and time range that the plot displays. */
final class AudioPlayback {
    private static final int OUTPUT_TIMEOUT_MS = 3000;

    record Range(List<AudioAnalysis.Frame> frames, int startSample, int endSample) {}

    private AudioPlayback() {}

    static void play(Range range, BooleanSupplier stopped) throws IOException, InterruptedException {
        if (stopped.getAsBoolean()) return;
        int minimum = AudioTrack.getMinBufferSize(AudioAnalysis.RATE, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT);
        if (minimum <= 0) throw new IOException("Audio output is unavailable");

        AudioTrack track = new AudioTrack.Builder()
                .setAudioAttributes(new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH).build())
                .setAudioFormat(new AudioFormat.Builder().setSampleRate(AudioAnalysis.RATE)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT).build())
                .setTransferMode(AudioTrack.MODE_STREAM)
                .setBufferSizeInBytes(Math.max(minimum, AudioAnalysis.RATE * Short.BYTES / 10))
                .build();
        try {
            if (track.getState() != AudioTrack.STATE_INITIALIZED) throw new IOException("Cannot initialize audio output");
            track.play();
            for (int sample = range.startSample(); sample < range.endSample() && !stopped.getAsBoolean();) {
                short[] pcm = range.frames().get(sample / AudioAnalysis.HOP).waveform();
                int offset = sample % AudioAnalysis.HOP;
                int count = Math.min(pcm.length - offset, range.endSample() - sample);
                write(track, pcm, offset, count, stopped);
                sample += count;
            }

            int length = range.endSample() - range.startSample();
            // Very short selections may need padding to start a streaming AudioTrack.
            int padding = track.getBufferSizeInFrames() - length;
            if (padding > 0 && !stopped.getAsBoolean()) write(track, new short[padding], 0, padding, stopped);
            long deadline = SystemClock.elapsedRealtime() + OUTPUT_TIMEOUT_MS;
            while (!stopped.getAsBoolean() && track.getPlaybackHeadPosition() < length) waitForOutput(deadline);
        } finally {
            try {
                if (track.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) {
                    track.pause();
                    track.flush();
                }
            } finally {
                track.release();
            }
        }
    }

    private static void write(AudioTrack track, short[] buffer, int offset, int count, BooleanSupplier stopped)
            throws IOException, InterruptedException {
        int end = offset + count;
        long deadline = SystemClock.elapsedRealtime() + OUTPUT_TIMEOUT_MS;
        while (offset < end && !stopped.getAsBoolean()) {
            int written = track.write(buffer, offset, end - offset, AudioTrack.WRITE_NON_BLOCKING);
            if (written < 0) throw new IOException("AudioTrack error " + written);
            if (written == 0) {
                waitForOutput(deadline);
            } else {
                offset += written;
                deadline = SystemClock.elapsedRealtime() + OUTPUT_TIMEOUT_MS;
            }
        }
    }

    private static void waitForOutput(long deadline) throws IOException, InterruptedException {
        if (SystemClock.elapsedRealtime() >= deadline) throw new IOException("Audio output timed out");
        Thread.sleep(10);
    }
}

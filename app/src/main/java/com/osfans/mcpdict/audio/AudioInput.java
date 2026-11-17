package com.osfans.mcpdict.audio;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaRecorder;
import android.net.Uri;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.function.BooleanSupplier;
import java.util.function.IntSupplier;

final class AudioInput {
    private static final int WAIT_US = 10_000;

    private AudioInput() {}

    static void record(Context context, AudioAnalysis analysis, BooleanSupplier stop, Runnable publish)
            throws IOException {
        if (context.checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            throw new SecurityException("Microphone permission missing");
        }
        AudioRecord recorder = null;
        for (int rate : new int[]{AudioAnalysis.RATE, 44_100, 48_000}) {
            int minimum = AudioRecord.getMinBufferSize(rate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
            if (minimum <= 0) continue;
            AudioRecord candidate = new AudioRecord(MediaRecorder.AudioSource.VOICE_RECOGNITION, rate,
                    AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, Math.max(minimum, rate / 5));
            if (candidate.getState() == AudioRecord.STATE_INITIALIZED) {
                recorder = candidate;
                break;
            }
            candidate.release();
        }
        if (recorder == null) throw new IOException("Cannot initialize the microphone");
        try {
            if (stop.getAsBoolean()) return;
            recorder.startRecording();
            if (recorder.getRecordingState() != AudioRecord.RECORDSTATE_RECORDING) {
                throw new IOException("Cannot start recording");
            }
            int rate = recorder.getSampleRate();
            analysis.setInputRate(rate);
            short[] buffer = new short[rate / 50];
            int published = 0;
            while (!stop.getAsBoolean() && !analysis.isFull()) {
                int count = recorder.read(buffer, 0, buffer.length, AudioRecord.READ_BLOCKING);
                if (count < 0) throw new IOException("AudioRecord error " + count);
                for (int i = 0; i < count; i++) analysis.accept(buffer[i] / 32768f);
                if (analysis.sampleCount() - published >= AudioAnalysis.RATE / 12) {
                    publish.run();
                    published = analysis.sampleCount();
                }
            }
        } finally {
            try {
                if (recorder.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) recorder.stop();
            } finally {
                recorder.release();
            }
        }
    }

    static long read(Context context, Uri uri, AudioAnalysis analysis, BooleanSupplier stop,
                     IntSupplier chooseStereoChannel) throws IOException {
        MediaExtractor extractor = new MediaExtractor();
        try {
            extractor.setDataSource(context, uri, null);
            for (int track = 0; track < extractor.getTrackCount(); track++) {
                MediaFormat format = extractor.getTrackFormat(track);
                String mime = format.getString(MediaFormat.KEY_MIME);
                if (mime == null || !mime.startsWith("audio/")) continue;
                extractor.selectTrack(track);
                if (mime.equals("audio/raw")) readPcm(extractor, format, analysis, stop, chooseStereoChannel);
                else decode(extractor, format, mime, analysis, stop, chooseStereoChannel);
                return format.containsKey(MediaFormat.KEY_DURATION) ? format.getLong(MediaFormat.KEY_DURATION) : 0;
            }
            throw new IOException("No audio track in the selected document");
        } finally {
            extractor.release();
        }
    }

    private static void readPcm(MediaExtractor extractor, MediaFormat format, AudioAnalysis analysis,
                                BooleanSupplier stop, IntSupplier chooseStereoChannel) {
        int bufferSize = format.containsKey(MediaFormat.KEY_MAX_INPUT_SIZE)
                ? Math.max(65_536, format.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE)) : 65_536;
        ByteBuffer buffer = ByteBuffer.allocateDirect(bufferSize).order(ByteOrder.LITTLE_ENDIAN);
        Pcm pcm = new Pcm(format, analysis, chooseStereoChannel);
        while (!stop.getAsBoolean() && !analysis.isFull()) {
            buffer.clear();
            int length = extractor.readSampleData(buffer, 0);
            if (length < 0) return;
            buffer.position(0);
            buffer.limit(length);
            pcm.feed(buffer);
            if (!extractor.advance()) return;
        }
    }

    private static void decode(MediaExtractor extractor, MediaFormat format, String mime, AudioAnalysis analysis,
                               BooleanSupplier stop, IntSupplier chooseStereoChannel) throws IOException {
        MediaCodec codec = MediaCodec.createDecoderByType(mime);
        try {
            codec.configure(format, null, null, 0);
            codec.start();
            MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
            boolean queuedEnd = false;
            Pcm pcm = null;
            while (!analysis.isFull() && !stop.getAsBoolean()) {
                if (!queuedEnd) {
                    int input = codec.dequeueInputBuffer(WAIT_US);
                    if (input >= 0) {
                        ByteBuffer buffer = codec.getInputBuffer(input);
                        if (buffer == null) throw new IOException("Decoder input unavailable");
                        buffer.clear();
                        int length = extractor.readSampleData(buffer, 0);
                        if (length < 0) {
                            codec.queueInputBuffer(input, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                            queuedEnd = true;
                        } else {
                            codec.queueInputBuffer(input, 0, length, extractor.getSampleTime(), 0);
                            extractor.advance();
                        }
                    }
                }
                int output = codec.dequeueOutputBuffer(info, WAIT_US);
                if (output == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    pcm = new Pcm(codec.getOutputFormat(), analysis, chooseStereoChannel);
                }
                if (output < 0) continue;
                try {
                    ByteBuffer buffer = codec.getOutputBuffer(output);
                    if (info.size > 0 && buffer != null && (info.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) == 0) {
                        if (pcm == null) pcm = new Pcm(codec.getOutputFormat(output), analysis, chooseStereoChannel);
                        if (stop.getAsBoolean()) return;
                        buffer.position(info.offset);
                        buffer.limit(info.offset + info.size);
                        pcm.feed(buffer.order(ByteOrder.nativeOrder()));
                    }
                } finally {
                    codec.releaseOutputBuffer(output, false);
                }
                if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) return;
            }
        } finally {
            codec.release();
        }
    }

    private static final class Pcm {
        private final AudioAnalysis analysis;
        private final int channels;
        private final int encoding;
        private final int frameSize;
        private final int selectedChannel;

        Pcm(MediaFormat format, AudioAnalysis analysis, IntSupplier chooseStereoChannel) {
            this.analysis = analysis;
            this.channels = format.getInteger(MediaFormat.KEY_CHANNEL_COUNT);
            if (channels <= 0) throw new IllegalArgumentException("Invalid channel count");
            this.encoding = format.containsKey(MediaFormat.KEY_PCM_ENCODING)
                    ? format.getInteger(MediaFormat.KEY_PCM_ENCODING) : AudioFormat.ENCODING_PCM_16BIT;
            int bytes = switch (encoding) {
                case AudioFormat.ENCODING_PCM_8BIT -> 1;
                case AudioFormat.ENCODING_PCM_16BIT -> 2;
                case AudioFormat.ENCODING_PCM_24BIT_PACKED -> 3;
                case AudioFormat.ENCODING_PCM_FLOAT, AudioFormat.ENCODING_PCM_32BIT -> 4;
                default -> throw new IllegalArgumentException("Unsupported PCM encoding: " + encoding);
            };
            frameSize = channels * bytes;
            selectedChannel = channels == 2 ? chooseStereoChannel.getAsInt() : -1;
            analysis.setInputRate(format.getInteger(MediaFormat.KEY_SAMPLE_RATE));
        }

        void feed(ByteBuffer buffer) {
            while (buffer.remaining() >= frameSize && !analysis.isFull()) {
                float sample = 0;
                for (int i = 0; i < channels; i++) {
                    float value = switch (encoding) {
                        case AudioFormat.ENCODING_PCM_8BIT -> ((buffer.get() & 255) - 128) / 128f;
                        case AudioFormat.ENCODING_PCM_16BIT -> buffer.getShort() / 32768f;
                        case AudioFormat.ENCODING_PCM_24BIT_PACKED -> {
                            int lo = buffer.get() & 255;
                            int mid = buffer.get() & 255;
                            yield (lo | (mid << 8) | (buffer.get() << 16)) / 8388608f;
                        }
                        case AudioFormat.ENCODING_PCM_32BIT -> buffer.getInt() / 2147483648f;
                        default -> buffer.getFloat();
                    };
                    if (selectedChannel < 0 || i == selectedChannel) sample += value;
                }
                analysis.accept(selectedChannel < 0 ? sample / channels : sample);
            }
        }
    }
}

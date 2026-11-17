package com.osfans.mcpdict.audio;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.osfans.mcpdict.R;
import com.osfans.mcpdict.Util.App;
import com.osfans.mcpdict.Util.FontUtil;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class RecordingAnalysisActivity extends AppCompatActivity {
    private final ExecutorService worker = Executors.newSingleThreadExecutor();
    private Session active;
    private AlertDialog channelDialog;
    private AudioPlotView plot;
    private TextView status;
    private Button record;
    private Button importAudio;
    private Button all;
    private Button zoomIn;
    private Button zoomOut;
    private Button selected;
    private Button previous;
    private SeekBar timeline;
    private TextView measurement;
    private final ActivityResultLauncher<String> requestMicrophone = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), allowed -> {
                if (allowed && active == null) start(null);
                if (!allowed) {
                    status.setText(R.string.audio_demo_permission_denied);
                    status.setVisibility(View.VISIBLE);
                }
            });
    private final ActivityResultLauncher<String[]> openAudio = registerForActivityResult(
            new ActivityResultContracts.OpenDocument(), uri -> {
                if (uri != null && active == null) start(uri);
            });
    private static final class Session {
        final boolean recording;
        volatile boolean stop;
        int stereoChannel = -1;

        Session(boolean recording) { this.recording = recording; }
    }

    @Override public void onCreate(Bundle savedInstanceState) {
        App.setLocale();
        App.setActivityTheme(this);
        super.onCreate(savedInstanceState);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        setContentView(root);

        Toolbar toolbar = new Toolbar(this);
        toolbar.setTitle(R.string.audio_demo_title);
        root.addView(toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        LinearLayout buttons = new LinearLayout(this);
        buttons.setPadding(dp(12), 0, dp(12), 0);
        root.addView(buttons, new LinearLayout.LayoutParams(-1, -2));
        record = new Button(this);
        record.setText(R.string.audio_demo_record);
        record.setOnClickListener(v -> recordOrStop());
        buttons.addView(record, new LinearLayout.LayoutParams(0, -2, 1));

        importAudio = new Button(this);
        importAudio.setText(R.string.audio_demo_import);
        importAudio.setOnClickListener(v -> openAudio.launch(new String[]{"audio/*"}));
        LinearLayout.LayoutParams importParams = new LinearLayout.LayoutParams(0, -2, 1);
        importParams.setMarginStart(dp(8));
        buttons.addView(importAudio, importParams);

        status = new TextView(this);
        status.setVisibility(View.GONE);
        status.setPadding(dp(16), dp(2), dp(16), dp(10));
        FontUtil.setTypeface(status);
        root.addView(status);
        plot = new AudioPlotView(this);
        root.addView(plot, new LinearLayout.LayoutParams(-1, 0, 1));

        measurement = new TextView(this);
        measurement.setPadding(dp(16), 0, dp(16), 0);
        measurement.setTextSize(12);
        root.addView(measurement);

        timeline = new SeekBar(this);
        timeline.setMax(10_000);
        timeline.setContentDescription(getString(R.string.audio_demo_timeline));
        timeline.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar bar, int progress, boolean fromUser) {
                if (fromUser) plot.scrollTo(progress / 10_000f);
            }
            @Override public void onStartTrackingTouch(SeekBar bar) {}
            @Override public void onStopTrackingTouch(SeekBar bar) {}
        });
        root.addView(timeline, new LinearLayout.LayoutParams(-1, dp(26)));

        LinearLayout zoom = new LinearLayout(this);
        zoom.setPadding(dp(8), 0, dp(8), dp(4));
        root.addView(zoom, new LinearLayout.LayoutParams(-1, -2));
        all = zoomButton(zoom, R.string.audio_demo_zoom_all, plot::showAll);
        zoomIn = zoomButton(zoom, R.string.audio_demo_zoom_in, () -> plot.zoom(0.5f));
        zoomOut = zoomButton(zoom, R.string.audio_demo_zoom_out, () -> plot.zoom(2));
        selected = zoomButton(zoom, R.string.audio_demo_zoom_selection, plot::zoomToSelection);
        previous = zoomButton(zoom, R.string.audio_demo_zoom_back, plot::previous);
        plot.setOnChange(this::updateControls);
        updateControls();
    }

    @Override public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(R.string.audio_demo_layers)
                .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS)
                .setOnMenuItemClickListener(item -> {
                    showLayers();
                    return true;
                });
        return true;
    }

    private void showLayers() {
        AudioPlotView.Layer[] layers = AudioPlotView.Layer.values();
        String[] names = new String[layers.length];
        boolean[] values = new boolean[layers.length];
        for (int i = 0; i < layers.length; i++) {
            names[i] = getString(layers[i].title);
            values[i] = plot.visible(layers[i]);
        }
        new AlertDialog.Builder(this)
                .setTitle(R.string.audio_demo_layers)
                .setMultiChoiceItems(names, values, (dialog, index, checked) -> plot.setVisible(layers[index], checked))
                .setPositiveButton(R.string.ok, null)
                .show();
    }

    private Button zoomButton(LinearLayout container, int title, Runnable action) {
        Button button = new Button(this);
        button.setText(title);
        button.setTextSize(12);
        button.setMinWidth(0);
        button.setMinHeight(0);
        button.setPadding(0, 0, 0, 0);
        button.setOnClickListener(view -> action.run());
        container.addView(button, new LinearLayout.LayoutParams(0, dp(44), 1));
        return button;
    }

    private void updateControls() {
        all.setEnabled(plot.canZoomOut());
        zoomIn.setEnabled(plot.canZoomIn());
        zoomOut.setEnabled(plot.canZoomOut());
        selected.setEnabled(plot.hasSelection());
        previous.setEnabled(plot.canGoBack());
        timeline.setEnabled(plot.canZoomOut());
        timeline.setProgress(Math.round(10_000 * plot.scrollPosition()));
        measurement.setVisibility(plot.hasCursor() ? View.VISIBLE : View.GONE);
        measurement.setText(plot.selectionReadout());
    }

    private void recordOrStop() {
        if (active != null) {
            active.stop = true;
            record.setEnabled(false);
        } else if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            start(null);
        } else {
            requestMicrophone.launch(Manifest.permission.RECORD_AUDIO);
        }
    }

    private void start(Uri uri) {
        Session session = new Session(uri == null);
        active = session;
        if (session.recording) plot.beginRecording();
        record.setText(session.recording ? R.string.audio_demo_stop : R.string.audio_demo_cancel_import);
        record.setEnabled(true);
        importAudio.setEnabled(false);
        status.setText(session.recording ? R.string.audio_demo_recording : R.string.audio_demo_reading);
        status.setVisibility(View.VISIBLE);
        worker.execute(() -> analyze(session, uri));
    }

    private void analyze(Session session, Uri uri) {
        AudioAnalysis analysis = new AudioAnalysis();
        boolean failed = false;
        long durationUs = 0;
        try {
            if (session.recording) {
                AudioInput.record(this, analysis, () -> session.stop, () -> {
                    List<AudioAnalysis.Frame> frames = analysis.drain();
                    int samples = analysis.sampleCount();
                    runOnUiThread(() -> {
                        if (active != session) return;
                        plot.append(frames, samples);
                        status.setText(getString(R.string.audio_demo_recording_time, seconds(samples)));
                    });
                });
            } else {
                durationUs = AudioInput.read(this, uri, analysis, () -> session.stop, () -> chooseStereoChannel(session));
            }
        } catch (IOException | RuntimeException error) {
            Log.w("RecordingAnalysis", "Cannot analyze audio", error);
            failed = true;
        }
        analysis.finish();
        List<AudioAnalysis.Frame> remaining = analysis.drain();
        int samples = analysis.sampleCount();
        boolean error = failed;
        double fileSeconds = durationUs / 1_000_000.0;
        runOnUiThread(() -> finishSession(session, remaining, samples, fileSeconds, error));
    }

    private int chooseStereoChannel(Session session) {
        if (session.stereoChannel >= 0 || session.stop) return session.stereoChannel;
        CountDownLatch dismissed = new CountDownLatch(1);
        runOnUiThread(() -> {
            if (active != session || session.stop || isFinishing() || isDestroyed()) {
                dismissed.countDown();
                return;
            }
            String[] channels = {getString(R.string.audio_demo_channel_left), getString(R.string.audio_demo_channel_right)};
            channelDialog = new AlertDialog.Builder(this)
                    .setTitle(R.string.audio_demo_choose_channel)
                    .setItems(channels, (dialog, index) -> session.stereoChannel = index)
                    .setNegativeButton(R.string.cancel, null)
                    .setOnDismissListener(dialog -> {
                        channelDialog = null;
                        dismissed.countDown();
                    })
                    .show();
        });
        try {
            dismissed.await();
        } catch (InterruptedException error) {
            Thread.currentThread().interrupt();
            session.stop = true;
            return -1;
        }
        if (session.stereoChannel < 0) session.stop = true;
        return session.stereoChannel;
    }

    private void finishSession(Session session, List<AudioAnalysis.Frame> remaining, int samples,
                               double fileSeconds, boolean failed) {
        if (active != session) return;
        active = null;
        record.setText(R.string.audio_demo_record);
        record.setEnabled(true);
        importAudio.setEnabled(true);
        if (session.recording) {
            plot.append(remaining, samples);
            plot.endRecording();
        } else if (!failed && !session.stop && samples > 0) {
            plot.setImport(remaining, samples);
        }
        if (session.stop && !session.recording) status.setText(R.string.audio_demo_import_canceled);
        else if (failed) status.setText(session.recording ? R.string.audio_demo_record_error : R.string.audio_demo_import_error);
        else if (samples == 0) status.setText(R.string.audio_demo_no_audio);
        else if (fileSeconds > 0) status.setText(getString(R.string.audio_demo_duration, fileSeconds));
        else if (!session.recording && samples == AudioAnalysis.MAX_SAMPLES) {
            status.setText(getString(R.string.audio_demo_limit, seconds(samples)));
        } else status.setText(getString(R.string.audio_demo_duration, seconds(samples)));
    }

    @Override protected void onStop() {
        if (active != null && active.recording) active.stop = true;
        super.onStop();
    }

    @Override protected void onDestroy() {
        if (active != null) active.stop = true;
        active = null;
        if (channelDialog != null) channelDialog.dismiss();
        worker.shutdownNow();
        super.onDestroy();
    }

    private static double seconds(int samples) { return (double) samples / AudioAnalysis.RATE; }
    private int dp(float value) { return Math.round(getResources().getDisplayMetrics().density * value); }
}

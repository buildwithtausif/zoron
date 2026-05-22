package com.zoron.whyred;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.topjohnwu.superuser.Shell;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.os.Build;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.content.pm.PackageManager;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.progressindicator.LinearProgressIndicator;

public class MainActivity extends AppCompatActivity {
    private TextView tvLogs, tvCurrentProfile, tvCpuInfo, tvPowerState, tvProcessReport;
    private View powerStateDot;
    private LineChart batteryChart;
    private LinearLayout legacyContainer;
    private TextView tvLegacyToggle;
    private boolean legacyExpanded = false;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable updateRunnable;

    // Cached data for export
    private String lastCsvData = "";
    private String lastLogData = "";

    // Progress bar views
    private MaterialCardView cardTransitionProgress;
    private TextView tvTransitionStatus, tvTransitionDetail;
    private LinearProgressIndicator transitionProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);

        // Core views
        tvLogs = findViewById(R.id.tvLogs);
        tvCurrentProfile = findViewById(R.id.tvCurrentProfile);
        tvCpuInfo = findViewById(R.id.tvCpuInfo);
        tvPowerState = findViewById(R.id.tvPowerState);
        tvProcessReport = findViewById(R.id.tvProcessReport);
        powerStateDot = findViewById(R.id.powerStateDot);
        batteryChart = findViewById(R.id.batteryChart);

        // Legacy section toggle
        legacyContainer = findViewById(R.id.legacyContainer);
        tvLegacyToggle = findViewById(R.id.tvLegacyToggle);
        findViewById(R.id.legacyHeader).setOnClickListener(v -> toggleLegacy());

        setupChart();

        // ZORON-X Mode Cards
        findViewById(R.id.cardZoronBalanced).setOnClickListener(v -> applyZoronMode("balanced"));
        findViewById(R.id.cardZoronDeep).setOnClickListener(v -> applyZoronMode("deep"));
        findViewById(R.id.cardZoronHibernation).setOnClickListener(v -> applyZoronMode("hibernation"));
        findViewById(R.id.cardZoronBurst).setOnClickListener(v -> applyZoronMode("burst"));
        findViewById(R.id.cardZoronNightwatch).setOnClickListener(v -> applyZoronMode("nightwatch"));

        // Legacy Profile Cards
        findViewById(R.id.cardNone).setOnClickListener(v -> applyLegacyProfile("none"));
        findViewById(R.id.cardBattery).setOnClickListener(v -> applyLegacyProfile("battery"));
        
        findViewById(R.id.cardBalanced).setOnClickListener(v -> {
            new AlertDialog.Builder(MainActivity.this)
                .setTitle("Legacy Mode Warning")
                .setMessage("This is a legacy profile. ZORON-X 'Balanced' offers superior thermal awareness and responsiveness. Would you like to try the ZORON-X version instead?")
                .setPositiveButton("Use ZORON-X", (dialog, which) -> applyZoronMode("balanced"))
                .setNegativeButton("Continue Legacy", (dialog, which) -> applyLegacyProfile("balanced"))
                .show();
        });
        
        findViewById(R.id.cardPerformance).setOnClickListener(v -> {
            new AlertDialog.Builder(MainActivity.this)
                .setTitle("Legacy Mode Warning")
                .setMessage("This is a legacy profile. ZORON-X 'Burst' provides better touch boosting and microburst engine support. Would you like to try ZORON-X Burst instead?")
                .setPositiveButton("Use ZORON-X Burst", (dialog, which) -> applyZoronMode("burst"))
                .setNegativeButton("Continue Legacy", (dialog, which) -> applyLegacyProfile("performance"))
                .show();
        });

        // Export buttons
        findViewById(R.id.btnExportCsv).setOnClickListener(v -> exportCsv());
        findViewById(R.id.btnExportLogs).setOnClickListener(v -> exportLogs());
        findViewById(R.id.btnExportProcesses).setOnClickListener(v -> exportProcessReport());

        // Transition progress bar
        cardTransitionProgress = findViewById(R.id.cardTransitionProgress);
        tvTransitionStatus = findViewById(R.id.tvTransitionStatus);
        tvTransitionDetail = findViewById(R.id.tvTransitionDetail);
        transitionProgressBar = findViewById(R.id.transitionProgressBar);
        findViewById(R.id.btnHideProgress).setOnClickListener(v -> cardTransitionProgress.setVisibility(View.GONE));

        // Check for OTA updates automatically on start
        OTAUpdater.checkUpdates(this, false);

        // Start foreground service for persistent notification
        startZoronService();

        // Start dashboard auto-refresh (15s for lower overhead)
        updateRunnable = new Runnable() {
            @Override
            public void run() {
                refreshDashboard();
                handler.postDelayed(this, 15000);
            }
        };
        handler.post(updateRunnable);

        // Start pulse animation on power state dot
        startPulseAnimation();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_check_updates) {
            OTAUpdater.checkUpdates(this, true);
            return true;
        } else if (item.getItemId() == R.id.action_settings) {
            startActivity(new android.content.Intent(this, SettingsActivity.class));
            return true;
        } else if (item.getItemId() == R.id.action_docs) {
            startActivity(new android.content.Intent(this, ModeLearnActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(updateRunnable);
    }

    // ==================== CHART SETUP (Material Expressive) ====================

    private void setupChart() {
        batteryChart.getDescription().setEnabled(false);
        batteryChart.setTouchEnabled(true);
        batteryChart.setDragEnabled(true);
        batteryChart.setScaleEnabled(true);
        batteryChart.setPinchZoom(true);
        batteryChart.setDrawGridBackground(false);
        batteryChart.setBackgroundColor(Color.TRANSPARENT);
        batteryChart.setExtraOffsets(8, 8, 8, 12);

        // Material Expressive X-Axis
        XAxis xAxis = batteryChart.getXAxis();
        xAxis.setTextColor(Color.parseColor("#B0B0B0"));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(false);
        xAxis.setTextSize(10f);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int mins = (int) value;
                if (mins < 60) return mins + "m";
                return (mins / 60) + "h" + (mins % 60 > 0 ? (mins % 60) + "m" : "");
            }
        });

        // Material Expressive Y-Axis
        YAxis leftAxis = batteryChart.getAxisLeft();
        leftAxis.setTextColor(Color.parseColor("#B0B0B0"));
        leftAxis.setAxisMaximum(100f);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setDrawGridLines(true);
        leftAxis.setGridColor(Color.parseColor("#1A1A1A"));
        leftAxis.setGridLineWidth(0.5f);
        leftAxis.enableGridDashedLine(8f, 4f, 0f);
        leftAxis.setDrawAxisLine(false);
        leftAxis.setTextSize(10f);
        leftAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return (int) value + "%";
            }
        });

        batteryChart.getAxisRight().setEnabled(false);

        // Material Expressive Legend
        Legend legend = batteryChart.getLegend();
        legend.setTextColor(Color.parseColor("#D0D0D0"));
        legend.setTextSize(11f);
        legend.setForm(Legend.LegendForm.CIRCLE);
        legend.setFormSize(8f);
        legend.setXEntrySpace(16f);
        legend.setYOffset(8f);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);

        // Smooth animations
        batteryChart.animateX(800);
    }

    // ==================== DASHBOARD REFRESH ====================

    private void refreshDashboard() {
        new Thread(() -> {
            Shell.Result result = Shell.cmd(
                    "cat /data/local/tmp/zoron/profile.txt 2>/dev/null || echo ''",
                    "echo '---SEP---'",
                    "cat /sys/devices/system/cpu/cpu0/cpufreq/scaling_governor 2>/dev/null || echo ''",
                    "echo '---SEP---'",
                    "tail -n 50 /data/local/tmp/zoron/log.txt 2>/dev/null || echo 'No logs.'",
                    "echo '---SEP---'",
                    "cat /data/local/tmp/zoron/battery.csv 2>/dev/null || echo ''",
                    "echo '---SEP---'",
                    "cat /data/local/tmp/zoron/power_state.txt 2>/dev/null || echo 'UNKNOWN'",
                    "echo '---SEP---'",
                    "tail -n 40 /data/local/tmp/zoron/process_report.txt 2>/dev/null || echo 'No process data yet.'"
            ).exec();

            if (!result.isSuccess()) {
                runOnUiThread(() -> {
                    tvCurrentProfile.setText("Mode: ERROR (Magisk Access Denied)");
                    tvLogs.setText("Error reading data. Make sure module is flashed and active.");
                });
                return;
            }

            StringBuilder out = new StringBuilder();
            for (String s : result.getOut()) {
                out.append(s).append("\n");
            }
            String outputStr = out.toString();

            String[] parts = outputStr.split("---SEP---\n");
            if (parts.length < 6) return;

            String profile = parts[0].trim();
            String gov = parts[1].trim();
            String logs = parts[2].trim();
            String csvData = parts[3].trim();
            String powerState = parts[4].trim();
            String processReport = parts[5].trim();

            // Cache for export
            lastCsvData = csvData;
            lastLogData = logs;

            runOnUiThread(() -> {
                // Update profile display
                String displayProfile = profile.isEmpty() ? "Unknown" : profile.toUpperCase();
                tvCurrentProfile.setText("Mode: " + displayProfile);
                tvCpuInfo.setText("CPU Governor: " + (gov.isEmpty() ? "Unknown" : gov));

                // Update power state with color
                updatePowerState(powerState);

                // Update logs
                tvLogs.setText(logs);

                // Update process report
                tvProcessReport.setText(processReport);

                // Update chart
                plotChart(csvData);
            });
        }).start();
    }

    // ==================== POWER STATE DISPLAY ====================

    private void updatePowerState(String state) {
        String displayState;
        int dotColor;

        if (state.startsWith("HYPER_ACTIVE")) {
            displayState = "⚡ HYPER ACTIVE";
            dotColor = Color.parseColor("#FF4444");
        } else if (state.startsWith("INTERACTIVE")) {
            displayState = "✋ INTERACTIVE";
            dotColor = Color.parseColor("#00FF7F");
        } else if (state.startsWith("LIGHT_IDLE")) {
            displayState = "💤 LIGHT IDLE";
            dotColor = Color.parseColor("#FFD700");
        } else if (state.startsWith("DEEP_IDLE")) {
            displayState = "🔒 DEEP IDLE";
            dotColor = Color.parseColor("#2196F3");
        } else if (state.startsWith("SLEEP_IDLE")) {
            displayState = "🌙 SLEEP IDLE";
            dotColor = Color.parseColor("#7B68EE");
        } else {
            displayState = "📡 DETECTING...";
            dotColor = Color.parseColor("#808080");
        }

        tvPowerState.setText(displayState);

        // Update dot color
        GradientDrawable dot = (GradientDrawable) powerStateDot.getBackground();
        dot.setColor(dotColor);
    }

    private void startPulseAnimation() {
        AlphaAnimation pulse = new AlphaAnimation(1.0f, 0.3f);
        pulse.setDuration(1000);
        pulse.setRepeatMode(Animation.REVERSE);
        pulse.setRepeatCount(Animation.INFINITE);
        powerStateDot.startAnimation(pulse);
    }

    // ==================== CHART PLOTTING (Material Expressive) ====================

    private void plotChart(String csvData) {
        if (csvData == null || csvData.isEmpty()) return;

        Map<String, List<Entry>> profileData = new HashMap<>();
        String[] lines = csvData.split("\n");

        long firstTimestamp = -1;

        for (String line : lines) {
            String[] tokens = line.trim().split(",");
            if (tokens.length >= 3) {
                try {
                    long ts = Long.parseLong(tokens[0]);
                    float level = Float.parseFloat(tokens[1]);
                    String profile = tokens[2];

                    if (firstTimestamp == -1) firstTimestamp = ts;

                    float xValue = (ts - firstTimestamp) / 60f;

                    if (!profileData.containsKey(profile)) {
                        profileData.put(profile, new ArrayList<>());
                    }
                    profileData.get(profile).add(new Entry(xValue, level));
                } catch (Exception ignored) {}
            }
        }

        LineData lineData = new LineData();

        // Material Expressive color palette — vibrant, harmonious, modern
        int[][] colorPalettes = {
            {Color.parseColor("#00E676"), Color.parseColor("#1B5E20")}, // Green gradient
            {Color.parseColor("#448AFF"), Color.parseColor("#1A237E")}, // Blue gradient
            {Color.parseColor("#FF5252"), Color.parseColor("#B71C1C")}, // Red gradient
            {Color.parseColor("#FFD740"), Color.parseColor("#F57F17")}, // Amber gradient
            {Color.parseColor("#E040FB"), Color.parseColor("#7B1FA2")}, // Purple gradient
            {Color.parseColor("#18FFFF"), Color.parseColor("#006064")}, // Cyan gradient
        };
        int colorIdx = 0;

        for (Map.Entry<String, List<Entry>> entry : profileData.entrySet()) {
            LineDataSet set = new LineDataSet(entry.getValue(), entry.getKey().toUpperCase());

            int mainColor = colorPalettes[colorIdx % colorPalettes.length][0];
            int fillColor = colorPalettes[colorIdx % colorPalettes.length][1];

            // Material Expressive line styling
            set.setColor(mainColor);
            set.setLineWidth(2.5f);
            set.setDrawCircles(false);
            set.setDrawValues(false);
            set.setMode(LineDataSet.Mode.CUBIC_BEZIER);
            set.setCubicIntensity(0.15f);

            // Gradient fill under the curve
            set.setDrawFilled(true);
            set.setFillColor(fillColor);
            set.setFillAlpha(40);

            // Highlight styling
            set.setHighLightColor(mainColor);
            set.setHighlightLineWidth(1f);
            set.setDrawHorizontalHighlightIndicator(false);

            lineData.addDataSet(set);
            colorIdx++;
        }

        batteryChart.setData(lineData);
        batteryChart.animateX(600);
        batteryChart.invalidate();
    }

    // ==================== LEGACY SECTION TOGGLE ====================

    private void toggleLegacy() {
        legacyExpanded = !legacyExpanded;
        legacyContainer.setVisibility(legacyExpanded ? View.VISIBLE : View.GONE);
        tvLegacyToggle.setText(legacyExpanded ? "▼" : "▶");
    }

    // ==================== EXPORT FUNCTIONS ====================

    private void exportCsv() {
        new Thread(() -> {
            Shell.Result result = Shell.cmd("cat /data/local/tmp/zoron/battery.csv 2>/dev/null").exec();
            if (!result.isSuccess() || result.getOut().isEmpty()) {
                runOnUiThread(() -> Toast.makeText(this, "No CSV data to export", Toast.LENGTH_SHORT).show());
                return;
            }

            StringBuilder csvContent = new StringBuilder();
            csvContent.append("Timestamp,Battery Level,Profile,Power State,CPU Freq,Temperature\n");
            for (String line : result.getOut()) {
                csvContent.append(line).append("\n");
            }

            try {
                File exportFile = new File(getExternalCacheDir(), "zoron_battery_export.csv");
                FileWriter writer = new FileWriter(exportFile);
                writer.write(csvContent.toString());
                writer.close();

                Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", exportFile);
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/csv");
                shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Zoron Battery Analytics Export");
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                runOnUiThread(() -> startActivity(Intent.createChooser(shareIntent, "Export Battery Data")));
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "Export failed: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        }).start();
    }

    private void exportLogs() {
        new Thread(() -> {
            Shell.Result result = Shell.cmd("cat /data/local/tmp/zoron/log.txt 2>/dev/null").exec();
            if (!result.isSuccess() || result.getOut().isEmpty()) {
                runOnUiThread(() -> Toast.makeText(this, "No logs to export", Toast.LENGTH_SHORT).show());
                return;
            }

            StringBuilder logContent = new StringBuilder();
            for (String line : result.getOut()) {
                logContent.append(line).append("\n");
            }

            try {
                File exportFile = new File(getExternalCacheDir(), "zoron_diagnostics.txt");
                FileWriter writer = new FileWriter(exportFile);
                writer.write(logContent.toString());
                writer.close();

                Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", exportFile);
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Zoron Diagnostics Export");
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                runOnUiThread(() -> startActivity(Intent.createChooser(shareIntent, "Export Diagnostics")));
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "Export failed: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        }).start();
    }

    // ==================== ERROR DIALOG ====================

    private void showErrorDialog(String errorMsg) {
        new AlertDialog.Builder(this)
            .setTitle("Execution Error")
            .setMessage(errorMsg)
            .setPositiveButton("Copy", (dialog, which) -> {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Zoron Error", errorMsg);
                if (clipboard != null) {
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(MainActivity.this, "Copied to clipboard", Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton("Close", null)
            .show();
    }

    // ==================== ZORON-X MODE APPLICATION ====================

    private void applyZoronMode(String mode) {
        // Show progress bar
        cardTransitionProgress.setVisibility(View.VISIBLE);
        transitionProgressBar.setProgress(0);
        tvTransitionStatus.setText("⚡ Applying " + mode.toUpperCase() + "...");
        tvTransitionDetail.setText("Running zoron_fastpath.sh");

        // Optimistic UI Update
        tvCurrentProfile.setText("Mode: APPLYING...");

        new Thread(() -> {
            // 1. Check if Fastpath is enabled
            Shell.Result fpCheck = Shell.cmd("cat /data/local/tmp/zoron/fastpath_enabled.txt 2>/dev/null || echo '1'").exec();
            boolean fastpathEnabled = true;
            if (fpCheck.isSuccess() && !fpCheck.getOut().isEmpty()) {
                fastpathEnabled = !"0".equals(fpCheck.getOut().get(0).trim());
            }

            if (fastpathEnabled) {
                runOnUiThread(() -> {
                    transitionProgressBar.setProgress(20);
                    tvTransitionDetail.setText("Fastpath: CPU governor + frequencies");
                });

                Shell.cmd("sh /system/bin/zoron_fastpath.sh set_mode " + mode + " || sh /data/adb/modules/zoron_x_optimizer/system/bin/zoron_fastpath.sh set_mode " + mode).exec();

                runOnUiThread(() -> {
                    transitionProgressBar.setProgress(40);
                    tvCurrentProfile.setText("Mode: FASTPATH ACTIVE");
                    tvTransitionStatus.setText("\u26a1 Fastpath applied");
                    tvTransitionDetail.setText("Running zoron_engine " + mode);
                });
            } else {
                runOnUiThread(() -> {
                    transitionProgressBar.setProgress(40);
                    tvTransitionStatus.setText("Fastpath disabled — running engine directly");
                    tvTransitionDetail.setText("Running zoron_engine " + mode);
                });
            }

            // 2. Heavy Engine processing
            runOnUiThread(() -> {
                transitionProgressBar.setProgress(50);
                tvTransitionDetail.setText("Engine: Thermal + I/O + zRAM tuning");
            });

            String scriptCmd = String.join("; ",
                "SCRIPT_PATH=\"\"",
                "for p in /data/local/tmp/zoron/zoron_engine /system/bin/zoron_engine /data/adb/modules/zoron_x_optimizer/system/bin/zoron_engine; do " +
                    "if [ -f \"$p\" ]; then SCRIPT_PATH=\"$p\"; break; fi; done",
                "if [ -z \"$SCRIPT_PATH\" ]; then " +
                    "echo 'ERROR: zoron_engine not found.'; " +
                    "exit 1; fi",
                "sed 's/\\r$//' \"$SCRIPT_PATH\" | sh -s " + mode
            );
            Shell.Result result = Shell.cmd(scriptCmd).exec();

            runOnUiThread(() -> {
                if (result.isSuccess()) {
                    transitionProgressBar.setProgress(100);
                    tvTransitionStatus.setText("✅ " + mode.toUpperCase() + " complete");
                    tvTransitionDetail.setText("All optimizations applied successfully");
                    tvCurrentProfile.setText("Mode: " + mode.toUpperCase());
                    // Auto-hide after 3 seconds
                    handler.postDelayed(() -> cardTransitionProgress.setVisibility(View.GONE), 3000);
                    refreshDashboard();
                } else {
                    transitionProgressBar.setProgress(100);
                    tvTransitionStatus.setText("❌ Error applying " + mode.toUpperCase());
                    tvTransitionDetail.setText("Tap to view error details");
                    StringBuilder err = new StringBuilder("Error applying ZORON-X " + mode + " mode:\n");
                    err.append("Exit Code: ").append(result.getCode()).append("\n\n");
                    err.append("--- stdout ---\n");
                    for (String e : result.getOut()) err.append(e).append("\n");
                    err.append("\n--- stderr ---\n");
                    for (String e : result.getErr()) err.append(e).append("\n");
                    showErrorDialog(err.toString());
                }
            });
        }).start();
    }

    // ==================== LEGACY PROFILE APPLICATION ====================

    private void applyLegacyProfile(String profile) {
        new Thread(() -> {
            // Search paths in priority order
            String scriptCmd = String.join("; ",
                "SCRIPT_PATH=\"\"",
                "for p in /data/local/tmp/zoron/whyred_opt /system/bin/whyred_opt /data/adb/modules/zoron_x_optimizer/system/bin/whyred_opt /data/adb/modules/whyred_battery_optimizer/system/bin/whyred_opt; do " +
                    "if [ -f \"$p\" ]; then SCRIPT_PATH=\"$p\"; break; fi; done",
                "if [ -z \"$SCRIPT_PATH\" ]; then " +
                    "echo 'ERROR: whyred_opt not found.'; " +
                    "echo ''; " +
                    "echo 'Searched paths:'; " +
                    "echo '  /data/local/tmp/zoron/whyred_opt'; " +
                    "echo '  /system/bin/whyred_opt'; " +
                    "echo '  /data/adb/modules/zoron_x_optimizer/system/bin/whyred_opt'; " +
                    "echo '  /data/adb/modules/whyred_battery_optimizer/system/bin/whyred_opt'; " +
                    "echo ''; " +
                    "echo 'Please reflash the Zoron module ZIP and reboot.'; " +
                    "exit 1; fi",
                "sed 's/\\r$//' \"$SCRIPT_PATH\" | sh -s " + profile
            );
            Shell.Result result = Shell.cmd(scriptCmd).exec();
            new Handler(Looper.getMainLooper()).post(() -> {
                if (result.isSuccess()) {
                    Toast.makeText(MainActivity.this, profile.toUpperCase() + " profile applied!", Toast.LENGTH_SHORT).show();
                    refreshDashboard();
                } else {
                    StringBuilder err = new StringBuilder("Error applying " + profile + " profile:\n");
                    err.append("Exit Code: ").append(result.getCode()).append("\n\n");
                    err.append("--- stdout ---\n");
                    for (String e : result.getOut()) err.append(e).append("\n");
                    err.append("\n--- stderr ---\n");
                    for (String e : result.getErr()) err.append(e).append("\n");
                    showErrorDialog(err.toString());
                }
            });
        }).start();
    }

    // ==================== PROCESS REPORT EXPORT ====================

    private void exportProcessReport() {
        new Thread(() -> {
            Shell.Result result = Shell.cmd("cat /data/local/tmp/zoron/process_report.txt 2>/dev/null").exec();
            if (!result.isSuccess() || result.getOut().isEmpty()) {
                runOnUiThread(() -> Toast.makeText(this, "No process data to export", Toast.LENGTH_SHORT).show());
                return;
            }

            StringBuilder content = new StringBuilder();
            for (String line : result.getOut()) {
                content.append(line).append("\n");
            }

            try {
                File exportFile = new File(getExternalCacheDir(), "zoron_process_report.txt");
                FileWriter writer = new FileWriter(exportFile);
                writer.write(content.toString());
                writer.close();

                Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", exportFile);
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Zoron Process Report Export");
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                runOnUiThread(() -> startActivity(Intent.createChooser(shareIntent, "Export Process Report")));
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "Export failed: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        }).start();
    }

    // ==================== FOREGROUND SERVICE ====================

    private void startZoronService() {
        // Request notification permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 1001);
                return;
            }
        }
        Intent serviceIntent = new Intent(this, ZoronForegroundService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1001 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startZoronService();
        }
    }
}

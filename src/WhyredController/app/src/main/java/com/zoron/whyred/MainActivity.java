package com.zoron.whyred;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.AppOpsManager;
import android.provider.Settings;
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
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
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
    private ImageView ivLegacyToggle;
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
        ivLegacyToggle = findViewById(R.id.ivLegacyToggle);
        findViewById(R.id.legacyHeader).setOnClickListener(v -> toggleLegacy());

        // Card press animations for ZORON-X mode cards
        setupCardPressAnimation(findViewById(R.id.cardZoronBalanced));
        setupCardPressAnimation(findViewById(R.id.cardZoronDeep));
        setupCardPressAnimation(findViewById(R.id.cardZoronHibernation));
        setupCardPressAnimation(findViewById(R.id.cardZoronBurst));
        setupCardPressAnimation(findViewById(R.id.cardZoronNightwatch));

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

        android.content.SharedPreferences prefs = getSharedPreferences("ZoronSettings", MODE_PRIVATE);
        boolean isRoot = prefs.getBoolean("is_root", false);

        // Setup Non-Root permission card buttons
        if (!isRoot) {
            findViewById(R.id.btnGrantUsage).setOnClickListener(v -> requestUsageStatsPermission());
            findViewById(R.id.btnGrantWrite).setOnClickListener(v -> {
                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:" + getPackageName()));
                startActivity(intent);
            });
            findViewById(R.id.btnGrantBattery).setOnClickListener(v -> {
                try {
                    Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, Uri.parse("package:" + getPackageName()));
                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(this, "Enable ignore battery optimization manually in system settings.", Toast.LENGTH_LONG).show();
                }
            });
            showNonRootAdvisoryDialog();
        }

        // Developer attribution click listener
        View devAttribution = findViewById(R.id.cardDeveloperAttribution);
        if (devAttribution != null) {
            devAttribution.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/buildwithtausif/zoron"));
                startActivity(intent);
            });
        }

        // Check for OTA updates automatically on start
        OTAUpdater.checkUpdates(this, false);

        // Autopilot Service runs persistently to handle dynamic video boosts
        if (hasUsageStatsPermission()) {
            startService(new Intent(this, ZoronAutopilotService.class));
        }

        com.google.android.material.materialswitch.MaterialSwitch switchAutopilot = findViewById(R.id.switchAutopilot);
        boolean autopilotEnabled = prefs.getBoolean("autopilot_enabled", false);
        switchAutopilot.setChecked(autopilotEnabled);
        
        switchAutopilot.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (!hasUsageStatsPermission()) {
                    buttonView.setChecked(false);
                    requestUsageStatsPermission();
                } else {
                    prefs.edit().putBoolean("autopilot_enabled", true).apply();
                    startService(new Intent(MainActivity.this, ZoronAutopilotService.class));
                    Toast.makeText(MainActivity.this, "Autopilot Enabled", Toast.LENGTH_SHORT).show();
                }
            } else {
                prefs.edit().putBoolean("autopilot_enabled", false).apply();
                // Do NOT stop the service, let it run in background to detect video playback and boost manual modes
                Toast.makeText(MainActivity.this, "Autopilot Disabled (Dynamic Video Boost active)", Toast.LENGTH_SHORT).show();
            }
        });

        // Start foreground service for persistent notification
        startZoronService();

        // Start dashboard auto-refresh (20s for lower overhead)
        updateRunnable = new Runnable() {
            @Override
            public void run() {
                refreshDashboard();
                handler.postDelayed(this, 20000);
            }
        };
        handler.post(updateRunnable);

        // Start pulse animation on power state dot
        startPulseAnimation();

        // Staggered entry animations
        animateStaggeredEntry();
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
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(updateRunnable);
    }

    @Override
    protected void onResume() {
        super.onResume();
        handler.removeCallbacks(updateRunnable);
        handler.post(updateRunnable);

        // Non-Root Permissions card management
        android.content.SharedPreferences prefs = getSharedPreferences("ZoronSettings", MODE_PRIVATE);
        boolean isRoot = prefs.getBoolean("is_root", false);
        if (!isRoot) {
            MaterialCardView cardPermissions = findViewById(R.id.cardNonRootPermissions);
            if (cardPermissions != null) {
                boolean hasUsage = hasUsageStatsPermission();
                boolean hasWrite = Settings.System.canWrite(this);
                boolean hasBattery = isIgnoringBatteryOptimizations();

                if (hasUsage && hasWrite && hasBattery) {
                    cardPermissions.setVisibility(View.GONE);
                } else {
                    cardPermissions.setVisibility(View.VISIBLE);
                    View btnUsage = findViewById(R.id.btnGrantUsage);
                    View btnWrite = findViewById(R.id.btnGrantWrite);
                    View btnBattery = findViewById(R.id.btnGrantBattery);
                    if (btnUsage != null) btnUsage.setVisibility(hasUsage ? View.GONE : View.VISIBLE);
                    if (btnWrite != null) btnWrite.setVisibility(hasWrite ? View.GONE : View.VISIBLE);
                    if (btnBattery != null) btnBattery.setVisibility(hasBattery ? View.GONE : View.VISIBLE);
                }
            }
        }
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

        int axisTextColor = getColor(R.color.chart_axis_text);
        int gridColor = getColor(R.color.chart_grid);
        int legendTextColor = getColor(R.color.chart_legend_text);

        // X-Axis
        XAxis xAxis = batteryChart.getXAxis();
        xAxis.setTextColor(axisTextColor);
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

        // Y-Axis
        YAxis leftAxis = batteryChart.getAxisLeft();
        leftAxis.setTextColor(axisTextColor);
        leftAxis.setAxisMaximum(100f);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setDrawGridLines(true);
        leftAxis.setGridColor(gridColor);
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

        // Legend
        Legend legend = batteryChart.getLegend();
        legend.setTextColor(legendTextColor);
        legend.setTextSize(11f);
        legend.setForm(Legend.LegendForm.CIRCLE);
        legend.setFormSize(8f);
        legend.setXEntrySpace(16f);
        legend.setYOffset(8f);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);

        batteryChart.animateX(800);
    }

    // ==================== DASHBOARD REFRESH ====================

    private void refreshDashboard() {
        android.content.SharedPreferences prefs = getSharedPreferences("ZoronSettings", MODE_PRIVATE);
        boolean isRoot = prefs.getBoolean("is_root", false);

        new Thread(() -> {
            String profile = "";
            String gov = "";
            String logs = "";
            String csvData = "";
            String powerState = "UNKNOWN";
            String processReport = "";

            if (isRoot) {
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

                profile = parts[0].trim();
                gov = parts[1].trim();
                logs = parts[2].trim();
                csvData = parts[3].trim();
                powerState = parts[4].trim();
                processReport = parts[5].trim();
            } else {
                try {
                    File zoronDir = new File(getFilesDir(), "zoron");
                    if (!zoronDir.exists()) zoronDir.mkdirs();

                    File profileFile = new File(zoronDir, "profile.txt");
                    profile = profileFile.exists() ? readLocalFile(profileFile).trim() : "balanced";

                    gov = "N/A (Non-Root Engine)";

                    File logFile = new File(zoronDir, "log.txt");
                    logs = logFile.exists() ? readLocalFile(logFile) : "No logs yet.";

                    File csvFile = new File(zoronDir, "battery.csv");
                    csvData = csvFile.exists() ? readLocalFile(csvFile) : "";

                    File powerFile = new File(zoronDir, "power_state.txt");
                    powerState = powerFile.exists() ? readLocalFile(powerFile).trim() : "UNKNOWN";

                    File procFile = new File(zoronDir, "process_report.txt");
                    processReport = procFile.exists() ? readLocalFile(procFile) : "No process data yet.";
                } catch (Exception e) {
                    logs = "Error reading files: " + e.getMessage();
                }
            }

            final String finalProfile = profile;
            final String finalGov = gov;
            final String finalLogs = logs;
            final String finalCsvData = csvData;
            final String finalPowerState = powerState;
            final String finalProcessReport = processReport;

            // Update chart only if data changed
            if (!finalCsvData.equals(lastCsvData)) {
                runOnUiThread(() -> plotChart(finalCsvData));
            }

            // Cache for export
            lastCsvData = finalCsvData;
            lastLogData = finalLogs;

            runOnUiThread(() -> {
                // Update profile display
                String displayProfile = finalProfile.isEmpty() ? "Unknown" : finalProfile.toUpperCase();
                tvCurrentProfile.setText("Mode: " + displayProfile);
                tvCpuInfo.setText("CPU Governor: " + (finalGov.isEmpty() ? "Unknown" : finalGov));

                // Update power state with color
                updatePowerState(finalPowerState);

                // Update logs
                tvLogs.setText(finalLogs);

                // Update process report
                tvProcessReport.setText(finalProcessReport);

                // AMOLED Dark theme enforcement
                boolean isDeepProfile = "deep".equals(finalProfile) || "hibernation".equals(finalProfile) || "nightwatch".equals(finalProfile);
                applyAmoledTheme(isDeepProfile);
            });
        }).start();
    }

    private void applyAmoledTheme(boolean amoledActive) {
        ViewGroup rootLayout = null;
        ViewGroup content = findViewById(android.R.id.content);
        if (content != null && content.getChildCount() > 0) {
            View firstChild = content.getChildAt(0);
            if (firstChild instanceof ViewGroup) {
                rootLayout = (ViewGroup) firstChild;
            }
        }
        if (rootLayout != null) {
            if (amoledActive) {
                rootLayout.setBackgroundColor(Color.BLACK);
            } else {
                rootLayout.setBackground(androidx.core.content.ContextCompat.getDrawable(this, R.drawable.bg_gradient_main));
            }
        }
    }

    // ==================== POWER STATE DISPLAY ====================

    private void updatePowerState(String state) {
        String displayState;
        int dotColor;

        if (state.startsWith("HYPER_ACTIVE")) {
            displayState = "⚡ HYPER ACTIVE";
            dotColor = getColor(R.color.state_hyper);
        } else if (state.startsWith("INTERACTIVE")) {
            displayState = "✋ INTERACTIVE";
            dotColor = getColor(R.color.state_interactive);
        } else if (state.startsWith("LIGHT_IDLE")) {
            displayState = "💤 LIGHT IDLE";
            dotColor = getColor(R.color.state_light_idle);
        } else if (state.startsWith("DEEP_IDLE")) {
            displayState = "🔒 DEEP IDLE";
            dotColor = getColor(R.color.state_deep_idle);
        } else if (state.startsWith("SLEEP_IDLE")) {
            displayState = "🌙 SLEEP IDLE";
            dotColor = getColor(R.color.state_sleep);
        } else {
            displayState = "📡 DETECTING...";
            dotColor = getColor(R.color.state_unknown);
        }

        tvPowerState.setText(displayState);

        // Update dot color
        GradientDrawable dot = (GradientDrawable) powerStateDot.getBackground();
        dot.setColor(dotColor);
    }

    private void startPulseAnimation() {
        // Enhanced pulse: combined alpha + scale animation
        ObjectAnimator alpha = ObjectAnimator.ofFloat(powerStateDot, "alpha", 1.0f, 0.4f);
        alpha.setDuration(1200);
        alpha.setRepeatMode(ObjectAnimator.REVERSE);
        alpha.setRepeatCount(ObjectAnimator.INFINITE);

        ObjectAnimator scaleX = ObjectAnimator.ofFloat(powerStateDot, "scaleX", 1.0f, 1.15f);
        scaleX.setDuration(1200);
        scaleX.setRepeatMode(ObjectAnimator.REVERSE);
        scaleX.setRepeatCount(ObjectAnimator.INFINITE);

        ObjectAnimator scaleY = ObjectAnimator.ofFloat(powerStateDot, "scaleY", 1.0f, 1.15f);
        scaleY.setDuration(1200);
        scaleY.setRepeatMode(ObjectAnimator.REVERSE);
        scaleY.setRepeatCount(ObjectAnimator.INFINITE);

        AnimatorSet pulseSet = new AnimatorSet();
        pulseSet.playTogether(alpha, scaleX, scaleY);
        pulseSet.start();
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

        // Purple-themed chart color palette
        int[][] colorPalettes = {
            {getColor(R.color.chart_line_1), getColor(R.color.chart_fill_1)},
            {getColor(R.color.chart_line_2), getColor(R.color.chart_fill_2)},
            {getColor(R.color.chart_line_3), getColor(R.color.chart_fill_3)},
            {getColor(R.color.chart_line_4), getColor(R.color.chart_fill_4)},
            {getColor(R.color.chart_line_5), getColor(R.color.chart_fill_5)},
            {getColor(R.color.chart_line_6), getColor(R.color.chart_fill_6)},
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
        if (legacyExpanded) {
            legacyContainer.setVisibility(View.VISIBLE);
            Animation expandAnim = AnimationUtils.loadAnimation(this, R.anim.expand_section);
            legacyContainer.startAnimation(expandAnim);
            if (ivLegacyToggle != null) {
                Animation rotateAnim = AnimationUtils.loadAnimation(this, R.anim.rotate_arrow_expand);
                ivLegacyToggle.startAnimation(rotateAnim);
            }
        } else {
            Animation collapseAnim = AnimationUtils.loadAnimation(this, R.anim.collapse_section);
            collapseAnim.setAnimationListener(new Animation.AnimationListener() {
                @Override public void onAnimationStart(Animation a) {}
                @Override public void onAnimationRepeat(Animation a) {}
                @Override public void onAnimationEnd(Animation a) {
                    legacyContainer.setVisibility(View.GONE);
                }
            });
            legacyContainer.startAnimation(collapseAnim);
            if (ivLegacyToggle != null) {
                Animation rotateAnim = AnimationUtils.loadAnimation(this, R.anim.rotate_arrow_collapse);
                ivLegacyToggle.startAnimation(rotateAnim);
            }
        }
    }

    // ==================== EXPORT FUNCTIONS ====================

    private void exportCsv() {
        new Thread(() -> {
            String csvData = getFileContent("/data/local/tmp/zoron/battery.csv", "");
            if (csvData.trim().isEmpty()) {
                runOnUiThread(() -> Toast.makeText(this, "No CSV data to export", Toast.LENGTH_SHORT).show());
                return;
            }

            StringBuilder csvContent = new StringBuilder();
            csvContent.append("Timestamp,Battery Level,Profile,Power State,CPU Freq,Temperature\n");
            csvContent.append(csvData);

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
            String logData = getFileContent("/data/local/tmp/zoron/log.txt", "");
            if (logData.trim().isEmpty()) {
                runOnUiThread(() -> Toast.makeText(this, "No logs to export", Toast.LENGTH_SHORT).show());
                return;
            }

            try {
                File exportFile = new File(getExternalCacheDir(), "zoron_diagnostics.txt");
                FileWriter writer = new FileWriter(exportFile);
                writer.write(logData);
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
        android.content.SharedPreferences prefs = getSharedPreferences("ZoronSettings", MODE_PRIVATE);
        boolean isRoot = prefs.getBoolean("is_root", false);
        if (!isRoot) {
            applyNonRootZoronModeInActivity(mode);
            return;
        }

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
                    if (isDestroyed()) return;
                    transitionProgressBar.setProgress(20);
                    tvTransitionDetail.setText("Fastpath: CPU governor + frequencies");
                });

                Shell.cmd("sh /system/bin/zoron_fastpath.sh set_mode " + mode + " || sh /data/adb/modules/zoron_x_optimizer/system/bin/zoron_fastpath.sh set_mode " + mode).exec();

                runOnUiThread(() -> {
                    if (isDestroyed()) return;
                    transitionProgressBar.setProgress(40);
                    tvCurrentProfile.setText("Mode: FASTPATH ACTIVE");
                    tvTransitionStatus.setText("⚡ Fastpath applied");
                    tvTransitionDetail.setText("Running zoron_engine " + mode);
                });
            } else {
                runOnUiThread(() -> {
                    if (isDestroyed()) return;
                    transitionProgressBar.setProgress(40);
                    tvTransitionStatus.setText("Fastpath disabled — running engine directly");
                    tvTransitionDetail.setText("Running zoron_engine " + mode);
                });
            }

            // 2. Heavy Engine processing
            runOnUiThread(() -> {
                if (isDestroyed()) return;
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
                if (isDestroyed()) return;
                if (result.isSuccess()) {
                    transitionProgressBar.setProgress(100);
                    tvTransitionStatus.setText("✅ " + mode.toUpperCase() + " complete");
                    tvTransitionDetail.setText("All optimizations applied successfully");
                    tvCurrentProfile.setText("Mode: " + mode.toUpperCase());
                    // Auto-hide after 3 seconds
                    handler.postDelayed(() -> {
                        if (!isDestroyed()) cardTransitionProgress.setVisibility(View.GONE);
                    }, 3000);
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
        android.content.SharedPreferences prefs = getSharedPreferences("ZoronSettings", MODE_PRIVATE);
        boolean isRoot = prefs.getBoolean("is_root", false);
        if (!isRoot) {
            applyLegacyProfileNonRoot(profile);
            return;
        }

        new Thread(() -> {
            // Search paths in priority order
            String scriptCmd = String.join("; ",
                "DEVICE=$(getprop ro.product.device 2>/dev/null)",
                "if [ \"$DEVICE\" = \"whyred\" ] || [ \"$DEVICE\" = \"tulip\" ]; then ",
                "  SCRIPT_NAME=\"whyred_opt\"",
                "else",
                "  SCRIPT_NAME=\"zoron_engine\"",
                "fi",
                "SCRIPT_PATH=\"\"",
                "for p in /data/local/tmp/zoron/$SCRIPT_NAME /system/bin/$SCRIPT_NAME /data/adb/modules/zoron_x_optimizer/system/bin/$SCRIPT_NAME /data/adb/modules/whyred_battery_optimizer/system/bin/$SCRIPT_NAME; do " +
                    "if [ -f \"$p\" ]; then SCRIPT_PATH=\"$p\"; break; fi; done",
                "if [ -z \"$SCRIPT_PATH\" ]; then " +
                    "echo \"ERROR: $SCRIPT_NAME not found.\"; " +
                    "exit 1; fi",
                "sed 's/\\r$//' \"$SCRIPT_PATH\" | sh -s " + profile
            );
            Shell.Result result = Shell.cmd(scriptCmd).exec();
            new Handler(Looper.getMainLooper()).post(() -> {
                if (isDestroyed()) return;
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
            String reportData = getFileContent("/data/local/tmp/zoron/process_report.txt", "");
            if (reportData.trim().isEmpty()) {
                runOnUiThread(() -> Toast.makeText(this, "No process data to export", Toast.LENGTH_SHORT).show());
                return;
            }

            try {
                File exportFile = new File(getExternalCacheDir(), "zoron_process_report.txt");
                FileWriter writer = new FileWriter(exportFile);
                writer.write(reportData);
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

    private boolean hasUsageStatsPermission() {
        AppOpsManager appOps = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, 
            android.os.Process.myUid(), getPackageName());
        if (mode == AppOpsManager.MODE_DEFAULT) {
            return checkCallingOrSelfPermission(android.Manifest.permission.PACKAGE_USAGE_STATS) == PackageManager.PERMISSION_GRANTED;
        }
        return mode == AppOpsManager.MODE_ALLOWED;
    }

    private void requestUsageStatsPermission() {
        new AlertDialog.Builder(this)
            .setTitle("Permission Required")
            .setMessage("Autopilot needs 'Usage Access' permission to detect the current foreground app and adjust performance automatically. Please enable it for ZORON-X in the next screen.")
            .setPositiveButton("Grant", (dialog, which) -> {
                startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    // ==================== CARD PRESS ANIMATION ====================

    private void setupCardPressAnimation(View card) {
        card.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    v.animate().scaleX(0.96f).scaleY(0.96f).setDuration(100).start();
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    v.animate().scaleX(1.0f).scaleY(1.0f)
                        .setDuration(200)
                        .setInterpolator(new OvershootInterpolator(2f))
                        .start();
                    break;
            }
            return false; // Don't consume — let onClick still fire
        });
    }

    // ==================== STAGGERED ENTRY ANIMATION ====================

    private void animateStaggeredEntry() {
        // Find the main scroll content container
        View scrollView = findViewById(android.R.id.content);
        if (scrollView == null) return;

        // Get the NestedScrollView's child (LinearLayout)
        ViewGroup rootContent = (ViewGroup) ((ViewGroup) scrollView).getChildAt(0); // CoordinatorLayout
        if (rootContent == null || rootContent.getChildCount() < 2) return;

        // The NestedScrollView is the second child (after AppBarLayout)
        View nestedScroll = rootContent.getChildAt(1);
        if (!(nestedScroll instanceof ViewGroup)) return;

        ViewGroup scrollContent = (ViewGroup) nestedScroll;
        if (scrollContent.getChildCount() == 0) return;

        // The LinearLayout inside NestedScrollView
        View innerLayout = scrollContent.getChildAt(0);
        if (!(innerLayout instanceof ViewGroup)) return;

        ViewGroup mainLayout = (ViewGroup) innerLayout;
        int childCount = mainLayout.getChildCount();

        for (int i = 0; i < childCount; i++) {
            View child = mainLayout.getChildAt(i);
            child.setAlpha(0f);
            child.setTranslationY(60f);
            child.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(500)
                .setStartDelay(i * 60L)
                .setInterpolator(new OvershootInterpolator(1.0f))
                .start();
        }
    }

    private boolean isIgnoringBatteryOptimizations() {
        android.os.PowerManager pm = (android.os.PowerManager) getSystemService(Context.POWER_SERVICE);
        return pm != null && pm.isIgnoringBatteryOptimizations(getPackageName());
    }

    private void applyNonRootZoronModeInActivity(String mode) {
        cardTransitionProgress.setVisibility(View.VISIBLE);
        transitionProgressBar.setProgress(30);
        tvTransitionStatus.setText("Applying Non-Root Mode...");
        tvTransitionDetail.setText("Configuring System settings: " + mode);
        tvCurrentProfile.setText("Mode: " + mode.toUpperCase());

        new Thread(() -> {
            try {
                File zoronDir = new File(getFilesDir(), "zoron");
                if (!zoronDir.exists()) zoronDir.mkdirs();
                File profileFile = new File(zoronDir, "profile.txt");
                java.io.FileWriter fw = new java.io.FileWriter(profileFile);
                fw.write(mode);
                fw.close();

                File logFile = new File(zoronDir, "log.txt");
                java.io.FileWriter logFw = new java.io.FileWriter(logFile, true);
                String timestamp = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(new java.util.Date());
                logFw.write("[" + timestamp + "] [USER] Changed mode to: " + mode.toUpperCase() + "\n");
                logFw.close();

                Intent serviceIntent = new Intent(MainActivity.this, ZoronAutopilotService.class);
                serviceIntent.putExtra("apply_mode", mode);
                startService(serviceIntent);

                Thread.sleep(800);
            } catch (Exception e) {
                e.printStackTrace();
            }

            runOnUiThread(() -> {
                if (isDestroyed()) return;
                transitionProgressBar.setProgress(100);
                tvTransitionStatus.setText("✅ " + mode.toUpperCase() + " complete");
                tvTransitionDetail.setText("Non-root optimizations applied");
                handler.postDelayed(() -> {
                    if (!isDestroyed()) cardTransitionProgress.setVisibility(View.GONE);
                }, 2000);
                refreshDashboard();
            });
        }).start();
    }

    private void applyLegacyProfileNonRoot(String profile) {
        new Thread(() -> {
            try {
                File zoronDir = new File(getFilesDir(), "zoron");
                if (!zoronDir.exists()) zoronDir.mkdirs();
                File profileFile = new File(zoronDir, "profile.txt");
                java.io.FileWriter fw = new java.io.FileWriter(profileFile);
                fw.write(profile);
                fw.close();

                File logFile = new File(zoronDir, "log.txt");
                java.io.FileWriter logFw = new java.io.FileWriter(logFile, true);
                String timestamp = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(new java.util.Date());
                logFw.write("[" + timestamp + "] [USER] Changed profile to legacy: " + profile.toUpperCase() + "\n");
                logFw.close();

                Intent serviceIntent = new Intent(MainActivity.this, ZoronAutopilotService.class);
                serviceIntent.putExtra("apply_mode", profile);
                startService(serviceIntent);
            } catch (Exception e) {
                e.printStackTrace();
            }

            runOnUiThread(() -> {
                if (isDestroyed()) return;
                Toast.makeText(MainActivity.this, profile.toUpperCase() + " profile applied!", Toast.LENGTH_SHORT).show();
                refreshDashboard();
            });
        }).start();
    }

    private String readLocalFile(File file) {
        StringBuilder sb = new StringBuilder();
        try (java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    private String getFileContent(String fileRootPath, String defaultContent) {
        android.content.SharedPreferences prefs = getSharedPreferences("ZoronSettings", MODE_PRIVATE);
        boolean isRoot = prefs.getBoolean("is_root", false);
        if (isRoot) {
            Shell.Result result = Shell.cmd("cat " + fileRootPath + " 2>/dev/null").exec();
            if (result.isSuccess() && !result.getOut().isEmpty()) {
                StringBuilder sb = new StringBuilder();
                for (String line : result.getOut()) {
                    sb.append(line).append("\n");
                }
                return sb.toString();
            }
            return defaultContent;
        } else {
            String filename = fileRootPath.substring(fileRootPath.lastIndexOf('/') + 1);
            File localFile = new File(new File(getFilesDir(), "zoron"), filename);
            if (localFile.exists()) {
                return readLocalFile(localFile);
            }
            return defaultContent;
        }
    }

    private void showNonRootAdvisoryDialog() {
        android.content.SharedPreferences prefs = getSharedPreferences("ZoronSettings", MODE_PRIVATE);
        boolean showWarning = prefs.getBoolean("show_non_root_warning", true);
        if (!showWarning) return;

        android.widget.ScrollView scrollView = new android.widget.ScrollView(this);
        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        int padding = (int) (16 * getResources().getDisplayMetrics().density);
        layout.setPadding(padding, padding, padding, padding);

        android.widget.TextView tvMessage = new android.widget.TextView(this);
        tvMessage.setText(
            "ZORON-X is designed and optimized primarily for Rooted devices (custom ROMs/kernels) " +
            "where standard OEM power management has been bypassed. Custom setups often disable core parking, " +
            "use aggressive scaling governors, and allow unmanaged background wakelocks, requiring an explicit saver.\n\n" +
            "Stock non-rooted devices already have highly optimized, vendor-specific power systems tuned by manufacturers, " +
            "and do not require third-party savers.\n\n" +
            "We recommend ZORON-X on non-root devices ONLY if your battery has suffered significant degradation/wear " +
            "and cannot be physically replaced. In this case, ZORON-X acts as a supplementary layer (toggling sync, capping " +
            "brightness/timeouts, disabling haptics) to stretch the remaining capacity."
        );
        tvMessage.setTextSize(14f);
        tvMessage.setTextColor(getColor(R.color.purple_on_surface_variant));
        tvMessage.setLineSpacing(4f, 1f);
        layout.addView(tvMessage);

        android.widget.CheckBox cbDontShow = new android.widget.CheckBox(this);
        cbDontShow.setText("Don't show this advisory again");
        cbDontShow.setTextColor(getColor(R.color.purple_on_surface));
        cbDontShow.setTextSize(14f);
        android.widget.LinearLayout.LayoutParams cbParams = new android.widget.LinearLayout.LayoutParams(
            android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
            android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cbParams.setMargins(0, (int) (16 * getResources().getDisplayMetrics().density), 0, 0);
        cbDontShow.setLayoutParams(cbParams);
        
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            cbDontShow.setButtonTintList(android.content.res.ColorStateList.valueOf(getColor(R.color.purple_primary)));
        }
        layout.addView(cbDontShow);

        scrollView.addView(layout);

        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("⚠️ Non-Root Advisory")
            .setView(scrollView)
            .setPositiveButton("I Understand", (dialog, which) -> {
                if (cbDontShow.isChecked()) {
                    prefs.edit().putBoolean("show_non_root_warning", false).apply();
                }
            })
            .setNeutralButton("Learn More", (dialog, which) -> {
                try {
                    startActivity(new android.content.Intent(this, ModeLearnActivity.class));
                } catch (Exception e) {
                    android.widget.Toast.makeText(this, "Unable to open documentation.", android.widget.Toast.LENGTH_SHORT).show();
                }
            })
            .setCancelable(false)
            .show();
    }
}

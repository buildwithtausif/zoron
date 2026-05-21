package com.zoron.whyred;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.topjohnwu.superuser.Shell;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private TextView tvLogs, tvCurrentProfile, tvCpuInfo;
    private LineChart batteryChart;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable updateRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);

        tvLogs = findViewById(R.id.tvLogs);
        tvCurrentProfile = findViewById(R.id.tvCurrentProfile);
        tvCpuInfo = findViewById(R.id.tvCpuInfo);
        batteryChart = findViewById(R.id.batteryChart);

        setupChart();

        findViewById(R.id.cardNone).setOnClickListener(v -> applyProfile("none"));
        findViewById(R.id.cardBattery).setOnClickListener(v -> applyProfile("battery"));
        findViewById(R.id.cardBalanced).setOnClickListener(v -> applyProfile("balanced"));
        findViewById(R.id.cardPerformance).setOnClickListener(v -> applyProfile("performance"));

        // Check for OTA updates automatically on start
        OTAUpdater.checkUpdates(this, false);

        updateRunnable = new Runnable() {
            @Override
            public void run() {
                refreshDashboard();
                handler.postDelayed(this, 10000); // 10 seconds refresh
            }
        };
        handler.post(updateRunnable);
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
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(updateRunnable);
    }

    private void setupChart() {
        batteryChart.getDescription().setEnabled(false);
        batteryChart.setTouchEnabled(true);
        batteryChart.setDragEnabled(true);
        batteryChart.setScaleEnabled(true);
        batteryChart.setPinchZoom(true);
        batteryChart.setBackgroundColor(Color.parseColor("#1E1E1E"));

        XAxis xAxis = batteryChart.getXAxis();
        xAxis.setTextColor(Color.WHITE);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);

        YAxis leftAxis = batteryChart.getAxisLeft();
        leftAxis.setTextColor(Color.WHITE);
        leftAxis.setAxisMaximum(100f);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setDrawGridLines(true);
        leftAxis.setGridColor(Color.parseColor("#333333"));

        batteryChart.getAxisRight().setEnabled(false);
        batteryChart.getLegend().setTextColor(Color.WHITE);
    }

    private void refreshDashboard() {
        new Thread(() -> {
            Shell.Result result = Shell.cmd(
                    "cat /data/local/tmp/zoron/profile.txt 2>/dev/null || echo ''",
                    "echo '---SEP---'",
                    "cat /sys/devices/system/cpu/cpu0/cpufreq/scaling_governor 2>/dev/null || echo ''",
                    "echo '---SEP---'",
                    "cat /data/local/tmp/zoron/log.txt 2>/dev/null || echo 'No logs.'",
                    "echo '---SEP---'",
                    "cat /data/local/tmp/zoron/battery.csv 2>/dev/null || echo ''"
            ).exec();

            if (!result.isSuccess()) {
                runOnUiThread(() -> {
                    tvCurrentProfile.setText("Current Profile: ERROR (Magisk Access Denied)");
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
            if (parts.length < 4) return;

            String profile = parts[0].trim();
            String gov = parts[1].trim();
            String logs = parts[2].trim();
            String csvData = parts[3].trim();

            runOnUiThread(() -> {
                tvCurrentProfile.setText("Current Profile: " + (profile.isEmpty() ? "Unknown" : profile.toUpperCase()));
                tvCpuInfo.setText("CPU Governor: " + (gov.isEmpty() ? "Unknown" : gov));
                tvLogs.setText(logs);
                plotChart(csvData);
            });
        }).start();
    }

    private void plotChart(String csvData) {
        if (csvData == null || csvData.isEmpty()) return;

        Map<String, List<Entry>> profileData = new HashMap<>();
        String[] lines = csvData.split("\n");
        
        long firstTimestamp = -1;

        for (String line : lines) {
            String[] tokens = line.trim().split(",");
            if (tokens.length == 3) {
                try {
                    long ts = Long.parseLong(tokens[0]);
                    float level = Float.parseFloat(tokens[1]);
                    String profile = tokens[2];

                    if (firstTimestamp == -1) firstTimestamp = ts;
                    
                    float xValue = (ts - firstTimestamp) / 60f; // Minutes elapsed

                    if (!profileData.containsKey(profile)) {
                        profileData.put(profile, new ArrayList<>());
                    }
                    profileData.get(profile).add(new Entry(xValue, level));
                } catch (Exception ignored) {}
            }
        }

        LineData lineData = new LineData();
        int[] colors = {Color.parseColor("#00FF7F"), Color.parseColor("#2196F3"), Color.parseColor("#F44336"), Color.parseColor("#607D8B")};
        int colorIdx = 0;

        for (Map.Entry<String, List<Entry>> entry : profileData.entrySet()) {
            LineDataSet set = new LineDataSet(entry.getValue(), entry.getKey().toUpperCase());
            set.setColor(colors[colorIdx % colors.length]);
            set.setCircleColor(colors[colorIdx % colors.length]);
            set.setLineWidth(2f);
            set.setCircleRadius(3f);
            set.setDrawValues(false);
            lineData.addDataSet(set);
            colorIdx++;
        }

        batteryChart.setData(lineData);
        batteryChart.invalidate();
    }

    private void applyProfile(String profile) {
        new Thread(() -> {
            Shell.Result result = Shell.cmd("sh /system/bin/whyred_opt " + profile).exec();
            new Handler(Looper.getMainLooper()).post(() -> {
                if (result.isSuccess()) {
                    Toast.makeText(MainActivity.this, profile.toUpperCase() + " profile applied!", Toast.LENGTH_SHORT).show();
                    refreshDashboard();
                } else {
                    StringBuilder err = new StringBuilder("Error:\n");
                    for (String e : result.getOut()) err.append(e).append("\n");
                    for (String e : result.getErr()) err.append(e).append("\n");
                    Toast.makeText(MainActivity.this, err.toString(), Toast.LENGTH_LONG).show();
                }
            });
        }).start();
    }
}

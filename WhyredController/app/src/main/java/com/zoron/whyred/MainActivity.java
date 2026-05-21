package com.zoron.whyred;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

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

        tvLogs = findViewById(R.id.tvLogs);
        tvCurrentProfile = findViewById(R.id.tvCurrentProfile);
        tvCpuInfo = findViewById(R.id.tvCpuInfo);
        batteryChart = findViewById(R.id.batteryChart);

        setupChart();

        findViewById(R.id.btnNone).setOnClickListener(v -> applyProfile("none"));
        findViewById(R.id.btnBattery).setOnClickListener(v -> applyProfile("battery"));
        findViewById(R.id.btnBalanced).setOnClickListener(v -> applyProfile("balanced"));
        findViewById(R.id.btnPerformance).setOnClickListener(v -> applyProfile("performance"));

        // Check for OTA updates
        OTAUpdater.checkUpdates(this);

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
            // Using libsu for persistent shell (no toast spam!)
            Shell.Result result = Shell.cmd(
                    "cat /data/adb/modules/whyred_battery_optimizer/profile.txt",
                    "echo '---SEP---'",
                    "cat /sys/devices/system/cpu/cpu0/cpufreq/scaling_governor",
                    "echo '---SEP---'",
                    "cat /data/local/tmp/zoron_logs/log.txt 2>/dev/null || echo 'No logs.'",
                    "echo '---SEP---'",
                    "cat /data/local/tmp/zoron_logs/battery.csv 2>/dev/null || echo ''"
            ).exec();

            if (!result.isSuccess()) return;

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
        Toast.makeText(this, "Applying " + profile + "...", Toast.LENGTH_SHORT).show();
        new Thread(() -> {
            Shell.cmd("/system/bin/whyred_opt " + profile).exec();
            runOnUiThread(() -> {
                Toast.makeText(MainActivity.this, "Profile applied!", Toast.LENGTH_SHORT).show();
                refreshDashboard();
            });
        }).start();
    }
}

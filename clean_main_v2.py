import os

with open(r'd:\Projects\zoron\MainActivity_backup.java', 'r', encoding='utf-8') as f:
    lines = f.readlines()

out = []
skip_mode = False
skip_brace_level = 0
for line in lines:
    stripped = line.strip()
    
    # Skip UI declarations
    if any(x in stripped for x in ['private TextView tvLogs', 'private View powerStateDot', 'private LineChart batteryChart', 'private LinearLayout legacyContainer', 'private TextView tvLegacyToggle', 'private ImageView ivLegacyToggle', 'private boolean legacyExpanded', 'private MaterialCardView cardTransitionProgress', 'private TextView tvTransitionStatus', 'private LinearProgressIndicator transitionProgressBar', 'private LinearLayout recommendationsContainer', 'private TextView tvBatteryHealthScore', 'import com.github.mikephil']):
        continue
        
    # Skip entire methods
    if stripped.startswith('private void setupChart()'):
        skip_mode = True
        skip_brace_level = 0
        
    if stripped.startswith('private void plotChart('):
        skip_mode = True
        skip_brace_level = 0
        
    if stripped.startswith('private void toggleLegacy('):
        skip_mode = True
        skip_brace_level = 0
        
    if stripped.startswith('private void startPulseAnimation('):
        skip_mode = True
        skip_brace_level = 0
        
    if stripped.startswith('private void setupCardPressAnimation('):
        skip_mode = True
        skip_brace_level = 0
        
    if stripped.startswith('private void animateStaggeredEntry('):
        skip_mode = True
        skip_brace_level = 0
        
    if stripped.startswith('private void showNonRootAdvisoryDialog('):
        skip_mode = True
        skip_brace_level = 0
        
    if skip_mode:
        if '{' in line:
            skip_brace_level += line.count('{')
        if '}' in line:
            skip_brace_level -= line.count('}')
            if skip_brace_level == 0:
                skip_mode = False
        continue

    # Skip ui elements in onCreate
    if stripped.startswith('tvLogs =') or stripped.startswith('tvCurrentProfile =') or stripped.startswith('tvCpuInfo =') or stripped.startswith('tvPowerState =') or stripped.startswith('tvProcessReport =') or stripped.startswith('powerStateDot =') or stripped.startswith('batteryChart ='):
        continue
    if stripped.startswith('legacyContainer =') or stripped.startswith('tvLegacyToggle =') or stripped.startswith('ivLegacyToggle =') or stripped.startswith('findViewById(R.id.legacyHeader)'):
        continue
    if stripped.startswith('recommendationsContainer =') or stripped.startswith('recommendationsList =') or stripped.startswith('rulesListContainer =') or stripped.startswith('tvBatteryHealthScore =') or stripped.startswith('tvBatteryCycles ='):
        continue
    if stripped.startswith('View btnAddRule ='):
        continue
    if 'findViewById(R.id.btnAddRule)' in line or 'startActivity(new Intent(this, RuleEditorActivity.class));' in line:
        continue
    if 'setupCardPressAnimation' in line:
        continue
    if 'setupChart();' in line:
        continue
    if 'findViewById(R.id.cardZoron' in line or 'findViewById(R.id.cardNone' in line or 'findViewById(R.id.cardBattery' in line or 'findViewById(R.id.cardBalanced' in line or 'findViewById(R.id.cardPerformance' in line:
        continue
    if 'findViewById(R.id.btnExport' in line:
        continue
    if 'cardTransitionProgress =' in line or 'tvTransitionStatus =' in line or 'tvTransitionDetail =' in line or 'transitionProgressBar =' in line or 'findViewById(R.id.btnHideProgress' in line:
        continue
    if 'findViewById(R.id.btnGrant' in line:
        continue
    if 'showNonRootAdvisoryDialog()' in line:
        continue
    if 'View devAttribution =' in line or 'devAttribution.setOnClickListener' in line or 'https://github.com/buildwithtausif/zoron' in line:
        continue
    if 'com.google.android.material.materialswitch.MaterialSwitch switchAutopilot =' in line or 'switchAutopilot.setChecked' in line or 'switchAutopilot.setOnCheckedChangeListener' in line:
        continue
    if 'startPulseAnimation();' in line or 'animateStaggeredEntry();' in line:
        continue
    if 'Toolbar toolbar =' in line or 'setSupportActionBar(toolbar);' in line:
        continue
    
    # Remove UI calls from refreshDashboard
    if 'tvCurrentProfile.setText' in line or 'tvLogs.setText' in line or 'tvCpuInfo.setText' in line or 'tvPowerState.setText' in line or 'tvProcessReport.setText' in line:
        continue
    if 'updatePowerState(' in line or 'plotChart(' in line:
        continue
    if 'MaterialCardView cardPermissions =' in line or 'cardPermissions.setVisibility' in line or 'View btnUsage =' in line or 'View btnWrite =' in line or 'View btnBattery =' in line or 'btnUsage.setVisibility' in line or 'btnWrite.setVisibility' in line or 'btnBattery.setVisibility' in line:
        continue
    if 'GradientDrawable dot = (GradientDrawable)' in line or 'dot.setColor(dotColor);' in line:
        continue

    out.append(line)

with open(r'd:\Projects\zoron\src\WhyredController\app\src\main\java\com\zoron\whyred\MainActivity.java', 'w', encoding='utf-8') as f:
    f.writelines(out)

print("Done")

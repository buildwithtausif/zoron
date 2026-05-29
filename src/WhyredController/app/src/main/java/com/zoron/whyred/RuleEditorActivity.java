package com.zoron.whyred;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.zoron.whyred.data.RuleEntity;
import com.zoron.whyred.data.ZoronDatabase;
import java.util.UUID;

public class RuleEditorActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rule_editor);

        Spinner spinnerCondition = findViewById(R.id.spinnerConditionType);
        Spinner spinnerAction = findViewById(R.id.spinnerActionType);
        EditText etCondVal = findViewById(R.id.etConditionValue);
        EditText etActionVal = findViewById(R.id.etActionValue);
        Button btnSave = findViewById(R.id.btnSaveRule);

        String[] conds = {"BATTERY_BELOW", "CHARGING", "APP_FOREGROUND"};
        spinnerCondition.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, conds));

        String[] actions = {"SET_MODE"};
        spinnerAction.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, actions));

        btnSave.setOnClickListener(v -> {
            String cType = conds[spinnerCondition.getSelectedItemPosition()];
            String aType = actions[spinnerAction.getSelectedItemPosition()];
            String cVal = etCondVal.getText().toString().trim();
            String aVal = etActionVal.getText().toString().trim();

            if (cVal.isEmpty() && !cType.equals("CHARGING")) {
                Toast.makeText(this, "Enter condition value", Toast.LENGTH_SHORT).show();
                return;
            }
            if (aVal.isEmpty()) {
                Toast.makeText(this, "Enter action value", Toast.LENGTH_SHORT).show();
                return;
            }

            RuleEntity rule = new RuleEntity();
            rule.ruleId = UUID.randomUUID().toString();
            rule.conditionType = cType;
            rule.conditionValue = cVal;
            rule.actionType = aType;
            rule.actionValue = aVal;
            rule.priority = 50;
            rule.isEnabled = true;

            new Thread(() -> {
                ZoronDatabase.getDatabase(this).ruleDao().insertRule(rule);
                com.zoron.whyred.engine.RuleEngineManager.exportRulesForDaemon(this);
                runOnUiThread(() -> {
                    Toast.makeText(this, "Rule saved", Toast.LENGTH_SHORT).show();
                    finish();
                });
            }).start();
        });
    }
}

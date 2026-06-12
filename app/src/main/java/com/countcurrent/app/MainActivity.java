package com.countcurrent.app;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private Spinner spinnerMonth;
    private EditText etUnits;
    private SeekBar seekbarUnits;
    private RadioGroup rgRebate;
    
    private CardView cardResults;
    private TextView tvTotalCharges;
    private TextView tvFinalCost;

    private static final String[] MONTHS = {
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Enforce Light Mode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Toolbar setup
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Bind views
        spinnerMonth = findViewById(R.id.spinner_month);
        etUnits = findViewById(R.id.et_units);
        seekbarUnits = findViewById(R.id.seekbar_units);
        rgRebate = findViewById(R.id.rg_rebate);
        cardResults = findViewById(R.id.card_results);
        tvTotalCharges = findViewById(R.id.tv_total_charges);
        tvFinalCost = findViewById(R.id.tv_final_cost);

        MaterialButton btnCalculate = findViewById(R.id.btn_calculate);
        MaterialButton btnViewRecords = findViewById(R.id.btn_view_records);
        MaterialButton btnAbout = findViewById(R.id.btn_about);

        // Initialize Month Spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, MONTHS);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMonth.setAdapter(adapter);

        // Sync SeekBar and EditText
        setupSyncSeekBarAndEditText();

        // Calculate Action
        btnCalculate.setOnClickListener(v -> handleCalculation());

        // View Records Navigation
        btnViewRecords.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, RecordsActivity.class);
            startActivity(intent);
        });

        // About Navigation
        btnAbout.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AboutActivity.class);
            startActivity(intent);
        });
    }

    private void setupSyncSeekBarAndEditText() {
        seekbarUnits.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    etUnits.setText(String.valueOf(progress));
                    etUnits.setError(null);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        etUnits.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    String val = s.toString();
                    if (!val.isEmpty()) {
                        int progress = Integer.parseInt(val);
                        if (progress <= 1000) {
                            seekbarUnits.setProgress(progress);
                        } else {
                            seekbarUnits.setProgress(1000);
                        }
                    } else {
                        seekbarUnits.setProgress(0);
                    }
                } catch (NumberFormatException e) {
                    seekbarUnits.setProgress(0);
                }
            }
        });
    }

    private void handleCalculation() {
        String unitsStr = etUnits.getText().toString().trim();

        // Validation
        if (unitsStr.isEmpty()) {
            etUnits.setError(getString(R.string.error_empty_units));
            etUnits.requestFocus();
            return;
        }

        int units;
        try {
            units = Integer.parseInt(unitsStr);
        } catch (NumberFormatException e) {
            etUnits.setError(getString(R.string.error_invalid_units));
            etUnits.requestFocus();
            return;
        }

        if (units < 1 || units > 1000) {
            etUnits.setError(getString(R.string.error_invalid_units));
            etUnits.requestFocus();
            return;
        }

        // Selected month
        String month = spinnerMonth.getSelectedItem().toString();

        // Selected rebate
        double rebate = getSelectedRebatePercentage();

        // Calculations
        double totalCharges = calculateTotalCharges(units);
        double finalCost = totalCharges - (totalCharges * rebate);

        // Display results
        tvTotalCharges.setText(String.format(Locale.getDefault(), "RM %.2f", totalCharges));
        tvFinalCost.setText(String.format(Locale.getDefault(), "RM %.2f", finalCost));
        cardResults.setVisibility(View.VISIBLE);

        // Save to Firebase Firestore
        ElectricityRecord record = new ElectricityRecord(
                month,
                units,
                totalCharges,
                rebate,
                finalCost,
                System.currentTimeMillis()
        );

        FirestoreManager.getInstance().addRecord(record, task -> {
            if (task.isSuccessful()) {
                // Show Snackbar success message
                Snackbar.make(findViewById(R.id.btn_calculate), R.string.toast_saved, Snackbar.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, R.string.toast_db_error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private double getSelectedRebatePercentage() {
        int checkedId = rgRebate.getCheckedRadioButtonId();
        if (checkedId == R.id.rb_rebate_1) return 0.01;
        if (checkedId == R.id.rb_rebate_2) return 0.02;
        if (checkedId == R.id.rb_rebate_3) return 0.03;
        if (checkedId == R.id.rb_rebate_4) return 0.04;
        if (checkedId == R.id.rb_rebate_5) return 0.05;
        return 0.00;
    }

    // Static calculation utility for TNB tiered rates
    public static double calculateTotalCharges(int units) {
        double total = 0.0;
        if (units <= 200) {
            total = units * 0.218;
        } else if (units <= 300) {
            total = (200 * 0.218) + ((units - 200) * 0.334);
        } else if (units <= 600) {
            total = (200 * 0.218) + (100 * 0.334) + ((units - 300) * 0.516);
        } else {
            total = (200 * 0.218) + (100 * 0.334) + (300 * 0.516) + ((units - 600) * 0.546);
        }
        return total;
    }
}

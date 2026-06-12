package com.countcurrent.app;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;

import java.util.Locale;

public class DetailActivity extends AppCompatActivity {

    private Spinner spinnerMonth;
    private EditText etUnits;
    private SeekBar seekbarUnits;
    private RadioGroup rgRebate;
    private TextView tvTotalCharges;
    private TextView tvFinalCost;

    private LinearLayout layoutButtonsView;
    private LinearLayout layoutButtonsEdit;

    private ElectricityRecord record;
    private boolean isEditMode = false;

    private static final String[] MONTHS = {
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // Retrieve the serializable extra
        record = (ElectricityRecord) getIntent().getSerializableExtra("selected_record");
        if (record == null) {
            Toast.makeText(this, "Error loading record data", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Toolbar setup
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // Bind views
        spinnerMonth = findViewById(R.id.spinner_detail_month);
        etUnits = findViewById(R.id.et_detail_units);
        seekbarUnits = findViewById(R.id.seekbar_detail_units);
        rgRebate = findViewById(R.id.rg_detail_rebate);
        tvTotalCharges = findViewById(R.id.tv_detail_total_charges);
        tvFinalCost = findViewById(R.id.tv_detail_final_cost);

        layoutButtonsView = findViewById(R.id.layout_buttons_view);
        layoutButtonsEdit = findViewById(R.id.layout_buttons_edit);

        MaterialButton btnEdit = findViewById(R.id.btn_detail_edit);
        MaterialButton btnDelete = findViewById(R.id.btn_detail_delete);
        MaterialButton btnSave = findViewById(R.id.btn_detail_save);
        MaterialButton btnCancel = findViewById(R.id.btn_detail_cancel);

        // Initialize Month Spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, MONTHS);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMonth.setAdapter(adapter);

        // Populate details
        populateRecordDetails();

        // Setup real-time calculations and sync
        setupRealTimeListeners();

        // Button Click Listeners
        btnEdit.setOnClickListener(v -> toggleEditMode(true));
        btnCancel.setOnClickListener(v -> toggleEditMode(false));
        btnSave.setOnClickListener(v -> saveUpdatedRecord());
        btnDelete.setOnClickListener(v -> showDeleteConfirmationDialog());
    }

    private void populateRecordDetails() {
        // Set Month spinner selection
        for (int i = 0; i < MONTHS.length; i++) {
            if (MONTHS[i].equalsIgnoreCase(record.getMonth())) {
                spinnerMonth.setSelection(i);
                break;
            }
        }

        // Set Units
        etUnits.setText(String.valueOf(record.getUnits()));
        seekbarUnits.setProgress(record.getUnits());

        // Set Rebate RadioGroup selection
        double rebate = record.getRebate();
        if (rebate == 0.01) rgRebate.check(R.id.rb_detail_rebate_1);
        else if (rebate == 0.02) rgRebate.check(R.id.rb_detail_rebate_2);
        else if (rebate == 0.03) rgRebate.check(R.id.rb_detail_rebate_3);
        else if (rebate == 0.04) rgRebate.check(R.id.rb_detail_rebate_4);
        else if (rebate == 0.05) rgRebate.check(R.id.rb_detail_rebate_5);
        else rgRebate.check(R.id.rb_detail_rebate_0);

        // Set Outputs
        tvTotalCharges.setText(String.format(Locale.getDefault(), "RM %.2f", record.getTotalCharges()));
        tvFinalCost.setText(String.format(Locale.getDefault(), "RM %.2f", record.getFinalCost()));
    }

    private void toggleEditMode(boolean enable) {
        isEditMode = enable;

        // Toggle Fields
        spinnerMonth.setEnabled(enable);
        etUnits.setEnabled(enable);
        seekbarUnits.setEnabled(enable);
        seekbarUnits.setVisibility(enable ? View.VISIBLE : View.GONE);
        
        // Toggle RadioGroup children
        for (int i = 0; i < rgRebate.getChildCount(); i++) {
            rgRebate.getChildAt(i).setEnabled(enable);
        }

        // Toggle Buttons Layouts
        if (enable) {
            layoutButtonsView.setVisibility(View.GONE);
            layoutButtonsEdit.setVisibility(View.VISIBLE);
        } else {
            layoutButtonsView.setVisibility(View.VISIBLE);
            layoutButtonsEdit.setVisibility(View.GONE);
            // Revert changes on cancel
            populateRecordDetails();
            etUnits.setError(null);
        }
    }

    private void setupRealTimeListeners() {
        // SeekBar listener
        seekbarUnits.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && isEditMode) {
                    etUnits.setText(String.valueOf(progress));
                    etUnits.setError(null);
                    recalculateOnEdit();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // EditText listener
        etUnits.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (!isEditMode) return;
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
                recalculateOnEdit();
            }
        });

        // RadioGroup selection listener
        rgRebate.setOnCheckedChangeListener((group, checkedId) -> {
            if (isEditMode) {
                recalculateOnEdit();
            }
        });
    }

    private void recalculateOnEdit() {
        String unitsStr = etUnits.getText().toString().trim();
        if (unitsStr.isEmpty()) return;

        try {
            int units = Integer.parseInt(unitsStr);
            if (units >= 1 && units <= 1000) {
                double rebate = getSelectedRebatePercentage();
                double totalCharges = MainActivity.calculateTotalCharges(units);
                double finalCost = totalCharges - (totalCharges * rebate);

                tvTotalCharges.setText(String.format(Locale.getDefault(), "RM %.2f", totalCharges));
                tvFinalCost.setText(String.format(Locale.getDefault(), "RM %.2f", finalCost));
            }
        } catch (NumberFormatException e) {
            // Ignore format exception in real-time update
        }
    }

    private double getSelectedRebatePercentage() {
        int checkedId = rgRebate.getCheckedRadioButtonId();
        if (checkedId == R.id.rb_detail_rebate_1) return 0.01;
        if (checkedId == R.id.rb_detail_rebate_2) return 0.02;
        if (checkedId == R.id.rb_detail_rebate_3) return 0.03;
        if (checkedId == R.id.rb_detail_rebate_4) return 0.04;
        if (checkedId == R.id.rb_detail_rebate_5) return 0.05;
        return 0.00;
    }

    private void saveUpdatedRecord() {
        String unitsStr = etUnits.getText().toString().trim();

        // Validate
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

        String month = spinnerMonth.getSelectedItem().toString();
        double rebate = getSelectedRebatePercentage();
        double totalCharges = MainActivity.calculateTotalCharges(units);
        double finalCost = totalCharges - (totalCharges * rebate);

        // Update local object properties (excluding id and original timestamp)
        record.setMonth(month);
        record.setUnits(units);
        record.setTotalCharges(totalCharges);
        record.setRebate(rebate);
        record.setFinalCost(finalCost);
        // keep same timestamp so position in history list does not change, or we can update it. Let's keep it.

        FirestoreManager.getInstance().updateRecord(record.getId(), record, task -> {
            if (task.isSuccessful()) {
                Toast.makeText(DetailActivity.this, R.string.toast_updated, Toast.LENGTH_SHORT).show();
                toggleEditMode(false);
            } else {
                Toast.makeText(DetailActivity.this, R.string.toast_db_error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_delete_title)
                .setMessage(R.string.dialog_delete_message)
                .setPositiveButton(R.string.dialog_yes, (dialog, which) -> deleteRecord())
                .setNegativeButton(R.string.dialog_no, null)
                .show();
    }

    private void deleteRecord() {
        FirestoreManager.getInstance().deleteRecord(record.getId(), task -> {
            if (task.isSuccessful()) {
                Toast.makeText(DetailActivity.this, R.string.toast_deleted, Toast.LENGTH_SHORT).show();
                finish(); // Close detail screen and return to history list
            } else {
                Toast.makeText(DetailActivity.this, R.string.toast_db_error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

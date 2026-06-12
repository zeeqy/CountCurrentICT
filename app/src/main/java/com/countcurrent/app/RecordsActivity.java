package com.countcurrent.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class RecordsActivity extends AppCompatActivity {

    private RecyclerView rvRecords;
    private LinearLayout layoutEmpty;
    private final List<ElectricityRecord> recordList = new ArrayList<>();
    private RecordAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_records);

        // Toolbar setup
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // Bind views
        rvRecords = findViewById(R.id.rv_records);
        layoutEmpty = findViewById(R.id.layout_empty);

        // RecyclerView setup
        rvRecords.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RecordAdapter(recordList, record -> {
            Intent intent = new Intent(RecordsActivity.this, DetailActivity.class);
            intent.putExtra("selected_record", record);
            startActivity(intent);
        });
        rvRecords.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadRecords();
    }

    private void loadRecords() {
        FirestoreManager.getInstance().getRecords(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                recordList.clear();
                for (DocumentSnapshot doc : task.getResult().getDocuments()) {
                    ElectricityRecord record = doc.toObject(ElectricityRecord.class);
                    if (record != null) {
                        record.setId(doc.getId()); // Map the document ID
                        recordList.add(record);
                    }
                }
                
                adapter.notifyDataSetChanged();

                if (recordList.isEmpty()) {
                    layoutEmpty.setVisibility(View.VISIBLE);
                    rvRecords.setVisibility(View.GONE);
                } else {
                    layoutEmpty.setVisibility(View.GONE);
                    rvRecords.setVisibility(View.VISIBLE);
                }
            } else {
                Toast.makeText(RecordsActivity.this, R.string.toast_db_error, Toast.LENGTH_SHORT).show();
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

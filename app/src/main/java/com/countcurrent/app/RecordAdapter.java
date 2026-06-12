package com.countcurrent.app;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

public class RecordAdapter extends RecyclerView.Adapter<RecordAdapter.RecordViewHolder> {

    private final List<ElectricityRecord> recordList;
    private final OnRecordClickListener clickListener;

    public interface OnRecordClickListener {
        void onRecordClick(ElectricityRecord record);
    }

    public RecordAdapter(List<ElectricityRecord> recordList, OnRecordClickListener clickListener) {
        this.recordList = recordList;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public RecordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_record, parent, false);
        return new RecordViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecordViewHolder holder, int position) {
        ElectricityRecord record = recordList.get(position);
        holder.bind(record, clickListener);
    }

    @Override
    public int getItemCount() {
        return recordList.size();
    }

    static class RecordViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvMonth;
        private final TextView tvSubtitle;
        private final TextView tvFinalCost;

        public RecordViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMonth = itemView.findViewById(R.id.tv_item_month);
            tvSubtitle = itemView.findViewById(R.id.tv_item_subtitle);
            tvFinalCost = itemView.findViewById(R.id.tv_item_final_cost);
        }

        public void bind(final ElectricityRecord record, final OnRecordClickListener listener) {
            tvMonth.setText(record.getMonth());
            
            // Format rebate as integer percentage (e.g. 0.02 -> 2%)
            int rebatePct = (int) Math.round(record.getRebate() * 100);
            String subtitle = String.format(Locale.getDefault(), "%d kWh (%d%% Rebate)", 
                    record.getUnits(), rebatePct);
            tvSubtitle.setText(subtitle);
            
            tvFinalCost.setText(String.format(Locale.getDefault(), "RM %.2f", record.getFinalCost()));

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRecordClick(record);
                }
            });
        }
    }
}

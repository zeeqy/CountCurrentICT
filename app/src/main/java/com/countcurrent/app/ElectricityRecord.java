package com.countcurrent.app;

import com.google.firebase.firestore.Exclude;

import java.io.Serializable;

public class ElectricityRecord implements Serializable {
    private String id;
    private String month;
    private int units;
    private double totalCharges;
    private double rebate; // decimal format (e.g. 0.02 for 2%)
    private double finalCost;
    private long timestamp;

    // Required empty constructor for Firestore
    public ElectricityRecord() {
    }

    public ElectricityRecord(String month, int units, double totalCharges, double rebate, double finalCost, long timestamp) {
        this.month = month;
        this.units = units;
        this.totalCharges = totalCharges;
        this.rebate = rebate;
        this.finalCost = finalCost;
        this.timestamp = timestamp;
    }

    @Exclude
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public int getUnits() {
        return units;
    }

    public void setUnits(int units) {
        this.units = units;
    }

    public double getTotalCharges() {
        return totalCharges;
    }

    public void setTotalCharges(double totalCharges) {
        this.totalCharges = totalCharges;
    }

    public double getRebate() {
        return rebate;
    }

    public void setRebate(double rebate) {
        this.rebate = rebate;
    }

    public double getFinalCost() {
        return finalCost;
    }

    public void setFinalCost(double finalCost) {
        this.finalCost = finalCost;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}

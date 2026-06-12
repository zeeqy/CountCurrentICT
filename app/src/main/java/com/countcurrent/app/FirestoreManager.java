package com.countcurrent.app;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

public class FirestoreManager {
    private static FirestoreManager instance;
    private final FirebaseFirestore db;
    private final CollectionReference recordsRef;

    @SuppressWarnings("deprecation")
    private FirestoreManager() {
        db = FirebaseFirestore.getInstance();
        
        // Configure Firestore offline persistence explicitly
        try {
            FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                    .setPersistenceEnabled(true)
                    .build();
            db.setFirestoreSettings(settings);
        } catch (Exception e) {
            // Firestore settings can only be set before any other operations
            e.printStackTrace();
        }
        
        recordsRef = db.collection("electricity_records");
    }

    public static synchronized FirestoreManager getInstance() {
        if (instance == null) {
            instance = new FirestoreManager();
        }
        return instance;
    }

    public void addRecord(ElectricityRecord record, OnCompleteListener<DocumentReference> listener) {
        recordsRef.add(record).addOnCompleteListener(listener);
    }

    public void getRecords(OnCompleteListener<QuerySnapshot> listener) {
        // Retrieve records ordered by timestamp descending (newest first)
        recordsRef.orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(listener);
    }

    public void updateRecord(String id, ElectricityRecord record, OnCompleteListener<Void> listener) {
        recordsRef.document(id).set(record).addOnCompleteListener(listener);
    }

    public void deleteRecord(String id, OnCompleteListener<Void> listener) {
        recordsRef.document(id).delete().addOnCompleteListener(listener);
    }
}

package com.example.myrecipebook.activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myrecipebook.R;
import com.example.myrecipebook.adapters.SavedLocationsAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SavedLocationsActivity extends AppCompatActivity {
    
    private RecyclerView locationsRecyclerView;
    private SavedLocationsAdapter adapter;
    private List<Map<String, Object>> savedLocations = new ArrayList<>();
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_locations);

        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        
        locationsRecyclerView = findViewById(R.id.locations_recycler_view);
        locationsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SavedLocationsAdapter(savedLocations);
        locationsRecyclerView.setAdapter(adapter);

        loadSavedLocations();
    }

    private void loadSavedLocations() {
        if (currentUser != null) {
            db.collection("Stores")
              .document(currentUser.getUid())
              .collection("UserStores")
              .orderBy("timestamp", Query.Direction.DESCENDING)
              .get()
              .addOnSuccessListener(queryDocumentSnapshots -> {
                  savedLocations.clear();
                  for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                      savedLocations.add(document.getData());
                  }
                  adapter.notifyDataSetChanged();
              });
        }
    }
}

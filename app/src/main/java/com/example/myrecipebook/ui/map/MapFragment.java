package com.example.myrecipebook.ui.map;

import android.Manifest;
import android.content.Intent;
import com.example.myrecipebook.activities.SavedLocationsActivity;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myrecipebook.R;
import com.example.myrecipebook.activities.MapDetailActivity;
import com.example.myrecipebook.adapters.SavedLocationsAdapter;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener {

    private static final String TAG = "MapFragment";
    private GoogleMap myGoogleMap;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Location currentLocation;
    private Marker tempMarker;
    private LatLng selectedLatLng;

    private EditText editTextStoreName;
    private Button buttonSaveStore;
    private ImageButton buttonPickLocation;
    private RecyclerView historyRecyclerView;
    private SavedLocationsAdapter savedLocationsAdapter;
    private List<Map<String, Object>> savedLocations = new ArrayList<>();

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private CollectionReference storesRef;

    private static final int REQUEST_CODE = 101;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_map, container, false);

        // Initialize UI elements
        editTextStoreName = view.findViewById(R.id.edit_text_store_name);
        buttonSaveStore = view.findViewById(R.id.button_save_store);
        buttonPickLocation = view.findViewById(R.id.button_pick_location);
        historyRecyclerView = view.findViewById(R.id.history_recycler_view);
        
        // Setup RecyclerView
        historyRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        savedLocationsAdapter = new SavedLocationsAdapter(savedLocations);
        historyRecyclerView.setAdapter(savedLocationsAdapter);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            storesRef = db.collection("Stores").document(currentUser.getUid()).collection("UserStores");
        } else {
            Toast.makeText(getContext(), "Please log in to manage stores", Toast.LENGTH_SHORT).show();
        }

        buttonSaveStore.setEnabled(false);
        buttonSaveStore.setOnClickListener(v -> saveStoreLocation());

        Button buttonViewSaved = view.findViewById(R.id.button_view_saved);
        buttonViewSaved.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), SavedLocationsActivity.class);
            startActivity(intent);
        });

        // Set up map toggle button
        buttonPickLocation.setOnClickListener(v -> {
            View mapView = getView().findViewById(R.id.map_activity);
            if (mapView.getVisibility() == View.VISIBLE) {
                mapView.setVisibility(View.GONE);
            } else {
                mapView.setVisibility(View.VISIBLE);
                // Initialize map if not already done
                if (myGoogleMap == null) {
                    SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                        .findFragmentById(R.id.map_activity);
                    if (mapFragment != null) {
                        mapFragment.getMapAsync(this);
                    }
                }
            }
        });

        return view;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        myGoogleMap = googleMap;
        myGoogleMap.setOnMapLongClickListener(this);
        myGoogleMap.getUiSettings().setZoomControlsEnabled(true);
        myGoogleMap.getUiSettings().setMyLocationButtonEnabled(true);

        // Default location (Kathmandu)
        LatLng defaultLocation = new LatLng(27.7172, 85.3240);
        myGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 12));

        if (ActivityCompat.checkSelfPermission(requireContext(), 
            Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            myGoogleMap.setMyLocationEnabled(true);
        }

        loadStoreMarkers();
    }

    @Override
    public void onMapLongClick(@NonNull LatLng latLng) {
        if (tempMarker != null) {
            tempMarker.remove();
        }
        
        selectedLatLng = latLng;
        tempMarker = myGoogleMap.addMarker(new MarkerOptions()
                .position(latLng)
                .title("Store Location")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
        
        buttonSaveStore.setEnabled(true);
    }

    private void saveStoreLocation() {
        String storeName = editTextStoreName.getText().toString().trim();
        
        if (storeName.isEmpty()) {
            Toast.makeText(getContext(), "Please enter a store name", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (selectedLatLng == null || currentUser == null) {
            return;
        }

        Map<String, Object> store = new HashMap<>();
        store.put("name", storeName);
        store.put("location", new GeoPoint(selectedLatLng.latitude, selectedLatLng.longitude));
        store.put("timestamp", System.currentTimeMillis());

        storesRef.add(store)
            .addOnSuccessListener(documentReference -> {
                Toast.makeText(getContext(), "Store saved successfully", Toast.LENGTH_SHORT).show();
                editTextStoreName.setText("");
                buttonSaveStore.setEnabled(false);
                if (tempMarker != null) {
                    tempMarker.remove();
                    tempMarker = null;
                }
                loadStoreMarkers();
            })
            .addOnFailureListener(e -> {
                Toast.makeText(getContext(), "Error saving store", Toast.LENGTH_SHORT).show();
            });
    }

    private void loadStoreMarkers() {
        if (currentUser == null || storesRef == null) return;

        storesRef.orderBy("timestamp", Query.Direction.DESCENDING).get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                savedLocations.clear();
                myGoogleMap.clear();
                LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();

                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    Map<String, Object> locationData = document.getData();
                    savedLocations.add(locationData);

                    GeoPoint geoPoint = (GeoPoint) locationData.get("location");
                    if (geoPoint != null) {
                        LatLng latLng = new LatLng(geoPoint.getLatitude(), geoPoint.getLongitude());
                        myGoogleMap.addMarker(new MarkerOptions()
                            .position(latLng)
                            .title(locationData.get("name").toString())
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                        boundsBuilder.include(latLng);
                    }
                }

                savedLocationsAdapter.notifyDataSetChanged();

                if (!savedLocations.isEmpty()) {
                    try {
                        myGoogleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 100));
                    } catch (Exception e) {
                        Log.e(TAG, "Error calculating bounds", e);
                    }
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error loading stores", e);
            });
    }
}

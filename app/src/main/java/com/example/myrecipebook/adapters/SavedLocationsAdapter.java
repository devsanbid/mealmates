package com.example.myrecipebook.adapters;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myrecipebook.R;
import com.google.firebase.firestore.GeoPoint;

import java.util.List;
import java.util.Map;

public class SavedLocationsAdapter extends RecyclerView.Adapter<SavedLocationsAdapter.ViewHolder> {

    private List<Map<String, Object>> locations;

    public SavedLocationsAdapter(List<Map<String, Object>> locations) {
        this.locations = locations;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_saved_location, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Map<String, Object> location = locations.get(position);
        holder.storeName.setText(location.get("name").toString());
        
        GeoPoint geoPoint = (GeoPoint) location.get("location");
        String coordinates = String.format("%.6f, %.6f", 
            geoPoint.getLatitude(), geoPoint.getLongitude());
        holder.location.setText(coordinates);

        holder.directionsButton.setOnClickListener(v -> {
            String uri = String.format("google.navigation:q=%s,%s", 
                geoPoint.getLatitude(), geoPoint.getLongitude());
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            intent.setPackage("com.google.android.apps.maps");
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return locations.size();
    }

    public void updateData(List<Map<String, Object>> newLocations) {
        locations = newLocations;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView storeName;
        TextView location;
        Button directionsButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            storeName = itemView.findViewById(R.id.text_store_name);
            location = itemView.findViewById(R.id.text_location);
            directionsButton = itemView.findViewById(R.id.button_directions);
        }
    }
}

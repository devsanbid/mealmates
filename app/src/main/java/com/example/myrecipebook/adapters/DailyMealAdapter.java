package com.example.myrecipebook.adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myrecipebook.R;
import com.example.myrecipebook.models.DailyMealItem;
import java.util.ArrayList;
import java.util.List;

public class DailyMealAdapter extends RecyclerView.Adapter<DailyMealAdapter.DailyMealViewHolder> {

    private List<DailyMealItem> mealList;
    private OnItemClickListener listener;

    // Interface for click events
    public interface OnItemClickListener {
        void onItemClick(DailyMealItem item, int position);
    }

    // Constructor
    public DailyMealAdapter() {
        this.mealList = new ArrayList<>();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public DailyMealViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.daily_meal_item, parent, false);
        return new DailyMealViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DailyMealViewHolder holder, int position) {
        DailyMealItem item = mealList.get(position);

        // Bind data to views
        holder.imageView.setImageResource(item.getImageResource());
        holder.titleTextView.setText(item.getTitle());
        holder.descriptionTextView.setText(item.getDescription());
        holder.discountTextView.setText(item.getDiscount());

        // Set click listener
        holder.cardView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(item, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mealList.size();
    }

    // Method to update the list dynamically
    @SuppressLint("NotifyDataSetChanged")
    public void submitList(List<DailyMealItem> newMealList) {
        if (newMealList != null) {
            this.mealList = new ArrayList<>(newMealList);
            notifyDataSetChanged();
        }
    }

    // ViewHolder class
    static class DailyMealViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        ImageView imageView;
        TextView titleTextView;
        TextView descriptionTextView;
        TextView discountTextView;

        public DailyMealViewHolder(@NonNull View itemView) {
            super(itemView);

            // Initialize views with correct IDs
            cardView = itemView.findViewById(R.id.cardView);
            imageView = itemView.findViewById(R.id.imageview);
            titleTextView = itemView.findViewById(R.id.titleTextView);
            descriptionTextView = itemView.findViewById(R.id.descriptionTextView);
            discountTextView = itemView.findViewById(R.id.discountTextView);
        }
    }
}
package com.example.myrecipebook.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton; // Import ImageButton
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myrecipebook.R;
import com.example.myrecipebook.models.PlannedRecipeItem;
import com.squareup.picasso.Picasso;
import java.io.File; // Import File
import java.util.ArrayList;
import java.util.List;
import android.util.Log; // Import Log

public class WeeklyPlanAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private OnRecipeClickListener recipeClickListener;

    private static final int VIEW_TYPE_DAY = 0;
    private static final int VIEW_TYPE_RECIPE = 1;

    private Context context;
    private List<Object> items; // List containing Strings (day) and PlannedRecipeItems
    private OnRemoveItemListener removeListener; // Listener instance

    // Listener interface
    public interface OnRemoveItemListener {
        void onRemoveItemClick(int position);
    }
    
    public interface OnRecipeClickListener {
        void onRecipeClick(String recipeId);
    }

    public WeeklyPlanAdapter(Context context, List<Object> items, OnRemoveItemListener removeListener, OnRecipeClickListener recipeClickListener) {
        this.context = context;
        this.items = items != null ? items : new ArrayList<>();
        this.removeListener = removeListener;
        this.recipeClickListener = recipeClickListener;
    }

    @Override
    public int getItemViewType(int position) {
        if (items.get(position) instanceof String) {
            return VIEW_TYPE_DAY;
        } else if (items.get(position) instanceof PlannedRecipeItem) {
            return VIEW_TYPE_RECIPE;
        }
        return super.getItemViewType(position); // Should not happen
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        if (viewType == VIEW_TYPE_DAY) {
            View view = inflater.inflate(R.layout.item_weekly_plan_day, parent, false); // Layout for Day Header
            return new DayViewHolder(view);
        } else { // VIEW_TYPE_RECIPE
            View view = inflater.inflate(R.layout.item_weekly_plan_recipe, parent, false); // Layout for Recipe Item
            return new RecipeViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        int viewType = getItemViewType(position);
        if (viewType == VIEW_TYPE_DAY) {
            DayViewHolder dayHolder = (DayViewHolder) holder;
            dayHolder.dayTextView.setText((String) items.get(position));
        } else { // VIEW_TYPE_RECIPE
            RecipeViewHolder recipeHolder = (RecipeViewHolder) holder;
            PlannedRecipeItem recipeItem = (PlannedRecipeItem) items.get(position);
            recipeHolder.titleTextView.setText(recipeItem.getTitle());

            // Load image from local file path or show placeholder
            String imagePath = recipeItem.getImageUrl();
            if (imagePath != null && !imagePath.isEmpty()) {
                File imgFile = new File(imagePath);
                 if(imgFile.exists() && imgFile.isFile()){
                    Picasso.get()
                        .load(imgFile) // Load from local File object
                        .placeholder(R.drawable.ic_baseline_image_24)
                        .error(R.drawable.ic_baseline_image_24)
                        .into(recipeHolder.recipeImageView);
                 } else {
                    Log.w("WeeklyPlanAdapter", "Image file not found or invalid at path: " + imagePath);
                    recipeHolder.recipeImageView.setImageResource(R.drawable.ic_baseline_image_24); // Set placeholder
                 }
            } else {
                recipeHolder.recipeImageView.setImageResource(R.drawable.ic_baseline_image_24);
            }

            // Set listener for the remove button
            recipeHolder.removeButton.setOnClickListener(v -> {
                if (removeListener != null) {
                    removeListener.onRemoveItemClick(position);
                }
            });
            // Handle recipe item click
            recipeHolder.itemView.setOnClickListener(v -> {
                if (recipeClickListener != null) {
                    recipeClickListener.onRecipeClick(recipeItem.getRecipeId());
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    // ViewHolder for Day Header
    static class DayViewHolder extends RecyclerView.ViewHolder {
        TextView dayTextView;
        DayViewHolder(@NonNull View itemView) {
            super(itemView);
            dayTextView = itemView.findViewById(R.id.day_header_textview); // ID in item_weekly_plan_day.xml
        }
    }

    // ViewHolder for Recipe Item
    static class RecipeViewHolder extends RecyclerView.ViewHolder {
        ImageView recipeImageView;
        TextView titleTextView;
        ImageButton removeButton; // Add remove button reference

        RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            recipeImageView = itemView.findViewById(R.id.planned_recipe_image);
            titleTextView = itemView.findViewById(R.id.planned_recipe_title);
            removeButton = itemView.findViewById(R.id.btn_remove_from_plan); // Initialize remove button
        }
    }

    // Method to update the list
    public void updateItems(List<Object> newItems) {
        this.items.clear();
        this.items.addAll(newItems);
        notifyDataSetChanged(); // Consider using DiffUtil for better performance
    }
}

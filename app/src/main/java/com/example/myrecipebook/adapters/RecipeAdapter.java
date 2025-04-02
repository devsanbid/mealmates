package com.example.myrecipebook.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.content.Intent;
import android.widget.Toast;
import android.content.Intent;
import com.example.myrecipebook.activities.RecipeDetailActivity;

import androidx.annotation.NonNull;
import com.google.gson.Gson;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myrecipebook.R;
import com.example.myrecipebook.models.Recipe;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;
import com.google.gson.Gson;

import java.io.File; // Import File
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {
    private List<Recipe> recipeList;
    private Context context;
    // Optional: Add listener for delete/purchase if needed later
    // private OnRecipeActionListener actionListener;

    public RecipeAdapter(List<Recipe> recipeList, Context context) {
        this.recipeList = recipeList;
        this.context = context;
        // this.actionListener = listener; // If using listener
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_recipe, parent, false);
        return new RecipeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        Recipe recipe = recipeList.get(position);
        holder.title.setText(recipe.getTitle());
        holder.description.setText(recipe.getDescription());
        holder.duration.setText(recipe.getDuration());
        holder.servings.setText(recipe.getServings());

        // Load image from local file path or show placeholder
        String imagePath = recipe.getImageUrl();
        if (imagePath != null && !imagePath.isEmpty()) {
            File imgFile = new File(imagePath);
            if(imgFile.exists() && imgFile.isFile()){ // Check if it's a file too
                Picasso.get()
                    .load(imgFile) // Load from local File object
                    .placeholder(R.drawable.ic_baseline_image_24) // Placeholder image
                    .error(R.drawable.ic_baseline_image_24) // Error image
                    .into(holder.image);
            } else {
                // Handle cases where path exists but file doesn't, or it's an old URL (optional)
                Log.w("RecipeAdapter", "Image file not found or invalid at path: " + imagePath);
                holder.image.setImageResource(R.drawable.ic_baseline_image_24); // Set placeholder
            }
        } else {
             holder.image.setImageResource(R.drawable.ic_baseline_image_24); // Set placeholder if no path
        }

        // --- Set up button listeners ---

        // Add to Plan button click listener
        holder.btnAddToPlan.setOnClickListener(v -> {
            showDaySelectionDialog(recipe);
        });

        // Optional: Delete button listener
        holder.btnDelete.setOnClickListener(v -> {
            // Implement delete confirmation and logic if needed
            Toast.makeText(context, "Delete clicked (not implemented)", Toast.LENGTH_SHORT).show();
            // if (actionListener != null) {
            //     actionListener.onDeleteRecipe(recipe.getId());
            // }
        });

        // Optional: Purchase button listener (Purpose unclear in this context, maybe favorite?)
        holder.btnPurchase.setOnClickListener(v -> {
            // Implement purchase/favorite logic if needed
            Toast.makeText(context, "Purchase/Favorite clicked (not implemented)", Toast.LENGTH_SHORT).show();
            // if (actionListener != null) {
            //     actionListener.onMarkAsPurchased(recipe.getId());
            // }
        });

        // Handle recipe item click
        holder.rootView.setOnClickListener(v -> {
            Intent intent = new Intent(context, RecipeDetailActivity.class);
            intent.putExtra("recipe", new Gson().toJson(recipe));
            context.startActivity(intent);
        });
    }

    // Add Gson import at top of file if not already present

    @Override
    public int getItemCount() {
        return recipeList.size();
    }

    // Optional: Interface for actions like delete/edit/purchase
    // public interface OnRecipeActionListener {
    //     void onDeleteRecipe(String recipeId);
    //     // Add other actions if needed
    // }

    public static class RecipeViewHolder extends RecyclerView.ViewHolder {
        TextView title, description, duration, servings;
        ImageView image;
        ImageButton btnDelete, btnPurchase;
        Button btnAddToPlan;
        View rootView;

        public RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            rootView = itemView;
            title = itemView.findViewById(R.id.recipe_title);
            description = itemView.findViewById(R.id.recipe_description);
            duration = itemView.findViewById(R.id.recipe_duration);
            servings = itemView.findViewById(R.id.recipe_servings);
            image = itemView.findViewById(R.id.recipe_image);
            btnDelete = itemView.findViewById(R.id.btn_delete);
            btnPurchase = itemView.findViewById(R.id.btn_purchase);
            btnAddToPlan = itemView.findViewById(R.id.btn_add_to_plan);
        }
    }

    // Method to show day selection dialog
    private void showDaySelectionDialog(Recipe recipe) {
        final String[] days = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Add '" + recipe.getTitle() + "' to which day?");
        builder.setItems(days, (dialog, which) -> {
            String selectedDay = days[which];
            addRecipeToWeeklyPlan(recipe, selectedDay);
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    // Method to handle adding the recipe to Firestore
    private void addRecipeToWeeklyPlan(Recipe recipe, String day) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(context, "You must be logged in to add to plan", Toast.LENGTH_SHORT).show();
            return;
        }
        if (recipe.getId() == null || recipe.getId().isEmpty()) {
             Toast.makeText(context, "Cannot add recipe without ID", Toast.LENGTH_SHORT).show();
             Log.e("RecipeAdapter", "Recipe ID is null or empty for title: " + recipe.getTitle());
             return;
        }

        String userId = currentUser.getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> planEntry = new HashMap<>();
        planEntry.put("recipeId", recipe.getId());
        planEntry.put("title", recipe.getTitle());
        planEntry.put("imageUrl", recipe.getImageUrl()); // Store the local path

        db.collection("WeeklyPlans").document(userId)
          .collection(day).document(recipe.getId())
          .set(planEntry)
          .addOnSuccessListener(aVoid -> {
              Log.d("RecipeAdapter", "Recipe added to plan successfully for " + day);
              Toast.makeText(context, "'" + recipe.getTitle() + "' added to " + day + "'s plan", Toast.LENGTH_SHORT).show();
          })
          .addOnFailureListener(e -> {
              Log.w("RecipeAdapter", "Error adding recipe to plan for " + day, e);
              Toast.makeText(context, "Error adding recipe to plan: " + e.getMessage(), Toast.LENGTH_SHORT).show();
          });
    }
}

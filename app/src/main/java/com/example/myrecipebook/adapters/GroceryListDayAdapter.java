package com.example.myrecipebook.adapters;

import android.app.Activity; // Import Activity for runOnUiThread
import android.content.Context;
import android.graphics.Paint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myrecipebook.R;
import com.example.myrecipebook.models.GroceryItem;
import com.example.myrecipebook.models.PlannedRecipeItem; // Use this for recipe title display

import java.util.ArrayList;
import java.util.List;

public class GroceryListDayAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_DAY = 0;
    private static final int VIEW_TYPE_RECIPE = 1;
    private static final int VIEW_TYPE_INGREDIENT = 2;
    private static final String TAG = "GroceryListDayAdapter";

    private List<Object> displayList; // Contains String (Day), PlannedRecipeItem (Recipe), GroceryItem (Ingredient)
    private Context context;
    private OnGroceryItemCheckListener checkListener;

    // Listener interface for checkbox changes
    public interface OnGroceryItemCheckListener {
        void onItemCheckChange(GroceryItem item, boolean isChecked);
    }

    public GroceryListDayAdapter(Context context, List<Object> displayList, OnGroceryItemCheckListener listener) {
        this.context = context;
        this.displayList = displayList != null ? displayList : new ArrayList<>();
        this.checkListener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        if (position < 0 || position >= displayList.size()) {
             Log.e(TAG, "getItemViewType called with invalid position: " + position);
             return -1; // Error case
        }
        Object item = displayList.get(position);
        if (item instanceof String) {
            return VIEW_TYPE_DAY;
        } else if (item instanceof PlannedRecipeItem) {
            return VIEW_TYPE_RECIPE;
        } else if (item instanceof GroceryItem) {
            return VIEW_TYPE_INGREDIENT;
        }
        Log.w(TAG, "Unknown item type at position: " + position + ", item class: " + (item != null ? item.getClass().getName() : "null"));
        return -1; // Indicate error or unknown type
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        Log.d(TAG, "onCreateViewHolder for viewType: " + viewType);
        View view;
        switch (viewType) {
            case VIEW_TYPE_DAY:
                view = inflater.inflate(R.layout.item_grocery_day, parent, false);
                return new DayViewHolder(view);
            case VIEW_TYPE_RECIPE:
                view = inflater.inflate(R.layout.item_grocery_recipe, parent, false);
                return new RecipeViewHolder(view);
            case VIEW_TYPE_INGREDIENT:
                view = inflater.inflate(R.layout.item_grocery_ingredient, parent, false);
                return new IngredientViewHolder(view);
            default:
                Log.e(TAG, "onCreateViewHolder received unknown viewType: " + viewType);
                // Return a default empty view holder to prevent crash
                view = new View(context);
                return new RecyclerView.ViewHolder(view) {};
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
         if (position < 0 || position >= displayList.size()) {
             Log.e(TAG, "onBindViewHolder called with invalid position: " + position);
             return;
         }
        Object item = displayList.get(position);
        int viewType = holder.getItemViewType();
        // Log.d(TAG, "onBindViewHolder for position " + position + ", viewType: " + viewType); // Optional: Verbose log
        try {
            switch (viewType) {
                case VIEW_TYPE_DAY:
                    String day = (String) item;
                    ((DayViewHolder) holder).dayTextView.setText(day);
                    // Log.d(TAG, "  Binding DAY: " + day);
                    break;
                case VIEW_TYPE_RECIPE:
                    PlannedRecipeItem recipeItem = (PlannedRecipeItem) item;
                    ((RecipeViewHolder) holder).recipeTitleTextView.setText(recipeItem.getTitle());
                    // Log.d(TAG, "  Binding RECIPE: " + recipeItem.getTitle());
                    break;
                case VIEW_TYPE_INGREDIENT:
                    IngredientViewHolder ingredientHolder = (IngredientViewHolder) holder;
                    GroceryItem groceryItem = (GroceryItem) item;
                    ingredientHolder.ingredientNameTextView.setText(groceryItem.getName());
                    // Log.d(TAG, "  Binding INGREDIENT: " + groceryItem.getName() + ", Purchased: " + groceryItem.isPurchased());

                    // Set checkbox state without triggering listener
                    ingredientHolder.ingredientCheckBox.setOnCheckedChangeListener(null);
                    ingredientHolder.ingredientCheckBox.setChecked(groceryItem.isPurchased());

                    // Apply strike-through
                    if (groceryItem.isPurchased()) {
                        ingredientHolder.ingredientNameTextView.setPaintFlags(ingredientHolder.ingredientNameTextView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                    } else {
                        ingredientHolder.ingredientNameTextView.setPaintFlags(ingredientHolder.ingredientNameTextView.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                    }

                    // Set listener
                    ingredientHolder.ingredientCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                        if (checkListener != null) {
                            // Update item state locally first for immediate UI feedback
                            groceryItem.setPurchased(isChecked);
                            // Use notifyItemChanged for better performance if possible, but requires stable IDs
                            notifyItemChanged(holder.getAdapterPosition()); // Update view for strike-through
                            // Notify the activity/fragment
                            checkListener.onItemCheckChange(groceryItem, isChecked);
                        }
                    });
                    break;
                default:
                     Log.w(TAG, "onBindViewHolder encountered unknown viewType: " + viewType + " at position " + position);
                     break; // Do nothing for unknown type
            }
        } catch (Exception e) {
             Log.e(TAG, "Error in onBindViewHolder at position " + position, e);
             // Handle error gracefully, maybe display an error state in the ViewHolder
        }
    }

    @Override
    public int getItemCount() {
        int count = displayList != null ? displayList.size() : 0;
        // Log.d(TAG, "getItemCount: " + count); // Optional: Verbose log
        return count;
    }

    public void updateList(List<Object> newList) {
        Log.d(TAG, "updateList called with " + (newList != null ? newList.size() : "null") + " items.");
        
        // Create a new list to avoid concurrency issues
        List<Object> updatedList = new ArrayList<>();
        if (newList != null) {
            updatedList.addAll(newList);
        }

        // Ensure UI update happens on main thread
        if (context instanceof Activity) {
            ((Activity) context).runOnUiThread(() -> {
                displayList.clear();
                displayList.addAll(updatedList);
                Log.d(TAG, "Updating adapter with " + displayList.size() + " items");
                notifyDataSetChanged();
            });
        } else {
            displayList.clear();
            displayList.addAll(updatedList);
            Log.w(TAG, "Context is not Activity, updating adapter with " + displayList.size() + " items");
            notifyDataSetChanged();
        }
    }

    // --- ViewHolders ---
    static class DayViewHolder extends RecyclerView.ViewHolder {
        TextView dayTextView;
        DayViewHolder(@NonNull View itemView) {
            super(itemView);
            dayTextView = itemView.findViewById(R.id.grocery_day_header_textview);
        }
    }

    static class RecipeViewHolder extends RecyclerView.ViewHolder {
        TextView recipeTitleTextView;
        RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            recipeTitleTextView = itemView.findViewById(R.id.grocery_recipe_title_textview);
        }
    }

    static class IngredientViewHolder extends RecyclerView.ViewHolder {
        TextView ingredientNameTextView;
        CheckBox ingredientCheckBox;
        IngredientViewHolder(@NonNull View itemView) {
            super(itemView);
            ingredientNameTextView = itemView.findViewById(R.id.grocery_ingredient_name);
            ingredientCheckBox = itemView.findViewById(R.id.grocery_ingredient_checkbox);
        }
    }
}

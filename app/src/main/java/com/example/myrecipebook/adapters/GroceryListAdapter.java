package com.example.myrecipebook.adapters;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myrecipebook.R;
import com.example.myrecipebook.models.GroceryItem;
import java.util.ArrayList;
import java.util.List;

public class GroceryListAdapter extends RecyclerView.Adapter<GroceryListAdapter.GroceryViewHolder> {

    private List<GroceryItem> groceryList;
    private Context context;
    private OnItemCheckListener checkListener;

    // Listener interface
    public interface OnItemCheckListener {
        void onItemCheckChange(GroceryItem item, boolean isChecked);
    }

    public GroceryListAdapter(List<GroceryItem> groceryList, Context context, OnItemCheckListener listener) {
        this.groceryList = groceryList != null ? groceryList : new ArrayList<>();
        this.context = context;
        this.checkListener = listener;
    }

    @NonNull
    @Override
    public GroceryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_grocery, parent, false); // Need item_grocery.xml layout
        return new GroceryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroceryViewHolder holder, int position) {
        GroceryItem item = groceryList.get(position);
        holder.itemNameTextView.setText(item.getName());

        // Set checkbox state without triggering listener
        holder.itemCheckBox.setOnCheckedChangeListener(null);
        holder.itemCheckBox.setChecked(item.isPurchased());

        // Apply strike-through based on purchased status
        if (item.isPurchased()) {
            holder.itemNameTextView.setPaintFlags(holder.itemNameTextView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            holder.itemNameTextView.setPaintFlags(holder.itemNameTextView.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }

        // Set listener for checkbox changes
        holder.itemCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (checkListener != null) {
                // Update item state locally first for immediate UI feedback (optional)
                item.setPurchased(isChecked);
                notifyItemChanged(holder.getAdapterPosition()); // Update view for strike-through
                // Notify the activity/fragment
                checkListener.onItemCheckChange(item, isChecked);
            }
        });
    }

    @Override
    public int getItemCount() {
        return groceryList.size();
    }

    // Method to update the list
    public void updateList(List<GroceryItem> newList) {
        this.groceryList.clear();
        if (newList != null) {
            this.groceryList.addAll(newList);
        }
        notifyDataSetChanged(); // Consider DiffUtil
    }

    // ViewHolder
    static class GroceryViewHolder extends RecyclerView.ViewHolder {
        TextView itemNameTextView;
        CheckBox itemCheckBox;

        public GroceryViewHolder(@NonNull View itemView) {
            super(itemView);
            itemNameTextView = itemView.findViewById(R.id.grocery_item_name); // ID in item_grocery.xml
            itemCheckBox = itemView.findViewById(R.id.grocery_item_checkbox); // ID in item_grocery.xml
        }
    }
}

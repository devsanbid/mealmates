package com.example.myrecipebook.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myrecipebook.R;
import com.example.myrecipebook.models.GroceryItem; // Reusing GroceryItem model
import java.util.ArrayList;
import java.util.List;

// Simple adapter to just display the names of grocery items
public class DelegationItemAdapter extends RecyclerView.Adapter<DelegationItemAdapter.ViewHolder> {

    private List<GroceryItem> itemList;
    private Context context;

    public DelegationItemAdapter(Context context, List<GroceryItem> itemList) {
        this.context = context;
        this.itemList = itemList != null ? itemList : new ArrayList<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_delegation, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GroceryItem item = itemList.get(position);
        holder.itemNameTextView.setText(item.getName());
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public void updateList(List<GroceryItem> newList) {
        Log.d("DelegationItemAdapter", "Updating list with " + (newList != null ? newList.size() : 0) + " items");
        
        // Create a new list to avoid potential reference issues
        List<GroceryItem> updatedList = new ArrayList<>();
        if (newList != null) {
            updatedList.addAll(newList);
        }
        
        // Update the main list on UI thread
        this.itemList = updatedList;
        Log.d("DelegationItemAdapter", "List now contains " + itemList.size() + " items");
        if (!itemList.isEmpty()) {
            Log.d("DelegationItemAdapter", "First item: " + itemList.get(0).getName());
        }
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView itemNameTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemNameTextView = itemView.findViewById(R.id.delegation_item_name);
        }
    }
}

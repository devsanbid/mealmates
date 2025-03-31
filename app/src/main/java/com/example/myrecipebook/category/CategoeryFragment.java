package com.example.myrecipebook.category;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar; // Import ProgressBar
import android.widget.TextView;
import android.widget.Toast; // Import Toast

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager; // Import LayoutManager
import androidx.recyclerview.widget.RecyclerView; // Import RecyclerView

import com.example.myrecipebook.R;
import com.example.myrecipebook.adapters.RecipeAdapter; // Import RecipeAdapter
import com.example.myrecipebook.models.Recipe; // Import Recipe model
import com.google.firebase.firestore.FirebaseFirestore; // Import Firestore
import com.google.firebase.firestore.Query; // Import Query
import com.google.firebase.firestore.QueryDocumentSnapshot; // Import Firestore
import com.google.firebase.firestore.QuerySnapshot; // Import Firestore

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CategoeryFragment extends Fragment {

    private static final String TAG = "CategoryFragment";
    private String categoryName;
    private TextView categoryTitleTextView;
    private RecyclerView categoryRecyclerView;
    private ProgressBar categoryProgressBar;
    private RecipeAdapter recipeAdapter;
    private List<Recipe> recipeList = new ArrayList<>();
    private FirebaseFirestore db;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance(); // Initialize Firestore
        // Retrieve the category name from arguments
        if (getArguments() != null) {
            categoryName = getArguments().getString("categoryName", "Unknown Category");
        } else {
            categoryName = "Unknown Category";
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_categories, container, false);

        // Initialize UI components
        categoryTitleTextView = view.findViewById(R.id.category_title);
        categoryRecyclerView = view.findViewById(R.id.category_recipes_recycler_view);
        categoryProgressBar = view.findViewById(R.id.category_progress_bar);

        // Set the title
        categoryTitleTextView.setText(categoryName);

        // Setup RecyclerView
        categoryRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recipeAdapter = new RecipeAdapter(recipeList, getContext()); // Use the existing RecipeAdapter
        categoryRecyclerView.setAdapter(recipeAdapter);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // First log all available categories for debugging
        db.collection("Recipes")
            .get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    List<String> categories = new ArrayList<>();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        String cat = document.getString("category");
                        if (cat != null && !categories.contains(cat)) {
                            categories.add(cat);
                        }
                    }
                    Log.d(TAG, "Available categories in Firestore: " + categories);
                } else {
                    Log.w(TAG, "Error getting categories", task.getException());
                }
                // Then load recipes for our category
                loadRecipesByCategory();
            });
    }

    private void loadRecipesByCategory() {
        if (categoryName == null || categoryName.equals("Unknown Category") || categoryName.equals("Default Category")) {
            Toast.makeText(getContext(), "Invalid category specified", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "Loading recipes for category: '" + categoryName + "'");
        showLoading(true);
        recipeList.clear(); // Clear previous list

        // First try querying with category field
        db.collection("Recipes")
          .whereEqualTo("category", categoryName)
          .get()
          .addOnCompleteListener(task -> {
              if (task.isSuccessful()) {
                  showLoading(false);
                  QuerySnapshot querySnapshot = task.getResult();
                  if (querySnapshot != null && !querySnapshot.isEmpty()) {
                      recipeList.clear();
                      for (QueryDocumentSnapshot document : querySnapshot) {
                          try {
                              Recipe recipe = document.toObject(Recipe.class);
                              recipe.setId(document.getId());
                              recipeList.add(recipe);
                          } catch (Exception e) {
                              Log.e(TAG, "Error converting document", e);
                          }
                      }
                      recipeAdapter.notifyDataSetChanged();
                      Log.d(TAG, "Successfully fetched " + recipeList.size() + " recipes for category: " + categoryName);
                  } else {
                      Log.d(TAG, "No recipes found for category: " + categoryName);
                      fetchAllRecipesThenFilter(categoryName); // Fallback if no results
                  }
              } else {
                  Log.w(TAG, "Category query failed, falling back to local filtering", task.getException());
                  fetchAllRecipesThenFilter(categoryName);
              }
          });
    }

    private void fetchAllRecipesThenFilter(String categoryName) {
        db.collection("Recipes")
            .get()
            .addOnCompleteListener(task -> {
                showLoading(false);
                if (task.isSuccessful()) {
                    QuerySnapshot querySnapshot = task.getResult();
                    if (querySnapshot != null && !querySnapshot.isEmpty()) {
                        recipeList.clear();
                        for (QueryDocumentSnapshot document : querySnapshot) {
                            try {
                                Recipe recipe = document.toObject(Recipe.class);
                                recipe.setId(document.getId());
                                // Only add if category matches or is null
                                if (recipe.getCategory() == null || 
                                    recipe.getCategory().equalsIgnoreCase(categoryName)) {
                                    recipeList.add(recipe);
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error converting document", e);
                            }
                        }
                        recipeAdapter.notifyDataSetChanged();
                        Log.d(TAG, "Filtered " + recipeList.size() + " recipes for category: " + categoryName);
                        if (recipeList.isEmpty()) {
                            Toast.makeText(getContext(), "No recipes found", Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    Log.w(TAG, "Error getting all recipes", task.getException());
                    Toast.makeText(getContext(), "Error loading recipes", Toast.LENGTH_SHORT).show();
                }
            });
    }

    private void showLoading(boolean isLoading) {
        if (categoryProgressBar != null) {
            categoryProgressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
        if (categoryRecyclerView != null) {
            categoryRecyclerView.setVisibility(isLoading ? View.GONE : View.VISIBLE);
        }
    }
}

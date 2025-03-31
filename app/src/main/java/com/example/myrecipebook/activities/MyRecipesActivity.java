package com.example.myrecipebook.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myrecipebook.R;
import com.example.myrecipebook.adapters.RecipeAdapter; // Reuse RecipeAdapter
import com.example.myrecipebook.models.Recipe;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MyRecipesActivity extends AppCompatActivity {

    private static final String TAG = "MyRecipesActivity";
    private RecyclerView myRecipesRecyclerView;
    private ProgressBar myRecipesProgressBar;
    private RecipeAdapter recipeAdapter;
    private List<Recipe> myRecipeList = new ArrayList<>();
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate started.");
        try {
            setContentView(R.layout.activity_my_recipes);
            Log.d(TAG, "Layout inflated successfully.");

            myRecipesRecyclerView = findViewById(R.id.my_recipes_recycler_view);
            myRecipesProgressBar = findViewById(R.id.my_recipes_progress_bar);
            Log.d(TAG, "UI elements initialized.");

            db = FirebaseFirestore.getInstance();
            mAuth = FirebaseAuth.getInstance();
            currentUser = mAuth.getCurrentUser();

            // Setup RecyclerView
            myRecipesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            // Initialize adapter with empty list first
            recipeAdapter = new RecipeAdapter(myRecipeList, this);
            myRecipesRecyclerView.setAdapter(recipeAdapter);
            Log.d(TAG, "RecyclerView setup complete.");

            if (currentUser != null) {
                Log.d(TAG, "User logged in. Loading recipes...");
                loadMyRecipes();
            } else {
                Log.w(TAG, "User not logged in.");
                Toast.makeText(this, "Please log in to view your recipes", Toast.LENGTH_SHORT).show();
                finish(); // Close if not logged in
            }
        } catch (Exception e) {
             Log.e(TAG, "Error during onCreate", e);
             Toast.makeText(this, "Error initializing My Recipes screen.", Toast.LENGTH_LONG).show();
             finish(); // Close activity on critical error
        }
    }

    private void loadMyRecipes() {
        if (currentUser == null) {
             Log.w(TAG, "loadMyRecipes called but currentUser is null.");
             return;
        }
        String userId = currentUser.getUid();
        Log.d(TAG, "Loading recipes for userId: " + userId);

        showLoading(true);
        myRecipeList.clear(); // Clear previous results before fetching

        db.collection("Recipes")
                .whereEqualTo("userId", userId) // Filter by the current user's ID
                .orderBy("title", Query.Direction.ASCENDING) // Optional: order results
                .get()
                .addOnCompleteListener(task -> {
                    showLoading(false);
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                            for (QueryDocumentSnapshot document : querySnapshot) {
                                try {
                                    Recipe recipe = document.toObject(Recipe.class);
                                    recipe.setId(document.getId()); // Set Firestore ID
                                    myRecipeList.add(recipe);
                                } catch (Exception e) {
                                    Log.e(TAG, "Error converting document to Recipe object: " + document.getId(), e);
                                }
                            }
                            recipeAdapter.notifyDataSetChanged(); // Update the adapter
                            Log.d(TAG, "Successfully fetched " + myRecipeList.size() + " user recipes.");
                        } else {
                            Log.d(TAG, "No recipes found for this user.");
                            Toast.makeText(MyRecipesActivity.this, "You haven't added any recipes yet.", Toast.LENGTH_SHORT).show();
                            // Ensure list is visually cleared if it becomes empty
                            recipeAdapter.notifyDataSetChanged();
                        }
                    } else {
                        Log.w(TAG, "Error getting user recipes.", task.getException());
                        Toast.makeText(MyRecipesActivity.this, "Error fetching recipes: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void showLoading(boolean isLoading) {
        if (myRecipesProgressBar != null) {
            myRecipesProgressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
        if (myRecipesRecyclerView != null) {
            // Hide recycler when loading, show when not loading (even if empty)
            myRecipesRecyclerView.setVisibility(isLoading ? View.GONE : View.VISIBLE);
        }
    }
}

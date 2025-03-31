package com.example.myrecipebook.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myrecipebook.R;
import com.example.myrecipebook.adapters.RecipeAdapter;
import com.example.myrecipebook.models.Recipe;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity { // Removed 'implements RecipeAdapter.OnRecipeActionListener'

    private RecyclerView recyclerView;
    private RecipeAdapter recipeAdapter;
    private List<Recipe> recipeList;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        recyclerView = findViewById(R.id.recentRecyclerView);
        progressBar = findViewById(R.id.progress_bar);
        recipeList = new ArrayList<>();

        // Set up RecyclerView with a GridLayoutManager (matches spanCount in XML)
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recipeAdapter = new RecipeAdapter(recipeList, this);
        recyclerView.setAdapter(recipeAdapter);

        // Fetch recipes from Firebase
        fetchRecipes();
    }

    private void fetchRecipes() {
        progressBar.setVisibility(View.VISIBLE);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        
        db.collection("Recipes")
            .orderBy("title")
            .get()
            .addOnCompleteListener(task -> {
                progressBar.setVisibility(View.GONE);
                if (task.isSuccessful()) {
                    recipeList.clear();
                    QuerySnapshot querySnapshot = task.getResult();
                    if (querySnapshot != null) {
                        for (QueryDocumentSnapshot document : querySnapshot) {
                            Recipe recipe = document.toObject(Recipe.class);
                            recipe.setId(document.getId());
                            recipeList.add(recipe);
                        }
                        recipeAdapter.notifyDataSetChanged();
                    }
                } else {
                    Toast.makeText(HomeActivity.this, 
                        "Error loading recipes: " + task.getException().getMessage(), 
                        Toast.LENGTH_SHORT).show();
                }
            });
    }

    // Commented out or remove the listener methods as the interface is no longer implemented
    /*
    @Override
    public void onEditRecipe(Recipe recipe) {
        // ...
    }

    @Override
    public void onDeleteRecipe(String recipeId) {
        // ...
    }

    @Override
    public void onMarkAsPurchased(String recipeId) {
        // ...
    }
    */
}

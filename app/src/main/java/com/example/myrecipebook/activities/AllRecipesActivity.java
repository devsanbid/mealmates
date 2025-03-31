package com.example.myrecipebook.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log; // For logging errors
import android.view.View;
import android.widget.ProgressBar; // Add ProgressBar for loading indication
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myrecipebook.R;
import com.example.myrecipebook.adapters.RecipeAdapter;
// import com.example.myrecipebook.RecipeDatabaseHelper; // Remove SQLite Helper
import com.example.myrecipebook.models.Recipe;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore; // Firestore import
import com.google.firebase.firestore.QueryDocumentSnapshot; // Firestore import
import com.google.firebase.firestore.QuerySnapshot; // Firestore import

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AllRecipesActivity extends AppCompatActivity {
    private static final String TAG = "AllRecipesActivity"; // For logging

    private RecyclerView recyclerView;
    private RecipeAdapter adapter;
    private List<Recipe> recipeList = new ArrayList<>();
    // private RecipeDatabaseHelper dbHelper; // Remove SQLite Helper
    private FirebaseFirestore db; // Firestore instance
    private ProgressBar progressBar; // Loading indicator

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_recipes);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        recyclerView = findViewById(R.id.recipesRecyclerView);
        progressBar = findViewById(R.id.progressBar); // Assuming you add a ProgressBar with this ID to your layout
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RecipeAdapter(recipeList, this); // Initialize adapter
        recyclerView.setAdapter(adapter);
        // dbHelper = new RecipeDatabaseHelper(this); // Remove SQLite Helper

        // Floating Action Button to add a new recipe
        FloatingActionButton fabAddRecipe = findViewById(R.id.fab_add_recipe);
        fabAddRecipe.setOnClickListener(v -> {
            Intent intent = new Intent(AllRecipesActivity.this, UploadRecipeActivity.class);
            startActivity(intent);
        });

        addDemoRecipesIfNeeded(); // Add demo recipes if they don't exist
        // fetchRecipesFromFirestore(); // Fetching will happen after demo check completes
    }

    private void addDemoRecipesIfNeeded() {
        // Define a unique identifier for one of the demo recipes to check existence
        String checkRecipeTitle = "Demo Pancakes";

        db.collection("Recipes").whereEqualTo("title", checkRecipeTitle).limit(1).get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    QuerySnapshot snapshot = task.getResult();
                    if (snapshot == null || snapshot.isEmpty()) {
                        // Demo recipe not found, add them
                        Log.d(TAG, "Demo recipes not found. Adding...");
                        addDemoRecipes();
                    } else {
                        // Demo recipes likely exist, proceed to fetch all
                        Log.d(TAG, "Demo recipes already exist.");
                        fetchRecipesFromFirestore();
                    }
                } else {
                    Log.w(TAG, "Error checking for demo recipes.", task.getException());
                    // Proceed to fetch anyway, might fail if rules are wrong
                    fetchRecipesFromFirestore();
                }
            });
    }

    private void addDemoRecipes() {
        List<Recipe> demoRecipes = new ArrayList<>();

        // Demo Recipe 1: Pancakes
        List<String> pancakeIngredients = List.of("1 cup Flour", "1 tsp Baking Powder", "1/2 tsp Salt", "1 tbsp Sugar", "1 Egg", "1 cup Milk", "2 tbsp Melted Butter");
        List<String> pancakeSteps = List.of("Mix dry ingredients.", "Whisk egg, milk, butter.", "Combine wet and dry.", "Cook on griddle.");
        // Corrected constructor call to include userId
        demoRecipes.add(new Recipe(null, "Demo Pancakes", "Classic fluffy pancakes.", "20 minutes", "4 servings", "", false, 20, "Breakfast", "demo_user", pancakeIngredients, pancakeSteps));

        // Demo Recipe 2: Simple Salad
        List<String> saladIngredients = List.of("Lettuce", "Tomato", "Cucumber", "Olive Oil", "Vinegar", "Salt", "Pepper");
        List<String> saladSteps = List.of("Wash and chop vegetables.", "Combine in a bowl.", "Drizzle with oil and vinegar.", "Season with salt and pepper.");
        // Corrected constructor call to include userId
        demoRecipes.add(new Recipe(null, "Demo Simple Salad", "A quick and easy side salad.", "10 minutes", "2 servings", "", false, 10, "Lunch", "demo_user", saladIngredients, saladSteps));

        // Demo Recipe 3: Basic Pasta
        List<String> pastaIngredients = List.of("Pasta", "Tomato Sauce", "Garlic", "Olive Oil", "Salt", "Pepper", "Parmesan Cheese (optional)");
        List<String> pastaSteps = List.of("Boil pasta according to package directions.", "Sauté garlic in olive oil.", "Add tomato sauce, salt, pepper. Simmer.", "Drain pasta.", "Combine pasta and sauce.", "Top with cheese if desired.");
        // Corrected constructor call to include userId
        demoRecipes.add(new Recipe(null, "Demo Basic Pasta", "Simple pasta with tomato sauce.", "25 minutes", "3 servings", "", false, 25, "Dinner", "demo_user", pastaIngredients, pastaSteps));

        // Add each demo recipe to Firestore
        for (Recipe recipe : demoRecipes) {
            db.collection("Recipes").add(recipe)
                .addOnSuccessListener(documentReference -> Log.d(TAG, "Added demo recipe: " + recipe.getTitle()))
                .addOnFailureListener(e -> Log.w(TAG, "Error adding demo recipe: " + recipe.getTitle(), e));
        }

        // After attempting to add demos, fetch all recipes including the new ones
        // Add a small delay or use a completion counter if strict ordering is needed,
        // but for this purpose, fetching immediately is usually fine.
        fetchRecipesFromFirestore();
    }

    private void fetchRecipesFromFirestore() {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE); // Show progress bar
        recyclerView.setVisibility(View.GONE); // Hide recycler view while loading

        db.collection("Recipes") // Reference the "Recipes" collection
                .get() // Get documents once
                .addOnCompleteListener(task -> {
                    if (progressBar != null) progressBar.setVisibility(View.GONE); // Hide progress bar

                    if (task.isSuccessful()) {
                        recipeList.clear(); // Clear previous data
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                            for (QueryDocumentSnapshot document : querySnapshot) {
                                try {
                                    // Convert Firestore document directly to Recipe object
                                    Recipe recipe = document.toObject(Recipe.class);
                                    // Important: Set the document ID from Firestore onto the recipe object
                                    recipe.setId(document.getId()); // Set the Firestore document ID
                                    recipeList.add(recipe);
                                } catch (Exception e) {
                                    Log.e(TAG, "Error converting document to Recipe object: " + document.getId(), e);
                                    // Handle error, maybe skip this document or show a specific message
                                }
                            }
                            adapter.notifyDataSetChanged(); // Notify adapter
                            recyclerView.setVisibility(View.VISIBLE); // Show recycler view
                            Log.d(TAG, "Successfully fetched " + recipeList.size() + " recipes.");
                        } else {
                            // No documents found
                            Toast.makeText(AllRecipesActivity.this, "No recipes found in Firestore", Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "No recipes found in Firestore collection.");
                            recyclerView.setVisibility(View.GONE); // Keep recycler hidden or show an empty state view
                        }
                    } else {
                        // Task failed
                        Log.w(TAG, "Error getting documents.", task.getException());
                        Toast.makeText(AllRecipesActivity.this, "Error fetching recipes: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_LONG).show();
                        recyclerView.setVisibility(View.GONE); // Keep recycler hidden
                    }
                });
    }

    // Optional: Add onResume to refresh data if needed when returning to the activity
    @Override
    protected void onResume() {
        super.onResume();
        // Consider if you need to re-fetch data every time the activity resumes
        // fetchRecipesFromFirestore(); // Uncomment if refresh on resume is desired
    }
}

// IMPORTANT: You should add a ProgressBar to your 'activity_all_recipes.xml' layout.
// Give it the android:id="@+id/progressBar". Place it appropriately (e.g., centered).
// Set its initial visibility to 'gone' or 'invisible' in the XML.
// Make sure the RecyclerView (android:id="@+id/recipesRecyclerView") is also present.

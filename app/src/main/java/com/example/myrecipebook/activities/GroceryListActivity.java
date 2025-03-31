package com.example.myrecipebook.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myrecipebook.R;
import com.example.myrecipebook.adapters.GroceryListDayAdapter; // Import new adapter
import com.example.myrecipebook.models.GroceryItem; // Import model
import com.example.myrecipebook.models.PlannedRecipeItem;
import com.example.myrecipebook.models.Recipe;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch; // Keep WriteBatch if needed later

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap; // Keep HashMap if needed later
import java.util.HashSet;
import java.util.List;
import java.util.Map; // Keep Map if needed later
import java.util.Objects; // Keep Objects for requireNonNull
import java.util.Set;
import java.util.stream.Collectors;
import java.util.LinkedHashMap; // Keep order of days


// Implement the new adapter's listener
public class GroceryListActivity extends AppCompatActivity implements GroceryListDayAdapter.OnGroceryItemCheckListener {

    private static final String TAG = "GroceryListActivity";
    private RecyclerView groceryRecyclerView;
    private GroceryListDayAdapter adapter; // Use the new adapter
    private List<Object> displayList = new ArrayList<>(); // List for the new adapter
    private ProgressBar progressBar;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private CollectionReference groceryStatusRef; // Ref to purchased status

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grocery_list);
        Log.d(TAG, "onCreate started.");


        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        groceryRecyclerView = findViewById(R.id.grocery_recycler_view);
        progressBar = findViewById(R.id.grocery_progress_bar);
        groceryRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        // Initialize the new adapter
        adapter = new GroceryListDayAdapter(this, displayList, this);
        groceryRecyclerView.setAdapter(adapter);
        Log.d(TAG, "UI Initialized with GroceryListDayAdapter.");

        if (currentUser != null) {
            Log.d(TAG, "User logged in: " + currentUser.getUid());
            groceryStatusRef = db.collection("GroceryStatus").document(currentUser.getUid()).collection("items");
            loadGroupedGroceryList(); // Call the loading method
        } else {
            Log.w(TAG, "User not logged in.");
            Toast.makeText(this, "Please log in to view grocery list", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    // Added onResume to refresh list when returning to activity
    @Override
    protected void onResume() {
        super.onResume();
        // Re-load data when activity resumes, in case plan/status changed
        if (currentUser != null) {
             Log.d(TAG, "onResume: Reloading grocery list.");
             loadGroupedGroceryList();
        }
    }


    private void loadGroupedGroceryList() {
        if (currentUser == null) {
            Log.w(TAG, "Current user is null, cannot load grocery list");
            return;
        }
        String userId = currentUser.getUid();
        Log.d(TAG, "loadGroupedGroceryList called for user: " + userId);
        showLoading(true);
        Log.d(TAG, "Starting to load weekly plan for user: " + userId);

        List<String> daysOrder = Arrays.asList("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday");
        Map<String, List<PlannedRecipeItem>> weeklyPlanMap = new LinkedHashMap<>();
        Map<String, Recipe> recipeDetailsMap = new HashMap<>();
        List<Task<?>> allPlanFetchTasks = new ArrayList<>();
        Log.d(TAG, "Initialized data structures for weekly plan");

        // 1. Fetch planned recipes for each day
        Log.d(TAG, "Step 1: Fetching weekly plan days...");
        for (String day : daysOrder) {
            weeklyPlanMap.put(day, new ArrayList<>()); // Initialize day entry
            String collectionPath = "WeeklyPlans/" + userId + "/" + day;
            Log.d(TAG, "Fetching planned recipes from: " + collectionPath);
            Task<QuerySnapshot> dayTask = db.collection("WeeklyPlans").document(userId).collection(day).get();
            allPlanFetchTasks.add(dayTask);
            Log.d(TAG, "Added fetch task for day: " + day);

            dayTask.addOnSuccessListener(daySnapshot -> {
                List<PlannedRecipeItem> recipesForDay = weeklyPlanMap.get(day);
                if (recipesForDay == null) {
                    Log.w(TAG, "Recipes list is null for day: " + day);
                    return;
                }
                Log.d(TAG, "Found " + daySnapshot.size() + " planned recipes for " + day);

                for (QueryDocumentSnapshot plannedDoc : daySnapshot) {
                    try {
                        PlannedRecipeItem plannedItem = plannedDoc.toObject(PlannedRecipeItem.class);
                        // Ensure recipeId is set (using doc ID as fallback)
                        if (plannedItem.getRecipeId() == null || plannedItem.getRecipeId().isEmpty()) {
                            plannedItem.setRecipeId(plannedDoc.getId());
                            Log.d(TAG, "Using document ID as recipe ID: " + plannedDoc.getId());
                        }
                        // Basic validation
                        if (plannedItem.getRecipeId() != null && !plannedItem.getRecipeId().isEmpty() && plannedItem.getTitle() != null) {
                             recipesForDay.add(plannedItem);
                             Log.d(TAG, "  Added planned recipe: " + plannedItem.getTitle() + " (ID: " + plannedItem.getRecipeId() + ") for " + day);
                        } else {
                             Log.w(TAG, "  Skipping invalid planned item in " + day + ": " + plannedDoc.getId() + 
                                 " - RecipeID: " + plannedItem.getRecipeId() + 
                                 ", Title: " + plannedItem.getTitle());
                        }
                    } catch (Exception e) {
                         Log.e(TAG, "Error converting planned item for " + day + ", docId: " + plannedDoc.getId(), e);
                    }
                }
            }).addOnFailureListener(e -> {
                Log.e(TAG, "Error fetching plan for day: " + day, e);
                Toast.makeText(GroceryListActivity.this, "Error loading plan for " + day, Toast.LENGTH_SHORT).show();
            });
        }

        // 2. After fetching all plan days, fetch details for unique recipes
        Tasks.whenAllComplete(allPlanFetchTasks).addOnCompleteListener(planTasksResult -> {
            Log.d(TAG, "Step 2: All plan days fetched. Fetching recipe details...");
            Set<String> recipeIdsToFetch = new HashSet<>();
            for (List<PlannedRecipeItem> dayList : weeklyPlanMap.values()) {
                for (PlannedRecipeItem item : dayList) {
                    recipeIdsToFetch.add(item.getRecipeId()); // Add all valid IDs
                }
            }

            if (recipeIdsToFetch.isEmpty()) {
                Log.d(TAG, "No valid recipe IDs in plan. Displaying empty list.");
                fetchPurchasedStatusAndDisplayGrouped(weeklyPlanMap, recipeDetailsMap, new HashSet<>()); // Proceed to display empty
                return;
            }

            List<Task<DocumentSnapshot>> detailTasks = new ArrayList<>();
            Log.d(TAG, "Fetching details for Recipe IDs: " + recipeIdsToFetch);
            for (String recipeId : recipeIdsToFetch) {
                 detailTasks.add(db.collection("Recipes").document(recipeId).get());
            }

            // 3. After fetching recipe details, aggregate ingredients and fetch purchased status
            // Use whenAllComplete to handle potential failures in fetching individual recipes
            Tasks.whenAllComplete(detailTasks).addOnCompleteListener(recipeDetailTaskResults -> {
                Log.d(TAG, "Step 3: Fetched recipe details results. Aggregating ingredients...");
                Set<String> uniqueIngredients = new HashSet<>();
                // Iterate through the results, checking for success and existence
                for (Task<?> task : recipeDetailTaskResults.getResult()) {
                     if (task.isSuccessful() && task.getResult() instanceof DocumentSnapshot) {
                        DocumentSnapshot recipeDoc = (DocumentSnapshot) task.getResult();
                        if (recipeDoc.exists()) {
                            Recipe recipe = recipeDoc.toObject(Recipe.class);
                            if (recipe != null) {
                                recipeDetailsMap.put(recipeDoc.getId(), recipe); // Store full recipe detail
                                if (recipe.getIngredients() != null) {
                                    List<String> cleaned = recipe.getIngredients().stream()
                                            .map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toList());
                                    uniqueIngredients.addAll(cleaned);
                                    Log.d(TAG, "  Ingredients from " + recipe.getTitle() + ": " + cleaned);
                                } else {
                                    Log.w(TAG, "  Ingredients list is null for recipe: " + recipeDoc.getId());
                                }
                            } else {
                                Log.w(TAG, "  Failed to convert recipe object: " + recipeDoc.getId());
                            }
                        } else {
                            Log.w(TAG, "Recipe document not found: " + recipeDoc.getId());
                        }
                    } else {
                         Log.e(TAG, "Error fetching a recipe detail", task.getException());
                    }
                }
                Log.d(TAG, "Aggregated unique ingredients: " + uniqueIngredients);

                // 4. Fetch purchased status
                fetchPurchasedStatusAndDisplayGrouped(weeklyPlanMap, recipeDetailsMap, uniqueIngredients);

            }); // No need for separate onFailureListener here, handled in whenAllComplete loop

        }).addOnFailureListener(e -> handleLoadError("Error fetching weekly plan days", e));
    }


    private void fetchPurchasedStatusAndDisplayGrouped(
            Map<String, List<PlannedRecipeItem>> weeklyPlanMap,
            Map<String, Recipe> recipeDetailsMap,
            Set<String> uniqueIngredients) {

        Log.d(TAG, "Step 4: Fetching purchased status...");
        if (groceryStatusRef == null) {
            Log.e(TAG, "groceryStatusRef is null");
            processAndDisplayGroupedList(weeklyPlanMap, recipeDetailsMap, new HashSet<>()); // Process without status
            return;
        }

        groceryStatusRef.get().addOnSuccessListener(purchasedSnapshot -> {
            Set<String> purchasedItems = new HashSet<>();
            if (purchasedSnapshot != null) {
                for (QueryDocumentSnapshot doc : purchasedSnapshot) {
                    purchasedItems.add(doc.getId());
                }
            }
            Log.d(TAG, "Fetched purchased status. Purchased: " + purchasedItems);

            // 5. Process and display the final grouped list
            processAndDisplayGroupedList(weeklyPlanMap, recipeDetailsMap, purchasedItems);

        }).addOnFailureListener(e -> {
            Log.e(TAG, "Error fetching purchased status", e);
            // Display list without status info on error
            processAndDisplayGroupedList(weeklyPlanMap, recipeDetailsMap, new HashSet<>());
        });
    }

    private void processAndDisplayGroupedList(
            Map<String, List<PlannedRecipeItem>> weeklyPlanMap,
            Map<String, Recipe> recipeDetailsMap,
            Set<String> purchasedItems) {

        Log.d(TAG, "Step 5: Processing data for display...");
        List<Object> newDisplayList = new ArrayList<>(); // Build new list
        boolean listHasContent = false;

        List<String> daysOrder = Arrays.asList("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday");

        for (String day : daysOrder) {
            List<PlannedRecipeItem> recipesForDay = weeklyPlanMap.get(day);
            if (recipesForDay != null && !recipesForDay.isEmpty()) {
                Log.d(TAG, "Processing day: " + day);
                listHasContent = true;
                newDisplayList.add(day); // Add Day Header

                for (PlannedRecipeItem plannedRecipe : recipesForDay) {
                    Log.d(TAG, "  Processing recipe: " + plannedRecipe.getTitle());
                    newDisplayList.add(plannedRecipe); // Add Recipe Header
                    Recipe fullRecipe = recipeDetailsMap.get(plannedRecipe.getRecipeId());
                    if (fullRecipe != null && fullRecipe.getIngredients() != null) {
                        Log.d(TAG, "    Found " + fullRecipe.getIngredients().size() + " ingredients.");
                        for (String ingredient : fullRecipe.getIngredients()) {
                            String cleanedIngredient = ingredient.trim();
                            if (!cleanedIngredient.isEmpty()) {
                                boolean isPurchased = purchasedItems.contains(cleanedIngredient);
                                newDisplayList.add(new GroceryItem(cleanedIngredient, isPurchased)); // Add Ingredient Item
                                Log.d(TAG, "      Added ingredient: " + cleanedIngredient + " (Purchased: " + isPurchased + ")");
                            }
                        }
                    } else {
                         Log.w(TAG, "    Full recipe details not found or ingredients missing for ID: " + plannedRecipe.getRecipeId());
                    }
                }
            } else {
                 Log.d(TAG, "No recipes found for day: " + day);
            }
        }

        Log.d(TAG, "Final display list size: " + newDisplayList.size());
        updateAdapter(newDisplayList); // Update adapter on UI thread
        showLoading(false);

        if (!listHasContent) {
             Toast.makeText(this, "Grocery list is empty (no recipes planned).", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onItemCheckChange(GroceryItem item, boolean isChecked) {
        if (currentUser == null || groceryStatusRef == null) return;
        String ingredientName = item.getName();
        // Basic validation for ingredient name as document ID
        if (ingredientName == null || ingredientName.isEmpty() || ingredientName.contains("/") || ingredientName.length() > 1500) {
             Log.e(TAG, "Invalid ingredient name used for Firestore document ID: " + ingredientName);
             Toast.makeText(this, "Cannot update status for invalid item name", Toast.LENGTH_SHORT).show();
             // Revert checkbox state visually? Requires getting the adapter position.
             return;
        }

        Log.d(TAG, "Checkbox changed for item: " + ingredientName + ", isChecked: " + isChecked);
        DocumentReference itemStatusRef = groceryStatusRef.document(ingredientName);

        if (isChecked) {
            Map<String, Object> data = new HashMap<>();
            data.put("purchased", true);
            itemStatusRef.set(data)
                .addOnSuccessListener(aVoid -> Log.d(TAG, ingredientName + " marked as purchased."))
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error marking " + ingredientName + " as purchased", e);
                    Toast.makeText(this, "Error updating status", Toast.LENGTH_SHORT).show();
                });
        } else {
            itemStatusRef.delete()
                .addOnSuccessListener(aVoid -> Log.d(TAG, ingredientName + " marked as not purchased."))
                .addOnFailureListener(e -> {
                     Log.w(TAG, "Error marking " + ingredientName + " as not purchased", e);
                     Toast.makeText(this, "Error updating status", Toast.LENGTH_SHORT).show();
                });
        }
    }

    private void updateAdapter(List<Object> items) {
        Log.d(TAG, "Updating adapter with " + items.size() + " items.");
        runOnUiThread(() -> {
            Log.d(TAG, "Executing updateAdapter on UI thread with " + items.size() + " items."); // Add log here
            if (adapter != null) {
                // Update the internal list reference in the adapter as well
                this.displayList.clear();
                this.displayList.addAll(items);
                adapter.updateList(this.displayList); // Pass the updated list
            } else {
                 Log.e(TAG, "Adapter is null during UI thread update!");
            }
            // Use class member recyclerView directly
            if (items.isEmpty()) {
                 Log.d(TAG,"Grocery list is empty or became empty.");
                 if (groceryRecyclerView != null) groceryRecyclerView.setVisibility(View.GONE);
            } else {
                 if (groceryRecyclerView != null) groceryRecyclerView.setVisibility(View.VISIBLE);
            }
        });
    }

    private void showLoading(boolean isLoading) {
         runOnUiThread(() -> { // Ensure UI updates are on the main thread
            if (progressBar != null) {
                progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            }
            // Use class member groceryRecyclerView directly
            if (groceryRecyclerView != null) {
                groceryRecyclerView.setVisibility(isLoading ? View.GONE : View.VISIBLE);
            }
         });
    }

    private void handleLoadError(String message, Exception e) {
         Log.e(TAG, message, e);
         // Use 'this' for context in an Activity
         Toast.makeText(this, message + ": " + (e != null ? e.getMessage() : "Unknown error"), Toast.LENGTH_LONG).show();
         showLoading(false);
         updateAdapter(new ArrayList<>()); // Show empty list on error
    }
}

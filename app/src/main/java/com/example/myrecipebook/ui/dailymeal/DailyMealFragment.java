package com.example.myrecipebook.ui.dailymeal;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.content.Intent;
import com.example.myrecipebook.activities.RecipeDetailActivity;
import com.example.myrecipebook.models.Recipe;
import com.google.gson.Gson;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myrecipebook.R;
import com.example.myrecipebook.adapters.WeeklyPlanAdapter;
import com.example.myrecipebook.models.PlannedRecipeItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration; // Keep import if listener logic is added later
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class DailyMealFragment extends Fragment implements WeeklyPlanAdapter.OnRemoveItemListener, WeeklyPlanAdapter.OnRecipeClickListener {

    private static final String TAG = "DailyMealFragment";
    private RecyclerView recyclerView;
    private WeeklyPlanAdapter adapter;
    private List<Object> planItems = new ArrayList<>();
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private ProgressBar progressBar;
    // ListenerRegistration is not used in the fetch-on-resume approach
    // private ListenerRegistration weeklyPlanListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.daily_meal_fragment, container, false);
        Log.d(TAG, "onCreateView called");

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        recyclerView = view.findViewById(R.id.recyclerView);
        progressBar = view.findViewById(R.id.progressBar);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Pass context carefully and the listener
        adapter = new WeeklyPlanAdapter(getActivity(), planItems, this, this::onRecipeClick); // Pass both listeners
        recyclerView.setAdapter(adapter);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume called. Fetching weekly plan.");
        // Fetch data every time the fragment becomes active
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            List<String> daysOrder = Arrays.asList("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday");
            fetchWeeklyPlanData(currentUser.getUid(), daysOrder);
        } else {
             Log.w(TAG, "User not logged in, cannot fetch weekly plan.");
             Toast.makeText(getContext(), "Please log in to view weekly plan", Toast.LENGTH_SHORT).show();
             planItems.clear();
             if (adapter != null) {
                 adapter.updateItems(new ArrayList<>()); // Ensure adapter has empty list
             }
             showLoading(false);
        }
    }

    // No need for onStart/onStop if we fetch in onResume and don't use listeners

    private void fetchWeeklyPlanData(String userId, List<String> daysOrder) {
         Log.d(TAG, "Fetching weekly plan for user: " + userId);
         showLoading(true);

         Map<String, List<PlannedRecipeItem>> weeklyPlanMap = new LinkedHashMap<>();
         for(String day : daysOrder) {
             weeklyPlanMap.put(day, new ArrayList<>());
         }
         AtomicInteger daysProcessed = new AtomicInteger(0);

         for (String day : daysOrder) {
             final String currentDay = day; // Use final variable for lambda
             db.collection("WeeklyPlans").document(userId)
               .collection(currentDay) // Use final variable
               .get()
               .addOnCompleteListener(task -> {
                   if (task.isSuccessful()) {
                       QuerySnapshot daySnapshot = task.getResult();
                       if (daySnapshot != null) {
                           List<PlannedRecipeItem> recipesForDay = weeklyPlanMap.get(currentDay); // Use final variable
                           // Should not be null due to LinkedHashMap initialization
                           if (recipesForDay == null) recipesForDay = new ArrayList<>();

                           for (QueryDocumentSnapshot document : daySnapshot) {
                               try {
                                   PlannedRecipeItem item = document.toObject(PlannedRecipeItem.class);
                                   if (item != null) {
                                       Log.d(TAG, "  - Fetched item for " + currentDay + ": " + item.getTitle() + " (ID: " + document.getId() + ")");
                                       recipesForDay.add(item);
                                   } else {
                                       Log.w(TAG, "  - Failed to convert document to PlannedRecipeItem for " + currentDay + ", docId: " + document.getId());
                                   }
                               } catch (Exception e) {
                                   Log.e(TAG, "Error converting planned recipe item for day " + currentDay + ", docId: " + document.getId(), e);
                               }
                           }
                           Log.d(TAG, "Fetched " + recipesForDay.size() + " recipes for " + currentDay);
                       } else {
                            Log.d(TAG, "Day snapshot was null for " + currentDay);
                       }
                   } else {
                       Log.w(TAG, "Error getting documents for day: " + currentDay, task.getException());
                   }

                   int processedCount = daysProcessed.incrementAndGet();
                   Log.d(TAG, "Processed day: " + currentDay + " (" + processedCount + "/" + daysOrder.size() + ")");
                   if (processedCount == daysOrder.size()) {
                       Log.d(TAG, "All days processed. Updating UI.");
                       processAndDisplayPlan(weeklyPlanMap);
                   }
               });
         }
     }

    private void processAndDisplayPlan(Map<String, List<PlannedRecipeItem>> weeklyPlanMap) {
         planItems.clear();
         boolean planIsEmpty = true;

         for (Map.Entry<String, List<PlannedRecipeItem>> entry : weeklyPlanMap.entrySet()) {
             String day = entry.getKey();
             List<PlannedRecipeItem> recipes = entry.getValue();

             if (!recipes.isEmpty()) {
                 planIsEmpty = false;
                 planItems.add(day); // Add day header
                 planItems.addAll(recipes); // Add recipes for that day
             }
         }

         showLoading(false);

         // Use a final variable for the lambda
         final boolean isPlanEmptyFinal = planIsEmpty;
         // Create a final copy of the list for the lambda
         final List<Object> finalPlanItems = new ArrayList<>(planItems);

         // Ensure adapter update happens on the UI thread and context is valid
         if (getActivity() != null && adapter != null) {
             getActivity().runOnUiThread(() -> {
                 Log.d(TAG, "Updating adapter on UI thread. Is empty: " + isPlanEmptyFinal + ", Item count: " + finalPlanItems.size());
                 adapter.updateItems(finalPlanItems); // Update with the final list copy

                 if (isPlanEmptyFinal) {
                     // Avoid showing toast if context might be null during rapid transitions
                     // Toast.makeText(getContext(), "Weekly plan is empty", Toast.LENGTH_SHORT).show();
                     Log.d(TAG, "Weekly plan is empty.");
                     if (recyclerView != null) {
                         recyclerView.setVisibility(View.GONE);
                     }
                 } else {
                     if (recyclerView != null) {
                         recyclerView.setVisibility(View.VISIBLE);
                     }
                 }
             });
         } else {
              Log.e(TAG, "Cannot update adapter: Activity (" + getActivity() + ") or Adapter (" + adapter + ") is null.");
         }
    }

     private void showLoading(boolean isLoading) {
        if (progressBar != null) {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
        // Only hide recycler view when loading, let processAndDisplayPlan handle showing it
        if (recyclerView != null && isLoading) {
            recyclerView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onRecipeClick(String recipeId) {
        if (getActivity() == null) return;
        
        // Fetch full recipe details from Firestore
        FirebaseFirestore.getInstance().collection("Recipes")
            .document(recipeId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    Recipe recipe = documentSnapshot.toObject(Recipe.class);
                    if (recipe != null) {
                        Intent intent = new Intent(getActivity(), RecipeDetailActivity.class);
                        intent.putExtra("recipe", new Gson().toJson(recipe));
                        startActivity(intent);
                    } else {
                        Toast.makeText(getActivity(), "Failed to load recipe details", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getActivity(), "Recipe not found", Toast.LENGTH_SHORT).show();
                }
            })
            .addOnFailureListener(e -> {
                Toast.makeText(getActivity(), "Error loading recipe: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Error loading recipe", e);
            });
    }

    @Override
    public void onRemoveItemClick(int position) {
        if (position < 0 || position >= planItems.size()) {
            Log.e(TAG, "Invalid position received for remove click: " + position);
            return;
        }

        Object clickedItem = planItems.get(position);
        if (!(clickedItem instanceof PlannedRecipeItem)) {
            Log.e(TAG, "Remove clicked on a non-recipe item (likely a day header)");
            return; // Cannot remove a day header
        }

        PlannedRecipeItem recipeToRemove = (PlannedRecipeItem) clickedItem;
        String recipeIdToRemove = recipeToRemove.getRecipeId();

        // Find the day this recipe belongs to
        String dayToRemoveFrom = null;
        for (int i = position - 1; i >= 0; i--) {
            if (planItems.get(i) instanceof String) {
                dayToRemoveFrom = (String) planItems.get(i);
                break;
            }
        }

        // Make variables effectively final for lambda
        final String finalDayToRemoveFrom = dayToRemoveFrom;
        final String finalRecipeIdToRemove = recipeIdToRemove;
        final PlannedRecipeItem finalRecipeToRemove = recipeToRemove; // Also need this for the Toast

        if (finalDayToRemoveFrom == null || finalRecipeIdToRemove == null || finalRecipeIdToRemove.isEmpty()) {
            Log.e(TAG, "Could not determine day or recipe ID for removal. Day: " + finalDayToRemoveFrom + ", RecipeID: " + finalRecipeIdToRemove);
            Toast.makeText(getContext(), "Error removing item", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(getContext(), "Not logged in", Toast.LENGTH_SHORT).show();
            return;
        }
        String userId = currentUser.getUid();

        Log.d(TAG, "Attempting to remove recipeId: " + finalRecipeIdToRemove + " from day: " + finalDayToRemoveFrom + " for user: " + userId);

        // Perform Firestore deletion
        db.collection("WeeklyPlans").document(userId)
          .collection(finalDayToRemoveFrom).document(finalRecipeIdToRemove)
          .delete()
          .addOnSuccessListener(aVoid -> {
              Log.d(TAG, "Successfully removed recipe from plan.");
              Toast.makeText(getContext(), "'" + finalRecipeToRemove.getTitle() + "' removed from " + finalDayToRemoveFrom, Toast.LENGTH_SHORT).show(); // Use final variable

              // Refresh the list visually immediately (optimistic update)
              // Need to be careful modifying the list while iterating or relying on position after removal
              // It's safer to re-fetch the data after deletion for consistency
              Log.d(TAG, "Deletion successful, re-fetching weekly plan data.");
              List<String> daysOrder = Arrays.asList("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday");
              fetchWeeklyPlanData(userId, daysOrder); // Re-fetch data

              /* // Optimistic update (can be complex with headers)
              planItems.remove(position);
              // Check if the preceding day header needs removal
              if (position > 0 && planItems.get(position - 1) instanceof String) {
                  boolean dayHasMoreItems = false;
                  if (position < planItems.size() && !(planItems.get(position) instanceof String)) {
                      dayHasMoreItems = true;
                  }
                  if (!dayHasMoreItems) {
                      planItems.remove(position - 1);
                  }
              }
              adapter.notifyDataSetChanged();
              */
          })
          .addOnFailureListener(e -> {
              Log.w(TAG, "Error removing recipe from plan", e);
              Toast.makeText(getContext(), "Failed to remove item: " + e.getMessage(), Toast.LENGTH_SHORT).show();
          });
    }
}

package com.example.myrecipebook.activities;

import android.Manifest; // Keep for reference, but permission check is commented
import android.content.pm.PackageManager; // Keep for reference
import android.os.Bundle;
// import android.telephony.SmsManager; // Commented out
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat; // Keep for reference
import androidx.core.content.ContextCompat; // Keep for reference
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myrecipebook.R;
import com.example.myrecipebook.adapters.DelegationItemAdapter;
import com.example.myrecipebook.models.GroceryItem;
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
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class DelegationActivity extends AppCompatActivity {

    private static final String TAG = "DelegationActivity";
    private EditText editTextPhoneNumber;
    private Button buttonSendSMS;
    private RecyclerView delegationRecyclerView;
    private ProgressBar progressBar;
    private DelegationItemAdapter adapter;
    private List<GroceryItem> fullGroceryList = new ArrayList<>(); // Holds all items initially
    private List<GroceryItem> itemsToSend = new ArrayList<>(); // Holds only non-purchased items

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private CollectionReference groceryStatusRef;

    // private static final int REQUEST_SMS_PERMISSION = 1; // Commented out

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delegation);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        editTextPhoneNumber = findViewById(R.id.editTextPhoneNumber);
        buttonSendSMS = findViewById(R.id.buttonSendSMS);
        delegationRecyclerView = findViewById(R.id.delegation_recycler_view);
        progressBar = findViewById(R.id.delegation_progress_bar);

        delegationRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new DelegationItemAdapter(this, itemsToSend); // Adapter uses the filtered list
        delegationRecyclerView.setAdapter(adapter);

        if (currentUser != null) {
            groceryStatusRef = db.collection("GroceryStatus").document(currentUser.getUid()).collection("items");
            loadNonPurchasedItems();
        } else {
            Toast.makeText(this, "Please log in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Comment out SMS permission check
        // checkSmsPermission();

        buttonSendSMS.setOnClickListener(v -> {
            // String phoneNumber = editTextPhoneNumber.getText().toString().trim(); // Phone number not needed for demo send
            if (!itemsToSend.isEmpty()) {
                // Simulate sending without actual SMS
                sendListDemo();
            } else {
                 Toast.makeText(DelegationActivity.this, "No items to delegate.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Comment out SMS permission check method
    /*
    private void checkSmsPermission() {
         if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, REQUEST_SMS_PERMISSION);
        }
    }
    */

    // Fetches all grocery items and filters for non-purchased ones
    private void loadNonPurchasedItems() {
        if (currentUser == null) return;
        String userId = currentUser.getUid();
        showLoading(true);

        // --- Fetching logic similar to GroceryListActivity ---
        List<String> daysOrder = Arrays.asList("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday");
        List<Task<QuerySnapshot>> dailyPlanTasks = new ArrayList<>();
        for (String day : daysOrder) {
            dailyPlanTasks.add(db.collection("WeeklyPlans").document(userId).collection(day).get());
        }
        Task<List<QuerySnapshot>> allDailyPlansTask = Tasks.whenAllSuccess(dailyPlanTasks);

        allDailyPlansTask.addOnSuccessListener(dailySnapshots -> {
            Set<String> uniqueRecipeIds = new HashSet<>();
            for (QuerySnapshot daySnapshot : dailySnapshots) {
                for (QueryDocumentSnapshot plannedDoc : daySnapshot) {
                    uniqueRecipeIds.add(plannedDoc.getId());
                }
            }
            if (uniqueRecipeIds.isEmpty()) {
                updateAdapter(new ArrayList<>());
                showLoading(false); return;
            }
            List<Task<DocumentSnapshot>> recipeDetailTasks = new ArrayList<>();
            for (String recipeId : uniqueRecipeIds) {
                // Handle potential null or empty recipeId if data is inconsistent
                if (recipeId != null && !recipeId.isEmpty()) {
                    recipeDetailTasks.add(db.collection("Recipes").document(recipeId).get());
                } else {
                     Log.w(TAG, "Encountered null or empty recipeId in weekly plan.");
                }
            }

            // Check if there are any valid tasks before proceeding
             if (recipeDetailTasks.isEmpty()) {
                 updateAdapter(new ArrayList<>());
                 showLoading(false);
                 return;
             }


            Task<List<DocumentSnapshot>> allRecipeDetailsTask = Tasks.whenAllSuccess(recipeDetailTasks);

            allRecipeDetailsTask.addOnSuccessListener(recipeSnapshots -> {
                Set<String> uniqueIngredients = new HashSet<>();
                for (DocumentSnapshot recipeDoc : recipeSnapshots) {
                    if (recipeDoc.exists()) {
                        Recipe recipe = recipeDoc.toObject(Recipe.class);
                        if (recipe != null && recipe.getIngredients() != null) {
                             List<String> cleanedIngredients = recipe.getIngredients().stream()
                                    .map(String::trim)
                                    .filter(s -> !s.isEmpty())
                                    .collect(Collectors.toList());
                            uniqueIngredients.addAll(cleanedIngredients);
                        }
                    } else {
                         Log.w(TAG, "Planned recipe document not found: " + recipeDoc.getId());
                    }
                }
                fetchPurchasedStatusAndFilter(uniqueIngredients); // Fetch status and filter
            }).addOnFailureListener(this::handleLoadError);
        }).addOnFailureListener(this::handleLoadError);
        // --- End Fetching Logic ---
    }

     private void fetchPurchasedStatusAndFilter(Set<String> uniqueIngredients) {
        if (groceryStatusRef == null) { showLoading(false); return; }

        groceryStatusRef.get().addOnSuccessListener(purchasedSnapshot -> {
            Set<String> purchasedItems = new HashSet<>();
            if (purchasedSnapshot != null) {
                for (QueryDocumentSnapshot doc : purchasedSnapshot) {
                    purchasedItems.add(doc.getId());
                }
            }

            fullGroceryList.clear(); // Clear previous full list
            itemsToSend.clear(); // Clear previous send list
            for (String ingredient : uniqueIngredients) {
                boolean isPurchased = purchasedItems.contains(ingredient);
                GroceryItem item = new GroceryItem(ingredient, isPurchased);
                fullGroceryList.add(item); // Add to full list
                if (!isPurchased) {
                    itemsToSend.add(item); // Add only non-purchased to send list
                }
            }
            Collections.sort(itemsToSend, (o1, o2) -> o1.getName().compareToIgnoreCase(o2.getName()));
            updateAdapter(itemsToSend); // Update adapter with non-purchased items
            showLoading(false);

        }).addOnFailureListener(e -> {
            Log.e(TAG, "Error fetching purchased status", e);
            // Show empty list on error
            updateAdapter(new ArrayList<>());
            showLoading(false);
        });
    }


    private String createMessage() {
        StringBuilder messageBuilder = new StringBuilder();
        messageBuilder.append("Grocery List:\n");
        if (itemsToSend.isEmpty()) {
            messageBuilder.append("(No items needed)");
        } else {
            for (GroceryItem item : itemsToSend) {
                messageBuilder.append("- ").append(item.getName()).append("\n");
            }
        }
        messageBuilder.append("\nThank you!");
        return messageBuilder.toString();
    }

    // Simulate sending the list - show Toast and mark items as sent
    private void sendListDemo() {
        if (itemsToSend.isEmpty()) {
            Toast.makeText(this, "No items to send", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create and show confirmation toast
        Toast.makeText(this, "Successfully sent all ingredients", Toast.LENGTH_SHORT).show();

        // Mark items as sent in Firestore
        markItemsAsSent(itemsToSend);
        
        // Clear the sent items from local list
        itemsToSend.clear();
        adapter.updateList(itemsToSend);
    }

    // Marks items as sent in Firestore (special status)
    private void markItemsAsSent(List<GroceryItem> sentItems) {
        if (currentUser == null || groceryStatusRef == null || sentItems.isEmpty()) return;

        WriteBatch batch = db.batch();
        Map<String, Object> data = new HashMap<>();
        data.put("sent", true);  // Mark as sent instead of purchased
        data.put("purchased", false);  // Ensure not marked as purchased

        for (GroceryItem item : sentItems) {
            DocumentReference itemRef = groceryStatusRef.document(item.getName());
            batch.set(itemRef, data);
        }

        batch.commit()
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "Marked " + sentItems.size() + " items as sent");
                // No need to refresh here - we already cleared the local list
            })
            .addOnFailureListener(e -> {
                Log.w(TAG, "Error marking items as sent", e);
                Toast.makeText(this, "Failed to update sent status", Toast.LENGTH_SHORT).show();
            });
    }

    // Comment out the original sendSMS method
    /*
    private void sendSMS(String phoneNumber, String message) {
        try {
            SmsManager smsManager = SmsManager.getDefault();
            ArrayList<String> messages = smsManager.divideMessage(message);
            smsManager.sendMultipartTextMessage(phoneNumber, null, messages, null, null);

            Log.d(TAG, "SMS sent to: " + phoneNumber);
            Toast.makeText(this, "SMS sent successfully", Toast.LENGTH_SHORT).show();

            // Mark sent items as purchased in Firestore
            markItemsAsPurchased(itemsToSend);

        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Failed to send SMS: " + e.getMessage());
            Toast.makeText(this, "Failed to send SMS", Toast.LENGTH_SHORT).show();
        }
    }
    */

    // Marks the sent items as purchased in Firestore
    private void markItemsAsPurchased(List<GroceryItem> sentItems) {
        if (currentUser == null || groceryStatusRef == null || sentItems.isEmpty()) return;

        WriteBatch batch = db.batch();
        Map<String, Object> data = new HashMap<>();
        data.put("purchased", true);

        for (GroceryItem item : sentItems) {
            DocumentReference itemRef = groceryStatusRef.document(item.getName());
            batch.set(itemRef, data); // Use set to create or overwrite
        }

        batch.commit()
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "Successfully marked " + sentItems.size() + " items as purchased after sending SMS.");
                // Refresh the list to show it's empty now
                loadNonPurchasedItems();
            })
            .addOnFailureListener(e -> Log.w(TAG, "Error marking items as purchased after sending SMS", e));
    }


    // Comment out SMS permission result handling
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        /* // SMS permission handling commented out
        if (requestCode == REQUEST_SMS_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "SMS Permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "SMS Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
        */
         // Keep handling for other permissions if added later
    }

     private void updateAdapter(List<GroceryItem> items) {
        Log.d(TAG, "Updating adapter with " + items.size() + " items");
        for (GroceryItem item : items) {
            Log.d(TAG, "Item: " + item.getName() + " (Purchased: " + item.isPurchased() + ")");
        }
        adapter.updateList(items);
        if (items.isEmpty()) {
            Log.d(TAG, "No items to delegate");
        }
    }

    private void showLoading(boolean isLoading) {
        if (progressBar != null) progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        if (delegationRecyclerView != null) delegationRecyclerView.setVisibility(isLoading ? View.GONE : View.VISIBLE);
    }

    private void handleLoadError(Exception e) {
         Log.e(TAG, "Error loading grocery data", e);
         Toast.makeText(this, "Error loading data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
         showLoading(false);
    }
}

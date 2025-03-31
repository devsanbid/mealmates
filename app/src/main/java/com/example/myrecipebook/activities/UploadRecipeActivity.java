package com.example.myrecipebook.activities;

import android.app.Activity; // Import Activity
import android.content.Context; // Import Context
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log; // Import Log
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myrecipebook.R;
import com.example.myrecipebook.models.Recipe;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser; // Import FirebaseUser
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UploadRecipeActivity extends AppCompatActivity {

    private static final String TAG = "UploadRecipeActivity"; // Define TAG for logging

    private EditText recipeTitle, recipeDescription, ingredientInput, stepInput;
    private Spinner categorySpinner;
    private NumberPicker prepTimePicker, servesPicker;
    private Button addIngredientButton, addStepButton, publishButton, buttonSelectImage;
    private ImageView recipeImagePreview;
    private LinearLayout ingredientList, stepList;
    private List<String> ingredients = new ArrayList<>();
    private List<String> steps = new ArrayList<>();
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private CollectionReference recipesCollection;
    private Uri selectedImageUri;
    private String savedImageFilePath;
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private View rootView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        recipesCollection = db.collection("Recipes");

        recipeTitle = findViewById(R.id.recipeTitle);
        recipeImagePreview = findViewById(R.id.recipe_image_preview);
        buttonSelectImage = findViewById(R.id.button_select_image);
        recipeDescription = findViewById(R.id.recipeDescription);
        categorySpinner = findViewById(R.id.categorySpinner);
        ingredientInput = findViewById(R.id.ingredientInput);
        addIngredientButton = findViewById(R.id.addIngredientButton);
        ingredientList = findViewById(R.id.ingredientList);
        stepInput = findViewById(R.id.stepInput);
        addStepButton = findViewById(R.id.addStepButton);
        stepList = findViewById(R.id.stepList);
        publishButton = findViewById(R.id.publishButton);
        prepTimePicker = findViewById(R.id.prepTimePicker);
        servesPicker = findViewById(R.id.servesPicker);
        rootView = findViewById(android.R.id.content);

        setupCategorySpinner();
        setupNumberPickers();
        setupImagePicker();
        setupButtonClickListeners();
    }

    private void setupCategorySpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.recipe_categories, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);
    }

    private void setupNumberPickers() {
        prepTimePicker.setMinValue(1);
        prepTimePicker.setMaxValue(300);
        servesPicker.setMinValue(1);
        servesPicker.setMaxValue(20);
    }

    private void setupImagePicker() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        if (selectedImageUri != null) {
                            recipeImagePreview.setImageURI(selectedImageUri);
                            Log.d(TAG, "Image selected: " + selectedImageUri.toString());
                        } else {
                            Log.e(TAG, "Selected image URI is null");
                            Toast.makeText(this, "Failed to get image URI", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.d(TAG, "Image selection cancelled or failed");
                    }
                });
    }

    private void setupButtonClickListeners() {
        buttonSelectImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            imagePickerLauncher.launch(intent);
        });

        addIngredientButton.setOnClickListener(v -> {
            String ingredient = ingredientInput.getText().toString().trim();
            if (!ingredient.isEmpty()) {
                ingredients.add(ingredient);
                ingredientInput.setText("");
                updateIngredientList();
            } else {
                ingredientInput.setError("Ingredient cannot be empty");
            }
        });

        addStepButton.setOnClickListener(v -> {
            String step = stepInput.getText().toString().trim();
            if (!step.isEmpty()) {
                steps.add(step);
                stepInput.setText("");
                updateStepList();
            } else {
                stepInput.setError("Step cannot be empty");
            }
        });

        publishButton.setOnClickListener(v -> {
            if (validateInput()) {
                saveImageLocallyAndUploadData();
            }
        });
    }


    private boolean validateInput() {
        // (Validation logic remains the same)
        String title = recipeTitle.getText().toString().trim();
        String description = recipeDescription.getText().toString().trim();

        if (title.isEmpty()) {
            recipeTitle.setError("Title is required");
            return false;
        }
        if (description.isEmpty()) {
            recipeDescription.setError("Description is required");
            return false;
        }
        if (ingredients.isEmpty()) {
            Toast.makeText(this, "Add at least one ingredient", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (steps.isEmpty()) {
            Toast.makeText(this, "Add at least one step", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }


    private void updateIngredientList() {
        // (updateIngredientList logic remains the same)
        ingredientList.removeAllViews();
        for (int i = 0; i < ingredients.size(); i++) {
            final int index = i;
            View ingredientView = getLayoutInflater().inflate(R.layout.list_item_editable, ingredientList, false);
            TextView textView = ingredientView.findViewById(R.id.item_text);
            ImageView deleteButton = ingredientView.findViewById(R.id.delete_button);
            textView.setText(ingredients.get(index));
            deleteButton.setOnClickListener(v -> {
                ingredients.remove(index);
                updateIngredientList();
            });
            ingredientList.addView(ingredientView);
        }
    }

    private void updateStepList() {
        // (updateStepList logic remains the same)
         stepList.removeAllViews();
        for (int i = 0; i < steps.size(); i++) {
             final int index = i;
            View stepView = getLayoutInflater().inflate(R.layout.list_item_editable, stepList, false);
            TextView textView = stepView.findViewById(R.id.item_text);
            ImageView deleteButton = stepView.findViewById(R.id.delete_button);
            textView.setText((index + 1) + ". " + steps.get(index));
            deleteButton.setOnClickListener(v -> {
                steps.remove(index);
                updateStepList();
            });
            stepList.addView(stepView);
        }
    }

    private void saveImageLocallyAndUploadData() {
        // (saveImageLocallyAndUploadData logic remains the same)
         if (selectedImageUri != null) {
            File internalDir = getApplicationContext().getDir("recipe_images", Context.MODE_PRIVATE);
            if (!internalDir.exists()) {
                internalDir.mkdirs();
            }
            String fileName = "recipe_" + System.currentTimeMillis() + ".jpg";
            File destinationFile = new File(internalDir, fileName);
            savedImageFilePath = destinationFile.getAbsolutePath();

            try (InputStream inputStream = getContentResolver().openInputStream(selectedImageUri);
                 OutputStream outputStream = new FileOutputStream(destinationFile)) {
                if (inputStream == null) throw new IOException("Unable to open input stream");
                byte[] buffer = new byte[1024];
                int length;
                while ((length = inputStream.read(buffer)) > 0) outputStream.write(buffer, 0, length);
                Log.d(TAG, "Image saved locally to: " + savedImageFilePath);
                uploadRecipeDataFirestore(savedImageFilePath);
            } catch (IOException e) {
                Log.e(TAG, "Error saving image locally", e);
                Toast.makeText(this, "Error saving image: " + e.getMessage(), Toast.LENGTH_LONG).show();
                uploadRecipeDataFirestore(null); // Upload without image path on error
            }
        } else {
            Log.d(TAG, "No image selected, uploading data without image path.");
            uploadRecipeDataFirestore(null);
        }
    }

    private void uploadRecipeDataFirestore(@Nullable String localImagePath) {
        // (uploadRecipeDataFirestore logic remains the same)
         String title = recipeTitle.getText().toString().trim();
        String description = recipeDescription.getText().toString().trim();
        String selectedCategory = categorySpinner.getSelectedItem().toString();
        long prepTime = prepTimePicker.getValue();
        int serves = servesPicker.getValue();

        if (currentUser == null) {
            Toast.makeText(this, "Error: User not logged in.", Toast.LENGTH_SHORT).show();
            publishButton.setEnabled(true);
            return;
        }
        String userId = currentUser.getUid();

        Toast.makeText(this, "Publishing Recipe...", Toast.LENGTH_SHORT).show();
        publishButton.setEnabled(false);

        Recipe recipe = new Recipe(
                null, title, description,
                prepTime + " minutes", serves + " servings",
                localImagePath != null ? localImagePath : "",
                false, prepTime, selectedCategory, userId,
                new ArrayList<>(ingredients), new ArrayList<>(steps)
        );

        recipesCollection.add(recipe)
            .addOnSuccessListener(documentReference -> {
                publishButton.setEnabled(true);
                Toast.makeText(UploadRecipeActivity.this, "Recipe Published!", Toast.LENGTH_LONG).show();
                clearForm();
                 Intent intent = new Intent(UploadRecipeActivity.this, AllRecipesActivity.class);
                 intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                 startActivity(intent);
                 finish();
            })
            .addOnFailureListener(e -> {
                 publishButton.setEnabled(true);
                 Toast.makeText(UploadRecipeActivity.this, "Failed to publish: " + e.getMessage(), Toast.LENGTH_LONG).show();
            });
    }

    private void clearForm() {
        // (clearForm logic remains the same)
         recipeTitle.setText("");
        recipeDescription.setText("");
        ingredientInput.setText("");
        stepInput.setText("");
        categorySpinner.setSelection(0);
        recipeImagePreview.setImageResource(R.drawable.ic_baseline_image_24);
        selectedImageUri = null;
        savedImageFilePath = null;
        ingredients.clear();
        steps.clear();
        updateIngredientList();
        updateStepList();
        prepTimePicker.setValue(prepTimePicker.getMinValue());
        servesPicker.setValue(servesPicker.getMinValue());
    }
}

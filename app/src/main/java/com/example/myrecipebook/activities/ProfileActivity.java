package com.example.myrecipebook.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity; // Import AppCompatActivity

import com.example.myrecipebook.R;
import com.example.myrecipebook.models.UserData; // Assuming UserData model exists
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ProfileActivity extends AppCompatActivity { // Extend AppCompatActivity

    private EditText profileNameEditText;
    private TextView profileEmailTextView;
    private EditText profileNewPasswordEditText, profileConfirmPasswordEditText;
    private Button saveProfileButton, changePasswordButton;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private DocumentReference userDocRef;

    private static final String TAG = "ProfileActivity"; // For logging

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set the content view using the new layout
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            // No user logged in, handle appropriately (e.g., redirect to login)
            Toast.makeText(this, "No user logged in.", Toast.LENGTH_SHORT).show();
            finish(); // Close the activity
            return;
        }

        // Get reference to the user's document in Firestore
        userDocRef = db.collection("Users").document(currentUser.getUid());

        // Initialize UI elements
        profileNameEditText = findViewById(R.id.profile_name_edittext);
        profileEmailTextView = findViewById(R.id.profile_email_textview);
        profileNewPasswordEditText = findViewById(R.id.profile_new_password_edittext);
        profileConfirmPasswordEditText = findViewById(R.id.profile_confirm_password_edittext);
        saveProfileButton = findViewById(R.id.profile_save_button);
        changePasswordButton = findViewById(R.id.profile_change_password_button);

        // Load user data from Firestore
        loadUserData();

        // Set up button click listeners
        saveProfileButton.setOnClickListener(v -> saveProfileChanges());
        changePasswordButton.setOnClickListener(v -> changePassword());
    }

    private void loadUserData() {
        // Display email from Firebase Auth
        if (currentUser != null && currentUser.getEmail() != null) {
            profileEmailTextView.setText(currentUser.getEmail());
        }

        // Fetch name from Firestore
        userDocRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                UserData userData = documentSnapshot.toObject(UserData.class);
                if (userData != null && userData.getName() != null) {
                    profileNameEditText.setText(userData.getName());
                }
            } else {
                // Handle case where user document doesn't exist in Firestore (shouldn't happen if registration worked)
                Toast.makeText(this, "User data not found in database.", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to load user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void saveProfileChanges() {
        String newName = profileNameEditText.getText().toString().trim();

        if (newName.isEmpty()) {
            profileNameEditText.setError("Name cannot be empty");
            return;
        }

        // Update only the name field in Firestore
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", newName); // Use the exact field name as in your UserData model and Firestore

        userDocRef.update(updates)
                .addOnSuccessListener(aVoid -> Toast.makeText(ProfileActivity.this, "Name updated successfully", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(ProfileActivity.this, "Failed to update name: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void changePassword() {
        String newPassword = profileNewPasswordEditText.getText().toString().trim();
        String confirmPassword = profileConfirmPasswordEditText.getText().toString().trim();

        if (newPassword.isEmpty()) {
            profileNewPasswordEditText.setError("New password cannot be empty");
            return;
        }
        if (newPassword.length() < 6) { // Enforce minimum password length (Firebase default is 6)
             profileNewPasswordEditText.setError("Password must be at least 6 characters");
             return;
        }
        if (!newPassword.equals(confirmPassword)) {
            profileConfirmPasswordEditText.setError("Passwords do not match");
            return;
        }

        if (currentUser != null) {
            currentUser.updatePassword(newPassword)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(ProfileActivity.this, "Password updated successfully", Toast.LENGTH_SHORT).show();
                            // Clear password fields after success
                            profileNewPasswordEditText.setText("");
                            profileConfirmPasswordEditText.setText("");
                        } else {
                            Toast.makeText(ProfileActivity.this, "Failed to update password: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_LONG).show();
                            // Consider requiring re-authentication if update fails due to security reasons
                        }
                    });
        }
    }
}

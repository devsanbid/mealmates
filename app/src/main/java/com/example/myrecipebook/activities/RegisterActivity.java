package com.example.myrecipebook.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myrecipebook.R;
import com.example.myrecipebook.models.UserData;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser; // Import FirebaseUser
import com.google.firebase.firestore.CollectionReference; // Firestore import
import com.google.firebase.firestore.DocumentReference; // Firestore import
import com.google.firebase.firestore.FirebaseFirestore; // Firestore import

import java.util.Objects;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private EditText signupEmail, signupPassword;
    private EditText signupName, signupUsername;

    private Button signupButton;
    private TextView loginRedirectText;

    // FirebaseDatabase database; // Remove Realtime Database
    // DatabaseReference reference; // Remove Realtime Database
    private FirebaseFirestore db; // Firestore instance
    private CollectionReference usersCollection; // Firestore collection reference

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_register);

        auth = FirebaseAuth.getInstance();

        signupEmail = findViewById(R.id.register_email);
        signupPassword = findViewById(R.id.register_password);
        signupButton = findViewById(R.id.register_button);
        loginRedirectText = findViewById(R.id.not_yet_register);
        signupName = findViewById(R.id.register_name);
        signupUsername = findViewById(R.id.register_username);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();
        usersCollection = db.collection("Users"); // Get reference to "Users" collection (case-sensitive)


        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String name = signupName.getText().toString();
                String email = signupEmail.getText().toString();
                String username = signupUsername.getText().toString();
                String password = signupPassword.getText().toString();

                if (name.isEmpty()) {
                    signupName.setError("Name cannot be empty");
                }
                if (email.isEmpty()) {
                    signupEmail.setError("Email cannot be empty");
                }
                if (password.isEmpty()) {
                    signupPassword.setError("Password cannot be empty");
                } else {

                    auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            if (task.isSuccessful()) {
                                FirebaseUser firebaseUser = auth.getCurrentUser();
                                if (firebaseUser != null) {
                                    String userId = firebaseUser.getUid();
                                    UserData helperClass = new UserData(name, email, username, ""); // Assuming profileImage is initially empty

                                    // Save to Cloud Firestore
                                    DocumentReference userDocRef = usersCollection.document(userId); // Create document reference with UID
                                    userDocRef.set(helperClass) // Use set() to save the UserData object
                                        .addOnCompleteListener(saveTask -> {
                                            if (saveTask.isSuccessful()) {
                                                Toast.makeText(RegisterActivity.this, "SignUp Successful & Data Saved to Firestore", Toast.LENGTH_SHORT).show();
                                                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                                                finish(); // Finish RegisterActivity so user can't go back
                                            } else {
                                                // Data saving failed! Log or show error
                                                Toast.makeText(RegisterActivity.this, "SignUp Successful BUT Firestore Save Failed: " + saveTask.getException().getMessage(), Toast.LENGTH_LONG).show();
                                                // Optional: Delete the authenticated user if saving data is critical
                                                // firebaseUser.delete();
                                            }
                                        });
                                } else {
                                     // Should not happen if task is successful, but good practice to check
                                     Toast.makeText(RegisterActivity.this, "SignUp Successful BUT User is null", Toast.LENGTH_LONG).show();
                                }
                            } else {
                                Toast.makeText(RegisterActivity.this, "SignUp Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });

        loginRedirectText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                finish();
            }
        });
    }
}

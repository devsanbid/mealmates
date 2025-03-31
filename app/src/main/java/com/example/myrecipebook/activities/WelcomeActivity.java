package com.example.myrecipebook.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.example.myrecipebook.MainActivity;
import com.example.myrecipebook.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser ;

public class WelcomeActivity extends AppCompatActivity {

    private FirebaseUser  user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser ();

        if (user != null) {
            startActivity(new Intent(WelcomeActivity.this, MainActivity.class));
            finish();
        }

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_welcome);
        RestApiThread restApiThread = new RestApiThread();
        restApiThread.start();
    }

    public void register(View view) {
        startActivity(new Intent(WelcomeActivity.this, RegisterActivity.class));
    }

    public void login(View view) {
        startActivity(new Intent(WelcomeActivity.this, LoginActivity.class));
    }

    class RestApiThread extends Thread {
        @Override
        public void run() {
            // Simulate a network operation or API call
            try {
                // Simulating a delay for the API call
                Thread.sleep(2000); // Simulate a 2-second delay

                // Here you can perform your API call or any background operation
                // For example, you can use HttpURLConnection or Retrofit to make a network request

                // After the operation, you might want to update the UI
                // Make sure to run UI updates on the main thread
                runOnUiThread(() -> {
                    // Update UI elements here if needed
                    // For example, you can show a Toast or update a TextView
                });

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
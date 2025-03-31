package com.example.myrecipebook.activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.myrecipebook.R;
import com.example.myrecipebook.ui.dailymeal.DailyMealFragment;

public class DailyMealPlanActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.daily_meal_plan);

        // Check if the fragment is already added, if not, add it
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new DailyMealFragment())
                    .commit();
        }
    }
}
package com.example.myrecipebook.activities;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.myrecipebook.R;
import com.example.myrecipebook.models.Recipe;
import com.google.gson.Gson;
import java.util.List;

public class RecipeDetailActivity extends AppCompatActivity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        // Get recipe data from intent
        String recipeJson = getIntent().getStringExtra("recipe");
        Recipe recipe = new Gson().fromJson(recipeJson, Recipe.class);

        // Initialize views
        ImageView recipeImage = findViewById(R.id.recipe_image);
        TextView title = findViewById(R.id.recipe_title);
        TextView description = findViewById(R.id.recipe_description);
        TextView duration = findViewById(R.id.recipe_duration);
        TextView servings = findViewById(R.id.recipe_servings);
        TextView ingredients = findViewById(R.id.recipe_ingredients);
        TextView steps = findViewById(R.id.recipe_steps);

        // Set recipe data
        title.setText(recipe.getTitle());
        description.setText(recipe.getDescription());
        duration.setText(recipe.getDuration());
        servings.setText(recipe.getServings());
        
        // Format ingredients and steps
        ingredients.setText(formatList(recipe.getIngredients()));
        steps.setText(formatList(recipe.getSteps()));
    }

    private String formatList(List<String> items) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < items.size(); i++) {
            sb.append(i + 1).append(". ").append(items.get(i)).append("\n");
        }
        return sb.toString();
    }
}

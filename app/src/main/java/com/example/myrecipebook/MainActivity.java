package com.example.myrecipebook;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.myrecipebook.activities.AllRecipesActivity;
import com.example.myrecipebook.activities.DelegationActivity;
import com.example.myrecipebook.activities.GroceryListActivity;
import com.example.myrecipebook.activities.HomeActivity;
import com.example.myrecipebook.activities.MapDetailActivity;
import com.example.myrecipebook.activities.MyRecipesActivity;
import com.example.myrecipebook.activities.ProfileActivity; // Import ProfileActivity
import com.example.myrecipebook.activities.SettingsActivity;
import com.example.myrecipebook.activities.UploadRecipeActivity;
import com.example.myrecipebook.activities.DailyMealPlanActivity;
import com.example.myrecipebook.activities.WelcomeActivity;
import com.example.myrecipebook.databinding.ActivityMainBinding;
import com.example.myrecipebook.models.UserData;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;
    private Button logoutButton;

    private ImageView profileImage;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    void checkLogin() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth == null || auth.getUid() == null) {
            finish();
        }
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        logout();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkLogin();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        checkLogin();
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        checkLogin();

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarMain.toolbar);

        // Set up the drawer layout and navigation view
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        // Set up the app bar configuration for navigation
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_categories, R.id.nav_my_recipes, R.id.nav_profile, R.id.nav_upload_recipe, R.id.nav_map)
                .setOpenableLayout(drawerLayout)
                .build();

        // Set up the navigation controller
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        // Initialize profile image
        // profileImage reference removed since we deleted the userimage view
        UpdateUserData();

        // Logout button setup
        logoutButton = findViewById(R.id.logout_button);
        logoutButton.setOnClickListener(v -> logout());

        // Add item selection listener to handle menu item clicks
        navigationView.setNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.nav_home:
                    navController.navigate(R.id.nav_home);
                    break;

                case R.id.nav_all_recipes:
                    startActivity(new Intent(MainActivity.this, AllRecipesActivity.class));
                    break;
                case R.id.nav_my_recipes:
                    startActivity(new Intent(MainActivity.this, MyRecipesActivity.class));
                    break;
                case R.id.nav_upload_recipe:
                    startActivity(new Intent(MainActivity.this, UploadRecipeActivity.class));
                    break;
                case R.id.nav_weekly_plan:
                    startActivity(new Intent(MainActivity.this, DailyMealPlanActivity.class));
                    break;
                case R.id.nav_grocery_list:
                    startActivity(new Intent(MainActivity.this, GroceryListActivity.class));
                    break;
                case R.id.nav_delegation:
                    startActivity(new Intent(MainActivity.this, DelegationActivity.class));
                    break;
                case R.id.nav_store_maps:
                    // Let navigation component handle this
                    navController.navigate(R.id.nav_map);
                    break;
                case R.id.nav_settings:
                    startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                    break;
                // Add case for Profile
                case R.id.nav_profile:
                    startActivity(new Intent(MainActivity.this, ProfileActivity.class));
                    break;
                default:
                    // Optional: Handle default case or log unexpected item ID
                    break;
            }

            // Close the drawer after selection
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }

    public void logout() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Logout");
        builder.setMessage("Are you sure you want to logout?");

        builder.setPositiveButton("Yes", (dialog, which) -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(MainActivity.this, WelcomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        });

        builder.setNegativeButton("No", (dialog, which) -> {
            // Do nothing
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    void UpdateUserData() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getUid() != null) {
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference usersRef = database.getReference("users");

            usersRef.child(auth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    UserData profileData = dataSnapshot.getValue(UserData.class);
                    if (profileData != null && profileImage != null) {
                        if (!profileData.getProfileImage().isEmpty()) {
                            Picasso.get().load(profileData.getProfileImage()).into(profileImage);
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    System.out.println("The read failed: " + databaseError.getCode());
                }
            });
        }
    }
}

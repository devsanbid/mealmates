package com.example.myrecipebook.models;

// Simple model to represent a recipe within the weekly plan
public class PlannedRecipeItem {
    private String recipeId;
    private String title;
    private String imageUrl;

    // Required empty constructor for Firestore
    public PlannedRecipeItem() {}

    public PlannedRecipeItem(String recipeId, String title, String imageUrl) {
        this.recipeId = recipeId;
        this.title = title;
        this.imageUrl = imageUrl;
    }

    // Getters
    public String getRecipeId() {
        return recipeId;
    }

    public String getTitle() {
        return title;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    // Setters (optional, but good practice)
    public void setRecipeId(String recipeId) {
        this.recipeId = recipeId;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}

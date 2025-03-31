package com.example.myrecipebook.models;

import java.util.List;

public class Recipe {
    private String id;
    private String title;
    private String description;
    private String duration; // e.g., "30 minutes"
    private String servings;
    private String imageUrl; // Stores local file path
    private boolean purchased; // Used for grocery list tracking? (Consider if needed here)
    private long prepTime;
    private String category;
    private String userId; // Added field to store creator's user ID
    private List<String> ingredients;
    private List<String> steps;

    // No-argument constructor required for Firestore
    public Recipe() {
    }

    // Constructor with parameters - Added category and userId
    public Recipe(String id, String title, String description, String duration, String servings, String imageUrl, boolean purchased, long prepTime, String category, String userId, List<String> ingredients, List<String> steps) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.duration = duration;
        this.servings = servings;
        this.imageUrl = imageUrl;
        this.purchased = purchased; // Initialize purchased status
        this.prepTime = prepTime;
        this.category = category;
        this.userId = userId; // Assign userId
        this.ingredients = ingredients;
        this.steps = steps;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getDuration() {
        return duration;
    }

    public String getServings() {
        return servings;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public boolean isPurchased() {
        return purchased;
    }

    public long getPrepTime() {
        return prepTime;
    }

    public String getCategory() {
        return category;
    }

     public String getUserId() { // Getter for userId
        return userId;
    }

    public List<String> getIngredients() {
        return ingredients;
    }

    public List<String> getSteps() {
        return steps;
    }

    // Setters
    public void setId(String id) {
        this.id = id;
    }

     public void setUserId(String userId) { // Setter for userId
        this.userId = userId;
    }

    public void setPurchased(boolean purchased) {
        this.purchased = purchased;
    }
    // Add other setters if needed
}

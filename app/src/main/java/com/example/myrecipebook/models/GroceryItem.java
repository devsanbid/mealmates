package com.example.myrecipebook.models;

public class GroceryItem {
    private String name;
    private boolean purchased;
    // Optional: Add fields like quantity, unit if needed later

    // Required empty constructor for potential Firestore use later if saving state
    public GroceryItem() {}

    public GroceryItem(String name, boolean purchased) {
        this.name = name;
        this.purchased = purchased;
    }

    // Getters
    public String getName() {
        return name;
    }

    public boolean isPurchased() {
        return purchased;
    }

    // Setters
    public void setName(String name) {
        this.name = name;
    }

    public void setPurchased(boolean purchased) {
        this.purchased = purchased;
    }

    // Override equals and hashCode based on name for Set operations
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GroceryItem that = (GroceryItem) o;
        return name != null ? name.equals(that.name) : that.name == null;
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }
}

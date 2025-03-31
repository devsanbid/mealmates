package com.example.myrecipebook.models;

public class DailyMealItem {
    private int imageResource;
    private String title;
    private String description;
    private String discount;

    public DailyMealItem(int imageResource, String title, String description, String discount) {
        this.imageResource = imageResource;
        this.title = title;
        this.description = description;
        this.discount = discount;
    }

    // Getters
    public int getImageResource() { return imageResource; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getDiscount() { return discount; }
}
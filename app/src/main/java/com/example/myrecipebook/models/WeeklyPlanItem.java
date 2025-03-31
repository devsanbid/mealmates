package com.example.myrecipebook.models;

public class WeeklyPlanItem {

    private String day;
    private String meals;

    public WeeklyPlanItem(String day, String meals) {
        this.day = day;
        this.meals = meals;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public String getMeals() {
        return meals;
    }

    public void setMeals(String meals) {
        this.meals = meals;
    }
}

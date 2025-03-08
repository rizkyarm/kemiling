package com.android.kemilingcom;

public class product {
    private int id;
    private String title;
    private int price;
    private String location;
    private float rating;
    private String imageUrl;

    // Constructor
    public product(int id, String title, int price, String location, float rating, String imageUrl) {
        this.id = id;
        this.title = title;
        this.price = price;
        this.location = location;
        this.rating = rating;
        this.imageUrl = imageUrl;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public int getPrice() {
        return price;
    }

    public String getLocation() {
        return location;
    }

    public float getRating() {
        return rating;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}
